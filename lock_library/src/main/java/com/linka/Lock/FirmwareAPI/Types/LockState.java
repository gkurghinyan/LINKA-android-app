package com.linka.Lock.FirmwareAPI.Types;

/**
 * Created by Loren-Admin on 8/20/2015.
 */
public class LockState {
    // Lock status values:
    public final static byte LOCK_STARTUP = 0;
    public final static byte LOCK_LOCKING = 1;
    public final static byte LOCK_UNLOCKING = 2;
    public final static byte LOCK_LOCKED = 3;
    public final static byte LOCK_UNLOCKED = 4;
    public final static byte LOCK_ERROR = 5;
    public final static byte LOCK_STALLED = 6;
    public final static byte LOCK_LOCKED_PAC = 7;
    public final static byte LOCK_UNLOCKED_PAC = 8;
    public final static byte LOCK_UNKNOWN_STATE = (byte)0xFF;

    public final byte m_nLockState;

    public LockState (byte _state)
    {
        m_nLockState = _state;
    }

    public String toString ()
    {
        String szStatus;
        switch (m_nLockState)
        {
            case LOCK_LOCKED:
                szStatus = "Locked";
                break;
            case LOCK_LOCKING:
                szStatus = "Locking";
                break;
            case LOCK_ERROR:
                szStatus = "Error";
                break;
            case LOCK_UNLOCKED:
                szStatus = "Unlocked";
                break;
            case LOCK_UNLOCKING:
                szStatus = "Unlocking";
                break;
            case LOCK_STALLED:
                szStatus = "Stalled";
                break;
            case LOCK_STARTUP:
                szStatus = "Startup";
                break;
            case LOCK_LOCKED_PAC:
                szStatus = "Locked-PAC";
                break;
            case LOCK_UNLOCKED_PAC:
                szStatus = "Unlocked-PAC";
                break;
            default:
                szStatus = String.format ("Unknown lock status %d.", m_nLockState);
        }
        return szStatus;
    }

    public byte GetValue() {
        return m_nLockState;
    }
}