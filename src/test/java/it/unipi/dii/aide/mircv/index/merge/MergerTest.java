package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class MergerTest {

    @Test
    public void writeAll() throws IOException {
        String pathTest1 = "data/test/test1.bin";
        String pathTest2 = "data/test/test2.bin";
        String pathTest3 = "data/test/test3.bin";
        String pathMerge = "data/test/merge.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("a"), 1);
        inv1.add(List.of("b"), 2);
        BinaryFile.writeBlock(inv1, pathTest1);
        InvertedIndex inv2 = new InvertedIndex();
        inv2.add(List.of("d"), 3);
        inv2.add(List.of("c"), 4);
        BinaryFile.writeBlock(inv2, pathTest2);
        InvertedIndex inv3 = new InvertedIndex();
        inv3.add(List.of("z", "a"), 5);
        inv3.add(List.of("c", "a"), 6);
        BinaryFile.writeBlock(inv3, pathTest3);
        Merger merge = new Merger(List.of(pathTest1, pathTest2, pathTest3));
        merge.writeAll(pathMerge);
        System.out.println(BinaryFile.readBlock(pathMerge).toString());
        assertEquals(List.of(new PostingIndex("a", List.of(1,5,6), List.of(1,1,1)),
                new PostingIndex("b", List.of(2), List.of(1)),
                new PostingIndex("c", List.of(4,6), List.of(1, 1)),
                new PostingIndex("d", List.of(3), List.of(1)),
                new PostingIndex("z", List.of(5), List.of(1))).toString(), BinaryFile.readBlock(pathMerge).toString());
    }

    @Test
    public void writeAllTest1() throws IOException {
        String pathTest1 = "data/test/test1.bin";
        String pathTest2 = "data/test/test2.bin";
        String pathTest3 = "data/test/test3.bin";
        String pathMerge = "data/test/merge.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("a"), 1);
        inv1.add(List.of("b"), 2);
        inv1.add(List.of("a"), 3);
        inv1.add(List.of("c"), 4);
        BinaryFile.writeBlock(inv1, pathTest1);
        InvertedIndex inv2 = new InvertedIndex();
        inv2.add(List.of("a"), 5);
        inv2.add(List.of("b"), 6);
        inv2.add(List.of("a"), 7);
        inv2.add(List.of("c"), 8);
        BinaryFile.writeBlock(inv2, pathTest2);
        InvertedIndex inv3 = new InvertedIndex();
        inv3.add(List.of("a"), 10);
        inv3.add(List.of("b"), 14);
        BinaryFile.writeBlock(inv3, pathTest3);
        Merger merge = new Merger(List.of(pathTest1, pathTest2, pathTest3));
        merge.writeAll(pathMerge);
        System.out.println(BinaryFile.readBlock(pathMerge).toString());
        assertEquals(List.of(new PostingIndex("a", List.of(1, 3, 5, 7, 10), List.of(1, 1, 1, 1, 1)),
                new PostingIndex("b", List.of(2, 6, 14), List.of(1, 1, 1)),
                new PostingIndex("c", List.of(4, 8), List.of(1, 1))).toString(), BinaryFile.readBlock(pathMerge).toString());

    }

    @Test
    public void altroTest() throws IOException {
        String path1 = "data/test/test1.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("a","a","a","b","b","b"), 1);
        inv1.add(List.of("b","b","b","c", "c", "c"), 2);
        inv1.add(List.of("b","b","b","c", "c", "c"), 3);
        inv1.add(List.of("b","b","b","c", "c", "c"), 4);
        BinaryFile.writeBlock(inv1,path1);
        String path2 = "data/test/test2.bin";
        InvertedIndex inv2 = new InvertedIndex();
        inv2.add(List.of("a","a","a","b","b","b"), 5);
        inv2.add(List.of("b","b","b","c", "c", "c"), 6);
        inv2.add(List.of("b","b","b","c", "c", "c"), 7);
        inv2.add(List.of("b","b","b","c", "c", "c"), 8);
        BinaryFile.writeBlock(inv2, path2);
        String path3 = "data/test/test3.bin";
        InvertedIndex inv3 = new InvertedIndex();
        inv3.add(List.of("a","a","a","b","b","b"), 9);
        inv3.add(List.of("b","b","b","c", "c", "c"), 10);
        inv3.add(List.of("b","b","b","c", "c", "c"), 11);
        inv3.add(List.of("b","b","b","c", "c", "c"), 12);
        BinaryFile.writeBlock(inv3,path3);
        Merger m=new Merger(List.of(path1,path2,path3));
        String pathMerge = "data/test/vocabularyTest.bin";
        m.writeAll(pathMerge);
        assertEquals(List.of(new PostingIndex("a", List.of(1, 5, 9), List.of(3,3,3)),
                new PostingIndex("b", List.of(1,2, 3,4,5,6,7,8,9,10,11,12), List.of(3,3,3,3,3,3,3,3,3,3,3,3)),
                new PostingIndex("c", List.of(2,3,4,6,7,8,10,11,12), List.of(3,3,3,3,3,3,3,3,3))).toString(), BinaryFile.readBlock(pathMerge).toString());
    }

}
