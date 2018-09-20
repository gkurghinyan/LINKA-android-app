package com.linka.Lock.FirmwareAPI.Comms;

/**
 * Created by Loren-Admin on 7/11/2016.
 *
 * Context packet with the following format:
 *
 typedef struct __attribute__((packed)) {
     uint8_t length;
     uint8_t cmd;		// CMD is VCMD_CONTEXT
     uint8_t encver;		// Encryption version, allowing upgrade of encryption algorithms
     uint32_t counter;
     uint16_t k1CRC;
     uint16_t k2CRC;
        // added 0.73:
     uint8_t roll
     uint8_t pitch
     uint8_t ucBondsAvailAndIndex;	// 14	(Number of available bonds << 4) | Current Index (4 bits each)
     uint16_t CRC;			//16b
 } VLSO_CONTEXT_PKT;
 *
 */
public class LockContextPacket extends LockDataPacket {

    public static final int ENCVER_BONDINGREQ  = 0x01; // LSB indicates that bonding is required
    public static final int ENCVER_1  = 0x00; // First encryption scheme, deployed 2015-16
    public static final int ENCVER_2  = 0x02; // Second encryption scheme, deployed 2017
    public static final int ENCVER_2B  = (ENCVER_2|ENCVER_BONDINGREQ); // Second encryption scheme, deployed 2017
    public static final int ENCVER_3  = 0x04;
    private final static int MIN_CONTEXT_PKT_LEN = 12;
    private int mLength;
    private int mCmd;
    private int mEncVer;
    private int mCounter;
    private short mk1CRC;
    private short mk2CRC;
    private int m_roll;
    private int m_pitch;
    private int mCRC;
    private int mBondsRemaining;
    private int mCurrentBondIndex;
    private boolean m_bValidPkt = false;


    public boolean updateCounter(int newCounter ) {
        boolean isChanged = false;
        if (mCounter != newCounter)
        {
            isChanged = true;
        }
        mCounter = newCounter;
        return isChanged;
    };

    public short getMk1CRC(){
        return mk1CRC;
    }

    public short getMk2CRC(){
        return mk2CRC;
    }

    public int getEncVer() { return (mEncVer); }      // NOTE: LSB indicated bonding requirement, so V1 encryption is 0x00, V2 is 0x02, etc.
    public boolean getBondingRequired() { return ((mEncVer&0x01) == 0x01); }
    public int getCounter() { return mCounter; };
    public LockContextPacket (byte[] pkt)
    {
        super (pkt);

        if (pkt.length >= MIN_CONTEXT_PKT_LEN)
        {
            mLength = pkt[0];
            mCmd = pkt[1];
            mEncVer = pkt[2];
            mCounter = LockAdV1.getLongFromBytes(pkt, 3);
            mk1CRC = LockAdV1.getShortFromBytesLittleEndian(pkt, 7);
            mk2CRC = LockAdV1.getShortFromBytesLittleEndian(pkt, 9);

            if (pkt.length > 12) {
                m_roll = pkt[11];
                m_pitch = pkt[12];
            }
            else
            {
                m_roll = 0xFF;
                m_pitch = 0xFF;
            }

            if (pkt.length > 14) {
                mBondsRemaining = (pkt[13] >> 4) & 0x0F;
                mCurrentBondIndex = pkt[13] & 0x0F;
            }
            else
            {
                mBondsRemaining = 0xFF; // Unavailable, old firmware
                mCurrentBondIndex = 0xFF; // Unavailable, old firmware
            }

            mCRC = LockAdV1.getShortFromBytesLittleEndian(pkt, pkt.length - 2);

            m_bValidPkt = checkPacketCRC(pkt);
        }
    }

    // Quick and easy way to determine if bonding is required for upstream components
    public boolean isBondingRequired(){
        if ((getEncVer() & ENCVER_BONDINGREQ) == ENCVER_BONDINGREQ) {
            return true;
        } else {
            return false;
        }
    }

    public String toString ()
    {
        StringBuilder builder = new StringBuilder();

        if (m_bValidPkt) {
            builder.append(String.format("LCP v %d: ctr %d, CRC1 0x%X CRC2 0x%X. Roll %d Pitch %d\r\nCurBond %d remaining %d", mEncVer, mCounter,
                                mk1CRC, mk2CRC, m_roll, m_pitch, mCurrentBondIndex, mBondsRemaining));
            switch (getEncVer())
            {
                case ENCVER_1:
                    builder.append("\r\nEncVer1, ");
                    break;
                case ENCVER_2:
                    builder.append("\r\nEncVer2, ");
                    break;
                case ENCVER_2B:
                    builder.append("\r\nEncVer2B, ");
                    break;

                case ENCVER_3:
                    builder.append("\r\nEncVer3, ");
                    break;
                default:
                    builder.append("\r\nEncVer ???, ");
                    break;
            }

            if ((getEncVer() & ENCVER_BONDINGREQ) == ENCVER_BONDINGREQ) {
                builder.append("Bonding required. \r\n");
            } else {
                builder.append ("Bonding not required. \r\n");
            }
        }
        else
        {
            builder.append("Invalid LockContextPacket:");
            builder.append(String.format("LCP v %d: ctr %d, CRC1 0x%X CRC2 0x%X.", mEncVer, mCounter, mk1CRC, mk2CRC));
        }
        return builder.toString();
    }

}
