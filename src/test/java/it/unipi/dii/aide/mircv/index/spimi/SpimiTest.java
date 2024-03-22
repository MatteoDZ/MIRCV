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
        Spimi.spimi(Configuration.PATH_DOCUMENTS_TEST, ConfigTest.PATH_STATISTICS, ConfigTest.PATH_BLOCKS, ConfigTest.PATH_DOC_TERMS);
        assertFalse(Objects.requireNonNull(FileUtils.getFilesOfDirectory(ConfigTest.DIRECTORY_TMP)).isEmpty());
        assertTrue(FileUtils.searchIfExists(ConfigTest.PATH_STATISTICS));
    }

}
