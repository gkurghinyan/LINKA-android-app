package com.linka.Lock.FirmwareAPI.Comms;

/**
 * Created by Loren-Admin on 10/14/2015.
 */
public class LockSettingPacket extends LockDataPacket {

    // \NOTE this list of settings needs to be synchronized with VLSOSettings.h
    public static final int VLSO_SETTING_LOCK_ID	= 0;		//!< Lock ID
    public static final int VLSO_SETTING_LOCK_PAC_CODE  = 1;	//!< Lock access code for PAC
    public static final int VLSO_SETTING_LOCK_ACTUATIONS  = 2;	//!< Lock total number of actuations
    public static final int VLSO_SETTING_LOCK_ERRORS  = 3;		//!< Lock total number of errors
    public static final int VLSO_SETTING_LOCK_MODE = 4;         //!< Lock operating mode
    public static final int VLSO_SETTING_STALL_MA = 5;
    public static final int VLSO_SETTING_ACCEL_LOCK_TH = 6;		//!< Threshold in accel to prevent locking
    public static final int VLSO_SETTING_ALARM_DELAY_S = 7;		//!< Delay in seconds before alarm starts sounding
    public static final int VLSO_SETTING_ALARM_TIMEOUT_S = 8;	//!< Alarm timeout in seconds after movement stops
    public static final int VLSO_SETTING_LAST_STATE = 9; 		//!< Saved state, when going to sleep
    public static final int VLSO_SETTING_BUMP_TH_MG = 10;
    public static final int VLSO_SETTING_RSSI_UNLOCK_MIN = 11;	//!< Minimum RSSI level to allow button press unlock
    public static final int VLSO_SETTING_AUDIO = 12;			//!< Bitmask settings for audio functions, \see VLS_AUDIO_XXXX
    public static final int VLSO_SETTING_PULSE_TH_MG = 13;		//!< Thresold for accelerometer pulse detection, in units of 0.063G, range 0-127 (0-8G)
    public static final int VLSO_SETTING_JOSTLE_100MS = 14;		//!< Time for jostle detection,
    public static final int VLSO_SETTING_HAMMER_TH_63MG = 15;	//!< Threshold for instant alarm due to hard impact
    public static final int VLSO_SETTING_ACCEL_DATARATE = 16;   //!< Accel datarate, 0 = 1.56Hz, 1 = 6.25Hz, 2 = 12.5Hz, etc up to 7 = 800Hz
    public static final int VLSO_SETTING_LOCKED_SLEEP_S = 17;
    public static final int VLSO_SETTING_LOCKED_RESLEEP_S = 18;
    public static final int VLSO_SETTING_ENC_COUNTER = 19;
    public static final int VLSO_SETTING_TEST_CYCLES = 20;
    public static final int VLSO_SETTING_MK1_0 = 21;
    public static final int VLSO_SETTING_MK1_1 = 22;
    public static final int VLSO_SETTING_MK2_0 = 23;
    public static final int VLSO_SETTING_MK2_1 = 24;
    public static final int VLSO_SETTING_MK3_0 = 25;
    public static final int VLSO_SETTING_MK3_1 = 26;
    public static final int VLSO_SETTING_BATT_RESERVE_PCT = 27;
    public static final int VLSO_SETTING_ROLL_ALRM_DEG = 28;
    public static final int VLSO_SETTING_PITCH_ALRM_DEG = 29;
    public static final int VLSO_SETTING_UNLOCKED_SLEEP_S = 30;
    public static final int VLSO_SETTING_BAD_PAC_TIMES = 31;
    public static final int VLSO_SETTING_BAD_ENC_TIMES = 32;
    public static final int VLSO_SETTING_SAVED_TAMPER = 33;
    public static final int VLSO_SETTING_BL_ENB_FLAGS = 34;
    public static final int VLSO_SETTING_BATT_CHGD_PCT = 35;
    public static final int VLSO_SETTING_BATT_CHGD_HYST = 36;
    public static final int VLSO_SETTING_ALARM_DURATION_S = 37;
    public static final int VLSO_SETTING_ACT_BLOCK_LOCK_S = 38;
    public static final int VLSO_SETTING_SIREN_BLOCK_ACT = 39;
    public static final int VLSO_SET_ACC_POST_LOCK_DELAY_S = 40;
    public static final int VLSO_SET_STALL_IGNORE_TIME_100MS = 41;  //!< Amount of time to ignore stall value while starting up motor
    public static final int VLSO_SET_MAX_UNLOCKING_TIME_250MS = 42;	//!< Maximum time for opening/closing, in 250ms increments. Range 0-127 -> 0s-31.75s. Default 10s (40)
    public static final int VLSO_SET_LOW_TEMP_C = 43;	//!< Temperature, C, at which low-temp settings go into effect (FW-1.3-008, FW-1.3-009, FW-1.3-010)
    public static final int VLSO_SET_TEMP_OFS_C = 44;
    public static final int VLSO_SET_STALL_DELAY_100MS = 45;	///< Per FW-1.3-011, delay from onset of stall to shackle retraction, in units of 100ms
    public static final int VLSO_SET_BONDING_REQUIRED = 46;         ///< Is bonding required (0 = no, 1 = yes, default yes)
    public static final int VLSO_SETTING_UNLOCKED_BUMP_TH_MG = 47;
    public static final int VLSO_SETTING_MIN_BATT_PCT_LOCK = 48;		///< Batt % threshold used for refusing to lock and forcing sleep when locked/disconnected
    public static final int VLSO_SETTING_MIN_BATT_PCT_SLEEP = 49;		///< Batt % threshold used for refusing to lock and forcing sleep when locked/disconnected
    public static final int VLSO_SETTING_UNLOCKED_UNCONN_SLEEP_S = 50;			///< When unlocked and disconnected, go to sleep after this much time.
    public static final int VLSO_SETTING_SLOW_DELAY_100MS = 51;			///< Time until the motor slows down, during locking/unlocking, in 100ms increments
    public static final int VLSO_SETTING_MOTOR_SPD_INITIAL = 52; 		///< Speed (actually voltage) setpoint for the motor when locking/unlocking initially
    public static final int VLSO_SETTING_MOTOR_SPD_SLOW = 53; 		///< Speed (actually voltage) setpoint for the motor when locking/unlocking after VLSO_SETTING_SLOW_DELAY_100MS elapsed
    public static final int VLSO_SETTING_MOTOR_LOCK_CODE = 54;		///< Non-defaultable code set at the factory to indicate the lock version/batch
    public static final int VLSO_SETTING_ALLOW_UNCONN_LOCK = 55;


    static final String[] VLSO_SETTINGS_DESCR = { "ID", "PAC", "ACTUATIONS", "ERRORS", "MODE", "STALL", "ACCEL lock th", "Alm delay s",         // 0-7
            "Alm tmt s", "Last State", "Bump th mG", "Min unlock RSSI(-)", "Audio", "Pulse mG",
            "Jostle 100ms", "Hammer mG", "Accel Datarate", "Locked Sleep s", "Locked resleep s",
            "ENCC", "Test Cycles", "MK10", "MK11", "MK20", "MK21", "MK30", "MK31", "Batt reserve pct",
            "Roll alm deg.", "Pitch alm deg.", "Unlocked sleep s", "Bad PAC entry max", "Bad encr max",
            "Saved Tamper", "BL flags", "Batt charged %", "Batt charged hyst",
            "[+1]", "[+2]"};



    public static final int VLSO_INVALID_VALUE = 0xFFFFFFFF;    // Used to mark unknown settings value

    public static final int LOCK_MODE_NORMAL = 0;
    public static final int LOCK_MODE_TEST = 2;
    public static final int LOCK_SETTING_PKT_OVERHEAD_BYTES = 5;    // Overhead is: len, cmd, setting,
    private int m_Setting;
    private int m_SettingValue;
    private int m_SettingValue2;
    private byte m_Command;

    public int settingIndex()
    {
        return m_Setting;
    }

    public int value()
    {
        return m_SettingValue;
    }

    public static String[] LockSettingDescr()
    {
        return VLSO_SETTINGS_DESCR;
    }

    public LockSettingPacket(byte[] data) {
        super(data);
        // Byte 0 is length, byte 1 is command (set or get setting)
        m_Command = data[1];
        m_Setting = data[2];
        m_SettingValue = bytesToULONG(data[3], data[4], data[5], data[6]);
        m_SettingValue2 = bytesToULONG(data[7], data[8], data[9], data[10]);
    }

    public LockSettingPacket (byte command, int settingIndex, int settingValue, int settingValue2)
    {
        super (command, new byte[LOCK_SETTING_PKT_OVERHEAD_BYTES + 8]);      // Setting packet length is 13, overhead plus two dword values
        //super(); is called by default here....
        m_Setting = settingIndex;
        m_SettingValue = settingValue;
        // First two bytes are <len><cmd>
        m_PacketData[2] = (byte)m_Setting;
        m_PacketData[3] = (byte)(settingValue & 0xFF);
        m_PacketData[4] = (byte)((settingValue >> 8) & 0xFF);
        m_PacketData[5] = (byte)((settingValue >> 16) & 0xFF);
        m_PacketData[6] = (byte)((settingValue >> 24) & 0xFF);
        m_PacketData[7] = (byte)(settingValue2 & 0xFF);
        m_PacketData[8] = (byte)((settingValue2 >> 8) & 0xFF);
        m_PacketData[9] = (byte)((settingValue2 >> 16) & 0xFF);
        m_PacketData[10] = (byte)((settingValue2 >> 24) & 0xFF);
        updateCRC();
    }

    public LockSettingPacket (byte command, int settingIndex, byte[] data)
    {
        super (command, new byte[LOCK_SETTING_PKT_OVERHEAD_BYTES + data.length]);      // Setting packet length is 13
        //super(); is called by default here....
        m_Setting = settingIndex;
        m_PacketData[2] = (byte)m_Setting;
        // First two bytes are <len><cmd>
        System.arraycopy(data, 0, m_PacketData, 3, data.length);
        updateCRC();
    }


    public String toString ()
    {
        StringBuilder builder = new StringBuilder();

        if (m_Command == LockCommand.VCMD_SET_SETTING)
            builder.append("SET_SETTING ");
        else if (m_Command == LockCommand.VCMD_GET_SETTING)
            builder.append("SET_SETTING ");
        else
            builder.append ("BAD SETTING CMD ");

        builder.append(String.format(" %s (%d) value %d (0x%X) %d (0x%X)", settingIndexToString(), m_Setting, m_SettingValue, m_SettingValue, m_SettingValue2, m_SettingValue2));

        return builder.toString();
    }

    public String settingIndexToString () {
        switch (m_Setting) {
            case VLSO_SETTING_LOCK_ID:
                return "VLSO_SETTING_LOCK_ID";
            case VLSO_SETTING_LOCK_PAC_CODE:
                return "VLSO_SETTING_LOCK_PAC_CODE";
            case VLSO_SETTING_LOCK_ACTUATIONS:
                return "VLSO_SETTING_LOCK_ACTUATIONS";
            case VLSO_SETTING_LOCK_ERRORS:
                return "VLSO_SETTING_LOCK_ERRORS";
            case VLSO_SETTING_LOCK_MODE:
                return "VLSO_SETTING_LOCK_MODE";
            case VLSO_SETTING_STALL_MA:
                return "VLSO_SETTING_STALL_MA";
            case VLSO_SETTING_ACCEL_LOCK_TH:
                return "VLSO_SETTING_ACCEL_LOCK_TH";
            case VLSO_SETTING_ALARM_DELAY_S:
                return "VLSO_SETTING_ALARM_DELAY_S";
            case VLSO_SETTING_ALARM_TIMEOUT_S:
                return "VLSO_SETTING_ALARM_TIMEOUT_S";
            case VLSO_SETTING_LAST_STATE:
                return "VLSO_SETTING_LAST_STATE";
            case VLSO_SETTING_BUMP_TH_MG:
                return "VLSO_SETTING_BUMP_TH_MG";
            case VLSO_SETTING_RSSI_UNLOCK_MIN:
                return "VLSO_SETTING_RSSI_UNLOCK_MIN";
            case VLSO_SETTING_AUDIO:
                return "VLSO_SETTING_AUDIO";
            case VLSO_SETTING_PULSE_TH_MG:
                return "VLSO_SETTING_PULSE_TH_MG";
            case VLSO_SETTING_JOSTLE_100MS:
                return "VLSO_SETTING_JOSTLE_100MS";
            case VLSO_SETTING_HAMMER_TH_63MG:
                return "VLSO_SETTING_JOSTLE_100MS";
            case VLSO_SETTING_ACCEL_DATARATE:
                return "VLSO_SETTING_ACCEL_DATARATE";
            case VLSO_SETTING_LOCKED_SLEEP_S:
                return "VLSO_SETTING_LOCKED_SLEEP_S";
            case VLSO_SETTING_LOCKED_RESLEEP_S:
                return "VLSO_SETTING_LOCKED_RESLEEP_S";
            case VLSO_SETTING_ENC_COUNTER:
                return "VLSO_SETTING_ENC_COUNTER";
            case VLSO_SETTING_TEST_CYCLES:
                return "VLSO_SETTING_TEST_CYCLES";
            case VLSO_SETTING_MK1_0:
                return "VLSO_SETTING_MK1_0";
            case VLSO_SETTING_MK1_1:
                return "VLSO_SETTING_MK1_1";
            case VLSO_SETTING_MK2_0:
                return "VLSO_SETTING_MK2_0";
            case VLSO_SETTING_MK2_1:
                return "VLSO_SETTING_MK2_1";
            case VLSO_SETTING_MK3_0:
                return "VLSO_SETTING_MK3_0";
            case VLSO_SETTING_MK3_1:
                return "VLSO_SETTING_MK3_1";
            case VLSO_SETTING_BATT_RESERVE_PCT:
                return "VLSO_SETTING_BATT_RESERVE_PCT";
            case VLSO_SETTING_ROLL_ALRM_DEG:
                return "VLSO_SETTING_ROLL_ALRM_DEG";
            case VLSO_SETTING_PITCH_ALRM_DEG:
                return "VLSO_SETTING_PITCH_ALRM_DEG";
            case VLSO_SETTING_UNLOCKED_SLEEP_S:
                return "VLSO_SETTING_UNLOCKED_SLEEP_S";
            case VLSO_SETTING_BAD_PAC_TIMES:
                return "VLSO_SETTING_BAD_PAC_TIMES";
            case VLSO_SETTING_BAD_ENC_TIMES:
                return "VLSO_SETTING_BAD_ENC_TIMES";
            case VLSO_SETTING_SAVED_TAMPER:
                return "VLSO_SETTING_SAVED_TAMPER";
            case VLSO_SETTING_BL_ENB_FLAGS:
                return "VLSO_SETTING_BL_ENB_FLAGS";
            case VLSO_SETTING_BATT_CHGD_PCT:
                return "VLSO_SETTING_BATT_CHGD_PCT";
            case VLSO_SETTING_BATT_CHGD_HYST:
                return "VLSO_SETTING_BATT_CHGD_HYST";
            case VLSO_SETTING_ALARM_DURATION_S:
                return "VLSO_SETTING_ALARM_DURATION_S";
            case VLSO_SETTING_ACT_BLOCK_LOCK_S:
                return "VLSO_SETTING_ACT_BLOCK_LOCK_S";
            case VLSO_SETTING_SIREN_BLOCK_ACT:
                return "VLSO_SETTING_SIREN_BLOCK_ACT";
            case VLSO_SET_ACC_POST_LOCK_DELAY_S:
                return "VLSO_SET_ACC_POST_LOCK_DELAY_S";
            case VLSO_SET_STALL_IGNORE_TIME_100MS:
                return "VLSO_SET_STALL_IGNORE_TIME_100MS";
            case VLSO_SET_MAX_UNLOCKING_TIME_250MS:
                return "VLSO_SET_MAX_UNLOCKING_TIME_250MS";
            case VLSO_SET_LOW_TEMP_C:
                return "VLSO_SET_LOW_TEMP_C";
            case VLSO_SET_TEMP_OFS_C:
                return "VLSO_SET_TEMP_OFS_C";
            case VLSO_SET_STALL_DELAY_100MS:
                return "VLSO_SET_STALL_DELAY_100MS";
            case VLSO_SET_BONDING_REQUIRED:
                return "VLSO_SET_BONDING_REQUIRED";
            case VLSO_SETTING_UNLOCKED_BUMP_TH_MG:
                return "VLSO_SETTING_UNLOCKED_BUMP_TH_MG";
            case VLSO_SETTING_MIN_BATT_PCT_LOCK:
                return "VLSO_SETTING_MIN_BATT_PCT_LOCK";
            case VLSO_SETTING_MIN_BATT_PCT_SLEEP:
                return "VLSO_SETTING_MIN_BATT_PCT_SLEEP";
            case VLSO_SETTING_UNLOCKED_UNCONN_SLEEP_S:
                return "VLSO_SETTING_UNLOCKED_UNCONN_SLEEP_S";
            case VLSO_SETTING_SLOW_DELAY_100MS:
                return "VLSO_SETTING_SLOW_DELAY_100MS";
            case VLSO_SETTING_MOTOR_SPD_INITIAL:
                return "VLSO_SETTING_MOTOR_SPD_INITIAL";
            case VLSO_SETTING_MOTOR_SPD_SLOW:
                return "VLSO_SETTING_MOTOR_SPD_SLOW";
            case VLSO_SETTING_MOTOR_LOCK_CODE:
                return "VLSO_SETTING_MOTOR_LOCK_CODE";

            case VLSO_SETTING_ALLOW_UNCONN_LOCK:
                return "VLSO_SETTING_ALLOW_UNCONN_LOCK";
            default:
                return "[UNKNOWN SETTING INDEX]";
        }
    }

}
