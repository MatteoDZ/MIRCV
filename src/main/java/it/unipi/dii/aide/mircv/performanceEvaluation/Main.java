package it.unipi.dii.aide.mircv.performanceEvaluation;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.Lexicon;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import it.unipi.dii.aide.mircv.query.Processer;
import it.unipi.dii.aide.mircv.query.TopKPriorityQueue;
import static it.unipi.dii.aide.mircv.performanceEvaluation.Statistics.printStats;

import org.javatuples.Pair;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.GZIPInputStream;



public class Main {

    /**
     * Executes the performance evaluation of search engine queries.
     */
    public static void main(String[] args) throws IOException {
        Statistics statistics = new Statistics();
        statistics.readFromDisk();

        File fileDAATTFIDF = new File(Configuration.DIRECTORY_PERFORMANCE_EVALUATION, "/DAATTFIDF.txt");
        File fileDAATBM25 = new File(Configuration.DIRECTORY_PERFORMANCE_EVALUATION, "/DAATBM25.txt");
        File fileDYNAMICPRUNINGTFIDF = new File(Configuration.DIRECTORY_PERFORMANCE_EVALUATION, "/DYNAMICPRUNINGTFIDF.txt");
        File fileDYNAMICPRUNINGBM25 = new File(Configuration.DIRECTORY_PERFORMANCE_EVALUATION, "/DYNAMICPRUNINGBM25.txt");

        FileUtils.deleteDirectory(Configuration.DIRECTORY_PERFORMANCE_EVALUATION);
        FileUtils.createDirectory(Configuration.DIRECTORY_PERFORMANCE_EVALUATION);
        FileUtils.createFiles(fileDAATTFIDF, fileDAATBM25, fileDYNAMICPRUNINGTFIDF, fileDYNAMICPRUNINGBM25);

        try {
            assert Configuration.PATH_QUERIES != null;
            InputStream gzip = new GZIPInputStream(Files.newInputStream(Paths.get(Configuration.PATH_QUERIES)));
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8));
            String line;
            BufferedWriter bufferedWriterDAATTFIDF = new BufferedWriter(new FileWriter(fileDAATTFIDF));
            BufferedWriter bufferedWriterDAATBM25 = new BufferedWriter(new FileWriter(fileDAATBM25));
            BufferedWriter bufferedWriterDYNAMICPRUNINGTFIDF = new BufferedWriter(new FileWriter(fileDYNAMICPRUNINGTFIDF));
            BufferedWriter bufferedWriterDYNAMICPRUNINGBM25 = new BufferedWriter(new FileWriter(fileDYNAMICPRUNINGBM25));
            ArrayList<Long> withCacheTFIDFDAAT = new ArrayList<>(), withCacheTFIDFDP = new ArrayList<>(),
                    withoutCacheTFIDFDAAT = new ArrayList<>(), withoutCacheTFIDFDP = new ArrayList<>(),
                    withCacheBM25DP = new ArrayList<>(), withCacheBM25DAAT = new ArrayList<>(),
                    withoutCacheBM25DAAT = new ArrayList<>(), withoutCacheBM25DP = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\t");

                execQuery(bufferedWriterDAATTFIDF, parts[0], parts[1], "tfidf", false, withoutCacheTFIDFDAAT);
                execQueryWithCache(parts[1], "tfidf", false, withCacheTFIDFDAAT);

                execQuery(bufferedWriterDAATBM25, parts[0], parts[1], "bm25", false, withoutCacheBM25DAAT);
                execQueryWithCache(parts[1], "bm25", false, withCacheBM25DAAT);

                execQuery(bufferedWriterDYNAMICPRUNINGTFIDF, parts[0], parts[1], "tfidf", true, withoutCacheTFIDFDP);
                execQueryWithCache(parts[1], "tfidf", true, withCacheTFIDFDP);

                execQuery(bufferedWriterDYNAMICPRUNINGBM25, parts[0], parts[1], "bm25", true, withoutCacheBM25DP);
                execQueryWithCache(parts[1], "bm25", true, withCacheBM25DP);

            }
            printStats("withoutCacheTFIDFDAAT", withoutCacheTFIDFDAAT);
            printStats("withCacheTFIDFDAAT", withCacheTFIDFDAAT);
            printStats("withoutCacheBM25DAAT", withoutCacheBM25DAAT);
            printStats("withCacheBM25DAAT", withCacheBM25DAAT);
            printStats("withoutCacheTFIDFDP", withoutCacheTFIDFDP);
            printStats("withCacheTFIDFDP", withCacheTFIDFDP);
            printStats("withoutCacheBM25DP", withoutCacheBM25DP);
            printStats("withCacheBM25DP", withCacheBM25DP);
            bufferedWriterDAATBM25.close();
            bufferedWriterDAATTFIDF.close();
            bufferedWriterDYNAMICPRUNINGBM25.close();
            bufferedWriterDYNAMICPRUNINGTFIDF.close();


        } catch (IOException e) {
            System.out.println("Problems with opening the query file to perform a performance evaluation: " + e.getMessage());

        }
    }

    /**
     * Write the top K priority queue pairs to the BufferedWriter in input.
     *
     * @param  bufferedWriter  the buffered writer to write to the file
     * @param  answerOfSearchEngine  the top K priority queue containing pairs of floats and integers
     * @param  qno  the query number
     */
    private static void write2File(BufferedWriter bufferedWriter, TopKPriorityQueue<Pair<Float, Integer>> answerOfSearchEngine, String qno) {
        ArrayList<Pair<Float, Integer>> pair = new ArrayList<>();
            if (answerOfSearchEngine != null) {
                while (!answerOfSearchEngine.isEmpty()) {
                    pair.add(answerOfSearchEngine.poll());
                }
                Collections.reverse(pair);
                pair.forEach(p -> {
                    try {
                        bufferedWriter.write(qno + " Q0 " + p.getValue1() + " " + (pair.indexOf(p) + 1) + " " + p.getValue0() + "\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
    }

    /**
     * Executes a query, adds the time taken to execute the
     * query to the passed list and writes the results to a BufferedWriter.
     *
     * @param  buffer           the BufferedWriter to write the results to
     * @param  qno              the query number
     * @param  query            the query string to be executed
     * @param  scoringFunction  the scoring function to use for the query
     * @param  pruning          a boolean indicating whether pruning should be applied
     * @param  listTimeExecution an ArrayList to store the time taken for execution
     */
    private static void execQuery(BufferedWriter buffer, String qno, String query, String scoringFunction, Boolean pruning, ArrayList<Long> listTimeExecution) throws IOException {
        Lexicon.getInstance().clear();
        long start = System.currentTimeMillis();
        TopKPriorityQueue<Pair<Float, Integer>> answerOfSearchEngine = Processer.processQuery(query, 10, false, scoringFunction, Configuration.COMPRESSION, pruning);
        long end = System.currentTimeMillis();
        listTimeExecution.add(end - start);
        write2File(buffer, answerOfSearchEngine, qno);
    }

    /**
     * Executes a query with caching mechanism and adds the time taken to execute the
     * query to the passed list.
     *
     * @param  query           the query string to be executed
     * @param  scoringFunction the scoring function to be used
     * @param  pruning         flag indicating if pruning should be applied
     * @param  listTimeExecution list to store the execution time of the query
     * @throws IOException     if an I/O error occurs
     */
    private static void execQueryWithCache(String query, String scoringFunction, Boolean pruning, ArrayList<Long> listTimeExecution) throws IOException {
        Lexicon.getInstance().clear();
        long start = System.currentTimeMillis();
        Processer.processQuery(query, 10, false, scoringFunction, Configuration.COMPRESSION, pruning);
        long end = System.currentTimeMillis();
        listTimeExecution.add(end - start);
    }

}
