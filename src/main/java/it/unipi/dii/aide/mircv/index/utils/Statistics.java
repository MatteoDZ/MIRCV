package it.unipi.dii.aide.mircv.index.utils;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Statistics {

    private int numdocs = 0;
    private double avg_doc_length = 0.0;
    private List<Integer> docs_length;
    private static MappedByteBuffer mbb;
    private static FileChannel fc = null;


    public Statistics() {
        try {
            // Open file channel for reading and writing
            fc = FileChannel.open(Paths.get("data/statistics"),
                    StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (
                IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + "data/statistics" + " file.");
        }
    }

    public int getNumdocs() {
        return numdocs;
    }

    public double getAvg_doc_length() {
        return avg_doc_length;
    }

    public List<Integer> getDocs_length() {
        return docs_length;
    }

    public void setNumdocs(int numdocs) {
        this.numdocs = numdocs;
    }

    public void setAvg_doc_length(double avg_doc_length) {
        this.avg_doc_length = avg_doc_length;
    }

    public void setDocs_length(List<Integer> docs_length) {
        this.docs_length = docs_length;
    }

    public void writeToDisk() throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, 4L + 4L + (4L * this.numdocs));
        mbb.putInt(this.numdocs);
        mbb.putDouble(this.avg_doc_length);
        for (Integer i : this.docs_length){
            mbb.putInt(i);
        }
    }

    public static Statistics read() throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        Statistics s = new Statistics();
        s.setNumdocs(mbb.getInt());
        s.setAvg_doc_length(mbb.getDouble());
        List<Integer> lst = new ArrayList<>();
        for (int i = 0; i < s.numdocs; i++){
            lst.add(mbb.getInt());
        }
        s.setDocs_length(lst);
        return s;
    }
}
