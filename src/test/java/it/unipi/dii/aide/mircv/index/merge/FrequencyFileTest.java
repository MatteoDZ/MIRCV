package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FrequencyFileTest {

    /*

    @BeforeAll
    static void setUp() {
        Configuration.setUpPathTest();
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
    }

    @Test
     void readFreqsBlockTest() throws IOException {
        List<Integer> freqs = List.of(1, 10, 13, 5, 7, 4, 4, 2, 7, 5, 4);
        FrequencyFile freq = new FrequencyFile(5);
        List<Long> offsets = freq.writeFrequencies(freqs, false);
        assertEquals(List.of(1, 10, 13, 5, 7).toString(), freq.readFreqsBlock(offsets.get(0), offsets.get(1), false).toString());
        assertEquals(List.of(4, 4, 2, 7, 5).toString(), freq.readFreqsBlock(offsets.get(1), offsets.get(2), false).toString());
        assertEquals(List.of(4).toString(), freq.readFreqsBlock(offsets.get(2), offsets.get(3), false).toString());
        List<Integer> freqsNew = List.of(1, 10 ,4 , 6, 3, 2);
        List<Long> offsetsNew = freq.writeFrequencies(freqsNew, false);
        System.out.println(offsetsNew);
        assertEquals(List.of(1, 10 ,4 , 6, 3).toString(), freq.readFreqsBlock(offsetsNew.get(0), offsetsNew.get(1), false).toString());
        assertEquals(List.of(2).toString(), freq.readFreqsBlock(offsetsNew.get(1), offsetsNew.get(2), false).toString());
    }

    @Test
     void readFreqsNoCompressionTest() throws IOException {
        List<Integer> freqs = List.of(1, 10, 13, 5, 7, 4, 4, 2, 7, 5, 4);
        FrequencyFile freq = new FrequencyFile(5);
        List<Long> offsets = freq.writeFrequencies(freqs,  false);
        assertEquals(freqs.toString(), freq.readFreqs(offsets, false).toString());
        List<Integer> freqsNew = List.of(1, 10 ,4 , 6, 3, 2);
        List<Long> offsetsNew = freq.writeFrequencies(freqsNew, false);
        assertEquals(freqsNew.toString(), freq.readFreqs(offsetsNew, false).toString());
    }

    @Test
    void readFreqsYesCompressionTest() throws IOException {
        List<Integer> freqs = List.of(1, 10, 13, 5, 7, 4, 4, 2, 7, 5, 4);
        FrequencyFile freq = new FrequencyFile( 5);
        List<Long> offsets = freq.writeFrequencies(freqs,  true);
        assertEquals(freqs.toString(), freq.readFreqs(offsets, true).toString());
        List<Integer> freqsNew = List.of(1, 10 ,4 , 6, 3, 2);
        List<Long> offsetsNew = freq.writeFrequencies(freqsNew, true);
        assertEquals(freqsNew.toString(), freq.readFreqs(offsetsNew, true).toString());
    }

    @Test
    void readFreqsBlockTest1() throws IOException {
        List<Integer> freqs = List.of(1, 10, 13, 5, 7, 4, 4, 2, 7, 5, 4);
        FrequencyFile freq = new FrequencyFile( 2);
        List<Long> offsets = freq.writeFrequencies(freqs, false);
        assertEquals(List.of(1, 10).toString(), freq.readFreqsBlock(offsets.get(0), offsets.get(1), false).toString());
        assertEquals(List.of(13, 5).toString(), freq.readFreqsBlock(offsets.get(1), offsets.get(2), false).toString());
        assertEquals(List.of(7, 4).toString(), freq.readFreqsBlock(offsets.get(2), offsets.get(3), false).toString());
        List<Integer> freqsNew = List.of(1, 10 ,4 , 6, 3, 2, 1);
        FrequencyFile freqFileNew = new FrequencyFile(3);
        List<Long> offsetsNew = freqFileNew.writeFrequencies(freqsNew ,false);
        assertEquals(List.of(1, 10 ,4).toString(), freqFileNew.readFreqsBlock(offsetsNew.get(0), offsetsNew.get(1), false).toString());
        assertEquals(List.of(6, 3, 2).toString(), freqFileNew.readFreqsBlock(offsetsNew.get(1), offsetsNew.get(2), false).toString());
        assertEquals(List.of(1).toString(), freqFileNew.readFreqsBlock(offsetsNew.get(2), offsetsNew.get(3), false).toString());
    }


    @Test
    void compressionTest() throws IOException{
        List<Integer> freqs = List.of(1, 10, 13, 5, 7, 4, 4, 2, 7, 5, 4);
        FrequencyFile freq = new FrequencyFile(5);
        List<Long> offsets = freq.writeFrequencies(freqs,true);
        assertEquals(List.of(1, 10, 13, 5, 7).toString(), freq.readFreqsBlock(offsets.get(0), offsets.get(1), true).toString());
        assertEquals(List.of(4, 4, 2, 7, 5).toString(), freq.readFreqsBlock(offsets.get(1), offsets.get(2), true).toString());
        assertEquals(List.of(4).toString(), freq.readFreqsBlock(offsets.get(2), offsets.get(3), true).toString());
        List<Integer> freqsNew = List.of(1, 15, 15, 15, 15);
        List<Long> offsetsNew = freq.writeFrequencies(freqsNew,true);
        assertEquals(freqsNew.toString(), freq.readFreqsBlock(offsetsNew.get(0), offsetsNew.get(1), true).toString());
        List<Integer> freqsNew1 = List.of(1, 15);
        List<Long> offsetsNew1 = freq.writeFrequencies(freqsNew1,true);
        assertEquals(freqsNew1.toString(), freq.readFreqsBlock(offsetsNew1.get(0), offsetsNew1.get(1), true).toString());
        List<Integer> freqsNew2 = List.of(3,5,11,1,6);
        List<Long> offsetsNew2 = freq.writeFrequencies(freqsNew2,true);
        assertEquals(freqsNew2.toString(), freq.readFreqsBlock(offsetsNew2.get(0), offsetsNew2.get(1), true).toString());
    }*/

}