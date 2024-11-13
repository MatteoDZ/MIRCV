package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import org.javatuples.Pair;

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
     * @param  block     the list of docIds to be written
     * @param lenghts    the list of lengths of the docIds to be written
     * @param  compression  whether to compress the block or not
     * @return A Pair with the initial file channel size and the number of bytes written.
     * @throws IOException if an I/O error occurs
     */
    public Pair<Long, Integer> writeBlock(List<Integer> block, List<Integer> lenghts, Boolean compression) throws IOException {
        long fc_size = fc.size();
        if (block.size() != lenghts.size()) {
            throw new IOException("Different lengths");
        }

        List<Integer> toBeWritten = new ArrayList<>();
        for (int i = 0; i < block.size(); i++) {
            toBeWritten.add(block.get(i));
            toBeWritten.add(lenghts.get(i));
        }

        if (!compression) {
            BinaryFile.writeIntListToBuffer(fc, toBeWritten);
        } else {
            BinaryFile.writeArrayByteToBuffer(fc, VariableByteCompressor.encode(toBeWritten));
        }

        return Pair.with(fc_size, Math.toIntExact((int) (fc.size() - fc_size)));
    }

}
