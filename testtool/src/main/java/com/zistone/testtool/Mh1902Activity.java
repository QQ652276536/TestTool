package com.zistone.testtool;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zistone.mylibrary.util.MyConvertUtil;
import com.zistone.testtool.secret_key_download.Des3Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android_serialport_api.SerialPort;

public class Mh1902Activity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final File FILE = new File("/dev/ttyHSL3");
    private static final int BAUD_RATE = 115200;
    private static final Map<Integer, String> DEVICEINFOMAP = new HashMap<Integer, String>() {{
        put(1, "TAG_KEY_SUCCESS");
        put(2, "TAG_KEY_FAIL");
        put(3, "TAG_KEY_SN");
        put(4, "TAG_KEY_MODEL_ID");
        put(5, "TAG_KEY_DEVICE_ID");
        put(6, "TAG_KEY_DESK_KEY");
        put(7, "TAG_KEY_PIN_KEY");
        put(8, "TAG_KEY_TK_DESK");
        put(9, "TAG_KEY_TK_PIN");
        put(10, "TAG_KEY_SOFT_VER");
        put(11, "TAG_KEY_HARDWARE_VER");
        put(12, "TAG_KEY_SECURITY_1");
        put(31, "TAG_BOOT_MODE");
    }};

    private SerialPort _serialPort;
    private InputStream _serialPortInputStream;
    private OutputStream _serialPortOutputStream;
    private Button _btn1, _btn2, _btn3, _btn4, _btn5, _btn6, _btn7, _btn8;
    private ImageButton _imgBtnTop, _imgBtnBottom, _imgBtnClear;
    private TextView _txt, _txt2;
    private ProgressBar _progressBar;
    private LinearLayout _linearLayout;
    private String _filePath = "";
    private int _currentThreadState = 0, _packageNum = 0, _blockNo = 0;
    private ReadThread _readThread;
    private boolean _readThreadFlag = false, _sendDataSuccess = false, _lastSendDataSuccess = false;

    private class ReadThread extends Thread {
        byte[] bytes = new byte[1024];

        @Override
        public void run() {
            super.run();
            while (!_readThreadFlag) {
                int size = 0;
                //没有数据时这里会阻塞
                try {
                    size = _serialPortInputStream.read(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (size > 6) {
                    byte[] readBytes = new byte[size];
                    System.arraycopy(bytes, 0, readBytes, 0, size);
                    final String dataStr = MyConvertUtil.ByteArrayToHexStr(readBytes);
                    Log.i(TAG, "收到串口数据：\n" + dataStr);
                    final String[] resultArray = MyConvertUtil.StrAddCharacter(dataStr, 2, " ").split(" ");
                    String cmdType = resultArray[5];
                    final String result = resultArray[6];
                    switch (cmdType) {
                        //基本参数
                        case "01":
                            String txt = "";
                            if (result.equals("00")) {
                                Log.i(TAG, "基本参数获取成功：\n");
                                //                                UpdateText(_txt, "基本参数获取成功：\n", "Append");
                                UpdateText(_txt, "固件版本：\n", "Append");
                                //解析的时候不算1个字节的命令头、2个字节的长度、2个字节的包属性、1个字节的命令码、1个字节的结果、2个字节的校验码
                                String[] data = new String[resultArray.length - 1 - 2 - 2 - 1 - 1 - 2];
                                System.arraycopy(resultArray, 7, data, 0, data.length);
                                int t;
                                int l = 0;
                                String v;
                                for (int i = 0; i < data.length; i++) {
                                    try {
                                        //T
                                        t = Integer.parseInt(data[i], 16);
                                        String tStr = DEVICEINFOMAP.get(t);
                                        txt += tStr + " ";
                                        //L
                                        l = Integer.parseInt(data[++i], 16);
                                        txt += l + " ";
                                        //V
                                        String[] vArray = new String[l];
                                        System.arraycopy(data, i + 1, vArray, 0, l);
                                        v = MyConvertUtil.StrArrayToStr(vArray);
                                        if (t == 3 || t == 5 || t == 10) {
                                            v = MyConvertUtil.HexStrToStr(v);
                                        } else {
                                            v = Integer.parseInt(v, 16) + "";
                                        }
                                        //硬件版本
                                        if (tStr.equals(DEVICEINFOMAP.get(11))) {
                                            Log.i(TAG, "硬件版本：" + v);
                                            v = v.replaceAll("zistone_", "");
                                        }
                                        //软件版本
                                        else if (tStr.equals(DEVICEINFOMAP.get(10))) {
                                            Log.i(TAG, "软件版本：" + v);
                                            v = v.replaceAll("zistone_", "");
                                            UpdateText(_txt, v + "\n", "Append");
                                        }
                                        txt += v + "\n";
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    i += l;
                                }
                                //                                UpdateText(_txt, txt + "\n", "Append");
                                Log.i(TAG, txt + "\n");
                            } else {
                                Log.e(TAG, dataStr + "（基本参数获取失败！）\n");
                                //                                UpdateText(_txt, dataStr + "（基本参数获取失败！）\n", "Append");
                                UpdateText(_txt, dataStr + "（固件版本获取失败！）\n", "Append");
                            }
                            break;
                        //请求下载
                        case "20":
                            if (result.equals("00") || result.equals("01")) {
                                UpdateText(_txt, "（已进入下载模式！）\r\n", "Append");
                                UpdateBtn(_btn5, true, null);
                            } else {
                                UpdateText(_txt, dataStr + "（下载模式请求失败！）\r\n", "Append");
                            }
                            break;
                        //下载固件
                        case "21":
                            Log.i(TAG, "收到上一包校验：" + dataStr);
                            if (result.equals("00")) {
                                UpdateText(_txt, "收到" + dataStr + "（该包数据正确！）\r\n", "Append");
                            } else {
                                //最后一包的包属性是“单独包”，不再是“后续包”，对应字节为0012，硬件会返回包校验错误，这里特殊处理
                                if (_blockNo == _packageNum) {
                                    UpdateText(_txt, "收到" + dataStr + "（该包数据正确！）\r\n", "Append");
                                }
                                //响应错误，改变发送数据的线程状态，放开线程后才会触发，也就是执行_currentThreadState = 1后才会触发
                                else {
                                    _lastSendDataSuccess = false;
                                    _sendDataSuccess = false;
                                    UpdateText(_txt, "收到" + dataStr + "（该包数据错误！）\r\n", "Append");
                                }
                            }
                            //收到响应，放开阻塞发送数据的线程
                            _currentThreadState = 1;
                            break;
                        //下载密钥加密密钥
                        case "23":
                            UpdateText(_txt, "下载密钥加密密钥：" + dataStr, "Append");
                            break;
                        //密钥擦除
                        case "1B":
                            if (result.equals("00"))
                                UpdateText(_txt, "密钥已擦除：" + dataStr + "\n", "Append");
                            break;
                    }
                }
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (null != cursor && cursor.moveToFirst()) {
            ;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    public String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * MAC结果
     * 60个字节，每8个字节相或，不够8位补0，得到8字节，然后用8个0作为des密钥对其加密，取前4个字节
     *
     * @param input
     * @return
     */
    private String CalcMac(String input) {
        String[] array = new String[8];
        String[] totalStrs = MyConvertUtil.StrAddCharacter(input, 16, " ").split(" ");
        for (int i = 0; i < totalStrs.length; i++) {
            if (i != totalStrs.length - 1) {
                String[] tempArray = MyConvertUtil.StrAddCharacter(totalStrs[i], 2, " ").split(" ");
                int binaryNum = 0;
                for (int j = 0; j < tempArray.length; j++) {
                    int tempHexNum = Integer.parseInt(tempArray[j], 16);
                    if (j == 0)
                        binaryNum = tempHexNum;
                    else
                        binaryNum |= tempHexNum;
                }
                String tmepResult = Integer.toHexString(binaryNum);
                if (tmepResult.length() < 2) {
                    tmepResult = "0" + tmepResult;
                }
                array[i] = tmepResult;
            }
            //不够8个字节前面补零
            else {
                String tempStr = MyConvertUtil.AddZeroForNum(totalStrs[i], 16, true);
                String[] tempArray = MyConvertUtil.StrAddCharacter(tempStr, 2, " ").split(" ");
                int binaryNum = 0;
                for (int j = 0; j < tempArray.length; j++) {
                    int tempHexNum = Integer.parseInt(tempArray[j], 16);
                    if (j == 0)
                        binaryNum = tempHexNum;
                    else
                        binaryNum |= tempHexNum;
                }
                String tmepResult = Integer.toHexString(binaryNum);
                if (tmepResult.length() < 2) {
                    tmepResult = "0" + tmepResult;
                }
                array[i] = tmepResult;
            }
        }
        String key = "00000000";
        String data = Arrays.toString(array).replaceAll("[\\s|\\[|\\]|,]", "");
        byte[] cipherBytes = Des3Util.UnionDesEncrypt(key.getBytes(), data.getBytes());
        String hexCipher = MyConvertUtil.ByteArrayToHexStr(cipherBytes);
        Log.i(TAG, "MAC里的16进制密文：" + hexCipher);
        String result = "00000000";
        result = hexCipher.substring(0, 8);
        Log.i(TAG, "MAC结果取前4个字节：" + result);
        return result;
    }

    private void UpdateBtn(final Button btn, final boolean enable, final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != str && !"".equals(str))
                    btn.setText(str);
                btn.setEnabled(enable);
            }
        });
    }

    private void UpdateProgress(final int progress, final int linearVisible) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _txt2.setText("已下载" + progress + "%");
                _progressBar.setProgress(progress);
                _linearLayout.setVisibility(linearVisible);
            }
        });
    }

    private void UpdateText(final TextView txt, final String str, final String setOrAppend) {
        if (null == txt)
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (setOrAppend) {
                    case "Set":
                        txt.setText(str);
                        break;
                    case "Append":
                        txt.append(str);
                        int offset = txt.getLineCount() * txt.getLineHeight();
                        if (offset > txt.getHeight())
                            txt.scrollTo(0, offset - txt.getHeight());
                        break;
                }
            }
        });
    }

    private int CalculateCRC(byte[] byteArray) {
        int crc = 0;
        for (byte tempByte : byteArray) {
            //0x80 = 128
            for (int i = 0x80; i != 0; i /= 2) {
                crc *= 2;
                //0x10000 = 65536
                if ((crc & 0x10000) != 0)
                    //0x11021 = 69665
                    crc ^= 0x11021;
                if ((tempByte & i) != 0)
                    //0x1021 = 4129
                    crc ^= 0x1021;
            }
        }
        return crc;
    }

    /**
     * 发送固件数据
     *
     * @param byteArray
     * @throws Exception
     */
    private void SendFirmwareData(final byte[] byteArray) {
        _btn5.setEnabled(false);
        //线程里发送数据，避免阻塞UI
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //发送固件数据包
                    int fileLen = byteArray.length;
                    Log.i(TAG, "固件数据总长度：" + fileLen);
                    String __________tempStr__________ = new String(byteArray);
                    //每包固定的长度（除包头外）=包长度（2字节）+包属性（2字节）+命令码（1字节）+固件名字（16字节）+固件总长度（3字节）+当前块号（2字节）+校验码（2字节）=28字节
                    int everyPackageFixedLen = 28;
                    //包属性
                    String packageProperty = "4012";
                    //固件名字，将高字节在前
                    String blockName = MyConvertUtil.StrToHexStr("kernel.bin");
                    //后面补零（左对齐后面补空格，空格替换为零）
                    blockName = String.format("%-32s", blockName).replace(' ', '0');
                    //固件总长度，将高字节在前
                    String dataTotalLenHexStr = MyConvertUtil.IntToHexStr(fileLen);
                    UpdateText(_txt, "固件数据总长度：" + fileLen + "\r\n", "Append");
                    //前面补零
                    dataTotalLenHexStr = MyConvertUtil.AddZeroForNum(dataTotalLenHexStr, 6, true);
                    //添加空格用于后面分割为数组
                    dataTotalLenHexStr = MyConvertUtil.StrAddCharacter(dataTotalLenHexStr, 2, " ");
                    //对Str数组进行排序
                    dataTotalLenHexStr = Arrays.toString(MyConvertUtil.SortStringArray(dataTotalLenHexStr.split(" "), false));
                    //将固件数据分包，每包的长度
                    int dataMax = 900;
                    if (fileLen < dataMax) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Mh1902Activity.this, "该升级包小于分包", Toast.LENGTH_LONG).show();
                                _btn5.setEnabled(true);
                            }
                        });
                        return;
                    }
                    //包长度，不包含包头=固件数据分包+每包固定长度，将高字节在前
                    int everyPackageLenNotLast = dataMax + everyPackageFixedLen;
                    String everyPackageLenNotLastHexStr = MyConvertUtil.IntToHexStr(everyPackageLenNotLast);
                    everyPackageLenNotLastHexStr = MyConvertUtil.AddZeroForNum(everyPackageLenNotLastHexStr, 4, true);
                    everyPackageLenNotLastHexStr = MyConvertUtil.StrAddCharacter(everyPackageLenNotLastHexStr, 2, " ");
                    everyPackageLenNotLastHexStr = Arrays.toString(MyConvertUtil.SortStringArray(everyPackageLenNotLastHexStr.split(" "), false));
                    //包数，向上取整
                    _packageNum = (int) Math.ceil((double) fileLen / dataMax);
                    //最后一个包的长度
                    int surplusLen = fileLen % dataMax;
                    //已下载数据长度
                    int currentBytes = 0;
                    for (int i = 0; i < _packageNum; i++) {
                        //当前块号，将高字节在前，从1开始
                        _blockNo = i + 1;
                        String blockNoHexStr = MyConvertUtil.IntToHexStr(_blockNo);
                        blockNoHexStr = MyConvertUtil.AddZeroForNum(blockNoHexStr, 4, true);
                        blockNoHexStr = MyConvertUtil.StrAddCharacter(blockNoHexStr, 2, " ");
                        blockNoHexStr = Arrays.toString(MyConvertUtil.SortStringArray(blockNoHexStr.split(" "), false));
                        //不是最后一包
                        if (i != _packageNum - 1) {
                            //包内容，不包含固件数据和校验码
                            String cmdx =
                                    "3F" + everyPackageLenNotLastHexStr + packageProperty + "21" + blockName + dataTotalLenHexStr + blockNoHexStr;
                            byte[] tempBytes1 = MyConvertUtil.HexStrToByteArray(cmdx);
                            //固件数据
                            final byte[] tempBytes2 = new byte[dataMax];
                            //源数组、源数组起始位置、目标数组、目标数组起始位置、要拷贝的数组长度
                            System.arraycopy(byteArray, i * dataMax, tempBytes2, 0, dataMax);
                            String __________tempStr1__________ = MyConvertUtil.ByteArrayToHexStr(tempBytes2);
                            //包内容，不包含校验码
                            byte[] tempBytes3 = new byte[tempBytes1.length + tempBytes2.length];
                            System.arraycopy(tempBytes1, 0, tempBytes3, 0, tempBytes1.length);
                            System.arraycopy(tempBytes2, 0, tempBytes3, tempBytes1.length, tempBytes2.length);
                            String __________tempStr2__________ = MyConvertUtil.ByteArrayToHexStr(tempBytes3);
                            //计算2位校验码，低字节在前
                            int crc = CalculateCRC(tempBytes3);
                            String crcHexStr = MyConvertUtil.IntToHexStr(crc);
                            crcHexStr = MyConvertUtil.AddZeroForNum(crcHexStr, 4, true);
                            crcHexStr = MyConvertUtil.StrAddCharacter(crcHexStr, 2, " ");
                            crcHexStr = Arrays.toString(MyConvertUtil.SortStringArray(crcHexStr.split(" "), true));
                            byte[] tempBytes4 = MyConvertUtil.HexStrToByteArray(crcHexStr);

                            byte crch, crcl;
                            crc &= 0xffff;
                            crch = (byte) ((crc >> 8) & 0xff);
                            crcl = (byte) (crc & 0xff);
                            byte[] tempBytes4_temp = new byte[]{crcl, crch};
                            tempBytes4 = tempBytes4_temp;
                            crcHexStr = MyConvertUtil.ByteArrayToHexStr(tempBytes4);

                            //一个完整分包的内容
                            byte[] tempBytes5 = new byte[tempBytes3.length + tempBytes4.length];
                            System.arraycopy(tempBytes3, 0, tempBytes5, 0, tempBytes3.length);
                            System.arraycopy(tempBytes4, 0, tempBytes5, tempBytes3.length, tempBytes4.length);
                            String __________tempStr3__________ = MyConvertUtil.ByteArrayToHexStr(tempBytes5);
                            //配合后面的while循环可以起到阻塞作用，也可以避免UI线程多次执行
                            _currentThreadState = 0;
                            _serialPortOutputStream.write(tempBytes5);
                            Log.i(TAG, "发送第" + _blockNo + "包：" + __________tempStr3__________ + "\r\n固件数据+包格式的长度（含包头“3F”）：" + tempBytes5.length +
                                    "，校验码：" + crcHexStr);
                            //取每包固件数据的长度计算进度
                            currentBytes += tempBytes2.length;
                            double progress = currentBytes * 100 / fileLen;
                            UpdateProgress((int) progress, View.VISIBLE);
                            UpdateText(_txt, "发送第" + _blockNo + "包数据...\r\n", "Append");
                            //等待上一包数据的验证结果
                            while (_currentThreadState == 0) {
                                continue;
                            }
                            //上一包数据不正确，中断升级
                            if (!_sendDataSuccess) {
                                UpdateText(_txt, "升级过程中遇到错误，升级终止!\r\n", "Append");
                                break;
                            }
                        }
                        //最后一包
                        else {
                            if (surplusLen == 0) {
                                Log.e(TAG, "数据长度为零，升级中止！");
                                return;
                            }
                            //最后一包的长度，高字节在前，记得加上包格式（不包含包头“3F”，所以为28）
                            String surplusLenHexStr = MyConvertUtil.IntToHexStr(surplusLen + everyPackageFixedLen);
                            surplusLenHexStr = MyConvertUtil.AddZeroForNum(surplusLenHexStr, 4, true);
                            surplusLenHexStr = MyConvertUtil.StrAddCharacter(surplusLenHexStr, 2, " ");
                            surplusLenHexStr = Arrays.toString(MyConvertUtil.SortStringArray(surplusLenHexStr.split(" "), false));
                            //包内容，不包含固件数据和校验码
                            //                            String cmdx = "3F" + surplusLenHexStr + packageProperty + "21" + blockName +
                            //                            dataTotalLenHexStr + blockNoHexStr;
                            String cmdx = "3F" + surplusLenHexStr + "001221" + blockName + dataTotalLenHexStr + blockNoHexStr;
                            byte[] tempBytes1 = MyConvertUtil.HexStrToByteArray(cmdx);
                            //固件数据
                            final byte[] tempBytes2 = new byte[surplusLen];
                            System.arraycopy(byteArray, i * dataMax, tempBytes2, 0, surplusLen);
                            String __________tempStr1__________ = MyConvertUtil.ByteArrayToHexStr(tempBytes2);
                            //包内容，不包含校验码
                            byte[] tempBytes3 = new byte[tempBytes1.length + tempBytes2.length];
                            System.arraycopy(tempBytes1, 0, tempBytes3, 0, tempBytes1.length);
                            System.arraycopy(tempBytes2, 0, tempBytes3, tempBytes1.length, tempBytes2.length);
                            String __________tempStr2__________ = MyConvertUtil.ByteArrayToHexStr(tempBytes3);
                            //计算2位校验码，低字节在前
                            int crc = CalculateCRC(tempBytes3);
                            String crcHexStr = MyConvertUtil.IntToHexStr(crc);
                            crcHexStr = MyConvertUtil.AddZeroForNum(crcHexStr, 4, true);
                            crcHexStr = MyConvertUtil.StrAddCharacter(crcHexStr, 2, " ");
                            crcHexStr = Arrays.toString(MyConvertUtil.SortStringArray(crcHexStr.split(" "), true));
                            byte[] tempBytes4 = MyConvertUtil.HexStrToByteArray(crcHexStr);

                            byte crch, crcl;
                            crc &= 0xffff;
                            crch = (byte) ((crc >> 8) & 0xff);
                            crcl = (byte) (crc & 0xff);
                            byte[] tempBytes4_temp = new byte[]{crcl, crch};
                            tempBytes4 = tempBytes4_temp;
                            crcHexStr = MyConvertUtil.ByteArrayToHexStr(tempBytes4);

                            //一个完整分包的内容
                            byte[] tempBytes5 = new byte[tempBytes3.length + tempBytes4.length];
                            System.arraycopy(tempBytes3, 0, tempBytes5, 0, tempBytes3.length);
                            System.arraycopy(tempBytes4, 0, tempBytes5, tempBytes3.length, tempBytes4.length);
                            String __________tempStr3__________ = MyConvertUtil.ByteArrayToHexStr(tempBytes5);
                            Log.i(TAG,
                                    "发送第" + _blockNo + "包：" + __________tempStr3__________ + "\r\n" + "固件数据+包格式的长度（含包头“3F”）：" + tempBytes5.length + "，校验码：" + crcHexStr);
                            _currentThreadState = 0;
                            _serialPortOutputStream.write(tempBytes5);
                            //取每包固件数据的长度计算进度
                            currentBytes += tempBytes2.length;
                            double value = currentBytes * 100 / fileLen;
                            UpdateProgress((int) value, View.VISIBLE);
                            UpdateText(_txt, "发送第" + _blockNo + "包（最后一包）数据...\r\n", "Append");
                            //等待上一包数据的验证结果
                            while (_currentThreadState == 0) {
                                continue;
                            }
                            //最后一包的包属性是“单独包”，不再是“后续包”，对应字节为0012，硬件会返回包校验错误，这里特殊处理
                            if (!_lastSendDataSuccess) {
                                UpdateText(_txt, "升级过程中遇到错误，升级终止!\r\n", "Append");
                            } else {
                                UpdateText(_txt, "升级完毕!\r\n", "Append");
                                UpdateProgress(0, View.GONE);
                            }
                            UpdateBtn(_btn5, true, null);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        _readThreadFlag = true;
        UpdateBtn(_btn1, true, "打开串口");
        UpdateBtn(_btn2, false, null);
        UpdateBtn(_btn3, false, null);
        UpdateBtn(_btn5, false, null);
        UpdateBtn(_btn6, false, null);
        UpdateText(_txt, "\r\n程序进入后台，串口关闭！", "Append");
        if (_serialPort != null) {
            _serialPort.close();
            _serialPort = null;
        }
        try {
            if (_serialPortOutputStream != null)
                _serialPortOutputStream.close();
            _serialPortOutputStream = null;
            if (_serialPortInputStream != null)
                _serialPortInputStream.close();
            _serialPortInputStream = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从后台唤醒，进入前台
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 102) {
                try {
                    Uri uri = data.getData();
                    Log.i(TAG, "File Uri:" + uri.toString());
                    _filePath = getPath(this, uri);
                    Log.i(TAG, "File Path:" + _filePath);
                    if (null != _filePath) {
                        int index = _filePath.lastIndexOf("/");
                        String fileName = _filePath.substring(index + 1);
                        _btn4.setText(fileName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                //回到顶部
                case R.id.btnTop_mh1902:
                    _txt.scrollTo(0, 0);
                    break;
                //回到底部
                case R.id.btnBottom_mh1902:
                    int offset = _txt.getLineCount() * _txt.getLineHeight();
                    if (offset > _txt.getHeight()) {
                        _txt.scrollTo(0, offset - _txt.getHeight());
                    }
                    break;
                //清屏
                case R.id.btnClear_mh1902:
                    _txt.setText("");
                    break;
                //打开串口
                case R.id.btn1_mh1902:
                    UpdateProgress(0, View.GONE);
                    if (_btn1.getText().equals("打开串口")) {
                        _serialPort = new SerialPort(FILE, BAUD_RATE, 0);
                        _serialPortOutputStream = _serialPort.getOutputStream();
                        _serialPortInputStream = _serialPort.getInputStream();
                        _readThreadFlag = false;
                        _readThread = new ReadThread();
                        _readThread.start();
                        UpdateBtn(_btn1, true, "关闭串口");
                        UpdateBtn(_btn2, true, null);
                        UpdateBtn(_btn3, true, null);
                        UpdateBtn(_btn5, true, null);
                        UpdateBtn(_btn6, true, null);
                        UpdateBtn(_btn7, true, null);
                        UpdateBtn(_btn8, true, null);
                    } else {
                        _readThreadFlag = true;
                        _serialPort.close();
                        _serialPort = null;
                        if (_serialPortOutputStream != null)
                            _serialPortOutputStream.close();
                        _serialPortOutputStream = null;
                        if (_serialPortInputStream != null)
                            _serialPortInputStream.close();
                        _serialPortInputStream = null;
                        UpdateBtn(_btn1, true, "打开串口");
                        UpdateBtn(_btn2, false, null);
                        UpdateBtn(_btn3, false, null);
                        UpdateBtn(_btn5, false, null);
                        UpdateBtn(_btn6, false, null);
                        UpdateBtn(_btn7, false, null);
                        UpdateBtn(_btn8, false, null);
                    }
                    break;
                //基本参数
                case R.id.btn2_mh1902: {
                    byte[] byteArray = MyConvertUtil.HexStrToByteArray("3F07000000017A9D");
                    _serialPortOutputStream.write(byteArray);
                }
                break;
                //请求下载
                case R.id.btn3_mh1902: {
                    byte[] byteArray = MyConvertUtil.HexStrToByteArray("3F070000002039A9");
                    _serialPortOutputStream.write(byteArray);
                }
                break;
                //选择文件
                case R.id.btn4_mh1902: {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 102);
                }
                break;
                //下载固件
                case R.id.btn5_mh1902: {
                    if (_filePath.isEmpty()) {
                        Toast.makeText(this, "请选择文件", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File file = new File(_filePath);
                    FileInputStream fileInputStream = new FileInputStream(file);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] tempByte = new byte[1024];
                    int len;
                    while ((len = fileInputStream.read(tempByte)) != -1) {
                        byteArrayOutputStream.write(tempByte, 0, len);
                    }
                    byte[] data = byteArrayOutputStream.toByteArray();
                    fileInputStream.close();
                    byteArrayOutputStream.close();
                    //重置分包发送数据的状态
                    UpdateProgress(0, View.VISIBLE);
                    _sendDataSuccess = true;
                    _lastSendDataSuccess = true;
                    //分包发送固件数据
                    SendFirmwareData(data);
                }
                break;
                //密钥擦除
                case R.id.btn6_mh1902: {
                    byte[] byteArray = MyConvertUtil.HexStrToByteArray("3F070000001B012E");
                    _serialPortOutputStream.write(byteArray);
                }
                break;
                //下载密钥加密密钥
                case R.id.btn7_mh1902: {
                    String ptk = "000000000000000000000000";
                    String key = "1111111111111111111111";

                    String ttest1 =
                            "3F470000002302000018154AA6AE315EE3708630C2C502FA13A78CA64DE9C1B123A70000000000000000000000000000000000000000000000000000000000000000C75D25372CBB";

                    //包头（1字节）+长度（除了包头3F以外，2字节）+包属性（2字节）+命令码（1字节）+数据域（若干字节）+校验码（包头到数据域，2字节）
                    //数据域：密钥类型（1字节）+发散密钥索引（1字节）+密钥索引（1字节）+密钥长度（1字节）+密钥的密文（？字节，
                    // 使用PTK做3DES加密，PTK默认为24位全0）+应用名（32字节，目前不使用，全部填0）+MAC（4字节，使用前面的60
                    // 个字节数据的作为明文，PTK作为密钥进行MAC运算，取前4字节）

                    //数据域里的密钥类型
                    String type = MyConvertUtil.IntToHexStr(07);
                    //数据域里的发散密钥索引
                    String index = MyConvertUtil.IntToHexStr(00);
                    //数据域里的密钥索引
                    String index2 = MyConvertUtil.IntToHexStr(00);
                    //数据域里的密钥长度
                    String keyLen = MyConvertUtil.IntToHexStr(16);
                    //数据域里的密文（使用PTK做3DES加密计算出来的）
                    byte[] ciphers = Des3Util.Union3DesEncrypt(key.getBytes(), ptk.getBytes());
                    String cipher = MyConvertUtil.ByteArrayToHexStr(ciphers);
                    //密文长度
                    int cipherLen = cipher.length() / 2;
                    Log.i(TAG, "使用PTK做3DES加密得到的16进制密文：" + cipher + "\n长度：" + cipherLen);
                    //数据域里的应用名
                    String name = "0000000000000000000000000000000000000000000000000000000000000000";
                    //应用名长度
                    int nameLen = name.length() / 2;
                    //MAC
                    String tempMacData = type + index + index2 + keyLen + cipher + name;
                    Log.i(TAG, "MAC前面的60个字节：" + tempMacData + "\n长度：" + tempMacData.length() / 2);
                    String mac = CalcMac(tempMacData);
                    //没有3F、包长度、校验码的命令
                    String temp = "000023" + type + index + index2 + keyLen + cipher + name + mac;
                    //加上“包长度”的2个字节和“校验码”的2个字节，组成最后的包的数据长度（有点绕口）
                    String hexLen = MyConvertUtil.IntToHexStr(temp.length() / 2 + 2 + 2);
                    hexLen = MyConvertUtil.AddZeroForNum(hexLen, 4, false);
                    //没有校验码的命令
                    temp = "3F" + hexLen + temp;
                    //校验码
                    int crc = CalculateCRC(MyConvertUtil.HexStrToByteArray(temp));
                    String crcHexStr = MyConvertUtil.IntToHexStr(crc);
                    crcHexStr = MyConvertUtil.AddZeroForNum(crcHexStr, 4, true);
                    crcHexStr = MyConvertUtil.StrAddCharacter(crcHexStr, 2, " ");
                    crcHexStr = Arrays.toString(MyConvertUtil.SortStringArray(crcHexStr.split(" "), true));
                    byte[] tempBytes4 = MyConvertUtil.HexStrToByteArray(crcHexStr);
                    String hexCrc = MyConvertUtil.ByteArrayToHexStr(tempBytes4);
                    Log.i(TAG, "生成的校验码：" + hexCrc);
                    //最后组成的命令
                    temp += hexCrc;
                    Log.i(TAG, "最后生成的命令：" + temp);
                    byte[] byteArray = MyConvertUtil.HexStrToByteArray(temp);
                    _serialPortOutputStream.write(byteArray);
                }
                break;
                //下载工作密钥
                case R.id.btn8_mh1902: {
                    String ttest1 =
                            "3F470000002302000018154AA6AE315EE3708630C2C502FA13A78CA64DE9C1B123A70000000000000000000000000000000000000000000000000000000000000000C75D25372CBB";
                    byte[] byteArray = MyConvertUtil.HexStrToByteArray(ttest1);
                    _serialPortOutputStream.write(byteArray);
                }
                break;
                default:
                    UpdateProgress(0, View.GONE);
                    break;
            }
        } catch (Exception e) {
            _txt.append(e.toString() + "\r\n");
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        _readThreadFlag = true;
        if (_serialPort != null)
            _serialPort.close();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mh1902);
        _txt = findViewById(R.id.txt_mh1902);
        _txt.setMovementMethod(ScrollingMovementMethod.getInstance());
        _imgBtnTop = findViewById(R.id.btnTop_mh1902);
        _imgBtnBottom = findViewById(R.id.btnBottom_mh1902);
        _imgBtnClear = findViewById(R.id.btnClear_mh1902);
        _txt2 = findViewById(R.id.txt2_mh1902);
        _progressBar = findViewById(R.id.progressBar_mh1902);
        _progressBar.setMax(100);
        _linearLayout = findViewById(R.id.llProgress_mh1902);
        _imgBtnTop.setOnClickListener(this);
        _imgBtnBottom.setOnClickListener(this);
        _imgBtnClear.setOnClickListener(this);
        _btn1 = findViewById(R.id.btn1_mh1902);
        _btn2 = findViewById(R.id.btn2_mh1902);
        _btn3 = findViewById(R.id.btn3_mh1902);
        _btn4 = findViewById(R.id.btn4_mh1902);
        _btn5 = findViewById(R.id.btn5_mh1902);
        _btn6 = findViewById(R.id.btn6_mh1902);
        _btn7 = findViewById(R.id.btn7_mh1902);
        _btn8 = findViewById(R.id.btn8_mh1902);
        _btn1.setOnClickListener(this);
        _btn2.setOnClickListener(this);
        _btn3.setOnClickListener(this);
        _btn4.setOnClickListener(this);
        _btn5.setOnClickListener(this);
        _btn6.setOnClickListener(this);
        _btn7.setOnClickListener(this);
        _btn8.setOnClickListener(this);
    }
}
