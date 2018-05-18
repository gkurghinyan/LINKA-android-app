package com.linka.Lock.FirmwareAPI.Debug;

import java.util.UUID;

/**
 * Created by Loren-Admin on 5/13/2015.
 */
public class NrfUartService {


    public static String NRF_UART_SERVICE_BASE_UUID = "6E400000-B5A3-F393-E0A9-E50E24DCCA9E";
    public static String NRF_UART_SERVICE_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    public static String NRF_UART_TX_CHAR_UUID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    public static String NRF_UART_RX_CHAR_UUID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";

    public final static UUID UUID_NRF_UART_SVC = UUID.fromString(NRF_UART_SERVICE_UUID);
    public final static UUID UUID_NRF_UART_RX = UUID.fromString(NRF_UART_RX_CHAR_UUID);
    public final static UUID UUID_NRF_UART_TX = UUID.fromString(NRF_UART_TX_CHAR_UUID);


}
