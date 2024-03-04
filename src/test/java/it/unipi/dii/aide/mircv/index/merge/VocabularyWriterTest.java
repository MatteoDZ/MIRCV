package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class VocabularyWriterTest {
    @Test
    public void writeReadTest() throws IOException {
        String path = "data/test/vocabularyTest.bin";
        FileUtils.deleteDirectory("data/test");
        FileUtils.createDirectory("data/test");
        VocabularyWriter vocabulary = new VocabularyWriter(path);
        vocabulary.write("ciao", 20L, 3, 10);
        vocabulary.read();
    }

}
