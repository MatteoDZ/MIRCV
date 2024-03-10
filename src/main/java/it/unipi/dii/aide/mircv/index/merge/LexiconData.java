package it.unipi.dii.aide.mircv.index.merge;

public class LexiconData {

    private String term;
    private long offset_doc_id = 0;
    private int upperTF = 0;
    private int tf = 0;
    private float idf = 0;
    private float upperTFIDF = 0;

    public LexiconData(){

    }

    public String getTerm() {
        return term;
    }

    public long getOffset_doc_id() {
        return offset_doc_id;
    }

    public int getUpperTF() {
        return upperTF;
    }

    public int getTf() {
        return tf;
    }

    public float getIdf() {
        return idf;
    }

    public float getUpperTFIDF() {
        return upperTFIDF;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setOffset_doc_id(long offset_doc_id) {
        this.offset_doc_id = offset_doc_id;
    }

    public void setUpperTF(int upperTF) {
        this.upperTF = upperTF;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    public void setIdf(float idf) {
        this.idf = idf;
    }

    public void setUpperTFIDF(float upperTFIDF) {
        this.upperTFIDF = upperTFIDF;
    }
}
