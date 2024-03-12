package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class MergerTest {

    @Test
    public void writeCompressionFalseTest() throws IOException {
        String pathTest1 = "data/test/test1.bin";
        String pathTest2 = "data/test/test2.bin";
        String pathTest3 = "data/test/test3.bin";
        String pathDocIds = "data/test/docIds";
        String pathFreqs = "data/test/freqs";
        String pathLexicon = "data/test/flexicon";
        String pathInvIndex = "data/test/invIndex";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("a"), 1);
        inv1.add(List.of("b"), 2);
        inv1.add(List.of("a"), 3);
        inv1.add(List.of("b"), 4);
        BinaryFile.writeBlock(inv1, pathTest1);
        InvertedIndex inv2 = new InvertedIndex();
        inv2.add(List.of("d"), 3);
        inv2.add(List.of("c"), 4);
        inv2.add(List.of("f"), 6);
        inv2.add(List.of("h"), 9);
        BinaryFile.writeBlock(inv2, pathTest2);
        InvertedIndex inv3 = new InvertedIndex();
        inv3.add(List.of("a"), 3);
        inv3.add(List.of("z", "a"), 5);
        inv3.add(List.of("c", "a"), 6);

        BinaryFile.writeBlock(inv3, pathTest3);
        Merge merge = new Merge(List.of(pathTest1, pathTest2, pathTest3), pathLexicon, pathDocIds, pathFreqs, 2);
        merge.write(pathInvIndex, false);
        InvertedIndexWriter inv = new InvertedIndexWriter(pathInvIndex, pathDocIds, pathFreqs, 2);
        Lexicon lex = new Lexicon(pathLexicon);
        // assertEquals(List.of(1, 3, 5, 6), inv.getDocIds(lex.findTerm("a"), false));
        assertEquals(List.of(2, 4), inv.getDocIds(lex.findTerm("b"), false));
        assertEquals(List.of(4, 6), inv.getDocIds(lex.findTerm("c"), false));
        assertEquals(List.of(3), inv.getDocIds(lex.findTerm("d"), false));
        assertEquals(2, inv.getFreq(lex.findTerm("a"), 3, false));
    }
    

}