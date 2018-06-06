package com.linka.Lock.FirmwareAPI.Comms;

import android.util.Log;

/**
 * Created by Loren-Admin on 4/4/2016.
 *
 * Ack/Nak is sent in a packet with the following structure:
 * typedef struct __attribute__((packed)) {
     uint8_t length;
     uint8_t cmd;
     uint8_t orig_cmd;	// original command
     uint32_t ulData;	// related data
     uint16_t CRC;	//9b
     } VLSO_ACK_NAK_PKT;

 *
 */
public class LockAckNakPacket {
    private final static String TAG = LockAckNakPacket.class.getSimpleName();
    private final static int MIN_ACKNAK_PKTLEN = 9;
    protected byte mLength;
    protected LockCommand mLockCommand;
    protected LockCommand mOriginalCommand;
    protected int mAssociatedData;
    protected int mCounter;

    public int getCounter() { return mCounter; }
    public LockCommand getAckNakedCommand() { return mOriginalCommand; }
    public int getAssociatedData () { return mAssociatedData; }

    public LockCommand getmLockCommand() {
        return mLockCommand;
    }

    public LockAckNakPacket(byte[] data) {
        if (data.length >= MIN_ACKNAK_PKTLEN)
        {
            mLength = data[0];

            if (mLength != data.length) {
                Log.d(TAG, "Invalid length for AckNak packet.");
            } else {
                mLockCommand = new LockCommand(data[1]);
                mOriginalCommand = new LockCommand(data[2]);
                mAssociatedData = LockAdV1.getLongFromBytes(data, 3);
                if (data.length >= 10)
                    mCounter = LockAdV1.getLongFromBytes(data, 7);
            }

        }
    }

    public String toString ()
    {
        StringBuilder builder = new StringBuilder();

        builder.append (mLockCommand);
        builder.append (" to orig cmd ");
        builder.append (mOriginalCommand);
        builder.append (" (");
        builder.append (Integer.toHexString(mAssociatedData));
        builder.append ("). ctr 0x");
        builder.append (Integer.toHexString(mCounter));
        return builder.toString();
    }

}
