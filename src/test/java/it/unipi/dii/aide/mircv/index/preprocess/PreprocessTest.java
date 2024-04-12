package it.unipi.dii.aide.mircv.index.preprocess;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PreprocessTest {

    @Test
    void processText() {
        assertLinesMatch(List.of("text", "url", "content"),
                Preprocess.processText("This is a 'text' -   with - a URL https://www.example.com and some content.", true));
        assertLinesMatch(List.of("text", "case", "punctuat", "remain"),
                Preprocess.processText("This text, with Case AnD Punctuation, remains.", true));
        assertLinesMatch(List.of("cat", "tabl"),
                Preprocess.processText("The cat is on the table.", true));
        assertLinesMatch(List.of("twinkl", "twinkl", "littl", "bat", "wonder", "world", "fly", "like", "tea", "trai", "sky"),
                Preprocess.processText("""
                        Twinkle, twinkle, little bat.
                         How I wonder what you’re at!
                         Up above the world you fly.
                         Like a tea-tray in the sky.""", true));
        assertLinesMatch(List.of("cat", "tabl", "attent"),
                Preprocess.processText("The      cat is on the table. Attention! â", true));
        assertLinesMatch(List.of("url", "univers", "pisa"),
                Preprocess.processText("this is the url of university of Pisa \n https://www.unipi.it", true));
        assertLinesMatch(List.of("1000", "2020"),
                Preprocess.processText("1000 2020 00001 tuttte", true));
    }

    @Test
    void removeUrls() {
        assertEquals("This is a text with a URL   and some content.",
                Preprocess.removeUrls("This is a text with a URL https://www.example.com and some content."));
        assertEquals("Text with URLs   and  ",
                Preprocess.removeUrls("Text with URLs https://example.com and https://anotherexample.com."));
        assertEquals("This is a text with an FTP link  ",
                Preprocess.removeUrls("This is a text with an FTP link ftp://ftp.example.com."));
        assertEquals("Text with special characters: *&^%$#",
                Preprocess.removeUrls("Text with special characters: *&^%$#"));
    }

    @Test
    void removeHtmlTags() {
        assertEquals("This is a test with HTML tags.",
                Preprocess.removeHtmlTags("<p>This is a <strong>test</strong> with <em>HTML</em> tags.</p>"));
        assertEquals("The alert('Hello'); is removed.",
                Preprocess.removeHtmlTags("The <script>alert('Hello');</script> is removed."));
    }

    @Test
    void removePunctuation() {
        assertEquals("This has   some   punctuation   That s it  Ok  a   â",
                Preprocess.removePunctuation("This has - some - punctuation!! That's it. Ok? a @ â"));
        assertEquals("            ", Preprocess.removePunctuation("...,,,!!!???"));
        assertEquals("Text with special characters        ",
                Preprocess.removePunctuation("Text with special characters: *&^%$#"));
    }

    @Test
    void removeWhitespaces() {
        assertEquals("This has multiple spaces.",
                Preprocess.removeWhitespaces("This  has           multiple                  spaces."));
    }

    @Test void removeStopwords() {
        assertEquals("This test remove common English stopwords.",
                Preprocess.removeStopwords("This is a test to remove some common English stopwords."));
        assertEquals("", Preprocess.removeStopwords("a an the and or but"));
        assertEquals("This text, Case Punctuation, remains.",
                Preprocess.removeStopwords("This text, with Case and Punctuation, remains."));
        assertEquals("The cat table.",
                Preprocess.removeStopwords("The cat is on the table."));
        assertEquals("",
                Preprocess.removeStopwords("to be or not to be"));
        assertEquals("sentence contains stopwords",
                Preprocess.removeStopwords("this sentence contains some stopwords"));
        assertEquals("test return true",
                Preprocess.removeStopwords("this test should return true"));
    }


    @Test
    void applyStemming() {
        assertEquals("Run run runner", Preprocess.applyStemming("Running runs runner"));
        assertEquals("Thi ha some punctuat That it Ok a", Preprocess.applyStemming("This has  some  punctuation Thats it Ok a"));
        assertEquals("jump jump jump", Preprocess.applyStemming("jumping jumps jumped"));
        assertEquals("plai plai plai", Preprocess.applyStemming("plays playing played"));
        assertEquals("goe go gone", Preprocess.applyStemming("goes going gone"));
    }

    @Test
    void tokenize() {
        assertLinesMatch(List.of(""), Preprocess.tokenize(""));
        assertLinesMatch(List.of("This", "is", "a", "test"), Preprocess.tokenize("This is a test"));
        assertLinesMatch(List.of("Multiple", "spaces"), Preprocess.tokenize("Multiple    spaces"));
    }

    @Test
    void removeDiacritics() {
        assertEquals(" ", Preprocess.removeDiacritics("â"));
    }

    @Test
    void uniqueToken(){
        assertLinesMatch(List.of("a", "b", "c"), Preprocess.uniqueToken(List.of("a", "b", "c", "a")));
    }

    @Test
    void removeDigitsTest(){
        assertEquals("In 1800 was born my grandfather  ", Preprocess.removeDigits("In 1800 was born my grandfather 09098873 00001"));
    }

    @Test
    void removeWordsThreeEqualLetterTest(){
        assertEquals("In 2000 was born my brother", Preprocess.removeWordsThreeEqualLetter("In 2000 was born my brother"));
        assertEquals("In 2000  born my brother", Preprocess.removeWordsThreeEqualLetter("In 2000 wassss born my brother"));
    }

}