package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.index.config.Configuration;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static it.unipi.dii.aide.mircv.index.merge.UtilsWriter.calculateDimensionInt;

/**
 * DocIdFileWriter class writes and reads document IDs to and from a file.
 */
public class DocIdFileWriter {
    private final FileChannel fc; // File channel for reading and writing
    private MappedByteBuffer mbb; // Mapped byte buffer for efficient I/O
    private final Integer BLOCK_SIZE; // Size of the data block
    private final List<Integer> block = new ArrayList<>(); // Temporary block to store document IDs
    private final List<Integer> termUpperBounds = new ArrayList<>(); // Upper bounds of terms

    /**
     * Constructor for DocIdFileWriter.
     *
     * @param path      The path of the file to be read/written.
     * @param blockSize The size of the data block.
     */
    public DocIdFileWriter(String path, int blockSize) {
        BLOCK_SIZE = blockSize;
        try {
            fc = FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + path + " file.");
        }
    }

    /**
     * Retrieves the list of term upper bounds.
     *
     * @return a list of integers representing the upper bounds of the terms.
     */
    public List<Integer> getTermUpperBounds() {
        return termUpperBounds;
    }

    /**
     * Writes document IDs to the file and returns the offsets of the written blocks.
     *
     * @param docIds      The list of document IDs to write.
     * @param compression Indicates whether to use compression.
     * @return The list of offsets.
     * @throws IOException If an I/O error occurs.
     */
    public List<Long> writeDocIds(List<Integer> docIds, boolean compression) throws IOException {
        List<Long> offsets = new ArrayList<>();
        termUpperBounds.clear();
        offsets.add(fc.size());

        for (Integer docId : docIds) {
            block.add(docId);
            if (block.size() == Configuration.BLOCK_SIZE) {
                offsets.add(writeBlock(block, compression));
                termUpperBounds.add(calculateTermUpperBounds(block));
                block.clear();
            }
        }

        if (!block.isEmpty()) {
            offsets.add(writeBlock(block, compression));
            termUpperBounds.add(calculateTermUpperBounds(block));
            block.clear();
        }

        return offsets;
    }

    /**
     * Writes a block of integers to the file channel.
     *
     * @param block       The list of integers to write.
     * @param compression The encoding to use for the block.
     * @return The size of the file channel after writing the block.
     * @throws IOException If an I/O error occurs.
     */
    private long writeBlock(List<Integer> block, boolean compression) throws IOException {
        if (!compression) {
            mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), calculateDimensionInt(block));
            for (int docId : block) {
                mbb.putInt(docId);
            }
        } else {
            byte[] compressed = VariableByteCompressor.encode(block);
            mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), compressed.length);
            mbb.put(compressed);
        }

        return fc.size();
    }

    /**
     * Reads a block of document IDs from the file.
     *
     * @param offsetsStart The starting offset in the file.
     * @param offsetsEnd   The ending offset in the file.
     * @param compression  Indicates whether the data is compressed.
     * @return A list of document IDs read from the file.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    public List<Integer> readDocIdsBlock(Long offsetsStart, Long offsetsEnd, boolean compression) throws IOException {
        if (!compression) {
            List<Integer> docIds = new ArrayList<>();
            mbb = fc.map(FileChannel.MapMode.READ_ONLY, offsetsStart, offsetsEnd);
            for (long i = offsetsStart; i < offsetsEnd; i += 4) {
                docIds.add(mbb.getInt());
            }
            return docIds;
        } else {
            byte[] compressed = new byte[(int) (offsetsEnd - offsetsStart)];
            mbb = fc.map(FileChannel.MapMode.READ_ONLY, offsetsStart, offsetsEnd);
            mbb.get(compressed);
            return VariableByteCompressor.decode(compressed);
        }
    }

    /**
     * Calculates the upper bounds of the term in the given block.
     *
     * @param block The list of integers representing the block.
     * @return The last element in the block.
     */
    public Integer calculateTermUpperBounds(List<Integer> block) {
        return block.stream().max(Integer::compareTo).get();
    }

    /**
     * Reads a list of document IDs based on a list of offsets.
     *
     * @param offsets     A list of offsets indicating the start and end positions of each block of document IDs.
     * @param compression Indicates whether to use compression.
     * @return A list of document IDs read from the offsets.
     * @throws IOException If there is an error reading the document IDs.
     */
    public List<Integer> readDocIds(List<Long> offsets, boolean compression) throws IOException {
        List<Integer> docIds = new ArrayList<>();
        for (int i = 0; i < offsets.size() - 1; i++) {
            docIds.addAll(readDocIdsBlock(offsets.get(i), offsets.get(i + 1), compression));
        }
        return docIds;
    }
}
