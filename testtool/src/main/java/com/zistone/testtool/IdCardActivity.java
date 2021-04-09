package com.zistone.testtool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
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
 * 身份证读取
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class IdCardActivity extends Activity {

    private static volatile boolean bThreadExit = true;
    private static volatile boolean bReadingCard = false;
    private static volatile boolean bFingerFlag = false;

    // private static final String PORT_NAME= "/dev/ttyS1";
    // private static final String PORT_NAME= "/dev/ttyMT3";
    // private static final int PORT_BAUDRATE = 115200;

    private static final String PORT_NAME = "/dev/ttyHSL2";
    private static final int BAUDRATE = 115200;
    private static final int READ_CARD_SUCCESS = 100; // 读卡成功
    private static final int READ_CARD_FAIL = 101; // 读卡失败
    private static final int READ_CARD_CLEAN_INFO = 102; // 刷新界面信息
    private static final int READ_CARD_THREAD_EXIT = 200; // 读卡线程退出

    private static final byte MIFARE_CARD_TEST_B_BLOCKNO = 16; // mifare卡块号 - 测试块读写
    private static final byte MIFARE_CARD_TEST_W_BLOCKNO = 17; // mifare卡块号 - 测试钱包读写

    private ImageView imageView01;
    private ReadIDCardThread idcardThread;
    private Button btnReadIDCard, btnVersion, btnExit;
    private EditText editSuccess, editFail;
    private static int successCount = 0;
    private static int failCount = 0;
    private Handler mHandler;
    private long startTime = 0, endTime = 0;

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

    private void InitEdit() {
        imageView01 = findViewById(R.id.imageView01);
        editSuccess = findViewById(R.id.edit_success);
        editFail = findViewById(R.id.edit_fail);
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
                    showIdCardInfo();
                    ++successCount;
                    editSuccess.setText(" " + successCount + " ");
                    endTime = System.currentTimeMillis() - startTime;
                    //读取成功后停止循环读取，并发出提示音
                    bThreadExit = true;
                    RingtoneManager rm = new RingtoneManager(getApplicationContext());//初始化 系统声音
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);//获取系统声音路径
                    Ringtone mRingtone = RingtoneManager.getRingtone(getApplicationContext(), uri);//通过Uri 来获取提示音的实例对象
                    mRingtone.play();
                    btnReadIDCard.setEnabled(true);
                    btnVersion.setEnabled(true);
                    btnReadIDCard.setText("读取身份证");
                } else if (msg.what == READ_CARD_FAIL) { // 读卡失败
                    ShowMessage(msg.obj.toString(), false);
                    imageView01.setImageDrawable(getResources().getDrawable(R.drawable.idcard_background));
                    imageView01.invalidate();
                    ++failCount;
                    editFail.setText(" " + failCount + " ");
                } else if (msg.what == READ_CARD_THREAD_EXIT) { // 等待读卡完成
                    ShowMessage("本次身份证读取结束", true);
                    btnReadIDCard.setEnabled(true);
                    btnVersion.setEnabled(true);
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
        Gpio.getInstance().set_gpio(1, 66);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_idcard);

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

        // btnReadIDCard.setEnabled(false);
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

    public static String unicode2String(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length / 2; i++) {
            int a = bytes[2 * i + 1];
            if (a < 0) {
                a = a + 256;
            }
            int b = bytes[2 * i];
            if (b < 0) {
                b = b + 256;
            }
            int c = (a << 8) | b;
            sb.append((char) c);
        }
        return sb.toString();
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

        // byte[] bmpData = new byte[38862];
        // idcardDevice.getPhotoBmpData(bmpData);
        // Bitmap bitmap = BitmapFactory.decodeByteArray(bmpData, 0,
        // bmpData.length);

        Bitmap bitmap = idcardDevice.getPhotoBmp();
        ImageView image_idcard = findViewById(R.id.imageView01);
        image_idcard.setImageBitmap(bitmap);
    }

    private class ReadIDCardThread extends Thread {

        @Override
        public void run() {
            synchronized (this) {
                while (!bThreadExit) {
                    if (bReadingCard) {
                        continue;
                    }

                    Message msg = new Message();
                    Message thread_msg = new Message();

                    bReadingCard = true;
                    int result = 0;
                    try {
                        startTime = System.currentTimeMillis();
                        int timeOut = 10; //超时10秒
                        result = idcardDevice.readIDCard(PORT_NAME, BAUDRATE, bFingerFlag, timeOut, message);
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
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // 判断是否人工停止
                    if (bThreadExit) {
                        thread_msg.what = READ_CARD_THREAD_EXIT;
                        mHandler.sendMessage(thread_msg);
                    }
                }//end while
            }
        }
    }

    public void OnClickReadIDCard(View view) throws Exception {
        if (btnReadIDCard.getText().equals("读取身份证")) {
            btnVersion.setEnabled(false);
            bThreadExit = false;
            btnReadIDCard.setText("停 止");
            idcardThread = new ReadIDCardThread();
            idcardThread.start();
        } else {
            bThreadExit = true;
            // 停止读卡时，先禁用按钮，等待当前一次读卡结束后再启用按钮
            btnReadIDCard.setEnabled(false);
            ShowMessage("请等待本次身份证读取结束，请稍候...", true);
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

        result = idcardDevice.getSAMVersion(PORT_NAME, BAUDRATE, samVersion, message);
        if (result == 0) {
            ShowMessage("SAM版本号：" + new String(samVersion), true);
        } else {
            ShowMessage("读取SAM版本失败," + getErrorDesc(result, message), true);
        }
    }

	/*
	public void OnClickReset(View view) {
		byte[] message = new byte[201];

		try {
			int ret = idcardDevice
					.resetSAM(edtPortName.getText().toString(),
							Integer.parseInt(edtBaudrate.getText().toString()),
							message);
			if (ret == 0) {
				ShowMessage("复位SAM模块成功", true);
			} else {
				ShowMessage("复位SAM模块失败 ,错误码：" + ret, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

    public void OnClickIDCardUid(View view) throws Exception {
        byte[] message = new byte[201];
        byte[] uid = new byte[101];

        int ret = idcardDevice.getIdCardUid(PORT_NAME, BAUDRATE, uid, message);
        if (ret == 0) {
            ShowMessage("身份证芯片序列号：" + new String(uid).trim(), true);
        } else {
            ShowMessage("读取身份证芯片序列号失败," + getErrorDesc(ret, message), true);
        }
    }

    public void OnClickGetSAMToStr(View view) throws Exception {
        byte[] message = new byte[201];
        byte[] strSAMID = new byte[51];

        int ret = idcardDevice.getSAMIDToStr(PORT_NAME, BAUDRATE, strSAMID, message);
        if (ret == 0) {
            ShowMessage("SAMID编号：" + new String(strSAMID), true);
        } else {
            ShowMessage("读取SAMID编号失败," + getErrorDesc(ret, message), true);
        }
    }

    public void OnClickPowerOn(View view) throws Exception {
        byte[] errMsg = new byte[201];
        byte[] retData = new byte[101];
        short[] retDataLen = new short[1];
        startTime = System.currentTimeMillis();
        int ret = cpuCard.PowerOn(PORT_NAME, BAUDRATE, retData, retDataLen, errMsg);
        endTime = System.currentTimeMillis() - startTime;
        if (ret == 0) {
            ShowMessage("CPU卡上电成功，应答数据：" + bytesToHexString(retData, retDataLen[0]), true);
        } else {
            ShowMessage("CPU卡上电失败," + getErrorDesc(ret, message), true);
            ShowMessage("错误信息:" + new String(errMsg), true);
        }
        ShowMessage("耗时:[" + endTime + "]毫秒", true);
    }

    public void OnClickExecApduCmd(View view) throws Exception {
        byte[] errMsg = new byte[201];
        byte[] retData = new byte[101];
        short[] retDataLen = new short[1];
        byte[] apduCmd = new byte[]{0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53, 0x59, 0x53,
                                    0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00};
        byte[] apduCmd2 = new byte[]{(byte) 0x9F, 0x26, 0x08, 0x14, (byte) 0xAD, (byte) 0xB4, (byte) 0xB7, (byte) 0xFC,
                                     (byte) 0x8B, 0x7E, (byte) 0xE5, (byte) 0x9F, 0x27, 0x01, (byte) 0x80, (byte) 0x9F,
                                     0x10, 0x13, 0x07, 0x02, 0x01, 0x03, (byte) 0xA0, (byte) 0xA0, 0x10, 0x01, 0x0A,
                                     0x01, 0x00, 0x00, 0x09, 0x38, 0x00, (byte) 0xA6, 0x6C, 0x6D, 0x78, (byte) 0x9F,
                                     0x37, 0x04, (byte) 0x9F, 0x21, 0x03, (byte) 0x9F, (byte) 0x9F, 0x36, 0x02, 0x06,
                                     (byte) 0x8F, (byte) 0x95, 0x05, 0x00, 0x00, 0x00, 0x08, 0x00, (byte) 0x9A, 0x03,
                                     0x11, 0x09, 0x01, (byte) 0x9C, 0x01, 0x33, (byte) 0x9F, 0x02, 0x06, 0x00, 0x00,
                                     0x00, 0x00, 0x00, 0x00, 0x5F, 0x2A, 0x02, 0x01, 0x56, (byte) 0x82, 0x02, 0x7C,
                                     0x00, (byte) 0x9F, 0x1A, 0x02, 0x01, 0x56, (byte) 0x9F, 0x03, 0x06, 0x00, 0x00,
                                     0x00, 0x00, 0x00, 0x00, (byte) 0x9F, 0x33, 0x03, (byte) 0xE0, (byte) 0xE1,
                                     (byte) 0xC8};
        short apduCmdLen = (short) apduCmd.length;

        startTime = System.currentTimeMillis();
        int ret = cpuCard.ExecApduCmd(PORT_NAME, BAUDRATE, apduCmd, apduCmdLen, retData, retDataLen, errMsg);
        if (ret == 0) {
            ShowMessage("执行APDU命令成功，返回数据:" + bytesToHexString(retData, retDataLen[0]), true);
        } else {
            ShowMessage("执行APDU命令失败," + getErrorDesc(ret, message), true);
        }
        endTime = System.currentTimeMillis() - startTime;
        ShowMessage("耗时:[" + endTime + "]毫秒", true);
    }

    public void OnClickPowerOff(View view) throws Exception {
        byte[] errMsg = new byte[201];
        int ret = cpuCard.Poweroff(PORT_NAME, BAUDRATE, errMsg);
        if (ret == 0) {
            ShowMessage("CPU卡下电成功", true);
        } else {
            ShowMessage("CPU卡下电失败," + getErrorDesc(ret, message), true);
        }
    }

    public void OnClickMifareActive(View view) throws Exception {
        byte[] errMsg = new byte[201];
        byte[] retData = new byte[50];
        short[] retDataLen = new short[1];
        int ret = mifarecard.CardActive(PORT_NAME, BAUDRATE, retData, retDataLen, errMsg);
        if (ret == 0) {
            ShowMessage("Mifare卡激活 成功，应答数据：" + bytesToHexString(retData, retDataLen[0]), true);
        } else {
            ShowMessage("Mifare卡激活失败," + getErrorDesc(ret, message), true);
        }
    }

    public void OnClickMifareAuthKey(View view) throws Exception {
        byte[] secretKey = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        int ret = mifarecard.AuthSecretKey(PORT_NAME, BAUDRATE, (byte) 0x00, MIFARE_CARD_TEST_B_BLOCKNO, secretKey, errMsg);
        if (ret == 0) {
            ShowMessage("密钥验证成功", true);
        } else {
            ShowMessage("密钥验证失败," + getErrorDesc(ret, message), true);
        }
    }

    public void OnClickMifareReadBlock(View view) throws Exception {
        byte[] retData = new byte[16];
        int ret = mifarecard.ReadBlock(PORT_NAME, BAUDRATE, MIFARE_CARD_TEST_B_BLOCKNO, retData, errMsg);
        if (ret == 0) {
            ShowMessage("读取块成功,返回数据:" + bytesToHexString(retData, retData.length), true);
        } else {
            ShowMessage("读取块失败," + getErrorDesc(ret, message), true);
        }
    }

    public void OnClickMifareWriteBlock(View view) throws Exception {
        byte[] blockData = new byte[]{0x12, 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE,
                                      (byte) 0xFF, 0x11, 0x22, (byte) 0x33, (byte) 0x44, 0x55, 0x66, (byte) 0x77,
                                      (byte) 0xEE,};
        int ret = mifarecard.WriteBlock(PORT_NAME, BAUDRATE, MIFARE_CARD_TEST_B_BLOCKNO, blockData, errMsg);
        if (ret == 0) {
            ShowMessage("写块成功", true);
        } else {
            ShowMessage("写块失败," + getErrorDesc(ret, message), true);
        }
    }

    public void OnClickMifareInitWallet(View view) throws Exception {
        // 低字节在前
        byte[] walletData = new byte[]{0x66, 0x12, (byte) 0xCD, (byte) 0xAB};
        int ret = mifarecard.InitWallet(PORT_NAME, BAUDRATE, MIFARE_CARD_TEST_W_BLOCKNO, walletData, errMsg);
        if (ret == 0) {
            ShowMessage("钱包初始化成功", true);
        } else {
            ShowMessage("钱包初始化失败," + getErrorDesc(ret, message), true);
        }
    }

    public void OnClickMifareReadWallet(View view) throws Exception {
        byte[] retData = new byte[4];
        int ret = mifarecard.ReadWallet(PORT_NAME, BAUDRATE, MIFARE_CARD_TEST_W_BLOCKNO, retData, errMsg);
        if (ret == 0) {
            ShowMessage("读取钱包值成功,数据:" + bytesToHexString(retData, retData.length), true);
        } else {
            ShowMessage("读取钱包值失败," + getErrorDesc(ret, message), true);
        }
    }

    public void OnClickMifareAddWallet(View view) throws Exception {
        byte[] walletData = new byte[]{0x11, 0x00, (byte) 0x00, (byte) 0x00};
        int ret = mifarecard.AddWallet(PORT_NAME, BAUDRATE, MIFARE_CARD_TEST_W_BLOCKNO, walletData, errMsg);
        if (ret == 0) {
            ShowMessage("Mifare卡钱包充值成功", true);
        } else {
            ShowMessage("Mifare卡钱包充值失败," + getErrorDesc(ret, message), true);
        }
    }

    public void OnClickMifareMinusWallet(View view) throws Exception {
        byte[] walletData = new byte[]{0x22, 0x00, 0x00, 0x00};
        int ret = mifarecard.MinusWallet(PORT_NAME, BAUDRATE, MIFARE_CARD_TEST_W_BLOCKNO, walletData, errMsg);
        if (ret == 0) {
            ShowMessage("Mifare卡钱包扣款成功", true);
        } else {
            ShowMessage("Mifare卡钱包扣款失败," + getErrorDesc(ret, message), true);
        }
    }
}
