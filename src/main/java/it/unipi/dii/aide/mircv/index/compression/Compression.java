package it.unipi.dii.aide.mircv.index.compression;

import java.util.List;

public interface Compression<B,I> {

    B[] encode(List<I> input);

    List<I>[] decode(B[] input);
}
