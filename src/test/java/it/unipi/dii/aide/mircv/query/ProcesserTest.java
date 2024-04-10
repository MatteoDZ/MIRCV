package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProcesserTest {

   @Test
    public void testQuery() throws IOException {
       // Configuration.setUpPathTest();

       boolean conjunctive = false;
       String scoringFunction = "tfidf";

        ArrayList<String> queryList = new ArrayList<>(Arrays.asList(
                "Python programming language",
                "passion for coding",
                "quick brown fox lazy dog",
                "versatile programming",
                "enjoy reading books"
        ));

        ArrayList<ArrayList<Integer>> groundTruth = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(4, 2)),
                new ArrayList<>(Arrays.asList(4, 2)),
                new ArrayList<>(Arrays.asList(4, 2)),
                new ArrayList<>(Arrays.asList(4, 2)),
                new ArrayList<>(List.of(4)),
                new ArrayList<>(List.of(4)),
                new ArrayList<>(List.of(4)),
                new ArrayList<>(List.of(4)),
                new ArrayList<>(List.of(9)),
                new ArrayList<>(List.of(9)),
                new ArrayList<>(List.of(9)),
                new ArrayList<>(List.of(9)),
                new ArrayList<>(List.of(9)),
                new ArrayList<>(List.of(9)),
                new ArrayList<>(List.of(9)),
                new ArrayList<>(List.of(9)),
                new ArrayList<>(List.of(3)),
                new ArrayList<>(List.of(3)),
                new ArrayList<>(List.of(3)),
                new ArrayList<>(List.of(3)),
                new ArrayList<>(List.of(3)),
                new ArrayList<>(List.of(3)),
                new ArrayList<>(List.of(3)),
                new ArrayList<>(List.of(3)),
                new ArrayList<>(Arrays.asList(4, 2)),
                new ArrayList<>(Arrays.asList(4, 2)),
                new ArrayList<>(Arrays.asList(4, 2)),
                new ArrayList<>(Arrays.asList(4, 2)),
                new ArrayList<>(List.of(4)),
                new ArrayList<>(List.of(4)),
                new ArrayList<>(List.of(4)),
                new ArrayList<>(List.of(4)),
                new ArrayList<>(List.of(8)),
                new ArrayList<>(List.of(8)),
                new ArrayList<>(List.of(8)),
                new ArrayList<>(List.of(8)),
                new ArrayList<>(List.of(8)),
                new ArrayList<>(List.of(8)),
                new ArrayList<>(List.of(8)),
                new ArrayList<>(List.of(8))
        ));
        ArrayList<ArrayList<Integer>> allResults = new ArrayList<>(); // Container for all results

        for (String query : queryList) {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    for(int k = 0; k < 2; k++) {
                        // PathAndFlags.DYNAMIC_PRUNING = (k == 1);
                        conjunctive = (i == 1);
                        scoringFunction = (j == 1) ? "bm25" : "tfidf";

                        allResults.add(Processer.getRankedQuery(Processer.processQuery(query, 2, conjunctive, scoringFunction)));
                    }
                }
            }
        }

        assertEquals(groundTruth, allResults);
    }


    @Test
    public void processQueryTest() throws IOException {
        // Configuration.setUpPathTest();
        System.out.println(Processer.processQuery("victim america", 4, false, "tfidf"));
    }
}
