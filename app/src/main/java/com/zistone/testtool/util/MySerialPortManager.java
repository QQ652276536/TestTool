package com.zistone.testtool.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public class MySerialPortManager {
    private static final File FILE = new File("/dev/ttyHSL3");
    private static final int BAUD_RATE = 115200;

    private static SerialPort _serialPort;
    private static InputStream _serialPortInputStream;
    private static OutputStream _serialPortOutputStream;

    public static SerialPort NewInstance() {
        try {
            if (_serialPort == null) {
                _serialPort = new SerialPort(FILE, BAUD_RATE, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return _serialPort;
    }

    public static void Close() {
        try {
            _serialPortOutputStream.close();
            _serialPortInputStream.close();
            _serialPort.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SendData(byte[] input) {
        try {
            _serialPortOutputStream = _serialPort.getOutputStream();
            _serialPortOutputStream.write(input);
            _serialPortOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] ReadData() {
        _serialPortInputStream = _serialPort.getInputStream();
        byte[] output = null;
        try {
            int size = _serialPortInputStream.available();
            while (size > 0) {
                output = new byte[size];
                _serialPortInputStream.read(output);
                size = _serialPortInputStream.available();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

}