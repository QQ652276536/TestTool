package com.zistone.testtool.faceidcompare.callback;

import com.zistone.testtool.faceidcompare.model.LivenessModel;

/**
 * 人脸检测回调接口
 */
public interface FaceDetectCallBack {
    void onFaceDetectCallback(LivenessModel livenessModel);

    void onTip(int code, String msg);

    void onFaceDetectDarwCallback(LivenessModel livenessModel);
}
