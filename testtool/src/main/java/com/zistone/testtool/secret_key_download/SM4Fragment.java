package com.zistone.testtool.secret_key_download;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.zistone.mylibrary.util.MyConvertUtil;
import com.zistone.testtool.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SM4Fragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SM4Fragment";

    private EditText _edtKey, _edtData, _edtResult;
    private Button _btnEncrypt, _btnDecrypt;

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

    private boolean CheckCipher(String input) {
        String reg = "[a-zA-Z0-9_]{32}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private boolean CheckData(String input) {
        String reg = "[a-zA-Z0-9_]{32}";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    @Override
    public void onClick(View v) {
        try {
            HideKeyboard(_edtData);
            HideKeyboard(_edtResult);
            String hexKey = _edtKey.getText().toString();
            if ("".equals(hexKey) || "密钥未下载".contains(hexKey) || "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF".equals(hexKey)) {
                Toast.makeText(getActivity(), "未检测到SM4密钥，请先下载", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (v.getId()) {
                case R.id.btn_encrypt_sm4: {
                    String hexData = _edtData.getText().toString();
                    if (!CheckData(hexData)) {
                        Toast.makeText(getActivity(), "数据长度必须为16个字节", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    byte[] encrypt = Sm4Util.encryptECB(MyConvertUtil.HexStrToByteArray(hexData), MyConvertUtil.HexStrToByteArray(hexKey));
                    String hexCipher = MyConvertUtil.ByteArrayToHexStr(encrypt);
                    Log.i(TAG, "加密后的16进制密文：" + hexCipher);
                    _edtResult.setText(hexCipher);
                }
                break;
                case R.id.btn_decrypt_sm4: {
                    String hexData = _edtData.getText().toString();
                    if (!CheckData(hexData)) {
                        Toast.makeText(getActivity(), "密文长度必须为16个字节", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    byte[] decrypt = Sm4Util.decryptECB(MyConvertUtil.HexStrToByteArray(hexData), MyConvertUtil.HexStrToByteArray(hexKey));
                    String hexPlain = MyConvertUtil.ByteArrayToHexStr(decrypt);
                    Log.i(TAG, "解密后16进制数据：" + hexPlain);
                    _edtResult.setText(hexPlain);
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_secret_key_sm4, container, false);
        _edtKey = view.findViewById(R.id.edt_key_sm4);
        _edtData = view.findViewById(R.id.edt_data_sm4);
        _edtResult = view.findViewById(R.id.edt_result_sm4);
        _btnEncrypt = view.findViewById(R.id.btn_encrypt_sm4);
        _btnEncrypt.setOnClickListener(this);
        _btnDecrypt = view.findViewById(R.id.btn_decrypt_sm4);
        _btnDecrypt.setOnClickListener(this);
        return view;
    }

}
