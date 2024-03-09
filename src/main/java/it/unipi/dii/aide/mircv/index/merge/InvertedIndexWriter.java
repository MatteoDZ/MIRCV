package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.config.Configuration;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.unipi.dii.aide.mircv.index.merge.UtilsWriter.*;

public class InvertedIndexWriter {
    private final FileChannel fc;
    private MappedByteBuffer mbb;
    public final FrequencyFileWriter frequencyWriter;
    public final DocIdFileWriter docIdWriter;

    // Constructor
    public InvertedIndexWriter(String pathInvertedIndex, String pathDocIds, String pathFrequencies, int blockSize) {
        try {
            // Open file channel for reading and writing
            fc = FileChannel.open(Paths.get(pathInvertedIndex),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + pathInvertedIndex + " file.");
        }
        // Initialize FrequencyFileWriter and DocIdFileWriter
        frequencyWriter = new FrequencyFileWriter(pathFrequencies, blockSize);
        docIdWriter = new DocIdFileWriter(pathDocIds, blockSize);
    }

    // Write document IDs and frequencies to the file and return the offset of the term
    public Long write(String term, List<Integer> docIds, List<Integer> freqs, boolean compress) throws IOException {
        Long termOffset = fc.size();

        // Write frequencies and document IDs to their respective files
        List<Long> frequenciesOffsets = frequencyWriter.writeFrequencies(freqs, compress);
        List<Long> docIdsOffsets = docIdWriter.writeDocIds(docIds, compress);
        List<Integer> termUpperBounds = docIdWriter.getTermUpperBounds();

        // Write metadata to the buffer
        writeShortToBuffer((short) termUpperBounds.size());
        writeIntListToBuffer(termUpperBounds);
        writeIntToBuffer((int)-1);
        writeLongListToBuffer(docIdsOffsets);
        writeLongToBuffer((long)-1);
        writeLongListToBuffer(frequenciesOffsets);
        writeLongToBuffer((long)-1);

        // System.out.println(term + " " + docIdsOffsets + " " + frequenciesOffsets + " " + termUpperBounds);

        return termOffset;
    }

    public List<Integer> getDocIds(Long offset, boolean compress) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset, fc.size() - offset);
        short numBlocks = mbb.getShort();
        List<Integer> termUpperBounds = new ArrayList<>();
        int termUpperBound;
        while ((termUpperBound = mbb.getInt()) != -1) {
            termUpperBounds.add(termUpperBound);
        }
        System.out.println("termUpperBounds " + termUpperBounds);
        List<Long> docIdsOffsets = new ArrayList<>();
        long docIdOffset;
        while ((docIdOffset = mbb.getLong()) != -1) {
            docIdsOffsets.add(docIdOffset);
        }
        // System.out.println("DocIds " + docIdsOffsets);
        return docIdWriter.readDocIds(docIdsOffsets, compress);
    }

    // Get frequency of a document in a given block
    public int getFreq(Long offset, int docId, boolean compress) throws IOException {
        short numBlocks = readShortFromBuffer(offset);
        System.out.println("numBlocks " + numBlocks);

        int blockIndex = findBlockIndex(offset, numBlocks, docId);
        if(blockIndex == -1) return 0;

        //leggiamo i docId
        List<Integer> docIdsBlock = docIdWriter.readDocIdsBlock(getOffsetsDocIds(offset, numBlocks, blockIndex).get(0),
                getOffsetsDocIds(offset, numBlocks, blockIndex).get(1),compress);

        System.out.println("InvertedIndexWriter DocIds"  + docIdsBlock);

        //leggiamo le frequenze
        List<Short> freqsBlock = frequencyWriter.readFreqsBlock(getOffsetsFreqs(offset, numBlocks, blockIndex).get(0),
                getOffsetsFreqs(offset, numBlocks, blockIndex).get(1),compress);

        System.out.println("InvertedIndexWriter Freqs"  + freqsBlock);


        int indexDocId = docIdsBlock.indexOf(docId);
        return (indexDocId == -1) ? 0 : freqsBlock.get(indexDocId);
    }

    // Get index block for a document ID
    public Integer getIndexBlock(List<Integer> termUpperBounds, int docId) {
        for (int i = 0; i < termUpperBounds.size(); i++) {
            if (docId <= termUpperBounds.get(i)) {
                return i;
            }
        }
        return -1;
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
    public List<Long> getOffsetsDocIds(Long offset, int numBlocchi, int indiceBloccoCercato) throws IOException {
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
    public List<Long> getOffsetsFreqs(Long offset, int numBlocchi, int indiceBloccoCercato) throws IOException {
        mbb=fc.map(FileChannel.MapMode.READ_ONLY,offset+2L+ (4L *(numBlocchi+1)) + (8L * (numBlocchi+1)) + (8L *indiceBloccoCercato) +8,
                offset+2L+ (4L * (numBlocchi+1)) + (8L * (numBlocchi+1)) + (8L *(indiceBloccoCercato+1)) +8);
        return List.of(mbb.getLong(),mbb.getLong());
    }

    // Helper method: Write a short value to the buffer
    private void writeShortToBuffer(short value) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 2);
        mbb.putShort(value);
    }

    private void writeIntToBuffer(int value) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 4);
        mbb.putInt(value);
    }

    private void writeLongToBuffer(long value) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 8);
        mbb.putLong(value);
    }

    // Helper method: Read a short value from the buffer
    private short readShortFromBuffer(Long offset) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset, 2);
        return mbb.getShort();
    }

    // Helper method: Write a list of integers to the buffer
    private void writeIntListToBuffer(List<Integer> values) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), calculateDimensionInt(values));
        for (int value : values) {
            mbb.putInt(value);
        }
    }

    // Helper method: Write a list of long values to the buffer
    private void writeLongListToBuffer(List<Long> values) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), calculateDimensionLong(values));
        for (Long value : values) {
            mbb.putLong(value);
        }
    }

    private void writeStringToBuffer(String term) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), term.length() * 2L);
        for(int i=0;i<term.length();i++){
            mbb.putChar(term.charAt(i));
        }
    }


    // Helper method: Read a range of long values from the buffer
    private long[] readLongRangeFromBuffer(Long offset, int blockIndex, long stride) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset + blockIndex * stride, 2 * stride);
        return new long[]{mbb.getLong(), mbb.getLong()};
    }

    // Helper method: Find the index block for a given document ID
    private int findBlockIndex(Long offset, short numBlocks, int docId) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, offset + 2L, numBlocks * 4);
        for (int i = 0; i < numBlocks; i++) {
            if (mbb.getInt() >= docId) {
                return i;
            }
        }
        return -1;
    }


    public void read() throws IOException{
        int limit = 16;
        try (InputStream input = new FileInputStream(Configuration.PATH_INVERTED_INDEX_OFFSETS);
             DataInputStream inputStream = new DataInputStream(input)) {
            while (inputStream.available() > 0) {
                short termLength = inputStream.readShort();

                int termUpperBound;
                List<Integer> termUpperBounds = new ArrayList<>();
                while ((termUpperBound = inputStream.readInt()) != -1) {
                    termUpperBounds.add(termUpperBound);
                }

                // Read the document IDs
                long docIdOffset;
                List<Long> docIdsOffsets = new ArrayList<>();
                while ((docIdOffset = inputStream.readLong()) != -1) {
                    docIdsOffsets.add(docIdOffset);
                }

                // Read the frequencies
                long frequencyOffset;
                List<Long> frequenciesOffsets = new ArrayList<>();
                while ((frequencyOffset = inputStream.readLong()) != -1) {
                    frequenciesOffsets.add(frequencyOffset);
                }

                // Create a new Term_PostingList object and add it to the list
                // invertedIndexBlock.add(new PostingIndex(termBuilder.toString(), docIds, frequencies));
                limit--;
                if (limit == 0)
                    break;
            }
        }
    }

}