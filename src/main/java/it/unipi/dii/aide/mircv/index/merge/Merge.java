package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.Statistics;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Merge {
    private final HashMap<BlockReader, PostingIndex> readerLines = new HashMap<>();
    private final Integer blockSize;
    private final Statistics stats = new Statistics();

    public Merge(List<String> paths, Integer blockSize) throws IOException {
        for (String path : paths) {
            BlockReader reader = new BlockReader(path);
            String line = reader.readTerm();
            List<Integer> docIds = reader.readNumbers();
            List<Integer> freqs = reader.readNumbers();
            readerLines.put(reader, new PostingIndex(line, docIds, freqs));
        }
        this.blockSize = blockSize;
        stats.readSpimiFromDisk();
    }

    public void write(boolean compress) throws IOException {
        long lexSize = 0L;
        long time_to_min_term = 0;
        long time_to_while_cycle = 0;
        long time_of_while_cycle = 0;
        long time_docs_freqs_removeduplicates = 0;
        long time_after_everything_dc = 0;
        long time_skip_1 = 0;
        long time_skip_2 = 0;
        long time_skip_3 = 0;
        long time_skip_4 = 0;
        long time_skip_5 = 0;
        long time_skip_6 = 0;
        long time_skip_7 = 0;
        long time_lexicon = 0;

        //InvertedIndexFile inv = new InvertedIndexFile( this.blockSize);
        Lexicon lexicon = new Lexicon();

        FileChannel fcSkippingBlock = FileChannel.open(Paths.get(Configuration.SKIPPING_BLOCK_PATH),
                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        FrequencyFile frequencyWriter = new FrequencyFile(blockSize);
        DocIdFile docIdWriter = new DocIdFile(blockSize);

        long time = System.currentTimeMillis();
        while (!readerLines.isEmpty()) {
            long time_read_term = System.currentTimeMillis();

            if (lexSize % 100000 == 0)
                System.out.println("Term number " + lexSize);

            lexSize++;

            long time_min_term = System.currentTimeMillis();
            time_to_min_term += (time_min_term - time_read_term);

            String minTerm = findMinTerm(readerLines);

            // System.out.println("minTerm: " + minTerm);

            PostingIndex minPosting = new PostingIndex(minTerm);
            Iterator<Map.Entry<BlockReader, PostingIndex>> iterator = readerLines.entrySet().iterator();

            long time_to_while = System.currentTimeMillis();
            time_to_while_cycle += (time_to_while - time_min_term);

            while (iterator.hasNext()) {

                Map.Entry<BlockReader, PostingIndex> entry = iterator.next();
                PostingIndex postingList = entry.getValue();

                // System.out.println("Term PostingList: " + postingList.getTerm());

                if (postingList.getTerm().equals(minTerm)) {
                    //we are inside a reader with the min term
                    minPosting.appendList(postingList);
                    // postingList.getFrequencies().get(0);

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

            long time_after_while = System.currentTimeMillis();
            time_of_while_cycle += time_after_while - time_to_while;



            List<Integer> docIdsNew = minPosting.getDocIds();
            List<Integer> freqsNew = minPosting.getFrequencies();
            int docId;

            while ((docId = findDuplicate(docIdsNew)) != -1) {
                // System.out.println("Prima DocIds: " + docIdsNew + " Freqs: " + freqsNew + " DocId: " + docId);
                docIdsNew.remove(docId);
                int freq = freqsNew.get(docId);
                freqsNew.remove( docId);
                freqsNew.add(docId, freq + freqsNew.get(docId + 1));
                // System.out.println("Dopo DocIds: " + docIdsNew + " Freqs: " + freqsNew + " DocId: " + docId);
            }
            // System.out.println("Term: " + minPosting.getTerm() + " DocIds: " + docIdsNew.size() + " Freqs: " + freqsNew.size());

            long time_docids_freqs = System.currentTimeMillis();
            time_docs_freqs_removeduplicates += time_docids_freqs - time_after_while;

            lexiconWrite(minPosting, fcSkippingBlock.size(), docIdsNew, freqsNew, lexicon);
            long time_after_lexicon = System.currentTimeMillis();
            time_lexicon += time_after_lexicon - time_docids_freqs;

            SkippingBlock skippingBlock = new SkippingBlock();

            long time_skipping_pre = System.currentTimeMillis();
            skippingBlock.setDoc_id_offset(docIdWriter.writeDocIds(docIdsNew, compress).get(0));
            long time_skipping_docidoffset = System.currentTimeMillis();
            time_skip_1 += time_skipping_docidoffset - time_skipping_pre;

            skippingBlock.setFreq_offset(frequencyWriter.writeFrequencies(freqsNew, compress).get(0));
            long time_skipping_freqsoffset = System.currentTimeMillis();
            time_skip_2 += time_skipping_freqsoffset - time_skipping_docidoffset;

            skippingBlock.setDoc_id_max(docIdsNew.get(docIdsNew.size() - 1));
            long time_skipping_docidmax = System.currentTimeMillis();
            time_skip_3 += time_skipping_docidmax - time_skipping_freqsoffset;

            skippingBlock.setDoc_id_size(docIdsNew.size());
            long time_skipping_docidsize = System.currentTimeMillis();
            time_skip_4 += time_skipping_docidsize - time_skipping_docidmax;

            skippingBlock.setFreq_size(freqsNew.size());
            long time_skipping_freqsize = System.currentTimeMillis();
            time_skip_5 += time_skipping_freqsize - time_skipping_docidsize;

            skippingBlock.setNum_posting_of_block((int) lexSize);
            long time_skipping_numblock = System.currentTimeMillis();
            time_skip_6 += time_skipping_numblock - time_skipping_freqsize;

            skippingBlock.writeOnDisk(fcSkippingBlock);
            long time_skipping_post = System.currentTimeMillis();
            time_skip_7 += time_skipping_post - time_skipping_numblock;



            //long offsetTerm = inv.write(docIdsNew, freqsNew, compress);
            //lexiconWrite(minPosting, offsetTerm, docIdsNew, freqsNew, lexicon);
            long time_after_writing = System.currentTimeMillis();
            time_after_everything_dc += time_after_writing - time_docids_freqs;


        }
        fcSkippingBlock.close();

        System.out.println("Time min term: " + time_to_min_term);
        System.out.println("Time before starting while: " + time_to_while_cycle);
        System.out.println("Time of internal while cycle: " + time_of_while_cycle);
        System.out.println("Time retrieving docs freqs and removing duplicates: " + time_docs_freqs_removeduplicates);
        System.out.println("Time writing lexicon and skipping block: " + time_after_everything_dc);

        System.out.println("It' lexiconing time: " + time_lexicon);

        System.out.println("Skip 1: " + time_skip_1);
        System.out.println("Skip 2: " + time_skip_2);
        System.out.println("Skip 3: " + time_skip_3);
        System.out.println("Skip 4: " + time_skip_4);
        System.out.println("Skip 5: " + time_skip_5);
        System.out.println("Skip 6: " + time_skip_6);
        System.out.println("Skip 7: " + time_skip_7);



        Statistics statistics = new Statistics();
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
        return -1;
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

        int df = pi.getPostings().size();
        float idf = (float) ((Math.log((double) stats.getNumDocs() / df)));

        for (Posting posting : pi.getPostings()) {
            actualBM25 = calculateBM25(tf, posting.getDoc_id());

            if (actualBM25 != -1 && actualBM25 > BM25Upper){
                BM25Upper = actualBM25;
            }

            if (tf < posting.getFrequency()) {
                tf = posting.getFrequency();
            }
        }
        lexicon.write(pi.getTerm(), offset, df, stats.getNumDocs(), tf, BM25Upper);
    }

    protected float calculateBM25(float tf, long doc_id){
        try {
            FileChannel fc = FileChannel.open(Path.of(Configuration.PATH_DOC_TERMS), StandardOpenOption.READ, StandardOpenOption.WRITE);
            int doc_len = BinaryFile.readIntFromBuffer(fc, doc_id*4L);
            fc.close();
            return (float) ((tf / (tf + Configuration.BM25_K1 * (1 - Configuration.BM25_B + Configuration.BM25_B * (doc_len / stats.getAvgDocLen())))));
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while reading from the " + Configuration.PATH_DOC_TERMS + " file.");
        }
    }


}
