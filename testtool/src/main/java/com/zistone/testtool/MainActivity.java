package com.zistone.testtool;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.zistone.testtool.secret_key_download.SecretKeyActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private Button _btnIdCard, _btnIdCardTest, _btnF1Key, _btnNFC, _btnGPSCamera, _btnScanTest, _btnOtg, _btnBankCard, _btnMh1902, _btnFaceArcsoft,
            _btnSecretKey, _btnUsbCamera;
    private boolean _isPermissionRequested = false;

    /**
     * 动态请求权限
     */
    private void RequestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !_isPermissionRequested) {
            _isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {Manifest.permission.REQUEST_INSTALL_PACKAGES, Manifest.permission.INTERNET, Manifest.permission.BLUETOOTH_ADMIN,
                                    Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA};
            for (String perm : permissions) {
                //进入到这里代表没有权限
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm))
                    permissionsList.add(perm);
            }
            if (!permissionsList.isEmpty())
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 1);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_idcard: {
                startActivity(new Intent(this, IdCardActivity.class));
            }
            break;
            case R.id.btn_idcard_test: {
                startActivity(new Intent(this, IdCardTestActivity.class));
            }
            break;
            case R.id.btn_f1_key: {
                startActivity(new Intent(this, KeyDialActivity.class));
            }
            break;
            case R.id.btn_nfc: {
                startActivity(new Intent(this, NfcActivity.class));
            }
            break;
            case R.id.btn_gps_camera: {
                startActivity(new Intent(this, WatermarkCameraActivity.class));
            }
            break;
            case R.id.btn_scan_test: {
                Intent intent = new Intent(this, ScanTestActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("key", "");
                intent.putExtras(bundle);
                startActivity(intent);
            }
            break;
            case R.id.btn_otg: {
                startActivity(new Intent(this, OTGActivity.class));
            }
            break;
            case R.id.btn_bank_card: {
                startActivity(new Intent(this, BankCard.class));
            }
            break;
            case R.id.btn_mh1902:
                startActivity(new Intent(this, Mh1902Activity.class));
                break;
            case R.id.btn_face_arcsoft:
                startActivity(new Intent(this, Face_ArcSoft_Activity.class));
                break;
            case R.id.btn_secretkey:
                startActivity(new Intent(this, SecretKeyActivity.class));
                break;
            case R.id.btn_usb_camera:
//                startActivity(new Intent(this, UsbCameraActivity.class));
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            this.finish();
        }
        if (keyCode == KeyEvent.KEYCODE_F2 && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent(this, ScanTestActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("key", "F2");
            intent.putExtras(bundle);
            //            startActivity(intent);
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestPermission();
        _btnIdCard = findViewById(R.id.btn_idcard);
        _btnIdCard.setOnClickListener(this);
        _btnIdCardTest = findViewById(R.id.btn_idcard_test);
        _btnIdCardTest.setOnClickListener(this);
        _btnF1Key = findViewById(R.id.btn_f1_key);
        _btnF1Key.setOnClickListener(this);
        _btnNFC = findViewById(R.id.btn_nfc);
        _btnNFC.setOnClickListener(this);
        _btnGPSCamera = findViewById(R.id.btn_gps_camera);
        _btnGPSCamera.setOnClickListener(this);
        _btnScanTest = findViewById(R.id.btn_scan_test);
        _btnScanTest.setOnClickListener(this);
        _btnOtg = findViewById(R.id.btn_otg);
        _btnOtg.setOnClickListener(this);
        _btnBankCard = findViewById(R.id.btn_bank_card);
        _btnBankCard.setOnClickListener(this);
        _btnMh1902 = findViewById(R.id.btn_mh1902);
        _btnMh1902.setOnClickListener(this);
        _btnFaceArcsoft = findViewById(R.id.btn_face_arcsoft);
        _btnFaceArcsoft.setOnClickListener(this);
        _btnSecretKey = findViewById(R.id.btn_secretkey);
        _btnSecretKey.setOnClickListener(this);
        _btnUsbCamera = findViewById(R.id.btn_usb_camera);
        _btnUsbCamera.setOnClickListener(this);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
