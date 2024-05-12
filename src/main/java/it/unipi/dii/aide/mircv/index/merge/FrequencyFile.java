package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import org.javatuples.Pair;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * FrequencyFileWriter class handles the writing and reading of frequencies to and from a file.
 */
public class FrequencyFile {
    private final FileChannel fc; // File channel for reading and writing

    /**
     * Constructor for FrequencyFileWriter.
     *
     */
    public FrequencyFile() {
        try {
            fc = FileChannel.open(Paths.get(Configuration.PATH_FREQ), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + Configuration.PATH_FREQ + " file.");
        }
    }

    /**
     * Writes a block of integers to the file.
     *
     * @param  block     the list of integers to be written
     * @param  compress  whether to compress the block or not
     * @return           the size of the file before writing the block
     * @throws IOException if an I/O error occurs
     */
    public Pair<Long, Integer> writeBlockP(List<Integer> block, boolean compress) throws IOException {
        long fc_size = fc.size();
        if (!compress) {
            BinaryFile.writeShortListToBuffer(fc, block);
        } else {
            byte[] compressed = UnaryCompressor.integerArrayCompression(block.stream()
                    .mapToInt(Integer::intValue)
                    .toArray());
            BinaryFile.writeArrayByteToBuffer(fc, compressed);
        }
        return Pair.with(fc_size, Math.toIntExact((int) fc.size() - fc_size));
    }

}
