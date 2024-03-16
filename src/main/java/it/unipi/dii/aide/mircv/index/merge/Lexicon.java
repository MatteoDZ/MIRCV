package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.config.Configuration;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

public class Lexicon {
    private final FileChannel fc;
    private final LFUCache<String, LexiconData> lfuCache = new LFUCache<>(Configuration.LEXICON_CACHE_SIZE);
    protected static final int MAX_LEN_OF_TERM = 32;
    private final LexiconData lexicon;


    public Lexicon(String pathLexicon) {
        lexicon = new LexiconData();
        try {
            fc = FileChannel.open(Paths.get(Objects.requireNonNull(pathLexicon)),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + pathLexicon + " file.");
        }
    }


    public void writeFixed(String term, long offset, List<Integer> docs, List<Integer> freqs) throws IOException {
        lexicon.setTerm(term);
        lexicon.setOffsetInvertedIndex(offset);
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
     * @param paddedString The padded string.
     * @return The string without padding.
     */
    public static String removePadding(String paddedString) {
        String trimmed = paddedString.trim();
        int nullIndex = trimmed.indexOf(' ');
        return nullIndex >= 0 ? trimmed.substring(0, nullIndex) : trimmed;
    }

    public LexiconData findTerm(String termToFind) throws IOException {
        long bot = 0;
        long mid;
        long top = fc.size()/LexiconData.ENTRY_SIZE;
        LexiconData entry = new LexiconData();

        while (bot <= top) {
            mid = (bot + top) / 2;
            entry.readEntryFromDisk(mid * LexiconData.ENTRY_SIZE, fc);

            if (entry.getTerm().isEmpty()) {
                System.out.println("Term "+termToFind+" not found in lexicon");
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
        System.out.println("Term "+termToFind+" not found in lexicon");
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
        LexiconData lexiconEntry = findTerm(term);
        if (lexiconEntry == null) {
            return null;
        }
        lfuCache.put(term, lexiconEntry);
        return lfuCache.get(term);
    }

    /**
     * Clears the cache.
     */
    public void clear(){
        lfuCache.clear();
    }

}
