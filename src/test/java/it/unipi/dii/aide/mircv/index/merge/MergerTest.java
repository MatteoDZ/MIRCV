package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MergerTest {
    /*String pathTest1 = Configuration.DIRECTORY_TEST + "/test1.bin";
    String pathTest2 = Configuration.DIRECTORY_TEST + "/test2.bin";
    String pathTest3 = Configuration.DIRECTORY_TEST + "/test3.bin";

    Statistics stats = new Statistics();

    @BeforeEach
    public void setUp() {
        Configuration.setUpPathTest();
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        try {
            FileChannel fc = FileChannel.open(Paths.get(Configuration.PATH_DOC_TERMS),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            BinaryFile.writeIntListToBuffer(fc, List.of(1,1,1,1,1,2,1,1,1,2,2)); // number to control maybe they are not correct
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + Configuration.PATH_DOC_TERMS + " file.");
        }
    }

    @Test
    public void writeCompressionFalseTest() throws IOException {
        stats.setNumDocs(12);
        stats.setAvgDocLen(1.3);
        stats.setTotalLenDoc(15);
        stats.writeSpimiToDisk(); // number to control maybe they are not correct
        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("a"), 1);
        inv1.add(List.of("b"), 2);
        inv1.add(List.of("a"), 3);
        inv1.add(List.of("b"), 4);
        inv1.add(List.of("a"), 20);
        BinaryFile.writeBlock(inv1, pathTest1);
        InvertedIndex inv2 = new InvertedIndex();
        inv2.add(List.of("z", "a"), 1);
        inv2.add(List.of("c"), 4);
        inv2.add(List.of("f"), 6);
        inv2.add(List.of("h"), 9);
        BinaryFile.writeBlock(inv2, pathTest2);
        InvertedIndex inv3 = new InvertedIndex();
        inv3.add(List.of("a"), 3);
        inv3.add(List.of("z", "m"), 5);
        inv3.add(List.of("c", "a"), 6);
        BinaryFile.writeBlock(inv3, pathTest3);
        Merge merge = new Merge(List.of(pathTest1, pathTest2, pathTest3), 2);
        merge.write(false);


        assertEquals(List.of(1, 3, 6, 20), inv.getDocIds(lex.findTerm("a").getOffset_skip_pointer(), false));
        assertEquals(List.of(2, 4), inv.getDocIds(lex.findTerm("b").getOffset_skip_pointer(), false));
        assertEquals(List.of(4, 6), inv.getDocIds(lex.findTerm("c").getOffset_skip_pointer(), false));
        assertEquals(List.of(1, 5), inv.getDocIds(lex.findTerm("z").getOffset_skip_pointer(), false));
        assertEquals(2, inv.getFreq(lex.findTerm("a").getOffset_skip_pointer(), 3, false));
    }

    @Test
    public void writeCompressionTrueTest() throws IOException {
        stats.setNumDocs(12);
        stats.setAvgDocLen(1.3);
        stats.setTotalLenDoc(15);
        stats.writeSpimiToDisk(); // number to control maybe they are not correct
        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("a"), 1);
        inv1.add(List.of("b"), 2);
        inv1.add(List.of("a"), 3);
        inv1.add(List.of("b"), 4);
        inv1.add(List.of("a"), 20);
        BinaryFile.writeBlock(inv1, pathTest1);
        InvertedIndex inv2 = new InvertedIndex();
        inv2.add(List.of("z"), 3);
        inv2.add(List.of("c"), 4);
        inv2.add(List.of("f"), 6);
        inv2.add(List.of("h"), 9);
        BinaryFile.writeBlock(inv2, pathTest2);
        InvertedIndex inv3 = new InvertedIndex();
        inv3.add(List.of("a"), 3);
        inv3.add(List.of("z", "m"), 5);
        inv3.add(List.of("c", "a"), 6);
        BinaryFile.writeBlock(inv3, pathTest3);
        Merge merge = new Merge(List.of(pathTest1, pathTest2, pathTest3),  2);
        merge.write( true);
        InvertedIndexFile inv = new InvertedIndexFile( 2);
        Lexicon lex = new Lexicon();
        assertEquals(List.of(1, 3, 6, 20), inv.getDocIds(lex.findTerm("a").getOffsetInvertedIndex(), true));
        assertEquals(List.of(2, 4), inv.getDocIds(lex.findTerm("b").getOffsetInvertedIndex(), true));
        assertEquals(List.of(4, 6), inv.getDocIds(lex.findTerm("c").getOffsetInvertedIndex(), true));
        assertEquals(List.of(3, 5), inv.getDocIds(lex.findTerm("z").getOffsetInvertedIndex(), true));
        assertEquals(2, inv.getFreq(lex.findTerm("a").getOffsetInvertedIndex(), 3, true));
    }

    @Test
    public void findDuplicateTest() {
        assertEquals(3, Merge.findDuplicate(List.of(1,2,3,4,4)));
        assertEquals(0, Merge.findDuplicate(List.of(1,1,2)));
        assertEquals(-1, Merge.findDuplicate(List.of(1,2,3)));
    }

    @Test
    public void findMinTermTest() throws IOException {
        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("c"), 1);
        inv1.add(List.of("d"), 2);
        BinaryFile.writeBlock(inv1, pathTest1);
        InvertedIndex inv2 = new InvertedIndex();
        inv2.add(List.of("a"), 3);
        inv2.add(List.of("b"), 4);
        BinaryFile.writeBlock(inv2, pathTest2);
        InvertedIndex inv3 = new InvertedIndex();
        inv3.add(List.of("e"), 3);
        inv3.add(List.of("z", "m"), 5);
        BinaryFile.writeBlock(inv3, pathTest3);
        HashMap<BlockReader, PostingIndex> readerLines = new HashMap<>();
        for (String path : List.of(pathTest1, pathTest2, pathTest3)) {
            BlockReader reader = new BlockReader(path);
            String line = reader.readTerm();
            List<Integer> docIds = reader.readNumbers();
            List<Integer> freqs = reader.readNumbers();
            readerLines.put(reader, new PostingIndex(line, docIds, freqs));
        }
        assertEquals("a", Merge.findMinTerm(readerLines));
    }*/
    

}