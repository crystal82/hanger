package com.uascent.jz.ua420r.hangerPrj;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.utils.Lg;
import com.uascent.jz.ua420r.utils.MyUtils;
import com.uascent.jz.ua420r.utils.SpHelper;
import com.uascent.jz.ua420r.view.CustomWaitDialog2;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.uascent.jz.ua420r.hangerPrj.HangerControlActivity.BINARY_KEY;

/**
 * 作者：HWQ on 2017/6/17 11:24
 * 描述：
 */

public class DeviceFragment extends BaseFragment implements View.OnTouchListener, View.OnClickListener {

    @Bind(R.id.tv_temp)
    TextView mTvTemp;
    @Bind(R.id.tv_humidity)
    TextView mTvHumidity;
    @Bind(R.id.iv_fenggan)
    ImageView mIvFenggan;
    @Bind(R.id.iv_xiaodu1)
    ImageView mIvXiaodu1;
    @Bind(R.id.iv_zhaoming)
    ImageView mIvZhaoming;
    @Bind(R.id.iv_xiaodu2)
    ImageView mIvXiaodu2;
    @Bind(R.id.iv_honggan)
    ImageView mIvHonggan;
    @Bind(R.id.iv_fenggan_hint1)
    ImageView mIvFengganHint1;
    @Bind(R.id.iv_xiaodu_hint1)
    ImageView mIvXiaoduHint1;
    @Bind(R.id.iv_xiaodu_hint2)
    ImageView mIvXiaoduHint2;
    @Bind(R.id.iv_fenggan_hint2)
    ImageView mIvFengganHint2;
    @Bind(R.id.iv_deng_hint)
    ImageView mIvDengHint;
    @Bind(R.id.ll_state_hint)
    LinearLayout mLlStateHint;
    @Bind(R.id.iv_lg)
    ImageView mIvLg;
    @Bind(R.id.iv_ssj_left)
    ImageView mIvSsjLeft;
    @Bind(R.id.iv_ssj_right)
    ImageView mIvSsjRight;
    @Bind(R.id.rl_hanger)
    RelativeLayout mRlHanger;
    @Bind(R.id.iv_setting)
    ImageView mIvSetting;
    @Bind(R.id.iv_voice)
    ImageView mIvVoice;
    @Bind(R.id.iv_close)
    ImageView mIvClose;
    @Bind(R.id.iv_row)
    ImageView mIvRow;
    @Bind(R.id.iv_jt)
    ImageView mIvJt;
    @Bind(R.id.tv_sz)
    TextView mTvSz;
    @Bind(R.id.tv_yy)
    TextView mTvYy;
    @Bind(R.id.ll_device)
    LinearLayout mLlDevice;

    private SettingDialog mSettingDialog;

    private ViewGroup.LayoutParams mLeftLayoutParams;
    private ViewGroup.LayoutParams mRightLayoutParams;
    private ViewGroup.MarginLayoutParams mJiaziParams;
    private int lastX, lastY;
    private int mLeftHight, mRightHight;
    private int mInitLeftHight;
    private int mChangeY;
    private int mRlHangerHeight;
    private int mIvDingbuHeight;
    private int mIvJiaziHeight;
    private int mIvJiaziTop; //距离顶部

    private int mMaxMargin;
    private int mAverage;
    private HangerControlActivity mActivity;
    private HangerBean mHangerBean;
    private String mPhysicalDeviceId;
    private CustomWaitDialog2 mCustomWaitDialog2;
    private int mColorTop;
    private int mColorBottom;
    private int mColorControl;
    private int mColorShenSuo;
    protected boolean isCreated = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        ButterKnife.bind(this, view);

        mActivity = (HangerControlActivity) getActivity();
        mHangerBean = mActivity.getHangerBean();
        mPhysicalDeviceId = mActivity.getPhysicalDeviceId();
        isCreated = true;
        mCustomWaitDialog2 = new CustomWaitDialog2(mActivity);

        initInfo();
        initListener();

        return view;
    }

    @Override
    public void onResume() {
        Lg.d("DeviceFragment------onResume:" + mAverage);
        //if (!AC.accountMgr().isLogin()) {}
        //    Intent intent = new Intent(getActivity(), LoginActivity.class);
        //   startActivity(intent);
        //   getActivity().finish();

        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Lg.d("DeviceFragment---setUserVisibleHint---:" + isVisibleToUser);
        if (!isCreated) {
            return;
        }
        if (isVisibleToUser) {
            //initView();
            mActivity.setActionBar(true, true, "手势操作");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Lg.d("DeviceFragment------onStart");
    }

    @Override
    public void initView() {
        Lg.d("DeviceFragment------initView");
        if (mRlHanger == null) {
            return;
        }
        initColor();
        initUi();
    }

    private void initUi() {
        int margin = mHangerBean.getHeightState() * mAverage;
        mJiaziParams.setMargins(0, margin, 0, 0);
        mIvLg.requestLayout();

        Lg.d("onGlobalLayout------:" + margin + "   mLeftHight:" + mLeftHight
                + "  " + mHangerBean.getHeightState()
                + "  " + mHangerBean.heightState);
        mLeftLayoutParams.height = mInitLeftHight + margin;
        mRightLayoutParams.height = mInitLeftHight + margin;
        mIvSsjLeft.setLayoutParams(mLeftLayoutParams);
        mIvSsjRight.setLayoutParams(mRightLayoutParams);

        Lg.d("mIvFengganHint1------:" + mHangerBean.getButtonState(6));

        int fengganHintDwId;
        if (mHangerBean.getButtonState(6)) {
            //风干
            fengganHintDwId = R.mipmap.fenggan_hint1;
        } else if (mHangerBean.getButtonState(5)) {
            //烘干
            fengganHintDwId = R.mipmap.fenggan_hint2;
        } else {
            fengganHintDwId = R.drawable.shape_null;
        }
        MyUtils.setDrawable(mActivity, mIvFengganHint1, fengganHintDwId);
        MyUtils.setDrawable(mActivity, mIvFengganHint2, fengganHintDwId);
        //mIvFengganHint2.setSelected(mHangerBean.getButtonState(6));

        mIvXiaoduHint1.setSelected(mHangerBean.getButtonState(7));
        mIvXiaoduHint2.setSelected(mHangerBean.getButtonState(7));
        mIvDengHint.setSelected(mHangerBean.isLightOpen());
        mIvVoice.setSelected(mHangerBean.isVoiceState());

        if (mHangerBean.getTemperature() != 255) {
            mTvTemp.setText("温度" + mHangerBean.getTemperature() + "℃");
        } else {
            mTvTemp.setVisibility(View.GONE);
        }
        if (mHangerBean.getHumidity() != 255) {
            mTvHumidity.setText("湿度" + mHangerBean.getHumidity() + "%");
        } else {
            mTvHumidity.setVisibility(View.GONE);
        }

        mIvVoice.setVisibility(mHangerBean.deviceBean.haveVoiceSet ? View.VISIBLE : View.INVISIBLE);
        mTvYy.setVisibility(mHangerBean.deviceBean.haveVoiceSet ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void initColor() {
        mColorTop = (int) SpHelper.get(SpConstant.COLOR_JT, getResources().getColor(MyConstant.COLOR_DEFAULT_JT));
        mColorBottom = (int) SpHelper.get(SpConstant.COLOR_LG, getResources().getColor(MyConstant.COLOR_DEFAULT_LG));
        mColorControl = (int) SpHelper.get(SpConstant.COLOR_SB, getResources().getColor(MyConstant.COLOR_DEFAULT_SB));
        mColorShenSuo = (int) SpHelper.get(SpConstant.COLOR_SSJ, getResources().getColor(MyConstant.COLOR_DEFAULT_SSJ));

        Lg.d("取出颜色:" + mColorTop + "    " +
                mColorBottom + "    " +
                mColorControl + "    " +
                mColorShenSuo);
        mIvFenggan.setColorFilter(mColorControl);
        mIvXiaodu1.setColorFilter(mColorControl);
        mIvZhaoming.setColorFilter(mColorControl);
        mIvXiaodu2.setColorFilter(mColorControl);
        mIvHonggan.setColorFilter(mColorControl);

        mIvSsjLeft.setColorFilter(mColorShenSuo);
        mIvSsjRight.setColorFilter(mColorShenSuo);

        mIvJt.setColorFilter(mColorTop);
        mIvLg.setColorFilter(mColorBottom);
    }

    private void initInfo() {
        mRlHangerHeight = mRlHanger.getMeasuredHeight();
        mLeftLayoutParams = mIvSsjLeft.getLayoutParams();
        mRightLayoutParams = mIvSsjRight.getLayoutParams();
        mJiaziParams = (ViewGroup.MarginLayoutParams) mIvLg.getLayoutParams();

        ViewTreeObserver vto = mRlHanger.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRlHanger.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Lg.d("mRlHanger.getHeight():" + mRlHanger.getHeight());
                Lg.d("mIvJt.getHeight():" + mIvJt.getHeight());
                Lg.d("mIvLg.getHeight:" + mIvLg.getHeight());
                Lg.d("mIvSsjLeft.getHeight():" + mIvSsjLeft.getHeight());
                Lg.d("mIvLg.getHeight():" + mIvLg.getY() + "  " + mIvLg.getTranslationY());
                mRlHangerHeight = mRlHanger.getHeight();
                mIvDingbuHeight = mIvJt.getHeight();
                mIvJiaziHeight = mIvLg.getHeight();
                mLeftHight = mIvSsjLeft.getHeight();
                mInitLeftHight = mIvSsjLeft.getHeight();
                mRightHight = mIvSsjRight.getHeight();

                mIvJiaziTop = (int) mIvLg.getY();
                mMaxMargin = (int) (mRlHangerHeight - (mRlHangerHeight * 0.1) - mIvJiaziHeight - mIvJiaziTop);
                mAverage = mMaxMargin / MyConstant.FLEX_LEVEL;
                Lg.d("！！！margin最大值:" + mMaxMargin);

                initColor();
                initUi();
            }
        });
    }

    private void initListener() {
        mIvLg.setOnTouchListener(this);
        mIvRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        MyUtils.setOnClick(this,
                mIvSetting, mIvVoice, mIvClose,
                mIvFenggan, mIvXiaodu1, mIvXiaodu2, mIvZhaoming, mIvHonggan);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Lg.d("  mRlHangerHeight:" + mRlHangerHeight + "   " + mLeftHight);
        mCloneHangerBean = mHangerBean.clone();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                mLeftHight = mIvSsjLeft.getMeasuredHeight();
                mRightHight = mIvSsjRight.getMeasuredHeight();
                mChangeY = (int) event.getRawY() - lastY;
                if (mChangeY < 0 || (mJiaziParams.topMargin < mMaxMargin)) {

                    int changeMargin = mJiaziParams.topMargin + mChangeY - mMaxMargin;
                    if ((mJiaziParams.topMargin + mChangeY) >= 0) {
                        if (changeMargin > 0) {
                            mJiaziParams.setMargins(0, mMaxMargin, 0, 0);
                        } else {
                            mJiaziParams.setMargins(0, mJiaziParams.topMargin + mChangeY, 0, 0);
                        }
                        mIvLg.requestLayout();
                    }
                    if ((mLeftHight + mChangeY) >= mInitLeftHight) {
                        mLeftLayoutParams.height = mLeftHight + mChangeY;
                        mRightLayoutParams.height = mRightHight + mChangeY;
                        if (changeMargin > 0) {
                            mLeftLayoutParams.height = mLeftLayoutParams.height - changeMargin;
                            mRightLayoutParams.height = mRightLayoutParams.height - changeMargin;
                        }

                        mIvSsjLeft.setLayoutParams(mLeftLayoutParams);
                        mIvSsjRight.setLayoutParams(mRightLayoutParams);
                    }

                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                }
                break;
            case MotionEvent.ACTION_UP:
                int heightGrade = mJiaziParams.topMargin / mAverage;
                heightGrade = (heightGrade > MyConstant.FLEX_LEVEL ? MyConstant.FLEX_LEVEL : heightGrade);
                //Toast.makeText(mActivity, "改变高度:" + heightGrade, Toast.LENGTH_SHORT).show();
                Lg.d("mJiaziParams.topMargin：" + mJiaziParams.topMargin + "   mAverage:" + mAverage + "  改变高度:" + heightGrade);
                mActivity.showDilaog();

                mCloneHangerBean.heightState = (byte) heightGrade;
                mActivity.sendCommand(BINARY_KEY, mCloneHangerBean.getSendData());
                //HangerBean.sendCommand(mActivity, mPhysicalDeviceId, MyConstant.CODE_MSG,
                //                       mHangerBean.getSendData());
                break;
        }
        return false;
    }

    private HangerBean mCloneHangerBean;
    private View mCurrentView;

    @Override
    public void onClick(View v) {
        boolean isControl = false;
        mCurrentView = v;
        mCloneHangerBean = mHangerBean.clone();
        switch (v.getId()) {
            //TODO:控制按钮
            case R.id.iv_close:
                isControl = true;
                mCloneHangerBean.heightState = -1;
                mCloneHangerBean.buttonState = 0;
                mCloneHangerBean.lightState = 0;
                //Toast.makeText(mActivity, "关机", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_fenggan:
                isControl = true;
                mCloneHangerBean.heightState = -1;
                boolean isFenggan = !mCloneHangerBean.getButtonState(6);
                mCloneHangerBean.setbuttonState(6, isFenggan);
                if (isFenggan && mCloneHangerBean.getButtonState(5)) {
                    mCloneHangerBean.setbuttonState(5, false);
                }
                break;
            case R.id.iv_honggan:
                isControl = true;
                mCloneHangerBean.heightState = -1;
                mCloneHangerBean.setbuttonState(5, !mCloneHangerBean.getButtonState(5));
                if (mCloneHangerBean.getButtonState(6)) {
                    mCloneHangerBean.setbuttonState(6, false);
                }
                break;
            case R.id.iv_xiaodu1:
            case R.id.iv_xiaodu2:
                isControl = true;
                mCloneHangerBean.heightState = -1;
                boolean isXiaodu = !mCloneHangerBean.getButtonState(7);
                mCloneHangerBean.setbuttonState(7, isXiaodu);
                break;
            case R.id.iv_zhaoming:
                isControl = true;
                mCloneHangerBean.heightState = -1;
                mCloneHangerBean.lightState = (byte) mCloneHangerBean.onLightClickData();
                break;
            case R.id.iv_voice:
                isControl = true;
                mCloneHangerBean.heightState = -1;
                mCloneHangerBean.voiceState = (byte) mCloneHangerBean.onVoiceClickData();
                break;

            case R.id.iv_setting:
                mSettingDialog = new SettingDialog(getActivity(), mEventListener, mHangerBean);
                mSettingDialog.show();
                break;
        }

        if (isControl) {
            //mCustomWaitDialog2.show();
            mActivity.showDilaog();
            mActivity.sendCommand(BINARY_KEY, mCloneHangerBean.getSendData());
            //HangerBean.sendCommand(getActivity(), mPhysicalDeviceId, MyConstant.CODE_MSG,
            //                       mCloneHangerBean.getSendData());
        }
    }

    private void initUiOnSend() {
        switch (mCurrentView.getId()) {
            //TODO:控制按钮
            case R.id.iv_fenggan:
                boolean isFenggan = mHangerBean.getButtonState(6);
                mIvFengganHint1.setSelected(isFenggan);
                mIvFengganHint2.setSelected(isFenggan);
                break;
            case R.id.iv_honggan:
                if (mHangerBean.getButtonState(6)) {
                    mIvFengganHint1.setSelected(false);
                    mIvFengganHint2.setSelected(false);
                }
                break;
            case R.id.iv_xiaodu1:
            case R.id.iv_xiaodu2:
                boolean isXiaodu = mHangerBean.getButtonState(7);
                mIvXiaoduHint1.setSelected(isXiaodu);
                mIvXiaoduHint2.setSelected(isXiaodu);
                break;
            case R.id.iv_zhaoming:
                mIvDengHint.setSelected(mHangerBean.isLightOpen());
                break;
            case R.id.iv_voice:
                mIvVoice.setSelected(mHangerBean.isVoiceState());
                break;
        }
    }

    public SettingDialog.EventListener mEventListener = new SettingDialog.EventListener() {

        @Override
        public void onMyStopTrackingTouch(SeekBar seekBar) {
            if (seekBar.getId() == R.id.sb_light) {
                mIvDengHint.setSelected(mHangerBean.isLightOpen());
            } else if (seekBar.getId() == R.id.sb_voice) {
                mIvVoice.setSelected(mHangerBean.isVoiceState());
            }
        }

        @Override
        public void onMyColorChanged(int color, String selectTypeId) {
            if (TextUtils.isEmpty(selectTypeId)) {
                return;
            }
            switch (selectTypeId) {
                case SpConstant.COLOR_JT:
                    mIvJt.setColorFilter(color);
                    break;
                case SpConstant.COLOR_SB:
                    mIvFenggan.setColorFilter(color);
                    mIvXiaodu1.setColorFilter(color);
                    mIvZhaoming.setColorFilter(color);
                    mIvXiaodu2.setColorFilter(color);
                    mIvHonggan.setColorFilter(color);
                    break;
                case SpConstant.COLOR_SSJ:
                    mIvSsjLeft.setColorFilter(color);
                    mIvSsjRight.setColorFilter(color);
                    break;
                case SpConstant.COLOR_LG:
                    mIvLg.setColorFilter(color);
                    break;
            }
        }
    };
}
