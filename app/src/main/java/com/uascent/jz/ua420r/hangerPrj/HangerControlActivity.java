package com.uascent.jz.ua420r.hangerPrj;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.uascent.jz.ua420r.ControlModule.GosControlModuleBaseActivity;
import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.utils.Lg;
import com.uascent.jz.ua420r.view.CustomWaitDialog2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HangerControlActivity extends GosControlModuleBaseActivity implements ViewPager.OnPageChangeListener {
    @Bind(R.id.vp_fragment)
    ViewPager mVpFragment;
    @Bind(R.id.iv_bg)
    ImageView mIvBg;

    private GizWifiDevice mDevice;
    private boolean issend = true;
    private boolean isInit = true;
    private int mCurrentAction;

    FragmentManager mFragmentManager;
    private long               deviceId;
    private String             physicalDeviceId;
    private HangerBean         mHangerBean;
    private List<BaseFragment> mList;
    private int                mCurrentPosition;
    private boolean            mIsDestroy;
    private CustomWaitDialog2  mCustomWaitDialog2;

    private enum handler_key {
        UPDATE_UI,
        DISCONNECT,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_control);
        ButterKnife.bind(this);

        deviceId = getIntent().getLongExtra("deviceId", 0);
        physicalDeviceId = getIntent().getStringExtra("physicalDeviceId");
        byte[] stateContent = getIntent().getByteArrayExtra("stateContent");
        mCustomWaitDialog2 = new CustomWaitDialog2(this);
        mCustomWaitDialog2.show();

        mVpFragment.addOnPageChangeListener(this);
        //TODO:初始化数据
        mHangerBean = HangerBean.getInstance(new byte[10]);
        mHangerBean.deviceId = deviceId;
        mHangerBean.physicalDeviceId = physicalDeviceId;

        initView();
        loadBgUrl();//TODO:加载图片背景
        initDevice();
        //setActionBar(true, true, getDeviceName());

        progressDialog.show();
        //startPollRequestState();//轮询请求状态

        //TODO:透传发送数据
        //sendCommand("binary", value);
    }


    public void showDilaog() {
        //mCustomWaitDialog2.show();
        //mHandler.postDelayed(waitRunnable, 5 * 1000);
    }

    Runnable waitRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCustomWaitDialog2.isShowing()) {
                Toast.makeText(HangerControlActivity.this, "等待响应超时", Toast.LENGTH_SHORT).show();
                mCustomWaitDialog2.cancel();
            }
        }
    };

    public void cancelDialog() {
        mCustomWaitDialog2.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDevice.setListener(gizWifiDeviceListener);
        getStatusOfDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Lg.d("HangerControlActivity onDestroy");
        mIsDestroy = true;
        progressDialog.cancel();
        mHandler.removeCallbacks(mRunnable);
        // 退出页面，取消设备订阅
        mDevice.setSubscribe(false);
        mDevice.setListener(null);
        ButterKnife.unbind(this);
    }

    public static String BINARY_KEY = "binary";

    public void sendCommand(String key, byte[] value) {
        Toast.makeText(this, "发送: key:" + key + "  value:" + Arrays.toString(value), Toast.LENGTH_SHORT).show();
        Lg.e("发送: key:" + key + "  value:" + Arrays.toString(value));
        if (value == null) {
            return;
        }
        int sn = 5;

        ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<>();
        hashMap.put(key, value);
        mDevice.write(hashMap, sn);
        Lg.d("TAG", "下发命令：" + hashMap.toString());
    }

    /**
     * Description:页面加载后弹出等待框，等待设备可被控制状态回调，如果一直不可被控，等待一段时间后自动退出界面
     */
    private void getStatusOfDevice() {
        // 设备是否可控
        if (isDeviceCanBeControlled()) {
            // 透传读取数据
            Lg.d("---透传读取数据---");
            sendCommand(BINARY_KEY, new byte[8]);
        } else {
            // 显示等待栏
            progressDialog.show();
            if (mDevice.isLAN()) {
                // 小循环10s未连接上设备自动退出
                mHandler.postDelayed(mRunnable, 10000);
            } else {
                // 大循环20s未连接上设备自动退出
                mHandler.postDelayed(mRunnable, 20000);
            }
        }
    }

    private boolean isDeviceCanBeControlled() {
        return mDevice.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled;
    }

    private String getDeviceName() {
        if (TextUtils.isEmpty(mDevice.getAlias())) {
            return mDevice.getProductName();
        }
        return mDevice.getAlias();
    }

    private void initDevice() {
        Intent intent = getIntent();
        mDevice = (GizWifiDevice) intent.getParcelableExtra("GizWifiDevice");
        mCurrentAction = intent.getIntExtra("CurrentAction", 0);
        Log.i("Apptest", mDevice.getDid());
    }

    //TODO:加载图片背景
    private void loadBgUrl() {
    }

    public String getPhysicalDeviceId() {
        return physicalDeviceId;
    }

    public HangerBean getHangerBean() {
        return mHangerBean;
    }

    private void initView() {
        mFragmentManager = getSupportFragmentManager();
        ControlFragment controlFragment  = new ControlFragment();
        DeviceFragment  deviceFragment   = new DeviceFragment();
       //ControlFragment controlFragment2 = new ControlFragment();
       //DeviceFragment  deviceFragment2  = new DeviceFragment();
        mList = new ArrayList<>();
        mList.add(deviceFragment);
        mList.add(controlFragment);
       //mList.add(controlFragment2);
       //mList.add(deviceFragment2);
        mVpFragment.setOffscreenPageLimit(4);
        mVpFragment.setAdapter(new MyFragmentAdapter(getSupportFragmentManager(), mList));
        mVpFragment.setCurrentItem(0, false);
        setActionBar(true, true, "手势操作");
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPosition = position;
    }

    //TODO:页面切换时间
    @Override
    public void onPageScrollStateChanged(int state) {
        //        若viewpager滑动未停止，直接返回
       //if (state != ViewPager.SCROLL_STATE_IDLE) return;
       ////        若当前为第一张，设置页面为倒数第二张
       //if (mCurrentPosition == 0) {
       //    mVpFragment.setCurrentItem(2, false);
       //    //mList.get(1).onHiddenChanged(true);
       //    //mList.get(2).onHiddenChanged(false);
       //} else if (mCurrentPosition == 3) {
       //    //        若当前为倒数第一张，设置页面为第二张
       //    mVpFragment.setCurrentItem(1, false);
       //    //mList.get(1).onHiddenChanged(false);
       //    //mList.get(2).onHiddenChanged(true);
       //}
    }

    private class MyFragmentAdapter extends FragmentPagerAdapter {

        List<BaseFragment> list;

        MyFragmentAdapter(FragmentManager fm, List<BaseFragment> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            //super.destroyItem(container, position, object);
        }

        /**
         * 返回需要展示的fragment
         *
         * @param position
         * @return
         */
        @Override
        public Fragment getItem(int position) {
            return list.get(position);
        }

        /**
         * 返回需要展示的fangment数量
         *
         * @return
         */
        @Override
        public int getCount() {
            return list.size();
        }

    }

    public void setPager(int item) {
        mVpFragment.setCurrentItem(item);
    }

    Handler mHandler = new Handler();

    private Runnable mRunnable = new Runnable() {
        public void run() {
            if (isDeviceCanBeControlled()) {
                //progressDialog.cancel();
            } else {
                //progressDialog.cancel();
                toastDeviceNoReadyAndExit();
            }
        }

    };

    private void toastDeviceNoReadyAndExit() {
        Toast.makeText(this, getString(R.string.equipment_no_response), Toast.LENGTH_SHORT).show();
        finish();
    }

    /*
     * 获取设备硬件信息回调
     */
    @Override
    protected void didGetHardwareInfo(GizWifiErrorCode result, GizWifiDevice device,
                                      ConcurrentHashMap<String, String> hardwareInfo) {
        super.didGetHardwareInfo(result, device, hardwareInfo);
        StringBuffer sb = new StringBuffer();
        if (GizWifiErrorCode.GIZ_SDK_SUCCESS != result) {
            myToast(getString(R.string.get_info_error) + result.name());
        } else {
            sb.append("Wifi Hardware Version:" + hardwareInfo.get(WIFI_HARDVER_KEY) + "\r\n");
            sb.append("Wifi Software Version:" + hardwareInfo.get(WIFI_SOFTVER_KEY) + "\r\n");
            sb.append("MCU Hardware Version:" + hardwareInfo.get(MCU_HARDVER_KEY) + "\r\n");
            sb.append("MCU Software Version:" + hardwareInfo.get(MCU_SOFTVER_KEY) + "\r\n");
            sb.append("Wifi Firmware Id:" + hardwareInfo.get(WIFI_FIRMWAREID_KEY) + "\r\n");
            sb.append("Wifi Firmware Version:" + hardwareInfo.get(WIFI_FIRMWAREVER_KEY) + "\r\n");
            // 设备属性
            sb.append("Device ID:" + "\r\n" + mDevice.getDid() + "\r\n");
            sb.append("Device IP:" + mDevice.getIPAddress() + "\r\n");
            sb.append("Device MAC:" + mDevice.getMacAddress() + "\r\n");
        }
        showHardwareInfo(sb.toString());
    }

    /**
     * 展示设备硬件信息
     *
     * @param hardwareInfo
     */
    private void showHardwareInfo(String hardwareInfo) {
        Lg.d("----showHardwareInfo--:" + hardwareInfo);
        String hardwareInfoTitle = getString(R.string.hardware_information);
        new AlertDialog.Builder(this).setTitle(hardwareInfoTitle).setMessage(hardwareInfo)
                .setPositiveButton(R.string.besure, null).show();
    }

    /*
     * 设置设备别名和备注回调
     */
    @Override
    protected void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
        super.didSetCustomInfo(result, device);
        Lg.d("----didSetCustomInfo--:" + result.name());
        if (GizWifiErrorCode.GIZ_SDK_SUCCESS == result) {
            myToast(getString(R.string.set_success));
            //progressDialog.cancel();
            finish();
        } else {
            myToast(getString(R.string.set_error) + result.name());
        }
    }

    /*
     * 设备状态改变回调，只有设备状态为可控才可以下发控制命令
     */
    @Override
    protected void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {
        super.didUpdateNetStatus(device, netStatus);
        Lg.e("接收到数据didUpdateNetStatus：" + netStatus);
        if (netStatus == GizWifiDeviceNetStatus.GizDeviceControlled) {
            mHandler.removeCallbacks(mRunnable);
            //progressDialog.cancel();
            Lg.d("---透传读取数据---");
            sendCommand(BINARY_KEY, new byte[8]);

            //TODO:判断当前是否是初始化，mCurrentAction
            //if (mCurrentAction == GosDeviceListActivity.ACTION_CLOCK && isInit) {
            //    Intent intent = new Intent(HangerControlActivity.this, TimerSettingActivity.class);
            //    intent.putExtra(Constant.DEVICE, mDevice);
            //    startActivity(intent);
            //}
            isInit = false;
        } else {
            mHandler.sendEmptyMessage(HangerControlActivity.handler_key.DISCONNECT.ordinal());
        }
    }

    /*
     * 设备上报数据回调，此回调包括设备主动上报数据、下发控制命令成功后设备返回ACK
     */
    @Override
    protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
                                  ConcurrentHashMap<String, Object> dataMap, int sn) {
        super.didReceiveData(result, device, dataMap, sn);
        Lg.d("HangerControl---didReceiveData---");
        if ((result == GizWifiErrorCode.GIZ_SDK_SUCCESS) && dataMap.get("binary") != null) {
            mCustomWaitDialog2.cancel();
            byte[] binary = (byte[]) dataMap.get("binary");
            Lg.e("liang", "didReceiveData接收:" + Arrays.toString(binary));
            Toast.makeText(HangerControlActivity.this, "接收数据:" + Arrays.toString(binary), Toast.LENGTH_LONG).show();
            progressDialog.cancel();
            mHangerBean.setReceiveData(binary);
            //mList.get(mVpFragment.getCurrentItem()).initView();
            mList.get(0).initView();
            mList.get(1).initView();

            mHandler.removeCallbacks(waitRunnable);
            cancelDialog();
        } else {
            progressDialog.cancel();
            //cancelDialog();
            //finish();
        }
    }

}
