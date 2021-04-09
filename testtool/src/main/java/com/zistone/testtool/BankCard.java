package com.zistone.testtool;

import android.app.ActivityManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zistone.gpio.Gpio;
import com.zistone.mylibrary.util.MyConvertUtil;
import com.zistone.mylibrary.util.MySoundPlayUtil;
import com.zz.api.CardDriverAPI;
import com.zz.impl.cpucard.CPUCardDeviceImpl;

import java.util.Arrays;
import java.util.List;

/**
 * 银行卡读取
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class BankCard extends AppCompatActivity {

    private static final String TAG = "BankCard";
    private static final String PORT_NAME = "/dev/ttyHSL2";
    private static final int BAUDRATE = 115200;

    private TextView _txt1;
    private boolean _threadFlag = false, _isPlaySound = false;
    private CPUCardDeviceImpl _cpuCard;
    private MediaPlayer _mediaPlayer = null;

    private void ThreadTask() {
        while (!_threadFlag) {
            OnClickPowerOn(null);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String bytesToHexString(byte[] bArray, int nArrayLen) {
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

    private void soundPay(int id) {
        if (_isPlaySound)
            return;
        _isPlaySound = true;
        _mediaPlayer = MediaPlayer.create(this, id);
        _mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                _isPlaySound = false;
                if (_mediaPlayer != null) {
                    _mediaPlayer.stop();
                    _mediaPlayer.release();
                    _mediaPlayer = null;
                }
            }
        });
        _mediaPlayer.start();
    }

    private String getErrorDesc(int errCode, byte[] errBuf) {
        String errMsg = new String(errBuf).toString().trim();
        switch (errCode) {
            case CardDriverAPI.ERR_CARD_SW3_WRONG:
                return "错误码:" + errCode + ",错误描述：非接卡应答结果错误，" + errMsg;
            case CardDriverAPI.ERR_CARD_SW3_MULTICARD:
                return "错误码:" + errCode + ",错误描述：射频区域有多张卡重叠，" + errMsg;
            case CardDriverAPI.ERR_FIND_CARD_TIMEOUT:
                return "错误码:" + errCode + ",错误描述：等待放卡超时，" + errMsg;
            case CardDriverAPI.ERR_PARAM_INVALID:
                return "错误码:" + errCode + ",错误描述：参数非法，" + errMsg;
            case CardDriverAPI.ERR_PORT_OPEN_FAIL:
                return "错误码:" + errCode + ",错误描述：打开串口失败，" + errMsg;
            case CardDriverAPI.ERR_SEND_DATA_FAIL:
                return "错误码:" + errCode + ",错误描述：发送数据失败，" + errMsg;
            case CardDriverAPI.ERR_WAIT_TIME_OUT:
                return "错误码:" + errCode + ",错误描述：等待应答超时，" + errMsg;
            case CardDriverAPI.ERR_RECV_DATA_FAIL:
                return "错误码:" + errCode + ",错误描述：接收应答失败，" + errMsg;
            default:
                return "错误码:" + errCode + ",错误描述：未知错误，" + errMsg;
        }
    }

    public void OnClickPowerOff(View view) {
        byte[] errMsg = new byte[201];
        int ret = _cpuCard.Poweroff(PORT_NAME, BAUDRATE, errMsg);
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
        //获取应用列表
        byte[] apduCmd = new byte[]{0x00, (byte) 0xa4, 0x04, 0x00, (byte) 0x0e, 0x32, 0x50, 0x41, 0x59, 0x2e, 0x53, 0x59, 0x53, 0x2e, 0x44, 0x44,
                                    0x46, 0x30, 0x31, 0x00};
        byte[] apduCmd1 = new byte[]{0x00, (byte) 0xa4, 0x04, 0x00, 0x08, (byte) 0xa0, 0x00, 0x00, 0x03, 0x33, 0x01, 0x01, 0x01, 0x00};
        byte[] apduCmd2 = new byte[]{0x00, (byte) 0xb2, 0x01, 0x0c, 0x00};
        short apduCmdLen = (short) apduCmd.length;
        int ret;
        while (true) {
            ret = _cpuCard.ExecApduCmd(PORT_NAME, BAUDRATE, apduCmd, apduCmdLen, retData, retDataLen, errMsg);
            if (ret == 0) {
                Log.i(TAG, "执行APDU命令成功，返回数据:" + bytesToHexString(retData, retDataLen[0]));
            } else {
                Log.e(TAG, "执行APDU命令失败，" + getErrorDesc(ret, new byte[100]));
                break;
            }
            apduCmdLen = (short) apduCmd1.length;
            ret = _cpuCard.ExecApduCmd(PORT_NAME, BAUDRATE, apduCmd1, apduCmdLen, retData, retDataLen, errMsg);
            if (ret == 0) {
                Log.i(TAG, "执行APDU命令成功，返回数据:" + bytesToHexString(retData, retDataLen[0]));
            } else {
                Log.e(TAG, "执行APDU命令失败，" + getErrorDesc(ret, new byte[100]));
                break;
            }
            apduCmdLen = (short) apduCmd2.length;
            ret = _cpuCard.ExecApduCmd(PORT_NAME, BAUDRATE, apduCmd2, apduCmdLen, retData, retDataLen, errMsg);
            if (ret == 0) {
                String result = bytesToHexString(retData, retDataLen[0]);
                Log.i(TAG, "执行APDU命令成功，返回数据:" + result);
                if (result.startsWith("70") && result.endsWith("9000")) {
                    String[] resultArray = MyConvertUtil.StrAddCharacter(result, 2, " ").split(" ");
                    String[] cardArray = new String[8];
                    System.arraycopy(resultArray, 4, cardArray, 0, 8);
                    String card = Arrays.toString(cardArray).replaceAll("[\\s|\\[|\\]|,]", "");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MySoundPlayUtil.SystemSoundPlay(BankCard.this);
                            _txt1.setText(card);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            _txt1.setText("未读取到卡号");
                        }
                    });
                }
                OnClickPowerOff(null);
            } else {
                Log.e(TAG, "执行APDU命令失败，" + getErrorDesc(ret, new byte[100]));
                break;
            }
            break;
        }
    }

    public void OnClickPowerOn(View view) {
        byte[] errMsg = new byte[201];
        byte[] retData = new byte[101];
        short[] retDataLen = new short[1];
        int ret = _cpuCard.PowerOn(PORT_NAME, BAUDRATE, retData, retDataLen, errMsg);
        if (ret == 0) {
            Log.i(TAG, "CPU卡上电成功，应答数据：" + bytesToHexString(retData, retDataLen[0]));
            OnClickExecApduCmd(null);
        } else {
            Log.e(TAG, "CPU卡上电失败，" + getErrorDesc(ret, new byte[100]));
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
        _cpuCard = new CPUCardDeviceImpl();
        _txt1 = findViewById(R.id.txt1_bank_card);
        _txt1.setMovementMethod(ScrollingMovementMethod.getInstance());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(() -> ThreadTask());
        thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        _threadFlag = true;
        //记录当前已经进入后台
        if (!isAppOnForeground()) {
            Gpio.getInstance().set_gpio(0, 66);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Gpio.getInstance().set_gpio(1, 66);
        _threadFlag = false;
        Thread thread = new Thread(() -> ThreadTask());
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _threadFlag = true;
        Gpio.getInstance().set_gpio(0, 66);
    }
}
