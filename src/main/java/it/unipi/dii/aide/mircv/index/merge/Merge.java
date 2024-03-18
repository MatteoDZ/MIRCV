package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.Statistics;

import java.io.IOException;
import java.util.*;

public class Merge {
    private final HashMap<BlockReader, PostingIndex> readerLines = new HashMap<>();
    private final String pathLexicon, pathDocIds, pathFreqs, pathStatistics;
    private final Integer blockSize;

    public Merge(List<String> paths, String pathLexicon, String pathDocIds, String pathFreqs, String pathStatistics, Integer blockSize) throws IOException {
        for (String path : paths) {
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
        this.pathStatistics = pathStatistics;
        this.blockSize = blockSize;
    }

    public void write(String path, boolean compress) throws IOException {
        long lexSize = 0L;

        InvertedIndexFile inv = new InvertedIndexFile(path, this.pathDocIds, this.pathFreqs, this.blockSize);
        Lexicon lexicon = new Lexicon(this.pathLexicon);


        while (!readerLines.isEmpty()) {
            if (lexSize % 100000 == 0)
                System.out.println("Term number " + lexSize);

            lexSize++;

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
            int docId;

            while ((docId = findDuplicate(docIdsNew)) != 0) {
                // System.out.println("Prima DocIds: " + docIdsNew + " Freqs: " + freqsNew + " DocId: " + docId);
                docIdsNew.remove(docId);
                int freq = freqsNew.get(docId);
                freqsNew.remove( docId);
                freqsNew.add(docId, freq + freqsNew.get(docId + 1));
                // System.out.println("Dopo DocIds: " + docIdsNew + " Freqs: " + freqsNew + " DocId: " + docId);
            }
            // System.out.println("Term: " + minPosting.getTerm() + " DocIds: " + docIdsNew + " Freqs: " + freqsNew);
            long offsetTerm = inv.write(docIdsNew, freqsNew, compress);
            lexicon.write(minPosting.getTerm(), offsetTerm, docIdsNew, freqsNew);

        }
        Statistics statistics = new Statistics(pathStatistics);
        statistics.setTerms(lexSize);
        statistics.writeMergeToDisk();
    }



    /**
     * Finds the first duplicate element in a given list of integers.
     *
     * @param  listContainingDuplicates  the list of integers to search for duplicates
     * @return                          the index of the first duplicate element in the list
     */
    protected static Integer findDuplicate (List<Integer> listContainingDuplicates) {
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
    protected static String findMinTerm (HashMap<BlockReader, PostingIndex> map){
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
