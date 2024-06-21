package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import org.javatuples.Pair;

import java.io.IOException;
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
     * Retrieves the minimum document ID from a list of PostingIndex by comparing the current min_doc with.
     *
     * @param postings List of PostingIndex objects.
     * @return Minimum document ID.
     */
    protected static int getMinDocId(ArrayList<PostingIndex> postings) {

        // Set the minDoc value to the maximum doc_id
        int minDoc = stats.getNumDocs();

        // Check each element in the postings list and update the minDoc value
        for (PostingIndex postingIndex : postings) {
            if (postingIndex.getCurrentPosting() != null) {
                minDoc = Math.min(minDoc, postingIndex.getCurrentPosting().getDoc_id());
            }
            else {
                return stats.getNumDocs(); //Check
            }
        }
        return minDoc;
    }

    /**
     * Retrieves the maximum document ID from a list of PostingIndex objects.
     *
     * @param postingIndices List of PostingIndex objects.
     * @return Maximum document ID.
     */
    private static int getMaxDocId(ArrayList<PostingIndex> postingIndices) {

        // Set the maxDoc value to -1
        int maxDoc = -1;

        // Check each element in the postings list and update the maxDoc value
        for (PostingIndex postingIndex : postingIndices) {
            if (postingIndex.getCurrentPosting() != null) {
                maxDoc = Math.max(maxDoc, postingIndex.getCurrentPosting().getDoc_id());
            } else {
                return 0;
            }
        }
        return maxDoc;
    }

    /**
     * Checks if the document IDs in a list of PostingIndex objects are equal.
     *
     * @param postingIndices List of PostingIndex objects.
     * @return True if document IDs are equal, false otherwise.
     */
    private static boolean areEquals(ArrayList<PostingIndex> postingIndices) {

        // postingIndices list is null
        if (postingIndices.get(0).getCurrentPosting() == null) {
            return false;
        }

        // Return the doc_id of the first element of the postingIndicesList
        int doc_id = postingIndices.get(0).getCurrentPosting().getDoc_id();

        for (int i = 1; i < postingIndices.size(); i++) {
            if (postingIndices.get(i).getCurrentPosting() == null || doc_id != postingIndices.get(i).getCurrentPosting().getDoc_id()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Scores the collection based on a specified ranking model (TFIDF or BM25).
     *
     * @param postings     List of PostingIndex objects.
     * @param k            Number of top results to retrieve.
     * @param scoringFunction  The ranking model to be used (TFIDF or BM25).
     * @param conjunctive  Boolean parameter to indicate if the query is conjunctive (AND) or disjunctive (OR).
     * @param compression  Boolean parameter to indicate if compression is used or not
     * @return Top-K results with their scores.
     */
    public static TopKPriorityQueue<Pair<Float, Integer>> scoreCollection(
            ArrayList<PostingIndex> postings, int k, String scoringFunction, boolean conjunctive, boolean compression) throws IOException {


        // Initialize the posting lists.
        for (PostingIndex index : postings) {
            index.openList();
            index.next(compression);
        }

        // Initialize the priority queue for top-K results.
        TopKPriorityQueue<Pair<Float, Integer>> topKPQ = new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));

        // Determine the starting document ID based on the query type.
        int doc_id = conjunctive ? get_doc_id(postings, compression) : getMinDocId(postings);


        // If there are no documents matching the query, return null.
        if (doc_id == stats.getNumDocs()) {
            return null;
        }

        // Process each document and calculate the score.
        int doc_len = stats.getNumDocs();

        while (doc_id != doc_len) {

            float score = 0.0F;

            // Calculate the score for each posting in the list.
            for (PostingIndex postingIndex : postings) {
                Posting posting = postingIndex.getCurrentPosting();


                if (posting != null) {
                    if (posting.getDoc_id() == doc_id) {
                        score += Scorer.score(
                                        posting,
                                        postingIndex.getIdf(),
                                        scoringFunction);
                                        postingIndex.next(compression);
                        //System.out.println(doc_id + " " + score);
                    }
                } else if (conjunctive) {
                    return topKPQ;
                }
            }

            // Add the document ID and its score to the priority queue.
            topKPQ.offer(new Pair<>(score, doc_id));

            // Move to the next document based on the query type.
            doc_id = conjunctive ? get_doc_id(postings, compression) : getMinDocId(postings);

            // If there are no more documents, exit the loop.
            if (doc_id == 0) {
                System.out.println("I QUIT");
                break;
            }
        }


        return topKPQ;
    }


    /**
     * Retrieves the document ID based on the maximum document ID in a list of PostingIndex objects.
     *
     * @param postingIndices List of PostingIndex objects.
     * @return Document ID.
     */
    private static int get_doc_id(ArrayList<PostingIndex> postingIndices, Boolean compression) {
        int doc_id = getMaxDocId(postingIndices);
        if (doc_id == 0) {
            return 0;
        }

        for (int i = 0; i < postingIndices.size(); i++) {
            Posting currentPosting = postingIndices.get(i).getCurrentPosting();

            // Check if all posting indices have equal docIds.
            if (areEquals(postingIndices)) {
                return doc_id;
            }

            // Handle the case where the current posting is null.
            if (currentPosting == null) {
                return 0;
            }

            int currentDocId = currentPosting.getDoc_id();

            // Update doc_id if the current posting has a higher document ID.
            if (currentDocId > doc_id) {
                doc_id = currentDocId;
                i = -1; // Reset i to restart the loop.
                continue;
            }

            // Move to the next document ID if the current one is lower.
            if (currentDocId < doc_id) {
                Posting geq = postingIndices.get(i).nextGEQ(doc_id, compression);

                // Check if posting is null
                if (geq == null) {
                    return 0;
                }

                int geqDocId = geq.getDoc_id();

                if (geqDocId > doc_id) {
                    doc_id = geqDocId;
                    i = -1;
                } else if (geqDocId == doc_id) {
                    if (areEquals(postingIndices)) {
                        return doc_id;
                    }
                    i = -1;
                }
            }
        }

        return 0;
    }

}
