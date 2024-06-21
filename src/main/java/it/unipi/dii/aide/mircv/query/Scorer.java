package it.unipi.dii.aide.mircv.query;


import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.LFUCache;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.utils.Statistics;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Class responsible for scoring based on different models (TFIDF, BM25).
 */
public class Scorer {

    private static final FileChannel fc;
    private static int doc = -1;
    private static int docCount = 0;
    static Statistics stats = new Statistics();

    static {
        try {
            fc = FileChannel.open(Path.of(Configuration.PATH_DOC_TERMS), StandardOpenOption.READ, StandardOpenOption.WRITE);
            stats.readFromDisk();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static final LFUCache<Integer, Integer> lfuCache = new LFUCache<>(Configuration.DOC_TERMS_CACHE_SIZE);

    /**
     * Calculates the score based on the scoring function (TFIDF or BM25).
     *
     * @param posting      The Posting object for a term in a document.
     * @param idf          The inverse document frequency of the term.
     * @param scoringFunction  The scoring function to be used (TFIDF or BM25).
     * @return The calculated score.
     */
    public static float score(Posting posting, float idf, String scoringFunction) throws IOException {
        if (scoringFunction.equals("tfidf")) {
            return calculateTFIDF(posting.getFrequency(), idf);
        } else if (scoringFunction.equals("bm25")) {
            return calculateBM25(posting, idf);
        }
        System.out.println("Chosen scoring function not available");
        return -1F;
    }

    /**
     * Calculates the TFIDF score for a term in a document.
     *
     * @param tf  The term frequency in the document.
     * @param idf The inverse document frequency of the term.
     * @return The TFIDF score.
     */
    public static float calculateTFIDF(int tf, float idf) {
        return (float) ((1 + Math.log(tf)) * idf);
    }

    /**
     * Calculates the BM25 score for a term in a document.
     *
     * @param posting The Posting object for a term in a document.
     * @param idf     The inverse document frequency of the term.
     * @return The BM25 score.
     */
    public static float calculateBM25(Posting posting, float idf) throws IOException {
        docCount++;
        System.out.println(docCount);
        int doc_len = getDoc_len(posting.getDoc_id());
        float tf = (float) (1 + Math.log(posting.getFrequency()));
        return (float) ((tf * idf) / (tf + Configuration.BM25_K1 * (1 - Configuration.BM25_B + Configuration.BM25_B * (doc_len / stats.getAvgDocLen()))));
    }

    /**
     * Return the lenght of a document.
     *
     * @param docId The doc_id to get.
     * @return The length of the document corresponding to the docId
     */
    public static Integer getDoc_len(int docId) throws IOException {
        if (lfuCache.containsKey(docId)) {
            return lfuCache.get(docId);
        }
        int doc_len = BinaryFile.readIntFromBuffer(fc, docId*4L);
        lfuCache.put(docId, doc_len);
        return doc_len;
    }

    /**
     * Clears the cache.
     *
     */
    public static void clearCache() {
        lfuCache.clear();
    }


}