package com.uascent.jz.ua420r.hangerPrj;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.utils.Lg;
import com.uascent.jz.ua420r.utils.MyUtils;
import com.uascent.jz.ua420r.view.CustomWaitDialog2;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.uascent.jz.ua420r.hangerPrj.HangerControlActivity.BINARY_KEY;

/**
 * 作者：HWQ on 2017/6/17 11:24
 * 描述：
 */

public class ControlFragment extends BaseFragment implements View.OnClickListener {
    @Bind(R.id.tv_humidity)
    TextView     mTvHumidity;
    @Bind(R.id.tv_temp)
    TextView     mTvTemp;
    @Bind(R.id.iv_control_sz)
    ImageView    mIvControlSz;
    @Bind(R.id.tv_sz)
    TextView     mTvSz;
    @Bind(R.id.iv_control_yy)
    ImageView    mIvControlYy;
    @Bind(R.id.tv_yy)
    TextView     mTvYy;
    @Bind(R.id.iv_control_tz)
    ImageView    mIvControlTz;
    @Bind(R.id.ll_control)
    LinearLayout mLlControl;
    @Bind(R.id.iv_control_ss)
    ImageView    mIvControlSs;
    @Bind(R.id.iv_control_fg)
    ImageView    mIvControlFg;
    @Bind(R.id.tv_fg)
    TextView     mTvFg;
    @Bind(R.id.iv_control_xj)
    ImageView    mIvControlXj;
    @Bind(R.id.iv_control_hg)
    ImageView    mIvControlHg;
    @Bind(R.id.iv_control_xd)
    ImageView    mIvControlXd;
    @Bind(R.id.tv_xd)
    TextView     mTvXd;
    @Bind(R.id.iv_control_gj)
    ImageView    mIvControlGj;
    @Bind(R.id.iv_control_zm)
    ImageView    mIvControlZm;
    @Bind(R.id.tv_zm)
    TextView     mTvZm;

    //@Bind(R.id.iv_left_row)
    //ImageView    mIvLeftRow;
    //@Bind(R.id.iv_right_row)
    //ImageView    mIvRightRow;


    private HangerControlActivity mActivity;
    private HangerBean            mHangerBean;
    private String                mPhysicalDeviceId;
    protected boolean isCreated = false;
    private CustomWaitDialog2 mCustomWaitDialog2;
    private SettingDialog     mSettingDialog;
    private HangerBean        mCloneHangerBean;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCreated = true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control_new, container, false);
        ButterKnife.bind(this, view);

        mActivity = (HangerControlActivity) getActivity();
        mHangerBean = mActivity.getHangerBean();
        mPhysicalDeviceId = mActivity.getPhysicalDeviceId();
        mCustomWaitDialog2 = new CustomWaitDialog2(mActivity);

        initListener();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Lg.d("ControlFragment---onStart---");
    }

    @Override
    public void onResume() {
        super.onResume();
        Lg.d("ControlFragment---onResume---");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public ControlFragment() {
    }

    private void initListener() {
        MyUtils.setOnClick(this, mIvControlSs,
                           mIvControlTz, mIvControlFg, mIvControlHg,
                           mIvControlXj, mIvControlXd, mIvControlZm,
                           mIvControlSz, mIvControlYy, mIvControlGj);

        //,mIvLeftRow, mIvRightRow
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Lg.d("ControlFragment---setUserVisibleHint---:" + isVisibleToUser);
        if (!isCreated) {
            return;
        }
        if (isVisibleToUser) {
            //initView();
            mActivity.setActionBar(true, true, "按键操作");
        }
    }

    @Override
    public void initView() {
        if (mHangerBean != null) {
            Lg.d("ControlFragment---初始化---：" + mHangerBean.voiceState + "  mHangerBean:" + Arrays.toString(mHangerBean.getSendData()));
            mIvControlXd.setSelected(mHangerBean.getButtonState(7));
            mIvControlFg.setSelected(mHangerBean.getButtonState(6));
            mIvControlHg.setSelected(mHangerBean.getButtonState(5));
            mIvControlZm.setSelected(mHangerBean.isLightOpen());
            mIvControlYy.setSelected(mHangerBean.isVoiceState());
            mIvControlSs.setSelected((mHangerBean.buttonState & 1) == 1);
            mIvControlXj.setSelected((mHangerBean.buttonState & 2) == 2);

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

            mIvControlYy.setVisibility(mHangerBean.deviceBean.haveVoiceSet ? View.VISIBLE : View.INVISIBLE);
            mTvYy.setVisibility(mHangerBean.deviceBean.haveVoiceSet ? View.VISIBLE : View.INVISIBLE);

            mIvControlZm.setVisibility(mHangerBean.deviceBean.haveLightSet ? View.VISIBLE : View.INVISIBLE);
            mTvZm.setVisibility(mHangerBean.deviceBean.haveLightSet ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private View mCurrentView;

    @Override
    public void onClick(View v) {
        boolean control = true;
        mCurrentView = v;
        mCloneHangerBean = mHangerBean.clone();
        switch (v.getId()) {
            case R.id.iv_control_ss:
                //上升1,00
                mCloneHangerBean.heightState = 0;//0xF0 (byte) (mCloneHangerBean.heightState & 252 | 1)
                break;
            case R.id.iv_control_tz:
                //停止 0,F0
                mCloneHangerBean.heightState = -16;//(byte) (mCloneHangerBean.heightState & 252);
                break;
            case R.id.iv_control_xj:
                //下降10,63
                mCloneHangerBean.heightState = 63;//(byte) (mCloneHangerBean.heightState & 252 | 2);
                break;

            case R.id.iv_control_xd:
                mCloneHangerBean.heightState = -1;
                boolean isXiaodu = !mCloneHangerBean.getButtonState(7);
                mCloneHangerBean.setbuttonState(7, isXiaodu);
                //mIvControlXd.setSelected(isXiaodu);
                break;
            //风干，烘干，一个
            case R.id.iv_control_fg:
                mCloneHangerBean.heightState = -1;
                boolean isFenggan = !mCloneHangerBean.getButtonState(6);
                mCloneHangerBean.setbuttonState(6, isFenggan);
                if (isFenggan && mCloneHangerBean.getButtonState(5)) {
                    mCloneHangerBean.setbuttonState(5, false);
                    //mIvControlHg.setSelected(false);
                }
                //mIvControlFg.setSelected(isFenggan);
                break;
            case R.id.iv_control_hg:
                mCloneHangerBean.heightState = -1;
                boolean isHonggan = !mCloneHangerBean.getButtonState(5);
                mCloneHangerBean.setbuttonState(5, isHonggan);
                if (isHonggan && mCloneHangerBean.getButtonState(6)) {
                    mCloneHangerBean.setbuttonState(6, false);
                    //mIvControlFg.setSelected(false);
                }
                //mIvControlHg.setSelected(isHonggan);
                break;
            case R.id.iv_control_zm:
                //不等于0这设置为0，关闭
                mCloneHangerBean.heightState = -1;
                mCloneHangerBean.lightState = (byte) mCloneHangerBean.onLightClickData();
                break;
            case R.id.iv_control_yy:
                mCloneHangerBean.heightState = -1;
                mCloneHangerBean.voiceState = (byte) mCloneHangerBean.onVoiceClickData();
                break;
            case R.id.iv_control_gj:
                //按电源键，所有的输出关闭，即发送风干，
                //消毒，照明，烘干全关闭，晾杆停止的码值组合，忽略关闭语音
                mCloneHangerBean.heightState = -1;
                mCloneHangerBean.buttonState = 0;
                mCloneHangerBean.lightState = 0;
                Toast.makeText(mActivity, "关机", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_control_sz:
                control = false;
                mSettingDialog = new SettingDialog(getActivity(), mEventListener, mHangerBean);
                mSettingDialog.show();
                break;

            //case R.id.iv_left_row:
            //case R.id.iv_right_row:
            //    control = false;
            //    mActivity.setPager(1);
            //    break;
        }

        //TODO:发送命令
        if (control) {
            //mCustomWaitDialog2.show();
            //mActivity.showDilaog();
            mActivity.sendCommand(BINARY_KEY, mCloneHangerBean.getSendData());

            //HangerBean.sendCommand(getActivity(), mPhysicalDeviceId, MyConstant.CODE_MSG,
            //                       mCloneHangerBean.getSendData());
        }
    }

    //设置Dialog事件
    public SettingDialog.EventListener mEventListener = new SettingDialog.EventListener() {
        @Override
        public void onMyStopTrackingTouch(SeekBar seekBar) {
            if (seekBar.getId() == R.id.sb_light) {
                mIvControlZm.setSelected(mHangerBean.isLightOpen());
            } else if (seekBar.getId() == R.id.sb_voice) {
                mIvControlYy.setSelected(mHangerBean.isVoiceState());
            }
        }
    };

}
