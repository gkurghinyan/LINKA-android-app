package com.linka.Lock.FirmwareAPI.Types;

/**
 * Created by Loren-Admin on 8/26/2015.
 */
public class SystemFlags {


    public final static long VLS_FLAG_RADIO_EV	=	(1L<<0);			// Radio has been active
    public final static long VLS_FLAG_DEVMODE1	=	(1L<<1);			//!< Development mode, ignores timeouts (for non magnet operation)
    public final static long VLS_FLAG_CHARGING	=	(1L<<2);			//!< Device is charging
    public final static long VLS_FLAG_CHARGED	=	(1L<<3);			//!< Device is charged

    public final static long VLS_FLAG_BATT_LO	=	(1L<<4);			//!< Battery is low
    public final static long VLS_FLAG_STALL  	=	(1L<<5);
    public final static long VLS_FLAG_ALARM_BUMP  =	(1L<<6);
    public final static long VLS_FLAG_ALARM_TEMP  =	(1L<<7);

    public final static long VLS_FLAG_ALERT  	=	(1L<<8); // More alert mode, due to bump etc
    public final static long VLS_FLAG_ALARM_TIP  =	(1L<<9);
    public final static long VLS_FLAG_CONNECTED	=	(1L<<10);
    public final static long VLS_FLAG_HFCLK				=	(1L<<11);
    public final static long VLS_FLAG_CYCLE_TEST_MODE 	=	(1L<<12);
    public final static long VLS_FLAG_1S_ACCEL_LO_ACTIVITY =	(1L<<13);	// low-threshold accel activity possibly denoting riding, etc.
    public final static long VLS_FLAG_HALL_ANOMALY		=	(1L<<14);
    public final static long VLS_FLAG_ALARM_HAMMER		=	(1L<<15);
    public final static long VLS_FLAG_ALARM_JOSTLE		=	(1L<<16);
    public final static long VLS_FLAG_SIREN_ACTIVE		=	(1L<<17);	// Siren is currently active
    public final static long VLS_FLAG_INVALID_PAC		=	(1L<<18);	// PAC code is invalid
    public final static long VLS_FLAG_BAD_PAC_ENTRY		=	(1L<<19);	// Bad PAC code entered
    public final static long VLS_FLAG_BEEPER_ACTIVE		=	(1L<<20);	// Beeper is active
    public final static long VLS_FLAG_ACCEL_SUPPRESS	=	(1L<<21);	// Suppress accelerometer (due to e.g. motor travel, etc)
    public final static long VLS_FLAG_LOW_TEMP          =   (1L<<22);

    public static String flagsToString (int nLockFlags)
    {
        int nNotHandledFlags = nLockFlags;

        StringBuilder szFlags = new StringBuilder();
        if ((nLockFlags & VLS_FLAG_RADIO_EV) != 0)
        {
            szFlags.append("VF_RADIO ");
            nNotHandledFlags &= ~VLS_FLAG_RADIO_EV;
        }
        if ((nLockFlags & VLS_FLAG_DEVMODE1) != 0)
        {
            szFlags.append("VF_DEVMODE1 ");
            nNotHandledFlags &= ~VLS_FLAG_DEVMODE1;
        }
        if ((nLockFlags & VLS_FLAG_CHARGING) != 0)
        {
            szFlags.append("Charging ");
            nNotHandledFlags &= ~VLS_FLAG_CHARGING;
        }
        if ((nLockFlags & VLS_FLAG_CHARGED) != 0)
        {
            szFlags.append("Charged. ");
            nNotHandledFlags &= ~VLS_FLAG_CHARGED;
        }
        if ((nLockFlags & VLS_FLAG_BATT_LO) != 0)
        {
            szFlags.append("VF_BATT_LO ");
            nNotHandledFlags &= ~VLS_FLAG_BATT_LO;
        }
        if ((nLockFlags & VLS_FLAG_STALL) != 0)
        {
            szFlags.append("VF_STALL ");
            nNotHandledFlags &= ~VLS_FLAG_STALL;
        }
        if ((nLockFlags & VLS_FLAG_ALARM_BUMP) != 0)
        {
            szFlags.append("VF_BUMP ");
            nNotHandledFlags &= ~VLS_FLAG_ALARM_BUMP;
        }
        if ((nLockFlags & VLS_FLAG_ALARM_TEMP) != 0)
        {
            szFlags.append("VF_TEMP ");
            nNotHandledFlags &= ~VLS_FLAG_ALARM_TEMP;
        }
        if ((nLockFlags & VLS_FLAG_ALERT) != 0)
        {
            szFlags.append("VF_ALERT ");
            nNotHandledFlags &= ~VLS_FLAG_ALERT;
        }
        if ((nLockFlags & VLS_FLAG_ALARM_TIP) != 0)
        {
            szFlags.append("VF_TIP ");
            nNotHandledFlags &= ~VLS_FLAG_ALARM_TIP;
        }
        if ((nLockFlags & VLS_FLAG_CONNECTED) != 0)
        {
            szFlags.append("CONNECTED ");
            nNotHandledFlags &= ~VLS_FLAG_CONNECTED;
        }
        if ((nLockFlags & VLS_FLAG_HFCLK) != 0)
        {
            szFlags.append ("HFCLK ");
            nNotHandledFlags &= ~VLS_FLAG_HFCLK;
        }
        if ((nLockFlags & VLS_FLAG_CYCLE_TEST_MODE) != 0)
        {
            szFlags.append ("CycleTestMode ");
            nNotHandledFlags &= ~VLS_FLAG_CYCLE_TEST_MODE;
        }
        nNotHandledFlags = AddDescrAndFilter (nLockFlags, nNotHandledFlags, VLS_FLAG_1S_ACCEL_LO_ACTIVITY, "Accel Low 1s", szFlags);
        nNotHandledFlags = AddDescrAndFilter (nLockFlags, nNotHandledFlags, VLS_FLAG_HALL_ANOMALY, "HallAnomaly", szFlags);
        nNotHandledFlags = AddDescrAndFilter (nLockFlags, nNotHandledFlags, VLS_FLAG_ALARM_HAMMER, "Alarm-hammer", szFlags);
        nNotHandledFlags = AddDescrAndFilter (nLockFlags, nNotHandledFlags, VLS_FLAG_ALARM_JOSTLE, "Alarm-jostle", szFlags);
        nNotHandledFlags = AddDescrAndFilter (nLockFlags, nNotHandledFlags, VLS_FLAG_SIREN_ACTIVE, "Siren active", szFlags);
        nNotHandledFlags = AddDescrAndFilter (nLockFlags, nNotHandledFlags, VLS_FLAG_INVALID_PAC, "Invalid PAC set", szFlags);
        nNotHandledFlags = AddDescrAndFilter (nLockFlags, nNotHandledFlags, VLS_FLAG_BAD_PAC_ENTRY, "Bad PAC entered.", szFlags);
        nNotHandledFlags = AddDescrAndFilter (nLockFlags, nNotHandledFlags, VLS_FLAG_BEEPER_ACTIVE, "Beeper on.", szFlags);
        nNotHandledFlags = AddDescrAndFilter (nLockFlags, nNotHandledFlags, VLS_FLAG_ACCEL_SUPPRESS, "Accel mute.", szFlags);
        nNotHandledFlags = AddDescrAndFilter (nLockFlags, nNotHandledFlags, VLS_FLAG_LOW_TEMP, "Low temp", szFlags);


        if (nNotHandledFlags != 0)
        {
            szFlags.append(String.format("Unhandled flags: 0x%X.", nNotHandledFlags));
        }


        return szFlags.toString();
    }

    //

    /**
     * Test for the specified flag, and if found append the description to the StringBuilder and return the updated notHandledFlags
     * @param nLockFlags        Current flags
     * @param nNotHandledFlags  Bitfield of flags that have not yet been handled
     * @param testFlag          Current flag to test for
     * @param szDescription     Description to add if this flag is found
     * @param szOutputBuilder   Builder to add description to.
     * @return
     */
    private static int AddDescrAndFilter (int nLockFlags, int nNotHandledFlags, long testFlag, String szDescription, StringBuilder szOutputBuilder)
    {
        if ((nLockFlags & testFlag) != 0)
        {
            szOutputBuilder.append (" ");
            szOutputBuilder.append (szDescription);
            nNotHandledFlags &= ~testFlag;
        }
        return nNotHandledFlags;
    }
}

