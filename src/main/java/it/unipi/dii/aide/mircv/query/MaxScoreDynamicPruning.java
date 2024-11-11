package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class MaxScoreDynamicPruning {

    private static final Statistics stats;

    static {
        stats = new Statistics();
        try {
            stats.readFromDisk();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static TopKPriorityQueue<Pair<Float, Integer>> maxScore(ArrayList<PostingIndex> postings, Integer k, String TFIDForBM25, Boolean conjunctive, Boolean compression) throws IOException {
        TopKPriorityQueue<Pair<Float, Integer>> topK = new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));
        float[] upperBounds = new float[postings.size()];

        for (PostingIndex posting : postings) {
            posting.openList();
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

    public static void sortPostings(ArrayList<PostingIndex> postings) {
        postings.sort(Comparator.comparing(PostingIndex::getUpperBound));
    }

    private static boolean checkIfEquals(ArrayList<PostingIndex> postings){

        //if (start + 1 == end || start.equals(end)) {return true;}

        if (postings.get(0).getCurrentPosting() == null) {return false;}
        int trueDocId = postings.get(0).getCurrentPosting().getDoc_id();
        for (int i = 1; i < postings.size(); i++){
            if (postings.get(i).getCurrentPosting().getDoc_id() != trueDocId){
                return false;
            }
        }
        return true;
    }

    public static int alignPostings(ArrayList<PostingIndex> postings, boolean compression) {
        int maxDocId = getHighestDocId(postings, postings.size());
        if (maxDocId == 0) { return 0; }

        for (int i = 0; i < postings.size(); i++) {
            Posting currentPosting = postings.get(i).getCurrentPosting();

            if (currentPosting == null) {return 0;}

            // Checks if all the postings pointed have the same docId, and if true, returns the docId
            boolean found = checkIfEquals(postings);
            if (found) {return currentPosting.getDoc_id();}

            // If the next docId
            if (currentPosting.getDoc_id() > maxDocId) {
                maxDocId = currentPosting.getDoc_id();
                i = -1;
                continue;
            }
            if (currentPosting.getDoc_id() < maxDocId) {
                Posting nextPostingGEQ = postings.get(i).nextGEQ(maxDocId, compression);
                if (nextPostingGEQ == null) {return 0;} //throw new IllegalArgumentException("nextGEQ returned a null value");}

                int geqDocId = nextPostingGEQ.getDoc_id();
                if (geqDocId > maxDocId) {
                    maxDocId = geqDocId;
                    i = -1;
                    continue;
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

    private static int getHighestDocId(ArrayList<PostingIndex> postingIndexes, Integer maxTerm) {

        // Set the inital value to -1
        int maxDoc = 0;

        for (int i = 0; i < maxTerm; i++) {
            if (postingIndexes.get(i).getCurrentPosting() != null) {
                maxDoc = Math.max(maxDoc, postingIndexes.get(i).getCurrentPosting().getDoc_id());
            } else {
                return 0;
            }
        }
        return maxDoc;
    }

    public static void conjunctiveDPQ(TopKPriorityQueue<Pair<Float, Integer>> topKPQ, ArrayList<PostingIndex> postings, String scoringFunction, boolean compression, float[] upperBounds, int k) throws IOException {
        float threshold = 0.0f;
        int highestDoc = alignPostings(postings, compression);


        if (stats.getNumDocs() == 0) {
            return;
        }

        while(highestDoc != 0) {
            boolean exit = false;
            float score = 0.0f;

            Posting currentPosting = postings.get(postings.size()-1).getCurrentPosting();
            if (currentPosting != null && currentPosting.getDoc_id() == highestDoc) {
                score += Scorer.getScore(currentPosting, postings.get(postings.size()- 1).getIdf(), scoringFunction);
                postings.get(postings.size()-1).next(compression);

                for (int i = postings.size() - 2; i >= 0; i--) {
                    Posting notPrunedPosting = postings.get(i).getCurrentPosting();
                    if (notPrunedPosting != null && notPrunedPosting.getDoc_id() == highestDoc) {
                        float tempScore = Scorer.getScore(notPrunedPosting, postings.get(i).getIdf(), scoringFunction);
                        postings.get(i).next(compression);
                        if (score + upperBounds[i] < threshold) {
                            exit = true;
                            break;
                        }
                        score += tempScore;
                    }
                }

            } else if (currentPosting == null) {
                return;
            }

            if(!exit) {
                if (topKPQ.offer(new Pair<>(score, highestDoc)) && topKPQ.size() == k) {
                    assert topKPQ.peek() != null;
                    threshold = topKPQ.peek().getValue0();
                }
            }
            highestDoc = alignPostings(postings, compression);
        }
    }

    public static void disjunctiveDPQ(TopKPriorityQueue<Pair<Float, Integer>> topKPQ, ArrayList<PostingIndex> postings, String scoringFunction, boolean compression, float[] upperBounds, int k) throws IOException {
        int minDoc = stats.getNumDocs();
        int termId = 0;
        float threshold = 0.0f;
        boolean exit = false;

        for (int term = 0; term < postings.size(); term++) {
            if (postings.get(term).getCurrentPosting() != null) {
                int newMinDoc = Math.min(minDoc, postings.get(term).getCurrentPosting().getDoc_id());
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
            if (currentPosting.getDoc_id() == minDoc) {
                score += Scorer.getScore(currentPosting, postings.get(termId).getIdf(), scoringFunction);
                postings.get(termId).next(compression);

                for (int i = termId + 1; i < postings.size(); i++) {
                    if (postings.get(i).getCurrentPosting() != null && postings.get(i).getCurrentPosting().getDoc_id() == minDoc) {
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
                    int newMinDoc = Math.min(minDoc, postings.get(term).getCurrentPosting().getDoc_id());
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
