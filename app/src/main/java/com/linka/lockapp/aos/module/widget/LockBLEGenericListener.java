package com.linka.lockapp.aos.module.widget;

import com.linka.Lock.FirmwareAPI.Comms.LockAckNakPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockContextPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockInfoPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockSettingPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockStatusPacket;

/**
 * Created by Vanson on 22/2/16.
 */
public interface LockBLEGenericListener {

    /* LockGattUpdateReceiver */

    public void onGattUpdateConnected(LockGattUpdateReceiver lockGattUpdateReceiver);
    public void onGattUpdateDisconnected(LockGattUpdateReceiver lockGattUpdateReceiver, int status);
    public void onGattUpdateDiscovered(LockGattUpdateReceiver lockGattUpdateReceiver);
    public void onGattUpdateBonded(LockGattUpdateReceiver lockGattUpdateReceiver);
    public void onGattUpdateStatusPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockStatusPacket lockStatusPacket);
    public void onGattUpdateSettingPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockSettingPacket lockSettingPacket);
    public void onGattUpdateContextPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockContextPacket lockContextPacket);
    public void onGattUpdateInfoPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockInfoPacket lockInfoPacket);
    public void onGattUpdateNak(LockGattUpdateReceiver lockGattUpdateReceiver, LockAckNakPacket nak);
    public void onGattUpdateAck(LockGattUpdateReceiver lockGattUpdateReceiver, LockAckNakPacket ack);
    public void onGattUpdateFirmwareVersionInfo(LockGattUpdateReceiver lockGattUpdateReceiver, String szFirmwareVersion);
    public void onGattUpdateFirmwareDebugInfo(LockGattUpdateReceiver lockGattUpdateReceiver, String szLogText);
    public void onGattUpdateReadRemoteRSSI(LockGattUpdateReceiver lockGattUpdateReceiver, int rssi);
    public void onGattBadEncPkt(LockGattUpdateReceiver lockGattUpdateReceiver, String szLogText);
}
