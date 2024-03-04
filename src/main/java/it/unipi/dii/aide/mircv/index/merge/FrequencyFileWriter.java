package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.compression.UnaryCompressor;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static it.unipi.dii.aide.mircv.index.merge.UtilsWriter.calculateDimensionShort;

/**
 * FrequencyFileWriter class handles the writing and reading of frequencies to and from a file.
 */
public class FrequencyFileWriter {
    private final FileChannel fc; // File channel for reading and writing
    private MappedByteBuffer mbb; // Mapped byte buffer for efficient I/O
    private final Integer BLOCK_SIZE; // Size of the data block

    /**
     * Constructor for FrequencyFileWriter.
     *
     * @param path      The path of the file to be read/written.
     * @param blockSize The size of the data block.
     */
    public FrequencyFileWriter(String path, int blockSize) {
        BLOCK_SIZE = blockSize;
        try {
            fc = FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + path + " file.");
        }
    }

    /**
     * Writes frequencies to the file and returns the offsets of the written blocks.
     *
     * @param freqs      The list of frequencies to write.
     * @param compress   The encoding type for the data.
     * @return The list of offsets.
     * @throws IOException If an I/O error occurs.
     */
    public List<Long> writeFrequencies(List<Integer> freqs, boolean compress) throws IOException {
        List<Long> offsets = new ArrayList<>();
        offsets.add(fc.size());
        List<Integer> block = new ArrayList<>();

        for (Integer freq : freqs) {
            block.add(freq);
            if (block.size() == BLOCK_SIZE) {
                offsets.add(writeBlock(block, compress));
                block.clear();
            }
        }

        if (!block.isEmpty()) {
            offsets.add(writeBlock(block, compress));
            block.clear();
        }

        return offsets;
    }

    /**
     * Writes a block of integers (shorts) to a file.
     *
     * @param block     A list of integers representing the block of data.
     * @param compress  The encoding type for the data.
     * @return The offset of the end of the block.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    private long writeBlock(List<Integer> block, boolean compress) throws IOException {
        if (!compress) {
            mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), calculateDimensionShort(block));
            for (int freq : block) {
                mbb.putShort((short) freq);
            }
        } else {
            byte[] compressed = UnaryCompressor.integerArrayCompression(block.stream()
                    .mapToInt(Integer::intValue)
                    .toArray());
            mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), compressed.length);
            mbb.put(compressed);
        }
        return fc.size();
    }

    /**
     * Reads the frequencies block between the specified offsets.
     *
     * @param offsetsStart The starting offset.
     * @param offsetsEnd   The ending offset.
     * @param compression  Indicates whether compression is enabled.
     * @return A list of Short values representing the frequencies.
     * @throws IOException If there is an error reading the file.
     */
    public List<Short> readFreqsBlock(Long offsetsStart, Long offsetsEnd, boolean compression) throws IOException {
        if (!compression) {
            mbb = fc.map(FileChannel.MapMode.READ_ONLY, offsetsStart, offsetsEnd);
            List<Short> freqs = new ArrayList<>();
            for (long i = offsetsStart; i < offsetsEnd; i += 2) {
                freqs.add(mbb.getShort());
            }
            return freqs;
        } else {
            byte[] compressed = new byte[(int) (offsetsEnd - offsetsStart)];
            mbb = fc.map(FileChannel.MapMode.READ_ONLY, offsetsStart, offsetsEnd);
            mbb.get(compressed);
            System.out.println(compressed.length);
            return UnaryCompressor.integerArrayDecompression(compressed, 5);
        }
    }

    /**
     * Reads the frequencies of documents from the given list of offsets.
     *
     * @param offsets     A list of offsets representing the positions of the document frequencies.
     * @param compression Indicates whether to use compression.
     * @return A list of short values representing the document IDs.
     * @throws IOException If an I/O error occurs while reading the frequencies.
     */
    public List<Short> readFreqs(List<Long> offsets, boolean compression) throws IOException {
        List<Short> frequencies = new ArrayList<>();
        for (int i = 0; i < offsets.size() - 1; i++) {
            frequencies.addAll(readFreqsBlock(offsets.get(i), offsets.get(i + 1), compression));
        }
        return frequencies;
    }
}
