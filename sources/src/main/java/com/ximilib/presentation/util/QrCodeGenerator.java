package com.ximilib.presentation.util;

import java.util.Arrays;

public final class QrCodeGenerator {

    private QrCodeGenerator() {}

    public static boolean[][] encode(String text) {
        if (text == null) text = "";
        byte[] data = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        int version;
        int totalDataCodewords;
        int eccCodewordsPerBlock;

        if (data.length <= 14) {
            version = 1;
            totalDataCodewords = 16;
            eccCodewordsPerBlock = 10;
        } else if (data.length <= 26) {
            version = 2;
            totalDataCodewords = 28;
            eccCodewordsPerBlock = 16;
        } else if (data.length <= 42) {
            version = 3;
            totalDataCodewords = 44;
            eccCodewordsPerBlock = 26;
        } else {
            version = 4;
            totalDataCodewords = 64;
            eccCodewordsPerBlock = 18 * 2;
        }

        int size = 17 + version * 4;
        BitBuffer bb = new BitBuffer();

        bb.appendBits(0b0100, 4);

        bb.appendBits(data.length, 8);
        for (byte b : data) {
            bb.appendBits(b & 0xFF, 8);
        }

        int bitLimit = totalDataCodewords * 8;
        int termLen = Math.min(4, bitLimit - bb.bitLength);
        if (termLen > 0) bb.appendBits(0, termLen);

        if (bb.bitLength % 8 != 0) {
            bb.appendBits(0, 8 - (bb.bitLength % 8));
        }

        int pad = 0;
        while (bb.bitLength < bitLimit) {
            bb.appendBits((pad % 2 == 0) ? 0xEC : 0x11, 8);
            pad++;
        }

        byte[] dataBytes = bb.getBytes();
        byte[] finalCodewords = addErrorCorrection(dataBytes, version, totalDataCodewords, eccCodewordsPerBlock);

        int[][] matrix = new int[size][size];
        boolean[][] isFunction = new boolean[size][size];

        placeFinder(matrix, isFunction, 0, 0);
        placeFinder(matrix, isFunction, size - 7, 0);
        placeFinder(matrix, isFunction, 0, size - 7);

        if (version >= 2) {
            int alignPos = size - 7;
            placeAlignment(matrix, isFunction, alignPos, alignPos);
        }

        for (int i = 8; i < size - 8; i++) {
            int color = (i % 2 == 0) ? 2 : 1;
            if (!isFunction[6][i]) {
                matrix[6][i] = color;
                isFunction[6][i] = true;
            }
            if (!isFunction[i][6]) {
                matrix[i][6] = color;
                isFunction[i][6] = true;
            }
        }

        matrix[4 * version + 9][8] = 2;
        isFunction[4 * version + 9][8] = true;

        reserveFormatAreas(isFunction, size);

        int bitIdx = 0;
        int totalBits = finalCodewords.length * 8;
        for (int right = size - 1; right > 0; right -= 2) {
            if (right == 6) right--;
            for (int vert = 0; vert < size; vert++) {
                for (int j = 0; j < 2; j++) {
                    int x = right - j;
                    boolean upward = ((right + 1) & 2) == 0;
                    int y = upward ? size - 1 - vert : vert;
                    if (!isFunction[y][x]) {
                        int bit = 0;
                        if (bitIdx < totalBits) {
                            bit = (finalCodewords[bitIdx / 8] >>> (7 - (bitIdx % 8))) & 1;
                            bitIdx++;
                        }

                        boolean mask = ((x + y) % 2 == 0);
                        boolean bitVal = (bit == 1) ^ mask;
                        matrix[y][x] = bitVal ? 2 : 1;
                    }
                }
            }
        }

        int formatBits = 0x085B;
        applyFormatBits(matrix, size, formatBits);

        boolean[][] result = new boolean[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                result[r][c] = (matrix[r][c] == 2);
            }
        }
        return result;
    }

    private static void placeFinder(int[][] matrix, boolean[][] isFunc, int x, int y) {
        for (int r = -1; r <= 7; r++) {
            for (int c = -1; c <= 7; c++) {
                int mr = y + r;
                int mc = x + c;
                if (mr >= 0 && mr < matrix.length && mc >= 0 && mc < matrix.length) {
                    isFunc[mr][mc] = true;
                    if (r >= 0 && r <= 6 && c >= 0 && c <= 6) {
                        boolean black = (r == 0 || r == 6 || c == 0 || c == 6 || (r >= 2 && r <= 4 && c >= 2 && c <= 4));
                        matrix[mr][mc] = black ? 2 : 1;
                    } else {
                        matrix[mr][mc] = 1;
                    }
                }
            }
        }
    }

    private static void placeAlignment(int[][] matrix, boolean[][] isFunc, int x, int y) {
        for (int r = -2; r <= 2; r++) {
            for (int c = -2; c <= 2; c++) {
                int mr = y + r;
                int mc = x + c;
                isFunc[mr][mc] = true;
                boolean black = (Math.abs(r) == 2 || Math.abs(c) == 2 || (r == 0 && c == 0));
                matrix[mr][mc] = black ? 2 : 1;
            }
        }
    }

    private static void reserveFormatAreas(boolean[][] isFunc, int size) {
        for (int i = 0; i < 9; i++) {
            isFunc[8][i] = true;
            isFunc[i][8] = true;
        }
        for (int i = size - 8; i < size; i++) {
            isFunc[8][i] = true;
            isFunc[i][8] = true;
        }
    }

    private static void applyFormatBits(int[][] matrix, int size, int bits) {

        for (int i = 0; i <= 5; i++) {
            matrix[8][i] = ((bits >>> i) & 1) == 1 ? 2 : 1;
        }
        matrix[8][7] = ((bits >>> 6) & 1) == 1 ? 2 : 1;
        matrix[8][8] = ((bits >>> 7) & 1) == 1 ? 2 : 1;
        matrix[7][8] = ((bits >>> 8) & 1) == 1 ? 2 : 1;
        for (int i = 9; i < 15; i++) {
            matrix[14 - i][8] = ((bits >>> i) & 1) == 1 ? 2 : 1;
        }

        for (int i = 0; i < 7; i++) {
            matrix[size - 1 - i][8] = ((bits >>> i) & 1) == 1 ? 2 : 1;
        }
        for (int i = 7; i < 15; i++) {
            matrix[8][size - 15 + i] = ((bits >>> i) & 1) == 1 ? 2 : 1;
        }
    }

    private static byte[] addErrorCorrection(byte[] data, int version, int dataLen, int eccLen) {
        if (version <= 3) {
            byte[] ecc = reedSolomonCompute(data, eccLen);
            byte[] result = new byte[data.length + ecc.length];
            System.arraycopy(data, 0, result, 0, data.length);
            System.arraycopy(ecc, 0, result, data.length, ecc.length);
            return result;
        }

        int block1DataLen = 32;
        int block2DataLen = 32;
        int eccPerBlock = 18;

        byte[] b1Data = Arrays.copyOfRange(data, 0, block1DataLen);
        byte[] b2Data = Arrays.copyOfRange(data, block1DataLen, block1DataLen + block2DataLen);

        byte[] b1Ecc = reedSolomonCompute(b1Data, eccPerBlock);
        byte[] b2Ecc = reedSolomonCompute(b2Data, eccPerBlock);

        byte[] result = new byte[64 + 36];
        int idx = 0;
        for (int i = 0; i < 32; i++) {
            result[idx++] = b1Data[i];
            result[idx++] = b2Data[i];
        }
        for (int i = 0; i < eccPerBlock; i++) {
            result[idx++] = b1Ecc[i];
            result[idx++] = b2Ecc[i];
        }
        return result;
    }

    private static byte[] reedSolomonCompute(byte[] data, int eccLen) {
        int[] generator = reedSolomonGenerator(eccLen);
        int[] msg = new int[data.length + eccLen];
        for (int i = 0; i < data.length; i++) {
            msg[i] = data[i] & 0xFF;
        }

        for (int i = 0; i < data.length; i++) {
            int coef = msg[i];
            if (coef != 0) {
                for (int j = 0; j < generator.length; j++) {
                    msg[i + j] ^= gfMultiply(generator[j], coef);
                }
            }
        }

        byte[] ecc = new byte[eccLen];
        for (int i = 0; i < eccLen; i++) {
            ecc[i] = (byte) msg[data.length + i];
        }
        return ecc;
    }

    private static int[] reedSolomonGenerator(int degree) {
        int[] poly = {1};
        for (int i = 0; i < degree; i++) {
            int root = gfExp(i);
            int[] next = new int[poly.length + 1];
            for (int j = 0; j < poly.length; j++) {
                next[j] ^= gfMultiply(poly[j], root);
                next[j + 1] ^= poly[j];
            }
            poly = next;
        }
        return poly;
    }

    private static final int[] GF_EXP = new int[512];
    private static final int[] GF_LOG = new int[256];

    static {
        int val = 1;
        for (int i = 0; i < 255; i++) {
            GF_EXP[i] = val;
            GF_EXP[i + 255] = val;
            GF_LOG[val] = i;
            val = (val << 1);
            if ((val & 0x100) != 0) val ^= 0x11D;
        }
    }

    private static int gfExp(int pow) {
        return GF_EXP[pow % 255];
    }

    private static int gfMultiply(int x, int y) {
        if (x == 0 || y == 0) return 0;
        return GF_EXP[GF_LOG[x] + GF_LOG[y]];
    }

    private static class BitBuffer {
        int[] buffer = new int[128];
        int bitLength = 0;

        void appendBits(int value, int numBits) {
            for (int i = numBits - 1; i >= 0; i--) {
                int bit = (value >>> i) & 1;
                int byteIdx = bitLength / 8;
                buffer[byteIdx] |= (bit << (7 - (bitLength % 8)));
                bitLength++;
            }
        }

        byte[] getBytes() {
            int numBytes = (bitLength + 7) / 8;
            byte[] res = new byte[numBytes];
            for (int i = 0; i < numBytes; i++) {
                res[i] = (byte) buffer[i];
            }
            return res;
        }
    }
}
