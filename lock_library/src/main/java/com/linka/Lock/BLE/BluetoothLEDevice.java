package com.linka.Lock.BLE;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.linka.Lock.FirmwareAPI.Comms.LockAdV1;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatCodePointException;
import java.util.UUID;

/**
 * Created by loren on 1/13/2015.
 */
public class BluetoothLEDevice  implements Comparable {
    private final BluetoothDevice mDevice;
    private final int mRSSI;
    private boolean mBonded;
    private byte[] mScanRecord;
    private boolean m_bCompareByRSSI;
    private ArrayList<AdRecord> mAdRecordList;
    private ArrayList<UUID> m128BitUUIDList;
    private LockAdV1 mLockAdInfo;

    private final static String TAG = BluetoothLEDevice.class.getSimpleName();

    public BluetoothLEDevice(BluetoothDevice device, int rssi, byte[] scanrecord, boolean bonded) {
        mDevice = device;
        mBonded = bonded;
        mRSSI = rssi;
        mScanRecord = scanrecord;
        m_bCompareByRSSI = true;
        mAdRecordList = new ArrayList<AdRecord>();
        m128BitUUIDList = new ArrayList<UUID>();        // Store list of scan record 128-bit IDs
        parseScanRecord(scanrecord);
    }

    public boolean has128BitUUID(UUID uuid)
    {
        return m128BitUUIDList.contains(uuid);
    }

    public boolean isBonded() { return mBonded; }

    //public static String getUUIDReversedFromByteArray(byte[] bytes) {
    //    return getUUIDReversedFromByteArray(bytes).toString();
    //}
    public static byte[] reverseByteArray (byte[] original, int offset)
    {
        int length = original.length - offset;

        byte[] reversed = new byte[length];
        for (int i = 0; i < (length); i++)
        {
            reversed[(length-1)-i] = original[i+offset];
        }
        return reversed;
    }

        public static UUID getUUIDReversedFromByteArray(byte[] bytes, int offset) {
        ByteBuffer bb = ByteBuffer.allocate(bytes.length - offset);
        bb.order(ByteOrder.LITTLE_ENDIAN);



        // The bytes are probably in reverse order, so we need to fix that
        byte[] reversed = reverseByteArray(bytes, offset);
        bb = ByteBuffer.wrap(reversed);
        long high = bb.getLong();
        long low = bb.getLong();
        UUID uuid = new UUID(high, low);
        return uuid;
    }

    public static UUID getUUIDFromByteArray(byte[] bytes, int offset) {
        ByteBuffer bb = ByteBuffer.allocate(bytes.length - offset);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        byte[] subset = Arrays.copyOfRange(bytes, offset, bytes.length - offset);
        bb = ByteBuffer.wrap(subset);
        long high = bb.getLong();
        long low = bb.getLong();
        UUID uuid = new UUID(high, low);
        return uuid;
    }


    private void parseScanRecord (byte[] scanrecord)
    {
        mScanRecord = scanrecord;
        // Parse scan record into ad record list
        int i = 0;
        while (i < mScanRecord.length)
        {
            int len = mScanRecord[i++];
            if ((len > 0) && (i + len) < mScanRecord.length) {
                int type = mScanRecord[i++];
                byte[] data = Arrays.copyOfRange(mScanRecord, i, i + len - 1);  // Data length is LEN - 2 (for the length and type bytes)
                AdRecord ad = new AdRecord(len, type, data);
                mAdRecordList.add(ad);
                // Parse Lock info if found
                if (ad.getType() == AdRecord.TYPE_MFR)
                {
                    if (ad.getData()[2] == LockAdV1.LINKA_AD_VER_1)  // LINKA advertising v1 = 0xC1
                    {
                        Log.d(TAG, String.format("Found v1 Lock v.0x%X record of length %d, parsing...", ad.getData()[2], ad.getLength()));
                        mLockAdInfo = new LockAdV1(ad.getData(), 0);

                        //mLockAdInfo = new ScootAdV1(ad.getData(), 4); // Data record starts 4 bytes in to data
                    } else if (ad.getData()[2] == LockAdV1.LINKA_AD_VER_2) // LINKA advertising v1
                    {
                        Log.d(TAG, String.format("Found v2 Lock v.0x%X record of length %d, parsing...", ad.getData()[2], ad.getLength()));
                        mLockAdInfo = new LockAdV1(ad.getData(), 0);
                    } else if ((ad.getData()[0] == 0x59) && (ad.getData()[1] == 0x00))    // Nordic mfg \todo replace with Velasso when available
                    {
                        if ((ad.getData()[2] == (byte) 0xBE) && (ad.getData()[3] == (byte) 0xAC)) {
                            // See if this is our beacon; the rest of the data should match our main service UUID

                            // Our UUID starts at [4]
                            UUID uuid = getUUIDFromByteArray(data, 4);
                            //if (uuid.compareTo())
                            m128BitUUIDList.add(uuid);  // \todo distinguish between beacon and advertised UUIDs
                            Log.d(TAG, "Found a beacon, UUID " + uuid.toString());

                        } else {
                            Log.d(TAG, "Other non beacon" + ad.toString());
                        }
                    } else {
                        Log.d(TAG, "Other mfg ad:" + ad.toString());
                    }
                }
                else if ((ad.getType() == AdRecord.TYPE_INCOMPLETE_128) || (ad.getType() == AdRecord.TYPE_COMPLETE_128))
                {
                    // Check to see if it is our UUID
                    Log.d(TAG, "Got UUID " + ad.toString());
                    if (data.length == (128/8)) {   // Should be 128bits/8bits/byte = 16 bytes in the 128-bit UUID
                        m128BitUUIDList.add(getUUIDReversedFromByteArray(data, 0));
                    }
                }
                else if ((ad.getType() == AdRecord.TYPE_NAME))
                {
                    Log.d(TAG, "Got Name " + ad.toString());
                }
                else
                {
                    Log.d(TAG, "Other ad type, " + ad.getType() + " of length " + ad.getLength());
                }

                i += len-1;
            }
            else {
                Log.d(TAG, String.format("Scan record length issue, len %d, index %d.", len, i));
                return;
            }
        }

    }

    public LockAdV1 updateAdvData(BluetoothDevice device, int rssi, byte[] scanrecord) {
        parseScanRecord(scanrecord);
        return mLockAdInfo;
    }

    public LockAdV1 getAdvData() {
        return mLockAdInfo;
    }

    public void setSortByRSSI (boolean sortByRSSI)
    {
        m_bCompareByRSSI = sortByRSSI;
    }

    public BluetoothDevice getDevice()
    {
        return mDevice;
    }

    public int getRSSI()
    {
        return mRSSI;
    }

    public byte[] getScanRecord()
    {
        return mScanRecord;
    }

    public String getLockAdData ()
    {
        if (mLockAdInfo != null)
            return mLockAdInfo.toString();
        else
            return "[Null]";
    }

    public boolean getLockAdPassState() {
        return (mLockAdInfo.GetSelfTestStatus() == 0);
    }


    public String getName()
    {
        return mDevice.getName();
    }

    public String getAddress()
    {
        return mDevice.getAddress();
    }


    public String getMfgScanRec ()
    {
        // If scan record contains mfg. data, return as hex-formatted string, else return null string.
        // Parse the scan record
        for (AdRecord r : mAdRecordList)
        {
            if (r.getType() == AdRecord.TYPE_MFR)
            {
                return r.getData().toString();
            }
        }

        return ("[Not found]");

    }

    @Override
    public int compareTo(Object another) {
        if (m_bCompareByRSSI) {
            return (mRSSI - ((BluetoothLEDevice) another).mRSSI);
        }
        else
        {
            // If not sorting by RSSI, we sort by name
            return getName().compareTo(((BluetoothLEDevice) another).getName());
        }
    }


    class AdRecord {
        private final int mLength;
        private final int mType;
        private final byte[] mData;
        public static final byte TYPE_MFR = (byte)0xFF;
        public static final byte TYPE_INCOMPLETE_128    = (byte)0x06;
        public static final byte TYPE_COMPLETE_128      = (byte)0x07;
        public static final byte TYPE_NAME              = (byte)0x09;   // Check this

        public String toString ()
        {
            StringBuilder builder = new StringBuilder();

            builder.append("Ad record type ");
            builder.append(mType);
            if (mType == TYPE_NAME)
            {
                // show raw data
                builder.append(" ");
                for (byte b : mData) {
                    try {
                        builder.append(String.format("%c", b));
                    } catch (IllegalFormatCodePointException e) {
                        //e.printStackTrace();
                    }
                }
            }
            else
            {
                // show raw data
                builder.append(" data ");
                for (byte b : mData) {
                    try {
                        builder.append(String.format("%02X", b));
                    } catch (IllegalFormatCodePointException e) {
                        e.printStackTrace();
                    }
                }
            }
            builder.append ("\r\n");
            return builder.toString();
        }

        AdRecord (int length, int type, byte[] data)
        {
            mLength = length;
            mType = type;
            mData = data;
        }

        int getType ()
        {
            return mType;
        }

        int getLength()
        {
            return mLength;
        }

        byte[] getData()
        {
            return mData;
        }
    }

}
