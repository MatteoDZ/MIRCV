package it.unipi.dii.aide.mircv.index;


import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.*;

/**
 * Class representing Document At A Time (DAAT) query processing.
 */
public class DAAT {

    /**
     * Retrieves the minimum document ID from a list of PostingIndex objects.
     *
     * @param postings List of PostingIndex objects.
     * @return Minimum document ID.
     */
    private static int getMinDocId(ArrayList<PostingIndex> postings) {
        Statistics stats = new Statistics();
        int min_doc = stats.getNumDocs();
        for (PostingIndex postingIndex : postings) {
            if (postingIndex.getPostingActual() != null) {
                min_doc = Math.min(min_doc, postingIndex.getPostingActual().getDoc_id());
            }
        }
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
            ArrayList<PostingIndex> postings, int k, String TFIDFOrBM25, boolean conjunctive) throws IOException {

        /*
        // Initialize the posting lists.
        for (PostingIndex index : postings) {
            index.
            index.openList();
            index.next();
        }

         */
        System.out.println("DAAT 56 ");
        // Initialize the priority queue for top-K results.
        TopKPriorityQueue<Pair<Float, Integer>> topK =
                new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));

        // Determine the starting document ID based on the query type.
        int doc_id = conjunctive ? get_doc_id(postings) : getMinDocId(postings);
        System.out.println("Check docId: " + doc_id);

        // If there are no documents matching the query, return null.
        if (doc_id == 0) {
            System.out.println("SONO QUI");
            return null;
        }

        // Process each document and calculate the score.
        Statistics stats = new Statistics();
        int doc_len = stats.getNumDocs();

        int numWords = 0;
        while (numWords != postings.size()) {
            float score = 0.0F;

            // Calculate the score for each posting in the list.
            for (PostingIndex postingIndex : postings) {
                for (Posting posting : postingIndex.getPostings()) {
                    if (posting != null) {
                        if (posting.getDoc_id() == doc_id) {
                            score += Scorer.score(
                                    posting,
                                    postingIndex.getIdf(),
                                    TFIDFOrBM25);
                            //postingIndex.next();
                        }
                    } else if (conjunctive) {
                        System.out.println("DAAT 88: " + topK.size());
                        return topK;
                    }
                }
                //Posting posting = postingIndex.getPostings().get(0);
            }


            // Add the document ID and its score to the priority queue.
            topK.offer(new Pair<>(score, doc_id));

            // Move to the next document based on the query type.
            doc_id = conjunctive ? get_doc_id(postings) : getMinDocId(postings);

            // If there are no more documents, exit the loop.
            if (doc_id == 0) {
                break;
            }
            numWords++;
        }

        System.out.println("DAAT 106: " + topK.size());
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
        System.out.println("DAAT 119 ");
        for (PostingIndex postingIndex : postingIndices) {
            System.out.println("DAAT 121 ");
            if (postingIndex != null) {
                max_doc = Math.max(max_doc, postingIndex.getDocIds().get(0));
            } else {
                System.out.println("DAAT 125 ");
                return 0;
            }
        }
        System.out.println("DAAT 130: " + max_doc);
        return max_doc;
    }

    /**
     * Checks if the document IDs in a list of PostingIndex objects are equal.
     *
     * @param postingIndices List of PostingIndex objects.
     * @return True if document IDs are equal, false otherwise.
     */
    private static boolean areEquals(ArrayList<PostingIndex> postingIndices) {
        if (postingIndices.get(0).getPostings().get(0) == null) {
            return false;
        }
        int doc_id = postingIndices.get(0).getPostings().get(0).getDoc_id();
        for (int i = 1; i < postingIndices.size(); i++) {
            if (postingIndices.get(i).getPostings().get(0) == null) {
                return false;
            }
            if (doc_id != postingIndices.get(i).getPostings().get(0).getDoc_id()) {
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
    private static int get_doc_id(ArrayList<PostingIndex> postingIndices) {
        System.out.println("GETDOCID: " + postingIndices.get(0).getPostings().size());
        int doc_id = get_max_doc_id(postingIndices);
        if (doc_id == 0) {
            System.out.println("DAAT 160");
            return 0;
        }

        for (int i = 0; i < postingIndices.size(); i++) {
            //for (Posting p : postingIndices.get(i).getPostings()) {} TODO: IMPLEMENTARE
            System.out.println("DAAT 170 ");
            // Check if all posting indices have equal document IDs.
            if (areEquals(postingIndices)) {
                return doc_id;
            }

            // Handle the case where a posting is null or has a higher document ID.
            if (postingIndices.get(i).getPostings().get(0) == null) {
                System.out.println("DAAT 172");
                return 0;
            }

            if (postingIndices.get(i).getPostings().get(0).getDoc_id() > doc_id) {
                doc_id = postingIndices.get(i).getPostings().get(0).getDoc_id();
                i = -1; // Reset i to restart the loop.
                System.out.println("DAAT 182");
                continue;
            }

            // Move to the next document ID if the current one is lower.
            if (postingIndices.get(i).getPostings().get(0).getDoc_id() < doc_id) {
                //Posting geq = postingIndices.get(i).nextGEQ(doc_id); TODO: NON SO SE FUNZIONA
                Posting geq = postingIndices.get(i).getPostings().get(0);
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
        System.out.println("DAAT 208");
        return 0;
    }
}
