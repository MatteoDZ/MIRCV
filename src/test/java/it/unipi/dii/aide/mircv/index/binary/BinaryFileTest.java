package it.unipi.dii.aide.mircv.index.binary;

import it.unipi.dii.aide.mircv.index.ConfigTest;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.merge.BlockReader;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryFileTest {

    @Test
    public void writeBlockTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(ConfigTest.DIRECTORY_TMP);
        InvertedIndex inv = new InvertedIndex();
        inv.add(List.of("a","a","a","b","b","b"), 1);
        inv.add(List.of("b","b","b","c", "c", "c"), 2);
        inv.add(List.of("b","b","b","c", "c", "c"), 3);
        inv.add(List.of("b","b","b","c", "c", "c"), 4);
        BinaryFile.writeBlock(inv, ConfigTest.PATH_BLOCKS);
        BlockReader b = new BlockReader(ConfigTest.PATH_BLOCKS);
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
    public void shortToBufferTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(ConfigTest.DIRECTORY_TMP);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_BLOCKS), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        BinaryFile.writeShortToBuffer(fc, (short)1);
        assertEquals(1, BinaryFile.readShortFromBuffer(fc, (long)0));
    }

    @Test
    public void intToBufferTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(ConfigTest.DIRECTORY_TMP);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_BLOCKS), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        BinaryFile.writeIntToBuffer(fc, (int)1);
        assertEquals(1, BinaryFile.readIntFromBuffer(fc, (long)0));
    }

    @Test
    public void longToBufferTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(ConfigTest.DIRECTORY_TMP);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_BLOCKS), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        BinaryFile.writeLongToBuffer(fc, (int)1);
        assertEquals(1, BinaryFile.readLongFromBuffer(fc, (long)0));
    }

    @Test
    public void arrayByteToBufferTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(ConfigTest.DIRECTORY_TMP);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_BLOCKS), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        BinaryFile.writeArrayByteToBuffer(fc, new byte[]{1});
        assertArrayEquals(new byte[]{1}, BinaryFile.readArrayByteFromBuffer(fc, (long)0, (long) new byte[]{1}.length));
    }

    @Test
    public void shortListToBuffer() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(ConfigTest.DIRECTORY_TMP);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_BLOCKS), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        BinaryFile.writeShortListToBuffer(fc, List.of(1));
        assertEquals(List.of(1).toString(),
                BinaryFile.readShortListFromBuffer(fc, (long)0, (long) List.of(1).size()*2).toString());
    }

    @Test
    public void intListToBuffer() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(ConfigTest.DIRECTORY_TMP);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_BLOCKS), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        BinaryFile.writeIntListToBuffer(fc, List.of(1));
        assertEquals(List.of(1).toString(),
                BinaryFile.readIntListFromBuffer(fc, (long)0, (long) List.of(1).size()*4).toString());
    }



}
