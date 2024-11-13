package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.IndexingMain;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.javatuples.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Scanner;

public class QueryMain {

    /**
     * Reads lines from a TAR.GZ file, processes each line, and writes the results to a new file.
     *
     * @throws IOException If an I/O error occurs while reading or writing files.
     */
    public static void main(String[] args) throws IOException {
        if(!FileUtils.filesExist(Configuration.SKIPPING_BLOCK_PATH, Configuration.PATH_DOCID, Configuration.PATH_FREQ, Configuration.PATH_LEXICON)){
            try {
                IndexingMain.main(new String[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        Scanner scanner = new Scanner(System.in);
        long timerStart, timerEnd;
        TopKPriorityQueue<Pair<Float,Integer>> topKPQ;
        String query, scoringFunction, dynamicPruning, typeOfQuery;


        do {
            System.out.print("Enter the query or write exit to close: ");
            query = scanner.nextLine();
            if (query.trim().isEmpty()) {
                System.out.println("The query is empty. Please enter a valid query.");
                continue;
            }
            else if(query.trim().equals("exit")) {
                break;
            }

            System.out.print("Select between DAAT and DynamicPruning (DP): ");
            dynamicPruning = scanner.nextLine();
            dynamicPruning = dynamicPruning.toUpperCase(Locale.ROOT);
            if (!dynamicPruning.equals("DAAT") && !dynamicPruning.equals("DP")) {
                System.out.println("Something went wrong, please repeat last input");
                continue;
            }

            System.out.print("Select Conjunctive (C) or Disjunctive(D): ");
            typeOfQuery = scanner.nextLine();
            typeOfQuery = typeOfQuery.toUpperCase(Locale.ROOT);
            if (!typeOfQuery.equals("C") && !typeOfQuery.equals("D")) {
                System.out.println("Something went wrong, please repeat last input");
                continue;
            }

            System.out.print("Select the scoring function bm25 or tfidf: ");
            scoringFunction = scanner.nextLine();
            scoringFunction = scoringFunction.toLowerCase(Locale.ROOT);
            if (!scoringFunction.equals("bm25") && !scoringFunction.equals("tfidf")) {
                System.out.println("Something went wrong, please repeat last input");
                continue;
            }

            timerStart = System.currentTimeMillis();
            topKPQ = (Processer.processQuery(query, 10, typeOfQuery.equals("C"), scoringFunction, Configuration.COMPRESSION, dynamicPruning.equals("DP")));
            timerEnd = System.currentTimeMillis();

            // Return null if the priority queue is null.
            if (topKPQ == null) {
                System.out.println("No documents were found for this query");
                continue;
            }

            // Retrieve the document IDs from the priority queue.
            ArrayList<Integer> queryResult = new ArrayList<>();
            while (!topKPQ.isEmpty()) {
                queryResult.add(topKPQ.poll().getValue1());
            }

            // Reverse the list to get the top-K results in descending order.
            Collections.reverse(queryResult);
            System.out.print("Results of document numbers: ");
            for (int i : queryResult) {
                System.out.print(i + " ");
            }
            System.out.println("with execution time: " + (timerEnd - timerStart) + "ms");

        } while (true);

        scanner.close();
    }

}