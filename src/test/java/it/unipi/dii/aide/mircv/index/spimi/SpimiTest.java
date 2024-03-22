package it.unipi.dii.aide.mircv.index.spimi;

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
        Configuration.setUpPathTest();
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEMP_FILES);
        Spimi.spimi(Configuration.PATH_DOCUMENTS_TEST);
        assertFalse(Objects.requireNonNull(FileUtils.getFilesOfDirectory(Configuration.DIRECTORY_TEMP_FILES)).isEmpty());
        assertTrue(FileUtils.searchIfExists(Configuration.PATH_STATISTICS));
    }

}
