package com.zistone.factorytest0718;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseFragment extends Fragment {

    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String ARG_PARAM3 = "param3";
    //4~12位字母数字组合
    public static final String REGEX_USERNAME = "([a-zA-Z0-9]{4,12})";
    //首位不能是数字,不能全为数字或字母,6~16位
    public static final String REGEX_PASSWORD = "^(?![0-9])(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
    //手机号
    public static final String REGEX_PHONE = "^(13[0-9]|14[5|7]|15[0|1|2|3|4|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";
    //字母+数字长度为16的倍数
    public static final String REGEX_MULTIPLE_16 = "([a-zA-Z0-9]{16})+";
    //字母+数字长度为16的倍数（Hex）
    public static final String REGEX_MULTIPLE_16_HEX = "([a-fA-F0-9]{16})+";
    //字母+数字长度为16/32/48
    public static final String REGEX_DESIGNATE_LEN = "[a-zA-Z0-9]{16}|[a-zA-Z0-9]{32}|[a-zA-Z0-9]{48}";
    //字母+数字长度为16/32/48（Hex）
    public static final String REGEX_DESIGNATE_LEN_HEX = "[a-fA-F0-9]{16}|[a-fA-F0-9]{32}|[a-fA-F0-9]{48}";

    /**
     * 通过正则表达式验证内容
     *
     * @param regex 正则表达式
     * @param str   内容
     * @return
     */
    public boolean VerifyStr(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 验证EditText的输入内容
     *
     * @param edt   控件
     * @param regex 表达式
     * @param str   提示
     */
    public boolean VerifyEdtInput(EditText edt, String regex, String str) {
        if (VerifyStr(regex, edt.getText().toString())) {
            edt.setError(null);
            return true;
        } else {
            edt.setError(str);
        }
        return false;
    }

    /**
     * 显示键盘
     *
     * @param view
     */
    public void ShowKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != inputMethodManager) {
            view.requestFocus();
            inputMethodManager.showSoftInput(view, 0);
        }
    }

    /**
     * 隐藏键盘
     *
     * @param view
     */
    public void HideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != inputMethodManager) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 到TextView顶部
     *
     * @param txt
     */
    public void TxtToTop(TextView txt) {
        txt.scrollTo(0, 0);
    }

    /**
     * 到TextView底部
     *
     * @param txt
     */
    public void TxtToBottom(TextView txt) {
        int offset = txt.getLineCount() * txt.getLineHeight();
        if (offset > txt.getHeight()) {
            txt.scrollTo(0, offset - txt.getHeight());
        }
    }

    /**
     * 清除TextView的内容
     *
     * @param txt
     */
    public void TxtClear(TextView txt) {
        txt.setText("");
        txt.scrollTo(0, 0);
    }

    /**
     * 判断点击区域是否在控件内
     *
     * @param v
     * @param event
     * @return
     */
    public boolean JudgeClickArea(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

}
