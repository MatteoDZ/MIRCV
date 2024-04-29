package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.index.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocIdFileTest {

    List<Integer> docIds = List.of(1, 20, 300, 401, 450, 461, 500, 6000, 70000, 800000, 8000000, 8800000);
    FileChannel fcDocIds;

    @BeforeEach
    public void setUp() throws IOException {
        Configuration.setUpPathTest();
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
    }


    @Test
     void writeDocIdsTest() throws IOException {
        DocIdFile doc = new DocIdFile( 5);
        long offsets = doc.writeDocIds(docIds,  false);
        fcDocIds = FileChannel.open(Paths.get(Configuration.PATH_DOCID), StandardOpenOption.READ);
        MappedByteBuffer mmbDocIds = fcDocIds.map(FileChannel.MapMode.READ_ONLY, offsets, 4L *docIds.size());
        List<Integer> docIdsRead = new ArrayList<>();
        for (int i = 0; i < docIds.size(); i++) {
            docIdsRead.add(mmbDocIds.getInt());
        }
        assertEquals(docIds, docIdsRead);
    }



}
