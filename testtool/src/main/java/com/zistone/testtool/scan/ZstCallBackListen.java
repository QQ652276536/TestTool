package com.zistone.testtool.scan;

public interface ZstCallBackListen {
    /**
     * 扫码回调
     *
     * @param data 扫到的数据长度
     * @param len  数据长度
     */
    public void onBarReceived(byte[] data, int len);
}