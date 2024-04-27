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
     * Retrieves the posting lists for the terms in the query.
     *
     * @param query      List of query terms.
     * @param conjunctive Boolean flag indicating conjunctive (AND) or disjunctive (OR) operation.
     * @return ArrayList of PostingIndex objects representing the posting lists of the query terms.
     */
    public static ArrayList<PostingIndex> getQueryPostingLists(ArrayList<String> query, boolean conjunctive, String scoringFun) throws IOException {
        /*ArrayList<PostingIndex> postingOfQuery = new ArrayList<>();
        Lexicon lexicon = new Lexicon();
        InvertedIndexFile invertedIndex = new InvertedIndexFile(Configuration.BLOCK_SIZE);
        for (String term : query) {
            LexiconData lexiconEntry = lexicon.get(term);
            List<Integer> docIds = invertedIndex.getDocIds(lexiconEntry.getOffsetInvertedIndex(), false);
            List<Integer> freqs = new ArrayList<>();
            for (Integer i : docIds){
                freqs.add(invertedIndex.getFreq(lexiconEntry.getOffsetInvertedIndex(), i, false));
            }
            PostingIndex postingIndex = new PostingIndex(term, docIds, freqs);

            if (lexiconEntry == null) {
                if (conjunctive) {
                    return null;
                }
                continue;
            }

            PostingIndex postingIndex = new PostingIndex(term, lexiconEntry.getOffsetInvertedIndex());
            postingIndex.setIdf(lexiconEntry.getIdf());
            System.out.println("POSTINGINDEX: " + postingIndex);
            if(false){ //PathAdnFlags.DYNAMIC_PRUNING
                if(scoringFun.equals("tfidf")){
                    postingIndex.setUpperBound(lexiconEntry.getUpperTFIDF());
                }else{
                    postingIndex.setUpperBound(lexiconEntry.getUpperBM25());
                }
            }
            postingOfQuery.add(postingIndex);
        }
        return postingOfQuery;*/




        ArrayList<PostingIndex> postingOfQuery = new ArrayList<>();
        PostingIndex postingIndex;
        for (String term : query) {
            LexiconData lexiconEntry = Lexicon.getInstance().get(term);
            if (lexiconEntry == null) {
                if (conjunctive) {
                    return null;
                }
                continue;
            }
            postingIndex=new PostingIndex(lexiconEntry.getTerm());
            postingIndex.setIdf(lexiconEntry.getIdf());
            /*if(PathAndFlags.DYNAMIC_PRUNING){
                if(scoringFun.equals("tfidf")){
                    postingIndex.setUpperBound(lexiconEntry.getUpperTFIDF());
                }else{
                    postingIndex.setUpperBound(lexiconEntry.getUpperBM25());
                }
            }*/
            postingOfQuery.add(postingIndex);
        }
        return postingOfQuery;



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
    public static TopKPriorityQueue<Pair<Float,Integer>> processQuery(String query, Integer k, Boolean conjunctive, String scoringFun) throws IOException {
        // Clean and preprocess the query.
        // String queryP = Preprocess.removeStopwords(query);
        List<String> cleaned = Preprocess.processText(query, Configuration.STEMMING_AND_STOPWORDS);

        // List<String> cleaned = Preprocess.processText(queryP, Configuration.STOPWORD_STEM_ENABLED);

        /*
        // Apply stopword removal and stemming if enabled.
        if (PathAndFlags.STOPWORD_STEM_ENABLED) {
            cleaned = Preprocess.removeStopwords(cleaned);
            cleaned = Preprocess.applyStemming(cleaned);
        }

         */



        // Check if the query is empty after preprocessing.
        if (cleaned.isEmpty()) {
            System.out.println("empty query");
            return null;
        }

        // Remove duplicates from the query terms.
        Set<String> queryDistinctWords = new HashSet<>(cleaned);

        // Retrieve posting lists for the query terms.
        ArrayList<PostingIndex> queryPostings = getQueryPostingLists(new ArrayList<>(queryDistinctWords), conjunctive,scoringFun);

        // Return null if no posting lists are retrieved.
        if (queryPostings == null || queryPostings.isEmpty()) {
            return null;
        }

        // Initialize a priority queue for the top-K results.
        TopKPriorityQueue<Pair<Float, Integer>> priorityQueue;

        /*
        // Choose between dynamic pruning and DAAT scoring based on the flag.
        if (PathAndFlags.DYNAMIC_PRUNING) {
            priorityQueue = MaxScoreDynamicPruning.maxScore(queryPostings,k,scoringFun,conjunctive);
        } else {
            priorityQueue = DAAT.scoreCollection(queryPostings, k, scoringFun, conjunctive);
        }
         */

        // priorityQueue = DAAT.scoreCollection(queryPostings, k, scoringFun, conjunctive);
        // priorityQueue = DAAT1.scoreCollection(queryPostings, k, scoringFun, conjunctive);
        priorityQueue = DAATchang.scoreCollection(queryPostings, k, scoringFun, conjunctive);
        assert priorityQueue != null;

        return priorityQueue;

    }
    public static ArrayList<Integer> getRankedQuery(TopKPriorityQueue<Pair<Float,Integer>>priorityQueue){
        // Return null if the priority queue is null.
        if (priorityQueue == null) {
            return null;
        }

        // Retrieve the document IDs from the priority queue.
        ArrayList<Integer> list = new ArrayList<>();
        while (!priorityQueue.isEmpty()) {
            Pair<Float, Integer> pair = priorityQueue.poll();
            list.add(pair.getValue1());
        }

        // Reverse the list to get the top-K results in descending order.
        Collections.reverse(list);
        return list;
    }
}