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
     * @return A priority queue containing the top-k documents with their scores.
     */
    public static TopKPriorityQueue<Pair<Float, Integer>> maxScore(ArrayList<PostingIndex> postings, int k, String TFIDFOrBM25, boolean conjunctive, Boolean compression) throws IOException {
        sortByUpperbound(postings);

        for (PostingIndex index : postings) {
            index.openList();
            index.next(compression);
        }

        TopKPriorityQueue<Pair<Float, Integer>> topKPriorityQueue = new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));
        float[] ub = new float[postings.size()];
        ub[0] = postings.get(0).getUpperBound();

        for (int i = 1; i < postings.size(); i++) {
            ub[i] = ub[i - 1] + postings.get(i).getUpperBound();
        }

        float threshold = 0.0F;
        int pivot = 0;
        int current;
        boolean skip;

        while (pivot < postings.size() && !isFinished(postings, pivot, postings.size())) {
            float score = 0.0F;
            skip = false;
            current = conjunctive ? get_doc_id(postings, pivot, postings.size(), compression) : getMinDocId(postings, pivot, postings.size());

            if (stats.getNumDocs() == 0) {
                break;
            }

            for (int i = pivot; i < postings.size(); i++) {
                if (postings.get(i).getPostingActual() != null && postings.get(i).getPostingActual().getDoc_id() == current) {
                    score += Scorer.score(postings.get(i).getPostingActual(), postings.get(i).getIdf(), TFIDFOrBM25);
                    postings.get(i).next(compression);
                } else if (conjunctive && postings.get(i).getPostingActual() == null) {
                    return topKPriorityQueue;
                }
            }

            for (int i = pivot - 1; i >= 0; i--) {
                if (score + ub[i] < threshold) {
                    skip = true;
                    break;
                }

                if (postings.get(i).getPostingActual() != null) {
                    if (postings.get(i).getPostingActual().getDoc_id() < current) {
                        Posting geq = postings.get(i).nextGEQ(current, compression);

                        if (geq == null && conjunctive || (conjunctive && geq.getDoc_id() != current)) {
                            skip = true;
                            break;
                        }

                        if (geq != null && geq.getDoc_id() == current) {
                            score += Scorer.score(geq, postings.get(i).getIdf(), TFIDFOrBM25);
                        }
                    } else if (postings.get(i).getPostingActual().getDoc_id() > current && conjunctive) {
                        skip = true;
                        break;
                    } else if (postings.get(i).getPostingActual().getDoc_id() == current) {
                        score += Scorer.score(postings.get(i).getPostingActual(), postings.get(i).getIdf(), TFIDFOrBM25);
                    }
                } else if (postings.get(i).getPostingActual() == null && conjunctive) {
                    return topKPriorityQueue;
                }
            }

            if (skip && conjunctive) {
                continue;
            }

            if (topKPriorityQueue.offer(new Pair<>(score, current))&&topKPriorityQueue.size()==k) {
                threshold = topKPriorityQueue.peek().getValue0();
                while (pivot < postings.size() && ub[pivot] < threshold) {
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
    private static boolean isFinished(ArrayList<PostingIndex> postingIndices, int start, int end) {
        for (int i = start; i < end; i++) {
            if (postingIndices.get(i).getPostingActual() != null) {
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
    private static int getMinDocId(ArrayList<PostingIndex> postings, int start, int end)  {
        int minDoc = stats.getNumDocs();

        for (int i = start; i < end; i++) {
            if (postings.get(i).getPostingActual() != null) {
                minDoc = Math.min(minDoc, postings.get(i).getPostingActual().getDoc_id());
            }
        }

        return minDoc;
    }

    /**
     * Gets the document ID using the max score strategy.
     *
     * @param postings The list of posting indices.
     * @param start    The start index.
     * @param end      The end index.
     * @return The document ID.
     */
    private static int get_doc_id(ArrayList<PostingIndex> postings, int start, int end, Boolean compression)  {
        int doc_id = get_max_doc_id(postings, start, end);

        if (doc_id == 0) {
            return 0;
        }

        for (int i = start; i < end; i++) {
            if (areEquals(postings, start, end)) {
                return doc_id;
            }

            if (postings.get(i).getPostingActual() == null) {
                return 0;
            }

            if (postings.get(i).getPostingActual().getDoc_id() > doc_id) {
                doc_id = postings.get(i).getPostingActual().getDoc_id();
                i = start - 1; // Reset i to restart the loop.
                continue;
            }

            if (postings.get(i).getPostingActual().getDoc_id() < doc_id) {
                Posting geq = postings.get(i).nextGEQ(doc_id, compression);

                if (geq == null) {
                    return 0;
                }

                if (geq.getDoc_id() > doc_id) {
                    doc_id = geq.getDoc_id();
                    i = start - 1; // Reset i to restart the loop.
                    continue;
                }

                if (geq.getDoc_id() == doc_id) {
                    if (areEquals(postings, start, end)) {
                        return doc_id;
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
    private static boolean areEquals(ArrayList<PostingIndex> postings, int start, int end) {
        if (start + 1 == end || start == end) {
            return true;
        }

        if (postings.get(0).getPostingActual() == null) {
            return false;
        }

        int doc_id = postings.get(0).getPostingActual().getDoc_id();

        for (int i = start + 1; i < end; i++) {
            if (postings.get(i).getPostingActual() != null) {
                if (postings.get(i).getPostingActual().getDoc_id() != doc_id) {
                    return false;
                }
            } else {
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
    private static int get_max_doc_id(ArrayList<PostingIndex> postings, int start, int end) {
        int doc_id = 0;

        for (int i = start; i < end; i++) {
            if (postings.get(i).getPostingActual() != null) {
                if (postings.get(i).getPostingActual().getDoc_id() > doc_id) {
                    doc_id = postings.get(i).getPostingActual().getDoc_id();
                }
            } else {
                return 0;
            }
        }

        return doc_id;
    }

}
