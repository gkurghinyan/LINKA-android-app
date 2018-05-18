package com.linka.Lock.FirmwareAPI.Comms;

import android.util.Log;

import com.linka.Lock.FirmwareAPI.Types.LockState;

/**
 * Created by Loren-Admin on 2/28/2016.
 *
 * Information packet containing accelerometer data and other info
 *
 *    uint8_t length;       // 1
 *    uint8_t cmd;
 *    uint8_t state;  		// Lock state
 *    uint8_t cRSSI;   // 4
 *    uint32_t ulUptime_s;	// Uptime, seconds
 *    uint16_t accel_x;     // 10, offset is 8
 *    uint16_t accel_y;
 *    uint16_t accel_z;       // 14b at this point
 *    uint16_t usActivityFlags;	// 16b
 *    uint8_t ucBootloaderVersion;
 *    uint8_t ucReserved (temperature, C)
 *    uint16_t CRC;           // 18b
 *
 */
public class LockInfoPacket extends LockDataPacket {
    private final static String TAG = LockInfoPacket.class.getSimpleName();
    private LockState mLockState;            // Current lock state
    private int m_RSSI;
    private int mUptime_s;
    private int m_nAccel_x, m_nAccel_y, m_nAccel_z;
    private double mAccel_x;
    private double mAccel_y;
    private double mAccel_z;
    private int mActivityFlags;
    private int mBootloaderVersion = 0xFF;
    private int mSpare = 0;
    int m_usCRC;
    public static final int MIN_INFO_PKT_LEN = 16; // Len, cmd, ...

    public LockState GetLockState() { return mLockState; }
    public double GetAccelX() { return mAccel_x; }
    public double GetAccelY() { return mAccel_y; }
    public double GetAccelZ() { return mAccel_z; }
    public int GetRSSI() { return m_RSSI; }

    public LockInfoPacket(byte[] data)
    {
        super(data);    // Base class will determine packet type, etc

        if (getCmdType().GetValue() != LockCommand.VCMD_INFO)
        {
            // Error, this is not a status packet
            Log.e(TAG, "Not an info packet.");
            m_bValid = false;
        }
        else if (m_PacketData.length < MIN_INFO_PKT_LEN)
        {
            Log.e(TAG, "Bad status packet length.");
            m_bValid = false;
        }
        else
        {
            // This is a status packet. Parse the fields
//            mLockState = m_PayloadData[0]; // Header is not included here. First payload byte is the state
            int length = m_PacketData[0];
            mLockState = new LockState(m_PacketData[2]);
            m_RSSI = m_PacketData[3];
            mUptime_s = bytesToULONG(m_PacketData[4], m_PacketData[5], m_PacketData[6], m_PacketData[7]);
            // Accelrometer data is big endian but we map to the nRF's LE in firmware, since everything else is little endian
            m_nAccel_x = LockAdV1.getShortFromBytesLittleEndian(m_PacketData, 8);
            m_nAccel_y = LockAdV1.getShortFromBytesLittleEndian(m_PacketData, 10);
            m_nAccel_z = LockAdV1.getShortFromBytesLittleEndian(m_PacketData, 12);
            mAccel_x = m_nAccel_x * LockAdV1.ACCEL_RESOLUTION_G_16BIT;
            mAccel_y = m_nAccel_y * LockAdV1.ACCEL_RESOLUTION_G_16BIT;
            mAccel_z = m_nAccel_z * LockAdV1.ACCEL_RESOLUTION_G_16BIT;
            mActivityFlags = LockAdV1.getShortFromBytesLittleEndian(m_PacketData, 14);
            if (length > 18)    // Added BL version and reserved
            {
                mBootloaderVersion = m_PacketData[16];
                mSpare = m_PacketData[17];
            }
            m_usCRC = LockAdV1.getShortFromBytesLittleEndian(m_PacketData, length-2);
        }
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("Info packet, uptime %d, rssi %d ", mUptime_s, m_RSSI));
        builder.append(mLockState.toString());
        builder.append(String.format(" %.3fG X, %.3fG Y, %.3fG Z ", mAccel_x, mAccel_y, mAccel_z));
        if (mBootloaderVersion != 0xFF)
        {
            // Bootloader version comes from the bootloader in major.minor format (1 byte each) but is then packed into one byte as MMmmmmmm
            // so the major version has 0-3 and minor 0-63.
            builder.append(String.format("\r\nBootloader v%d.%d Temp %ddeg.", (mBootloaderVersion>>6), mBootloaderVersion&0x3F, mSpare));
        }
        return builder.toString();
    }

}




