package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

public class DAAT1Test {

    @Test
    public void conjunctiveTest() throws IOException {

        PostingIndex p1 = new PostingIndex("ciao");
        PostingIndex p2 = new PostingIndex("mondo");

        ArrayList<PostingIndex> postings = new ArrayList<>();
        postings.add(p1);
        postings.add(p2);

        TopKPriorityQueue<Pair<Float, Integer>> topK = DAAT1.conjunctive(postings, 10, "TFIDF");
        System.out.println(topK);
    }

}
