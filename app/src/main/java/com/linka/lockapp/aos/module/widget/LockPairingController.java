package com.linka.lockapp.aos.module.widget;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import com.linka.Lock.BLE.BluetoothLeQueuedService;
import com.linka.Lock.FirmwareAPI.Comms.LINKAGattAttributes;
import com.linka.Lock.FirmwareAPI.Comms.LockAckNakPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockContextPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockEncV1;
import com.linka.Lock.FirmwareAPI.Comms.LockInfoPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockSettingPacket;
import com.linka.Lock.FirmwareAPI.Comms.LockStatusPacket;
import com.linka.Lock.FirmwareAPI.LINKA_BLE_Service;
import com.linka.Lock.FirmwareAPI.Types.AuthState;
import com.linka.lockapp.aos.module.helpers.LogHelper;
import com.linka.lockapp.aos.module.model.Linka;

import java.util.Set;

/**
 * Created by Vanson on 21/8/2016.
 */
public class LockPairingController {

    public interface LockPairingControllerCallback
    {
        void onConnect(LockPairingController lockPairingController);
    }
    public LockPairingControllerCallback callback;

    boolean is_ble_ready;
    boolean is_device_connecting;
    boolean is_device_disconnecting;

    boolean is_device_is_just_connected = false;
    boolean is_device_is_just_auth_none = false;
    boolean is_device_is_just_auth_paired = false;
    boolean is_device_is_just_auth_complete = false;
    boolean is_device_is_just_auth_complete_can_respond = false;
    boolean is_device_is_just_auth_complete_responded = false;
    int is_device_is_just_auth_none_count = 0;
    int is_device_is_just_auth_paired_count = 0;
    boolean is_device_is_just_auth_none_warning_shown = false;
    boolean isInitContext = false;

    byte authState = AuthState.AUTH_NONE;


    Context context;
    Linka linka;
    LockGattUpdateReceiver lockGattUpdateReceiver;
    LockBLEGenericListener lockBLEGenericListener;
    BluetoothGatt bluetoothGatt = null;
    LINKA_BLE_Service.BluetoothGattCharacteristicBundle bundle = null;
    BluetoothLeQueuedService.BluetoothGattQueuedActions actions = null;

    boolean isDeinitialized = false;

    LockBLEServiceProxy lockBLEServiceProxy;
    public LockControllerBundle lockControllerBundle = new LockControllerBundle();
    public LockControllerSetEncryptionKeyLogic lockControllerSetEncryptionKeyLogic = null;


    public void doConnectDevice() {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null)
        {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice _device : pairedDevices)
            {
                if (_device.getAddress().equals(linka.lock_address))
                {
                    if (_device.getBondState() == BluetoothDevice.BOND_BONDED)
                    {
                        if (getIsDeviceDisconnected(_device))
                        {
                            lockBLEServiceProxy.unpairDevice(_device);

                            actions = new BluetoothLeQueuedService.BluetoothGattQueuedActions();
                            bluetoothGatt = lockBLEServiceProxy.connect(linka.lock_address, null, actions);

                            return;
                        }
                        else
                        {
                            return;
                        }
                    }
                }
            }
        }

        actions = new BluetoothLeQueuedService.BluetoothGattQueuedActions();
        bluetoothGatt = lockBLEServiceProxy.connect(linka.lock_address, null, actions);
    }

    public void doDisconnectDevice() {
        if (lockBLEServiceProxy != null && getBluetoothGatt() != null) {
            lockBLEServiceProxy.disconnect(getBluetoothGatt());
            lockBLEServiceProxy.close(getBluetoothGatt());
            is_device_connecting = false;
        }
    }


    public boolean doBond() {
        if (lockBLEServiceProxy != null && getBluetoothGatt() != null) {
            if (getBluetoothGatt().getDevice().getBondState() == BluetoothDevice.BOND_NONE && lockControllerBundle.bondingRequired)
            {
                return lockBLEServiceProxy.doAction_Bond(getBluetoothGatt());
            }
        }
        return false;
    }

    public void doUnbond() {
        lockBLEServiceProxy.doAction_Unbond(getBluetoothGatt());
    }

    public boolean doActivate()
    {
        return lockBLEServiceProxy.doAction_activate(lockControllerBundle);
    }


    public boolean doAction_SetEncryptionKey(byte[] keyToSet, LockControllerSetEncryptionKeyLogic.LockControllerSetEncryptionKeyCallback callback) {
        lockControllerSetEncryptionKeyLogic = new LockControllerSetEncryptionKeyLogic(lockControllerBundle, callback, lockBLEServiceProxy);
        return lockControllerSetEncryptionKeyLogic.doAction_SetEncryptionKey(keyToSet);
    }


    public boolean getIsBLEReady() {
        return is_ble_ready;
    }

    public boolean getIsDeviceConnecting() {
        return is_device_connecting;
    }

    public boolean getIsDeviceDisconnecting() {
        return is_device_disconnecting;
    }

    public boolean getIsDeviceDisconnected(BluetoothDevice device) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null)
        {
            return false;
        }
        return bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == 0;
    }

    public Linka getLinka() {
        return linka;
    }

    BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }
    LINKA_BLE_Service.BluetoothGattCharacteristicBundle getBluetoothGattBundle() {
        return bundle;
    }
    BluetoothLeQueuedService.BluetoothGattQueuedActions getBluetoothGattQueuedActions() {
        return actions;
    }

    public LockPairingController(Context context, Linka linka, LockBLEServiceProxy lockBLEServiceProxy, LockPairingControllerCallback callback) {
        this.context = context;
        this.linka = linka;
        linka.isConnected = false;
        this.lockBLEServiceProxy = lockBLEServiceProxy;
        this.callback = callback;
    }

    public void initialize(boolean autoconnect) {
        lockBLEGenericListener = new LockBLEGenericListener() {
            @Override
            public void onGattUpdateConnected(LockGattUpdateReceiver lockGattUpdateReceiver) {
                is_device_connecting = false;
                is_device_is_just_connected = true;
                isInitContext = false;
            }

            @Override
            public void onGattUpdateDisconnected(LockGattUpdateReceiver lockGattUpdateReceiver, int status) {
                if (onDisconnectCallback != null)
                {
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (bluetoothAdapter != null)
                    {
                        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                        for (BluetoothDevice _device : pairedDevices)
                        {
                            if (_device.getAddress().equals(linka.lock_address))
                            {
                                if (_device.getBondState() == BluetoothDevice.BOND_BONDED)
                                {
                                    lockBLEServiceProxy.unpairDevice(_device);
                                }
                            }
                        }
                    }

                    is_device_disconnecting = false;
                    onDisconnectCallback.onComplete();

                    deinitialize(null);
                    onDisconnectCallback = null;
                }
                else
                {
                    is_device_disconnecting = false;
                    lockBLEServiceProxy.close(getBluetoothGatt());
                    bluetoothGatt = null;
                    bundle = null;
                }
            }

            @Override
            public void onGattUpdateBonded(LockGattUpdateReceiver lockGattUpdateReceiver) {
                LogHelper.e("SUCCESSFUL", "BONDED, SHOULD FETCH SETTINGS");
            }

            @Override
            public void onGattUpdateReadRemoteRSSI(LockGattUpdateReceiver lockGattUpdateReceiver, int rssi) {
            }

            @Override
            public void onGattBadEncPkt(LockGattUpdateReceiver lockGattUpdateReceiver, String szLogText) {
                LogHelper.e("onGattBadEncPkt", "" + szLogText);
                if (lockControllerSetEncryptionKeyLogic != null && lockControllerSetEncryptionKeyLogic.m_bPendingKeyOperation)
                {
                    lockControllerSetEncryptionKeyLogic.tryAction_SetEncryptionKeyRunCallback(false);
                }
            }

            @Override
            public void onGattUpdateDiscovered(LockGattUpdateReceiver lockGattUpdateReceiver) {
                if (lockBLEServiceProxy != null && lockBLEServiceProxy.mLINKA_BLE_Service != null) {
                    bundle = lockBLEServiceProxy.mLINKA_BLE_Service.PopulateGattCharacteristics(getBluetoothGatt(), getBluetoothGattQueuedActions());
                    if (bundle != null)
                    {
                        if (lockBLEServiceProxy.mLINKA_BLE_Service.SearchAndReadCharacteristic(LINKAGattAttributes.UUID_VLSO_DATA_TX, getBluetoothGatt(), getBluetoothGattBundle(), getBluetoothGattQueuedActions()))
                        {
                            // VALID , CONNECTED!
                            lockControllerBundle.gatt = getBluetoothGatt();
                            lockControllerBundle.bundle = getBluetoothGattBundle();
                            lockControllerBundle.actions = getBluetoothGattQueuedActions();
                            lockControllerBundle.setLockMACAddress(linka.getMACAddress());
                        }
                        lockBLEServiceProxy.mLINKA_BLE_Service.SearchAndReadCharacteristic(LINKAGattAttributes.UUID_FIRMWARE_VER, getBluetoothGatt(), getBluetoothGattBundle(), getBluetoothGattQueuedActions());
                    }
                }
            }

            @Override
            public void onGattUpdateStatusPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockStatusPacket lockStatusPacket) {
            }

            @Override
            public void onGattUpdateSettingPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockSettingPacket lockSettingPacket) {
            }

            @Override
            public void onGattUpdateContextPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockContextPacket lockContextPacket) {

                // Support for v2 Lock
                // The Master Keys have changed
                // And Bonding is now optional
                switch (lockContextPacket.getEncVer())
                {
                    case LockContextPacket.ENCVER_1:
                        lockControllerBundle.isV2Lock = false;
                        lockControllerBundle.bondingRequired = true;
                        LogHelper.i("onGattUpdateContextPacketUpdated", "Lock v1 Found, Bonding REQUIRED");
                        break;
                    case LockContextPacket.ENCVER_2:
                        lockControllerBundle.isV2Lock = true;
                        lockControllerBundle.bondingRequired = false;
                        LogHelper.i("onGattUpdateContextPacketUpdated", "Lock v2 Found, Bonding NOT REQUIRED");
                        break;
                    case LockContextPacket.ENCVER_2B:
                        lockControllerBundle.isV2Lock = true;
                        lockControllerBundle.bondingRequired = true;
                        LogHelper.i("onGattUpdateContextPacketUpdated", "Lock v2 Found, Bonding REQUIRED");
                        break;
                    default:
                        lockControllerBundle.isV2Lock = false;
                        lockControllerBundle.bondingRequired = true;
                        LogHelper.i("onGattUpdateContextPacketUpdated", "Missing ENCVER, Assuming v1");
                        break;
                }

                boolean isUpdated = lockControllerBundle.setLockContextData(lockContextPacket);
                lockBLEServiceProxy.processEncryptionSettingsQueue(lockControllerBundle, "Pairing/onGattUpdateContextPacketUpdated"); // Update packet counter and process queue

                if (getBluetoothGatt() != null)
                {
                    LogHelper.i("PAIRING Ack", "Counter Updated: " + (isUpdated ? "true" : "false") + " " + getBluetoothGatt().getDevice().getBondState());
                }

                checkifReady();
            }

            @Override
            public void onGattUpdateInfoPacketUpdated(LockGattUpdateReceiver lockGattUpdateReceiver, LockInfoPacket lockInfoPacket) {

            }

            @Override
            public void onGattUpdateNak(LockGattUpdateReceiver lockGattUpdateReceiver, LockAckNakPacket nak) {

                // Update the counter value
                boolean isUpdated = false;
                if (lockControllerBundle != null && lockControllerBundle.mLockContextData != null)
                {
                    isUpdated = lockControllerBundle.mLockContextData.updateCounter(nak.getCounter());
                    lockBLEServiceProxy.processEncryptionSettingsQueue(lockControllerBundle, "Pairing/onGattUpdateNak"); // Update packet counter and process queue
                }

                LogHelper.e("PAIRING Nak", "Counter: " + nak.getCounter() + ", Can Respond: " + (is_device_is_just_auth_complete_can_respond ? "true" : "false"));
                if (getBluetoothGatt()!=null && getBluetoothGatt().getDevice()!=null) {
                    LogHelper.e("PAIRING Nak", "Counter Updated: " + (isUpdated ? "true" : "false") + " " + getBluetoothGatt().getDevice().getBondState());
                }

                lockControllerSetEncryptionKeyLogic.tryAction_SetEncryptionKeyRunCallback(false);
            }

            @Override
            public void onGattUpdateAck(LockGattUpdateReceiver lockGattUpdateReceiver, LockAckNakPacket ack) {

                // Update the counter value
                boolean isUpdated = false;
                if (lockControllerBundle != null && lockControllerBundle.mLockContextData != null)
                {
                    isUpdated = lockControllerBundle.mLockContextData.updateCounter(ack.getCounter());
                    lockBLEServiceProxy.processEncryptionSettingsQueue(lockControllerBundle, "Pairing/onGattUpdateAck"); // Update packet counter and process queue
                }

                LogHelper.e("PAIRING Ack", "Counter: " + ack.getCounter() + ", Can Respond: " + (is_device_is_just_auth_complete_can_respond ? "true" : "false"));
//                LogHelper.e("PAIRING Ack", "Counter Updated: " + (isUpdated ? "true" : "false") + " " + getBluetoothGatt().getDevice().getBondState());


                // If we are awaiting a key operation, do it now
                if (lockControllerSetEncryptionKeyLogic != null && lockControllerSetEncryptionKeyLogic.m_bPendingKeyOperation)
                {
                    lockControllerSetEncryptionKeyLogic.tryToSetEncryptionKey(
                            lockControllerSetEncryptionKeyLogic.m_PendingKeyToSet,
                            lockControllerSetEncryptionKeyLogic.m_PendingSlotToSet,
                            LockEncV1.KEY_PART.UPPER
                    );
                }
                else
                {
                    if (lockControllerSetEncryptionKeyLogic != null) {
                        lockControllerSetEncryptionKeyLogic.tryAction_SetEncryptionKeyRunCallback(true);
                    }
                }

                checkifReady();
            }

            @Override
            public void onGattUpdateFirmwareVersionInfo(LockGattUpdateReceiver lockGattUpdateReceiver, String szFirmwareVersion) {
            }

            @Override
            public void onGattUpdateFirmwareDebugInfo(LockGattUpdateReceiver lockGattUpdateReceiver, String szLogText) {
            }
        };

        lockGattUpdateReceiver = new LockGattUpdateReceiver(context, lockBLEServiceProxy, lockBLEGenericListener, linka);
        lockGattUpdateReceiver.onResume();

        if (autoconnect) {
            doConnectDevice();
        }
    }

    void checkifReady()
    {
        if (lockBLEServiceProxy == null || getBluetoothGatt() == null) {
            return;
        }

        if (getBluetoothGatt().getDevice().getBondState() == BluetoothDevice.BOND_NONE && lockControllerBundle.bondingRequired)
        {
            doBond();
            return;
        }

        if (lockControllerBundle.mLockContextData == null)
        {
            return;
        }

        if (callback != null)
        {
            callback.onConnect(this);
            callback = null;
        }
    }

    public void deinitialize(OnDisconnectCallback callback) {
        if (callback != null)
        {
            if (lockBLEServiceProxy != null) {
                onDisconnectCallback = callback;
                lockBLEServiceProxy.disconnect(getBluetoothGatt());
            }
        }
        else
        {
            if (lockGattUpdateReceiver != null) {
                lockGattUpdateReceiver.onPause();
            }
            if (lockBLEServiceProxy != null) {
                lockBLEServiceProxy.disconnect(getBluetoothGatt());
                lockBLEServiceProxy.close(getBluetoothGatt());
            }
            lockBLEGenericListener = null;
            lockGattUpdateReceiver = null;
            isDeinitialized = true;
        }
    }

    public interface OnDisconnectCallback
    {
        void onComplete();
    }
    public OnDisconnectCallback onDisconnectCallback;
}
