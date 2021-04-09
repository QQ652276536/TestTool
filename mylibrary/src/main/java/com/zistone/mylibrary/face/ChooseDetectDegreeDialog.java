package com.zistone.mylibrary.face;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.DialogFragment;

import com.zistone.mylibrary.R;
import com.zistone.mylibrary.face.util.ConfigUtil;

import static com.arcsoft.face.enums.DetectFaceOrientPriority.ASF_OP_0_ONLY;
import static com.arcsoft.face.enums.DetectFaceOrientPriority.ASF_OP_180_ONLY;
import static com.arcsoft.face.enums.DetectFaceOrientPriority.ASF_OP_270_ONLY;
import static com.arcsoft.face.enums.DetectFaceOrientPriority.ASF_OP_90_ONLY;
import static com.arcsoft.face.enums.DetectFaceOrientPriority.ASF_OP_ALL_OUT;

public class ChooseDetectDegreeDialog extends DialogFragment implements View.OnClickListener {

    private void initView(View dialogView) {
        //设置视频模式下的人脸优先检测方向
        RadioGroup radioGroupFtOrient = dialogView.findViewById(R.id.radio_group_ft_orient);
        RadioButton rbOrient0 = dialogView.findViewById(R.id.rb_orient_0);
        RadioButton rbOrient90 = dialogView.findViewById(R.id.rb_orient_90);
        RadioButton rbOrient180 = dialogView.findViewById(R.id.rb_orient_180);
        RadioButton rbOrient270 = dialogView.findViewById(R.id.rb_orient_270);
        RadioButton rbOrientAll = dialogView.findViewById(R.id.rb_orient_all);
        Button btnClose = dialogView.findViewById(R.id.btn_close_face_setting_menu);
        btnClose.setOnClickListener(v -> dismiss());
        switch (ConfigUtil.GetDetectionAngle(getActivity())) {
            case ASF_OP_90_ONLY:
                rbOrient90.setChecked(true);
                break;
            case ASF_OP_180_ONLY:
                rbOrient180.setChecked(true);
                break;
            case ASF_OP_270_ONLY:
                rbOrient270.setChecked(true);
                break;
            case ASF_OP_ALL_OUT:
                rbOrientAll.setChecked(true);
                break;
            case ASF_OP_0_ONLY:
            default:
                rbOrient0.setChecked(true);
                break;
        }
        radioGroupFtOrient.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_orient_90) {
                ConfigUtil.SetDetectionAngle(getActivity(), ASF_OP_90_ONLY);
            } else if (checkedId == R.id.rb_orient_180) {
                ConfigUtil.SetDetectionAngle(getActivity(), ASF_OP_180_ONLY);
            } else if (checkedId == R.id.rb_orient_270) {
                ConfigUtil.SetDetectionAngle(getActivity(), ASF_OP_270_ONLY);
            } else if (checkedId == R.id.rb_orient_all) {
                ConfigUtil.SetDetectionAngle(getActivity(), ASF_OP_ALL_OUT);
            } else {
                ConfigUtil.SetDetectionAngle(getActivity(), ASF_OP_0_ONLY);
            }
            dismiss();
        });
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_face_choose_direction, container);
        initView(dialogView);
        return dialogView;
    }

}
