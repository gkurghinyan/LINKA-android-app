package com.linka.Lock.FirmwareAPI.Comms;

/**
 * Created by Loren-Admin on 8/20/2015.
 */
public class LockCommand  {
    public static final byte VCMD_UNLOCK = 0;
    public static final byte VCMD_LOCK = 1;
    public static final byte VCMD_STATUS = 2;
    public static final byte VCMD_SET_SETTING = 3;
    public static final byte VCMD_GET_SETTING = 4;
    public static final byte VCMD_HALT = 5;
    public static final byte VCMD_ACK = 6;			//!< ACK, subcommand is ACK'd command followed by reason code
    public static final byte VCMD_NAK = 7;			//!< NAK, subcommand is NAK'd command followed by reason code
    public static final byte VCMD_FWUPG = 8;
    public static final byte VCMD_SLEEP = 9;        //!< Go to sleep
    public static final byte VCMD_INFO = 10;
    public static final byte VCMD_CONTEXT = 11;
    public static final byte VCMD_DEFAULT_SETTINGS = 12;		//!< Default all EEPROM settings
    public static final byte VCMD_FORGET_BONDS = 13;			//!< Forget all bonds
    public static final byte VCMD_ACTIVATE_SIREN = 14;          //!< Activate the siren for N seconds, specified by subcommand byte
    public static final byte VCMD_AUTHENTICATE = 15;			//!< Authenticate ourselves to the lock, allowing e.g. double-press operations etc.
    public static final byte VCMD_FORGET_CURRENT_BOND = 16;	//!< Forget the current device's bonding
    public static final byte VCMD_FORGET_BOND_X = 17;		//!< Forget specified bond
    public static final byte VCMD_PLAY_TUNE = 18;			//!< Play specified tune (subcommand is tune index)
    public static final byte VCMD_STOP_ALARM = 20;          //!< Stop siren


        /*

 * Packet structure for communicating with the
 * lock from the app.
        typedef struct __attribute__((packed)) {
            uint8_t length;
            uint8_t cmd;
            uint8_t data[16];
            uint16_t CRC;	//20b
        } VLSO_CMDPKT;

         */

    private byte m_value;

    public LockCommand (byte _val)
    {
        m_value = _val;
    }

    public byte GetValue()
    {
        return m_value;
    }

    public String toString ()
    {
        switch (m_value)
        {
            case VCMD_UNLOCK:
                return "VCMD_UNLOCK";
            case VCMD_LOCK:
                return "VCMD_LOCK";
            case VCMD_GET_SETTING:
                return "VCMD_GET_SETTING";
            case VCMD_SET_SETTING:
                return "VCMD_SET_SETTING";
            case VCMD_STATUS:
                return "VCMD_STATUS";
            case VCMD_HALT:
                return "VCMD_HALT";
            case VCMD_ACK:
                return "VCMD_ACK";
            case VCMD_NAK:
                return "VCMD_NAK";
            default:
                return String.format("[unknown cmd %d.]", m_value);
        }
    }


}