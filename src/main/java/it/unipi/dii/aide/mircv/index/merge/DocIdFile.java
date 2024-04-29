package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.index.config.Configuration;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * DocIdFileWriter class writes and reads document IDs to and from a file.
 */
public class DocIdFile {
    private final FileChannel fc; // File channel for reading and writing
    private final Integer BLOCK_SIZE; // Size of the data block
    private final List<Integer> block = new ArrayList<>(); // Temporary block to store document IDs

    /**
     * Constructor for DocIdFileWriter.
     *
     * @param blockSize The size of the data block.
     */
    public DocIdFile(int blockSize) {
        BLOCK_SIZE = blockSize;
        try {

            fc = FileChannel.open(Paths.get(Configuration.PATH_DOCID), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + Configuration.PATH_DOCID + " file.");
        }
    }

    /**
     * Writes document IDs to the file and returns the offsets of the written blocks.
     *
     * @param docIds      The list of document IDs to write.
     * @param compression Indicates whether to use compression.
     * @return The list of offsets.
     * @throws IOException If an I/O error occurs.
     */
    public Long writeDocIds(List<Integer> docIds, boolean compression) throws IOException {
        long offset = fc.size();

        for (Integer docId : docIds) {
            block.add(docId);
            if (block.size() == BLOCK_SIZE) {
                writeBlock(block, compression);
                block.clear();
            }
        }

        if (!block.isEmpty()) {
            writeBlock(block, compression);
            block.clear();
        }

        return offset;
    }

    /**
     * Writes a block of integers to the file channel.
     *
     * @param block       The list of integers to write.
     * @param compression The encoding to use for the block.
     * @throws IOException If an I/O error occurs.
     */
    public long writeBlock(List<Integer> block, boolean compression) throws IOException {
        long fc_size = fc.size();
        if (!compression) {
            BinaryFile.writeIntListToBuffer(fc, block);
        } else {
            BinaryFile.writeArrayByteToBuffer(fc, VariableByteCompressor.encode(block));
        }
        return fc_size;
    }

}
