package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LexiconTest {

    @Test
    public void writeNoCompressionTest() throws IOException {
        List<Integer> docIdsA = List.of(0, 1, 20, 300, 401, 450, 461, 500, 6000, 70000, 800000, 8000000, 8800000, 8800001);
        List<Integer> freqsA = List.of(10, 1, 2, 3, 41, 45, 46, 50, 600, 7000, 8000, 1000, 8800, 700);
        List<Integer> docIdsB = List.of(1, 11, 21, 35);
        List<Integer> freqsB = List.of(10, 5, 4, 5);
        String pathDoc = "data/test/testDoc.bin";
        String pathFreq= "data/test/testFreq.bin";
        String pathIndex= "data/test/testIndex.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        InvertedIndexFile invIndex = new InvertedIndexFile(pathIndex, pathDoc, pathFreq, 4);
        Long offsetA = invIndex.write(docIdsA, freqsA,false);
        Long offsetB = invIndex.write(docIdsB, freqsB,false);
        Lexicon lexicon = new Lexicon("data/test/Lexicon.bin");
        lexicon.writeFixed("a",offsetA, docIdsA, freqsA);
        lexicon.writeFixed("b",offsetB, docIdsB, freqsB);
        Long offsetLexiconA = lexicon.get("a").getOffsetInvertedIndex();
        Long offsetLexiconB = lexicon.get("b").getOffsetInvertedIndex();

        assertEquals(offsetA,offsetLexiconA);
        assertEquals(docIdsA, invIndex.getDocIds(offsetLexiconA, false));
        for(int i=0;i<docIdsA.size();i++) {
            assertEquals(freqsA.get(i), invIndex.getFreq(offsetLexiconA, docIdsA.get(i), false));
        }

        assertEquals(offsetB,offsetLexiconB);
        assertEquals(docIdsB, invIndex.getDocIds(offsetLexiconB, false));
        for(int i=0;i<docIdsB.size();i++) {
            assertEquals(freqsB.get(i), invIndex.getFreq(offsetLexiconB, docIdsB.get(i), false));
        }
    }

    @Test
    public void writeYesCompressionTest() throws IOException {
        List<Integer> docIdsA = List.of(0, 1, 20, 300, 401, 450, 461, 500, 6000, 70000, 800000, 8000000, 8800000, 8800001);
        List<Integer> freqsA = List.of(10, 1, 2, 3, 41, 45, 46, 50, 600, 7000, 8000, 1000, 8800, 700);
        List<Integer> docIdsB = List.of(1, 11, 21, 35);
        List<Integer> freqsB = List.of(10, 5, 4, 5);
        String pathDoc = "data/test/testDoc.bin";
        String pathFreq= "data/test/testFreq.bin";
        String pathIndex= "data/test/testIndex.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        InvertedIndexFile invIndex = new InvertedIndexFile(pathIndex, pathDoc, pathFreq, 4);
        Long offsetA = invIndex.write(docIdsA, freqsA,true);
        Long offsetB = invIndex.write(docIdsB, freqsB,true);
        Lexicon lexicon = new Lexicon("data/test/Lexicon.bin");
        lexicon.writeFixed("a",offsetA, docIdsA, freqsA);
        lexicon.writeFixed("b",offsetB, docIdsB, freqsB);
        Long offsetLexiconA = lexicon.findTerm("a").getOffsetInvertedIndex();
        Long offsetLexiconB = lexicon.findTerm("b").getOffsetInvertedIndex();

        assertEquals(offsetA,offsetLexiconA);
        assertEquals(docIdsA, invIndex.getDocIds(offsetLexiconA, true));
        for(int i=0;i<docIdsA.size();i++) {
            assertEquals(freqsA.get(i), invIndex.getFreq(offsetLexiconA, docIdsA.get(i), true));
        }

        assertEquals(offsetB,offsetLexiconB);
        assertEquals(docIdsB, invIndex.getDocIds(offsetLexiconB, true));
        for(int i=0;i<docIdsB.size();i++) {
            assertEquals(freqsB.get(i), invIndex.getFreq(offsetLexiconB, docIdsB.get(i), true));
        }
    }

    @Test
    public void padStringToLengthTest() {
        assertEquals("a                               ", Lexicon.padStringToLength("a"));
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Lexicon.padStringToLength("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test
    public void removePaddingTest() {
        assertEquals("a", Lexicon.removePadding("a                               "));
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Lexicon.removePadding("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

}
