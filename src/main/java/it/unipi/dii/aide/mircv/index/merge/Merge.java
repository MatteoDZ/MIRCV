package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.Statistics;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
                    postingList.getFrequencies().get(0);

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
            lexiconWrite(minPosting, offsetTerm, docIdsNew, freqsNew, lexicon);

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

    protected void lexiconWrite(PostingIndex pi, long offset, List<Integer> docIds, List<Integer> freqs, Lexicon lexicon) throws IOException {
        float BM25Upper = 0F;
        float actualBM25;
        int  tf  = 0;

        Statistics stats = new Statistics(this.pathStatistics);
        stats.readSPIMI();
        int df = pi.getPostings().size();
        float idf = (float) ((Math.log((double) stats.getNumDocs() / df)));

        for (Posting posting : pi.getPostings()) {
            actualBM25 = 1F;

            if (actualBM25 != -1 && actualBM25 > BM25Upper){
                BM25Upper = actualBM25;
            }

            if (tf < posting.getFrequency()) {
                tf = posting.getFrequency();
            }
        }

        lexicon.write(pi.getTerm(), offset, df, stats.getNumDocs(), tf);
    }


    protected float calculateBM25(PostingIndex pi, float tf, long doc_id, Statistics stats){
        int doc_len = pi.getDocIds().size();
        return (float) ((tf / (tf + Configuration.BM25_K1 * (1 - Configuration.BM25_B + Configuration.BM25_B * (doc_len / stats.getAvgDocLen())))));
    }

    /*protected static float calculateBM25(float tf, long doc_id, Statistics stats){
        try {
            assert Configuration.PATH_DOCIDS != null;
            FileChannel fc = FileChannel.open(Path.of(Configuration.PATH_DOCIDS), StandardOpenOption.READ);
            MappedByteBuffer mappedByteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, (doc_id - 1) * 20 + 8 + 4, 8);
            long doc_len = mappedByteBuffer.getLong();
            return (float) ((tf / (tf + Configuration.BM25_K1 * (1 - Configuration.BM25_B + Configuration.BM25_B * (doc_len / stats.getAvgDocLen())))));
        } catch (IOException e) {
            System.out.println(e);
            return -1F;
        }
    }*/


}
