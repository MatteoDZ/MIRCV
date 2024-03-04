package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvertedIndexWriterTest {


    @Test
    public void getFreqTest() throws IOException {
        List<Integer> docIds = List.of(0, 1, 20, 300, 401, 450, 461, 500, 6000, 70000, 800000, 8000000, 8800000, 8800001);
        List<Integer> freqs = List.of(10, 1, 2, 3, 41, 45, 46, 50, 600, 7000, 8000, 1000, 8800, 700);
        String pathDoc = "data/test/testDoc.bin";
        String pathFreq= "data/test/testFreq.bin";
        String pathIndex= "data/test/testIndex.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        InvertedIndexWriter invIndex = new InvertedIndexWriter(pathIndex, pathDoc, pathFreq, 4);
        Long offset = invIndex.write(docIds, freqs,false);
        //
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
        List<Integer> docIdsNew = List.of(10, 200, 8000, 7000000, 7100000);
        List<Integer> freqsNew = List.of(1, 2, 3 ,4, 2);
        Long offsetNew = invIndex.write(docIdsNew, freqsNew, false);
        assertEquals(freqsNew.get(0), invIndex.getFreq(offsetNew, docIdsNew.get(0), false));
        assertEquals(freqsNew.get(1), invIndex.getFreq(offsetNew, docIdsNew.get(1), false));
        assertEquals(freqsNew.get(2), invIndex.getFreq(offsetNew, docIdsNew.get(2), false));
        assertEquals(freqsNew.get(3), invIndex.getFreq(offsetNew, docIdsNew.get(3), false));
        assertEquals(freqsNew.get(4), invIndex.getFreq(offsetNew, docIdsNew.get(4), false));
        assertEquals(0, invIndex.getFreq(offsetNew, 8000000, false));
    }

    @Test
    public void getFreqCompressedTest() throws IOException {
        List<Integer> docIds = List.of(0, 1, 20, 300, 401, 450, 461, 500, 6000, 70000, 800000, 8000000, 8800000, 8800001);
        List<Integer> freqs = List.of(10, 1, 2, 3, 41, 45, 46, 50, 600, 7000, 8000, 1000, 8800, 700);
        String pathDoc = "data/test/testDoc.bin";
        String pathFreq= "data/test/testFreq.bin";
        String pathIndex= "data/test/testIndex.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        InvertedIndexWriter invIndex = new InvertedIndexWriter(pathIndex, pathDoc, pathFreq, 4);
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
        List<Integer> docIdsNew = List.of(10, 200, 8000, 7000000, 7100000);
        List<Integer> freqsNew = List.of(1, 2, 3 ,4, 2);
        Long offsetNew = invIndex.write(docIdsNew, freqsNew,true);
        assertEquals(freqsNew.get(0), invIndex.getFreq(offsetNew, docIdsNew.get(0), true));
        assertEquals(freqsNew.get(1), invIndex.getFreq(offsetNew, docIdsNew.get(1), true));
        assertEquals(freqsNew.get(2), invIndex.getFreq(offsetNew, docIdsNew.get(2), true));
        assertEquals(freqsNew.get(3), invIndex.getFreq(offsetNew, docIdsNew.get(3), true));
        assertEquals(freqsNew.get(4), invIndex.getFreq(offsetNew, docIdsNew.get(4), true));
        assertEquals(0, invIndex.getFreq(offsetNew, 8000000, true));
    }

}
