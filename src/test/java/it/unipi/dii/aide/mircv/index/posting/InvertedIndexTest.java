package it.unipi.dii.aide.mircv.index.posting;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class InvertedIndexTest {

    @Test
    public void addTest() {
        InvertedIndex invIndex = new InvertedIndex();
        invIndex.add(Arrays.asList("a", "b", "c"), 1);
        assertTrue(invIndex.isPresent("a"));
        assertTrue(invIndex.isPresent("b"));
        assertTrue(invIndex.isPresent("c"));
        assertFalse(invIndex.isPresent("z"));
        assertEquals(List.of(1), invIndex.searchTerm("a").getDocIds());
        assertEquals(List.of(1), invIndex.searchTerm("a").getFrequencies());
        assertEquals(List.of(1), invIndex.searchTerm("b").getDocIds());
        assertEquals(List.of(1), invIndex.searchTerm("b").getFrequencies());
        assertEquals(List.of(1), invIndex.searchTerm("c").getDocIds());
        assertEquals(List.of(1), invIndex.searchTerm("c").getFrequencies());
        invIndex.add(Arrays.asList("d", "e", "a", "a"), 2);
        assertEquals(Arrays.asList(1, 2), invIndex.searchTerm("a").getDocIds());
        assertEquals(Arrays.asList(1, 2), invIndex.searchTerm("a").getFrequencies());
        assertEquals(List.of(2), invIndex.searchTerm("d").getDocIds());
        assertEquals(List.of(1), invIndex.searchTerm("d").getFrequencies());
        assertEquals(List.of(2), invIndex.searchTerm("e").getDocIds());
        assertEquals(List.of(1), invIndex.searchTerm("e").getFrequencies());
    }

    @Test
    public void isPresentTest() {
        InvertedIndex invIndex = new InvertedIndex();
        invIndex.add(Arrays.asList("a", "b", "c"), 1);
        assertFalse(invIndex.isPresent("d"));
        assertTrue(invIndex.isPresent("a"));
    }

    @Test
    public void searchTermTest() {
        InvertedIndex invIndex = new InvertedIndex();
        invIndex.add(Arrays.asList("a", "b", "c"), 1);
        assertNull(invIndex.searchTerm("d"));
        assertEquals("a", invIndex.searchTerm("a").getTerm());
    }

    @Test
    public void sortTest(){
        InvertedIndex invIndex = new InvertedIndex();
        invIndex.add(Arrays.asList("z", "a", "b", "ba", "bb"), 1);
        assertEquals(Arrays.asList("a", "b", "ba", "bb", "z"), invIndex.sort());
    }

    @Test
    public void cleanTest(){
        InvertedIndex invIndex = new InvertedIndex();
        invIndex.add(Arrays.asList("z", "a", "b", "ba", "bb"), 1);
        assertTrue(invIndex.isPresent("a"));
        assertTrue(invIndex.isPresent("b"));
        invIndex.clean();
        assertFalse(invIndex.isPresent("a"));
        assertFalse(invIndex.isPresent("b"));
    }
}
