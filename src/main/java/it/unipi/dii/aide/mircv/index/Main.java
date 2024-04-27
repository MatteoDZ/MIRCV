package it.unipi.dii.aide.mircv.index;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.Merge;
import it.unipi.dii.aide.mircv.index.spimi.Spimi;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;

import java.io.*;
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
            Spimi.spimi(Configuration.PATH_DOCUMENTS);
            long endTime_spimi = System.currentTimeMillis();
            System.out.println(printTime("Spimi", startTime_spimi, endTime_spimi));
        }

        if(!FileUtils.fileExists()){
            long startTime_merge = System.currentTimeMillis();
            System.out.println("Merge is starting....");
            Merge merge = new Merge(Objects.requireNonNull(FileUtils.getFilesOfDirectory(Configuration.DIRECTORY_TEMP_FILES)), Configuration.BLOCK_SIZE);
            merge.write(Configuration.COMPRESSION);
            long endTime_merge = System.currentTimeMillis();
            System.out.println(printTime("Merge", startTime_merge, endTime_merge));
        }

        /*InvertedIndexFile invRead = new InvertedIndexFile(Configuration.BLOCK_SIZE);

        Lexicon lexicon = new Lexicon();

        System.out.println("SENZA CACHE");
        long savedtime = 0;
        long start_search_time = System.currentTimeMillis();
        long offsetTerm = lexicon.get("hello").getOffsetInvertedIndex();
        List<Integer>  lst = invRead.getDocIds(offsetTerm, Configuration.COMPRESSION);
        for (Integer i : lst){
            long start_freq_time = System.currentTimeMillis();
            int freq = invRead.getFreqCache(offsetTerm, i, Configuration.COMPRESSION);
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
            int freq = invRead.getFreqCache(offsetTermCache, i, Configuration.COMPRESSION);
            long end_freq_time = System.currentTimeMillis();
            savedtime += (end_freq_time - start_freq_time);
            // System.out.println("TEMPO RECUPERO FREQUENZE DOC " + i +  " are "+ freq + " in " + (end_freq_time-start_freq_time) + " ms");
        }
        long end_search_time_cache = System.currentTimeMillis();
        System.out.println(("Search: " + (end_search_time_cache-start_search_time_cache) + " ms"));
        System.out.println("Somma tempo frequenze: " + savedtime + " ms");

        Statistics statistics = new Statistics();
        statistics.readFromDisk();
        System.out.println(statistics);*/

        // LFUCache<Pair<Long, Integer>, Integer> temp = invRead.getLfuCache();
        //System.out.println(ObjectSizeFetcher.getObjectSize(temp));
        /*
        //soluzione non usata (per ora) per controllare la heap occupata

        long usedMemory=0;
        long maxMemory=0;
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean(); //con queste due righe determiniamo quanta heap è occupata
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage(); //
        maxMemory=heapMemoryUsage.getMax();
        usedMemory= heapMemoryUsage.getUsed();
         */

        /*Scanner scanner = new Scanner(System.in);
        long timerStart, timerEnd;
        TopKPriorityQueue<Pair<Float,Integer>> topKPriorityQueue;
        ArrayList<Integer> queryResult;
        String query, scoreFun;


        do {
            System.out.print("Query-> ");
            query = scanner.nextLine();
            if (query.trim().isEmpty()) {
                System.out.println("empty query");
                continue;
            }

            System.out.print("Daat(1) or exit(2)?");
            int chose = Integer.parseInt(scanner.nextLine());
            if (chose != 1 && chose != 2) {
                System.out.println("no good choice, please repeat");
                continue;
            } else if (chose == 2) {
                break;
            }

            System.out.print("Conjunctive(1) or Disjunctive(2)?");
            int chose1 = Integer.parseInt(scanner.nextLine());
            if (chose1 != 1 && chose1 != 2) {
                System.out.println("no conjunctive nor disjunctive selected, please restart");
                continue;
            }

            System.out.print("Score function bm25 or tfidf->");
            scoreFun = scanner.nextLine();
            if (!scoreFun.equals("bm25") && !scoreFun.equals("tfidf")) {
                System.out.println("no tfidf or bm25");
                continue;
            }

            timerStart = System.currentTimeMillis();
            topKPriorityQueue = (Processer.processQuery(query, 10, chose1 == 1, scoreFun));
            timerEnd = System.currentTimeMillis();
            queryResult=Processer.getRankedQuery(topKPriorityQueue);

            List<Integer>  lst2 = invRead.getDocIds(offsetTerm, Configuration.COMPRESSION);

            if (queryResult == null) {
                System.out.println("no docs for the query");
            } else {
                System.out.print("Results of DocNos-> ");
                for (int i : queryResult) {
                    System.out.print(i + " ");
                }
                System.out.println("with time->" + (timerEnd - timerStart) + "ms");
            }

        } while (true);

        scanner.close();*/
    }

    public static String printTime(String phase, long startTime, long endTime){
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        long minutes = executionTimeInSeconds / 60;
        long remainingSeconds = executionTimeInSeconds % 60;
        return phase + " is completed in : " + minutes + " minutes and  " + remainingSeconds + " seconds";
    }
}