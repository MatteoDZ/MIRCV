package it.unipi.dii.aide.mircv.index.utils;

import it.unipi.dii.aide.mircv.index.ConfigTest;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class StatisticsTest {
    @Test
    public void readWriteTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);

        Statistics statisticsWriter = new Statistics(ConfigTest.PATH_STATISTICS);
        statisticsWriter.setTotalLenDoc(123456);
        statisticsWriter.setNumDocs(1000);
        statisticsWriter.setAvgDocLen((double) 123456/1000);
        statisticsWriter.writeSpimiToDisk();
        statisticsWriter.setTerms(7899);
        statisticsWriter.writeMergeToDisk();

        Statistics statisticsReader = new Statistics(ConfigTest.PATH_STATISTICS);
        statisticsReader.readFromDisk();

        assertEquals(statisticsWriter.getAvgDocLen(), statisticsReader.getAvgDocLen(), 0.001);
        assertEquals(statisticsWriter.getNumDocs(), statisticsReader.getNumDocs());
        assertEquals(statisticsWriter.getTotalLenDoc(), statisticsReader.getTotalLenDoc());
        assertEquals(statisticsWriter.getTerms(), statisticsReader.getTerms());
    }
}
