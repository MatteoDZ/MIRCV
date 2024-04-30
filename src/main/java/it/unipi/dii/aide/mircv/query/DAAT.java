package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
     * Retrieves the minimum document ID from a list of PostingIndex objects.
     *
     * @param postings List of PostingIndex objects.
     * @return Minimum document ID.
     */
    private static int getMinDocId(ArrayList<PostingIndex> postings) {
        System.out.println("getMin");
        int min_doc = stats.getNumDocs();
        System.out.println("min_doc: " + min_doc);
        for (PostingIndex postingIndex : postings) {
            if (postingIndex.getPostingActual() != null) {
                System.out.println(postingIndex.getPostingActual().getDoc_id());
                min_doc = Math.min(min_doc, postingIndex.getPostingActual().getDoc_id());
            }
        }
        System.out.println("min_doc end: " + min_doc);
        return min_doc;
    }

    /**
     * Scores the collection based on a specified ranking model (TFIDF or BM25).
     *
     * @param postings     List of PostingIndex objects.
     * @param k            Number of top results to retrieve.
     * @param TFIDFOrBM25  The ranking model to be used (TFIDF or BM25).
     * @param conjunctive  Boolean flag indicating conjunctive (AND) or disjunctive (OR) operation.
     * @return Top-K results with their scores.
     */
    public static TopKPriorityQueue<Pair<Float, Integer>> scoreCollection(
            ArrayList<PostingIndex> postings, int k, String TFIDFOrBM25, boolean conjunctive, boolean compression) throws IOException {

        System.out.println("Started scoreCollection");

        // Initialize the posting lists.
        for (PostingIndex index : postings) {
            index.openList();
            index.next(compression);
        }

        System.out.println("Created postingIndex");

        // Initialize the priority queue for top-K results.
        TopKPriorityQueue<Pair<Float, Integer>> topK =
                new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));

        System.out.println("Created topk PQ: " + topK);

        // Determine the starting document ID based on the query type.
        int doc_id = conjunctive ? get_doc_id(postings, compression) : getMinDocId(postings);

        System.out.println("doc_id: " + doc_id);

        // If there are no documents matching the query, return null.
        if (doc_id == stats.getNumDocs()) {
            return null;
        }

        // Process each document and calculate the score.
        int doc_len = stats.getNumDocs();

        System.out.println("doc_len: " + doc_len);

        while (doc_id != doc_len) {


            float score = 0.0F;



            // Calculate the score for each posting in the list.
            for (PostingIndex postingIndex : postings) {
                Posting posting = postingIndex.getPostingActual();


                if (posting != null) {
                    if (posting.getDoc_id() == doc_id) {
                        score += Scorer.score(
                                posting,
                                postingIndex.getIdf(),
                                TFIDFOrBM25);
                        postingIndex.next(compression);
                        //System.out.println(doc_id + " " + score);
                    }
                } else if (conjunctive) {
                    return topK;
                }

            }

            // Add the document ID and its score to the priority queue.
            topK.offer(new Pair<>(score, doc_id));

            System.out.println(topK);

            // Move to the next document based on the query type.
            doc_id = conjunctive ? get_doc_id(postings, compression) : getMinDocId(postings);

            // If there are no more documents, exit the loop.
            if (doc_id == 0) {
                break;
            }
        }


        return topK;
    }

    /**
     * Retrieves the maximum document ID from a list of PostingIndex objects.
     *
     * @param postingIndices List of PostingIndex objects.
     * @return Maximum document ID.
     */
    private static int get_max_doc_id(ArrayList<PostingIndex> postingIndices) {
        int max_doc = 0;
        for (PostingIndex postingIndex : postingIndices) {
            if (postingIndex.getPostingActual() != null) {
                max_doc = Math.max(max_doc, postingIndex.getPostingActual().getDoc_id());
            } else {
                return 0;
            }
        }
        return max_doc;
    }

    /**
     * Checks if the document IDs in a list of PostingIndex objects are equal.
     *
     * @param postingIndices List of PostingIndex objects.
     * @return True if document IDs are equal, false otherwise.
     */
    private static boolean areEquals(ArrayList<PostingIndex> postingIndices) {
        if (postingIndices.get(0).getPostingActual() == null) {
            return false;
        }
        int doc_id = postingIndices.get(0).getPostingActual().getDoc_id();
        for (int i = 1; i < postingIndices.size(); i++) {
            if (postingIndices.get(i).getPostingActual() == null) {
                return false;
            }
            if (doc_id != postingIndices.get(i).getPostingActual().getDoc_id()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the document ID based on the maximum document ID in a list of PostingIndex objects.
     *
     * @param postingIndices List of PostingIndex objects.
     * @return Document ID.
     */
    private static int get_doc_id(ArrayList<PostingIndex> postingIndices, Boolean compression) {
        System.out.println("getDoc");
        int doc_id = get_max_doc_id(postingIndices);
        if (doc_id == 0) {
            return 0;
        }

        for (int i = 0; i < postingIndices.size(); i++) {
            // Check if all posting indices have equal document IDs.
            if (areEquals(postingIndices)) {
                return doc_id;
            }

            // Handle the case where a posting is null or has a higher document ID.
            if (postingIndices.get(i).getPostingActual() == null) {
                return 0;
            }

            if (postingIndices.get(i).getPostingActual().getDoc_id() > doc_id) {
                doc_id = postingIndices.get(i).getPostingActual().getDoc_id();
                i = -1; // Reset i to restart the loop.
                continue;
            }

            // Move to the next document ID if the current one is lower.
            if (postingIndices.get(i).getPostingActual().getDoc_id() < doc_id) {
                Posting geq = postingIndices.get(i).nextGEQ(doc_id, compression);
                if (geq == null) {
                    return 0;
                }
                if (geq.getDoc_id() > doc_id) {
                    doc_id = geq.getDoc_id();
                    i = -1; // Reset i to restart the loop.
                    continue;
                }
                if(geq.getDoc_id()==doc_id){
                    if(areEquals(postingIndices)){
                        return doc_id;
                    }
                    i=-1;
                }
            }
        }
        return 0;
    }

}
