package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LexiconTest {

    @Before
    public void setUp() {
        Configuration.setUpPathTest();
        FileUtils.deleteDirectory(Configuration.DIRECTORY_TEST);
        FileUtils.createDirectory(Configuration.DIRECTORY_TEST);
    }

    @Test
    public void writeTest() throws IOException {
        Lexicon lexicon = new Lexicon();
        lexicon.write("a",0L, 0, 0d,0, 0f, 0);
        lexicon.write("b",0L, 0, 0d,0, 0f, 0);
        LexiconData lexiconDataA = lexicon.get("a");
        assertEquals("a", lexiconDataA.getTerm());
        assertEquals(0L, lexiconDataA.getOffset_skip_pointer());
        assertEquals(0, lexiconDataA.getDf());
        assertEquals(0d, lexiconDataA.getTf(), 0.0001);

    }



    @Test
    public void padStringToLengthTest() {
        assertEquals("a                               ", Lexicon.padStringToLength("a"));
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Lexicon.padStringToLength("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test
    public void removePaddingTest() {
        assertEquals("a", Lexicon.removePadding("a                               "));
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Lexicon.removePadding("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
    }

    @Test
    public void findTerm() throws IOException {
        Lexicon lexicon = new Lexicon();
        lexicon.write("a",0L, 0, 0d,0, 0f, 0);
        lexicon.write("b",0L, 0, 0d,0, 0f, 0);
        assertEquals("a", lexicon.findTerm("a").getTerm());
        assertNull(lexicon.findTerm("c"));
    }

}
