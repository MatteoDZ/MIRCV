package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import org.javatuples.Pair;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class QueryPerformanceEvaluationMainTest {
    @BeforeAll
    static void setUp() {
        Configuration.setUpPathTest();
    }


    public static ArrayList<Integer> getRankedQuery(TopKPriorityQueue<Pair<Float,Integer>> topKPQ){
        // Return null if the priority queue is null.
        if (topKPQ == null) {
            return null;
        }

        // Retrieve the document IDs from the priority queue.
        ArrayList<Integer> list = new ArrayList<>();
        while (!topKPQ.isEmpty()) {
            list.add(topKPQ.poll().getValue1());
        }

        // Reverse the list to get the top-K results in descending order.
        Collections.reverse(list);
        return list;
    }

    // Query results using collection_subset_top100.tar.gz
    @Test
    public void mainTestNoCompression() throws IOException {
        setUp();

        assertEquals(List.of(1, 5, 3, 6, 8, 2),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "tfidf", false, false)));
        assertEquals(List.of(1, 5, 3, 6, 8, 2),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "bm25", false, false)));
        assertEquals(List.of(5, 1, 2, 3, 8, 6),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "tfidf", false, false)));
        assertEquals(List.of(5, 1, 2, 3, 8, 6),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "bm25", false, false)));

        assertEquals(List.of(1, 5, 3, 6, 8, 2),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "tfidf", false, true)));
        assertEquals(List.of(1, 5, 3, 6, 8, 2),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "bm25", false, true)));
        assertEquals(List.of(5, 1, 2, 3, 8, 6),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "tfidf", false, true)));
        assertEquals(List.of(5, 1, 2, 3, 8, 6),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "bm25", false, true)));

        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, true, "tfidf", false, false)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, true, "bm25", false, false)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, false, "tfidf", false, false)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, false, "bm25", false, false)));

        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, true, "tfidf", false, true)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, true, "bm25", false, true)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, false, "tfidf", false, true)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, false, "bm25", false, true)));

        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "tfidf", false, false)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "bm25", false, false)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "tfidf", false, false)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "bm25", false, false)));

        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "tfidf", false, true)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "bm25", false, true)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "tfidf", false, true)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "bm25", false, true)));

        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, true, "tfidf", false, false)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, true, "bm25", false, false)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, false, "tfidf", false, false)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, false, "bm25", false, false)));

        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, true, "tfidf", false, true)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, true, "bm25", false, true)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, false, "tfidf", false, true)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, false, "bm25", false, true)));
    }

    // Query results using collection_subset_top100.tar.gz
    @Test
    public void mainTestYesCompression() throws IOException {
        setUp();

        assertEquals(List.of(1, 5, 3, 6, 8, 2),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "tfidf", true, false)));
        assertEquals(List.of(1, 5, 3, 6, 8, 2),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "bm25", true, false)));
        assertEquals(List.of(5, 1, 2, 3, 8, 6),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "tfidf", true, false)));
        assertEquals(List.of(5, 1, 2, 3, 8, 6),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "bm25", true, false)));

        assertEquals(List.of(1, 5, 3, 6, 8, 2),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "tfidf", true, true)));
        assertEquals(List.of(1, 5, 3, 6, 8, 2),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "bm25", true, true)));
        assertEquals(List.of(5, 1, 2, 3, 8, 6),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "tfidf", true, true)));
        assertEquals(List.of(5, 1, 2, 3, 8, 6),
                getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "bm25", true, true)));

        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, true, "tfidf", true, false)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, true, "bm25", true, false)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, false, "tfidf", true, false)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, false, "bm25", true, false)));

        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, true, "tfidf", true, true)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, true, "bm25", true, true)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, false, "tfidf", true, true)));
        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                getRankedQuery(Processer.processQuery("costa rica", 10, false, "bm25", true, true)));

        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "tfidf", true, false)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "bm25", true, false)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "tfidf", true, false)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "bm25", true, false)));

        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "tfidf", true, true)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "bm25", true, true)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "tfidf", true, true)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "bm25", true, true)));

        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, true, "tfidf", true, false)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, true, "bm25", true, false)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, false, "tfidf", true, false)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, false, "bm25", true, false)));

        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, true, "tfidf", true, true)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, true, "bm25", true, true)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, false, "tfidf", true, true)));
        assertEquals(List.of(3, 6, 7, 1),
                getRankedQuery(Processer.processQuery("war II", 4, false, "bm25", true, true)));
    }
}
