package it.unipi.dii.aide.mircv.index.binary;

import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class BinaryFileTest {

    @Test
    public void writeBlockTest(){
        String path = "../data/test/test.bin";
        FileUtils.deleteDirectory("../data/test");
        FileUtils.createDirectory("../data/test");
        InvertedIndex inv = new InvertedIndex();
        inv.add(List.of("a","a","a","b","b","b"), 1);
        inv.add(List.of("b","b","b","c", "c", "c"), 2);
        inv.add(List.of("b","b","b","c", "c", "c"), 3);
        inv.add(List.of("b","b","b","c", "c", "c"), 4);
        BinaryFile.writeBlock(inv, path);
        assertEquals(inv.getInvertedIndexBlock().values().toString(), BinaryFile.readBlock(path).toString());
    }
    @Test
    public void appendTest() {
        String path = "../data/test/test.bin";
        FileUtils.deleteDirectory("../data/test");
        FileUtils.createDirectory("../data/test");
        BinaryFile.appendToBinaryFile(path,"c",List.of(1,2),List.of(1,2));
        BinaryFile.appendToBinaryFile(path,"f",List.of(4,5),List.of(1,2));
        assertEquals(List.of(new PostingIndex("c", List.of(1, 2), List.of(1, 2)), new PostingIndex("f", List.of(4, 5), List.of(1, 2))).toString(), BinaryFile.readBlock(path).toString());
    }



}
