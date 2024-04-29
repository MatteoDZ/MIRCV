package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.index.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FrequencyFileTest {

    List<Short> freqsRead = List.of( (short)1, (short)10, (short)13,(short) 5, (short)7, (short)4, (short)4, (short)2, (short)7, (short)5, (short)4);
    List<Integer> freqsWrite = List.of( 1, 10, 13, 5, 7, 4, 4, 2, 7, 5, 4);
    FileChannel fcFreqs;


    @BeforeAll
    static void setUp() {
        Configuration.setUpPathTest();
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
    }


    @Test
     void readFreqsTest() throws IOException {
        FrequencyFile freq = new FrequencyFile(5);
        long offsets = freq.writeFrequencies(freqsWrite,  false);
        fcFreqs = FileChannel.open(Paths.get(Configuration.PATH_FREQ), StandardOpenOption.READ);
        MappedByteBuffer mmbFreq = fcFreqs.map(FileChannel.MapMode.READ_ONLY, offsets, 2L *freqsWrite.size());
        List<Short> FreqsRead = new ArrayList<>();
        for (int i = 0; i < freqsWrite.size(); i++) {
            FreqsRead.add(mmbFreq.getShort());
        }
        assertEquals(freqsRead, FreqsRead);
    }





}