package com.zistone.testtool;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zistone.mylibrary.util.MyProgressDialogUtil;

/**
 * NFC
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class NfcActivity extends AppCompatActivity {

    private static final String TAG = "NfcActivity";
    private NfcAdapter _nfcAdapter;
    private PendingIntent _pendingIntent;
    private TextView _txt1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        //初始化NfcAdapter
        _nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //初始化PendingIntent，当有NFC设备连接上的时候，就交给当前Activity处理
        _pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
        _txt1 = findViewById(R.id.txt1_nfc);
        _txt1.setGravity(Gravity.CENTER);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 当前app正在前端界面运行，这个时候有intent发送过来，那么系统就会调用onNewIntent回调方法，将intent传送过来
        // 我们只需要在这里检验这个intent是否是NFC相关的intent，如果是，就调用处理方法
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
        }
        processIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            _nfcAdapter.enableForegroundDispatch(this, _pendingIntent, null, null);
        } catch (Exception e) {
            MyProgressDialogUtil.ShowWarning(this, "知道了", "警告", "该设备不支NFC，无法使用此功能！", false, () -> {
                finish();
            });
        }
    }

    private void processIntent(Intent intent) {
        //取出封装在intent中的TAG
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tagFromIntent);
        if (ndef != null) {
            String str1 = ndef.getType();
            Log.e(TAG, "【ndef.getType】" + str1);
        }
        String id = ByteArrayToHexString(tagFromIntent.getId());
        _txt1.setText("序列号\r\n" + id);
        Log.e(TAG, "【getId】" + id);
        String[] strArray = tagFromIntent.getTechList();
        Log.e(TAG, "【tagFromIntent.getTechList】");
        for (String temp : strArray) {
            Log.e(TAG, temp);
        }
        String strx = readTagClassic(tagFromIntent);
        if (null != strx)
            _txt1.append("\r\n" + strx);
    }

    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    public String readTagClassic(Tag tag) {
        MifareClassic mfc = null;
        // 读取TAG
        try {
            mfc = MifareClassic.get(tag);
            //            mfc.connect();
            String metaInfo = "";
            int type = mfc.getType();// 获取TAG的类型
            int sectorCount = mfc.getSectorCount();// 获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共" + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize() + "B\n";
            //            for (int j = 0; j < sectorCount; j++) {
            //                // Authenticate a sector with key A.
            //
            //                auth = mfc.authenticateSectorWithKeyA(j,
            //                        MifareClassic.KEY_DEFAULT);
            //                int bCount;
            //                int bIndex;
            //                if (auth) {
            //                    metaInfo += "Sector " + j + ":验证成功\n";
            //                    // 读取扇区中的块
            //                    bCount = mfc.getBlockCountInSector(j);
            //                    bIndex = mfc.sectorToBlock(j);
            //                    for (int i = 0; i < bCount; i++) {
            //                        byte[] data = mfc.readBlock(bIndex);
            //                        metaInfo += "Block " + bIndex + " : "
            //                                + ByteArrayToHexString(data) + "\n";
            //                        bIndex++;
            //                    }
            //                } else {
            //                    metaInfo += "Sector " + j + ":验证失败\n";
            //                }
            //            }
            return metaInfo;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != mfc)
                    mfc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }
}
