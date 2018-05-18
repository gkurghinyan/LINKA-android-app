package com.linka.Lock.FirmwareAPI.Types;

/**
 * Created by Loren-Admin on 8/30/2015.
 */
public class StateTransitionReason {
    /**
     * Must synchronize with firmware's VLSOComms.h
     *
     * typedef enum {
     REASON_NONE = 0,
     REASON_KEYPRESS = 1,			/// Transition caused by user keypress
     REASON_TIMEOUT = 2,				/// Transition caused by timeout
     REASON_LIMIT_SW = 3,			/// Transition caused by limit switch
     REASON_AUTH_REQ = 4,			/// Transition caused by authorized user request
     REASON_PAC_ENTRY = 5,			/// Transition caused by correct PAC code entry
     REASON_PAC_FAIL = 6,			/// Transition caused by incorrect/bad PAC code entry
     REASON_STARTUP = 7,				/// Transition caused by startup
     } TRANSITION_REASON;

     */
    public static final byte REASON_NONE = 0;
    public static final byte REASON_KEYPRESS = 1;			/// Transition caused by user keypress
    public static final byte REASON_TIMEOUT = 2;				/// Transition caused by timeout
    public static final byte REASON_LIMIT_SW = 3;			/// Transition caused by limit switch
    public static final byte REASON_AUTH_REQ = 4;			/// Transition caused by authorized user request
    public static final byte REASON_PAC_ENTRY = 5;			/// Transition caused by correct PAC code entry
    public static final byte REASON_PAC_FAIL = 6;			/// Transition caused by incorrect/bad PAC code entry
    public static final byte REASON_STARTUP_UNKNOWN = 7;				/// Transition caused by startup
    public static final byte REASON_TEST_MODE = 8;              /// We are cycling the lock in test mode
    public static final byte REASON_HW_ERR = 9; 				/// Hardware error; e.g. communications error with motor controller
    public static final byte REASON_TIMEOUT_EXIT_LOCKED_LIM = 10;	/// Transition caused by staying in limit switch too long while exiting it
    public static final byte REASON_TIMEOUT_EXIT_UNLOCKED_LIM = 11;	/// Transition caused by staying in limit switch too long while exiting it
    public static final byte REASON_FAULT = 12;
    public static final byte REASON_STALL = 13;
    public static final byte REASON_BLE_CMD = 14;
    public static final byte REASON_IDLE_TIMEOUT = 15;
    public static final byte REASON_STARTUP_BUTTONPRESS = 16;
    public static final byte REASON_STARTUP_ACCEL1 = 17;
    public static final byte REASON_STARTUP_ACCEL2 = 18;
    public static final byte REASON_STARTUP_USB = 19;


    private final byte m_TransitionReason;

    public StateTransitionReason (byte _reason)
    {
        m_TransitionReason = _reason;
    }

    public String toString ()
    {
        return toString(m_TransitionReason);
    }

    public static String toString(byte reason)
    {
        switch (reason)
        {
            case REASON_NONE:
                return "None";
            case REASON_KEYPRESS:
                return "Keypress";
            case REASON_TIMEOUT:
                return "Timeout";
            case REASON_LIMIT_SW:
                return "Limit sw.";
            case REASON_AUTH_REQ:
                return "Auth. required.";
            case REASON_PAC_ENTRY:
                return "PAC entry.";
            case REASON_PAC_FAIL:
                return "PAC fail";
            case REASON_STARTUP_UNKNOWN:
                return "Start-unk.";
            case REASON_TEST_MODE:
                return "Test Mode";
            case REASON_HW_ERR:
                return "Hardware Error";
            case REASON_TIMEOUT_EXIT_LOCKED_LIM:
                return "Timeout ex. locked limit";
            case REASON_TIMEOUT_EXIT_UNLOCKED_LIM:
                return "Timeout ex. unlocked limit";
            case REASON_FAULT:
                return "Fault";
            case REASON_STALL:
                return "Stall";
            case REASON_BLE_CMD:
                return "BLE CMD";
            case REASON_IDLE_TIMEOUT:
                return "Idle timeout";
            case REASON_STARTUP_ACCEL1:
                return "Start-accel1";
            case REASON_STARTUP_ACCEL2:
                return "Start-accel2";
            case REASON_STARTUP_BUTTONPRESS:
                return "Start-button";
            case REASON_STARTUP_USB:
                return "Start-USB";
            default:
                return String.format("[Unknown reason %d.]", reason);
        }
    }



}
