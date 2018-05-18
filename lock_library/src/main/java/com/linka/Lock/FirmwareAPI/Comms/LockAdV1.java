package com.linka.Lock.FirmwareAPI.Comms;


import com.linka.Lock.FirmwareAPI.Types.AuthState;
import com.linka.Lock.FirmwareAPI.Types.LockState;
import com.linka.Lock.FirmwareAPI.Types.SystemFlags;
import com.linka.Lock.Utility.DebugHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by Loren-Admin on 4/13/2015.
 */
public class LockAdV1 {

    public final static byte LINKA_AD_VER_1 = (byte)0xC1;
    public final static byte LINKA_AD_VER_2 = (byte)0xC2;
    public final int m_nAdPacketVersion;
    private final double m_batt_V;
    private final LockState m_nLockState;
    // private final int m_nLockMode;
    private final int m_nLockFlags;
    private final short m_nAccel_X;
    private final short m_nAccel_Y;
    private final short m_nAccel_Z;
    private final double mfAccelX;
    private final double mfAccelY;
    private final double mfAccelZ;
    //  private final short m_nZdiff;
    //  private final double m_fZdiff;
    private final short m_nTemperature;
    private final double m_fTemperature;
    private final AuthState m_nAuthState;
    private final int m_ulStatus;
    private final short m_usCRC;

    private final byte[] m_Data;
    private byte[] m_MACAddr;
    private String m_MACAddr_str = "";

    //
    // For 16-bit full resolution accelerometer data:
    public final static double ACCEL_RESOLUTION_G_16BIT = (0.063 / 1000);
    public final static double ACCEL_RESOLUTION_G_8BIT = (0.063 / 1000) * 256;

    public final static double TEMP_RESOLUTION_C = 0.5; // NRF reports in 0.25C but we convert to 0.5 to fit in 8 bit byte for advertising data brevity





    // Lock Mode

    // Lock Flags:
//    public final static byte FLAG_STALL = 0x01;
//    public final static byte FLAG_ALARM_BUMP = 0x02;
//    public final static byte FLAG_ALARM_TEMP = 0x04;
//    public final static byte FLAG_ALARM_ALERT = 0x08;
//    public final static byte FLAG_ALARM_TIP = 0x10;
//    public final static byte FLAG_CHARGING = (byte)0x20;
//    public final static byte FLAG_CHARGED = (byte)0x40;
    public final static long VLS_FLAG_ALARM_TIP = (1L<<9);


    // Test status flags, from either adv or status packet
    public final static long  VLT_TEST_EEPROM	=		(1L<<0);
    public final static long  VLT_TEST_ACCEL	=		(1L<<1);
    public final static long  VLT_TEST_MOTOR_DRIVER =	(1L<<2);
    public final static long  VLT_TEST_I2CBUSS	=	(1L<<3);
    public final static long  VLT_TEST_RADIO	=		(1L<<4);
    public final static long  VLT_TEST_ENCRYP	=		(1L<<5);
    public final static long  VLT_TEST_CRC		=	(1L<<6);
    public final static long  VLT_TEST_BOOTLOADER_PRESENT =	(1L<<7);

    public int GetStatusFlags () { return m_nLockFlags; }
    public int GetSelfTestStatus () { return m_ulStatus; }
    public LockState GetLockState() { return m_nLockState; }
    public AuthState GetAuthState() { return m_nAuthState; }
    public double GetBatteryVoltage() { return m_batt_V; }
    public int GetAccelX() { return m_nAccel_X; }
    public int GetAccelY() { return m_nAccel_Y; }
    public int GetAccelZ() { return m_nAccel_Z; }
    public double GetAccelX_g() { return mfAccelX; }
    public double GetAccelY_g() { return mfAccelY; }
    public double GetAccelZ_g() { return mfAccelZ; }
    public String GetMACAddr_str() { return m_MACAddr_str; }
    public double getM_fTemperature() { return m_fTemperature; }

    public LockAdV1 (byte[] data, int offset)
    {
        // Parse the lock data structure
            /*
            Format of the v1 lock adv. data is:
            [2] Version - 0x01
            [3] Special - 0xC0
            [4-5] - battery voltage, mV
            [6] - Lock status
            [7] - Lock flags
            [8] - Lock mode
            [9-10] - accel_x
            [11-12] - accel_y
            [13-14] - accel_x
            [15] - csum

typedef struct __attribute__((packed)) {
	uint8_t		ucVersion;		/// 0xC1 for this version
	uint8_t		ucCounter;
	uint8_t		ucBatt_dV;		/// Battery voltage in deci-Volts (range 0-25.5V, 1 bit = 100mV)
	LOCK_STATE ucLockState;	//
	uint32_t  ulLockFlags;
	AUTH_STATE ucAuthState;
	uint8_t		ucAccel_X;
	uint8_t		ucAccel_Y;
	uint8_t		ucAccel_Z;		// 9b
	uint8_t		ucTemp_C;		// 10b
	uint32_t	ulStatus;		// 11,12,13,14, test status	14b
	uint16_t	usCRC;          // 15, 16
} VLSO_ADV_DATA_PKT;

             */
        // Make a copy of the data
        m_Data = data;

        offset += 2;    // Skip the 0xFF 0xFF

        if (m_Data[offset] == LockAdV1.LINKA_AD_VER_1) {
            m_nAdPacketVersion = LockAdV1.LINKA_AD_VER_1;
            m_batt_V = (m_Data[2 + offset]) / 10.0;  // Battery in decivolts
            m_nLockState = new LockState(m_Data[3 + offset]);
            m_nLockFlags = getLongFromBytes(m_Data, 4 + offset); //(int)(m_Data[5+offset] + (m_Data[4+offset]<<8));
            m_nAuthState = new AuthState(m_Data[8 + offset]);
            //m_nLockMode = m_Data[4+offset];
            // Accelerometer data is big endian
            m_nAccel_X = (short) (m_Data[9 + offset]);
            m_nAccel_Y = (short) (m_Data[10 + offset]);
            m_nAccel_Z = (short) (m_Data[11 + offset]);

            mfAccelX = m_nAccel_X * ACCEL_RESOLUTION_G_8BIT;
            mfAccelY = m_nAccel_Y * ACCEL_RESOLUTION_G_8BIT;
            mfAccelZ = m_nAccel_Z * ACCEL_RESOLUTION_G_8BIT;
            //m_nTemperature = (short)(m_Data[15] + (m_Data[16]<<8));
            if (data.length - offset > 14) {
                m_nTemperature = m_Data[12 + offset];
                m_fTemperature = m_nTemperature * TEMP_RESOLUTION_C;
                m_ulStatus = getLongFromBytes(m_Data, 13 + offset);
                //m_usCRC = getShortFromBytes (m_Data[18+offset], m_Data[17+offset]);
                m_usCRC = 0x00;
            } else {
                m_nTemperature = 0;
                m_fTemperature = 0;
                m_ulStatus = 0;
                m_usCRC = 0x00;
            }
        } else if (m_Data[offset] == LockAdV1.LINKA_AD_VER_2) {

            m_nAdPacketVersion = LockAdV1.LINKA_AD_VER_2;
            m_batt_V = (m_Data[1 + offset]) / 10.0;  // Battery in decivolts
            m_nLockState = new LockState(m_Data[2 + offset]);
            m_nLockFlags = getLongFromBytes(m_Data, 3 + offset); //(int)(m_Data[5+offset] + (m_Data[4+offset]<<8));
            m_nAuthState = new AuthState(m_Data[7 + offset]);
            //m_nLockMode = m_Data[4+offset];
            // Accelerometer data is big endian
            m_nAccel_X = (short) (m_Data[8 + offset]);
            m_nAccel_Y = (short) (m_Data[9 + offset]);
            m_nAccel_Z = (short) (m_Data[10 + offset]);

            mfAccelX = m_nAccel_X * ACCEL_RESOLUTION_G_8BIT;
            mfAccelY = m_nAccel_Y * ACCEL_RESOLUTION_G_8BIT;
            mfAccelZ = m_nAccel_Z * ACCEL_RESOLUTION_G_8BIT;
            //m_nTemperature = (short)(m_Data[15] + (m_Data[16]<<8));
            m_nTemperature = 0; // Unsupported in v2 packet



            if (data.length - offset > 14) {
                m_fTemperature = m_nTemperature * TEMP_RESOLUTION_C;
                m_ulStatus = getLongFromBytes(m_Data, 11 + offset);     // Should be 12?
                //m_usCRC = getShortFromBytes (m_Data[18+offset], m_Data[17+offset]);
                m_MACAddr = new byte[6];
                System.arraycopy(m_Data, offset + 15, m_MACAddr, 0, 6);
                // TODO: Advertisement Data seems to be sending back a different MAC than the main MAC
                // Causing all sorts of havoc on our DB, because we use the MAC as a unique key for everything
                m_MACAddr_str = String.format("%s:%s:%s:%s:%s:%s",
                        String.format("%02X", m_MACAddr[5]),
                        String.format("%02X", m_MACAddr[4]),
                        String.format("%02X", m_MACAddr[3]),
                        String.format("%02X", m_MACAddr[2]),
                        String.format("%02X", m_MACAddr[1]),
                        String.format("%02X", m_MACAddr[0])
                        );

                //m_MACAddr = Arrays.copyOf(m_Data, )
                m_usCRC = getShortFromBytesBigEndian(m_Data, offset + 21);
            } else {
                m_fTemperature = 0;
                m_ulStatus = 0;
                m_usCRC = 0x00;
            }
        } else {
            // Unknown version
            m_nAdPacketVersion = 0;
            m_batt_V = 0;
            m_nLockState = new LockState((byte)0);
            m_nLockFlags = 0;
            m_nAuthState = new AuthState((byte)0);
            //m_nLockMode = m_Data[4+offset];
            // Accelerometer data is big endian
            m_nAccel_X = 0;
            m_nAccel_Y = 0;
            m_nAccel_Z = 0;

            mfAccelX = 0;
            mfAccelY = 0;
            mfAccelZ = 0;
            //m_nTemperature = (short)(m_Data[15] + (m_Data[16]<<8));
            m_nTemperature = 0;
            m_fTemperature = 0;
            m_ulStatus = 0;
            m_usCRC = 0x00;
        }

        /*
        if (data.length > (14+offset))
        {
            // Opposite endianness
            m_nZdiff = (short)(m_Data[13+offset] + (m_Data[14+offset]<<8));
            m_fZdiff = m_nZdiff * ACCEL_RESOLUTION_G;
        }
        else
        {
            m_nZdiff = 0;
            m_fZdiff = 0.0;
        }
        */
    }

    // \ todo move these elsewhere to common util parser class

    public static int getLongFromBytes(byte[] mData, int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(mData[i]);
        bb.put(mData[i+1]);
        bb.put(mData[i+2]);
        bb.put(mData[i+3]);
        int ulVal = bb.getInt(0);
        return ulVal;
    }

    public static short getShortFromBytesLittleEndian (byte[] data, int offset)
    {
        return getShortFromBytes(data, offset, ByteOrder.LITTLE_ENDIAN);
    }

    public static short getShortFromBytesBigEndian (byte[] data, int offset) {
        return getShortFromBytes(data, offset, ByteOrder.BIG_ENDIAN);
    }

    private static short getShortFromBytes (byte[] data, int offset, java.nio.ByteOrder endian)
    {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(endian);
        bb.put(data[offset]);
        bb.put(data[offset+1]);
        short shortVal = bb.getShort(0);
        return shortVal;
    }



    private String testFailuresToString()
    {
        StringBuilder builder = new StringBuilder();
        if (m_ulStatus == 0)
        {
            builder.append("PASS.");
        }
        else {
            builder.append(String.format("Fail: (0x%X):", m_ulStatus));
            builder.append(m_ulStatus);
            if ((m_ulStatus & VLT_TEST_ACCEL) != 0)
                builder.append("VLT_TEST_ACCEL");

            if ((m_ulStatus & VLT_TEST_CRC) != 0)
                builder.append("VLT_TEST_CRC");

            if ((m_ulStatus & VLT_TEST_EEPROM) != 0)
                builder.append("VLT_TEST_EEPROM");

            if ((m_ulStatus & VLT_TEST_ENCRYP) != 0)
                builder.append("VLT_TEST_ENCRYP");

            if ((m_ulStatus & VLT_TEST_I2CBUSS) != 0)
                builder.append("VLT_TEST_I2CBUSS");

            if ((m_ulStatus & VLT_TEST_MOTOR_DRIVER) != 0)
                builder.append("VLT_TEST_MOTORDRV");

            if ((m_ulStatus & VLT_TEST_RADIO) != 0)
                builder.append("VLT_TEST_RADIO");

            if ((m_ulStatus & VLT_TEST_BOOTLOADER_PRESENT) != 0)
                builder.append("VLT_TEST_BOOTLOADER_PRESENT");
        }
        return builder.toString();
    }



    public String toString ()
    {
        StringBuilder builder = new StringBuilder();

        //return String.format ("%.2fV, Status 0x%02X, Flags 0x%02X, Mode 0x%02X, Accel %d,%d,%d", m_batt_V, m_nLockState, m_nLockFlags, m_nLockMode, m_nAccel_X, m_nAccel_Y, m_nAccel_Z);
        builder.append(String.format ("Batt %.2fV Temp %.2fC\r\nStatus:%s (0x%02x) \r\nFlags:%s (0x%02x)", m_batt_V, m_fTemperature, m_nLockState.toString(), m_nLockState.GetValue(), SystemFlags.flagsToString(m_nLockFlags), m_nLockFlags));
        builder.append(String.format ("\r\nAccel %.3fG(x, 0x%X) %.3fG(y, 0x%X), %.3fG(z, 0x%X)\r\n", mfAccelX, m_nAccel_X, mfAccelY, m_nAccel_Y, mfAccelZ, m_nAccel_Z));
        builder.append(String.format ("\r\nAuthState 0x%x, Status 0x%X, CRC 0x%X", m_nAuthState.GetValue(), m_ulStatus, m_usCRC));
        if (m_nAdPacketVersion == LINKA_AD_VER_2)
        {
            builder.append ("V2 packet, MAC ");
            builder.append (DebugHelper.dumpByteArray(m_MACAddr));
        }
        builder.append ("\r\n" + testFailuresToString());
        //   builder.append(String.format ("\r\nAccel delta Z %.3fG (0x%x)", m_fZdiff, m_nZdiff));
        builder.append (String.format ("\r\nLength %d, ", m_Data.length));
        byte[] ourData = Arrays.copyOfRange(m_Data, 2, m_Data.length);
        short crc = (short) CRC16CCITT.calculateCrc(ourData, ourData.length - CRC16CCITT.CRC_LENGTH_BYTES);
        if (crc == m_usCRC)
        {
            builder.append("CRC match.\r\n");
        }
        else
        {
            builder.append (String.format("CRC failure (0x%X calc, 0x%X data.)\r\n", crc, m_usCRC));
            builder.append ("Packet for CRC calc:" + DebugHelper.dumpByteArray(ourData));
        }
        builder.append ("Raw: ");
        builder.append (bytesToHex(m_Data));
        return builder.toString();
    }

    final protected char[] hexArray = "0123456789ABCDEF".toCharArray();
    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }
}
