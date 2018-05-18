package com.linka.Lock.Utility;

/**
 * Created by Loren-Admin on 8/27/2015.
 */
public class DebugHelper {

    public static String dumpByteArray (byte[] data)
    {
        StringBuilder builder = new StringBuilder();

        if (data == null)
        {
            builder.append("[null]");
        }
        else
        {
            for (byte b : data)
            {
                builder.append(String.format("0x%02X, ", b));
            }
        }
        return builder.toString();

    }


    /**
     * Get byte representation from hex-formatted MAC address string
     * like aa:bb:cc:dd:ee:ff
     * @param szMAC
     * @return byte array parsed from string
     */
    public static byte[] getMACFromString(String szMAC) {
        byte[] MAC = new byte[6];

        int spacing;
        if (szMAC.substring(2, 3).equalsIgnoreCase(":")) {
            spacing = 3;    // For format of aa:bb:cc:22:33:44
        }
        else {
            // for format like aabbcc112233
            spacing = 2;
        }

        for(int i = 0; i < 6; i++) {
            MAC[i] = (byte)Integer.parseInt(szMAC.substring(spacing*i, (spacing*i)+2), 16);
        }

        return MAC;
    }


}
