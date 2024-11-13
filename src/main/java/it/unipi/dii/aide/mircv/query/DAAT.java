package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import org.javatuples.Pair;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;

public class DAAT {

    private static final Statistics stats;

    static {
        stats = new Statistics();
        try {
            stats.readFromDisk();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Function that to calculate the score of a query.
     *
     * @param postings          ArrayList of PostingIndex objects to be scored.
     * @param k                 Integer number of top documents to be found in the PriorityQueue.
     * @param scoringFunction   String to indicate which scoring function to use (TFIDF or BM25).
     * @param conjunctive       Boolean flag to indicate if the query is conjunctive or disjunctive.
     * @param compression       Boolean flag to indicate if the data is compressed.
     * @return                  TopKPriorityQueue of Pair<Float, Integer> objects containing the top k documents and their scores sorted in ascending order.
     * @throws IOException
     */
    public static TopKPriorityQueue<Pair<Float, Integer>> scoreQuery(ArrayList<PostingIndex> postings, int k, String scoringFunction, boolean conjunctive, boolean compression) throws IOException {

        TopKPriorityQueue<Pair<Float, Integer>> topKPQ = new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));

        for (PostingIndex index : postings) {
            index.getBlocks();
            index.next(compression);
        }

        if (!conjunctive) {
            disjunctiveQ(topKPQ, postings, scoringFunction, compression);
        }
        else {
            conjunctiveQ(topKPQ, postings, scoringFunction, compression);
        }

        return topKPQ;
    }

    /**
     * Function to calculate the score of a disjunctive query.
     *
     * @param topKPQ            TopKPriorityQueue of Pair<Float, Integer> objects to store the top k documents and their scores.
     * @param postings          ArrayList of PostingIndex objects each containing the posting for a query term.
     * @param scoringFunction   String to indicate which scoring function to use (TFIDF or BM25).
     * @param compression       Boolean flag to indicate if the data is compressed.
     * @throws IOException
     */
    public static void disjunctiveQ(TopKPriorityQueue<Pair<Float, Integer>> topKPQ, ArrayList<PostingIndex> postings, String scoringFunction, boolean compression) throws IOException {
        int minDoc = stats.getNumDocs();

        // Check each element in the postings list and update the minDoc value
        for (PostingIndex postingIndex : postings) {
            if (postingIndex.getCurrentPosting() != null) {
                minDoc = Math.min(minDoc, postingIndex.getCurrentPosting().getDocId());
            }
            else {
                minDoc = stats.getNumDocs();
            }
        }

        if (minDoc == stats.getNumDocs()) {
            throw new IllegalArgumentException("The postingIndex list has a null value");
        }

        // Loop through the postings list and calculate the score for each posting
        while (minDoc != stats.getNumDocs()) {

            float score = 0.0F;

            // Calculate the score for each posting in the list.
            for (PostingIndex postingIndex : postings) {
                Posting posting = postingIndex.getCurrentPosting();

                // If the posting is not null and the docId is equal to the minDoc, calculate the score
                if (posting != null) {
                    if (posting.getDocId() == minDoc) {
                        score += Scorer.getScore(posting, postingIndex.getIdf(), scoringFunction);
                        postingIndex.next(compression);
                    }
                }
            }

            // Add the document ID and its score to the priority queue.
            topKPQ.offer(new Pair<>(score, minDoc));

            // Get the next posting with the minimum docId
            minDoc = stats.getNumDocs();
            for (PostingIndex postingIndex : postings) {
                if (postingIndex.getCurrentPosting() != null) {
                    minDoc = Math.min(minDoc, postingIndex.getCurrentPosting().getDocId());
                }
                else {
                    minDoc = stats.getNumDocs();
                }
            }

            // If there are no more documents, exit the loop.
            if (minDoc == stats.getNumDocs()) {
                break;
            }
        }
    }

    /**
     * Function to check if all the postings points to the same docId.
     *
     * @param postings  ArrayList of PostingIndex objects each containing the posting for a query term.
     * @return          Boolean flag indicating if all the postings have the same docId.
     */
    private static boolean checkIfEquals(ArrayList<PostingIndex> postings){
        if (postings.get(0).getCurrentPosting() == null) {return false;}
        int trueDocId = postings.get(0).getCurrentPosting().getDocId();
        for (int i = 1; i < postings.size(); i++){
            if (postings.get(i).getCurrentPosting().getDocId() != trueDocId){
                return false;
            }
        }
        return true;
    }

    /**
     * Function to align each PostingIndex in postings to the same docId.
     *
     * @param postings      ArrayList of PostingIndex objects each containing the posting for a query term.
     * @param compression   Boolean flag to indicate if the data is compressed.
     * @param maxId         Integer value of the maximum docId currently pointed by the postings.
     * @return              Integer value of the docId to which the postings are aligned.
     */
    public static int alignPostings(ArrayList<PostingIndex> postings, boolean compression, int maxId) {
        int docId = maxId;
        if (docId == 0) { return 0; }

        // Loop through the postings list
        for (int i = 0; i < postings.size(); i++) {
            Posting currentPosting = postings.get(i).getCurrentPosting();

            if (currentPosting == null) {return 0;}

            // Checks if all the postings pointed have the same docId, and if true, returns the docId
            boolean found = checkIfEquals(postings);
            if (found) {return currentPosting.getDocId();}

            // If the docId pointed is higher change docId
            if (currentPosting.getDocId() > docId) {
                docId = currentPosting.getDocId();
                i = -1;
                continue;
            }

            // If the docId pointed is lower, get the next docId greater or equal to the current docId through nextGEQ
            if (currentPosting.getDocId() < docId) {
                Posting nextPostingGEQ = postings.get(i).nextGEQ(docId, compression);
                if (nextPostingGEQ == null) {return 0;}

                int geqDocId = nextPostingGEQ.getDocId();
                if (geqDocId > docId) {
                    docId = geqDocId;
                    i = -1;
                } else if (geqDocId == docId) {
                    boolean foundGeq = checkIfEquals(postings);
                    if (foundGeq) {
                        return docId;
                    }
                    i = -1;
                }
            }
        }
        return 0;
    }

    /**
     * Function to retrieve the maximum document ID from a list of PostingIndex objects.
     *
     * @param postingIndexes List of PostingIndex objects.
     * @return Maximum document ID.
     */
    private static int getHighestDocId(ArrayList<PostingIndex> postingIndexes) {

        // Set the inital value to -1
        int maxDoc = -1;

        // Check each element in the postings list and update the maxDoc value
        for (PostingIndex postingIndex : postingIndexes) {
            if (postingIndex.getCurrentPosting() != null) {
                maxDoc = Math.max(maxDoc, postingIndex.getCurrentPosting().getDocId());
            } else {
                return 0;
            }
        }
        return maxDoc;
    }

    /**
     * Function to calculate the score of a conjunctive query.
     *
     * @param topKPQ            TopKPriorityQueue of Pair<Float, Integer> objects to store the top k documents and their scores.
     * @param postings          ArrayList of PostingIndex objects each containing the posting for a query term.
     * @param scoringFunction   String to indicate which scoring function to use (TFIDF or BM25).
     * @param compression       Boolean flag to indicate if the data is compressed.
     * @throws IOException
     */
    public static void conjunctiveQ(TopKPriorityQueue<Pair<Float, Integer>> topKPQ, ArrayList<PostingIndex> postings, String scoringFunction, boolean compression) throws IOException {
        int docId = alignPostings(postings, compression, getHighestDocId(postings));

        while (docId != 0) {

            float score = 0.0F;

            // Calculate the score for each posting in the list.
            for (PostingIndex postingIndex : postings) {
                Posting posting = postingIndex.getCurrentPosting();

                // If the posting is not null and the docId is equal to the minDoc, calculate the score
                if (posting != null) {
                    if (posting.getDocId() == docId) {
                        score += Scorer.getScore(posting, postingIndex.getIdf(), scoringFunction);
                        postingIndex.next(compression);
                    }
                }

            }

            // Add the document ID and its score to the priority queue if the score is higher than the lowest on the priority queue.
            topKPQ.offer(new Pair<>(score, docId));
            // Get the next posting with the highest docId of the currently pointed.
            int maxId = getHighestDocId(postings);
            // Align the postings to the same docId.
            docId = alignPostings(postings, compression, maxId);

        }
    }
}
