package it.unipi.dii.aide.mircv.index.merge;

import java.util.List;

public class UtilsWriter {
    public static int calculateDimensionShort(List<Integer> blocco){
        return blocco.size()*2; //visto che scriveremo degli short
    }

    public static int calculateDimensionInt(List<Integer> block){
        return block.size()*4; //visto che scriveremo degli short
    }

    public static int calculateDimensionLong(List<Long> block){
        return block.size()*8; //visto che scriveremo degli short
    }


}
