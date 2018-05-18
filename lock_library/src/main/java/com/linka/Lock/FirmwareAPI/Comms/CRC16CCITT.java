package com.linka.Lock.FirmwareAPI.Comms;

/**
 * Created by Loren-Admin on 6/24/2015.
 */

import java.nio.charset.Charset;

/*************************************************************************
 *  Compilation:  javac CRC16CCITT.java
 *  Execution:    java CRC16CCITT s
 *  Dependencies:
 *
 *  Reads in a sequence of bytes and prints out its 16 bit
 *  Cylcic Redundancy Check (CRC-CCIIT 0xFFFF).
 *
 *  1 + x + x^5 + x^12 + x^16 is irreducible polynomial.
 *
 *  % java CRC16-CCITT 123456789
 *  CRC16-CCITT = 29b1
 *
 *************************************************************************/

public class CRC16CCITT {
    public static final int CRC_LENGTH_BYTES = 2;

    static int calculateCrc(String szData) {
        byte[] bytes = szData.getBytes();
        return calculateCrc(bytes);
    }

    static int calculateCrc(byte[] bytes) {
        return calculateCrc(bytes, bytes.length);
    }

    static int calculateCrc(byte[] bytes, int length) {
        int crc = 0xFFFF;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)

        // byte[] testBytes = "123456789".getBytes("ASCII");

        //byte[] bytes = args[0].getBytes();

        //for (byte b : bytes) {
        for (int j = 0; j < length; j++)
        {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((bytes[j]   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }

        crc &= 0xffff;
        //System.out.println("CRC16-CCITT = " + Integer.toHexString(crc));
        return crc;
    }


    /**
     * Test vector:
     * "123456789" -> 0x29B1
     */
    static String crc16_test ()
    {
        String szTestVector = "123456789";
        byte[] test_vector = szTestVector.getBytes(Charset.forName("UTF-8"));
        //int crcOut = calculate_crc(test_vector);    // Not working, gives 0xFEE8 for "123456789"
        int crcOut = calculateCrc(test_vector);
        return String.format ("CRC16-CCITT of %s is 0x%X.", szTestVector, crcOut);    // Gives 0xBB3D, correct for plain vanilla "CRC-16", see e.g. http://www.lammertbies.nl/comm/info/crc-calculation.html
    }

}
