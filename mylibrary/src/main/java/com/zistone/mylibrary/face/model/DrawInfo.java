package com.zistone.mylibrary.face.model;

import android.graphics.Rect;

/**
 * 绘制内容
 */
public class DrawInfo {

    private Rect rect;
    private int sex;
    private int age;
    private int liveness;
    private int color;
    private String name;

    /**
     * @param rect     要绘制的区域
     * @param sex      性别
     * @param age      年龄
     * @param liveness 活体
     * @param color    矩形框的颜色
     * @param name     姓名，人脸库比对时用的字段
     */
    public DrawInfo(Rect rect, int sex, int age, int liveness, int color, String name) {
        this.rect = rect;
        this.sex = sex;
        this.age = age;
        this.liveness = liveness;
        this.color = color;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getLiveness() {
        return liveness;
    }

    public void setLiveness(int liveness) {
        this.liveness = liveness;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
