package org.finos.waltz.common;

public interface Columns {
    int A = 0;
    int B = 1;
    int C = 2;
    int D = 3;
    int E = 4;
    int F = 5;
    int G = 6;
    int H = 7;
    int I = 8;
    int J = 9;
    int K = 10;
    int L = 11;
    int M = 12;
    int N = 13;
    int O = 14;
    int P = 15;
    int Q = 16;
    int R = 17;
    int S = 18;
    int T = 19;
    int U = 20;
    int V = 21;
    int W = 22;
    int X = 23;
    int Y = 24;
    int Z = 25;
    int AA = 26;
    int AB = 27;
    int AC = 28;
    int AD = 29;
    int AE = 30;
    int AF = 31;
    int AG = 32;
    int AH = 33;
    int AI = 34;
    int AJ = 35;
    int AK = 36;
    int AL = 37;
    int AM = 38;
    int AN = 39;
    int AO = 40;
    int AP = 41;
    int AQ = 42;
    int AR = 43;
    int AS = 44;
    int AT = 45;
    int AU = 46;
    int AV = 47;
    int AW = 48;
    int AX = 49;
    int AY = 50;
    int AZ = 51;
    int BA = 52;


    /**
     * Zero indexed offset of the given column identifier.
     *
     * Examples:
     * A: 0
     * B: 1
     * Z: 25
     * AA: 26
     * AB: 27
     * AZ: 51
     * BA: 52
     * BZ: 77
     * CA: 78
     * CZ: 103
     * JV: 281
     * @param col
     * @return
     */
    static int toOffset(String col) {
        String asUpper = col.trim().toUpperCase();

        int pos = asUpper.length() - 1;
        int acc = 0;

        for (char c : asUpper.toCharArray()) {
            int offset = c - 65;
            acc = acc + (pos == 0
                    ? offset
                    : (26 * (pos + offset)));
            pos--;
        }

        return acc;
    }
}
