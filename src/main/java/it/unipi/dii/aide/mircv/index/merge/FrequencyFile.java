package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.index.config.Configuration;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * FrequencyFileWriter class handles the writing and reading of frequencies to and from a file.
 */
public class FrequencyFile {
    private final FileChannel fc; // File channel for reading and writing
    private final Integer BLOCK_SIZE; // Size of the data block

    /**
     * Constructor for FrequencyFileWriter.
     *
     * @param blockSize The size of the data block.
     */
    public FrequencyFile(int blockSize) {
        BLOCK_SIZE = blockSize;
        try {
            fc = FileChannel.open(Paths.get(Configuration.PATH_FREQ), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + Configuration.PATH_FREQ + " file.");
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
    public Long writeFrequencies(List<Integer> freqs, boolean compress) throws IOException {
        long offset = fc.size();
        List<Integer> block = new ArrayList<>();

        for (Integer freq : freqs) {
            block.add(freq);
            if (block.size() == BLOCK_SIZE) {
                writeBlock(block, compress);
                block.clear();
            }
        }

        if (!block.isEmpty()) {
            writeBlock(block, compress);
            block.clear();
        }

        return offset;
    }

    /**
     * Writes a block of integers (shorts) to a file.
     *
     * @param block     A list of integers representing the block of data.
     * @param compress  The encoding type for the data.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    private void writeBlock(List<Integer> block, boolean compress) throws IOException {
        if (!compress) {
            BinaryFile.writeShortListToBuffer(fc, block);
        } else {
            byte[] compressed = UnaryCompressor.integerArrayCompression(block.stream()
                    .mapToInt(Integer::intValue)
                    .toArray());
            BinaryFile.writeArrayByteToBuffer(fc, compressed);
        }
    }
}
