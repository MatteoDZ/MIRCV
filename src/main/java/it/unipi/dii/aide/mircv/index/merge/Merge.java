package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.binary.BinaryFile;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.posting.PostingIndex;
import it.unipi.dii.aide.mircv.index.utils.Statistics;
import org.javatuples.Pair;

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

        Lexicon lexicon = new Lexicon();

        FileChannel fcSkippingBlock = FileChannel.open(Paths.get(Configuration.SKIPPING_BLOCK_PATH),
                StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        FrequencyFile frequencyWriter = new FrequencyFile();
        DocIdFile docIdWriter = new DocIdFile();

        long docIds_offset;
        long freqs_offset;
        long docIds_offset_old=0L;

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



                if (postingList.getTerm().equals(minTerm)) {
                    //we are inside a reader with the min term
                    minPosting.appendList(postingList);

                    BlockReader reader = entry.getKey();
                    String line = reader.readTerm();


                    if (line != null) {
                        List<Integer> docIds = reader.readNumbers();
                        List<Integer> freqs = reader.readNumbers();
                        readerLines.put(reader, new PostingIndex(line, docIds, freqs));
                    } else {
                        iterator.remove(); // Remove the current reader from the list
                    }
                }
            }

            List<Integer> docIdsNew = minPosting.getDocIds();
            List<Integer> freqsNew = minPosting.getFrequencies();
            int docId;


            int block_size;
            int num_blocks;

            if (minPosting.getPostings().size() <= this.blockSize) {
                block_size = minPosting.getPostings().size();
                num_blocks = 1;
            }
            else {
                block_size = (int) Math.ceil(Math.sqrt(minPosting.getPostings().size()));
                num_blocks = (int) Math.ceil((double) minPosting.getPostings().size()/block_size);
            }

            ArrayList<Integer> docIds;
            ArrayList<Integer> freqs;

            // System.out.println("Size of minPosting: " + minPosting.getPostings().size() + " block_size: " + block_size + " num_blocks: " + num_blocks);

            lexiconWrite(minPosting, fcSkippingBlock.size(), lexicon, num_blocks);

            for (int currentBlock = 0; currentBlock < num_blocks; currentBlock++){
                docIds = new ArrayList<>();
                freqs = new ArrayList<>();

                for(int i = 0; i < block_size; i++) {
                    if(currentBlock * block_size + i < minPosting.getPostings().size()) {
                        docIds.add(docIdsNew.get(currentBlock * block_size + i));
                        freqs.add(freqsNew.get(currentBlock * block_size + i));
                    }
                }

                Pair<Long, Integer> pair_docIds = docIdWriter.writeBlock(docIds, compress);
                Pair<Long, Integer> pair_freqs = frequencyWriter.writeBlockP(freqs, compress);

                SkippingBlock skippingBlock = new SkippingBlock();
                skippingBlock.setDoc_id_offset(pair_docIds.getValue0());
                skippingBlock.setFreq_offset(pair_freqs.getValue0());
                skippingBlock.setDoc_id_max(docIds.get(docIds.size() - 1));
                skippingBlock.setDoc_id_size(compress ? pair_docIds.getValue1() : docIds.size());
                skippingBlock.setFreq_size(compress ? pair_freqs.getValue1() : freqs.size());
                skippingBlock.setNum_posting_of_block(docIds.size());
                if(!skippingBlock.writeToDisk(fcSkippingBlock)) {
                    System.out.println("Problems with writing the block of postings to disk.");
                }

            }
        }
        fcSkippingBlock.close();

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

    /**
     * Writes the lexicon entry for a given term.
     *
     * @param pi The PostingIndex containing the postings for the term.
     * @param offset The offset at which to write the lexicon entry.
     * @param lexicon The Lexicon object for writing the entry.
     * @param numBlock The number of blocks.
     * @throws IOException If an I/O error occurs.
     */
    protected void lexiconWrite(PostingIndex pi, Long offset, Lexicon lexicon, Integer numBlock) throws IOException {
        float BM25Upper = 0F;
        float actualBM25;
        int  tf  = 0;

        int df = pi.getPostings().size();

        for (Posting posting : pi.getPostings()) {
            actualBM25 = calculateBM25(tf, posting.getDoc_id());

            if (actualBM25 != -1 && actualBM25 > BM25Upper){
                BM25Upper = actualBM25;
            }

            if (tf < posting.getFrequency()) {
                tf = posting.getFrequency();
            }
        }
        lexicon.write(pi.getTerm(), offset, df, (double) stats.getNumDocs(), tf, BM25Upper, numBlock);
    }

    /**
     * Calculates the BM25 score for a specific term in a document given the term frequency (tf) and the document ID.
     * Utilizes the BM25 configuration parameters defined in the Configuration class.
     *
     * @param tf The term frequency (tf) in the document.
     * @param doc_id The ID of the document.
     * @return The calculated BM25 score for the term in the specified document.
     * @throws RuntimeException If an error occurs while reading from the document terms file.
     */
    protected float calculateBM25(Integer tf, Integer doc_id){
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
