package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockReaderTest {
    @Test
    public void readBlockTest() throws IOException {
        String pathTest1 = "data/test/test1.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("a"), 1);
        inv1.add(List.of("b"), 2);
        BinaryFile.writeBlock(inv1, pathTest1);
        BlockReader b = new BlockReader(pathTest1);
        assertEquals("a", b.readTerm());
        assertEquals(List.of(1), b.readNumbers());
        assertEquals(List.of(1), b.readNumbers());
        assertEquals("b", b.readTerm());
        assertEquals(List.of(2), b.readNumbers());
        assertEquals(List.of(1), b.readNumbers());
        assertEquals("block terminated", b.readTerm());
    }

    @Test
    public void readBlockTest1() throws IOException {
        String pathTest1 = "data/test/test1.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("a", "a", "b"), 1);
        inv1.add(List.of("b", "c"), 2);
        BinaryFile.writeBlock(inv1, pathTest1);
        BlockReader b = new BlockReader(pathTest1);
        assertEquals("a", b.readTerm());
        assertEquals(List.of(1), b.readNumbers());
        assertEquals(List.of(2), b.readNumbers());
        assertEquals("b", b.readTerm());
        assertEquals(List.of(1, 2), b.readNumbers());
        assertEquals(List.of(1, 1), b.readNumbers());
        assertEquals("c", b.readTerm());
        assertEquals(List.of(2), b.readNumbers());
        assertEquals(List.of(1), b.readNumbers());
        assertEquals("block terminated", b.readTerm());

    }



}