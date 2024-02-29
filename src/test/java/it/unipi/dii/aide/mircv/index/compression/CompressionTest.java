package it.unipi.dii.aide.mircv.index.compression;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompressionTest {


    @Test
    void VariableByteEncodeTest() {
        byte[] arr = new byte[]{6, (byte) 184, (byte) 133};
        byte[] arr2 = VariableByteCompressor.encode(List.of(824, 5));

        for (int i = 0; i < arr.length; i++) {
            assertEquals(arr[i], arr2[i]);
        }
    }

    @Test
    void VariableByteDecodeTest() {
        Integer[] arr = new Integer[]{824, 5};
        byte[] tbd = new byte[]{(byte) 6, (byte) 184, (byte) 133};
        List<Integer> arr2 = VariableByteCompressor.decode(tbd);

        for (int i = 0; i < arr.length; i++) {
            assertEquals(arr[i], arr2.get(i));
        }
    }
}
