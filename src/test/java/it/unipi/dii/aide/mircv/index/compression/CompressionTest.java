package it.unipi.dii.aide.mircv.index.compression;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompressionTest {


    @Test
    void VariableByteEncodeTest() {
        byte[] arr = new byte[]{6, (byte) 184, (byte) 133};
        byte[] arr2 = VariableByteCompressor.encode(List.of(824, 5));

        assertArrayEquals(arr, arr2);

        int[] input = {1, 127, 128, 255, 256, 16383, 16384};
        byte[][] expected = {
                {(byte) 0b00000001},
                {(byte) 0b01111111},
                {(byte) 0b00000001, (byte) 0b10000000},
                {(byte) 0b00000001, (byte) 0b11111111},
                {(byte) 0b00000010, (byte) 0b10000000},
                {(byte) 0b01111111, (byte) 0b11111111},
                {(byte) 0b00000001, (byte) 0b10000000, (byte) 0b10000000}
        };
        for (int i = 0; i < input.length; i++) {
            byte[] result = VariableByteCompressor.encode(Collections.singletonList(input[i]));
            assertArrayEquals(expected[i], result);
        }
    }

    @Test
    void VariableByteDecodeTest() {
        Integer[] arr = new Integer[]{824, 5};
        byte[] tbd = new byte[]{(byte) 6, (byte) 184, (byte) 133};
        List<Integer> arr2 = VariableByteCompressor.decode(tbd);

        assertArrayEquals(arr, arr2.toArray());

        byte[][] input = {
                {(byte) 0b00000001},
                {(byte) 0b01111111},
                {(byte) 0b00000001, (byte) 0b10000000},
                {(byte) 0b00000001, (byte) 0b11111111},
                {(byte) 0b00000010, (byte) 0b10000000},
                {(byte) 0b01111111, (byte) 0b11111111},
                {(byte) 0b00000001, (byte) 0b10000000, (byte) 0b10000000}
        };
        int[] expected = {1, 127, 128, 255, 256, 16383, 16384};

        for (int i = 0; i < input.length; i++) {
            System.out.println(Arrays.toString(input[i]));
            List<Integer> result = VariableByteCompressor.decode(input[i]);
            System.out.println(result);
            assertEquals(expected[i], result.get(i));
        }
    }
}
