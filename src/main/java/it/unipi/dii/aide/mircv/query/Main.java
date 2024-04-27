package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.config.Configuration;
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
        Scanner scanner = new Scanner(System.in);
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

            System.out.print("Daat(1) or Dynamic Pruning(2) or exit(3)?");
            int chose = Integer.parseInt(scanner.nextLine());
            if (chose != 1 && chose != 2 && chose != 3) {
                System.out.println("no good choice, please repeat");
                continue;
            } else if (chose == 3) {
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

            Configuration.DYNAMIC_PRUNING = chose != 1;
            timerStart = System.currentTimeMillis();
            topKPriorityQueue = (Processer.processQuery(query, 10, chose1 == 1, scoreFun, Configuration.COMPRESSION, Configuration.DYNAMIC_PRUNING));
            timerEnd = System.currentTimeMillis();
            queryResult=Processer.getRankedQuery(topKPriorityQueue);

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

        scanner.close();
    }

}