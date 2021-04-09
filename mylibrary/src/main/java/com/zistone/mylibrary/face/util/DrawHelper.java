package com.zistone.mylibrary.face.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.hardware.Camera;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.zistone.mylibrary.face.model.DrawInfo;
import com.zistone.mylibrary.face.widget.FaceRectView;

import java.util.List;

/**
 * 绘制人脸框帮助类，用于在{@link com.zistone.mylibrary.face.widget.FaceRectView}上绘制矩形
 */
public class DrawHelper {

    //预览宽度
    private int _previewWidth;
    //预览高度
    private int _previewHeight;
    //绘制控件的宽度
    private int _canvasWidth;
    //绘制控件的高度
    private int _canvasHeight;
    //旋转角度
    private int _cameraDisplayOrientation;
    //相机ID
    private int _cameraId;
    //是否水平镜像显示（若相机是镜像显示的，设为true，用于纠正）
    private boolean _isMirror;
    //为兼容部分设备使用，水平再次镜像
    private boolean _mirrorHorizontal;
    //为兼容部分设备使用，垂直再次镜像
    private boolean _mirrorVertical;

    /**
     * 人对核验的时候绘制人脸框
     *
     * @param canvas
     * @param rect
     * @param color
     * @param faceRectThickness
     */
    public static void DrawFaceRect(Canvas canvas, Rect rect, int color, int faceRectThickness) {
        if (canvas == null || rect == null) {
            return;
        }
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(faceRectThickness);
        paint.setColor(color);
        Path mPath = new Path();
        mPath.moveTo(rect.left, rect.top + rect.height() / 4);
        mPath.lineTo(rect.left, rect.top);
        mPath.lineTo(rect.left + rect.width() / 4, rect.top);
        mPath.moveTo(rect.right - rect.width() / 4, rect.top);
        mPath.lineTo(rect.right, rect.top);
        mPath.lineTo(rect.right, rect.top + rect.height() / 4);
        mPath.moveTo(rect.right, rect.bottom - rect.height() / 4);
        mPath.lineTo(rect.right, rect.bottom);
        mPath.lineTo(rect.right - rect.width() / 4, rect.bottom);
        mPath.moveTo(rect.left + rect.width() / 4, rect.bottom);
        mPath.lineTo(rect.left, rect.bottom);
        mPath.lineTo(rect.left, rect.bottom - rect.height() / 4);
        canvas.drawPath(mPath, paint);
    }

    public static Rect AdjustRect(Rect rect, int previewWidth, int previewHeight, int canvasWidth, int canvasHeight, int cameraOri, int mCameraId) {
        if (rect == null) {
            return null;
        }
        if (canvasWidth < canvasHeight) {
            int t = previewHeight;
            previewHeight = previewWidth;
            previewWidth = t;
        }
        float widthRatio = (float) canvasWidth / (float) previewWidth;
        float heightRatio = (float) canvasHeight / (float) previewHeight;

        if (cameraOri == 0 || cameraOri == 180) {
            rect.left *= widthRatio;
            rect.right *= widthRatio;
            rect.top *= heightRatio;
            rect.bottom *= heightRatio;
        } else {
            rect.left *= heightRatio;
            rect.right *= heightRatio;
            rect.top *= widthRatio;
            rect.bottom *= widthRatio;
        }
        Rect newRect = new Rect();
        switch (cameraOri) {
            case 0:
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = canvasWidth - rect.left;
                    newRect.right = canvasWidth - rect.right;
                } else {
                    newRect.left = rect.left;
                    newRect.right = rect.right;
                }
                newRect.top = rect.top;
                newRect.bottom = rect.bottom;
                break;
            case 90:
                newRect.right = canvasWidth - rect.top;
                newRect.left = canvasWidth - rect.bottom;
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = canvasHeight - rect.left;
                    newRect.bottom = canvasHeight - rect.right;
                } else {
                    newRect.top = rect.left;
                    newRect.bottom = rect.right;
                }
                break;
            case 180:
                newRect.top = canvasHeight - rect.bottom;
                newRect.bottom = canvasHeight - rect.top;
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = rect.left;
                    newRect.right = rect.right;
                } else {
                    newRect.left = canvasWidth - rect.right;
                    newRect.right = canvasWidth - rect.left;
                }
                break;
            case 270:
                newRect.left = rect.top;
                newRect.right = rect.bottom;
                if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = rect.left;
                    newRect.bottom = rect.right;
                } else {
                    newRect.top = canvasHeight - rect.right;
                    newRect.bottom = canvasHeight - rect.left;
                }
                break;
            default:
                break;
        }
        return newRect;
    }

    /**
     * 创建一个绘制辅助类对象，并且设置绘制相关的参数
     *
     * @param previewWidth             预览宽度
     * @param previewHeight            预览高度
     * @param canvasWidth              绘制控件的宽度
     * @param canvasHeight             绘制控件的高度
     * @param cameraDisplayOrientation 旋转角度
     * @param cameraId                 相机ID
     * @param isMirror                 是否水平镜像显示（若相机是镜像显示的，设为true，用于纠正）
     * @param mirrorHorizontal         为兼容部分设备使用，水平再次镜像
     * @param mirrorVertical           为兼容部分设备使用，垂直再次镜像
     */
    public DrawHelper(int previewWidth, int previewHeight, int canvasWidth, int canvasHeight, int cameraDisplayOrientation, int cameraId, boolean isMirror, boolean mirrorHorizontal, boolean mirrorVertical) {
        this._previewWidth = previewWidth;
        this._previewHeight = previewHeight;
        this._canvasWidth = canvasWidth;
        this._canvasHeight = canvasHeight;
        this._cameraDisplayOrientation = cameraDisplayOrientation;
        this._cameraId = cameraId;
        this._isMirror = isMirror;
        this._mirrorHorizontal = mirrorHorizontal;
        this._mirrorVertical = mirrorVertical;
    }

    public void Draw(FaceRectView faceRectView, List<DrawInfo> drawInfoList) {
        if (faceRectView == null) {
            return;
        }
        faceRectView.ClearFaceInfo();
        if (drawInfoList == null || drawInfoList.size() == 0) {
            return;
        }
        faceRectView.AddFaceInfo(drawInfoList);
    }

    /**
     * 在人脸属性（视频）检测的时候绘制人脸框
     *
     * @param ftRect 人脸框
     * @return 调整后的需要被绘制到View上的rect
     */
    public Rect AdjustRect(Rect ftRect) {
        int previewWidth = this._previewWidth;
        int previewHeight = this._previewHeight;
        int canvasWidth = this._canvasWidth;
        int canvasHeight = this._canvasHeight;
        int cameraDisplayOrientation = this._cameraDisplayOrientation;
        int cameraId = this._cameraId;
        boolean isMirror = this._isMirror;
        boolean mirrorHorizontal = this._mirrorHorizontal;
        boolean mirrorVertical = this._mirrorVertical;
        if (ftRect == null) {
            return null;
        }
        Rect rect = new Rect(ftRect);
        float horizontalRatio;
        float verticalRatio;
        if (cameraDisplayOrientation % 180 == 0) {
            horizontalRatio = (float) canvasWidth / (float) previewWidth;
            verticalRatio = (float) canvasHeight / (float) previewHeight;
        } else {
            horizontalRatio = (float) canvasHeight / (float) previewWidth;
            verticalRatio = (float) canvasWidth / (float) previewHeight;
        }
        rect.left *= horizontalRatio;
        rect.right *= horizontalRatio;
        rect.top *= verticalRatio;
        rect.bottom *= verticalRatio;

        Rect newRect = new Rect();
        switch (cameraDisplayOrientation) {
            case 0:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = canvasWidth - rect.right;
                    newRect.right = canvasWidth - rect.left;
                } else {
                    newRect.left = rect.left;
                    newRect.right = rect.right;
                }
                newRect.top = rect.top;
                newRect.bottom = rect.bottom;
                break;
            case 90:
                newRect.right = canvasWidth - rect.top;
                newRect.left = canvasWidth - rect.bottom;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = canvasHeight - rect.right;
                    newRect.bottom = canvasHeight - rect.left;
                } else {
                    newRect.top = rect.left;
                    newRect.bottom = rect.right;
                }
                break;
            case 180:
                newRect.top = canvasHeight - rect.bottom;
                newRect.bottom = canvasHeight - rect.top;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.left = rect.left;
                    newRect.right = rect.right;
                } else {
                    newRect.left = canvasWidth - rect.right;
                    newRect.right = canvasWidth - rect.left;
                }
                break;
            case 270:
                newRect.left = rect.top;
                newRect.right = rect.bottom;
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    newRect.top = rect.left;
                    newRect.bottom = rect.right;
                } else {
                    newRect.top = canvasHeight - rect.right;
                    newRect.bottom = canvasHeight - rect.left;
                }
                break;
            default:
                break;
        }

        /**
         * isMirror mirrorHorizontal finalIsMirrorHorizontal
         * true         true                false
         * false        false               false
         * true         false               true
         * false        true                true
         *
         * XOR
         */
        if (isMirror ^ mirrorHorizontal) {
            int left = newRect.left;
            int right = newRect.right;
            newRect.left = canvasWidth - right;
            newRect.right = canvasWidth - left;
        }
        if (mirrorVertical) {
            int top = newRect.top;
            int bottom = newRect.bottom;
            newRect.top = canvasHeight - bottom;
            newRect.bottom = canvasHeight - top;
        }
        return newRect;
    }

    /**
     * 绘制数据信息到View上
     * 若 {@link DrawInfo#getName()} 不为null则绘制 {@link DrawInfo#getName()}的内容
     * 若 {@link DrawInfo#getName()} 为null则绘制 {@link DrawInfo}的内容
     *
     * @param canvas            需要被绘制的view的canvas
     * @param drawInfo          绘制信息
     * @param faceRectThickness 人脸框厚度
     * @param paint             画笔
     */
    public static void DrawFaceRect(Canvas canvas, DrawInfo drawInfo, int faceRectThickness, Paint paint) {
        if (canvas == null || drawInfo == null) {
            return;
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(faceRectThickness);
        paint.setColor(drawInfo.getColor());
        paint.setAntiAlias(true);
        Path path = new Path();
        //左上
        Rect rect = drawInfo.getRect();
        path.moveTo(rect.left, rect.top + rect.height() / 4);
        path.lineTo(rect.left, rect.top);
        path.lineTo(rect.left + rect.width() / 4, rect.top);
        //右上
        path.moveTo(rect.right - rect.width() / 4, rect.top);
        path.lineTo(rect.right, rect.top);
        path.lineTo(rect.right, rect.top + rect.height() / 4);
        //右下
        path.moveTo(rect.right, rect.bottom - rect.height() / 4);
        path.lineTo(rect.right, rect.bottom);
        path.lineTo(rect.right - rect.width() / 4, rect.bottom);
        //左下
        path.moveTo(rect.left + rect.width() / 4, rect.bottom);
        path.lineTo(rect.left, rect.bottom);
        path.lineTo(rect.left, rect.bottom - rect.height() / 4);
        canvas.drawPath(path, paint);
        //绘制文字，用最细的即可，避免在某些低像素设备上文字模糊
        paint.setStrokeWidth(2);
        if (drawInfo.getName() == null) {
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setTextSize(rect.width() / 8);
            String str = (drawInfo.getSex() == GenderInfo.MALE ? "男" : (drawInfo.getSex() == GenderInfo.FEMALE ? "女" : "未知")) + "," + (drawInfo.getAge() == AgeInfo.UNKNOWN_AGE ? "未知" : drawInfo.getAge()) + "," + (drawInfo.getLiveness() == LivenessInfo.ALIVE ? "活体" : (drawInfo.getLiveness() == LivenessInfo.NOT_ALIVE ? "非活体" : "未知"));
            canvas.drawText(str, rect.left, rect.top - 10, paint);
        } else {
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setTextSize(rect.width() / 8);
            canvas.drawText(drawInfo.getName(), rect.left, rect.top - 10, paint);
        }
    }

    public void setPreviewWidth(int previewWidth) {
        this._previewWidth = previewWidth;
    }

    public void setPreviewHeight(int previewHeight) {
        this._previewHeight = previewHeight;
    }

    public void setCanvasWidth(int canvasWidth) {
        this._canvasWidth = canvasWidth;
    }

    public void setCanvasHeight(int canvasHeight) {
        this._canvasHeight = canvasHeight;
    }

    public void setCameraDisplayOrientation(int cameraDisplayOrientation) {
        this._cameraDisplayOrientation = cameraDisplayOrientation;
    }

    public void setCameraId(int cameraId) {
        this._cameraId = cameraId;
    }

    public void setMirror(boolean mirror) {
        _isMirror = mirror;
    }

    public int getPreviewWidth() {
        return _previewWidth;
    }

    public int getPreviewHeight() {
        return _previewHeight;
    }

    public int getCanvasWidth() {
        return _canvasWidth;
    }

    public int getCanvasHeight() {
        return _canvasHeight;
    }

    public int getCameraDisplayOrientation() {
        return _cameraDisplayOrientation;
    }

    public int getCameraId() {
        return _cameraId;
    }

    public boolean isMirror() {
        return _isMirror;
    }

    public boolean isMirrorHorizontal() {
        return _mirrorHorizontal;
    }

    public void setMirrorHorizontal(boolean mirrorHorizontal) {
        this._mirrorHorizontal = mirrorHorizontal;
    }

    public boolean isMirrorVertical() {
        return _mirrorVertical;
    }

    public void setMirrorVertical(boolean mirrorVertical) {
        this._mirrorVertical = mirrorVertical;
    }

}
