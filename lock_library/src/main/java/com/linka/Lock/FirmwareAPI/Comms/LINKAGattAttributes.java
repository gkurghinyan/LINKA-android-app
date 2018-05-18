/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linka.Lock.FirmwareAPI.Comms;

import com.linka.Lock.FirmwareAPI.Debug.NrfUartService;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class LINKAGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String BATT_MV_CHARACTERISTIC = "4424d2f5-9f9c-4e4a-a8a0-bf2d9bf545b8";           // Leaving this for now, in case it's required

    // The Bluetooth_Base_UUID is: 00000000-0000-1000-8000 00805F9B34FB. All the 16-bit Attribute UUIDs defined in the adopted specifications use the above base.
    public static String VLSO_MAIN_SVC_ID = "d31e0470-416b-499b-95aa-5db239a0bfbe";
    public static String VLSO_MAIN_SVC_BASE_ID = "d31e0000-416b-499b-95aa-5db239a0bfbe";
    public static String VLSO_MAIN_DATA_TX = "d31e0003-416b-499b-95aa-5db239a0bfbe";
    public static String VLSO_MAIN_DATA_RX = "d31e0002-416b-499b-95aa-5db239a0bfbe";

    public static String FIRMWARE_VER_CHAR = "00002a26-0000-1000-8000-00805f9b34fb";

    public final static UUID UUID_VLSO_MAIN_SVC = UUID.fromString(VLSO_MAIN_SVC_ID);
    public final static UUID UUID_VLSO_DATA_TX = UUID.fromString(VLSO_MAIN_DATA_TX);        // TX from the standpoint of the firmware, so the app should read
    public final static UUID UUID_VLSO_DATA_RX = UUID.fromString(VLSO_MAIN_DATA_RX);        // RX from the standpoint of the firmware, so the app should write to this
    public final static UUID UUID_FIRMWARE_VER = UUID.fromString(FIRMWARE_VER_CHAR);

    static {
        // Services.
        attributes.put(NrfUartService.NRF_UART_SERVICE_UUID, "NRF UART");
        attributes.put(VLSO_MAIN_SVC_ID, "LINKA main service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Characteristics.
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put(BATT_MV_CHARACTERISTIC, "Battery (mV)");
        attributes.put(NrfUartService.NRF_UART_RX_CHAR_UUID, "NRF UART Rx");
        attributes.put(NrfUartService.NRF_UART_TX_CHAR_UUID, "NRF UART Tx");
        attributes.put(VLSO_MAIN_DATA_TX, "Velasso data TX");
        attributes.put(VLSO_MAIN_DATA_RX, "Velasso data RX");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
