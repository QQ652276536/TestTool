package com.zistone.mylibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * 扇形图控件
 *
 * @author LiWei
 * @date 2020/9/15 17:44
 * @email 652276536@qq.com
 */
public class MySectorView extends View {

    private static final String TAG = "MySectorView";

    private int[] _colors = {Color.DKGRAY, Color.CYAN, Color.RED, Color.GREEN};
    private Paint _paint, _linePaint;
    private ArrayList<ViewData> _viewDatas = new ArrayList<>();
    private int _width;
    private int _height;
    private RectF _rectF;

    public static class ViewData {
        public String name;
        public int value;
        public int color;
        public float percentage;
        public float angle;

        public ViewData(int value, String name) {
            this.value = value;
            this.name = name;
        }
    }

    public MySectorView(Context context) {
        super(context);
        InitPaint();
    }

    public MySectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        InitPaint();
    }

    public MySectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        InitPaint();
    }

    public void SetData(ArrayList<ViewData> _viewDatas) {
        this._viewDatas = _viewDatas;
        InitData();
    }

    private void InitPaint() {
        _paint = new Paint();
        _paint.setAntiAlias(true);
        _paint.setColor(Color.WHITE);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setTextSize(60);
        _linePaint = new Paint();
        _linePaint.setAntiAlias(true);
        _linePaint.setColor(Color.WHITE);
        _linePaint.setStyle(Paint.Style.STROKE);
        _linePaint.setStrokeWidth(2);
        _rectF = new RectF();
    }

    private void InitData() {
        if (null == _viewDatas || _viewDatas.size() == 0) {
            return;
        }
        float sumValue = 0;
        for (int i = 0; i < _viewDatas.size(); i++) {
            ViewData viewData = _viewDatas.get(i);
            sumValue += viewData.value;
            viewData.color = _colors[i];
        }
        for (ViewData data : _viewDatas) {
            //计算百分比
            float percentage = data.value / sumValue;
            //对应的角度
            float angle = percentage * 360;
            data.percentage = percentage;
            data.angle = angle;
        }
    }

    @Override
    protected void onSizeChanged(int _width, int _height, int oldw, int oldh) {
        super.onSizeChanged(_width, _height, oldw, oldh);
        this._width = _width;
        this._height = _height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "宽：" + _width + "，高：" + _height);
        canvas.translate(_width / 2, _height / 2);
        float currentStartAngle = 0;
        //饼状图半径(取宽高里最小的值)
        float r = (float) (Math.min(_width, _height) / 2);
        Log.i(TAG, "饼状图半径：" + r);
        //设置将要用来画扇形的矩形的轮廓
        _rectF.set(-r, -r, r, r);
        for (int i = 0; i < _viewDatas.size(); i++) {
            ViewData data = _viewDatas.get(i);
            _paint.setColor(data.color);
            //绘制扇形(绘制圆弧)
            canvas.drawArc(_rectF, currentStartAngle, data.angle, true, _paint);
            //绘制分割线
            canvas.drawArc(_rectF, currentStartAngle, data.angle, true, _linePaint);
            //绘制扇形上文字
            float textAngle = currentStartAngle + data.angle / 2;
            _paint.setColor(Color.WHITE);
            //后面的偏移量是自己看着加的
            float x = (float) (r / 2 * Math.cos(textAngle * Math.PI / 180)) - 15;
            float y = (float) (r / 2 * Math.sin(textAngle * Math.PI / 180)) + 20;
            canvas.drawText(data.name, x, y, _paint);
            Log.i(TAG, "第" + (i + 1) + "个扇形的角度：" + data.angle + "，文字的角度：" + textAngle + "，文字的坐标：" + x + "，" + y);
            //改变起始角度
            currentStartAngle += data.angle;
        }
    }

}