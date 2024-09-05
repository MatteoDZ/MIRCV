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

            minDoc = stats.getNumDocs();
            for (PostingIndex postingIndex : postings) {
                if (postingIndex.getCurrentPosting() != null) {
                    minDoc = Math.min(minDoc, postingIndex.getCurrentPosting().getDoc_id());
                }
                else {
                    minDoc = stats.getNumDocs();
                }
            }

            // If there are no more documents, exit the loop.
            if (minDoc == stats.getNumDocs()) {
                //System.out.println("I QUIT");
                break;
            }
        }
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

            if (currentPosting == null) {return 0;} //throw new IllegalArgumentException("The posting is null");}

            // Checks if all the postings pointed have the same docId, and if true, returns the docId
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
                if (nextPostingGEQ == null) {return 0;} //throw new IllegalArgumentException("nextGEQ returned a null value");}

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
        return 0;
    }

    /**
     * Retrieves the maximum document ID from a list of PostingIndex objects.
     *
     * @param postingIndexes List of PostingIndex objects.
     * @return Maximum document ID.
     */
    private static int getMaxDocId(ArrayList<PostingIndex> postingIndexes) {

        // Set the inital value to -1
        int maxDoc = 0;

        // Check each element in the postings list and update the maxDoc value
        for (PostingIndex postingIndex : postingIndexes) {
            if (postingIndex.getCurrentPosting() != null) {
                //System.out.println((postingIndex.getCurrentPosting().getDoc_id()) + " line 220");
                maxDoc = Math.max(maxDoc, postingIndex.getCurrentPosting().getDoc_id());
            } else {
                return 0;
            }
        }
        return maxDoc;
    }


    public static void conjunctiveQ(TopKPriorityQueue<Pair<Float, Integer>> topKPQ, ArrayList<PostingIndex> postings, String scoringFunction, boolean compression) throws IOException {
        int docId = alignPostings(postings, compression, getMaxDocId(postings));
        boolean exit = false;


        while (docId != stats.getNumDocs() && !exit) {

            //if (docId == -1) {break;}

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

                if (docId == 0) {
                    //System.out.println(docId);
                    exit = true;
                    break;
                }
            }

        }
    }
}
