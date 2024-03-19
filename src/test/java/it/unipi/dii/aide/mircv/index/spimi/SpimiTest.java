package it.unipi.dii.aide.mircv.index.spimi;

import it.unipi.dii.aide.mircv.index.ConfigTest;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.Test;

import java.io.IOException;
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
    }
}
