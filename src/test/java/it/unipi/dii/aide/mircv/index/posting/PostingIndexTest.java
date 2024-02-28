package it.unipi.dii.aide.mircv.index.posting;

import org.junit.Test;
import java.util.Arrays;
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
}
