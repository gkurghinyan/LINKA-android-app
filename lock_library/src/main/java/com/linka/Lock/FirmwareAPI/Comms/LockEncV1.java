package com.linka.Lock.FirmwareAPI.Comms;

import android.util.Log;

import com.linka.Lock.Utility.DebugHelper;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//import org.apache.commons.codec.binary.Hex;

/**
 * Created by Loren-Admin on 7/11/2016.
 */
public class LockEncV1 {

    private static Object lock = new Object(); // Semaphore

    public static byte[] dataWithHexString(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    // Security through obscurity
    // Not great, but it's a minimal guard against someone who obtains the raw key
    // transmitted back to the server
    public static byte[] unobscureKey(String obscuredKey) {
        String key = "";
        for (int i = 0; i < obscuredKey.length(); i+= 2)
        {
            String hexByte = obscuredKey.substring(i, i+2);
            int value = Integer.parseInt(hexByte, 16);
            int inverse = 0xff - value;
            String hex = Integer.toHexString(inverse);
            if (hex.length() < 2) {
                hex = '0' + hex; // pad with leading zero if needed
            }
            key += hex;
        }
        return dataWithHexString(key);
    }

    private static byte[] linka_v1_salt_v1 = { (byte)0xf9, (byte)0x85, (byte)0xde, (byte)0xef, (byte)0xec, (byte)0xec, 0x2d, (byte)0xf4, (byte)0xc7, (byte)0x9c, (byte)0x91, (byte)0xe2, 0x31, 0x4b, (byte)0x84, 0x66 };
    private static byte[] linka_v1_salt_v2 =  { 0x7c, 0x23, 0x50, 0x22, 0x76, 0x62, (byte)0x80, (byte)0xbd, 0x6b, 0x7a, 0x5b, (byte)0x99, (byte)0xca, (byte)0x9b, 0x5a, 0x29  };

    public static String hexStringFromData(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for(byte b: data)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }




    public enum PRIV_LEVEL { PRIV_ADMIN, PRIV_USER, PRIV_NONE };
    static final int ENC_PKT_LEN16 = 16; // All encrypted payloads are 16b
    static final int ENC_KEY_LEN16 = 16; // AES128 key length, 16bytes
    static final int CRC16_LENGTH = 2;   // CRC16 is two bytes

    private String TAG = LockEncV1.class.getSimpleName();

    public LockEncV1()
    {

    }

    public enum KEY_PART {
        LOWER,
        UPPER,
    };

    public enum KEY_SLOT {
        SLOT1,
        SLOT2,
    }

    public boolean useMaster = false;
    public boolean useSubkey = false;

    private byte[] masterKey;
    private byte[] subKey;

    public int keyIndex = 0;
    public PRIV_LEVEL priv_level = PRIV_LEVEL.PRIV_NONE;

    public void setKey(byte[] masterKey, int keyIndex, PRIV_LEVEL priv_level)
    {
        /*
        // Logged to ensure master key wasn't kept in memory longer than necessary
        if (this.masterKey != null) {
            LogHelper.e("MASTERKEY", "KEY WAS " + hexStringFromData(this.masterKey) + " AND IS NOW " + hexStringFromData(masterKey));
        } else { LogHelper.e("MASTERKEY", "KEY WAS NULL AND IS NOW " + hexStringFromData(masterKey)); }
        */
        this.useMaster = true;
        this.useSubkey = false;
        this.masterKey = masterKey;
        this.keyIndex = keyIndex;
        this.priv_level = priv_level;
        //Log.e("LOCKENCV1","MASTER KEY SET TO " + hexStringFromData(masterKey));
    }

    public boolean isKeySet() {
        if (this.subKey == null && this.masterKey == null) {
            return false;
        }
        return true;
    }

    public void setSubkey(byte[] subKey, int keyIndex, PRIV_LEVEL priv_level)
    {
        this.useMaster = false;
        this.useSubkey = true;
        this.subKey = subKey;
        this.keyIndex = keyIndex;
        this.priv_level = priv_level;
        //Log.e("LOCKENCV1","SUB KEY SET TO " + hexStringFromData(subKey));
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public int getNeighbourKeyIndex() {
        switch (keyIndex) {
            case 0:
                return 1;
            case 1:
                return 0;
            default:
                break;
        }
        return 0;
    }

    public byte[] CreateEncryptedPacket (byte[] MACaddr, int command, int subcommand, LockContextPacket context) {
        byte[] payload = new byte[2];
        payload[0] = (byte)command;
        payload[1] = (byte)subcommand;
        return CreateEncryptedPacket(MACaddr, payload, context);
    }

    public byte[] CreateEncryptedSetSettingPacket (byte[] MACaddr, int settingIndex, int settingValue, LockContextPacket context) {
        LockSettingPacket setting = new LockSettingPacket(LockCommand.VCMD_SET_SETTING, settingIndex, settingValue, 0);
        return CreateEncryptedPacket(MACaddr, setting.getPayload(), context);
    }

    /**
     *
     * @param MACaddr   - our MAC, for encryption of the packet
     * @param part
     * @param context
     * @return
     */
    public byte[] CreateEncryptedSetKeySettingPacket (byte[] MACaddr, KEY_SLOT targetKeySlot, byte[] keyToSet, KEY_PART part, LockContextPacket context) {

        byte[] keypart = new byte[8];
        int keyslot = 0;

        if (part == KEY_PART.LOWER)
        {
            keypart = Arrays.copyOfRange(keyToSet, 0, 8); // First 8 bytes are the "lower" part
            if (targetKeySlot == KEY_SLOT.SLOT1)
            {
                keyslot = LockSettingPacket.VLSO_SETTING_MK1_0;
            }
            else if (targetKeySlot == KEY_SLOT.SLOT2)
            {
                keyslot = LockSettingPacket.VLSO_SETTING_MK2_0;
            }
        }
        else if (part == KEY_PART.UPPER)
        {
            keypart = Arrays.copyOfRange(keyToSet, 8, 16);
            if (targetKeySlot == KEY_SLOT.SLOT1)
            {
                keyslot = LockSettingPacket.VLSO_SETTING_MK1_1;
            }
            else if (targetKeySlot == KEY_SLOT.SLOT2)
            {
                keyslot = LockSettingPacket.VLSO_SETTING_MK2_1;
            }
        }



        LockSettingPacket setting = new LockSettingPacket(LockCommand.VCMD_SET_SETTING, keyslot, keypart);
        return CreateEncryptedPacket(MACaddr, setting.getPayload(), context);
    }

    public byte[] CreateEncryptedGetSettingPacket (byte[] MACaddr, int settingIndex, LockContextPacket context) {
        LockSettingPacket setting = new LockSettingPacket(LockCommand.VCMD_GET_SETTING, settingIndex, 0, 0);
        return CreateEncryptedPacket(MACaddr, setting.getPayload(), context);
    }


    /**
     *
     * @param MACaddr
     * @param payload
     * @param context
     * @return
     */
    private byte[] CreateEncryptedPacket (byte[] MACaddr, byte[] payload, LockContextPacket context) {

        if (context == null) {
            Log.e("Error", "Error Creating Encrypted Packet: LockContextPacket == nil");
            return null;
        }

        if (MACaddr == null) {
            Log.e("Error", "Error Creating Encrypted Packet: MACAddr == nil");
            return null;
        }

        if (payload == null) {
            Log.e("Error", "Error Creating Encrypted Packet: payload == nil");
            return null;
        }

        if (useMaster && (masterKey == null || masterKey.length < 16)) {
            Log.e("Error", "Error Creating Encrypted Packet: useMaster = true but masterKey == nil");
            return null;
        }

        if (useSubkey && (subKey == null || subKey.length < 16)) {
            Log.e("Error", "Error Creating Encrypted Packet: useSubkey = true but subKey == nil");
            return null;
        }



        byte[] wrapperPkt = new byte[20];   // Outer packet, format is <LEN><KEYINFO><ENCPKT[16]><CRC>
        byte[] encPkt = new byte[ENC_PKT_LEN16];
        byte[] subkey = null;

        // Zero packets
        Arrays.fill(wrapperPkt, (byte) 0);

        // First 6 bytes of IV are MAC address, also used in the subkey generation. Reversed from the network order
        byte[] revMAC = new byte[6];
        for (int i = 0; i < 6; i++) {
            revMAC[5 - i] = MACaddr[i];
        }


        if (useMaster) {
            if (priv_level == PRIV_LEVEL.PRIV_ADMIN) {
                subkey = GetSubkey(this.masterKey, keyIndex, revMAC, 0, context.getEncVer());
            } else if (priv_level == PRIV_LEVEL.PRIV_USER) {
                subkey = GetSubkey(this.masterKey, keyIndex, revMAC, 1, context.getEncVer());
            } else if (priv_level == PRIV_LEVEL.PRIV_NONE) {
                subkey = new byte[ENC_PKT_LEN16];
            } else {
                return null;
            }
        } else if (useSubkey) {
            subkey = this.subKey;
        }


        // Outer packet has <LEN><KEYINFO><ENCPKT[16]><CRC>
        wrapperPkt[0] = 20;

        // Second byte has highest bit set to indicate that it's an encrypted packet (otherwise this is the command byte)
        wrapperPkt[1] |= (byte) 0x80;

        // Key index is stored in secondmost upper bit of keyinfo byte. This is the slot that the relevant key is stored in the lock.
        if (keyIndex == 1) {
            wrapperPkt[1] |= ((byte) 0x40);
        }

        // Priv/subkey index is stored in bits 6:5, but at this point we only have ADMIN (0) and USER (1)
        if (priv_level == PRIV_LEVEL.PRIV_USER) {
            wrapperPkt[1] |= ((byte) 0x10);
        }

        // now create the inner packet and encrypt it.
        encPkt[0] = ENC_PKT_LEN16;
        // Copy the payload
        System.arraycopy(payload, 0, encPkt, 1, payload.length);

    /* Now we do this at the top to allow for different sized payloads
    // Fill the rest of the packet with random values
    Random rand = new Random();
    for (int i = 3; i < (encPkt.length - CRC16_LENGTH); i++) {
        encPkt[i] = (byte) rand.nextInt(256);
    }
    */

        // Update the inner CRC
        LockDataPacket.updatePacketCrc(encPkt);

        // Create the IV and encrypt the packet
        byte[] IV = new byte[16];
        //System.arraycopy(src, srcpos, dst, dstpos, length);
        System.arraycopy(revMAC, 0, IV, 0, 6);
        // Next 4 bytes are the counter value
        int counter = context.getCounter();
        IV[9] = (byte) ((counter >> 24) & 0xFF);
        IV[8] = (byte) ((counter >> 16) & 0xFF);
        IV[7] = (byte) ((counter >> 8) & 0xFF);
        IV[6] = (byte) ((counter >> 0) & 0xFF);
        // Lower 4 bytes (0-3) currently unused; could be enhanced.

        // Encrypt the packet
        byte[] encryptedEncPkt = CBC_EncryptBlock(subkey, IV, encPkt);

        // Copy the encrypted packet into the wrapper packet
        System.arraycopy(encryptedEncPkt, 0, wrapperPkt, 2, ENC_PKT_LEN16);

        // Update the outer CRC of the wrapper packet
        LockDataPacket.updatePacketCrc(wrapperPkt);
/*
        Log.e(TAG, "Encr key:" + DebugHelper.dumpByteArray(masterKey));
        Log.e(TAG, "Encr subkey:" + DebugHelper.dumpByteArray(subkey));
        Log.e(TAG, "Encr MAC:" + DebugHelper.dumpByteArray(MACaddr));
        Log.e(TAG, "Encr revMAC:" + DebugHelper.dumpByteArray(revMAC));
        Log.e(TAG, "Encr IV:" + DebugHelper.dumpByteArray(IV));
        Log.e(TAG, "Encr subK:" + DebugHelper.dumpByteArray(subkey));
        Log.e(TAG, "clear pkt:" + DebugHelper.dumpByteArray(encPkt));
        Log.e(TAG, "Encr pkt:" + DebugHelper.dumpByteArray(encryptedEncPkt));
        Log.e(TAG, "wrapper pkt:" + DebugHelper.dumpByteArray(wrapperPkt));
*/
        return wrapperPkt;

    }

    /*
    byte[] CreateEncPkt (int command)
    {

    }
*/
    private byte[] GetSubkey (byte[] key, int key_index, byte[] lock_MAC,  int subkey_index, int enc_ver) {
        /*
        Subkey construction is:
        IV: 0: version
            1: key index
            2: 0x11 for keyset 1, 0x22 for keyset 2
            3-9: preset IV
            10-15: lock MAC
         */
        byte[] subIV =  { 0x00, 0x00, 0x00, (byte)0xb2, (byte)0x9c, 0x2d, 0x53, (byte)0xf6, (byte)0xca, (byte)0xeb, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };


        byte[] salt;
        switch (enc_ver)
        {
            case LockContextPacket.ENCVER_1:
            case LockContextPacket.ENCVER_BONDINGREQ:
                subIV[0] = (byte)enc_ver;
                salt = linka_v1_salt_v1;
                break;
            case LockContextPacket.ENCVER_2:
            case LockContextPacket.ENCVER_2B:
                subIV[0] = (byte)LockContextPacket.ENCVER_2;    // Don't include the bonding flag, since it can be changed via settings
                salt = linka_v1_salt_v2;
                break;
            default:
                // Error, unsupported encryption version
                Log.e(TAG, String.format("Error, unsupported encryption version 0x%X", enc_ver));
                salt = null;
                break;
        }

        subIV[1] = (byte)subkey_index;
        if (key_index == 0)
            subIV[2] = 0x11;
        else if (key_index == 1)
            subIV[2] = 0x22;

        System.arraycopy(lock_MAC, 0, subIV, 10, 6);

        // Now that we have the key and IV, we can create the subkey
        return CBC_EncryptBlock(key, subIV, salt);
    }


    private byte[] CBC_EncryptBlock (byte[] key, byte[] IV, byte[] input)
    {
        Cipher aesCBC = null;
        try {
            aesCBC = Cipher.getInstance("AES/CBC/NOPADDING");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        SecretKey aesKey = new SecretKeySpec(key, "AES");
        IvParameterSpec aesIV = new IvParameterSpec(IV);

        try {
            aesCBC.init(Cipher.ENCRYPT_MODE, aesKey, aesIV);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        byte[] result = new byte[0];
        try {
            result = aesCBC.doFinal(input);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return result;
    }
}
