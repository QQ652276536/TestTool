package com.zistone.testtool.secret_key_download;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.zistone.mylibrary.util.MyConvertUtil;
import com.zistone.mylibrary.util.MySerialPortManager;
import com.zistone.testtool.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "KeyFragment";
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

    private EditText _desFrgmEdtKey, _sm4FrgmEdtKey;
    private EditText _edtDesKey, _edtSm4key, _edtVer;
    private Button _btnDownload;
    private TextView _txt, _txtDes, _txtSm4;
    private String _desKey = "密钥未下载", _sm4Key = "密钥未下载";

    private void SetKey() {
        _edtDesKey.setHint(_desKey);
        _edtSm4key.setHint(_sm4Key);
        _desFrgmEdtKey.setText(_desKey);
        _sm4FrgmEdtKey.setText(_sm4Key);
    }

    public void ShowKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != inputMethodManager) {
            view.requestFocus();
            inputMethodManager.showSoftInput(view, 0);
        }
    }

    public void HideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != inputMethodManager) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean CheckDesKey(String input) {
        String reg = "[a-zA-Z0-9_]{16}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private boolean CheckSm4Key(String input) {
        String reg = "[a-zA-Z0-9]{32}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    @Override
    public void onClick(View v) {
        HideKeyboard(_edtDesKey);
        HideKeyboard(_edtSm4key);
        switch (v.getId()) {
            //写入SN
            case R.id.btn_download_key:
                String desKey = _edtDesKey.getText().toString();
                String sm4Key = _edtSm4key.getText().toString();
                if (!CheckDesKey(desKey) || !CheckSm4Key(sm4Key)) {
                    String error = "";
                    _txt.setTextColor(Color.RED);
                    if (!CheckDesKey(desKey)) {
                        error = "DES的密钥必须为字母/数字，长度为8个字节！\n";
                    }
                    if (!CheckSm4Key(sm4Key)) {
                        error += "SM4的密钥必须为字母/数字，长度为16个字节！";
                    }
                    _txt.setText(error);
                    return;
                }
                try {
                    MySerialPortManager.SendData(("AT+QCSN=\"" + desKey + sm4Key + "\"\r\n").getBytes());
                    Thread.sleep(100);
                    MySerialPortManager.SendData("AT+QCSN?\r\n".getBytes());
                    Thread.sleep(100);
                    byte[] bytes = MySerialPortManager.ReadData();
                    if (null != bytes) {
                        String result = new String(bytes);
                        Log.i(TAG, "读取到的串口数据：" + result);
                        String sn = MyConvertUtil.SubTwoStrContent(result, "\"", "\"");
                        Log.i(TAG, "截取出来的SN：" + sn);
                        _txt.setTextColor(Color.BLUE);
                        _txt.setText("密钥下载成功");
                        _desFrgmEdtKey.setText(desKey);
                        _sm4FrgmEdtKey.setText(sm4Key);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                    _txt.setTextColor(Color.RED);
                    _txt.setText("密钥下载失败");
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            MySerialPortManager.NewInstance("/dev/ttyHSL3", 115200);
            MySerialPortManager.SendData(MyConvertUtil.HexStrToByteArray("3F07000000017A9D"));
            Thread.sleep(100);
            byte[] infoBytes = MySerialPortManager.ReadData();
            if (infoBytes.length > 6) {
                final String dataStr = MyConvertUtil.ByteArrayToHexStr(infoBytes);
                final String[] resultArray = MyConvertUtil.StrAddCharacter(dataStr, 2, " ").split(" ");
                String cmdType = resultArray[5];
                final String result = resultArray[6];
                //基本参数
                if ("01".equals(cmdType)) {
                    String txt = "";
                    if (result.equals("00")) {
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
                                }
                                //软件版本
                                else if (tStr.equals(DEVICEINFOMAP.get(10))) {
                                    Log.i(TAG, "软件版本：" + v);
                                    //不显示公司Logo
                                    String tmepV = v.replace("zistone_", "");
                                    _edtVer.setText(tmepV);
                                    _btnDownload.setEnabled(true);
                                    //读取到硬件版本（设备已激活）才读取SN（密钥）
                                    MySerialPortManager.NewInstance("/dev/smd11", 115200);
                                    Log.i(TAG, "已打开串口");
                                    MySerialPortManager.SendData("AT+QCSN?\r\n".getBytes());
                                    Thread.sleep(100);
                                    byte[] bytes = MySerialPortManager.ReadData();
                                    if (null != bytes) {
                                        String sn = MyConvertUtil.SubTwoStrContent(new String(bytes), "\"", "\"");
                                        Log.i(TAG, "读取到设备的SN：" + sn);
                                        if (null != sn && sn.length() > 47) {
                                            _desKey = sn.substring(0, 16);
                                            _sm4Key = sn.substring(16, 48);
                                            if (_desKey.toUpperCase().equals("FFFFFFFFFFFFFFFF") || _sm4Key.toUpperCase().equals("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF")) {
                                                _desKey = "密钥未下载";
                                                _sm4Key = "密钥未下载";
                                            }
                                            SetKey();
                                        }
                                    }
                                }
                                txt += v + "\n";
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, e.toString());
                                _txt.setTextColor(Color.RED);
                                _txt.setText("请先激活再使用密钥");
                                SetKey();
                            }
                            i += l;
                        }
                        Log.i(TAG, "基本参数获取成功:" + txt);
                    } else {
                        Log.e(TAG, "基本参数获取失败");
                        SetKey();
                    }
                }
            } else {
                _txt.setTextColor(Color.RED);
                _txt.setText("请先激活再使用密钥");
                SetKey();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            _txt.setTextColor(Color.RED);
            _txt.setText("请先激活再使用密钥");
            SetKey();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MySerialPortManager.Close();
        Log.i(TAG, "已关闭串口");
    }

    @Override
    public void onStart() {
        super.onStart();
        _desFrgmEdtKey = getActivity().findViewById(R.id.edt_key_des);
        _sm4FrgmEdtKey = getActivity().findViewById(R.id.edt_key_sm4);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_secret_key_key, container, false);
        _edtDesKey = view.findViewById(R.id.edt_key1);
        _edtSm4key = view.findViewById(R.id.edt_key2);
        _btnDownload = view.findViewById(R.id.btn_download_key);
        _btnDownload.setOnClickListener(this::onClick);
        _txt = view.findViewById(R.id.txt_key);
        _txtDes = view.findViewById(R.id.txt_des_key);
        _txtSm4 = view.findViewById(R.id.txt_des_sm4);
        _edtVer = view.findViewById(R.id.edt_ver_key);
        return view;
    }

}
