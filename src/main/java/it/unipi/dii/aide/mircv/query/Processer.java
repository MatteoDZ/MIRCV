package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.Lexicon;
import it.unipi.dii.aide.mircv.index.merge.LexiconData;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.preprocess.Preprocess;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.*;

/**
 * Class for processing queries.
 */
public class Processer {

    /**
     * Retrieve the posting lists for each term of the passed query.
     *
     * @param query         ArrayList of strings containing each term of the query.
     * @param conjunctive   Boolean flag to check if the query is conjunctive or disjunctive.
     * @param TFIDForBM25   String value to determine the scoring method between TFIDF and BM25.
     * @param pruning       Boolean flag to check whether to apply DynamicPruning or not.
     * @return              ArrayList containing the postingIndexes retrieved.
     * @throws IOException
     */
    public static ArrayList<PostingIndex> retrievePostingLists(ArrayList<String> query, boolean conjunctive, String TFIDForBM25, boolean pruning) throws IOException {
        ArrayList<PostingIndex> retrievedPostings  = new ArrayList<>();
        Lexicon lexicon = Lexicon.getInstance();
        for (String term : query) {
            LexiconData lexiconData = lexicon.get(term);
            if (lexiconData == null && conjunctive) {
                return null;
            }
            else if (lexiconData == null) {
                continue;
            }

            PostingIndex postingIndex = new PostingIndex(term);
            postingIndex.setIdf(lexiconData.getIdf());
            if (pruning){
                if (TFIDForBM25.equals("tfidf")) {
                    postingIndex.setUpperBound(lexiconData.getUpperTFIDF());
                }
                else {
                    postingIndex.setUpperBound(lexiconData.getUpperBM25());
                }
            }
            retrievedPostings.add(postingIndex);
        }
        return retrievedPostings;
    }

    /**
     * Processes the query and returns a list of document IDs.
     *
     * @param query        Input query string.
     * @param k            Number of top results to retrieve.
     * @param conjunctive  Boolean flag indicating conjunctive (AND) or disjunctive (OR) operation.
     * @param scoringFun   The scoring function to be used.
     * @return ArrayList of document IDs matching the query.
     */
    public static TopKPriorityQueue<Pair<Float,Integer>> processQuery(String query, Integer k, Boolean conjunctive, String scoringFun, Boolean compression, Boolean pruning) throws IOException {

        // Clean and preprocess the query.
        List<String> processed = Preprocess.processText(query, Configuration.STEMMING_AND_STOPWORDS);
        Set<String> queryNoDuplicates = new HashSet<>(processed);
        ArrayList<String> cleaned = new ArrayList<>(queryNoDuplicates);
        if (cleaned.isEmpty()) {
            System.out.println("The query has no searchable terms");
            return null;
        }
        System.out.println(cleaned);

        // Retrieve posting list for each query term.
        ArrayList<PostingIndex> queryPostings = retrievePostingLists(cleaned, conjunctive,scoringFun, pruning);

        // Return null if no posting lists are retrieved.
        if (queryPostings == null || queryPostings.isEmpty()) {
            return null;
        }

        // Initialize a priority queue for the top-K results and gets the score.
        TopKPriorityQueue<Pair<Float, Integer>> topKPQ;
        if (pruning) {
            topKPQ = DynamicPruning.maxScore(queryPostings,k,scoringFun,conjunctive, compression);
        } else {
            topKPQ = DAAT.scoreQuery(queryPostings, k, scoringFun, conjunctive, compression);
        }

        return topKPQ;

    }

    /**
     * Retrieves the ranked query from the given priority queue.
     *
     * @param  topKPQ  The priority queue containing the query results.
     * @return         ArrayList of integers representing the ranked query.
     */
    public static ArrayList<Integer> getRankedQuery(TopKPriorityQueue<Pair<Float,Integer>> topKPQ){
        // Return null if the priority queue is null.
        if (topKPQ == null) {
            return null;
        }

        // Retrieve the document IDs from the priority queue.
        ArrayList<Integer> list = new ArrayList<>();
        while (!topKPQ.isEmpty()) {
            list.add(topKPQ.poll().getValue1());
        }

        // Reverse the list to get the top-K results in descending order.
        Collections.reverse(list);
        return list;
    }
}