package com.zistone.testtool;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.zistone.testtool.util.MyInstallAPKUtil;
import com.zistone.testtool.util.MyProgressDialogUtil;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {
    private static final String TAG = "IdCardActivity";
    private static final String LOCAL_PATH = "sdcard";
    private static final String CAMERA_PACKAGE = "com.tencent.zebra";

    private Button _btnIdCard, _btnIdCardTest, _btnF1Key, _btnNFC, _btnGPSCamera, _btnFaceId, _btnScanTest, _btnOtg;
    private boolean _isPermissionRequested = false;

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
        _btnFaceId = findViewById(R.id.btn_face_id);
        _btnFaceId.setOnClickListener(this);
        _btnScanTest = findViewById(R.id.btn_scan_test);
        _btnScanTest.setOnClickListener(this);
        _btnOtg = findViewById(R.id.btn_otg);
        _btnOtg.setOnClickListener(this);
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
            case R.id.btn_face_id: {
                startActivity(new Intent(this, FaceMenuActivity.class));
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
        }
    }

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

    private void Install(String title, String content, String apk) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setNegativeButton("好的", (dialog, which) -> {
            try {
                MyProgressDialogUtil.ShowProgressDialog(this, false, "请稍等...");
                File file = MyInstallAPKUtil.CopyFromAssets(this, apk, LOCAL_PATH);
                MyProgressDialogUtil.Dismiss();
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(this, this.getPackageName() + ".fileprovider", file);
                } else {
                    uri = Uri.fromFile(file);
                }
                MyInstallAPKUtil.Install(uri, this);
            } catch (Exception e) {
                e.printStackTrace();
                MyProgressDialogUtil.Dismiss();
            }
        });
        builder.setPositiveButton("不了", (dialog, which) -> {
        });
        builder.show();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
}
