package com.zistone.factorytest0718;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.mylibrary.BaseActivity;
import com.zistone.mylibrary.util.MyProgressDialogUtil;
import com.zistone.mylibrary.view.MyBarGraphView;
import com.zistone.mylibrary.view.MyRemoteControlButton;
import com.zistone.mylibrary.view.MySectorView;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来测试一些东西的，没有任何实际功能...
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class Test1Activity extends BaseActivity {

    private static final String TAG = "Test1Activity";

    private TextView _txt;
    private MyRemoteControlButton _myRemoteControlButton;
    private MySectorView _mySectorView;
    private MyBarGraphView _myBarGraphView;
    //柱状图的线程标识、圆形进度条的线程标识
    private boolean _threadFlag1 = false, _threadFlag2 = false;

    private List<String> _listX = new ArrayList<String>() {{
        add("贰");
        add("仨");
        add("肆");
        add("伍");
        add("陆");
        add("染");
        add("捌");
        add("玖");
        add("壹");
        add("拾");
        add("贰");
        add("仨");
        add("肆");
        add("伍");
        add("陆");
        add("染");
        add("捌");
        add("玖");
        add("壹");
        add("1");
        add("2");
        add("3");
        add("4");
        add("5");
        add("6");
        add("7");
        add("8");
        add("9");
        add("10");
    }};
    private List<Integer> _listY1 = new ArrayList<Integer>() {{
        add(10);
        add(56);
        add(1);
        add(10);
        add(78);
        add(89);
        add(0);
        add(67);
        add(23);
        add(4);
        add(78);
        add(90);
        add(3);
        add(54);
        add(12);
        add(56);
        add(89);
        add(8);
        add(69);
        add(0);
        add(9);
        add(0);
        add(7);
        add(0);
        add(36);
        add(5);
        add(3);
        add(36);
        add(234);
    }};
    private List<Integer> _listY2 = new ArrayList<Integer>() {{
        add(10);
        add(56);
        add(0);
        add(89);
        add(0);
        add(67);
        add(54);
        add(10);
        add(78);
        add(89);
        add(234);
        add(89);
        add(0);
        add(67);
        add(67);
        add(23);
        add(4);
        add(78);
        add(90);
        add(3);
        add(54);
        add(12);
        add(56);
        add(89);
        add(8);
        add(69);
        add(36);
        add(54);
        add(12);
    }};

    @Override
    protected void onDestroy() {
        _threadFlag1 = true;
        _threadFlag2 = true;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test1);
        //        SetBaseContentView(R.layout.activity_test1);

        //仿遥控器按钮
        _txt = findViewById(R.id.txt_test1);
        _txt.setText("");
        _myRemoteControlButton = findViewById(R.id.mrcb_test1);
        MyRemoteControlButton.RoundMenu roundMenu = new MyRemoteControlButton.RoundMenu();
        roundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sound_record_start1);
        roundMenu.onClickListener = v -> Toast.makeText(Test1Activity.this, "下", Toast.LENGTH_SHORT).show();
        _myRemoteControlButton.AddRoundMenu(roundMenu);

        roundMenu = new MyRemoteControlButton.RoundMenu();
        roundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sound_record_start1);
        roundMenu.onClickListener = v -> Toast.makeText(Test1Activity.this, "左", Toast.LENGTH_SHORT).show();
        _myRemoteControlButton.AddRoundMenu(roundMenu);

        roundMenu = new MyRemoteControlButton.RoundMenu();
        roundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sound_record_start1);
        roundMenu.onClickListener = v -> Toast.makeText(Test1Activity.this, "上", Toast.LENGTH_SHORT).show();
        _myRemoteControlButton.AddRoundMenu(roundMenu);

        roundMenu = new MyRemoteControlButton.RoundMenu();
        roundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sound_record_start1);
        roundMenu.onClickListener = v -> Toast.makeText(Test1Activity.this, "右", Toast.LENGTH_SHORT).show();
        _myRemoteControlButton.AddRoundMenu(roundMenu);

        Bitmap centerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera_before);
        _myRemoteControlButton.SetCenterButton(centerBitmap, v -> Toast.makeText(Test1Activity.this, "点击了中心圆圈", Toast.LENGTH_SHORT).show());

        //扇形控件
        _mySectorView = findViewById(R.id.msv_test1);
        _mySectorView.SetData(new ArrayList<MySectorView.ViewData>() {{
            add(new MySectorView.ViewData(1, "A"));
            add(new MySectorView.ViewData(1, "B"));
            add(new MySectorView.ViewData(2, "C"));
            add(new MySectorView.ViewData(2, "D"));
        }});

        //柱状图
        _myBarGraphView = findViewById(R.id.mbcv_bar_graph);
        Thread thread1 = new Thread(() -> {
            int i = 0;
            while (!_threadFlag1) {
                int finalI = i;
                runOnUiThread(() -> {
                    if (finalI % 2 == 0)
                        _myBarGraphView.SetData(_listX, _listY1);
                    else
                        _myBarGraphView.SetData(_listX, _listY2);
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
            }
        });
        thread1.start();

        //圆形进度条
        MyProgressDialogUtil.ShowCircleProgressDialog(this, true, () -> {
            Toast.makeText(Test1Activity.this, "点击外部可消失~", Toast.LENGTH_SHORT).show();
            _threadFlag2 = true;
        }, "圆形进度条...");
        MyProgressDialogUtil.SetCircleProgressMax(100);
        Thread thread2 = new Thread(() -> {
            int j = 0;
            while (!_threadFlag2 && j < 100) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                j++;
                int finalJ = j;
                runOnUiThread(() -> MyProgressDialogUtil.SetCircleProgressCurrent(finalJ));
            }
            MyProgressDialogUtil.DismissAlertDialog();
        });
        thread2.start();
    }

}
