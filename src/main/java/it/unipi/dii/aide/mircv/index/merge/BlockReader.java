package it.unipi.dii.aide.mircv.index.merge;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BlockReader class reads terms and associated document IDs from a binary file.
 */
public class BlockReader {
    private final DataInputStream input; // Input stream for reading data
    String lastWord; // Last term read from the file

    /**
     * Constructor for BlockReader.
     *
     * @param path The path of the binary file to be read.
     * @throws FileNotFoundException If the specified file is not found.
     */
    public BlockReader(String path) throws FileNotFoundException {
        // File path
        input = new DataInputStream(new FileInputStream(path));
    }

    /**
     * Reads a term from the binary file.
     *
     * @return The term read from the file.
     * @throws IOException If an I/O error occurs.
     */
    public String readTerm() throws IOException {
        int termLength;
        try {
            // Read the length of the term
            termLength = input.readInt();
        } catch (EOFException e) {
            // If no data is found, the block is terminated
            return null;
        }

        // Read the term characters based on the length
        StringBuilder term = new StringBuilder();
        for (int i = 0; i < termLength; i++) {
            term.append(input.readChar());
        }
        return term.toString();
    }

    /**
     * Reads a list of document IDs from the binary file.
     *
     * @return The list of document IDs read from the file.
     * @throws IOException If an I/O error occurs.
     */
    public List<Integer> readNumbers() throws IOException {
        int docId;
        List<Integer> docIds = new ArrayList<>();

        // Read document IDs until the sentinel value (-1) is encountered
        while ((docId = input.readInt()) != -1) {
            docIds.add(docId);
        }

        return docIds;
    }
}

