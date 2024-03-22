package it.unipi.dii.aide.mircv.index;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.InvertedIndexFile;
import it.unipi.dii.aide.mircv.index.merge.Lexicon;
import it.unipi.dii.aide.mircv.index.merge.Merge;
import it.unipi.dii.aide.mircv.index.spimi.Spimi;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class MainTest {

    @Test
    public void MainTestNoCompression() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(ConfigTest.DIRECTORY_TMP);


        long startTime_spimi = System.currentTimeMillis();
        System.out.println("Spimi is starting....");
        Spimi.spimi(Configuration.PATH_DOCUMENTS_TEST, ConfigTest.PATH_STATISTICS, ConfigTest.PATH_BLOCKS, ConfigTest.PATH_DOC_TERMS);
        long endTime_spimi = System.currentTimeMillis();
        System.out.println(Main.printTime("Spimi", startTime_spimi, endTime_spimi));

        long startTime_merge = System.currentTimeMillis();
        System.out.println("Merge is starting....");
        Merge merge = new Merge(
                Objects.requireNonNull(FileUtils.getFilesOfDirectory(ConfigTest.DIRECTORY_TMP)),
                ConfigTest.PATH_LEXICON, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, ConfigTest.PATH_STATISTICS,
                Configuration.BLOCK_SIZE, ConfigTest.PATH_DOC_TERMS);
        merge.write(ConfigTest.PATH_INV_INDEX, false);
        long endTime_merge = System.currentTimeMillis();
        System.out.println(Main.printTime("Merge", startTime_merge, endTime_merge));

        InvertedIndexFile invRead = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, Configuration.BLOCK_SIZE);

        Lexicon lexicon = new Lexicon(ConfigTest.PATH_LEXICON);

        long offsetTermWar = lexicon.get("war").getOffsetInvertedIndex();
        assertEquals(List.of(1,3,6,7,34), invRead.getDocIds(offsetTermWar, false).stream().limit(5).toList());
        assertEquals(1, (int)invRead.getFreqCache(offsetTermWar, 1, false));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermWar, 3, false));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermWar, 6, false));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermWar, 7, false));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermWar, 34, false));


        long offsetTermBomb = lexicon.get("bomb").getOffsetInvertedIndex();
        assertEquals(List.of(1,2,3,5,6,7,8), invRead.getDocIds(offsetTermBomb, false).stream().limit(7).toList());
        assertEquals(1, (int)invRead.getFreqCache(offsetTermBomb, 1, false));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermBomb, 2, false));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermBomb, 3, false));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermBomb, 5, false));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermBomb, 6, false));

    }


    @Test
    public void MainTestYesCompression() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(ConfigTest.DIRECTORY_TMP);


        long startTime_spimi = System.currentTimeMillis();
        System.out.println("Spimi is starting....");
        Spimi.spimi(Configuration.PATH_DOCUMENTS_TEST, ConfigTest.PATH_STATISTICS, ConfigTest.PATH_BLOCKS, ConfigTest.PATH_DOC_TERMS);
        long endTime_spimi = System.currentTimeMillis();
        System.out.println(Main.printTime("Spimi", startTime_spimi, endTime_spimi));

        long startTime_merge = System.currentTimeMillis();
        System.out.println("Merge is starting....");
        Merge merge = new Merge(
                Objects.requireNonNull(FileUtils.getFilesOfDirectory(ConfigTest.DIRECTORY_TMP)),
                ConfigTest.PATH_LEXICON, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, ConfigTest.PATH_STATISTICS, Configuration.BLOCK_SIZE, ConfigTest.PATH_DOC_TERMS);
        merge.write(ConfigTest.PATH_INV_INDEX, true);
        long endTime_merge = System.currentTimeMillis();
        System.out.println(Main.printTime("Merge", startTime_merge, endTime_merge));

        InvertedIndexFile invRead = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, Configuration.BLOCK_SIZE);

        Lexicon lexicon = new Lexicon(ConfigTest.PATH_LEXICON);

        long offsetTermWar = lexicon.get("war").getOffsetInvertedIndex();
        assertEquals(List.of(1,3,6,7,34), invRead.getDocIds(offsetTermWar, true).stream().limit(5).toList());
        assertEquals(1, (int)invRead.getFreqCache(offsetTermWar, 1, true));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermWar, 3, true));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermWar, 6, true));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermWar, 7, true));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermWar, 34, true));


        long offsetTermBomb = lexicon.get("bomb").getOffsetInvertedIndex();
        assertEquals(List.of(1,2,3,5,6,7,8), invRead.getDocIds(offsetTermBomb, true).stream().limit(7).toList());
        assertEquals(1, (int)invRead.getFreqCache(offsetTermBomb, 1, true));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermBomb, 2, true));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermBomb, 3, true));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermBomb, 5, true));
        assertEquals(1, (int)invRead.getFreqCache(offsetTermBomb, 6, true));

    }
}
