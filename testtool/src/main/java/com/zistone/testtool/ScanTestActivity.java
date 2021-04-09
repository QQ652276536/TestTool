package com.zistone.testtool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.serialport.SerialPortManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zistone.mylibrary.util.MySoundPlayUtil;
import com.zistone.testtool.scan.ZstCallBackListen;
import com.zistone.testtool.scan.ZstScanManager;

import java.io.File;
import java.security.InvalidParameterException;

/**
 * 扫码
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class ScanTestActivity extends Activity {
    private static String TAG = "ScanTestActivity";
    private TextView mReception;
    private Button btn_scan_one, btn_scan_continuous, btn_exit;
    private SharedPreferences sp;
    private boolean inPlaySound = false;
    private boolean isScaning = false;
    private String gpio_num_str;
    private int out_time;
    private String out_time_str;
    private String serial_str;
    //	private boolean isOpened = false;
    private boolean isScanContinuous = false;
    private ZstScanManager mZstScanManager;
    private ZstCallBackListen mMyZstBarListen;
    private int scan_num = 0;
    private String _keyStr = "";

    protected void hideSoftInput(View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public class MyZstBarListen implements ZstCallBackListen {
        public void onBarReceived(final byte[] data, final int len) {
            Log.d(TAG, "isScanContinuous = " + isScanContinuous);
            Log.d(TAG, "len = " + len);
            Log.d(TAG, "data = " + data);
            if (len <= 0 || data == null) {
                return;
            }
            Message msg = mHandler.obtainMessage();
            if (data != null && data.length > 1 && len > 0) {
                msg.obj = convertCharToString(data, len);
                Log.d(TAG, "msg.obj0 = " + msg.obj);
                msg.arg1 = len;
            }

            if (isScanContinuous) {
                msg.what = 1;
                mHandler.removeMessages(1);
                mHandler.sendMessage(msg);
            } else {
                msg.what = 2;
                mHandler.removeMessages(2);
                mHandler.sendMessage(msg);
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg.what = " + msg.what);
            Log.d(TAG, "msg.obj = " + msg.obj);
            switch (msg.what) {
                case 1://轮询
                    isScaning = true;
                    Log.d(TAG, "isScaning1 = " + isScaning);
                    scan_num++;
                    if (scan_num % 20 != 0) {
                        mReception.append("[" + scan_num + "]" + msg.obj + "\n");
                    } else {
                        mReception.setText("[" + scan_num + "]" + msg.obj + "\n");
                    }
                    mZstScanManager.startBarScan(mMyZstBarListen);
                    mHandler.removeMessages(4);
                    mHandler.sendEmptyMessageDelayed(4, Integer.parseInt(out_time_str) * 1000);
                    soundPay();
                    break;
                case 2://单次扫码成功
                    soundPay();
                    mReception.append(msg.obj + "\n");
                    stopOneScan();
                    break;
                case 3://单次扫码超时
                    stopOneScan();
                    break;
                case 4://连续扫码超时
                    stopContinuousScan();
                    break;
                default:
                    break;
            }
        }
    };

    private void stopOneScan() {
        isScaning = false;
        mZstScanManager.stopBarScan();
        btn_scan_one.setText("扫描一次");
        btn_scan_continuous.setEnabled(true);
    }

    private void stopContinuousScan() {
        isScaning = false;
        isScanContinuous = false;
        mZstScanManager.stopBarScan();
        btn_scan_one.setEnabled(true);
        btn_scan_continuous.setText("持续扫描");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        String model = android.os.Build.MODEL;
        if (model.contains("msm8953")) {
            serial_str = "/dev/ttyHSL1";
            gpio_num_str = "99";
            out_time_str = "10";
        } else {
            serial_str = "/dev/ttyHSL0";
            gpio_num_str = "75";
            out_time_str = "10";
        }
        setContentView(R.layout.activity_scantest);
        Bundle bundle = this.getIntent().getExtras();
        _keyStr = bundle.getString("key");
        mReception = findViewById(R.id.EditTextReception);
        hideSoftInput(mReception);
        mMyZstBarListen = new MyZstBarListen();
        mZstScanManager = new ZstScanManager(this);
        sp = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
        gpio_num_str = sp.getString("GPIO", gpio_num_str);
        out_time_str = sp.getString("OUTTIME", out_time_str);
        btn_scan_one = findViewById(R.id.scan_one);
        btn_scan_one.setOnClickListener(v -> {
            Log.d(TAG, "isScaning = " + isScaning);
            if (!isScaning) {
                isScaning = true;
                mHandler.removeMessages(3);
                mReception.setText("");
                btn_scan_one.setText("停止扫描");
                mHandler.sendEmptyMessageDelayed(3, Integer.parseInt(out_time_str) * 1000);
                mZstScanManager.startBarScan(mMyZstBarListen);
                btn_scan_continuous.setEnabled(false);
            } else {
                stopOneScan();
            }
        });
        btn_scan_continuous = findViewById(R.id.scan_continuous);
        btn_scan_continuous.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScaning) {
                    isScaning = true;
                    isScanContinuous = true;
                    scan_num = 0;
                    mReception.setText("");
                    mZstScanManager.startBarScan(mMyZstBarListen);
                    btn_scan_one.setEnabled(false);
                    mHandler.sendEmptyMessageDelayed(4, Integer.parseInt(out_time_str) * 1000);
                    btn_scan_continuous.setText("停止扫描");
                } else {
                    stopContinuousScan();
                }
            }
        });
        btn_exit = findViewById(R.id.exit);
        btn_exit.setOnClickListener(v -> {
            if (mZstScanManager != null) {
                mZstScanManager.stopBarScan();
                mZstScanManager.closeBarDevice();
            }
            ScanTestActivity.this.finish();
            //				System.exit(0);
        });
        //		openScanDevice();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        openScanDevice();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mZstScanManager != null) {
            mZstScanManager.stopBarScan();
            mZstScanManager.closeBarDevice();
        }
        btn_scan_one.setText("扫描一次");
        btn_scan_continuous.setText("持续扫描");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (_keyStr != null && "F2".equals(_keyStr)) {
            isScaning = true;
            mHandler.removeMessages(3);
            mReception.setText("");
            btn_scan_one.setText("停止扫描");
            mHandler.sendEmptyMessageDelayed(3, Integer.parseInt(out_time_str) * 1000);
            mZstScanManager.startBarScan(mMyZstBarListen);
            btn_scan_continuous.setEnabled(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private int openScanDevice() {
        serial_str = sp.getString("DEVICE", serial_str);
        int baud_rate = Integer.decode(sp.getString("BAUDRATE", "9600"));
        int data_bits = Integer.decode(sp.getString("DATA", "8"));
        int stop_bits = Integer.decode(sp.getString("STOP", "1"));
        int flow = 0;
        int parity = 'N';
        String flow_ctrl = sp.getString("FLOW", "None");
        String parity_check = sp.getString("PARITY", "None");
        Log.d(TAG, "baud_rate = " + baud_rate);
        /* Check parameters */
        if ((serial_str.length() == 0) || (baud_rate == -1)) {
            throw new InvalidParameterException();
        }
        Log.d(TAG, "path = " + serial_str);
        if (flow_ctrl.equals("RTS/CTS"))
            flow = 1;
        else if (flow_ctrl.equals("XON/XOFF"))
            flow = 2;
        if (parity_check.equals("Odd"))
            parity = 'O';
        else if (parity_check.equals("Even"))
            parity = 'E';
        int retOpen = -1;
        if (mZstScanManager != null) {
            int gpio_num;
            Log.d(TAG, "gpio = " + gpio_num_str);
            if (gpio_num_str != null) {
                gpio_num = Integer.parseInt(gpio_num_str);
            } else {
                gpio_num = Integer.parseInt("75");
            }
            Log.e(TAG,
                    "serial_str=" + serial_str + "，baud_rate=" + baud_rate + "，flow=" + flow + "，data_bits=" + data_bits + "，stop_bits=" + stop_bits + "，parity=" + parity + "，gpio_num=" + gpio_num);
            retOpen = mZstScanManager.openbardevice(new File(serial_str), baud_rate, flow, data_bits, stop_bits, parity, gpio_num);
        }
        Log.d(TAG, "retOpen = " + retOpen);
        btn_scan_one.setEnabled(false);
        btn_scan_continuous.setEnabled(false);
        if (retOpen == SerialPortManager.RET_OPEN_SUCCESS || retOpen == SerialPortManager.RET_DEVICE_OPENED) {
            btn_scan_one.setEnabled(true);
            btn_scan_continuous.setEnabled(true);
            //			isOpened = true;
        } else if (retOpen == SerialPortManager.RET_NO_PRTMISSIONS) {
            DisplayError("您没有对该串口的读写权限");
        } else if (retOpen == SerialPortManager.RET_ERROR_CONFIG) {
            DisplayError("请先配置您的串口");
        } else {
            DisplayError("由于未知原因，串口无法打开");
        }
        return retOpen;
    }

    private void DisplayError(String resourceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanTestActivity.this);
        builder.setTitle("错误");
        builder.setMessage(resourceId);
        //		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
        //			@Override
        //			public void onClick(DialogInterface dialog, int which) {
        //				}
        //			});
        builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mZstScanManager.closeBarDevice();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        Log.d(TAG, "resultCode = " + resultCode);
        if (resultCode == 1) {
            //			isOpened = false;
            mZstScanManager.closeBarDevice();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        mHandler = null;
        super.onDestroy();
        if (mZstScanManager != null) {
            mZstScanManager.stopBarScan();
            mZstScanManager.closeBarDevice();
        }
        ScanTestActivity.this.finish();
        //		System.exit(0);
    }

    private void soundPay() {
        if (inPlaySound)
            return;
        inPlaySound = true;
        MySoundPlayUtil.SystemSoundPlay(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mZstScanManager != null) {
                mZstScanManager.stopBarScan();
                mZstScanManager.closeBarDevice();
            }
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    public static String convertCharToString(byte[] buf, int len) {
        String barcodeMsg = "";
        for (int i = 0; i < len; i++) {
            if (buf[i] != 0) {
                if (buf[i] == '\n')
                    ;
                    //					barcodeMsg += '\n';
                else if (buf[i] == '\r')
                    ;
                    //					barcodeMsg += '\r';
                else
                    barcodeMsg += (char) (buf[i]); // Asc码转换底层返回的字节数组数据
            }
        }
        return barcodeMsg;
    }

    private void SetGpioNumber() {
        final EditText et = new EditText(this);
        gpio_num_str = sp.getString("GPIO", "75");
        et.setText(gpio_num_str);
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanTestActivity.this);
        builder.setTitle("GPIO Number");
        builder.setView(et);
        //		builder.setNegativeButton("", new DialogInterface.OnClickListener() {
        //			@Override
        //			public void onClick(DialogInterface dialog, int which) {
        //				}
        //			});
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gpio_num_str = et.getText().toString();
                Editor editor = sp.edit();
                editor.putString("GPIO", gpio_num_str);
                editor.commit();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void SetOutTime() {
        final EditText et = new EditText(this);
        out_time_str = sp.getString("OUTTIME", "10");
        et.setText(out_time_str);
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanTestActivity.this);
        builder.setTitle("Out time");
        builder.setView(et);
        //		builder.setNegativeButton("", new DialogInterface.OnClickListener() {
        //			@Override
        //			public void onClick(DialogInterface dialog, int which) {
        //				}
        //			});
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                out_time_str = et.getText().toString();
                Editor editor = sp.edit();
                editor.putString("OUTTIME", out_time_str);
                editor.commit();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
