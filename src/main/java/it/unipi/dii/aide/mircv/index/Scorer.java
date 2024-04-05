package it.unipi.dii.aide.mircv.index;


import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.config.Configuration;
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

    /**
     * Calculates the score based on the specified scoring function (TFIDF or BM25).
     *
     * @param posting      The Posting object for a term in a document.
     * @param idf          The inverse document frequency of the term.
     * @param TFIDForBM25  The scoring function to be used (TFIDF or BM25).
     * @return The calculated score.
     */
    public static float score(Posting posting, float idf, String TFIDForBM25) throws IOException {
        if (TFIDForBM25.equals("tfidf")) {
            return calculateTFIDF(posting.getFrequency(), idf);
        } else if (TFIDForBM25.equals("bm25")) {
            return calculateBM25(posting, idf);
        }
        System.out.println("Non-valid scoring function chosen");
        return -1F;
    }

    /**
     * Calculates the TFIDF score for a term in a document.
     *
     * @param tf  The term frequency in the document.
     * @param idf The inverse document frequency of the term.
     * @return The calculated TFIDF score.
     */
    public static float calculateTFIDF(int tf, float idf) {
        return (float) ((1 + Math.log(tf)) * idf);
    }

    /**
     * Calculates the BM25 score for a term in a document.
     *
     * @param posting The Posting object for a term in a document.
     * @param idf     The inverse document frequency of the term.
     * @return The calculated BM25 score.
     */
    public static float calculateBM25(Posting posting, float idf) throws IOException {
        Statistics stats = new Statistics();
        FileChannel fc = FileChannel.open(Path.of(Configuration.PATH_DOC_TERMS), StandardOpenOption.READ, StandardOpenOption.WRITE);
        int doc_len = BinaryFile.readIntFromBuffer(fc, posting.getDoc_id()*4L);
        fc.close();
        float tf = (float) (1 + Math.log(posting.getFrequency()));
        return (float) ((tf * idf) / (tf + Configuration.BM25_K1 * (1 - Configuration.BM25_B + Configuration.BM25_B * (doc_len / stats.getAvgDocLen()))));
    }
}