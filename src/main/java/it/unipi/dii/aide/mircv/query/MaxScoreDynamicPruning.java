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

    /**
     * Sorts the posting indices by their upper bounds.
     *
     * @param postingIndices The posting indices to be sorted.
     */
    private static void sortByUpperbound(ArrayList<PostingIndex> postingIndices) {
        postingIndices.sort(Comparator.comparing(PostingIndex::getUpperBound));
    }

    /**
     * Performs the max score dynamic pruning algorithm for a set of postings.
     *
     * @param postings      The list of posting indices.
     * @param k             The number of top documents to retrieve.
     * @param TFIDFOrBM25   The scoring model to use (TFIDF or BM25).
     * @param conjunctive   Whether to use conjunctive retrieval.
     * @param compression   Whether to use compression.
     * @return A priority queue containing the top-k documents with their scores.
     */
    public static TopKPriorityQueue<Pair<Float, Integer>> maxScore(ArrayList<PostingIndex> postings, Integer k, String TFIDFOrBM25, Boolean conjunctive, Boolean compression) throws IOException {
        sortByUpperbound(postings);

        for (PostingIndex index : postings) {
            index.openList();
            index.next(compression);
        }

        TopKPriorityQueue<Pair<Float, Integer>> topKPriorityQueue = new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));

        float[] upperBounds = new float[postings.size()];
        upperBounds[0] = postings.get(0).getUpperBound();

        for (int i = 1; i < postings.size(); i++) {
            upperBounds[i] = upperBounds[i - 1] + postings.get(i).getUpperBound();
        }

        float threshold = 0.0F;
        int pivot = 0;
        int currentDocId;
        boolean skip;

        while (pivot < postings.size() && !isFinished(postings, pivot, postings.size())) {
            float score = 0.0F;
            skip = false;
            currentDocId = conjunctive ? getDocId(postings, pivot, postings.size(), compression) : getMinDocId(postings, pivot, postings.size());

            if (stats.getNumDocs() == 0) {
                break;
            }

            for (int i = pivot; i < postings.size(); i++) {
                if (postings.get(i).getCurrentPosting() != null && postings.get(i).getCurrentPosting().getDoc_id() == currentDocId) {
                    score += Scorer.score(postings.get(i).getCurrentPosting(), postings.get(i).getIdf(), TFIDFOrBM25);
                    postings.get(i).next(compression);
                } else if (conjunctive && postings.get(i).getCurrentPosting() == null) {
                    return topKPriorityQueue;
                }
            }

            for (int i = pivot - 1; i >= 0; i--) {
                if (score + upperBounds[i] < threshold) {
                    skip = true;
                    break;
                }

                if (postings.get(i).getCurrentPosting() != null) {
                    if (postings.get(i).getCurrentPosting().getDoc_id() < currentDocId) {
                        Posting geq = postings.get(i).nextGEQ(currentDocId, compression);

                        if (geq == null && conjunctive || (conjunctive && geq.getDoc_id() != currentDocId)) {
                            skip = true;
                            break;
                        }

                        if (geq != null && geq.getDoc_id() == currentDocId) {
                            score += Scorer.score(geq, postings.get(i).getIdf(), TFIDFOrBM25);
                        }
                    } else if (postings.get(i).getCurrentPosting().getDoc_id() > currentDocId && conjunctive) {
                        skip = true;
                        break;
                    } else if (postings.get(i).getCurrentPosting().getDoc_id() == currentDocId) {
                        score += Scorer.score(postings.get(i).getCurrentPosting(), postings.get(i).getIdf(), TFIDFOrBM25);
                    }
                } else if (postings.get(i).getCurrentPosting() == null && conjunctive) {
                    return topKPriorityQueue;
                }
            }

            if (skip && conjunctive) {
                continue;
            }

            if (topKPriorityQueue.offer(new Pair<>(score, currentDocId))&&topKPriorityQueue.size()==k) {
                threshold = topKPriorityQueue.peek().getValue0();
                while (pivot < postings.size() && upperBounds[pivot] < threshold) {
                    pivot++;
                }
            }
        }

        return topKPriorityQueue;
    }

    /**
     * Checks if the postings from start to end are finished.
     *
     * @param postingIndices The posting indices to check.
     * @param start          The start index.
     * @param end            The end index.
     * @return True if the postings are finished, false otherwise.
     */
    private static boolean isFinished(ArrayList<PostingIndex> postingIndices, Integer start, Integer end) {
        for (int i = start; i < end; i++) {
            if (postingIndices.get(i).getCurrentPosting() != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the minimum document ID from postings.
     *
     * @param postings The list of posting indices.
     * @param start    The start index.
     * @param end      The end index.
     * @return The minimum document ID.
     */
    private static int getMinDocId(ArrayList<PostingIndex> postings, Integer start, Integer end)  {
        int minDocId = stats.getNumDocs();

        for (int i = start; i < end; i++) {
            PostingIndex currentPostingIndex = postings.get(i);
            if (currentPostingIndex != null) {
                minDocId = Math.min(minDocId, currentPostingIndex.getCurrentPosting().getDoc_id());
            }
        }

        return minDocId;
    }

    /**
     * Gets the document ID using the max score strategy.
     *
     * @param postings The list of posting indices.
     * @param start    The start index.
     * @param end      The end index.
     * @return The document ID.
     */
    private static int getDocId(ArrayList<PostingIndex> postings, Integer start, Integer end, Boolean compression)  {
        int maxDocId = getMaxDocId(postings, start, end);

        if (maxDocId == 0) {
            return 0;
        }

        for (int i = start; i < end; i++) {
            if (areEquals(postings, start, end)) {
                return maxDocId;
            }

            if (postings.get(i).getCurrentPosting() == null) {
                return 0;
            }

            int currentDocId = postings.get(i).getCurrentPosting().getDoc_id();

            if (currentDocId > maxDocId) {
                maxDocId = currentDocId;
                i = start - 1; // Reset i to restart the loop.
                continue;
            }

            if (currentDocId < maxDocId) {
                Posting nextGEQPosting = postings.get(i).nextGEQ(maxDocId, compression);

                if (nextGEQPosting == null) {
                    return 0;
                }

                if (nextGEQPosting.getDoc_id() > maxDocId) {
                    maxDocId = nextGEQPosting.getDoc_id();
                    i = start - 1; // Reset i to restart the loop.
                    continue;
                }

                if (nextGEQPosting.getDoc_id() == maxDocId) {
                    if (areEquals(postings, start, end)) {
                        return maxDocId;
                    }
                    i = start - 1;
                }
            }
        }
        return 0;
    }

    /**
     * Checks if the postings are equal from start to end.
     *
     * @param postings The list of posting indices.
     * @param start    The start index.
     * @param end      The end index.
     * @return True if the postings are equal, false otherwise.
     */
    private static boolean areEquals(ArrayList<PostingIndex> postings, Integer start, Integer end) {
        if (start + 1 == end || start.equals(end)) {
            return true;
        }

        PostingIndex firstPosting = postings.get(0);
        if (firstPosting.getCurrentPosting() == null) {
            return false;
        }

        int referenceDocId = firstPosting.getCurrentPosting().getDoc_id();

        for (int i = start + 1; i < end; i++) {
            PostingIndex currentPosting = postings.get(i);
            if (currentPosting.getCurrentPosting() == null || currentPosting.getCurrentPosting().getDoc_id() != referenceDocId) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the maximum document ID from postings.
     *
     * @param postings The list of posting indices.
     * @param start    The start index.
     * @param end      The end index.
     * @return The maximum document ID.
     */
    private static int getMaxDocId(ArrayList<PostingIndex> postings, Integer start, Integer end) {
        int maxDocumentId = 0;

        for (int i = start; i < end; i++) {
            PostingIndex currentPostingIndex = postings.get(i);
            if (currentPostingIndex.getCurrentPosting() != null) {
                maxDocumentId = Math.max(maxDocumentId, currentPostingIndex.getCurrentPosting().getDoc_id());
            } else {
                return 0;
            }
        }

        return maxDocumentId;
    }

}
