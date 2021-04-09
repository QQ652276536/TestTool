package com.zistone.testtool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.zistone.gpio.Gpio;
import com.zz.api.CardDriverAPI;
import com.zz.impl.cpucard.CPUCardDeviceImpl;
import com.zz.impl.idcard.IDCardDeviceImpl;
import com.zz.impl.idcard.IDCardInterface;
import com.zz.impl.mifarecard.MifareCardDeviceImpl;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 身份证压力测试
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class IdCardTestActivity extends Activity {

    private static volatile boolean bThreadExit = true;
    private static volatile boolean bReadingCard = false;
    private static volatile boolean bFingerFlag = false;

    // private static final String PORT_NAME= "/dev/ttyS1";
    // private static final String PORT_NAME= "/dev/ttyMT3";
    // private static final int PORT_BAUDRATE = 115200;

    private static final int READ_CARD_SUCCESS = 100; // 读卡成功
    private static final int READ_CARD_FAIL = 101; // 读卡失败
    private static final int READ_CARD_CLEAN_INFO = 102; // 刷新界面信息
    private static final int READ_CARD_THREAD_EXIT = 200; // 读卡线程退出

    private static final byte MIFARE_CARD_TEST_B_BLOCKNO = 16; // mifare卡块号 - 测试块读写
    private static final byte MIFARE_CARD_TEST_W_BLOCKNO = 17; // mifare卡块号 - 测试钱包读写

    private ImageView imageView01;
    private ReadIDCardThread idcardThread;
    private Button btnReadIDCard, btnVersion, btnExit;
    private EditText editSuccess, editFail, editTime, _editCount;
    private static int successCount = 0;
    private static int failCount = 0;
    private Handler mHandler;

    byte[] textData = new byte[256];
    byte[] photoData = new byte[1024];
    byte[] fingerData = new byte[1024];
    byte[] message = new byte[100];
    int[] textDataLen = new int[1];
    int[] photoDataLen = new int[1];
    int[] fingerDataLen = new int[1];
    byte[] errMsg = new byte[201];

    private IDCardInterface idcardDevice;
    private CPUCardDeviceImpl cpuCard;
    private MifareCardDeviceImpl mifarecard;
    private boolean isActive = false;

    private long _startTime, _endTime;
    private boolean _stopReadFlag = false;
    private int _testCount = 1000;

    private void InitEdit() {
        imageView01 = findViewById(R.id.imageView01);
        editSuccess = findViewById(R.id.edit_success);
        editTime = findViewById(R.id.edit_time);
        editFail = findViewById(R.id.edit_fail);
        _editCount = findViewById(R.id.edit_count);
    }

    private void InitButton() {
        btnReadIDCard = findViewById(R.id.btn_read_idcard);
        btnVersion = findViewById(R.id.btn_readversion);
        btnExit = findViewById(R.id.btn_exit);
    }

    private void InitButtonTouchListener() {
        // 实现按钮按下及弹起时的效果
        btnReadIDCard.setOnTouchListener(myTouchListener);
        btnVersion.setOnTouchListener(myTouchListener);
        btnExit.setOnTouchListener(myTouchListener);
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

    @SuppressLint("HandlerLeak")
    private void InitHandlerMessage() {
        // 初始化handle，绑定在主线程中的队列消息中
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                // 接收消息
                if (msg.what == READ_CARD_SUCCESS) { // 读卡成功
                    _endTime = System.currentTimeMillis();
                    long time = _endTime - _startTime;
                    editTime.setText(time + "毫秒");
                    showIdCardInfo();
                    ++successCount;
                    if ((successCount + failCount) == _testCount) {
                        bThreadExit = true;
                        // 停止读卡时，先禁用按钮，等待当前一次读卡结束后再启用按钮
                        btnReadIDCard.setEnabled(false);
                        ShowMessage("正在等待本次读卡结束，请稍候...", true);
                    }
                    editSuccess.setText(" " + successCount + " ");
                } else if (msg.what == READ_CARD_FAIL) { // 读卡失败
                    if ((successCount + failCount) == _testCount) {
                        bThreadExit = true;
                        // 停止读卡时，先禁用按钮，等待当前一次读卡结束后再启用按钮
                        btnReadIDCard.setEnabled(false);
                        ShowMessage("正在等待本次读卡结束，请稍候...", true);
                    }
                    ShowMessage(msg.obj.toString(), false);
                    imageView01.setImageDrawable(getResources().getDrawable(R.drawable.idcard_background));
                    imageView01.invalidate();
                    ++failCount;
                    editFail.setText(" " + failCount + " ");
                } else if (msg.what == READ_CARD_THREAD_EXIT) { // 等待读卡完成
                    ShowMessage("本次读卡结束", true);
                    btnVersion.setEnabled(true);
                    btnReadIDCard.setEnabled(true);
                    btnReadIDCard.setText("读取身份证");
                } else if (msg.what == READ_CARD_CLEAN_INFO) { // 轮询间隔太短时，不适合每次都清屏
                    ShowMessage("", false);
                    imageView01.setImageDrawable(getResources().getDrawable(R.drawable.idcard_background));
                    imageView01.invalidate();
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Gpio.getInstance().set_gpio(1, 66);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_idcardtest);

        idcardDevice = new IDCardDeviceImpl();
        cpuCard = new CPUCardDeviceImpl();
        mifarecard = new MifareCardDeviceImpl();

        InitEdit();
        InitButton();
        InitHandlerMessage();
        InitButtonTouchListener();

        btnReadIDCard.setFocusable(true);
        btnReadIDCard.setFocusableInTouchMode(true);
        btnReadIDCard.requestFocus();
        btnReadIDCard.requestFocusFromTouch();
    }

    private OnTouchListener myTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Button upStepBtn = (Button) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                upStepBtn.setBackgroundResource(R.drawable.button_down);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                upStepBtn.setBackgroundResource(R.drawable.button);
            }
            return false;
        }
    };

    @Override
    protected void onDestroy() {
        _stopReadFlag = true;
        Gpio.getInstance().set_gpio(0, 66);
        super.onDestroy();
    }

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

    private void ShowMessage(String strMsg, Boolean bAdd) {
        EditText edit_show_msg = findViewById(R.id.edit_show_msg);
        if (bAdd) {
            String strShowMsg = edit_show_msg.getText().toString();
            strMsg = strShowMsg + strMsg;
        }
        edit_show_msg.setText(strMsg + "\r\n");
        ScrollView scrollView_show_msg = findViewById(R.id.scrollView_show_msg);
        scrollToBottom(scrollView_show_msg, edit_show_msg);
    }

    public static void scrollToBottom(final View scroll, final View inner) {
        Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            public void run() {
                if (scroll == null || inner == null) {
                    return;
                }
                int offset = inner.getMeasuredHeight() - scroll.getHeight();
                if (offset < 0) {
                    offset = 0;
                }
                scroll.scrollTo(0, offset);
            }
        });
    }

    public void showIdCardInfo() {
        if (idcardDevice.getIDCardType() == 0) {
            ShowMessage("姓名：" + idcardDevice.getName() + "	性别:" + idcardDevice.getSex(), false);
            ShowMessage("民族：" + idcardDevice.getNation() + "	出生日期：" + idcardDevice.getBorn(), true);
            ShowMessage("住址：" + idcardDevice.getAddress(), true);
            ShowMessage("身份证号码：" + idcardDevice.getIdNumber(), true);
            ShowMessage("签发机关：" + idcardDevice.getIssueOffice(), true);
            ShowMessage("有效期限:" + idcardDevice.getBeginDate() + "-" + idcardDevice.getEndDate(), true);
        } else if (idcardDevice.getIDCardType() == 1) {
            ShowMessage("英文姓名：" + idcardDevice.getEnglishName() + "	性别:" + idcardDevice.getSex(), false);
            ShowMessage("永久居留证号码：" + idcardDevice.getIdNumber(), true);
            ShowMessage("国籍及所在地区代码：" + idcardDevice.getAreaCode(), true);
            ShowMessage("中文姓名：" + idcardDevice.getName(), true);
            ShowMessage("出生日期：" + idcardDevice.getBorn(), true);
            ShowMessage("证件版本号：" + idcardDevice.getCardVersionNum(), true);
            ShowMessage("当次申请受理机关代码:" + idcardDevice.getIssueOffice(), true);
            ShowMessage("有效期限:" + idcardDevice.getBeginDate() + "-" + idcardDevice.getEndDate(), true);
        } else if (idcardDevice.getIDCardType() == 2) {
            ShowMessage("姓名：" + idcardDevice.getName() + "	性别:" + idcardDevice.getSex(), false);
            ShowMessage("出生日期：" + idcardDevice.getBorn(), true);
            ShowMessage("住址：" + idcardDevice.getAddress(), true);
            ShowMessage("身份证号码：" + idcardDevice.getIdNumber(), true);
            ShowMessage("签发机关：" + idcardDevice.getIssueOffice(), true);
            ShowMessage("有效期限:" + idcardDevice.getBeginDate() + "-" + idcardDevice.getEndDate(), true);
            ShowMessage("签发次数：" + idcardDevice.getIssueCount(), true);
            ShowMessage("通行证号码：" + idcardDevice.getPassportNum(), true);
        } else {
            ShowMessage("证件类型状态非法", true);
        }
        if (bFingerFlag) {
            if (idcardDevice.getFingerLen() == 0) {
                ShowMessage("卡片内无指纹", true);
            } else {
                ShowMessage("卡片内有指纹", true);
            }
        } else {
            ShowMessage("未读指纹数据", true);
        }
        Bitmap bitmap = idcardDevice.getPhotoBmp();
        ImageView image_idcard = findViewById(R.id.imageView01);
        image_idcard.setImageBitmap(bitmap);
    }

    private class ReadIDCardThread extends Thread {

        @Override
        public void run() {
            synchronized (this) {
                while (!bThreadExit) {
                    if (!bReadingCard) {
                        Message msg = new Message();
                        Message thread_msg = new Message();

                        bReadingCard = true;
                        int result = 0;
                        try {
                            int timeOut = 10; //超时10秒
                            _startTime = System.currentTimeMillis();
                            result = idcardDevice.readIDCard("/dev/ttyHSL2", 115200, bFingerFlag, timeOut, message);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        if (result != 0) {
                            bReadingCard = false;
                            try {
                                String str = new String(message, "GBK");
                                msg.what = READ_CARD_FAIL;
                                msg.obj = String.format("错误信息:%s", str);
                                mHandler.sendMessage(msg);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        } else {
                            bReadingCard = false;
                            msg.what = READ_CARD_SUCCESS;
                            mHandler.sendMessage(msg);
                        }

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // 判断是否人工停止
                        if (bThreadExit) {
                            thread_msg.what = READ_CARD_THREAD_EXIT;
                            mHandler.sendMessage(thread_msg);
                        }
                    }
                }

            }
        }
    }

    public void OnClickReadIDCard(View view) {
        if (btnReadIDCard.getText().equals("读取身份证")) {
            successCount = 0;
            failCount = 0;
            editSuccess.setText(" " + successCount + " ");
            editFail.setText(" " + failCount + " ");
            try {
                _testCount = Integer.valueOf(_editCount.getText().toString());
            } catch (Exception e) {
                _testCount = 1000;
                e.printStackTrace();
                ShowMessage(e.toString(), false);
                return;
            }
            btnVersion.setEnabled(false);
            bThreadExit = false;
            btnReadIDCard.setText("停 止");
            idcardThread = new ReadIDCardThread();
            idcardThread.start();
        } else {
            bThreadExit = true;
            // 停止读卡时，先禁用按钮，等待当前一次读卡结束后再启用按钮
            btnReadIDCard.setEnabled(false);
            ShowMessage("正在等待本次读卡结束，请稍候...", true);
        }
    }

    public void OnClickExit(View view) {
        this.finish();
    }

    String getErrorDesc(int errCode, byte[] errBuf) {
        String errMsg = new String(errBuf).trim();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public void OnClickReadVersion(View view) throws Exception {
        int result = 0;
        byte[] message = new byte[201];
        byte[] samVersion = new byte[201];

        result = idcardDevice.getSAMVersion("/dev/ttyHSL2", 115200, samVersion, message);
        if (result == 0) {
            ShowMessage("SAM版本号：" + new String(samVersion), true);
        } else {
            ShowMessage("读取SAM版本失败," + getErrorDesc(result, message), true);
        }
    }

}
