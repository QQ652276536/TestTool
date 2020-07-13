package com.zistone.testtool;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zistone.gpio.Gpio;
import com.zz.api.CardDriverAPI;
import com.zz.impl.cpucard.CPUCardDeviceImpl;

import java.util.List;

public class BankCard extends AppCompatActivity {
    private static final String TAG = "BankCard";
    private static final String PORT_NAME = "/dev/ttyHSL2";
    private static final int BAUDRATE = 115200;

    private TextView _txt1;
    private boolean isActive = false;
    private CPUCardDeviceImpl cpuCard;

    public static final String bytesToHexString(byte[] bArray, int nArrayLen) {
        StringBuffer sb = new StringBuffer(nArrayLen);
        String sTemp;
        for (int i = 0; i < nArrayLen; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    private String getErrorDesc(int errCode, byte[] errBuf) {
        String errMsg = new String(errBuf).toString().trim();
        switch (errCode) {
            case CardDriverAPI.ERR_CARD_SW3_WRONG:
                return "错误码:" + errCode + ",错误描述：非接卡应答结果错误," + errMsg;
            case CardDriverAPI.ERR_CARD_SW3_MULTICARD:
                return "错误码:" + errCode + ",错误描述：射频区域有多张卡重叠," + errMsg;
            case CardDriverAPI.ERR_FIND_CARD_TIMEOUT:
                return "错误码:" + errCode + ",错误描述：等待放卡超时," + errMsg;
            case CardDriverAPI.ERR_PARAM_INVALID:
                return "错误码:" + errCode + ",错误描述：参数非法," + errMsg;
            case CardDriverAPI.ERR_PORT_OPEN_FAIL:
                return "错误码:" + errCode + ",错误描述：打开串口失败," + errMsg;
            case CardDriverAPI.ERR_SEND_DATA_FAIL:
                return "错误码:" + errCode + ",错误描述：发送数据失败," + errMsg;
            case CardDriverAPI.ERR_WAIT_TIME_OUT:
                return "错误码:" + errCode + ",错误描述：等待应答超时," + errMsg;
            case CardDriverAPI.ERR_RECV_DATA_FAIL:
                return "错误码:" + errCode + ",错误描述：接收应答失败," + errMsg;
            default:
                return "错误码:" + errCode + ",错误描述：未知错误," + errMsg;
        }
    }

    public void OnClickPowerOff(View view) {
        byte[] errMsg = new byte[201];
        int ret = cpuCard.Poweroff(PORT_NAME, BAUDRATE, errMsg);
        if (ret == 0) {
            Log.i(TAG, "CPU卡下电成功");
        } else {
            Log.e(TAG, "CPU卡下电失败" + getErrorDesc(ret, new byte[100]));
        }
    }

    public void OnClickExecApduCmd(View view) {
        byte[] errMsg = new byte[201];
        byte[] retData = new byte[512];
        short[] retDataLen = new short[1];

        byte[] apduCmd = new byte[]{0x00, (byte) 0xa4, 0x04, 0x00, (byte) 0x0e, 0x32, 0x50, 0x41, 0x59, 0x2e, 0x53, 0x59, 0x53, 0x2e, 0x44, 0x44,
                                    0x46, 0x30, 0x31, 0x00}; //获取应用列表

        byte[] apduCmd1 = new byte[]{0x00, (byte) 0xa4, 0x04, 0x00, 0x08, (byte) 0xa0, 0x00, 0x00, 0x03, 0x33, 0x01, 0x01, 0x01, 0x00};
        byte[] apduCmd2 = new byte[]{0x00, (byte) 0xb2, 0x01, 0x0c, 0x00};
        short apduCmdLen = (short) apduCmd.length;
        int ret;
        while (true) {
            ret = cpuCard.ExecApduCmd(PORT_NAME, BAUDRATE, apduCmd, apduCmdLen, retData, retDataLen, errMsg);
            if (ret == 0) {
                Log.i(TAG, "执行APDU命令成功，返回数据:" + bytesToHexString(retData, retDataLen[0]));
            } else {
                Log.e(TAG, "执行APDU命令失败," + getErrorDesc(ret, new byte[100]));
                break;
            }
            apduCmdLen = (short) apduCmd1.length;
            ret = cpuCard.ExecApduCmd(PORT_NAME, BAUDRATE, apduCmd1, apduCmdLen, retData, retDataLen, errMsg);
            if (ret == 0) {
                Log.i(TAG, "执行APDU命令成功，返回数据:" + bytesToHexString(retData, retDataLen[0]));
            } else {
                Log.e(TAG, "执行APDU命令失败," + getErrorDesc(ret, new byte[100]));
                break;
            }
            apduCmdLen = (short) apduCmd2.length;
            ret = cpuCard.ExecApduCmd(PORT_NAME, BAUDRATE, apduCmd2, apduCmdLen, retData, retDataLen, errMsg);
            if (ret == 0) {
                Log.i(TAG, "执行APDU命令成功，返回数据:" + bytesToHexString(retData, retDataLen[0]));
            } else {
                Log.e(TAG, "执行APDU命令失败," + getErrorDesc(ret, new byte[100]));
                break;
            }
            break;
        }
    }

    public void OnClickPowerOn(View view) {
        byte[] errMsg = new byte[201];
        byte[] retData = new byte[101];
        short[] retDataLen = new short[1];
        int ret = cpuCard.PowerOn(PORT_NAME, BAUDRATE, retData, retDataLen, errMsg);
        if (ret == 0) {
            Log.i(TAG, "CPU卡上电成功，应答数据：" + bytesToHexString(retData, retDataLen[0]));
        } else {
            Log.e(TAG, "CPU卡上电失败，" + getErrorDesc(ret, new byte[100]) + "，错误信息：" + new String(errMsg));
        }
    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_card);
        Gpio.getInstance().set_gpio(1, 66);
        cpuCard = new CPUCardDeviceImpl();
        _txt1 = findViewById(R.id.txt1_bank_card);
        _txt1.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isAppOnForeground()) {
            //记录当前已经进入后台
            isActive = false;
            Gpio.getInstance().set_gpio(0, 66);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Gpio.getInstance().set_gpio(1, 66);
        if (!isActive) {
            //从后台唤醒，进入前台
            isActive = true;
        }
    }

    @Override
    protected void onDestroy() {
        Gpio.getInstance().set_gpio(0, 66);
        super.onDestroy();
    }
}
