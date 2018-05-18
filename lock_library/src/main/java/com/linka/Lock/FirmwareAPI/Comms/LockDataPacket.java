package com.linka.Lock.FirmwareAPI.Comms;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by Loren-Admin on 5/14/2015.
 * This class is the base class for any communications packet
 * to/from the lock, including status packet, info packet, etc.
 * All packets have the same general structure:
 * <LENGTH><CMD><DATA 0><DATA 1><DATA ...><CRC>
 * where <LENGTH> is the overall length (including length and CRC)
 */
public class LockDataPacket {
    private final static String TAG = LockDataPacket.class.getSimpleName();

    protected LockCommand m_Cmd;
    protected boolean m_bValid = false;
    protected boolean m_bCRCValid = false;
    protected byte m_nLength;
    protected byte[] m_PayloadData;
    protected byte[] m_PacketData;  // Full packet data
    protected final byte MIN_VALID_PKTLEN = 4;    // Length, command, CRC16
    protected int m_CRC16;
    /**
     * Create a lock data packet with the supplied command and data
     * @param cmd
     * @param data
     */
    public LockDataPacket (byte cmd, byte[] data)
    {
        m_Cmd = new LockCommand(cmd);
        if (data != null) {
            m_nLength = (byte)(MIN_VALID_PKTLEN + data.length);
            m_PayloadData = new byte[data.length];
            System.arraycopy(data, 0, m_PayloadData, 0, data.length);
        }
        else
        {
            m_nLength = MIN_VALID_PKTLEN;
            m_PayloadData = null;
        }
        m_PacketData = new byte[m_nLength];

        m_PacketData[0] = m_nLength;
        m_PacketData[1] = m_Cmd.GetValue();
        setPacketCrc(calcPacketCrc());
        m_bValid = checkPacketCRC(m_PacketData);
    }

    protected void updateCRC()
    {
        setPacketCrc(calcPacketCrc());
        m_bValid = checkPacketCRC(m_PacketData);
    }

    public boolean isValid()
    {
        return m_bValid;
    }

    public static final int INVALID_CALC_CRC = 0x10000;
    public static final int INVALID_RXD_CRC = 0x20000;
    public int calcPacketCrc ()
    {
        int crc = INVALID_CALC_CRC;
        if (m_PacketData != null)
        {
            crc = CRC16CCITT.calculateCrc(m_PacketData, m_PacketData.length - CRC16CCITT.CRC_LENGTH_BYTES);
        }
        return crc;
    }

    public static int calcPacketCrc (byte[] packet)
    {
        int crc = INVALID_CALC_CRC;
        if (packet != null)
        {
            crc = CRC16CCITT.calculateCrc(packet, packet.length - CRC16CCITT.CRC_LENGTH_BYTES);
        }
        return crc;
    }

    public static void updatePacketCrc (byte[] pkt)
    {
        if (pkt != null)
        {
            int crc = calcPacketCrc(pkt);
            pkt[pkt.length - 1] = (byte)(crc & 0xFF);
            pkt[pkt.length - 2] = (byte)((crc >> 8) & 0xFF);
        }
    }

    public void setPacketCrc (int crc)
    {
        if (m_PacketData != null)
        {
            m_PacketData[m_PacketData.length - 1] = (byte)(crc & 0xFF);
            m_PacketData[m_PacketData.length - 2] = (byte)((crc >> 8) & 0xFF);
        }
    }

    // \TODO fix some sort of sign extension issue here
    public int getPacketCrc ()
    {
        int crc = INVALID_RXD_CRC;
        if (m_PacketData != null)
        {
            crc = (m_PacketData[m_PacketData.length - 1] | (m_PacketData[m_PacketData.length - 2]<<8)) & 0xFFFF;
        }
        return crc;
    }

    public static int getPacketCrc (byte[] packet)
    {
        int crc = INVALID_RXD_CRC;
        if (packet != null)
        {
            crc = (0xFF & (packet[packet.length - 1]) | ((0xFF & packet[packet.length - 2])<<8)) & 0xFFFF;
        }
        return crc;
    }

    protected static boolean checkPacketCRC(byte[] packet)
    {
        // Checks the CRC-16 of the packet, returns true if ok.
        int calcdCrc = calcPacketCrc(packet);
        int pktCrc = getPacketCrc(packet);
        if (calcdCrc != pktCrc)
        {
            Log.d(TAG, String.format("CRC mismatch, calc 0x%X, pkt 0x%X.", calcdCrc, pktCrc));
        }
        return (calcdCrc == pktCrc);
    }


    /**
     * Get the command type, discarding the ACK bit.
     * @return command type without the ACK bit
     */
    public LockCommand getCmdType ()
    {
        return m_Cmd;
    }

    /**
     * Get the byte array representing the data packet.
     * \note this method will update the CRC before returning
     * the byte array.
     * @return Byte array representing the raw packet data.
     */
    public byte[] getData()
    {
        /*
        if (m_bValid)
        {
            // Create the packet byte array
            byte[] packet = new byte[m_nLength];
            packet[0] = m_nLength;
            packet[1] = m_Cmd;
            if (m_nLength > MIN_VALID_PKTLEN)
            {
                System.arraycopy(m_PayloadData, 0, packet, 2, m_PayloadData.length);
            }
            packet[m_nLength - 2] = (byte)((m_CRC16 >> 8) & 0xFF);
            packet[m_nLength - 1] = (byte)(m_CRC16 & 0xFF);
            return packet;
        }
        else
            return null;
            */
        return m_PacketData;
    }

    /**
     * Get just the payload of the packet
     * @return
     */
    public byte[] getPayload() {
        return Arrays.copyOfRange(m_PacketData, 1, m_PacketData.length - 3);
    }

    /**
     * Parse data stream of bytes to command
     * Format is:
     * <length><command><data....><CRC16>
     * @param data
     */
    public LockDataPacket(byte[] data)
    {
        if (data.length != data[0])
        {
            // Invalid packet
            m_bValid = false;
            Log.e(TAG, "Bad length for received packet.");
        }
        else if (data.length >= MIN_VALID_PKTLEN)
        {
            // parse the packet
            m_nLength = data[0];
            m_Cmd = new LockCommand(data[1]);
            m_bCRCValid = checkPacketCRC(data);
            m_bValid = checkPacketCRC(data);


            if (data.length > MIN_VALID_PKTLEN)     // If there is data (valid to have a command packet with no data
            {
                m_PayloadData = new byte[data.length - MIN_VALID_PKTLEN];
                System.arraycopy(data, 2, m_PayloadData, 0, data.length - MIN_VALID_PKTLEN);
                m_PacketData = new byte[data.length];   // PacketData is a full copy of the packet
                System.arraycopy(data, 0, m_PacketData, 0, data.length);
            }
            else
            {
                m_PayloadData = null;
                m_PacketData = null;
                m_bValid = false;
            }
        }
        else
        {
            // Invalid packet, too short
            m_bValid = false;
        }
    }





    public String toString ()
    {
        StringBuilder builder = new StringBuilder();

//        if (!m_bValid)
//        {
//            builder.append("[Invalid packet]");
//        }
//        else
        {
            if (m_bValid)
                builder.append("Valid ");
            else
                builder.append("Invalid ");
            builder.append(String.format("pkt of len %d, CRC 0x%X ", m_nLength, m_CRC16));
            if (!m_bCRCValid)
                builder.append("CRC invalid");

            builder.append (" command ");
            LockCommand cmd = m_Cmd;
            builder.append (cmd.toString());
            /*
            switch (m_Cmd & CMD_MASK)
            {
                builder.append (m_Cm)
                case CMD_TYPE.CMD_IA_COMMAND:
                    builder.append("CHALLENGE_A");
                    break;
                case CMD_TYPE.CMD_FA_COMMAND:
                    builder.append("CHALLENGE_B");
                    break;
                case CMD_TYPE.CMD_GET_INFO:
                    builder.append("GET_INFO");
                    break;
                case CMD_GET_STATUS:
                    builder.append("GET_STATUS");
                    // For the status packet, byte 0 (after len, cmd) is the state \todo make class for these packet types
                    if (m_PayloadData != null) {
                        builder.append(" \r\nLock state: ");
                        switch (m_PayloadData[0])
                        {
                            case LOCK_LOCKED:
                                builder.append("LOCK_LOCKED");
                                break;
                            case LOCK_UNLOCKING:
                                builder.append("LOCK_UNLOCKING");
                                break;
                            case LOCK_UNLOCKED:
                                builder.append("LOCK_UNLOCKED");
                                break;
                            case LOCK_LOCKING:
                                builder.append("LOCK_LOCKING");
                                break;
                            default:
                                builder.append (String.format("ERROR: unknown state %d.", m_PayloadData[0]));
                                break;

                        }
                        // @todo parse properly with LockStatusPacket.java

                        builder.append(String.format(" state %d.", m_PayloadData[0]));
                        builder.append(String.format(" batt %.2fV (%d%%)", (m_PayloadData[2] + (m_PayloadData[3]<<8))/1000.0, m_PayloadData[4]));    // Voltage sent in millivolts
                        AUTH_STATE auth = new AUTH_STATE(m_PayloadData[5]);
                        builder.append(auth.toString());
                    }
                    break;
                case CMD_LOADAPPLET:
                    builder.append("LOADAPPLET");
                    break;
                case CMD_READ_I2C:
                    builder.append("READ_I2C");
                    break;
                case CMD_WRITE_I2C:
                    builder.append("WRITE_I2C");
                    break;
                default:
                    builder.append(String.format("Unknown command (0x%02X)", m_Cmd));
                    break;
            }
            */
        }
        return builder.toString();
    }


    protected int bytesToUSHORT(byte low_byte, byte high_byte)
    {
        int USHORTVAL = (low_byte & 0xFF) | ((high_byte & 0xFF) << 8);
        return USHORTVAL;
    }

    protected int bytesToULONG(byte lsb, byte b1, byte b2, byte msb)
    {
        int USHORTVAL = (lsb & 0xFF) | ((b1 & 0xFF) << 8) | ((b2 & 0xFF) << 16) | ((msb & 0xFF) << 24);
        return USHORTVAL;
    }
}
