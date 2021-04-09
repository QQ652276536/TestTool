package com.zistone.testtool.scan;

import android.content.Context;
import android.serialport.SerialPortManager;

import com.zistone.gpio.Gpio;

import java.io.File;

public class ZstScanManager extends SerialPortManager {
    private ZstCallBackListen mZstBarListen = null;
    private int mGpio_num = 75;
    private Gpio mGpio = Gpio.getInstance();
    private Context mContext;

    public ZstScanManager(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * 打开扫码设备
     *
     * @param device    设备节点
     * @param baudrate  波特率
     * @param flow_ctrl 流控
     * @param databits  数据位
     * @param stopbits  停止位
     * @param parity    校验位
     * @param gpio      控制扫码的gpio号码
     * @return 1-已打开     0-打开成功     -1-没权限     -2-参数配置错误    -3-未知错误
     */
    public int openbardevice(File device, int baudrate, int flow_ctrl, int databits, int stopbits, int parity, int gpio) {
        mGpio_num = gpio;
        return openSerialPort(device, baudrate, flow_ctrl, databits, stopbits, parity);
    }

    @Override
    protected void onDataReceived(byte[] buffer, int size) {
        // TODO Auto-generated method stub
        if (mZstBarListen != null) {
            mZstBarListen.onBarReceived(buffer, size);
        }
        mGpio.set_gpio(0, mGpio_num);
    }

    /**
     * 开始扫码
     *
     * @param callback 扫码回调
     */
    public void startBarScan(ZstCallBackListen callback) {
        if (mZstBarListen == null)
            mZstBarListen = callback;
        mGpio.set_gpio(0, mGpio_num);
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mGpio.set_gpio(1, mGpio_num);
        startReadSerialPort();
    }

    /**
     * 停止扫码
     */
    public void stopBarScan() {
        mGpio.set_gpio(0, mGpio_num);
        stopReadSerialPort();
    }

    /**
     * 关闭扫码设备
     */
    public void closeBarDevice() {
        closeSerialPort();
    }
}
