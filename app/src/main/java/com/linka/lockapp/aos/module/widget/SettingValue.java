package com.linka.lockapp.aos.module.widget;

/**
 * Created by benedict on 6/4/17.
 */

// Structure used to queue setting changes, both read and write
public class SettingValue {
    private Integer settingIndex;
    private Integer settingValue;
    private PACKET_TYPE packetType;

    public SettingValue(Integer settingIndex, Integer settingValue, PACKET_TYPE packetType){
        this.settingIndex = settingIndex;
        this.settingValue = settingValue;
        this.packetType   = packetType;
    }

    public Integer getSettingIndex()   { return settingIndex; }
    public Integer getSettingValue()   { return settingValue; }
    public PACKET_TYPE getPacketType() { return packetType; }

    // Denote the kind of encrypted packet
    public enum PACKET_TYPE {
        COMMAND,
        WRITE,
        READ
    };

}

