package it.unipi.dii.aide.mircv.query;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DAATTest {

    @BeforeEach
    public void setUp(){
        Configuration.setUpPathTest();
    }

    @Test
    public void getMinDocIdTest(){
        PostingIndex p1 = new PostingIndex("a", List.of(4, 6), List.of(1,1));
        PostingIndex p2 = new PostingIndex("b", List.of(2, 3), List.of(1,1));
        ArrayList<PostingIndex> postings = new ArrayList<>();
        postings.add(p1);
        postings.add(p2);
        assertEquals(2, DAAT.getMinDocId(postings));
    }
}
