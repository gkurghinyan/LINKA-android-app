package com.linka.Lock.FirmwareAPI.Types;

/**
 * Created by Loren-Admin on 8/20/2015.
 */
public class AuthState {
    public static final byte AUTH_NONE = 0;
    public static final byte AUTH_PAIRING = 1;
    public static final byte AUTH_COMPLETE = 2;
    public static final byte AUTH_ERROR = 3;
    public static final byte AUTH_PAIRED = 4;

    private final byte m_AuthState;

    public AuthState (byte _state)
    {
        m_AuthState = _state;
    }

    public String toString()
    {
        switch (m_AuthState)
        {
            case AUTH_NONE:
                return "Auth: None";
            case AUTH_PAIRING:
                return "Auth: Pairing";
            case AUTH_COMPLETE:
                return "Auth: Complete";
            case AUTH_ERROR:
                return "Auth: Error";
            case AUTH_PAIRED:
                return "Auth: Paired";
            default:
                return String.format("[Auth unknown state %d.]", m_AuthState);
        }
    }

    public byte GetValue() {
        return m_AuthState;
    }


}
