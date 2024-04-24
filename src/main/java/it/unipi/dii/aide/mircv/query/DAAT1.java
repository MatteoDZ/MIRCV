package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.InvertedIndexFile;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.*;

public class DAAT1 {

    public static TopKPriorityQueue<Pair<Float, Integer>> scoreCollection(ArrayList<PostingIndex> postings, int k, String TFIDFOrBM25, boolean conjunctive) throws IOException {
        if (conjunctive) {
            return conjunctive(postings, k, TFIDFOrBM25);
        } else {
            return disjunctive(postings, k, TFIDFOrBM25);
        }
    }

    protected static TopKPriorityQueue<Pair<Float, Integer>> conjunctive(ArrayList<PostingIndex> postings, int k, String TFIDFOrBM25) throws IOException {
        PostingIndex p1 = postings.get(0);
        postings.remove(p1);
        InvertedIndexFile invertedIndexFile = new InvertedIndexFile(Configuration.BLOCK_SIZE);
        long offsetP1 = p1.getOffsetInvertedIndex();
        List<Integer> docIds = invertedIndexFile.getDocIds(offsetP1, Configuration.COMPRESSION);
        TopKPriorityQueue<Pair<Float, Integer>> topK = new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));
        for (Integer docId : docIds) {
            float score = 0.0F;
            int freqP1 = invertedIndexFile.getFreqCache(offsetP1, docId, Configuration.COMPRESSION);
            for (PostingIndex postingIndex : postings) {
                int freq = invertedIndexFile.getFreqCache(postingIndex.getOffsetInvertedIndex(), docId, Configuration.COMPRESSION);
                // System.out.println(docId + "->" + freqP1 + " " + freq);
                if (freq  !=-1) {
                    Posting posting = new Posting(docId, freq + freqP1);
                    score += Scorer.score(posting, postingIndex.getIdf(), TFIDFOrBM25);
                }
            }
            topK.offer(new Pair<>(score, docId));
        }
        return topK;
    }

    protected static TopKPriorityQueue<Pair<Float, Integer>> disjunctive(ArrayList<PostingIndex> postings, int k, String TFIDFOrBM25) throws IOException {
        InvertedIndexFile invertedIndexFile = new InvertedIndexFile(Configuration.BLOCK_SIZE);
        TopKPriorityQueue<Pair<Float, Integer>> topK = new TopKPriorityQueue<>(k, Comparator.comparing(Pair::getValue0));
        Map<Integer, Pair<Integer, Float>> docIdsTotal = new HashMap<>();
        for(PostingIndex postingIndex: postings){
            long offset = postingIndex.getOffsetInvertedIndex();
            List<Integer> docIds = invertedIndexFile.getDocIds(offset, Configuration.COMPRESSION);
            for(Integer docId: docIds){
                int freq = invertedIndexFile.getFreqCache(offset, docId, Configuration.COMPRESSION);
                if(docIdsTotal.containsKey(docId)){
                    // docIdsTotal.get(docId).getValue1()+ postingIndex.getIdf(); -> NON SO SE VA BENE
                    docIdsTotal.put(docId, new Pair<>(
                            docIdsTotal.get(docId).getValue0() + freq, docIdsTotal.get(docId).getValue1() + postingIndex.getIdf()));
                }else {
                    docIdsTotal.put(docId, new Pair<>(freq, postingIndex.getIdf()));
                }
            }
        }
        for (Map.Entry<Integer, Pair<Integer, Float>> entry : docIdsTotal.entrySet()) {
            int docId = entry.getKey();
            Pair <Integer, Float> pair = entry.getValue();
            // System.out.println(docId + "->" + pair.getValue0());
            Posting posting = new Posting(docId, pair.getValue0());
            float score = Scorer.score(posting, pair.getValue1(), TFIDFOrBM25);
            topK.offer(new Pair<>(score, docId));
        }
        return topK;
    }

}