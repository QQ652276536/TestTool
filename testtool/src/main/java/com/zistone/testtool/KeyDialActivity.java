package com.zistone.testtool;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 设置一键拨号
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class KeyDialActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "KeyDialActivity";
    private static final String REGEX = "^1[3|4|5|8][0-9]\\d{8}$";
    private TextView _txt;
    private EditText _edt;
    private String _phoneStr = "";
    private Button _btn;

    private void SavePhone() {
        MySharedPreference.SetPhone(this, _phoneStr);
        try {
            Process process = Runtime.getRuntime().exec("setprop persist.sys.sos_number " + _phoneStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keydial);
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {Manifest.permission.CALL_PHONE};
            for (String perm : permissions) {
                //进入到这里代表没有权限
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm))
                    permissionsList.add(perm);
            }
            if (!permissionsList.isEmpty())
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 1);
        }
        _txt = findViewById(R.id.textView);
        _edt = findViewById(R.id.editText);
        _btn = findViewById(R.id.btn_keydial);
        _btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_keydial:
                _phoneStr = _edt.getText().toString();
                if ("".equals(_phoneStr.trim())) {
                    _txt.setText("请输入正确的电话号码");
                    _txt.setTextColor(Color.RED);
                    Toast.makeText(this, "请输入正确的电话号码", Toast.LENGTH_SHORT).show();
                    return;
                }
                SavePhone();
                finish();
                break;
        }
    }
}
