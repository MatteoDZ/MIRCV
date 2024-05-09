package it.unipi.dii.aide.mircv.index;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.*;
import it.unipi.dii.aide.mircv.index.spimi.Spimi;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class IndexingMainTest {
    @BeforeAll
    static void setUp() {
        Configuration.setUpPathTest();
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEMP_FILES);
    }

    @Test
    public void MainTestNoCompression() throws IOException {
        setUp();

        long startTime_spimi = System.currentTimeMillis();
        System.out.println("Spimi is starting....");
        Spimi.spimi(Configuration.PATH_DOCUMENTS_TEST);
        long endTime_spimi = System.currentTimeMillis();
        System.out.println(IndexingMain.printCompletionTime("Spimi", startTime_spimi, endTime_spimi));

        long startTime_merge = System.currentTimeMillis();
        System.out.println("Merge is starting....");
        Merge merge = new Merge(Objects.requireNonNull(FileUtils.getFilesOfDirectory(Configuration.DIRECTORY_TEMP_FILES)), Configuration.BLOCK_SIZE);
        merge.write( false);
        long endTime_merge = System.currentTimeMillis();
        System.out.println(IndexingMain.printCompletionTime("Merge", startTime_merge, endTime_merge));


    }


    @Test
    public void MainTestYesCompression() throws IOException {
        setUp();


        long startTime_spimi = System.currentTimeMillis();
        System.out.println("Spimi is starting....");
        Spimi.spimi(Configuration.PATH_DOCUMENTS_TEST);
        long endTime_spimi = System.currentTimeMillis();
        System.out.println(IndexingMain.printCompletionTime("Spimi", startTime_spimi, endTime_spimi));

        long startTime_merge = System.currentTimeMillis();
        System.out.println("Merge is starting....");
        Merge merge = new Merge(
                Objects.requireNonNull(FileUtils.getFilesOfDirectory(Configuration.DIRECTORY_TEMP_FILES)), Configuration.BLOCK_SIZE);
        merge.write(true);
        long endTime_merge = System.currentTimeMillis();
        System.out.println(IndexingMain.printCompletionTime("Merge", startTime_merge, endTime_merge));

    }
}
