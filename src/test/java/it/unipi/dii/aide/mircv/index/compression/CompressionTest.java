package it.unipi.dii.aide.mircv.index.compression;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompressionTest {


    @Test
    public void testConvertToUnary() {
        int[] input = {1, 5, 10, 15, 20};
        byte[] expected = {(byte) 0b01111011, (byte) 0b11111110, (byte) 0b11111111,
                (byte) 0b11111101, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11000000};
        byte[] result = UnaryCompressor.integerArrayCompression(input);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testConvertFromUnary() {
        byte[] input = {(byte) 0b01111011, (byte) 0b11111110, (byte) 0b11111111,
                (byte) 0b11111101, (byte) 0b11111111, (byte) 0b11111111, (byte) 0b11111110};
        assertEquals(List.of((short)1, (short)5, (short)10, (short)15, (short)25)
                , UnaryCompressor.integerArrayDecompression(input, 5));
    }


    @Test
    void VariableByteEncodeTest() {

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
        byte[] expectedA = {
                (byte) 0b00000001,
                (byte) 0b01111111,
                (byte) 0b00000001, (byte) 0b10000000,
                (byte) 0b00000001, (byte) 0b11111111,
                (byte) 0b00000010, (byte) 0b10000000,
                (byte) 0b01111111, (byte) 0b11111111,
                (byte) 0b00000001, (byte) 0b10000000, (byte) 0b10000000
        };

        assertArrayEquals(expectedA,
                VariableByteCompressor.encode(List.of(1, 127, 128, 255, 256, 16383, 16384)));

        for (int i = 0; i < input.length; i++) {
            byte[] result = VariableByteCompressor.encode(Collections.singletonList(input[i]));
            assertArrayEquals(expected[i], result);
        }

    }

    @Test
    void VariableByteDecodeTest() {
        byte[] inputA = {
                (byte) 0b00000001,
                (byte) 0b01111111,
                (byte) 0b00000001, (byte) 0b10000000,
                (byte) 0b00000001, (byte) 0b11111111,
                (byte) 0b00000010, (byte) 0b10000000,
                (byte) 0b01111111, (byte) 0b11111111,
                (byte) 0b00000001, (byte) 0b10000000, (byte) 0b10000000
        };
        assertEquals(List.of(1, 127, 128, 255, 256, 16383, 16384),
                VariableByteCompressor.decode(inputA));
    }




    @Test
    //test the variable byte compressor
    public void testVariableByteCompressor(){
        //test compressInt
        byte[] expected = {5, 2, -72, 4, -125, -48}; //expected outputs
        assertArrayEquals(expected, VariableByteCompressor.encode(List.of(5, 312, 66000)));

        //test compressInt
        byte[] expected2 = {5, 2, -72, 4, -125, -48, 1, -127, 2, -128, -126}; //expected outputs
        assertArrayEquals(expected2, VariableByteCompressor.encode(List.of(5, 312, 66000, 129, 32770)));

        //test compressArrayInt
        assertEquals(List.of(5, 312, 66000, 129, 32770),
                VariableByteCompressor.decode(new byte[]{5, 2, -72, 4, -125, -48, 1, -127, 2, -128, -126}));
    }


    @Test
    public void testCoherence(){
        assertEquals(List.of(5, 312, 66000),
                VariableByteCompressor.decode(VariableByteCompressor.encode(List.of(5, 312, 66000))));
        assertEquals(List.of(1, 127, 128, 255, 256, 16383, 16384),
                VariableByteCompressor.decode(VariableByteCompressor.encode(List.of(1, 127, 128, 255, 256, 16383, 16384))));

        assertEquals(List.of((short)5, (short)312, (short)32000),
                UnaryCompressor.integerArrayDecompression(UnaryCompressor.integerArrayCompression(new int[]{5, 312, 32000}), 3));
        assertEquals(List.of((short)1, (short)127, (short)128, (short)255, (short)256, (short)16383, (short)16384),
                UnaryCompressor.integerArrayDecompression(UnaryCompressor.integerArrayCompression(new int[]{1, 127, 128, 255, 256, 16383, 16384}), 7));

    }



}
