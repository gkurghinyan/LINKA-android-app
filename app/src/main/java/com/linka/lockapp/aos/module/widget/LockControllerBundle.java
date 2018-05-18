package com.linka.lockapp.aos.module.widget;

import android.bluetooth.BluetoothGatt;

import com.linka.Lock.BLE.BluetoothLeQueuedService.BluetoothGattQueuedActions;
import com.linka.Lock.FirmwareAPI.Comms.LockContextPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockEncV1;
import com.linka.Lock.FirmwareAPI.LINKA_BLE_Service.BluetoothGattCharacteristicBundle;
import com.linka.lockapp.aos.module.model.Linka;
import com.linka.lockapp.aos.module.model.LinkaAccessKey;

/**
 * Created by Vanson on 5/8/2016.
 */
public class LockControllerBundle {
    public BluetoothGatt gatt;
    public BluetoothGattCharacteristicBundle bundle;
    public BluetoothGattQueuedActions actions;

    public String fwVersion = "";
    public String mLockMACAddress = null;
    public LockEncV1 mLockEnc = new LockEncV1();
    public LockContextPacket mLockContextData;

    // Adding for v2 locks
    // Redesign for v3
    public boolean isV2Lock = false;
    protected boolean bondingRequired = true;

    public void setFwVersion(String fwVersion)
    {
        this.fwVersion = fwVersion;
    }

    public String getFwVersionNumber()
    {
        if (fwVersion.equals(""))
        {
            return "";
        }
        else
        {
            String ver = fwVersion.replace("v", "");
            return ver;
        }
    }

    public void setLockMACAddress(String lockMACAddress)
    {
        this.mLockMACAddress = lockMACAddress;
    }

    public void setKey(byte[] masterKey, int keyIndex, LockEncV1.PRIV_LEVEL priv_level)
    {
        this.mLockEnc.setKey(masterKey, keyIndex, priv_level);
    }

    public void setSubkey(byte[] subKey, int keyIndex, LockEncV1.PRIV_LEVEL priv_level)
    {
        this.mLockEnc.setSubkey(subKey, keyIndex, priv_level);
    }

    public boolean setLockContextData(LockContextPacket lockContextData)
    {
        boolean isCounterChanged = false;
        if (this.mLockContextData != null)
        {
            if (this.mLockContextData.getCounter() != lockContextData.getCounter())
            {
                isCounterChanged = true;
            }
        }
        this.mLockContextData = lockContextData;
        return isCounterChanged;
    }


    /* HELPER FUNC */

    public byte[] getMACAddressByte()
    {
        if (mLockMACAddress == null)
        {
            return getMACAddressByteFromMACAddressString("00:00:00:00:00:00");
        }
        return getMACAddressByteFromMACAddressString(mLockMACAddress);
    }

    public byte[] getMACAddressByteFromMACAddressString(String mac)
    {
        String[] macAddressParts = mac.split(":");

        byte[] macAddressBytes = new byte[6];
        for(int i=0; i<6; i++){
            Integer hex = Integer.parseInt(macAddressParts[i], 16);
            macAddressBytes[i] = hex.byteValue();
        }

        return macAddressBytes;
    }

}
