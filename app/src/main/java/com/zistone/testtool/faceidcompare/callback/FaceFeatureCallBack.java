package com.zistone.testtool.faceidcompare.callback;

/**
 * 人脸特征抽取回调接口
 */
public interface FaceFeatureCallBack {
    void onFaceFeatureCallBack(float featureSize, byte[] feature);
}
