package it.unipi.dii.aide.mircv.index.binary;


import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

public class BinaryFile {


    /**
     * Writes an inverted index block to a binary file.
     *
     * @param inv  the inverted index object containing the block to be written
     * @param path the file path where the block will be written
     * @throws RuntimeException if an error occurs while writing to the binary file
     */
    public static void writeBlock(InvertedIndex inv, String path){ //a serve a differenziarlo dal metodo precedente. Ã¨ una beta
        try (FileChannel fc = FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(),inv.calculateDimensionByte(2,4,2)); // errore nella scelta della dimensione . non dovrebbe avere il +10000. con +10000 da ancora errore quindi il tasso di errore Ã¨ molto alto. indagare
            List<String> sorted_keys=inv.sort();
            for(String key: sorted_keys){
                PostingIndex tpl=inv.searchTerm(key);
                String termine=tpl.getTerm();
                List<Integer> doc_ids=tpl.getDocIds();
                List<Integer> freqs=tpl.getFrequencies();

                putInt(mbb, termine, doc_ids, freqs);
            }
            //mbb.putInt(0); // esperimento per vedere se da ancora errore in lettura

        } catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException("An error occurred while writing to the binary file.");
        }
    }

    private static void putInt(MappedByteBuffer mbb, String termine, List<Integer> doc_ids, List<Integer> freqs) {
        mbb.putInt(termine.length()); //scrivo in un primo int la lunghezza del termine
        for(int i=0;i<termine.length();i++){
            mbb.putChar(termine.charAt(i));
        }

        for (Integer doc_id : doc_ids) {
            mbb.putInt(doc_id);
        }
        mbb.putInt(-1); //separatore tra doc id e freq
        for (Integer freq : freqs) {
            mbb.putInt(freq);
        }
        mbb.putInt(-1);
    }


    /**
     * Reads a block of data from a binary file and returns a list of Term_PostingList objects.
     * Each Term_PostingList object contains a term, a list of document IDs, and a list of frequencies.
     *
     * @param path The path of the binary file to read.
     * @return The list of Term_PostingList objects read from the file.
     * @throws RuntimeException if an error occurs while reading the file.
     */
    public static List<PostingIndex> readBlock(String path) {
        List<PostingIndex> invertedIndexBlock = new ArrayList<>();
        try (InputStream input = new FileInputStream(path);
             DataInputStream inputStream = new DataInputStream(input)) {
            while(inputStream.available()>0){
                // Read the length of the term
                int termLength = inputStream.readInt();
                // Read the characters of the term
                StringBuilder termBuilder = new StringBuilder();
                for (int i = 0; i < termLength; i++) {
                    termBuilder.append(inputStream.readChar());
                }

                // Read the document IDs
                int docId;
                List<Integer> docIds = new ArrayList<>();
                while ((docId = inputStream.readInt()) != -1) {
                    docIds.add(docId);
                }

                // Read the frequencies
                int frequency;
                List<Integer> frequencies = new ArrayList<>();
                while ((frequency = inputStream.readInt()) != -1) {
                    frequencies.add(frequency);
                }

                // Create a new Term_PostingList object and add it to the list
                invertedIndexBlock.add(new PostingIndex(termBuilder.toString(), docIds, frequencies));
                // System.out.println(new Term_PostingList(termBuilder.toString(), docIds, frequencies).toString());
            }
            return invertedIndexBlock;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while reading the binary file.", e);
        }
    }

    /**
     * Retrieves the position of the end of the file specified by the given path.
     *
     * @param  path  the path of the file
     * @return       the position of the end of the file
     * @throws RuntimeException if an error occurs while reading to the binary file
     */
    public static long getPosition(String path) {
        try (FileChannel fc = FileChannel.open(Paths.get(path), StandardOpenOption.READ)) {
            return fc.size();
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while reading the binary file.");
        }
    }

    /**
     * Appends the given term, list of document IDs, and list of frequencies to a binary file.
     *
     * @param  path  the path to the binary file
     * @param  term  the term to be appended
     * @param  docs  the list of document IDs
     * @param  freqs the list of frequencies
     */
    public static void appendToBinaryFile(String path, String term, List<Integer> docs, List<Integer> freqs) {
        try (FileChannel fc = FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(),
                    (long) docs.size() * 2 * 4 + term.length() * 2L + 2 * 4 + 4);
            putInt(mbb, term, docs, freqs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String readBinaryFile(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Path.of(path));
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while reading the binary file.");
        }
    }

}
