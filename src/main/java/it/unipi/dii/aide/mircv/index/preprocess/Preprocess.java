package it.unipi.dii.aide.mircv.index.preprocess;

import it.unipi.dii.aide.mircv.index.config.Configuration;

import org.tartarus.snowball.ext.PorterStemmer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Preprocess {
    private static final Set<String> stopwords;

    static {
        try (Stream<String> lines = Files.lines(Paths.get(Objects.requireNonNull(Configuration.PATH_STOPWORDS)))) {
            stopwords = lines.collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("File " + Configuration.PATH_STOPWORDS + " not found");
        }
    }

    /**
     * Processes the given text by applying a series of preprocessing steps.
     *
     * @param  text                          the text to be processed
     * @param  flagStemmingAndStopwords      indicates whether stemming and stopwords removal should be applied
     * @return                               a list of processed strings
     */
    public static List<String> processText(String text, boolean flagStemmingAndStopwords) {
        return Stream.of(text)
                .map(Preprocess::removeUrls)
                .map(Preprocess::removeHtmlTags)
                .map(Preprocess::removePunctuation)
                .map(Preprocess::removeDiacritics)
                .map(Preprocess::removeDigits)
                .map(Preprocess::removeWordsThreeEqualLetter)
                .map(Preprocess::removeWhitespaces)
                .map(String::toLowerCase)
                .map(word -> flagStemmingAndStopwords ? Preprocess.applyStemming(Preprocess.removeStopwords(word)) : word)
                .map(Preprocess::tokenize)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Removes URLs from the input text.
     *
     * @param text The input text containing URLs.
     * @return The text with URLs removed.
     */
    protected static String removeUrls(String text) {
        return text.replaceAll("[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)", " ");
    }

    /**
     * Removes HTML tags from the input text.
     *
     * @param text The input text containing HTML tags.
     * @return The text with HTML tags removed.
     */
    protected static String removeHtmlTags(String text) { return text.replaceAll("<[^>]+>", "");}

    /**
     * Removes punctuation from the input text.
     *
     * @param text The input text containing punctuation.
     * @return The text without punctuation.
     */
    protected static String removePunctuation(String text) {
        return text.chars()
                .map(c -> Character.isLetterOrDigit(c) || Character.isWhitespace(c) ? c : ' ')
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Removes all digits.
     *
     * @param  text  the input text to remove digits from
     * @return       the modified text with digits removed
     */
    public static String removeDigits(String text) {
        return text.replaceAll("[^a-zA-Z ]", "");
    }

    /**
     * Removes consecutive occurrences of three or more equal letters in a given text.
     *
     * @param text The input text from which to remove consecutive occurrences.
     * @return The text with consecutive occurrences of three or more equal letters replaced with two occurrences.
     */
    public static String removeWordsThreeEqualLetter(String text) {
        return text.replaceAll("(.)\\1{2,}", "$1$1");
    }

    /**
     * Removes multiple whitespaces from the input text.
     *
     * @param text The input text containing multiple whitespaces.
     * @return The text with consecutive whitespaces collapsed to a single space.
     */
    protected static String removeWhitespaces(String text) {
        return text.replaceAll(" +", " ");
    }

    /**
     * Removes stopwords from the input text.
     *
     * @param text The input text containing stopwords.
     * @return The text without stopwords.
     */
    public static String removeStopwords(String text) {
        String[] words = text.split("\\s+");
        StringBuilder filteredText = new StringBuilder();
        for (String word : words) {
            if (!stopwords.contains(word)) {
                filteredText.append(word).append(" ");
            }
        }
        return filteredText.toString().trim();
    }


    /**
     * Applies stemming to each word in the input text.
     *
     * @param text The input text to which stemming is applied.
     * @return The text with stemming applied to each word.
     */
    protected static String applyStemming(String text) {
        PorterStemmer stemmer = new PorterStemmer();
        StringJoiner stemmedText = new StringJoiner(" ");
        String[] words = text.split("\\s+");
        for (String word : words) {
            stemmer.setCurrent(word);
            if (stemmer.stem()) {
                stemmedText.add(stemmer.getCurrent());
            }
        }
        return stemmedText.toString();
    }

    /**
     * Splits a string into tokens using spaces as delimiters.
     *
     * @param text The string to be divided into tokens.
     * @return An array of strings, each representing a token obtained by splitting the string based on spaces.
     */
    protected static List<String> tokenize(String text) {return List.of(text.split("\\s+"));}

    /**
     * Removes diacritics from the given text.
     *
     * @param  text  the text to remove diacritics from
     * @return the text with diacritics removed
     */
    protected static String removeDiacritics(String text) {
        return text.replaceAll("[^\\p{ASCII}]", " ");
    }

    /**
     * A function that takes a list of strings and returns a new list with unique strings.
     *
     * @param  tokens   the list of strings
     * @return          the new list with unique strings
     */
    public static List<String> uniqueToken(List<String> tokens) {
        return tokens.stream().distinct().collect(Collectors.toList());
    }

}
