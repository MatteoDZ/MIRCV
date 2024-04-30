package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.index.config.Configuration;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * DocIdFileWriter class writes and reads document IDs to and from a file.
 */
public class DocIdFile {
    private final FileChannel fc; // File channel for reading and writing

    /**
     * Constructor for DocIdFileWriter.
     *
     */
    public DocIdFile() {
        try {
            fc = FileChannel.open(Paths.get(Configuration.PATH_DOCID), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + Configuration.PATH_DOCID + " file.");
        }
    }

    /**
     * Writes a block of integers to the file.
     *
     * @param  block     the list of integers to be written
     * @param  compression  whether to compress the block or not
     * @return           the size of the file before writing the block
     * @throws IOException if an I/O error occurs
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
