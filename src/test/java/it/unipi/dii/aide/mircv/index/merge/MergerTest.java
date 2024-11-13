package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MergerTest {
    String pathTest1 = Configuration.DIRECTORY_TEST + "/test1.bin";
    String pathTest2 = Configuration.DIRECTORY_TEST + "/test2.bin";
    String pathTest3 = Configuration.DIRECTORY_TEST + "/test3.bin";

    Statistics stats = new Statistics();

    FileChannel fcSkippingBlock;
    FileChannel fcDocIds;
    FileChannel fcFreqs;

    public MergerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() throws IOException {
        Configuration.setUpPathTest();
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);

        try {
            FileChannel fc = FileChannel.open(Paths.get(Configuration.PATH_DOC_TERMS),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            BinaryFile.writeIntListToBuffer(fc, List.of(1,1,1,1,1,2,1,1,2,1)); // number to control maybe they are not correct
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + Configuration.PATH_DOC_TERMS + " file.");
        }
    }

    @Test
    public void writeCompressionFalseTest() throws IOException {
        Statistics stats1 = new Statistics();
        stats1.setNumDocs(10);
        stats1.setAvgDocLen(1.3);
        stats1.setTotalLenDoc(15);
        stats1.writeSpimiToDisk();

        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("a"), 1);
        inv1.add(List.of("b"), 2);
        inv1.add(List.of("a"), 3);
        inv1.add(List.of("b"), 4);
        inv1.add(List.of("a"), 5);
        BinaryFile.writeBlock(inv1, pathTest1);
        InvertedIndex inv2 = new InvertedIndex();
        inv2.add(List.of("z", "a"), 6);
        inv2.add(List.of("c"), 7);
        inv2.add(List.of("f"), 8);
        BinaryFile.writeBlock(inv2, pathTest2);
        InvertedIndex inv3 = new InvertedIndex();
        inv3.add(List.of("c", "a"), 9);
        BinaryFile.writeBlock(inv3, pathTest3);
        Merge merge = new Merge(List.of(pathTest1, pathTest2, pathTest3), 2);
        merge.write(false);


        fcSkippingBlock = FileChannel.open(Paths.get(Configuration.SKIPPING_BLOCK_PATH), StandardOpenOption.READ);
        fcDocIds = FileChannel.open(Paths.get(Configuration.PATH_DOCID), StandardOpenOption.READ);
        fcFreqs = FileChannel.open(Paths.get(Configuration.PATH_FREQ), StandardOpenOption.READ);

        long offsetSkippping = Lexicon.getInstance().get("a").getOffset_skip_pointer();
        MappedByteBuffer mmbSkipping = fcSkippingBlock.map(FileChannel.MapMode.READ_ONLY, offsetSkippping, (8 + 4) * 2 + 4 + 4);
        long offsetDocIds =mmbSkipping.getLong();
        int docIdSize = mmbSkipping.getInt();
        long offsetFreqs = mmbSkipping.getLong();
        int freqSize = mmbSkipping.getInt();


        MappedByteBuffer mmbDocIds = fcDocIds.map(FileChannel.MapMode.READ_ONLY, offsetDocIds, 40);
        List<Integer> docIds = new ArrayList<>();
        List<Integer> docLens = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            docIds.add(mmbDocIds.getInt());
            docLens.add(mmbDocIds.getInt());
        }
        List<Short> freqs = new ArrayList<>();
        MappedByteBuffer mmbFreqs = fcFreqs.map(FileChannel.MapMode.READ_ONLY, offsetFreqs, 2L *5);
        for (int i = 0; i < 5; i++) {
            freqs.add(mmbFreqs.getShort());
        }


        assertEquals(List.of(1, 3, 5, 6,9), docIds);
        assertEquals(List.of((short) 1,(short) 1, (short)1, (short)1, (short)1), freqs);

        docIds.clear();
        freqs.clear();

        offsetSkippping = Lexicon.getInstance().get("b").getOffset_skip_pointer();
        mmbSkipping = fcSkippingBlock.map(FileChannel.MapMode.READ_ONLY, offsetSkippping, (8 + 4) * 2 + 4 + 4);
        offsetDocIds =mmbSkipping.getLong();
        docIdSize = mmbSkipping.getInt();
        offsetFreqs = mmbSkipping.getLong();
        freqSize = mmbSkipping.getInt();
        mmbDocIds = fcDocIds.map(FileChannel.MapMode.READ_ONLY, offsetDocIds, 16);
        docIds = new ArrayList<>();
        docLens = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            docIds.add(mmbDocIds.getInt());
            docLens.add(mmbDocIds.getInt());

        }
        freqs = new ArrayList<>();
        mmbFreqs = fcFreqs.map(FileChannel.MapMode.READ_ONLY, offsetFreqs, 2L *2);
        for (int i = 0; i < 2; i++) {
            freqs.add(mmbFreqs.getShort());
        }

        assertEquals(List.of(2, 4), docIds);
        assertEquals(List.of((short) 1,(short) 1), freqs);
    }

    @Test
    public void writeCompressionTrueTest() throws IOException {
        Statistics stats1 = new Statistics();
        stats1.setNumDocs(10);
        stats1.setAvgDocLen(1.3);
        stats1.setTotalLenDoc(15);
        stats1.writeSpimiToDisk();

        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("a"), 1);
        inv1.add(List.of("b"), 2);
        inv1.add(List.of("a"), 3);
        inv1.add(List.of("b"), 4);
        inv1.add(List.of("a"), 5);
        BinaryFile.writeBlock(inv1, pathTest1);
        InvertedIndex inv2 = new InvertedIndex();
        inv2.add(List.of("z", "a"), 6);
        inv2.add(List.of("c"), 7);
        inv2.add(List.of("f"), 8);
        BinaryFile.writeBlock(inv2, pathTest2);
        InvertedIndex inv3 = new InvertedIndex();
        inv3.add(List.of("c", "a"), 9);
        BinaryFile.writeBlock(inv3, pathTest3);
        Merge merge = new Merge(List.of(pathTest1, pathTest2, pathTest3), 2);
        merge.write(true);

        fcSkippingBlock = FileChannel.open(Paths.get(Configuration.SKIPPING_BLOCK_PATH), StandardOpenOption.READ);
        fcDocIds = FileChannel.open(Paths.get(Configuration.PATH_DOCID), StandardOpenOption.READ, StandardOpenOption.WRITE);
        fcFreqs = FileChannel.open(Paths.get(Configuration.PATH_FREQ), StandardOpenOption.READ, StandardOpenOption.WRITE);


        long offsetSkippping = Lexicon.getInstance().get("b").getOffset_skip_pointer();
        MappedByteBuffer mmbSkipping = fcSkippingBlock.map(FileChannel.MapMode.READ_ONLY, offsetSkippping, (8 + 4) * 2 + 4 + 4);
        long offsetDocIds =mmbSkipping.getLong();
        int docIdSize = mmbSkipping.getInt();

        MappedByteBuffer mmbDocIds = fcDocIds.map(FileChannel.MapMode.READ_ONLY, offsetDocIds, fcDocIds.size());


        byte[] doc_ids = new byte[docIdSize];

        mmbDocIds.get(doc_ids, 0, docIdSize);

        List<Integer> doc_ids_decompressed = VariableByteCompressor.decode(doc_ids);
        List<Integer> docIds = new ArrayList<>();
        for(int i=0;i<doc_ids_decompressed.size();i++) {
            if(i%2==0)
                docIds.add(doc_ids_decompressed.get(i));
        }

        assertEquals(List.of(2, 4), docIds);


    }


    @Test
    public void findMinTermTest() throws IOException {
        InvertedIndex inv1 = new InvertedIndex();
        inv1.add(List.of("c"), 1);
        inv1.add(List.of("d"), 2);
        BinaryFile.writeBlock(inv1, pathTest1);
        InvertedIndex inv2 = new InvertedIndex();
        inv2.add(List.of("a"), 3);
        inv2.add(List.of("b"), 4);
        BinaryFile.writeBlock(inv2, pathTest2);
        InvertedIndex inv3 = new InvertedIndex();
        inv3.add(List.of("e"), 3);
        inv3.add(List.of("z", "m"), 5);
        BinaryFile.writeBlock(inv3, pathTest3);
        HashMap<BlockReader, PostingIndex> readerLines = new HashMap<>();
        for (String path : List.of(pathTest1, pathTest2, pathTest3)) {
            BlockReader reader = new BlockReader(path);
            String line = reader.readTerm();
            List<Integer> docIds = reader.readNumbers();
            List<Integer> freqs = reader.readNumbers();
            readerLines.put(reader, new PostingIndex(line, docIds, freqs));
        }
        assertEquals("a", Merge.findMinTerm(readerLines));
    }
    

}