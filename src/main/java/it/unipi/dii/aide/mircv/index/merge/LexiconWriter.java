package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.config.Configuration;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class LexiconWriter {

    private final FileChannel fc;
    private MappedByteBuffer mbb;

    private HashMap<String, Long> lexicon; // <term, offset>

    public HashMap<String, Long> getLexicon() {return lexicon;}

    public LexiconWriter(String pathLexicon) {
        lexicon = new HashMap<>();
        try {
            // Open file channel for reading and writing
            fc = FileChannel.open(Paths.get(Objects.requireNonNull(pathLexicon)),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + pathLexicon + " file.");
        }
    }


    public void write(String term, long offset) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 2);
        mbb.putShort((short)term.length());
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), term.length() * 2L);
        for(int i=0;i<term.length();i++){
            mbb.putChar(term.charAt(i));
        }
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), 8);
        mbb.putLong(offset);
    }

    public void read(String pathLexicon){
        try (InputStream input = new FileInputStream(pathLexicon);
             DataInputStream inputStream = new DataInputStream(input)) {
            while (inputStream.available() > 0) {
                short termLength = inputStream.readShort();
                // Read the characters of the term
                StringBuilder termBuilder = new StringBuilder();
                for (int i = 0; i < termLength; i++) {
                    termBuilder.append(inputStream.readChar());
                }
                long offset = inputStream.readLong();
                lexicon.put(termBuilder.toString(), offset);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
