package com.linka.Lock.FirmwareAPI.Comms;

import android.util.Log;

import com.linka.Lock.FirmwareAPI.Types.AuthState;
import com.linka.Lock.FirmwareAPI.Types.LockState;
import com.linka.Lock.FirmwareAPI.Types.SelfTestFlags;
import com.linka.Lock.FirmwareAPI.Types.StateTransitionReason;
import com.linka.Lock.FirmwareAPI.Types.SystemFlags;


/**
 * Created by Loren-Admin on 5/20/2015.
 *
 *
 * Defined in VLSOComms.h:
 typedef struct __attribute__((packed)) {
 uint8_t length;
 uint8_t cmd;
 uint8_t state;  // Lock state
 uint8_t batt_pct_raw_x2;  // Raw battery percentage in units of 0.5%
 uint16_t batt_mV;
 uint8_t batt_pct;
 uint8_t ucAuthState;	//!< Authentication state machine state (\see AUTH_STATE)
 uint32_t ulTestStatus;
 uint32_t ulStatusFlags;
 uint8_t ucTransitionReason;
 uint8_t ucReserved;
 uint16_t CRC;           // 18b
 } VLSO_STATUS_PACKET;

 *
 */
public class LockStatusPacket extends LockDataPacket {
    private final static String TAG = LockStatusPacket.class.getSimpleName();

    public byte mTransitionReason;
    public byte mTransitionReasonPrev;
    private byte mReserved;

    private LockState mLockState;            // Current lock state
    private AuthState mAuthState;
    int mSelfTestStatusFlags;
    int mStateFlags;
    int m_usCRC;
    //private
    private byte mLockTestTimer_s;   // Time remaining in this state (seconds)
    //private short mLockBattery_mV;      // Current battery voltage, mV
    private double mBatteryVoltage;
    private byte mBatteryPercent;      // Lock battery in percent remaining
    private double mBatteryPercentRaw;

    public static final int MIN_STATUS_PKT_LEN = 8; // Len, cmd, ...

    public double GetBatteryVoltage()
    {
        return mBatteryVoltage;
    }

    public byte GetBatteryPercent()
    {
        return mBatteryPercent;
    }
    public int GetStateFlags() { return mStateFlags; }
    public int GetStatusFlags() { return mSelfTestStatusFlags; }
    public int GetCurrent_mA() { return mReserved; }

    public LockState GetLockState()
    {
        return mLockState;
    }

    public long GetTamperSate(){
        return (mStateFlags & LockAdV1.VLS_FLAG_ALARM_TIP);
    }

    //if GetTamperState() == 0 nothings happened

    public AuthState GetAuthState()
    {
        return mAuthState;
    }

    public String toString ()
    {
        StringBuilder builder = new StringBuilder();
        //builder.append (super.toString());

        builder.append(String.format("Lock Status (%d): batt ", mLockTestTimer_s));
        builder.append(String.format("%.3fV (%d%%, raw %f%%)\r\n", mBatteryVoltage, mBatteryPercent, mBatteryPercentRaw));
        builder.append("State :");
        builder.append(mLockState);
        builder.append(" , reason ");
        builder.append(StateTransitionReason.toString(mTransitionReason));
        builder.append("\r\n");
        builder.append(mAuthState);
        builder.append("\r\n");

        if (mSelfTestStatusFlags != 0)
        {
            builder.append(String.format("Test failures: 0x%X: %s\r\n", mSelfTestStatusFlags, SelfTestFlags.flagsToString(mSelfTestStatusFlags)));
        }

        if (mStateFlags != 0)
        {
            builder.append (String.format ("State flags: 0x%X: %s\r\n", mStateFlags, SystemFlags.flagsToString(mStateFlags)));
        }

        builder.append (String.format ("CRC: 0x%X. Stall %dmA", m_usCRC, mReserved));

        return builder.toString();
    }

    public LockStatusPacket(byte[] data)
    {
        super(data);    // Base class will determine packet type, etc

        if (getCmdType().GetValue() != LockCommand.VCMD_STATUS)
        {
            // Error, this is not a status packet
            Log.e(TAG, "Not a status packet.");
            m_bValid = false;
        }
        else if (m_PacketData.length < MIN_STATUS_PKT_LEN)
        {
            Log.e(TAG, "Bad status packet length.");
            m_bValid = false;
        }
        else
        {
            // This is a status packet. Parse the fields
//            mLockState = m_PayloadData[0]; // Header is not included here. First payload byte is the state
            mLockState = new LockState(m_PacketData[2]);
            //mLockTestTimer_s = m_PacketData[3];
            mBatteryPercentRaw = (m_PacketData[3]&0xFF) * 0.5; // Convert from pct*2 to percent
            //mBatteryVoltage = ((int)m_PacketData[4] + ((int)m_PacketData[5]<<8))/1000.0;
            mBatteryVoltage = bytesToUSHORT(m_PacketData[4], m_PacketData[5])/1000.0;
            mBatteryPercent = m_PacketData[6];
            mAuthState = new AuthState(m_PacketData[7]);
            if (m_PacketData.length >= 12)
                mSelfTestStatusFlags = bytesToULONG(m_PacketData[8], m_PacketData[9], m_PacketData[10], m_PacketData[11]);
            else
                mSelfTestStatusFlags = 0xFFFFFFFF;
            if (m_PacketData.length >= 16)
            {
                mStateFlags = bytesToULONG(m_PacketData[12], m_PacketData[13], m_PacketData[14], m_PacketData[15]);
            }
            else
            {
                mStateFlags = 0xFFFFFFFF;
            }

            // transition reason
            mTransitionReasonPrev = mTransitionReason;
            mTransitionReason = m_PacketData[16];
            // Reserved
            mReserved = m_PacketData[17];

            // CRC
            if (m_PacketData.length >= 18)
            {
                m_usCRC = bytesToUSHORT(m_PacketData[18], m_PacketData[19]);
            }
            else
            {
                m_usCRC = 0xFFFF;
            }
            //mLockBattery_mV = parse_short(m_PayloadData[2]
        }
    }

}
