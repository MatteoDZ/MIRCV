package it.unipi.dii.aide.mircv.index.merge;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

public class Lexicon {

    private final FileChannel fc;
    private MappedByteBuffer mbb;

    protected static final int MAX_LEN_OF_TERM = 32;

    //private HashMap<String, Long> lexicon; // <term, offset>
    private LexiconData lexicon;

    //public HashMap<String, Long> getLexicon() {return lexicon;}

    public Lexicon(String pathLexicon) {
        lexicon = new LexiconData();
        try {
            // Open file channel for reading and writing
            fc = FileChannel.open(Paths.get(Objects.requireNonNull(pathLexicon)),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + pathLexicon + " file.");
        }
    }


    public void writeFixed(String term, long offset, List<Integer> docs, List<Integer> freqs) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), MAX_LEN_OF_TERM + 8);
        mbb.put(padStringToLength(term).getBytes(StandardCharsets.UTF_8));
        mbb.putLong(offset);
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

    public long findTerm(String termToFind) throws IOException {
        long bot = 0;
        long mid;
        long top = fc.size()/40;
        String termFound;

        while (bot <= top) {
            mid = (bot + top) / 2;
            mbb=fc.map(FileChannel.MapMode.READ_ONLY,mid * (MAX_LEN_OF_TERM+8), MAX_LEN_OF_TERM + 8);

            byte[] termBytes = new byte[32];
            mbb.get(termBytes);
            long offset = mbb.getLong();


            termFound = removePadding(new String(termBytes, StandardCharsets.UTF_8));

            if (termFound.isEmpty()) {
                return -1;
            }

            int comparisonResult = termToFind.compareTo(termFound);

            if (comparisonResult == 0) {
                return offset;
            } else if (comparisonResult > 0) {
                bot = mid + 1;
            } else {
                top = mid - 1;
            }
        }
        return -1;

    }

}
