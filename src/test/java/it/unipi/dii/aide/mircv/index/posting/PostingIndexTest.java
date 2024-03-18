package it.unipi.dii.aide.mircv.index.posting;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PostingIndexTest {

    @Test
    public void addPostingTest() {
        PostingIndex postings  = new PostingIndex("abc");
        assertEquals("abc", postings.getTerm());
        postings.addPosting(1);
        postings.addPosting(2);
        assertEquals(Arrays.asList(1,2), postings.getDocIds());
        assertEquals(Arrays.asList(1,1), postings.getFrequencies());
        postings.addPosting(2);
        assertEquals(Arrays.asList(1,2), postings.getDocIds());
        assertEquals(Arrays.asList(1,2), postings.getFrequencies());
    }

    @Test
    public void getDocIdsTest() {
        PostingIndex postings = new PostingIndex("abc", Arrays.asList(1, 2, 3), Arrays.asList(1, 1, 2));
        assertEquals(Arrays.asList(1,2,3), postings.getDocIds());
        assertEquals("abc", postings.getTerm());
        postings.addPosting(4);
        assertEquals(Arrays.asList(1,2,3,4), postings.getDocIds());
        postings.addPosting(4);
        assertEquals(Arrays.asList(1,2,3,4), postings.getDocIds());
    }

    @Test
    public void getFrequenciesTest() {
        PostingIndex postings = new PostingIndex("abc", Arrays.asList(1, 2, 3), Arrays.asList(1, 1, 2));
        assertEquals(Arrays.asList(1,1,2), postings.getFrequencies());
        assertEquals("abc", postings.getTerm());
        postings.addPosting(4);
        assertEquals(Arrays.asList(1,1,2,1), postings.getFrequencies());
        postings.addPosting(4);
        assertEquals(Arrays.asList(1,1,2,2), postings.getFrequencies());
    }

    @Test
    public void appendListTest() {
        PostingIndex postings1 = new PostingIndex("abc", Arrays.asList(1, 2, 3), Arrays.asList(1, 1, 2));
        PostingIndex postings2 = new PostingIndex("def", Arrays.asList(2, 4, 5), Arrays.asList(1, 1, 2));
        postings1.appendList(postings2);
        assertEquals(List.of(1,2,2,3,4,5), postings1.getDocIds());
        assertEquals(List.of(1,1,1,2,1,2), postings1.getFrequencies());
    }

}
