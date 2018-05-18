package com.linka.Lock.FirmwareAPI.Types;

/**
 * Created by Loren-Admin on 8/27/2015.
 */
public class SelfTestFlags {
    public final static long  VLT_TEST_EEPROM	=		(1L<<0);
    public final static long  VLT_TEST_ACCEL	=		(1L<<1);
    public final static long  VLT_TEST_MOTOR_DRIVER =	(1L<<2);
    public final static long  VLT_TEST_I2CBUSS	=	(1L<<3);
    public final static long  VLT_TEST_RADIO	=		(1L<<4);
    public final static long  VLT_TEST_ENCRYP	=		(1L<<5);
    public final static long  VLT_TEST_CRC		=	(1L<<6);
    public final static long  VLT_TEST_BOOTLOADER_PRESENT	= (1L<<7);
    public final static long  VLT_TEST_SETTINGS   = (1L<<8);
    public final static long  VLT_TEST_BONDTABLE = 		(1L<<9);

    public static String flagsToString (int nLockFlags)
    {
        int nNotHandledFlags = nLockFlags;

        StringBuilder szFlags = new StringBuilder();
        if ((nLockFlags & VLT_TEST_EEPROM) != 0)
        {
            szFlags.append("VLT_TEST_EEPROM ");
            nNotHandledFlags &= ~VLT_TEST_EEPROM;
        }
        if ((nLockFlags & VLT_TEST_ACCEL) != 0)
        {
            szFlags.append("VLT_TEST_ACCEL ");
            nNotHandledFlags &= ~VLT_TEST_ACCEL;
        }
        if ((nLockFlags & VLT_TEST_MOTOR_DRIVER) != 0)
        {
            szFlags.append("VLT_TEST_MOTOR_DRIVER ");
            nNotHandledFlags &= ~VLT_TEST_MOTOR_DRIVER;
        }
        if ((nLockFlags & VLT_TEST_I2CBUSS) != 0)
        {
            szFlags.append("VLT_TEST_I2CBUSS ");
            nNotHandledFlags &= ~VLT_TEST_I2CBUSS;
        }
        if ((nLockFlags & VLT_TEST_RADIO) != 0)
        {
            szFlags.append("VLT_TEST_RADIO ");
            nNotHandledFlags &= ~VLT_TEST_RADIO;
        }
        if ((nLockFlags & VLT_TEST_ENCRYP) != 0)
        {
            szFlags.append("VLT_TEST_ENCRYP ");
            nNotHandledFlags &= ~VLT_TEST_ENCRYP;
        }
        if ((nLockFlags & VLT_TEST_CRC) != 0)
        {
            szFlags.append("VLT_TEST_CRC ");
            nNotHandledFlags &= ~VLT_TEST_CRC;
        }
        if ((nLockFlags & VLT_TEST_BOOTLOADER_PRESENT) != 0)
        {
            szFlags.append("Bootloader not present ");
            nNotHandledFlags &= ~VLT_TEST_BOOTLOADER_PRESENT;
        }
        if ((nLockFlags & VLT_TEST_SETTINGS) != 0)
        {
            szFlags.append("Setting(s) defaulted ");
            nNotHandledFlags &= ~VLT_TEST_SETTINGS;
        }
        if ((nLockFlags & VLT_TEST_BONDTABLE) != 0)
        {
            szFlags.append("Bondtable ");
            nNotHandledFlags &= ~VLT_TEST_BONDTABLE;
        }


        if (nNotHandledFlags != 0)
        {
            szFlags.append(String.format("Unhandled flags: 0x%X.", nNotHandledFlags));
        }


        return szFlags.toString();
    }

}
