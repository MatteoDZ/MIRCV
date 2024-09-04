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
     * Scores the collection based on a specified ranking model (TFIDF or BM25).
     *
     * @param postings     List of PostingIndex objects.
     * @param k            Number of top results to retrieve.
     * @param scoringFunction  The ranking model to be used (TFIDF or BM25).
     * @param conjunctive  Boolean parameter to indicate if the query is conjunctive (AND) or disjunctive (OR).
     * @param compression  Boolean parameter to indicate if compression is used or not
     * @return Top-K results with their scores.
     */
    /*
    public static TopKPriorityQueue<Pair<Float, Integer>> scoreCollection(
            ArrayList<PostingIndex> postings, int k, String scoringFunction, boolean conjunctive, boolean compression) throws IOException {

        int doc_len = stats.getNumDocs();
        int doc_id = conjunctive ? get_doc_id(postings, compression) : getMinDocId(postings);

        // Initialize the posting lists.
        for (PostingIndex index : postings) {
            index.openList();
            index.next(compression);
        }

        // Initialize the priority queue for top-K results.
        TopKPriorityQueue<Pair<Float, Integer>> topKPQ = new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));

        // Determine the starting document ID based on the query type.



        // If there are no documents matching the query, return null.
        if (doc_id == stats.getNumDocs()) {
            return null;
        }

        // Process each document and calculate the score.

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

     */

    public static TopKPriorityQueue<Pair<Float, Integer>> scoreQuery(
            ArrayList<PostingIndex> postings, int k, String scoringFunction, boolean conjunctive, boolean compression) throws IOException {

        int doc_len = stats.getNumDocs();
        TopKPriorityQueue<Pair<Float, Integer>> topKPQ = new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));

        for (PostingIndex index : postings) {
            index.openList();
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

    public static void disjunctiveQ(TopKPriorityQueue<Pair<Float, Integer>> topKPQ, ArrayList<PostingIndex> postings, String scoringFunction, boolean compression) throws IOException {
        int minDoc = stats.getNumDocs();

        // Check each element in the postings list and update the minDoc value
        for (PostingIndex postingIndex : postings) {
            if (postingIndex.getCurrentPosting() != null) {
                minDoc = Math.min(minDoc, postingIndex.getCurrentPosting().getDoc_id());
            }
            else {
                minDoc = stats.getNumDocs(); //Check
            }
        }

        if (minDoc == stats.getNumDocs()) {
            throw new IllegalArgumentException("The postingIndex list has a null value");
        }

        while (minDoc != stats.getNumDocs()) {

            float score = 0.0F;

            // Calculate the score for each posting in the list.
            for (PostingIndex postingIndex : postings) {
                Posting posting = postingIndex.getCurrentPosting();


                if (posting != null) {
                    if (posting.getDoc_id() == minDoc) {
                        score += Scorer.score(
                                posting,
                                postingIndex.getIdf(),
                                scoringFunction);
                        postingIndex.next(compression);
                    }
                }
            }

            // Add the document ID and its score to the priority queue.
            topKPQ.offer(new Pair<>(score, minDoc));

            // Move to the next document based on the query type.
            for (PostingIndex postingIndex : postings) {
                if (postingIndex.getCurrentPosting() != null) {
                    minDoc = Math.min(minDoc, postingIndex.getCurrentPosting().getDoc_id());
                }
                else {
                    minDoc = stats.getNumDocs();
                }
            }

            // If there are no more documents, exit the loop.
            if (minDoc == -1) {
                //System.out.println("I QUIT");
                break;
            }
        }
    }

    /**
     * Retrieves the maximum document ID from a list of PostingIndex objects.
     *
     * @param postingIndexes List of PostingIndex objects.
     * @return Maximum document ID.
     */
    private static int getMaxDocId(ArrayList<PostingIndex> postingIndexes) {

        // Set the inital value to -1
        int maxDoc = -1;

        // Check each element in the postings list and update the maxDoc value
        for (PostingIndex postingIndex : postingIndexes) {
            if (postingIndex.getCurrentPosting() != null) {
                System.out.println((postingIndex.getCurrentPosting().getDoc_id()) + " line 220");
                maxDoc = Math.max(maxDoc, postingIndex.getCurrentPosting().getDoc_id());
            } else {
                return -1;
            }
        }
        return maxDoc;
    }

    private static boolean checkIfEquals(ArrayList<PostingIndex> postings){
        if (postings.get(0).getCurrentPosting() == null) {return false;}
        int trueDocId = postings.get(0).getCurrentPosting().getDoc_id();
        for (int i = 1; i < postings.size(); i++){
            if (postings.get(i).getCurrentPosting().getDoc_id() != trueDocId){
                return false;
            }
        }
        return true;
    }

    public static int alignPostings(ArrayList<PostingIndex> postings, boolean compression, int maxId) {
        int docId = maxId;
        if (docId == 0) { return 0; }

        for (int i = 0; i < postings.size(); i++) {
            Posting currentPosting = postings.get(i).getCurrentPosting();

            if (currentPosting == null) {return -1;} //throw new IllegalArgumentException("The posting is null");}

            // Checks if all the postings have the same docId, and if true, returns the docId
            boolean found = checkIfEquals(postings);
            if (found) {return currentPosting.getDoc_id();}

            // If the next docId
            if (currentPosting.getDoc_id() > docId) {
                docId = currentPosting.getDoc_id();
                i = -1;
                continue;
            }
            if (currentPosting.getDoc_id() < docId) {
                //System.out.println("currentPosting.getDoc_id() < docId RESULT = " + docId);
                Posting nextPostingGEQ = postings.get(i).nextGEQ(docId, compression);
                if (nextPostingGEQ == null) {return -1;} //throw new IllegalArgumentException("nextGEQ returned a null value");}

                int geqDocId = nextPostingGEQ.getDoc_id();
                if (geqDocId > docId) {
                    docId = geqDocId;
                    i = -1;
                    continue;
                } else if (geqDocId == docId) {
                    boolean foundGeq = checkIfEquals(postings);
                    if (foundGeq) {
                        return docId;
                    }
                    i = -1;
                }
            }
        }
        return -1;
    }


     public static void conjunctiveQ(TopKPriorityQueue<Pair<Float, Integer>> topKPQ, ArrayList<PostingIndex> postings, String scoringFunction, boolean compression) throws IOException {
         int docId = alignPostings(postings, compression, getMaxDocId(postings));


         while (docId != stats.getNumDocs()) {

             if (docId == -1) {break;}

             float score = 0.0F;

             // Calculate the score for each posting in the list.
             for (PostingIndex postingIndex : postings) {
                 Posting posting = postingIndex.getCurrentPosting();


                 if (posting != null) {
                     if (posting.getDoc_id() == docId) {
                         score += Scorer.score(
                                 posting,
                                 postingIndex.getIdf(),
                                 scoringFunction);
                         postingIndex.next(compression);
                     }
                 }

                 topKPQ.offer(new Pair<>(score, docId));
                 int maxId = getMaxDocId(postings);
                 docId = alignPostings(postings, compression, maxId);

                 if (docId == -1) {
                     System.out.println(docId);
                     break;
                 }
             }

         }
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
            Posting posting = postingIndices.get(i).getCurrentPosting();
            if (posting == null || doc_id != posting.getDoc_id()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the document ID based on the maximum document ID in a list of PostingIndex objects.
     *
     * @param postingIndexes List of PostingIndex objects.
     * @return Document ID.
     */
    private static int get_doc_id(ArrayList<PostingIndex> postingIndexes, Boolean compression) {
        int doc_id = getMaxDocId(postingIndexes);

        if (doc_id == 0) {
            return 0;
        }

        for (int i = 0; i < postingIndexes.size(); i++) {
            Posting currentPosting = postingIndexes.get(i).getCurrentPosting();

            // Check if all posting indices have equal docIds.
            if (areEquals(postingIndexes)) {
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
                Posting geq = postingIndexes.get(i).nextGEQ(doc_id, compression);

                // Check if posting is null
                if (geq == null) {
                    return 0;
                }

                int geqDocId = geq.getDoc_id();

                if (geqDocId > doc_id) {
                    doc_id = geqDocId;
                    i = -1;
                } else if (geqDocId == doc_id) {
                    if (areEquals(postingIndexes)) {
                        return doc_id;
                    }
                    i = -1;
                }
            }
        }

        return 0;
    }
}
