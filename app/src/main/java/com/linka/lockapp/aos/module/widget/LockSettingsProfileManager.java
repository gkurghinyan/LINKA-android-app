package com.linka.lockapp.aos.module.widget;

import android.util.Log;
import com.linka.Lock.FirmwareAPI.Comms.LockSettingPacket;
import com.linka.lockapp.aos.module.helpers.LogHelper;

/**
 * Created by benedict on 6/4/17.
 */

public class LockSettingsProfileManager {

    static final SettingValue[] FW_1_4_3_Setting_Profile = {
            //DEFAULT is Suburban
            new SettingValue(LockSettingPacket.VLSO_SETTING_ALARM_DELAY_S, 6, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_ALARM_DURATION_S, 5, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_BUMP_TH_MG, 5, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_JOSTLE_100MS, 34, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_ROLL_ALRM_DEG, 9, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_PITCH_ALRM_DEG, 9, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SET_ACC_POST_LOCK_DELAY_S, 2, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_UNLOCKED_BUMP_TH_MG, 20, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SET_STALL_DELAY_100MS, 0, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_BAD_ENC_TIMES, 10, SettingValue.PACKET_TYPE.WRITE)
    };

    static final SettingValue[] FW_1_5_9_Setting_Profile = {
            //DEFAULT is Suburban
            new SettingValue(LockSettingPacket.VLSO_SETTING_ALARM_DELAY_S, 6, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_ALARM_DURATION_S, 5, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_BUMP_TH_MG, 5, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_JOSTLE_100MS, 34, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_ROLL_ALRM_DEG, 9, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_PITCH_ALRM_DEG, 9, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SET_ACC_POST_LOCK_DELAY_S, 2, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_UNLOCKED_BUMP_TH_MG, 20, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_STALL_MA, 60, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SETTING_MOTOR_SPD_SLOW, 63, SettingValue.PACKET_TYPE.WRITE),
            new SettingValue(LockSettingPacket.VLSO_SET_STALL_DELAY_100MS, 18, SettingValue.PACKET_TYPE.WRITE)
    };

    static final SettingValue[] APP_1_8_SETTING_PROFILE = {
            //DEFAULT is Suburban
            new SettingValue(LockSettingPacket.VLSO_SETTING_UNLOCKED_BUMP_TH_MG, 20, SettingValue.PACKET_TYPE.WRITE)
    };

    static void updateLockSettingsProfile(LockController lockController, String fwVersion) {
        //If firmware version is the 1.4.3, then write the new profile settings
        if (fwVersion.equals("1.4.3")) {
            LogHelper.e("LockSettingsProfileMgr", "Updating Lock with 1.4.3 Settings Profile");

            for (SettingValue setting : FW_1_4_3_Setting_Profile) {
                LogHelper.e("LockSettingsProfileMgr", "Updating Setting: " + setting.getSettingIndex() + " with value: " + setting.getSettingValue());

                lockController.lockBLEServiceProxy.doAction_WriteSetting("LockSettingsProfileManager->updateLockSettingsProfile", setting.getSettingIndex(), setting.getSettingValue(), lockController.lockControllerBundle);
            }
            lockController.getLinka().updateLockSettingsProfile = false;
            lockController.getLinka().saveSettings();
        } else if  (fwVersion.equals("1.5.9")){
            LogHelper.e("LockSettingsProfileMgr", "Updating Lock with 1.5.9 Settings Profile");

            for (SettingValue setting : FW_1_5_9_Setting_Profile) {

                lockController.lockBLEServiceProxy.doAction_WriteSetting("LockSettingsProfileManager->updateLockSettingsProfile", setting.getSettingIndex(), setting.getSettingValue(), lockController.lockControllerBundle);
            }

            //Read stall to make sure its set
            lockController.lockBLEServiceProxy.doAction_ReadSetting("LockController->onGattSettingPacketUpdated", LockSettingPacket.VLSO_SETTING_STALL_MA, lockController.lockControllerBundle);

            lockController.getLinka().updateLockSettingsProfile = false;
            lockController.getLinka().saveSettings();


        }
    }
    static void updateAppSettingsProfile(LockController lockController) {
        //If stall delay is 30 or 0, it is the old setting
            LogHelper.e("LockAppProfileMgr", "App updated, now update settings");

            for (SettingValue setting : APP_1_8_SETTING_PROFILE) {
                LogHelper.e("LockAppProfileMgr", "Updating Setting: " + setting.getSettingIndex() + " with value: " + setting.getSettingValue());

                lockController.lockBLEServiceProxy.doAction_WriteSetting("LockSettingsProfileManager->updateAppSettingsProfile", setting.getSettingIndex(), setting.getSettingValue(), lockController.lockControllerBundle);
            }
            lockController.getLinka().updateAppSettingsProfile = false;
            lockController.getLinka().saveSettings();
    }

}
