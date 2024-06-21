package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.javatuples.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    /**
     * Reads lines from a TAR.GZ file, processes each line, and writes the results to a new file.
     *
     * @throws IOException If an I/O error occurs while reading or writing files.
     */
    public static void main(String[] args) throws IOException {
        if(!FileUtils.filesExist(Configuration.SKIPPING_BLOCK_PATH, Configuration.PATH_DOCID, Configuration.PATH_FREQ, Configuration.PATH_LEXICON)){
            throw new RuntimeException("The index data files does not exist, run the indexing main first");
        }


        Scanner scanner = new Scanner(System.in);
        long timerStart, timerEnd;
        TopKPriorityQueue<Pair<Float,Integer>> topKPriorityQueue;
        ArrayList<Integer> queryResult;
        String query, scoringFunction, dynamicPruning, typeOfQuery;


        do {
            System.out.print("Enter the query: ");
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
            if (!dynamicPruning.equals("DAAT") && !dynamicPruning.equals("DP")) {
                System.out.println("Something went wrong, please repeat last input");
                continue;
            }

            System.out.print("Select Conjunctive (C) or Disjunctive(D): ");
            typeOfQuery = scanner.nextLine();
            if (!typeOfQuery.equals("C") && !typeOfQuery.equals("D")) {
                System.out.println("Something went wrong, please repeat last input");
                continue;
            }

            System.out.print("Select the scoring function bm25 or tfidf: ");
            scoringFunction = scanner.nextLine();
            if (!scoringFunction.equals("bm25") && !scoringFunction.equals("tfidf")) {
                System.out.println("Something went wrong, please repeat last input");
                continue;
            }

            timerStart = System.currentTimeMillis();
            topKPriorityQueue = (Processer.processQuery(query, 10, typeOfQuery.equals("C"), scoringFunction, Configuration.COMPRESSION, dynamicPruning.equals("DP")));
            timerEnd = System.currentTimeMillis();
            queryResult=Processer.getRankedQuery(topKPriorityQueue);

            if (queryResult == null) {
                System.out.println("No documents were found for this query.");
            } else {
                System.out.print("Results of document numbers: ");
                for (int i : queryResult) {
                    System.out.print(i + " ");
                }
                System.out.println("with execution time: " + (timerEnd - timerStart) + "ms");
            }

        } while (true);

        scanner.close();
    }

}