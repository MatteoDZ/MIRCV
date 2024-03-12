package it.unipi.dii.aide.mircv.index.binary;

import it.unipi.dii.aide.mircv.index.merge.BlockReader;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class BinaryFileTest {

    @Test
    public void writeBlockTest() throws IOException {
        String path = "data/test/test.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        InvertedIndex inv = new InvertedIndex();
        inv.add(List.of("a","a","a","b","b","b"), 1);
        inv.add(List.of("b","b","b","c", "c", "c"), 2);
        inv.add(List.of("b","b","b","c", "c", "c"), 3);
        inv.add(List.of("b","b","b","c", "c", "c"), 4);
        BinaryFile.writeBlock(inv, path);
        BlockReader b = new BlockReader(path);
        assertEquals("a", b.readTerm());
        assertEquals(List.of(1), b.readNumbers());
        assertEquals(List.of(3), b.readNumbers());
        assertEquals("b", b.readTerm());
        assertEquals(List.of(1, 2, 3, 4), b.readNumbers());
        assertEquals(List.of(3, 3, 3, 3), b.readNumbers());
        assertEquals("c", b.readTerm());
        assertEquals(List.of(2, 3, 4), b.readNumbers());
        assertEquals(List.of(3, 3, 3), b.readNumbers());
    }



}
