package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.ConfigTest;
import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.index.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvertedIndexFileTest {

    List<Integer> docIds = List.of(0, 1, 20, 300, 401, 450, 461, 500, 6000, 70000, 800000, 8000000, 8800000, 8800001);
    List<Integer> freqs = List.of(10, 1, 2, 3, 41, 45, 46, 50, 600, 7000, 8000, 1000, 8800, 700);
    List<Integer> docIdsNew = List.of(10, 200, 8000, 7000000, 7100000);
    List<Integer> freqsNew = List.of(1, 2, 3 ,4, 2);

    @Test
    public void writeNoCompressionTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,false);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_INV_INDEX), StandardOpenOption.READ, StandardOpenOption.WRITE);
        assertEquals(4, BinaryFile.readShortFromBuffer(fc, offset));
        assertEquals(List.of(300, 500, 8000000, 8800001), BinaryFile.readIntListFromBuffer(fc, offset+2, offset+2 + 4*4));
        assertEquals(-1, BinaryFile.readIntFromBuffer(fc,offset+2 + 4*4));
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 4+offset+2 + 4*4, fc.size());
        List<Long> docIdsOffsets = new ArrayList<>();
        long docIdOffset;
        while ((docIdOffset = mbb.getLong()) != -1) {
            docIdsOffsets.add(docIdOffset);
        }
        assertEquals(List.of(0L, 16L, 32L, 48L, 56L), docIdsOffsets);
        List<Long> docIdsFreqs = new ArrayList<>();
        long freqffset;
        while ((freqffset = mbb.getLong()) != -1) {
            docIdsFreqs.add(freqffset);
        }
        assertEquals(List.of(0L, 8L, 16L, 24L, 28L), docIdsFreqs);
    }

    @Test
    public void writeYesCompressionTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,true);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_INV_INDEX), StandardOpenOption.READ, StandardOpenOption.WRITE);
        assertEquals(4, BinaryFile.readShortFromBuffer(fc, offset));
        assertEquals(List.of(300, 500, 8000000, 8800001), BinaryFile.readIntListFromBuffer(fc, offset+2, offset+2 + 4*4));
        assertEquals(-1, BinaryFile.readIntFromBuffer(fc,offset+2 + 4*4));
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 4+offset+2 + 4*4, fc.size());
        List<Long> docIdsOffsets = new ArrayList<>();
        long docIdOffset;
        while ((docIdOffset = mbb.getLong()) != -1) {
            docIdsOffsets.add(docIdOffset);
        }
        int byteFirstBlockDocIds = VariableByteCompressor.encode(List.of(0, 1, 20, 300)).length;
        int byteSecondBlockDocIds = VariableByteCompressor.encode(List.of( 401, 450, 461, 500)).length;
        int byteThirdBlockDocIds = VariableByteCompressor.encode(List.of(6000, 70000, 800000, 8000000)).length;
        int byteFourthBlockDocIds = VariableByteCompressor.encode(List.of(8800000, 8800001)).length;
        assertEquals(List.of(0L, (long)byteFirstBlockDocIds, (long)byteFirstBlockDocIds+byteSecondBlockDocIds, (long)byteFirstBlockDocIds+byteSecondBlockDocIds+byteThirdBlockDocIds, (long)byteFirstBlockDocIds+byteSecondBlockDocIds+byteThirdBlockDocIds+byteFourthBlockDocIds), docIdsOffsets);
        List<Long> docIdsFreqs = new ArrayList<>();
        long freqffset;
        while ((freqffset = mbb.getLong()) != -1) {
            docIdsFreqs.add(freqffset);
        }
        int byteFirstBlockFreqs = UnaryCompressor.integerArrayCompression(new int[] {10, 1, 2, 3}).length;
        int byteSecondBlockFreqs = UnaryCompressor.integerArrayCompression(new int[] {41, 45, 46, 50}).length;
        int byteThirdBlockFreqs = UnaryCompressor.integerArrayCompression(new int[] {600, 7000, 8000, 1000}).length;
        int byteFourthBlockFreqs = UnaryCompressor.integerArrayCompression(new int[] {8800, 700}).length;
        assertEquals(List.of(0L, (long)byteFirstBlockFreqs, (long)byteFirstBlockFreqs+byteSecondBlockFreqs, (long)byteFirstBlockFreqs+byteSecondBlockFreqs+byteThirdBlockFreqs, (long)byteFirstBlockFreqs+byteSecondBlockFreqs+byteThirdBlockFreqs+byteFourthBlockFreqs), docIdsFreqs);
    }


    @Test
    public void getFreqNoCompressionTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,false);
        assertEquals(freqs.get(0), invIndex.getFreq(offset, docIds.get(0), false));
        assertEquals(freqs.get(1), invIndex.getFreq(offset, docIds.get(1), false));
        assertEquals(freqs.get(2), invIndex.getFreq(offset, docIds.get(2), false));
        assertEquals(freqs.get(3), invIndex.getFreq(offset, docIds.get(3), false));
        assertEquals(freqs.get(4), invIndex.getFreq(offset, docIds.get(4), false));
        assertEquals(freqs.get(5), invIndex.getFreq(offset, docIds.get(5), false));
        assertEquals(freqs.get(6), invIndex.getFreq(offset, docIds.get(6), false));
        assertEquals(freqs.get(7), invIndex.getFreq(offset, docIds.get(7), false));
        assertEquals(freqs.get(8), invIndex.getFreq(offset, docIds.get(8), false));
        assertEquals(freqs.get(9), invIndex.getFreq(offset, docIds.get(9), false));
        assertEquals(freqs.get(10), invIndex.getFreq(offset, docIds.get(10), false));
        assertEquals(freqs.get(11), invIndex.getFreq(offset, docIds.get(11), false));
        assertEquals(freqs.get(12), invIndex.getFreq(offset, docIds.get(12), false));
        assertEquals(freqs.get(13), invIndex.getFreq(offset, docIds.get(13), false));
        assertEquals(0, invIndex.getFreq(offset, 10, false));
        Long offsetNew = invIndex.write(docIdsNew, freqsNew, false);
        assertEquals(freqsNew.get(0), invIndex.getFreq(offsetNew, docIdsNew.get(0), false));
        assertEquals(freqsNew.get(1), invIndex.getFreq(offsetNew, docIdsNew.get(1), false));
        assertEquals(freqsNew.get(2), invIndex.getFreq(offsetNew, docIdsNew.get(2), false));
        assertEquals(freqsNew.get(3), invIndex.getFreq(offsetNew, docIdsNew.get(3), false));
        assertEquals(freqsNew.get(4), invIndex.getFreq(offsetNew, docIdsNew.get(4), false));
        assertEquals(0, invIndex.getFreq(offsetNew, 8000000, false));
    }

    @Test
    public void getFreqYesCompressionTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,true);
        assertEquals(freqs.get(0), invIndex.getFreq(offset, docIds.get(0), true));
        assertEquals(freqs.get(1), invIndex.getFreq(offset, docIds.get(1), true));
        assertEquals(freqs.get(2), invIndex.getFreq(offset, docIds.get(2), true));
        assertEquals(freqs.get(3), invIndex.getFreq(offset, docIds.get(3), true));
        assertEquals(freqs.get(4), invIndex.getFreq(offset, docIds.get(4), true));
        assertEquals(freqs.get(5), invIndex.getFreq(offset, docIds.get(5), true));
        assertEquals(freqs.get(6), invIndex.getFreq(offset, docIds.get(6), true));
        assertEquals(freqs.get(7), invIndex.getFreq(offset, docIds.get(7), true));
        assertEquals(freqs.get(8), invIndex.getFreq(offset, docIds.get(8), true));
        assertEquals(freqs.get(9), invIndex.getFreq(offset, docIds.get(9), true));
        assertEquals(freqs.get(10), invIndex.getFreq(offset, docIds.get(10), true));
        assertEquals(freqs.get(11), invIndex.getFreq(offset, docIds.get(11), true));
        assertEquals(freqs.get(12), invIndex.getFreq(offset, docIds.get(12), true));
        assertEquals(freqs.get(13), invIndex.getFreq(offset, docIds.get(13), true));
        assertEquals(0, invIndex.getFreq(offset, 10, true));
        Long offsetNew = invIndex.write(docIdsNew, freqsNew,true);
        assertEquals(freqsNew.get(0), invIndex.getFreq(offsetNew, docIdsNew.get(0), true));
        assertEquals(freqsNew.get(1), invIndex.getFreq(offsetNew, docIdsNew.get(1), true));
        assertEquals(freqsNew.get(2), invIndex.getFreq(offsetNew, docIdsNew.get(2), true));
        assertEquals(freqsNew.get(3), invIndex.getFreq(offsetNew, docIdsNew.get(3), true));
        assertEquals(freqsNew.get(4), invIndex.getFreq(offsetNew, docIdsNew.get(4), true));
        assertEquals(0, invIndex.getFreq(offsetNew, 8000000, true));
    }


    @Test
    public void getDocIdsTestNoCompression() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,false);
        assertEquals(docIds, invIndex.getDocIds(offset, false));
    }

    @Test
    public void getDocIdsTestYesCompression() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,true);
        assertEquals(docIds, invIndex.getDocIds(offset, true));
    }

    @Test
    public void getOffsetsDocIdsNoCompressionTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,false);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_INV_INDEX), StandardOpenOption.READ);
        short numBlocks = BinaryFile.readShortFromBuffer(fc, offset);
        assertEquals(List.of(0L,16L),invIndex.getOffsetsDocIds(offset, numBlocks, 0));
        assertEquals(List.of(16L,32L),invIndex.getOffsetsDocIds(offset, numBlocks, 1));
        assertEquals(List.of(32L,48L),invIndex.getOffsetsDocIds(offset, numBlocks, 2));
        assertEquals(List.of(48L,56L),invIndex.getOffsetsDocIds(offset, numBlocks, 3));
    }

    @Test
    public void getOffsetsDocIdsYesCompressionTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,true);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_INV_INDEX), StandardOpenOption.READ);
        short numBlocks = BinaryFile.readShortFromBuffer(fc, offset);
        int byteFirstBlock = VariableByteCompressor.encode(List.of(0, 1, 20, 300)).length;
        assertEquals(List.of(0L,(long) byteFirstBlock),invIndex.getOffsetsDocIds(offset, numBlocks, 0));
        int byteSecondBlock = VariableByteCompressor.encode(List.of( 401, 450, 461, 500)).length;
        assertEquals(List.of((long) byteFirstBlock,(long) byteFirstBlock+byteSecondBlock),invIndex.getOffsetsDocIds(offset, numBlocks, 1));
        int byteThirdBlock = VariableByteCompressor.encode(List.of(6000, 70000, 800000, 8000000)).length;
        assertEquals(List.of((long) byteFirstBlock+byteSecondBlock, (long)byteFirstBlock+byteSecondBlock+byteThirdBlock),invIndex.getOffsetsDocIds(offset, numBlocks, 2));
        int byteFourthBlock = VariableByteCompressor.encode(List.of(8800000, 8800001)).length;
        assertEquals(List.of((long)byteFirstBlock+byteSecondBlock+byteThirdBlock,(long)byteFirstBlock+byteSecondBlock+byteThirdBlock+byteFourthBlock),invIndex.getOffsetsDocIds(offset, numBlocks, 3));
    }

    @Test
    public void getOffsetsFreqsNoCompressionTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,false);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_INV_INDEX), StandardOpenOption.READ);
        short numBlocks = BinaryFile.readShortFromBuffer(fc, offset);
        assertEquals(List.of(0L,8L),invIndex.getOffsetsFreqs(offset, numBlocks, 0));
        assertEquals(List.of(8L,16L),invIndex.getOffsetsFreqs(offset, numBlocks, 1));
        assertEquals(List.of(16L,24L),invIndex.getOffsetsFreqs(offset, numBlocks, 2));
        assertEquals(List.of(24L,28L),invIndex.getOffsetsFreqs(offset, numBlocks, 3));
    }

    @Test
    public void getOffsetsFreqsYesCompressionTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,true);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_INV_INDEX), StandardOpenOption.READ);
        short numBlocks = BinaryFile.readShortFromBuffer(fc, offset);
        int byteFirstBlock = UnaryCompressor.integerArrayCompression(new int[] {10, 1, 2, 3}).length;
        assertEquals(List.of(0L,(long) byteFirstBlock),invIndex.getOffsetsFreqs(offset, numBlocks, 0));
        int byteSecondBlock = UnaryCompressor.integerArrayCompression(new int[] {41, 45, 46, 50}).length;
        assertEquals(List.of((long) byteFirstBlock,(long) byteFirstBlock+byteSecondBlock),invIndex.getOffsetsFreqs(offset, numBlocks, 1));
        int byteThirdBlock = UnaryCompressor.integerArrayCompression(new int[] {600, 7000, 8000, 1000}).length;
        assertEquals(List.of((long) byteFirstBlock+byteSecondBlock, (long)byteFirstBlock+byteSecondBlock+byteThirdBlock),invIndex.getOffsetsFreqs(offset, numBlocks, 2));
        int byteFourthBlock = UnaryCompressor.integerArrayCompression(new int[] {8800, 700}).length;
        assertEquals(List.of((long)byteFirstBlock+byteSecondBlock+byteThirdBlock,(long)byteFirstBlock+byteSecondBlock+byteThirdBlock+byteFourthBlock),invIndex.getOffsetsFreqs(offset, numBlocks, 3));
    }


    @Test
    public void findBlockIndexNoCompressionTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,false);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_INV_INDEX), StandardOpenOption.READ);
        short numBlocks = BinaryFile.readShortFromBuffer(fc, offset);
        assertEquals(4, numBlocks);
        assertEquals(0, invIndex.findBlockIndex(offset, numBlocks, 0));
        assertEquals(0, invIndex.findBlockIndex(offset, numBlocks, 300));
        assertEquals(1, invIndex.findBlockIndex(offset, numBlocks, 401));
        assertEquals(1, invIndex.findBlockIndex(offset, numBlocks, 500));
        assertEquals(2, invIndex.findBlockIndex(offset, numBlocks, 6000));
        assertEquals(2, invIndex.findBlockIndex(offset, numBlocks, 800000));
        assertEquals(3, invIndex.findBlockIndex(offset, numBlocks, 8800000));
        assertEquals(3, invIndex.findBlockIndex(offset, numBlocks, 8800001));
        Long offsetNew = invIndex.write(docIdsNew, freqsNew, false);
        short numBlocksNew = BinaryFile.readShortFromBuffer(fc, offsetNew);
        assertEquals(2, numBlocksNew);
        assertEquals(0, invIndex.findBlockIndex(offsetNew, numBlocks, 10));
        assertEquals(0, invIndex.findBlockIndex(offsetNew, numBlocks, 7000000));
        assertEquals(1, invIndex.findBlockIndex(offsetNew, numBlocks, 7100000));
    }

    @Test
    public void findBlockIndexYesCompressionTest() throws IOException {
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
        InvertedIndexFile invIndex = new InvertedIndexFile(ConfigTest.PATH_INV_INDEX, ConfigTest.PATH_DOC_IDS, ConfigTest.PATH_FREQ, 4);
        Long offset = invIndex.write(docIds, freqs,true);
        FileChannel  fc = FileChannel.open(Paths.get(ConfigTest.PATH_INV_INDEX), StandardOpenOption.READ);
        short numBlocks = BinaryFile.readShortFromBuffer(fc, offset);
        assertEquals(4, numBlocks);
        assertEquals(0, invIndex.findBlockIndex(offset, numBlocks, 0));
        assertEquals(0, invIndex.findBlockIndex(offset, numBlocks, 300));
        assertEquals(1, invIndex.findBlockIndex(offset, numBlocks, 401));
        assertEquals(1, invIndex.findBlockIndex(offset, numBlocks, 500));
        assertEquals(2, invIndex.findBlockIndex(offset, numBlocks, 6000));
        assertEquals(2, invIndex.findBlockIndex(offset, numBlocks, 800000));
        assertEquals(3, invIndex.findBlockIndex(offset, numBlocks, 8800000));
        assertEquals(3, invIndex.findBlockIndex(offset, numBlocks, 8800001));
        Long offsetNew = invIndex.write(docIdsNew, freqsNew, true);
        short numBlocksNew = BinaryFile.readShortFromBuffer(fc, offsetNew);
        assertEquals(2, numBlocksNew);
        assertEquals(0, invIndex.findBlockIndex(offsetNew, numBlocks, 10));
        assertEquals(0, invIndex.findBlockIndex(offsetNew, numBlocks, 7000000));
        assertEquals(1, invIndex.findBlockIndex(offsetNew, numBlocks, 7100000));
    }

}
