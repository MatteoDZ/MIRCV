package it.unipi.dii.aide.mircv.index;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.InvertedIndexFile;
import it.unipi.dii.aide.mircv.index.merge.Lexicon;
import it.unipi.dii.aide.mircv.index.merge.Merge;
import it.unipi.dii.aide.mircv.index.spimi.Spimi;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import it.unipi.dii.aide.mircv.index.utils.Statistics;

import java.io.*;
import java.util.List;
import java.util.Objects;

public class Main {

    /**
     * Reads lines from a TAR.GZ file, processes each line, and writes the results to a new file.
     *
     * @throws IOException If an I/O error occurs while reading or writing files.
     */
    public static void main(String[] args) throws IOException {

        if (!FileUtils.searchIfExists(Configuration.DIRECTORY_TEMP_FILES)) {
            FileUtils.createDirectory(Configuration.DIRECTORY_TEMP_FILES);
        }

        System.out.println("Number of files in temporary directory: " + FileUtils.getNumberFiles(Configuration.DIRECTORY_TEMP_FILES));


        if (FileUtils.getNumberFiles(Configuration.DIRECTORY_TEMP_FILES) <= 0) {
            long startTime_spimi = System.currentTimeMillis();
            System.out.println("Spimi is starting....");
            Spimi.spimi(Configuration.PATH_DOCUMENTS, Configuration.PATH_STATISTICS);
            long endTime_spimi = System.currentTimeMillis();
            System.out.println(printTime("Spimi", startTime_spimi, endTime_spimi));
        }

        if(!FileUtils.searchIfExists(Configuration.PATH_INVERTED_INDEX)){
            long startTime_merge = System.currentTimeMillis();
            System.out.println("Merge is starting....");
            Merge merge = new Merge(Objects.requireNonNull(FileUtils.getFilesOfDirectory(Configuration.DIRECTORY_TEMP_FILES)),
                    Configuration.PATH_LEXICON, Configuration.PATH_DOCIDS, Configuration.PATH_FEQUENCIES, Configuration.PATH_STATISTICS, Configuration.BLOCK_SIZE);
            merge.write(Configuration.PATH_INVERTED_INDEX, Configuration.COMPRESSION);
            long endTime_merge = System.currentTimeMillis();
            System.out.println(printTime("Merge", startTime_merge, endTime_merge));
        }

        InvertedIndexFile invRead = new InvertedIndexFile(Configuration.PATH_INVERTED_INDEX, Configuration.PATH_DOCIDS, Configuration.PATH_FEQUENCIES, Configuration.BLOCK_SIZE);

        Lexicon lexicon = new Lexicon(Configuration.PATH_LEXICON);

        System.out.println("SENZA CACHE");
        long savedtime = 0;
        long start_search_time = System.currentTimeMillis();
        long offsetTerm = lexicon.get("hello").getOffsetInvertedIndex();
        List<Integer>  lst = invRead.getDocIds(offsetTerm, Configuration.COMPRESSION);
        for (Integer i : lst){
            long start_freq_time = System.currentTimeMillis();
            int freq = invRead.getFreq(offsetTerm, i, Configuration.COMPRESSION);
            long end_freq_time = System.currentTimeMillis();
            savedtime += (end_freq_time - start_freq_time);
            // System.out.println("TEMPO RECUPERO FREQUENZE DOC " + i +  " are "+ freq + " in " + (end_freq_time-start_freq_time) + " ms");
        }
        long end_search_time = System.currentTimeMillis();
        System.out.println(("Search: " + (end_search_time-start_search_time) + " ms"));
        System.out.println("Somma tempo frequenze: " + savedtime + " ms");


        System.out.println("CON CACHE");
        savedtime =0;
        long start_search_time_cache = System.currentTimeMillis();
        long offsetTermCache = lexicon.get("hello").getOffsetInvertedIndex();
        List<Integer> lstcache = invRead.getDocIds(offsetTermCache, Configuration.COMPRESSION);
        for (Integer i : lstcache){
            long start_freq_time = System.currentTimeMillis();
            int freq = invRead.getFreq(offsetTermCache, i, Configuration.COMPRESSION);
            long end_freq_time = System.currentTimeMillis();
            savedtime += (end_freq_time - start_freq_time);
            // System.out.println("TEMPO RECUPERO FREQUENZE DOC " + i +  " are "+ freq + " in " + (end_freq_time-start_freq_time) + " ms");
        }
        long end_search_time_cache = System.currentTimeMillis();
        System.out.println(("Search: " + (end_search_time_cache-start_search_time_cache) + " ms"));
        System.out.println("Somma tempo frequenze: " + savedtime + " ms");

        Statistics statistics = new Statistics(Configuration.PATH_STATISTICS);
        statistics.readFromDisk();
        System.out.println(statistics.toString());
        

        /*
        //soluzione non usata (per ora) per controllare la heap occupata

        long usedMemory=0;
        long maxMemory=0;
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean(); //con queste due righe determiniamo quanta heap è occupata
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage(); //
        maxMemory=heapMemoryUsage.getMax();
        usedMemory= heapMemoryUsage.getUsed();
         */
    }

    public static String printTime(String phase, long startTime, long endTime){
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        long minutes = executionTimeInSeconds / 60;
        long remainingSeconds = executionTimeInSeconds % 60;
        return phase + " is completed in : " + minutes + " minutes and  " + remainingSeconds + " seconds";
    }
}