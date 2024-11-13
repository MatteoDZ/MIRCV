package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class DynamicPruning {

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
     * Function to calculate the top k documents for a query using dynamic pruning with max score.
     *
     * @param postings     ArrayList of PostingIndex objects each containing the posting for a query term.
     * @param k            Integer value of the maximum size of the priority queue.
     * @param TFIDForBM25  String to indicate which scoring function to use (TFIDF or BM25).
     * @param conjunctive  Boolean flag to indicate if the query is conjunctive.
     * @param compression  Boolean flag to indicate if the data is compressed.
     * @return             TopKPriorityQueue of Pair<Float, Integer> objects containing the top k documents and their scores.
     */
    public static TopKPriorityQueue<Pair<Float, Integer>> maxScore(ArrayList<PostingIndex> postings, Integer k, String TFIDForBM25, Boolean conjunctive, Boolean compression) throws IOException {
        TopKPriorityQueue<Pair<Float, Integer>> topK = new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));
        float[] upperBounds = new float[postings.size()];

        for (PostingIndex posting : postings) {
            posting.getBlocks();
            posting.next(compression);
        }

        sortPostings(postings);

        upperBounds[0] = postings.get(0).getUpperBound();
        for (int i = 1; i < postings.size(); i++) {
            upperBounds[i] = upperBounds[i - 1] + postings.get(i).getUpperBound();
        }

        if (conjunctive) {
            conjunctiveDPQ(topK, postings, TFIDForBM25, compression, upperBounds, k);
        }

    else {
        disjunctiveDPQ(topK, postings, TFIDForBM25, compression, upperBounds, k);
    }


        return topK;
    }

    /**
     * Function to sort the postings based on the upper bound of the postings.
     *
     * @param postings  ArrayList of PostingIndex objects each containing the posting for a query term.
     */
    public static void sortPostings(ArrayList<PostingIndex> postings) {
        postings.sort(Comparator.comparing(PostingIndex::getUpperBound));
    }

    /**
     * Function to check if all the postings points to the same docId.
     *
     * @param postings  ArrayList of PostingIndex objects each containing the posting for a query term.
     * @return          Boolean flag indicating if all the postings have the same docId.
     */
    private static boolean checkIfEquals(ArrayList<PostingIndex> postings){

        //if (start + 1 == end || start.equals(end)) {return true;}

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
     * @return              Integer value of the docId to which the postings are aligned.
     */
    public static int alignPostings(ArrayList<PostingIndex> postings, boolean compression) {
        int maxDocId = getHighestDocId(postings, postings.size());
        if (maxDocId == 0) { return 0; }

        for (int i = 0; i < postings.size(); i++) {
            Posting currentPosting = postings.get(i).getCurrentPosting();

            if (currentPosting == null) {return 0;}

            // Checks if all the postings pointed have the same docId, and if true, returns the docId
            boolean found = checkIfEquals(postings);
            if (found) {return currentPosting.getDocId();}

            // If the next docId is greater than the current maxDocId, update the maxDocId and reset the loop
            if (currentPosting.getDocId() > maxDocId) {
                maxDocId = currentPosting.getDocId();
                i = -1;
                continue;
            }

            // If the docId pointed is lower, get the next docId greater or equal to the current docId through nextGEQ
            if (currentPosting.getDocId() < maxDocId) {
                Posting nextPostingGEQ = postings.get(i).nextGEQ(maxDocId, compression);
                if (nextPostingGEQ == null) {return 0;}

                int geqDocId = nextPostingGEQ.getDocId();
                if (geqDocId > maxDocId) {
                    maxDocId = geqDocId;
                    i = -1;
                } else if (geqDocId == maxDocId) {
                    boolean foundGeq = checkIfEquals(postings);
                    if (foundGeq) {
                        return maxDocId;
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
    private static int getHighestDocId(ArrayList<PostingIndex> postingIndexes, Integer maxTerm) {

        // Set the inital value to -1
        int maxDoc = 0;

        for (int i = 0; i < maxTerm; i++) {
            if (postingIndexes.get(i).getCurrentPosting() != null) {
                maxDoc = Math.max(maxDoc, postingIndexes.get(i).getCurrentPosting().getDocId());
            } else {
                return 0;
            }
        }
        return maxDoc;
    }

    /**
     * Function to calculate the score of a conjunctive query using dynamic pruning.
     *
     * @param topKPQ            TopKPriorityQueue of Pair<Float, Integer> objects to store the top k documents and their scores.
     * @param postings          ArrayList of PostingIndex objects each containing the posting for a query term.
     * @param scoringFunction   String to indicate which scoring function to use (TFIDF or BM25).
     * @param compression       Boolean flag to indicate if the data is compressed.
     * @param upperBounds       Array of floats containing the upper bounds of the query terms.
     * @param k                 Integer value of the maximum size of the priority queue.
     */
    public static void conjunctiveDPQ(TopKPriorityQueue<Pair<Float, Integer>> topKPQ, ArrayList<PostingIndex> postings, String scoringFunction, boolean compression, float[] upperBounds, int k) throws IOException {
        float threshold = 0.0f;
        int highestDoc = alignPostings(postings, compression);


        if (stats.getNumDocs() == 0) {
            return;
        }

        while(highestDoc != 0) {
            boolean exit = false;
            float score = 0.0f;

            // Calculate the score for the last posting in the list.
            Posting currentPosting = postings.get(postings.size()-1).getCurrentPosting();
            if (currentPosting != null && currentPosting.getDocId() == highestDoc) {
                score += Scorer.getScore(currentPosting, postings.get(postings.size()- 1).getIdf(), scoringFunction);
                postings.get(postings.size()-1).next(compression);

                // Calculate the score for the rest of the postings in the list and update the score if current score + upperbound > threshold .
                for (int i = postings.size() - 2; i >= 0; i--) {
                    Posting notPrunedPosting = postings.get(i).getCurrentPosting();
                    if (notPrunedPosting != null && notPrunedPosting.getDocId() == highestDoc) {
                        if (score + upperBounds[i] < threshold) {
                            postings.get(i).next(compression);
                            exit = true;
                            break;
                        }
                        score += Scorer.getScore(notPrunedPosting, postings.get(i).getIdf(), scoringFunction);
                        postings.get(i).next(compression);
                    }
                }

            } else if (currentPosting == null) {
                return;
            }

            // If the score + upper bound is greater than the threshold, try to add the document to the priority queue.
            if(!exit) {
                if (topKPQ.offer(new Pair<>(score, highestDoc)) && topKPQ.size() == k) {
                    assert topKPQ.peek() != null;
                    threshold = topKPQ.peek().getValue0();
                }
            }
            // Align the postings to the next document.
            highestDoc = alignPostings(postings, compression);
        }
    }

    /**
     * Function to calculate the score of a disjunctive query.
     *
     * @param topKPQ            TopKPriorityQueue of Pair<Float, Integer> objects to store the top k documents and their scores.
     * @param postings          ArrayList of PostingIndex objects each containing the posting for a query term.
     * @param scoringFunction   String to indicate which scoring function to use (TFIDF or BM25).
     * @param compression       Boolean flag to indicate if the data is compressed.
     * @param upperBounds       Array of floats containing the upper bounds of the query terms.
     * @param k                 Integer value of the maximum size of the priority queue.
     */
    public static void disjunctiveDPQ(TopKPriorityQueue<Pair<Float, Integer>> topKPQ, ArrayList<PostingIndex> postings, String scoringFunction, boolean compression, float[] upperBounds, int k) throws IOException {
        int minDoc = stats.getNumDocs();
        int termId = 0;
        float threshold = 0.0f;
        boolean exit = false;

        for (int term = 0; term < postings.size(); term++) {
            if (postings.get(term).getCurrentPosting() != null) {
                int newMinDoc = Math.min(minDoc, postings.get(term).getCurrentPosting().getDocId());
                if (newMinDoc < minDoc) {
                    minDoc = newMinDoc;
                    termId = term;
                }
            }
        }

        if (minDoc == stats.getNumDocs()) {
            throw new IllegalArgumentException("The postingIndex list has a null value");
        }

        while (minDoc != stats.getNumDocs()) {

            float score = 0.0F;

            // Calculate the score for each posting in the list.
            Posting currentPosting = postings.get(termId).getCurrentPosting();
            if (currentPosting.getDocId() == minDoc) {
                score += Scorer.getScore(currentPosting, postings.get(termId).getIdf(), scoringFunction);
                postings.get(termId).next(compression);

                for (int i = postings.size()-1; i > 0; i--) {
                    if (postings.get(i).getCurrentPosting() != null && postings.get(i).getCurrentPosting().getDocId() == minDoc) {
                        Posting notPrunedPosting = postings.get(i).getCurrentPosting();
                        float tempScore = Scorer.getScore(notPrunedPosting, postings.get(i).getIdf(), scoringFunction);
                        postings.get(i).next(compression);
                        if (score + upperBounds[i] < threshold) {
                            exit = true;
                            break;
                        }
                        score += tempScore;

                    }
                }
                if(!exit) {
                    if (topKPQ.offer(new Pair<>(score, minDoc)) && topKPQ.size() == k) {
                        assert topKPQ.peek() != null;
                        threshold = topKPQ.peek().getValue0();
                    }
                }

            }

            exit = false;
            minDoc = stats.getNumDocs();
            for (int term = 0; term < postings.size(); term++) {
                if (postings.get(term).getCurrentPosting() != null) {
                    int newMinDoc = Math.min(minDoc, postings.get(term).getCurrentPosting().getDocId());
                    if (newMinDoc < minDoc) {
                        minDoc = newMinDoc;
                        termId = term;
                    }
                }
            }
            if (minDoc == 0){
                throw new RuntimeException();
            }

            // If there are no more documents, exit the loop.
            if (minDoc == stats.getNumDocs()) {
                break;
            }
        }
    }
}
