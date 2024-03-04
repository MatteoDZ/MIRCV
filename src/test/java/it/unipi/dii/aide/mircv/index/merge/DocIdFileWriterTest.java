package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocIdFileWriterTest{

    @Test
     void readDocIdsBlockTest() throws IOException {
        List<Integer> docIds = List.of(1, 20, 300, 401, 450, 461, 500, 6000, 70000, 800000, 8000000, 8800000);
        String path = "data/test/testWriter.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        DocIdFileWriter doc = new DocIdFileWriter(path, 5);
        List<Long> offsets = doc.writeDocIds(docIds,  false);
        assertEquals(List.of(1,20,300,401,450).toString(), doc.readDocIdsBlock(offsets.get(0), offsets.get(1), false).toString());
        assertEquals(List.of(461, 500, 6000, 70000, 800000).toString(), doc.readDocIdsBlock(offsets.get(1), offsets.get(2), false).toString());
        assertEquals(List.of(8000000, 8800000).toString(), doc.readDocIdsBlock(offsets.get(2), offsets.get(3), false).toString());
        List<Integer> docIdsNew = List.of(1, 10 , 100, 2000, 7000, 80000);
        List<Long> offsetsNew = doc.writeDocIds(docIdsNew,  false);
        assertEquals(List.of(1, 10 , 100, 2000, 7000).toString(), doc.readDocIdsBlock(offsetsNew.get(0), offsetsNew.get(1), false).toString());
        assertEquals(List.of(80000).toString(), doc.readDocIdsBlock(offsetsNew.get(1), offsetsNew.get(2), false).toString());
    }

    @Test
     void readDocIdsTest() throws IOException {
        List<Integer> docIds = List.of(1, 20, 300, 401, 450, 461, 500, 6000, 70000, 800000, 8000000, 8800000);
        String path = "data/test/testWriter.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        DocIdFileWriter doc = new DocIdFileWriter(path, 4);
        List<Long> offsets = doc.writeDocIds(docIds,  false);
        System.out.println("offsets "+offsets);
        System.out.println("massimi "+doc.getTermUpperBounds());
        System.out.println(doc.readDocIdsBlock(16L,48L, false));
    }

    @Test
     void calculateTermUpperBoundsTest() throws IOException {
        List<Integer> docIds = List.of(1, 20, 300, 401, 450, 461, 500, 6000, 70000, 800000, 8000000, 8800000);
        String path = "data/test/testWriter.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        DocIdFileWriter doc = new DocIdFileWriter(path, 5);
        doc.writeDocIds(docIds,  false);
        assertEquals(List.of(450,800000,8800000), doc.getTermUpperBounds());
        List<Integer> docIdsNew = List.of(1, 10 , 100, 2000, 7000, 80000);
        doc.writeDocIds(docIdsNew,  false);
        assertEquals(List.of(7000, 80000), doc.getTermUpperBounds());
    }

    @Test
    void compressionTest() throws IOException{
        List<Integer> docsIds = List.of( 0, 1, 10, 20, 30, 500, 1000, 5000, 10000, 100000, 500000, 700000, 1000000, 5000000, 8000000);
        String path = "data/test/testWriter.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        DocIdFileWriter doc = new DocIdFileWriter(path, 5);
        List<Long> offsets = doc.writeDocIds(docsIds,true);
        assertEquals(List.of(0, 1, 10, 20, 30).toString(), doc.readDocIdsBlock(offsets.get(0), offsets.get(1), true).toString());
        assertEquals(List.of(500, 1000, 5000, 10000, 100000).toString(), doc.readDocIdsBlock(offsets.get(1), offsets.get(2), true).toString());
        assertEquals(List.of(500000, 700000, 1000000, 5000000, 8000000).toString(), doc.readDocIdsBlock(offsets.get(2), offsets.get(3), true).toString());
        List<Integer> docsIdsNew = List.of( 0, 1);
        List<Long> offsetsNew = doc.writeDocIds(docsIdsNew,true);
        assertEquals(docsIdsNew.toString(), doc.readDocIdsBlock(offsetsNew.get(0), offsetsNew.get(1), true).toString());
        List<Integer> docsIdsNew1 = List.of( 0, 8000000, 88000000);
        List<Long> offsetsNew1 = doc.writeDocIds(docsIdsNew1,true);
        assertEquals(docsIdsNew1.toString(), doc.readDocIdsBlock(offsetsNew1.get(0), offsetsNew1.get(1), true).toString());
        List<Integer> docsIdsNew2 = List.of( 0);
        List<Long> offsetsNew2 = doc.writeDocIds(docsIdsNew2,true);
        assertEquals(docsIdsNew2.toString(), doc.readDocIdsBlock(offsetsNew2.get(0), offsetsNew2.get(1), true).toString());
        List<Integer> docsIdsNew3 = List.of( 8000000, 8800000);
        List<Long> offsetsNew3 = doc.writeDocIds(docsIdsNew3,true);
        assertEquals(docsIdsNew3.toString(), doc.readDocIdsBlock(offsetsNew3.get(0), offsetsNew3.get(1), true).toString());
    }


}
