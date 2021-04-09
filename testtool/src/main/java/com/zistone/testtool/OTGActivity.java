package com.zistone.testtool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * OTG
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class OTGActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "OTGActivity";
    private Button _btnOpen, _btnClose, _btnFile;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otg);
        _btnOpen = findViewById(R.id.btnOpen_otg);
        _btnOpen.setOnClickListener(this);
        _btnClose = findViewById(R.id.btnClose_otg);
        _btnClose.setOnClickListener(this);
        _btnFile = findViewById(R.id.btnFile_otg);
        _btnFile.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.btnOpen_otg:
                    Runtime.getRuntime().exec("gpio-test 1 1");
                    break;
                case R.id.btnClose_otg:
                    Runtime.getRuntime().exec("gpio-test 1 0");
                    break;
                case R.id.btnFile_otg:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent, 1);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
