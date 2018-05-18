package com.linka.lockapp.aos.module.widget;

import com.linka.Lock.FirmwareAPI.Comms.LockEncV1;
import com.linka.lockapp.aos.module.helpers.LogHelper;

/**
 * Created by Vanson on 5/8/2016.
 */
public class LockControllerSetEncryptionKeyLogic {


    public boolean m_bPendingKeyOperation = false;
    public byte[] m_PendingKeyToSet = null;
    public int m_PendingSlotToSet = 0;
    public LockControllerSetEncryptionKeyCallback m_bPendingKeyOperationCallback = null;


    public LockControllerBundle bundle;
    public LockBLEServiceProxy lockBLEServiceProxy;

    public LockControllerSetEncryptionKeyLogic(LockControllerBundle bundle, LockControllerSetEncryptionKeyCallback callback, LockBLEServiceProxy lockBLEServiceProxy)
    {
        this.bundle = bundle;
        this.m_bPendingKeyOperationCallback = callback;
        this.lockBLEServiceProxy = lockBLEServiceProxy;
    }


    public interface LockControllerSetEncryptionKeyCallback
    {
        void onComplete(boolean ret);
    }



    public void tryAction_SetEncryptionKeyRunCallback(boolean ret) {
        LogHelper.e("SetEncryptionKeyRunCallback", ret ? "true" : "false");
        if (m_bPendingKeyOperationCallback != null) {
            m_bPendingKeyOperationCallback.onComplete(ret);
            m_bPendingKeyOperationCallback = null;
        }
    }


    public boolean doAction_SetEncryptionKey(byte[] keyToSet)
    {
        return tryToSetEncryptionKey(keyToSet, LockEncV1.KEY_PART.LOWER);
    }

    public boolean tryToSetEncryptionKey(byte[] keyToSet, LockEncV1.KEY_PART part)
    {
        int slotToSet_int = bundle.mLockEnc.getNeighbourKeyIndex();
        return tryToSetEncryptionKey(keyToSet, slotToSet_int, part);
    }

    public boolean tryToSetEncryptionKey(byte[] keyToSet, int slotToSet_int, LockEncV1.KEY_PART part)
    {
        LockEncV1.KEY_SLOT slotToSet = LockEncV1.KEY_SLOT.SLOT1;
        if (slotToSet_int == 1) {
            slotToSet = LockEncV1.KEY_SLOT.SLOT2;
        }

        boolean ret = lockBLEServiceProxy.tryToSetEncryptionKey(keyToSet, slotToSet, part, bundle);

        if (!ret) {
            m_bPendingKeyOperation = false;
            m_PendingKeyToSet = null;
            m_PendingSlotToSet = 0;
            this.tryAction_SetEncryptionKeyRunCallback(false);
        } else {
            if (part == LockEncV1.KEY_PART.UPPER) {
                LogHelper.e("SetEncryptionKey", "[UPPER]");
                m_bPendingKeyOperation = false;
                m_PendingKeyToSet = null;
                m_PendingSlotToSet = 0;
            } else {
                LogHelper.e("SetEncryptionKey", "[LOWER]");
                m_bPendingKeyOperation = true;
                m_PendingKeyToSet = keyToSet;
                m_PendingSlotToSet = slotToSet_int;
            }
        }
        return ret;
    }
}
