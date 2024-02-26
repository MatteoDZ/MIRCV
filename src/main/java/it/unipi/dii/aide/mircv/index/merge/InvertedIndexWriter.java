package it.unipi.dii.aide.mircv.index.merge;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static it.unipi.dii.aide.mircv.index.merge.UtilsWriter.*;

public class InvertedIndexWriter {
    private final FileChannel fc;
    private MappedByteBuffer mbb;
    public final FrequencyFileWriter frequencyWriter;
    public final DocIdFileWriter docIdWriter;
    private final int blockSize;

    // Constructor
    public InvertedIndexWriter(String pathInvertedIndex, String pathDocIds, String pathFrequencies, int blockSize) {
        this.blockSize = blockSize;
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
    public Long write(List<Integer> docIds, List<Integer> freqs, boolean compress) throws IOException {
        Long termOffset = fc.size();

        // Write frequencies and document IDs to their respective files
        List<Long> frequenciesOffsets = frequencyWriter.writeFrequencies(freqs, compress);
        List<Long> docIdsOffsets = docIdWriter.writeDocIds(docIds, compress);
        List<Integer> termUpperBounds = docIdWriter.getTermUpperBounds();

        // Write metadata to the buffer
        writeShortToBuffer((short) termUpperBounds.size());
        writeIntListToBuffer(termUpperBounds);
        writeLongListToBuffer(docIdsOffsets);
        writeLongListToBuffer(frequenciesOffsets);

        return termOffset;
    }

    // Get frequency of a document in a given block
    public int getFreq(Long offset, int docId, boolean compress) throws IOException {
        short numBlocks = readShortFromBuffer(offset);
        int blockIndex = findBlockIndex(offset, numBlocks, docId);

        if (blockIndex == -1) return 0;

        // Read offsets for document IDs and frequencies
        long[] docIdsOffsets = readLongRangeFromBuffer(offset + 2L + 4L * numBlocks, blockIndex, 8L);
        long[] freqsOffsets = readLongRangeFromBuffer(
                offset + 2L + (4L + 8L * numBlocks) * (numBlocks + 1) + 8L * blockIndex, blockIndex, 8L);

        // Read document IDs and frequencies
        List<Integer> docIdsBlock = docIdWriter.readDocIdsBlock(docIdsOffsets[0], docIdsOffsets[1], compress);
        List<Short> freqsBlock = frequencyWriter.readFreqsBlock(freqsOffsets[0], freqsOffsets[1], compress);

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
        mbb=fc.map(FileChannel.MapMode.READ_ONLY,offset+2L+ 4L *numBlocchi+ 8L *indiceBloccoCercato,
                offset+2L+ 4L *numBlocchi+ 8L *(indiceBloccoCercato+1));
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
        mbb=fc.map(FileChannel.MapMode.READ_ONLY,offset+2L+ (4L *numBlocchi) + (8L *numBlocchi) + (8L *indiceBloccoCercato) +8,
                offset+2L+ (4L *numBlocchi) + (8L *numBlocchi) + (8L *(indiceBloccoCercato+1)) +8);
        return List.of(mbb.getLong(),mbb.getLong());
    }

    // Helper method: Write a short value to the buffer
    private void writeShortToBuffer(short value) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 2);
        mbb.putShort(value);
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
}