package it.unipi.dii.aide.mircv.index.spimi;

import it.unipi.dii.aide.mircv.index.ConfigTest;
import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.InvertedIndexFile;
import it.unipi.dii.aide.mircv.index.merge.Lexicon;
import it.unipi.dii.aide.mircv.index.merge.Merge;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.javatuples.Pair;
import org.junit.Test;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpimiTest {

    @Test
    public void spimiTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(ConfigTest.DIRECTORY_TMP);
        Spimi.spimi(Configuration.PATH_DOCUMENTS_TEST, ConfigTest.PATH_STATISTICS, ConfigTest.PATH_BLOCKS);
        assertFalse(Objects.requireNonNull(FileUtils.getFilesOfDirectory(ConfigTest.DIRECTORY_TMP)).isEmpty());
        assertTrue(FileUtils.searchIfExists(ConfigTest.PATH_STATISTICS));

        Merge merge = new Merge(List.of("data/test/tmp/testBlocks_0.bin"), ConfigTest.PATH_LEXICON, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, ConfigTest.PATH_STATISTICS, 2);
        merge.write(ConfigTest.PATH_INV_INDEX, false);
    }

    @Test
    public void spimiRead() throws IOException {
        final FileChannel fc;
        try {
            // Open file channel for reading and writing
            fc = FileChannel.open(Paths.get("data/test/docprova"),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + "data/test/docprova" + " file.");
        }

        final FileChannel fc2;
        try {
            // Open file channel for reading and writing
            fc2 = FileChannel.open(Paths.get("data/test/testDoc"),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + "data/test/docprova" + " file.");
        }

        InvertedIndexFile invRead = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, Configuration.BLOCK_SIZE);
        Lexicon lexicon = new Lexicon(ConfigTest.PATH_LEXICON);

        // "hellp" -> 20, 19 -> 1, 2   hashmap(doc_id, freq) -> [(20, 1), (19,2) ...]


        List<Integer> lst = BinaryFile.readIntListFromBuffer(fc, 0L, fc.size());
        for (Integer p : lst) {
            System.out.println(p);
        }
    }
}
