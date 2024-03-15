package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.InvertedIndex;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Merge {
    private final HashMap<BlockReader, PostingIndex> readerLines = new HashMap<>();
    private final String pathLexicon, pathDocIds, pathFreqs;
    private final Integer blockSize;

    public Merge(List<String> paths, String pathLexicon, String pathDocIds, String pathFreqs, Integer blockSize) throws IOException {
        for(String path :paths){
            BlockReader reader = new BlockReader(path);
            String line = reader.readTerm();
            List<Integer> docIds = reader.readNumbers();
            List<Integer> freqs = reader.readNumbers();
            // System.out.println("Path: " + path + " Riga: " + line + " DocId: " + docIds + " Freq: " + freqs);
            readerLines.put(reader, new PostingIndex(line, docIds, freqs));
        }

        this.pathFreqs = pathFreqs;
        this.pathDocIds = pathDocIds;
        this.pathLexicon = pathLexicon;
        this.blockSize = blockSize;
    }

    public void write(String path, boolean compress) throws IOException {

        int i = 0;

        InvertedIndexFile inv = new InvertedIndexFile(path, this.pathDocIds, this.pathFreqs, this.blockSize);
        Lexicon lexicon = new Lexicon(this.pathLexicon);


        while (!readerLines.isEmpty()) {
            if (i % 100000 == 0)
                System.out.println("Line number " + i);
            i++;

            String minTerm = findMinTerm(readerLines);
            // System.out.println("minTerm: " + minTerm);

            PostingIndex minPosting = new PostingIndex(minTerm);
            Iterator<Map.Entry<BlockReader, PostingIndex>> iterator = readerLines.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<BlockReader, PostingIndex> entry = iterator.next();
                PostingIndex postingList = entry.getValue();

                // System.out.println("Term PostingList: " + postingList.getTerm());


                if (postingList.getTerm().equals(minTerm)) {
                    //we are inside a reader with the min term
                    minPosting.appendList(postingList);

                    // System.out.println("Term IF " + postingList.getTerm());

                    BlockReader reader = entry.getKey();
                    String line = reader.readTerm();
                    // System.out.println("Line: " + line);


                    if (line != null) {
                        List<Integer> docIds = reader.readNumbers();
                        // System.out.println("DocIds: " + docIds);
                        List<Integer> freqs = reader.readNumbers();
                        // System.out.println("Freqs: " + freqs);
                        readerLines.put(reader, new PostingIndex(line, docIds, freqs));
                    } else {
                        iterator.remove(); // Remove the current reader from the list
                    }
                }
            }

            List<Integer> docIdsNew = minPosting.getDocIds();
            List<Integer> freqsNew = minPosting.getFrequencies();
            Integer docId;

            while ((docId = findDuplicate(docIdsNew)) != 0){
                // System.out.println("Prima DocIds: " + docIdsNew + " Freqs: " + freqsNew + " DocId: " + docId);
                docIdsNew.remove((int) docId);
                int freq = freqsNew.get(docId);
                freqsNew.remove((int) docId);
                freqsNew.add(docId,  freq+ freqsNew.get(docId + 1));
                // System.out.println("Dopo DocIds: " + docIdsNew + " Freqs: " + freqsNew + " DocId: " + docId);
            }
            // System.out.println("Term: " + minPosting.getTerm() + " DocIds: " + docIdsNew + " Freqs: " + freqsNew);
            long offsetTerm = inv.write(docIdsNew, freqsNew, compress);
            lexicon.writeFixed(minPosting.getTerm(), offsetTerm, docIdsNew, freqsNew);


            }
        }

        /**
         * Find duplicates in the given list of integers and return a set containing the duplicate elements.
         *
         * @param  listContainingDuplicates   the list of integers containing possible duplicates
         * @return a set containing the duplicate elements
         */
        public Integer findDuplicate (List<Integer> listContainingDuplicates) {
            final Set<Integer> set1 = new HashSet<>();

            for (Integer yourInt : listContainingDuplicates) {
                if (!set1.add(yourInt)) {
                    return listContainingDuplicates.indexOf(yourInt);
                }
            }
            return 0;
        }


        /**
         * this function finds the min term between all the posting lists of the same line of the intermediateIndexes
         * @param map is the map of the posting lists, containing the reader of each intermediateIndex file associated to the last posting list read from that intermediateIndex
         * @return the min term found
         */
        public static String findMinTerm (HashMap < BlockReader, PostingIndex > map){
            String minTerm = null;

            for (PostingIndex postingList : map.values()) {
                String term = postingList.getTerm();
                if (minTerm == null || term.compareTo(minTerm) < 0) {
                    minTerm = term;
                }
            }

            return minTerm;
        }


    }
