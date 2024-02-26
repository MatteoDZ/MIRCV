package it.unipi.dii.aide.mircv.index.merge;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static it.unipi.dii.aide.mircv.index.merge.UtilsWriter.calculateDimensionInt;

public class VocabularyWriter { //oggetto che gestisce la scrittura sul vocabulary.
    private final FileChannel fc;
    private MappedByteBuffer mbb;

    private final Long ENTRY_SIZE = 54L; // 2L * 15 + 8 + 4 + 4 + 2*4
    VocabularyWriter(String path){
        try {
            fc = FileChannel.open(Paths.get(path), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        }catch (IOException e) {
            throw new RuntimeException("An error occurred while writing to the " + path + " file.");
        }
    }
    public void write(String term,long invertedOffset,int collectionFrequency,int termFrequency) throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), ENTRY_SIZE);
        for(int i=0;i<term.length();i++){
            mbb.putChar(term.charAt(i));
        }
        mbb.putShort((short) -1);
        mbb.putLong(invertedOffset);
        mbb.putShort((short) -1);
        mbb.putInt(collectionFrequency);
        mbb.putShort((short) -1);
        mbb.putInt(termFrequency);
        mbb.putShort((short) -1);
    }

    public void read() throws IOException {
        mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        StringBuilder term = new StringBuilder();
        char a;
        while((a = mbb.getChar()) !=-1){
            term.append(a);
        }
    }

    /*
    termine | puntatore al inv index | statiche varie ...
     */
}
