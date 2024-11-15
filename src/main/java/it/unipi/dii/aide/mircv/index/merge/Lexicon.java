package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.config.Configuration;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class Lexicon {
    private final FileChannel fc; //filechannel for read and write operations
    private final LFUCache<String, LexiconData> lfuCache = new LFUCache<>(Configuration.LEXICON_CACHE_SIZE); //least frequently used cache
    protected static final int MAX_LEN_OF_TERM = 32; //maximum length of a term
    private final LexiconData lexicon;
    private static final Lexicon instance = new Lexicon();


    public Lexicon() {
        lexicon = new LexiconData();
        try {
            fc = FileChannel.open(Path.of(Paths.get(Objects.requireNonNull(Configuration.PATH_LEXICON)).toUri()),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + Configuration.PATH_LEXICON + " file.");
        }
    }

    /**
     * Retrieves the singleton instance of the Lexicon class.
     *
     * @return The Lexicon instance.
     */
    public static Lexicon getInstance() {
        return instance;
    }


    public void write(String term, Long offset, Integer df, Double docnum, Integer tf, Float bm25, Integer numBlocks) throws IOException {
        lexicon.setTerm(term);
        lexicon.setOffset_skip_pointer(offset);
        lexicon.setIdf((float) Math.log10((double) docnum /df));
        lexicon.setUpperTFIDF((float) ((1 + Math.log(tf)) * lexicon.getIdf()));
        lexicon.setUpperBM25(bm25);
        lexicon.setNumBlocks(numBlocks);
        lexicon.writeEntryToDisk(fc);
    }

    /**
     * Pads a string to a specified length.
     *
     * @param input The input string.
     * @return The padded string.
     */
    public static String padStringToLength(String input) {
        if (input.length() >= MAX_LEN_OF_TERM) {
            return input.substring(0, MAX_LEN_OF_TERM);
        } else {
            return String.format("%1$-" + MAX_LEN_OF_TERM + "s", input);
        }
    }

    /**
     * Removes padding from a padded string.
     *
     * @param originalString The padded string.
     * @return The string without padding.
     */
    public static String removePadding(String originalString) {
        String trimmed = originalString.trim();
        int nullIndex = trimmed.indexOf(' ');
        return nullIndex >= 0 ? trimmed.substring(0, nullIndex) : trimmed;
    }

    /**
     * Find a specific term within the lexicon data using binary search.
     *
     * @param  termToFind    the term to search for
     * @return               the LexiconData object representing the found term, or null if not found
     */
    public LexiconData findTerm(String termToFind) throws IOException {
        long bot = 0;
        long mid;
        long top = fc.size()/LexiconData.ENTRY_SIZE;
        LexiconData entry = new LexiconData();
        while (bot <= top) {
            mid = (bot + top) / 2;
            entry.readEntryFromDisk(mid * LexiconData.ENTRY_SIZE, fc);
            if (entry.getTerm().isEmpty()) {
                return null;
            }
            String termFound = entry.getTerm();
            int comparisonResult = termToFind.compareTo(termFound);
            if (comparisonResult == 0) {
                return entry;
            } else if (comparisonResult > 0) {
                bot = mid + 1;
            } else {
                top = mid - 1;
            }
        }
        return null;
    }


    /**
     * Gets the LexiconEntry for a given term, either from the cache or by retrieving it.
     *
     * @param term The term to get.
     * @return The LexiconEntry for the term, or null if not found.
     */
    public LexiconData get(String term) throws IOException {
        if (lfuCache.containsKey(term)) {
            return lfuCache.get(term);
        }
        LexiconData lexiconData = findTerm(term);
        if (lexiconData == null) {
            return null;
        }
        lfuCache.put(term, lexiconData);
        return lexiconData;
    }

    /**
     * Clears the cache.
     */
    public void clear(){
        lfuCache.clear();
    }

}
