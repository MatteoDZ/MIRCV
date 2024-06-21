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
    private long doc_id_offset;
    private int doc_id_size;
    private long freq_offset;
    private int freq_size;
    private int doc_id_max;
    private int num_posting_of_block;
    private static long file_offset = 0;
    public static final int size_of_element = 8 * 2 + 4 * 4; //4bytes * (n_int) + 8bytes * (n_long)

    /**
     * Writes the skipping block information to disk.
     *
     * @param file_to_write The file channel to write the skipping block information.
     * @return True if writing is successful, false otherwise.
     */
    public boolean writeToDisk(FileChannel file_to_write) {
        /*
        if (file_to_write == null) {
            return false;
        }

         */
        assert file_to_write!=null : "FileChannel is null";
        try {
            MappedByteBuffer mappedByteBuffer = file_to_write.map(FileChannel.MapMode.READ_WRITE, file_offset, size_of_element);
            if (mappedByteBuffer == null) {
                return false;
            }
            mappedByteBuffer.putLong(doc_id_offset);
            mappedByteBuffer.putInt(doc_id_size);
            mappedByteBuffer.putLong(freq_offset);
            mappedByteBuffer.putInt(freq_size);
            mappedByteBuffer.putInt(doc_id_max);
            mappedByteBuffer.putInt(num_posting_of_block);
            file_offset += size_of_element;
            return true;
        } catch (IOException e) {
            System.out.println("Error while writing the postings to disk.");
            return false;
        }
    }

    // Getters and setters...

    public void setDoc_id_offset(long doc_id_offset) {
        this.doc_id_offset = doc_id_offset;
    }

    public void setDoc_id_size(int doc_id_size) {
        this.doc_id_size = doc_id_size;
    }


    public void setFreq_offset(long freq_offset) {
        this.freq_offset = freq_offset;
    }


    public void setFreq_size(int freq_size) {
        this.freq_size = freq_size;
    }

    public int getDoc_id_max() {
        return doc_id_max;
    }

    public void setDoc_id_max(int doc_id_max) {
        this.doc_id_max = doc_id_max;
    }


    public void setNum_posting_of_block(int num_posting_of_block) {
        this.num_posting_of_block = num_posting_of_block;
    }

    /**
     * Reads the postings from the disk based on the skipping block information.
     *
     * @param compression indicates if the data is compressed or not
     * @return ArrayList of Posting objects representing the postings in the skipping block.
     */
    public ArrayList<Posting> getSkippingBlockPostings(Boolean compression) {

        assert bufferDocId != null : "Error with the DocID MappedByteBuffer";
        assert bufferFreq != null : "Error with the Frequency MappedByteBuffer";

        bufferDocId.position((int) doc_id_offset);
        bufferFreq.position((int) freq_offset);

        ArrayList<Posting> postings = new ArrayList<>();

        if (compression) {
            byte[] doc_ids = new byte[doc_id_size];
            byte[] freqs = new byte[freq_size];

            bufferDocId.get(doc_ids, 0, doc_id_size);
            bufferFreq.get(freqs, 0, freq_size);

            List<Short> freqs_decompressed = UnaryCompressor.integerArrayDecompression(freqs, num_posting_of_block);
            List<Integer> doc_ids_decompressed = VariableByteCompressor.decode(doc_ids);


            for (int i = 0; i < num_posting_of_block; i++) {
                Posting posting = new Posting(doc_ids_decompressed.get(i), freqs_decompressed.get(i));
                postings.add(posting);
            }
        } else {
            for (int i = 0; i < num_posting_of_block; i++) {
                Posting posting = new Posting(bufferDocId.getInt(), bufferFreq.getShort());
                postings.add(posting);
            }
        }

        return postings;
    }

    @Override
    public String toString() {
        return "SkippingBlock{" +
                "doc_id_offset=" + doc_id_offset +
                ", doc_id_size=" + doc_id_size +
                ", freq_offset=" + freq_offset +
                ", freq_size=" + freq_size +
                ", doc_id_max=" + doc_id_max +
                ", num_posting_of_block=" + num_posting_of_block +
                ", file_offset=" + file_offset +
                '}';
    }
}
