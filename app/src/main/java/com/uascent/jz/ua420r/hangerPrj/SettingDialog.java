package com.uascent.jz.ua420r.hangerPrj;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.hangerPrj.HangerBean;
import com.uascent.jz.ua420r.hangerPrj.HangerControlActivity;
import com.uascent.jz.ua420r.hangerPrj.MyConstant;
import com.uascent.jz.ua420r.hangerPrj.SpConstant;
import com.uascent.jz.ua420r.utils.Lg;
import com.uascent.jz.ua420r.utils.MyUtils;
import com.uascent.jz.ua420r.utils.SpHelper;

import static com.uascent.jz.ua420r.hangerPrj.HangerControlActivity.BINARY_KEY;

/**
 * 作者：HWQ on 2017/6/27 11:17
 * 描述：
 */

public class SettingDialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, ColorPicker.OnColorChangedListener, ColorPicker.OnColorSelectedListener, View.OnTouchListener {

    private       HangerControlActivity mActivity;
    private       SeekBar               mSb_light;
    private       SeekBar               mSb_voice;
    private       TextView              mTv_light;
    private       TextView              mTv_voice;
    private       Button                mBtnJt;
    private       Button                mBtnSb;
    private       Button                mBtnSsj;
    private       Button                mBtnLg;
    private       ColorPicker           mPicker;
    public        EventListener         listener;
    private       AlertDialog           mDialog;
    private       HangerBean            mHangerBean;
    private final HangerBean            mCloneHangerBean;
    private       ToggleButton          mToggleBtn;
    private       RelativeLayout        mRl_body;


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.toggle_btn:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //TODO:人体感应开关
                    Lg.d("人体感应开关:---" + !mToggleBtn.isChecked());
                    mCloneHangerBean.setbuttonState(4, !mToggleBtn.isChecked());
                    mActivity.sendCommand(BINARY_KEY, mCloneHangerBean.getSendData());
                }
        }
        return false;
    }


    public abstract static class EventListener {
        public void onMyProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        public void onMyStartTrackingTouch(SeekBar seekBar) {
        }

        public void onMyStopTrackingTouch(SeekBar seekBar) {
        }

        public void onMyColorChanged(int color, String selectTypeId) {
        }

        public void onMyColorSelected(int color) {
        }

        public void onMyClick(View v) {
        }
    }

    public SettingDialog(Activity activity, EventListener listener, HangerBean hangerBean) {
        this.listener = listener;
        mActivity = (HangerControlActivity) activity;
        mHangerBean = hangerBean;
        mCloneHangerBean = hangerBean.clone();
        init();
    }

    public AlertDialog show() {
        mDialog.show();
        return mDialog;
    }

    public void dismiss() {
        mDialog.dismiss();
    }

    public void setEventListener(EventListener listener) {
        this.listener = listener;
    }

    private void init() {
        LayoutInflater inflater = mActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_setting,
                                       (ViewGroup) mActivity.findViewById(R.id.ll_setting));
        mDialog = new AlertDialog.Builder(mActivity, AlertDialog.THEME_HOLO_LIGHT).show();

        mSb_light = (SeekBar) layout.findViewById(R.id.sb_light);
        mSb_voice = (SeekBar) layout.findViewById(R.id.sb_voice);
        mTv_light = (TextView) layout.findViewById(R.id.tv_light);
        mTv_voice = (TextView) layout.findViewById(R.id.tv_voice);
        mSb_light = (SeekBar) layout.findViewById(R.id.sb_light);
        mToggleBtn = (ToggleButton) layout.findViewById(R.id.toggle_btn);
        mSb_light.setOnSeekBarChangeListener(this);
        mSb_voice.setOnSeekBarChangeListener(this);
        mToggleBtn.setOnTouchListener(this);

        //是否带体感
        layout.findViewById(R.id.rl_body)
                .setVisibility(mHangerBean.deviceBean.haveBody ? View.VISIBLE : View.GONE);
        //是否显示亮度音量
        layout.findViewById(R.id.ll_voice)
                .setVisibility(mHangerBean.deviceBean.haveVoiceSet ? View.VISIBLE : View.GONE);
        layout.findViewById(R.id.ll_light)
                .setVisibility(mHangerBean.deviceBean.haveLightSet ? View.VISIBLE : View.GONE);
        initView();

        layout.findViewById(R.id.rl_set_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                showColorPicker();
            }
        });
        layout.findViewById(R.id.rl_login_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mDialog.getWindow().setContentView(layout);
    }

    private void initView() {
        Lg.e("初始化Setting  initView---: " + mHangerBean.buttonState);
        mTv_light.setText("亮度(" + mHangerBean.getLightState() + ")");
        mTv_voice.setText("音量(" + mHangerBean.getVoiceState() + ")");
        mSb_light.setProgress(mHangerBean.getLightState());
        mSb_voice.setProgress(mHangerBean.getVoiceState());
        mToggleBtn.setChecked((mHangerBean.buttonState & 16) == 16);
    }

    private void showColorPicker() {
        Dialog bottomDialog = new Dialog(mActivity, R.style.ActionSheetDialogStyle);
        View   contentView  = LayoutInflater.from(mActivity).inflate(R.layout.activity_color_activity, null);
        //ColorPicker选择器控件
        mBtnJt = (Button) contentView.findViewById(R.id.btn_jt);
        mBtnSb = (Button) contentView.findViewById(R.id.btn_sb);
        mBtnSsj = (Button) contentView.findViewById(R.id.btn_ssj);
        mBtnLg = (Button) contentView.findViewById(R.id.btn_lg);
        mPicker = (ColorPicker) contentView.findViewById(R.id.picker);
        mPicker.setOnColorChangedListener(this);
        mPicker.setOnColorSelectedListener(this);
        MyUtils.setOnClick(this, mBtnJt, mBtnSb, mBtnSsj, mBtnLg);

        bottomDialog.setContentView(contentView);
        ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
        layoutParams.width = mActivity.getResources().getDisplayMetrics().widthPixels;
        contentView.setLayoutParams(layoutParams);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        //bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        listener.onMyProgressChanged(seekBar, progress, fromUser);
        if (seekBar.getId() == R.id.sb_light) {
            mTv_light.setText("亮度(" + progress + ")");
            //mCloneHangerBean.lightState = (byte) (progress << 4);
            mCloneHangerBean.lightState = HangerBean.getProgressState(mCloneHangerBean.lightState, progress);
        } else {
            mTv_voice.setText("音量(" + progress + ")");
            //mCloneHangerBean.voiceState = (byte) (progress << 4);
            mCloneHangerBean.voiceState = HangerBean.getProgressVoiceState(mCloneHangerBean.voiceState, progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        listener.onMyStartTrackingTouch(seekBar);
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
        listener.onMyStopTrackingTouch(seekBar); //TODO:设置成功,更改主界面Ui

        mActivity.showDilaog();
        mActivity.sendCommand(BINARY_KEY, mCloneHangerBean.getSendData());
        //HangerBean.sendCommand(mActivity,
        //                       mHangerBean.physicalDeviceId,
        //                       MyConstant.CODE_MSG,
        //                       mCloneHangerBean.getSendData());
    }

    @Override
    public void onColorChanged(int color) {
        listener.onMyColorChanged(color, mCurrentSelectId);
        if (TextUtils.isEmpty(mCurrentSelectId)) {
            return;
        }
        listener.onMyColorChanged(color, mCurrentSelectId);
    }

    @Override
    public void onColorSelected(int color) {
        listener.onMyColorSelected(color);
        Lg.d("当前选择颜色：" + mCurrentSelectId + "   color:" + color);
        if (!TextUtils.isEmpty(mCurrentSelectId)) {
            SpHelper.putCommit(mCurrentSelectId, color);
        }
    }

    @Override
    public void onClick(View v) {
        listener.onMyClick(v);
        switch (v.getId()) {
            case R.id.btn_jt:
                onColorBtnClick(v, SpConstant.COLOR_JT, MyConstant.COLOR_DEFAULT_JT);
                break;
            case R.id.btn_sb:
                onColorBtnClick(v, SpConstant.COLOR_SB, MyConstant.COLOR_DEFAULT_SB);
                break;
            case R.id.btn_ssj:
                onColorBtnClick(v, SpConstant.COLOR_SSJ, MyConstant.COLOR_DEFAULT_SSJ);
                break;
            case R.id.btn_lg:
                onColorBtnClick(v, SpConstant.COLOR_LG, MyConstant.COLOR_DEFAULT_LG);
                break;
        }
    }

    //颜色选择器
    private String mCurrentSelectId;
    private View   mCurrentSelectBtn;

    private void onColorBtnClick(View v, String colorSp, int defaultColor) {
        mCurrentSelectId = colorSp;
        if (mCurrentSelectBtn != null) {
            mCurrentSelectBtn.setSelected(false);
        }
        v.setSelected(true);
        mCurrentSelectBtn = v;
        int currentColor = (int) SpHelper.get(colorSp, defaultColor);
        mPicker.setColor(currentColor);
        mPicker.setOldCenterColor(currentColor);
    }
}
