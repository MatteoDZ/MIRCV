package it.unipi.dii.aide.mircv.index.binary;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.BlockReader;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryFileTest {

    @BeforeEach
    public void setUp() {
        Configuration.setUpPathTest();
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEMP_FILES);
    }

    @Test
    public void writeBlockTest() throws IOException {
        InvertedIndex inv = new InvertedIndex();
        inv.add(List.of("a","a","a","b","b","b"), 1);
        inv.add(List.of("b","b","b","c", "c", "c"), 2);
        inv.add(List.of("b","b","b","c", "c", "c"), 3);
        inv.add(List.of("b","b","b","c", "c", "c"), 4);
        BinaryFile.writeBlock(inv, Configuration.PATH_BLOCKS);
        BlockReader b = new BlockReader(Configuration.PATH_BLOCKS);
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


    @Test
    public void intToBufferTest() throws IOException {
        FileChannel  fc = FileChannel.open(Paths.get(Configuration.PATH_BLOCKS), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        BinaryFile.writeIntToBuffer(fc, 1);
        assertEquals(1, BinaryFile.readIntFromBuffer(fc, (long)0));
    }





}
