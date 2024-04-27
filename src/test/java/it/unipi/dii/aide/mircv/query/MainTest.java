package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.javatuples.Pair;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainTest {
    @BeforeAll
    static void setUp() {
        Configuration.setUpPathTest();
    }

    @Test
    public void mainTest() throws IOException {
        setUp();
        long timerStart, timerEnd;
        TopKPriorityQueue<Pair<Float,Integer>> topKPriorityQueue;
        ArrayList<Integer> queryResult;
        String query, scoreFun;

        query = "atomic bomb";
        int chose = 1; // System.out.print("Daat(1) or exit(2)?");
        int chose1 = 1; //System.out.print("Conjunctive(1) or Disjunctive(2)?");
        scoreFun = "tfidf"; //System.out.print("Score function bm25 or tfidf->");



        timerStart = System.currentTimeMillis();
        topKPriorityQueue = Processer.processQuery(query, 10, chose1 == 1, scoreFun);
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
    }
}
