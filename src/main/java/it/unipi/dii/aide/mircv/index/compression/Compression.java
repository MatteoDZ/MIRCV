package it.unipi.dii.aide.mircv.index.compression;

import java.util.List;

public interface Compression {

    byte[] encode(List<Integer> input);

    List<Integer>[] decode(byte[] input);
}
