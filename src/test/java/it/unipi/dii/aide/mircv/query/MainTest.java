package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MainTest {
    @BeforeAll
    static void setUp() {
        Configuration.setUpPathTest();
    }

    @Test
    public void mainTestNoCompression() throws IOException {
        setUp();

        assertEquals(List.of(1, 5, 3, 6, 8, 2),
                Processer.getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "tfidf", false, false)));
        assertEquals(List.of(2, 3, 5, 6, 8, 1),
                Processer.getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "bm25", false, false)));
        assertEquals(List.of(5, 1, 2, 3, 8, 6),
                Processer.getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "tfidf", false, false)));
        assertEquals(List.of(1, 2, 3, 5, 6, 0),
                Processer.getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "bm25", false, false)));

        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                Processer.getRankedQuery(Processer.processQuery("costa rica", 10, true, "tfidf", false, false)));
        assertEquals(List.of(40, 41, 42, 43, 44, 45, 46, 47, 48, 39),
                Processer.getRankedQuery(Processer.processQuery("costa rica", 10, true, "bm25", false, false)));
        Processer.getRankedQuery(Processer.processQuery("costa rica", 10, false, "tfidf", false, false));
        assertEquals(List.of(40, 41, 42, 43, 44, 45, 46, 47, 48, 39),
                Processer.getRankedQuery(Processer.processQuery("costa rica", 10, false, "bm25", false, false)));

        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                Processer.getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "tfidf", false, false)));
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0),
                Processer.getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "bm25", false, false)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                Processer.getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "tfidf", false, false)));
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0),
                Processer.getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "bm25", false, false)));

        assertEquals(List.of(3, 6, 7, 1),
                Processer.getRankedQuery(Processer.processQuery("war II", 4, true, "tfidf", false, false)));
        assertEquals(List.of(3, 6, 7, 1),
                Processer.getRankedQuery(Processer.processQuery("war II", 4, true, "bm25", false, false)));
        assertEquals(List.of(3, 6, 7, 1),
                Processer.getRankedQuery(Processer.processQuery("war II", 4, false, "tfidf", false, false)));
        assertEquals(List.of(3, 6, 7, 1),
                Processer.getRankedQuery(Processer.processQuery("war II", 4, false, "bm25", false, false)));
    }


    @Test
    public void mainTestYesCompression() throws IOException {
        setUp();

        assertEquals(List.of(1, 5, 3, 6, 8, 2),
                Processer.getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "tfidf", true, false)));
        assertEquals(List.of(2, 3, 5, 6, 8, 1),
                Processer.getRankedQuery(Processer.processQuery("atomic bomb", 6, true, "bm25", true, false)));
        assertEquals(List.of(5, 1, 2, 3, 8, 6),
                Processer.getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "tfidf", true, false)));
        assertEquals(List.of(1, 2, 3, 5, 6, 0),
                Processer.getRankedQuery(Processer.processQuery("atomic bomb", 6, false, "bm25", true, false)));

        assertEquals(List.of(45, 42, 43, 47, 44, 41, 48, 46, 40, 39),
                Processer.getRankedQuery(Processer.processQuery("costa rica", 10, true, "tfidf", true, false)));
        assertEquals(List.of(40, 41, 42, 43, 44, 45, 46, 47, 48, 39),
                Processer.getRankedQuery(Processer.processQuery("costa rica", 10, true, "bm25", true, false)));
        Processer.getRankedQuery(Processer.processQuery("costa rica", 10, false, "tfidf", true, false));
        assertEquals(List.of(40, 41, 42, 43, 44, 45, 46, 47, 48, 39),
                Processer.getRankedQuery(Processer.processQuery("costa rica", 10, false, "bm25", true, false)));

        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                Processer.getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "tfidf", true, false)));
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0),
                Processer.getRankedQuery(Processer.processQuery("Manhattan project", 10, true, "bm25", true, false)));
        assertEquals(List.of(2, 7, 3, 5, 4, 6, 8, 1, 9, 0),
                Processer.getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "tfidf", true, false)));
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 0),
                Processer.getRankedQuery(Processer.processQuery("Manhattan project", 10, false, "bm25", true, false)));

        assertEquals(List.of(3, 6, 7, 1),
                Processer.getRankedQuery(Processer.processQuery("war II", 4, true, "tfidf", true, false)));
        assertEquals(List.of(3, 6, 7, 1),
                Processer.getRankedQuery(Processer.processQuery("war II", 4, true, "bm25", true, false)));
        assertEquals(List.of(3, 6, 7, 1),
                Processer.getRankedQuery(Processer.processQuery("war II", 4, false, "tfidf", true, false)));
        assertEquals(List.of(3, 6, 7, 1),
                Processer.getRankedQuery(Processer.processQuery("war II", 4, false, "bm25", true, false)));
    }
}
