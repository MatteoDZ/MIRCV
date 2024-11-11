package it.unipi.dii.aide.mircv.index.merge;

import it.unipi.dii.aide.mircv.index.compression.UnaryCompressor;
import it.unipi.dii.aide.mircv.index.compression.VariableByteCompressor;
import it.unipi.dii.aide.mircv.index.config.Configuration;
import it.unipi.dii.aide.mircv.index.posting.Posting;
import it.unipi.dii.aide.mircv.index.utils.FileUtils;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class SkippingBlock {

    private static MappedByteBuffer bufferDocId = null;
    private static MappedByteBuffer bufferFreq = null;

    static {
        try {
            if(FileUtils.filesExist(Configuration.PATH_DOCID, Configuration.PATH_FREQ)){
                FileChannel fcDocId = FileChannel.open(Paths.get(Configuration.PATH_DOCID), StandardOpenOption.READ);
                FileChannel fcFreq = FileChannel.open(Paths.get(Configuration.PATH_FREQ), StandardOpenOption.READ);
                bufferDocId = fcDocId.map(FileChannel.MapMode.READ_ONLY, 0, fcDocId.size());
                bufferFreq = fcFreq.map(FileChannel.MapMode.READ_ONLY, 0, fcFreq.size());
            }

        } catch (IOException e) {
            System.out.println("Error while opening the file channel");
        }
    }
    private long docIdOffset;
    private int docIdSize;
    private long freqOffset;
    private int freqSize;
    private int docIdMax;
    private int numPostingOfBlock;
    private static long fileOffset = 0;
    public static final int SIZE_OF_ELEMENT = 8 * 2 + 4 * 4; //4bytes * (n_int) + 8bytes * (n_long)

    /**
     * Writes the skipping block information to disk.
     *
     * @param fileToWrite The file channel to write the skipping block information.
     * @return True if writing is successful, false otherwise.
     */
    public boolean writeToDisk(FileChannel fileToWrite) {

        assert fileToWrite!=null : "FileChannel is null";

        try {
            MappedByteBuffer mappedByteBuffer = fileToWrite.map(FileChannel.MapMode.READ_WRITE, fileOffset, SIZE_OF_ELEMENT);
            if (mappedByteBuffer == null) {
                return false;
            }
            mappedByteBuffer.putLong(docIdOffset);
            mappedByteBuffer.putInt(docIdSize);
            mappedByteBuffer.putLong(freqOffset);
            mappedByteBuffer.putInt(freqSize);
            mappedByteBuffer.putInt(docIdMax);
            mappedByteBuffer.putInt(numPostingOfBlock);
            fileOffset += SIZE_OF_ELEMENT;
            return true;
        } catch (IOException e) {
            System.out.println("Error while writing the postings to disk.");
            return false;
        }
    }

    // Getters and setters...

    public void setDocIdOffset(long docIdOffset) {
        this.docIdOffset = docIdOffset;
    }

    public void setDocIdSize(int docIdSize) {
        this.docIdSize = docIdSize;
    }


    public void setFreqOffset(long freqOffset) {
        this.freqOffset = freqOffset;
    }


    public void setFreqSize(int freqSize) {
        this.freqSize = freqSize;
    }

    public int getDocIdMax() {
        return docIdMax;
    }

    public void setDocIdMax(int docIdMax) {
        this.docIdMax = docIdMax;
    }


    public void setNumPostingOfBlock(int numPostingOfBlock) {
        this.numPostingOfBlock = numPostingOfBlock;
    }

    /**
     * Reads the postings from the disk based on the skipping block information.
     *
     * @param compression Boolean flag that indicates if the data is compressed or not.
     * @return ArrayList of Posting objects representing the postings in the skipping block.
     */
    public ArrayList<Posting> getSkippingBlockPostings(Boolean compression) {

        assert bufferDocId != null : "Error with the DocID MappedByteBuffer";
        assert bufferFreq != null : "Error with the Frequency MappedByteBuffer";

        bufferDocId.position((int) docIdOffset);
        bufferFreq.position((int) freqOffset);

        ArrayList<Posting> postings = new ArrayList<>();

        if (compression) {
            byte[] doc_ids = new byte[docIdSize];
            byte[] freqs = new byte[freqSize];

            bufferDocId.get(doc_ids, 0, docIdSize);
            bufferFreq.get(freqs, 0, freqSize);

            List<Short> freqsDecompressed = UnaryCompressor.integerArrayDecompression(freqs, numPostingOfBlock);
            List<Integer> docIdsDecompressed = VariableByteCompressor.decode(doc_ids);


            for (int i = 0; i < numPostingOfBlock; i++) {
                Posting posting = new Posting(docIdsDecompressed.get(2*i), freqsDecompressed.get(i), docIdsDecompressed.get(2*i + 1));
                postings.add(posting);
            }
        } else {
            for (int i = 0; i < numPostingOfBlock; i++) {
                Posting posting = new Posting(bufferDocId.getInt(), bufferFreq.getShort(), bufferDocId.getInt());
                postings.add(posting);
            }
        }

        return postings;
    }

    @Override
    public String toString() {
        return "SkippingBlock{" +
                "docIdOffset: " + docIdOffset +
                ", docIdSize: " + docIdSize +
                ", freqOffset: " + freqOffset +
                ", freqSize: " + freqSize +
                ", docIdMax: " + docIdMax +
                ", numPostingOfBlock: " + numPostingOfBlock +
                ", fileOffset: " + fileOffset +
                '}';
    }
}
