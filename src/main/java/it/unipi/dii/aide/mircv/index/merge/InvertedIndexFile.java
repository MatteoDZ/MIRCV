package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import org.javatuples.Pair;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class InvertedIndexFile {
    private final FileChannel fc;
    private MappedByteBuffer mbb;
    public final FrequencyFile frequencyWriter;
    public final DocIdFile docIdWriter;
    private final LFUCache<Pair<Long, Integer>, Integer> lfuCache = new LFUCache<>(Configuration.INVERTED_INDEX_CACHE_SIZE);

    // Constructor
    public InvertedIndexFile(String pathInvertedIndex, String pathDocIds, String pathFrequencies, int blockSize) {
        try {
            // Open file channel for reading and writing
            fc = FileChannel.open(Paths.get(pathInvertedIndex),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + pathInvertedIndex + " file.");
        }
        // Initialize FrequencyFileWriter and DocIdFileWriter
        frequencyWriter = new FrequencyFile(pathFrequencies, blockSize);
        docIdWriter = new DocIdFile(pathDocIds, blockSize);
    }

    /**
     * Write document IDs and frequencies to the file and return the offset of the term
     *
     * @param docIds list of document IDs
     * @param freqs list of frequencies
     * @param compress flag indicating whether to compress the data
     * @return the offset of the term
     * @throws IOException if an I/O error occurs
     */
    public Long write(List<Integer> docIds, List<Integer> freqs, boolean compress) throws IOException {
        Long termOffset = fc.size();

        // Write frequencies and document IDs to their respective files
        List<Long> frequenciesOffsets = frequencyWriter.writeFrequencies(freqs, compress);
        List<Long> docIdsOffsets = docIdWriter.writeDocIds(docIds, compress);
        List<Integer> termUpperBounds = docIdWriter.getTermUpperBounds();

        // Write metadata to the buffer
        BinaryFile.writeShortToBuffer(fc, (short) termUpperBounds.size());
        BinaryFile.writeIntListToBuffer(fc, termUpperBounds);
        BinaryFile.writeIntToBuffer(fc, -1);
        BinaryFile.writeLongListToBuffer(fc, docIdsOffsets);
        BinaryFile.writeLongToBuffer(fc, -1);
        BinaryFile.writeLongListToBuffer(fc, frequenciesOffsets);
        BinaryFile.writeLongToBuffer(fc, -1);

        // System.out.println(term + " " + docIdsOffsets + " " + frequenciesOffsets + " " + termUpperBounds);

        return termOffset;
    }

    /**
     * Retrieves document IDs from the file starting from the specified offset, with optional compression.
     * @param offset the starting position in the file
     * @param compress true if the document IDs should be compressed, false otherwise
     * @return a list of document IDs
     * @throws IOException if an I/O error occurs
     */
    public List<Integer> getDocIds(Long offset, boolean compress) throws IOException {
        long start_search_time = System.currentTimeMillis();
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset, fc.size() - offset);
        mbb.getShort();
        while (mbb.getInt() != -1); //skip termUpperBounds
        List<Long> docIdsOffsets = new ArrayList<>();
        long docIdOffset;
        while ((docIdOffset = mbb.getLong()) != -1) {
            docIdsOffsets.add(docIdOffset);
        }
        long end_search_time = System.currentTimeMillis();

        // System.out.println("Number of offsets: " + docIdsOffsets.size());
        // System.out.println("Number of docIds: " + docIdWriter.readDocIds(docIdsOffsets, compress).size());

        System.out.println("TEMPO getDocIds " + (end_search_time-start_search_time) + " ms");
        return docIdWriter.readDocIds(docIdsOffsets, compress);
    }

    /**
     * Get frequency of a document in a given block.
     * @param offset the offset in the file
     * @param docId the ID of the document
     * @param compress whether compression is enabled
     * @return the frequency of the document
     * @throws IOException if an I/O error occurs
     */
    // Get frequency of a document in a given block
    public Integer getFreq(Long offset, int docId, boolean compress) throws IOException {
        short numBlocks = BinaryFile.readShortFromBuffer(fc, offset);
        //System.out.println("numBlocks " + numBlocks);

        int blockIndex = findBlockIndex(offset, numBlocks, docId);

        if(blockIndex == -1) return 0;

        //leggiamo i docId
        List<Integer> docIdsBlock = docIdWriter.readDocIdsBlock(getOffsetsDocIds(offset, numBlocks, blockIndex).get(0),
                getOffsetsDocIds(offset, numBlocks, blockIndex).get(1),compress);

        // System.out.println("InvertedIndexWriter DocIds"  + docIdsBlock);

        //leggiamo le frequenze
        List<Short> freqsBlock = frequencyWriter.readFreqsBlock(getOffsetsFreqs(offset, numBlocks, blockIndex).get(0),
                getOffsetsFreqs(offset, numBlocks, blockIndex).get(1),compress);

        int indexDocId = docIdsBlock.indexOf(docId);
        // System.out.println("InvertedIndexWriter Freqs"  + freqsBlock);
        // System.out.println("InvertedIndexWriter Freqs Index"  + indexDocId);
        // if(indexDocId != -1)  System.out.println("InvertedIndexWriter Freq "  + freqsBlock.get(indexDocId));
        return (indexDocId == -1) ? 0 : (int)freqsBlock.get(indexDocId);
    }

    public Integer getFreqCache(Long offset, int docId, boolean compress) throws IOException {
        Pair<Long, Integer> pair = new Pair<>(offset, docId);
        if (lfuCache.containsKey(pair)) {
            return lfuCache.get(pair);
        }
        Integer freq = getFreq(offset, docId, compress);
        if (freq == -1) {
            return -1;
        }
        lfuCache.put(pair, freq);
        return freq;
    }

    /**
     * Retrieves the start and end offset of a document ID based on the given offset, number of blocks, and block index.
     *
     * @param  offset              the starting offset
     * @param  numBlocchi          the number of the block
     * @param  indiceBloccoCercato the index of the block to search for
     * @return                     a list containing the retrieved start and end offset of a document ID
     * @throws IOException         if there is an error while performing I/O operations
     */
    protected List<Long> getOffsetsDocIds(Long offset, int numBlocchi, int indiceBloccoCercato) throws IOException {
        mbb=fc.map(FileChannel.MapMode.READ_ONLY,offset+2L+ 4L * (numBlocchi+1)+ 8L *indiceBloccoCercato,
                offset+2L+ 4L * (numBlocchi+1)+ 8L *(indiceBloccoCercato+1));
        return List.of(mbb.getLong(),mbb.getLong());
    }

    /**
     * Retrieves the start and end offset of a frequency based on the given offset, number of blocks, and block index.
     *
     * @param  offset              the starting offset
     * @param  numBlocchi          the number of the block
     * @param  indiceBloccoCercato the index of the block to search for
     * @return                     a list containing the retrieved start and end offset of a frequency
     * @throws IOException         if there is an error while performing I/O operations
     */
    protected List<Long> getOffsetsFreqs(Long offset, int numBlocchi, int indiceBloccoCercato) throws IOException {
        mbb=fc.map(FileChannel.MapMode.READ_ONLY,offset+2L+ (4L *(numBlocchi+1)) + (8L * (numBlocchi+1)) + (8L *indiceBloccoCercato) +8,
                offset+2L+ (4L * (numBlocchi+1)) + (8L * (numBlocchi+1)) + (8L *(indiceBloccoCercato+1)) +8);
        return List.of(mbb.getLong(),mbb.getLong());
    }


    /**
     * Find the index block for a given document ID.
     *
     * @param offset the offset in the file
     * @param numBlocks the number of blocks
     * @param docId the document ID to search for
     * @return the index of the block, or -1 if not found
     * @throws IOException if an I/O error occurs
     */
    protected int findBlockIndex(Long offset, short numBlocks, int docId) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset + 2L, numBlocks * 4);
        for (int i = 0; i < numBlocks; i++) {
            if (mbb.getInt() >= docId) {
                return i;
            }
        }
        return -1;
    }

    public LFUCache<Pair<Long, Integer>, Integer> getLfuCache() {
        return lfuCache;
    }

}