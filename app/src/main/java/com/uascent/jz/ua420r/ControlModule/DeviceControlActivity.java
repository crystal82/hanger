package com.uascent.jz.ua420r.ControlModule;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizDeviceSharingUserRole;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.larksmart7618.sdk.communication.tools.commen.ToastTools;
import com.uascent.jz.ua420r.CommonModule.GosDeploy;
import com.uascent.jz.ua420r.DeviceModule.GosDeviceListActivity;
import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.sharingdevice.addSharedActivity;
import com.uascent.jz.ua420r.timer.Constant;
import com.uascent.jz.ua420r.timer.TimerSettingActivity;
import com.uascent.jz.ua420r.utils.HexStrUtils;
import com.uascent.jz.ua420r.utils.Lg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by maxiao on 2017/7/10.
 */

public class DeviceControlActivity extends GosControlModuleBaseActivity
        implements View.OnClickListener, TextView.OnEditorActionListener, SeekBar.OnSeekBarChangeListener {

    /**
     * 设备列表传入的设备变量
     */
    private GizWifiDevice mDevice;

    private boolean sw_bool_switch = false;
    private boolean issend         = true;

    @Bind(R.id.ll_dev)
    public LinearLayout ll_Dev;

    @Bind(R.id.ll_c)
    public LinearLayout ll_C;

    @Bind(R.id.iv_switch)
    public ImageView iv_Switch;

    @Bind(R.id.s_switch)
    public ImageView s_Switch;

    @Bind(R.id.share)
    public ImageView share;

    @Bind(R.id.timer)
    public ImageView timer;

    @Bind(R.id.tv_switch)
    public TextView tv_Switch;

    @Bind(R.id.tv_share)
    public TextView tv_Share;

    @Bind(R.id.tv_timer)
    public TextView tv_Timer;

    @Bind(R.id.dev_sw)
    public TextView  dev_Sw;
    @Bind(R.id.iv_wifi_icon)
    public ImageView imgLvl;

    private int mCurrentAction;
    private boolean isInit = true;


    private Vibrator mVibrator;  //声明一个振动器对象

    List<String> attrsSwitch = new ArrayList<>();
    List<String> attrsLvl    = new ArrayList<>();

    private enum handler_key {

        /**
         * 更新界面
         */
        UPDATE_UI,

        DISCONNECT,
    }

    private Runnable mRunnable = new Runnable() {
        public void run() {
            if (isDeviceCanBeControlled()) {
                progressDialog.cancel();
            } else {
                progressDialog.cancel();
                toastDeviceNoReadyAndExit();
            }
        }

    };

    /**
     * The handler.
     */
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DeviceControlActivity.handler_key key = DeviceControlActivity.handler_key.values()[msg.what];
            switch (key) {
                case UPDATE_UI:
                    updateUI(mSwitch);
                    break;
                case DISCONNECT:
                    toastDeviceDisconnectAndExit();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        ButterKnife.bind(this);
        initDevice();
        setActionBar(true, true, getDeviceName());
        actionBar.setBackgroundDrawable(GosDeploy.setBarColor(R.color.color_7a7a7a));
        initEvent();
    }

    private void initEvent() {
        timer.setOnClickListener(this);
        share.setOnClickListener(this);
        mVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
    }

    private void initDevice() {
        Intent intent = getIntent();
        mDevice = (GizWifiDevice) intent.getParcelableExtra("GizWifiDevice");
        mCurrentAction = intent.getIntExtra("CurrentAction", 0);
        Log.i("Apptest", mDevice.getDid());
        attrsSwitch.add(KEY_SWITCH);
        attrsLvl.add(Constant.RSSI_LVL);
    }

    private String getDeviceName() {
        if (TextUtils.isEmpty(mDevice.getAlias())) {
            return mDevice.getProductName();
        }
        return mDevice.getAlias();
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
        mHandler.removeCallbacks(mRunnable);
        // 退出页面，取消设备订阅
        mDevice.setSubscribe(false);
        mDevice.setListener(null);
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.iv_switch, R.id.ll_c, R.id.s_switch})
    public void swOnClick() {
        if (sw_bool_switch) {
            sendData(KEY_SWITCH, !sw_bool_switch);
        } else {
            sendData(KEY_SWITCH, !sw_bool_switch);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.timer:
                Intent intent = new Intent(DeviceControlActivity.this, TimerSettingActivity.class);
                intent.putExtra(Constant.DEVICE, mDevice);
                startActivity(intent);
                break;
            case R.id.share:
                GizDeviceSharingUserRole sharingRole = mDevice.getSharingRole();

                if (sharingRole != null) {
                    int role = sharingRole.ordinal();
                    if (role == 1 || role == 2) {
                        Intent intent2 = new Intent(DeviceControlActivity.this, addSharedActivity.class);
                        intent2.putExtra("productname", mDevice.getProductName());
                        intent2.putExtra("did", mDevice.getDid());
                        startActivity(intent2);
                    } else
                        ToastTools.short_Toast(this, getString(R.string.no_permission));

                } else
                    ToastTools.short_Toast(this, getString(R.string.unknown_error));

                break;
            default:
                break;
        }
    }

    /*
     * ========================================================================
     * EditText 点击键盘“完成”按钮方法
     * ========================================================================
     */
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        switch (v.getId()) {
            default:
                break;
        }
        hideKeyBoard(ll_Dev);
        return false;

    }

    /*
     * ========================================================================
     * seekbar 回调方法重写
     * ========================================================================
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        switch (seekBar.getId()) {
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            default:
                break;
        }
    }

    /*
     * ========================================================================
     * 菜单栏
     * ========================================================================
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_more, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_setDeviceInfo:
                setDeviceInfo();
                break;

            case R.id.action_getHardwareInfo:
                if (mDevice.isLAN()) {
                    mDevice.getHardwareInfo();
                } else {
                    myToast(getString(R.string.not_in_lan));
                }
                break;

            case R.id.action_getStatu:
                mDevice.getDeviceStatus(attrsSwitch);
                mDevice.getDeviceStatus(attrsLvl);
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Description:根据保存的的数据点的值来更新UI
     */
    public void updateUI(boolean sw) {
        Log.e("TAG", "" + sw);
        if (sw) {
            ll_Dev.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
            ll_C.setBackgroundResource(R.drawable.device_on);
            iv_Switch.setImageResource(R.drawable.on);
            dev_Sw.setText(getString(R.string.is_open));
            dev_Sw.setTextColor(ContextCompat.getColor(this, R.color.white));
            tv_Switch.setTextColor(ContextCompat.getColor(this, R.color.white));
            s_Switch.setImageResource(R.drawable.s_on);
            share.setImageResource(R.drawable.share_on);
            timer.setImageResource(R.drawable.timer_on);
            tv_Switch.setTextColor(ContextCompat.getColor(this, R.color.white));
            tv_Share.setTextColor(ContextCompat.getColor(this, R.color.white));
            tv_Timer.setTextColor(ContextCompat.getColor(this, R.color.white));
            sw_bool_switch = true;
            actionBar.setBackgroundDrawable(GosDeploy.setBarColor(R.color.primary));

        } else {
            sw_bool_switch = false;
            ll_Dev.setBackgroundColor(ContextCompat.getColor(this, R.color.color_7a7a7a));
            ll_C.setBackgroundResource(R.drawable.device_off);
            iv_Switch.setImageResource(R.drawable.off);
            dev_Sw.setText(getString(R.string.is_close));
            dev_Sw.setTextColor(ContextCompat.getColor(this, R.color.color_c2c0c2));
            tv_Switch.setTextColor(ContextCompat.getColor(this, R.color.color_c2c0c2));
            s_Switch.setImageResource(R.drawable.s_off);
            share.setImageResource(R.drawable.share_off);
            timer.setImageResource(R.drawable.timer_off);
            tv_Switch.setTextColor(ContextCompat.getColor(this, R.color.color_c2c0c2));
            tv_Share.setTextColor(ContextCompat.getColor(this, R.color.color_c2c0c2));
            tv_Timer.setTextColor(ContextCompat.getColor(this, R.color.color_c2c0c2));
            actionBar.setBackgroundDrawable(GosDeploy.setBarColor(R.color.color_7a7a7a));
        }
    }

    public void updateLvl(int lvl) {
        Lg.e("lvl=" + lvl);
        if (lvl < 1) {
            imgLvl.setImageResource(R.drawable.lvl_0);
            imgLvl.setAlpha(0.5f);
        } else if (1 <= lvl && lvl < 2) {
            imgLvl.setImageResource(R.drawable.lvl_1);
            imgLvl.setAlpha(1.0f);
        } else if (2 <= lvl && lvl < 4) {
            imgLvl.setImageResource(R.drawable.lvl_2);
            imgLvl.setAlpha(1.0f);
        } else if (4 <= lvl) {
            imgLvl.setImageResource(R.drawable.lvl_3);
            imgLvl.setAlpha(1.0f);
        }
    }

    private void setEditText(EditText et, Object value) {
        et.setText(value.toString());
        et.setSelection(value.toString().length());
        et.clearFocus();
    }

    /**
     * Description:页面加载后弹出等待框，等待设备可被控制状态回调，如果一直不可被控，等待一段时间后自动退出界面
     */
    private void getStatusOfDevice() {
        // 设备是否可控
        if (isDeviceCanBeControlled()) {
            // 可控则查询当前设备状态
            mDevice.getDeviceStatus(attrsSwitch);
            mDevice.getDeviceStatus(attrsLvl);
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

    private void sendData(String key, Object value) {
        if (issend) {
            issend = false;
            sendCommand(key, value);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    issend = true;
                }
            }, 200);
            mVibrator.vibrate(new long[]{100, 100}, -1);
            updateUI(!sw_bool_switch);
        }
    }

    /**
     * 发送指令,下发单个数据点的命令可以用这个方法
     * <p>
     * <h3>注意</h3>
     * <p>
     * 下发多个数据点命令不能用这个方法多次调用，一次性多次调用这个方法会导致模组无法正确接收消息，参考方法内注释。
     * </p>
     *
     * @param key   数据点对应的标识名
     * @param value 需要改变的值
     */
    private void sendCommand(String key, Object value) {
        if (value == null) {
            return;
        }

        int                               sn      = 5;
        ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<String, Object>();
        hashMap.put(key, value);
        mDevice.write(hashMap, sn);
        Log.i("TAG", "下发命令：" + hashMap.toString());
    }

    private boolean isDeviceCanBeControlled() {
        return mDevice.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled;
    }

    private void toastDeviceNoReadyAndExit() {
        Toast.makeText(this, getString(R.string.equipment_no_response), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void toastDeviceDisconnectAndExit() {
        Toast.makeText(DeviceControlActivity.this, getString(R.string.disconnect), Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * 展示设备硬件信息
     *
     * @param hardwareInfo
     */
    private void showHardwareInfo(String hardwareInfo) {
        String hardwareInfoTitle = getString(R.string.hardware_information);
        new AlertDialog.Builder(this).setTitle(hardwareInfoTitle).setMessage(hardwareInfo)
                .setPositiveButton(R.string.besure, null).show();
    }

    /**
     * Description:设置设备别名与备注
     */
    private void setDeviceInfo() {

        final Dialog mDialog = new AlertDialog.Builder(this).setView(new EditText(this)).create();
        mDialog.show();

        Window window = mDialog.getWindow();
        window.setContentView(R.layout.alert_gos_set_device_info);

        final EditText etAlias;
        //final EditText etRemark;
        etAlias = (EditText) window.findViewById(R.id.etAlias);
        //etRemark = (EditText) window.findViewById(R.id.etRemark);

        LinearLayout llNo, llSure;
        llNo = (LinearLayout) window.findViewById(R.id.llNo);
        llSure = (LinearLayout) window.findViewById(R.id.llSure);

        if (!TextUtils.isEmpty(mDevice.getAlias())) {
            setEditText(etAlias, mDevice.getAlias());
        }
       /* if (!TextUtils.isEmpty(mDevice.getRemark())) {
            setEditText(etRemark, mDevice.getRemark());
        }*/

        llNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        llSure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etAlias.getText().toString())) {
                    myToast(getString(R.string.set_alias));
                    return;
                }
                mDevice.setCustomInfo("", etAlias.getText().toString());
                mDialog.dismiss();
                String loadingText = (String) getText(R.string.loadingtext);
                progressDialog.setMessage(loadingText);
                progressDialog.show();
            }
        });

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                hideKeyBoard(ll_Dev);
            }
        });
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

    /*
     * 设置设备别名和备注回调
     */
    @Override
    protected void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
        super.didSetCustomInfo(result, device);
        if (GizWifiErrorCode.GIZ_SDK_SUCCESS == result) {
            myToast(getString(R.string.set_success));
            progressDialog.cancel();
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
            progressDialog.cancel();

            //TODO:判断当前是否是初始化，mCurrentAction
            if (mCurrentAction == GosDeviceListActivity.ACTION_CLOCK && isInit) {
                Intent intent = new Intent(DeviceControlActivity.this, TimerSettingActivity.class);
                intent.putExtra(Constant.DEVICE, mDevice);
                startActivity(intent);
            }
            isInit = false;
        } else {
            mHandler.sendEmptyMessage(DeviceControlActivity.handler_key.DISCONNECT.ordinal());
        }
    }

    /*
     * 设备上报数据回调，此回调包括设备主动上报数据、下发控制命令成功后设备返回ACK
     */
    @Override
    protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
                                  ConcurrentHashMap<String, Object> dataMap, int sn) {
        super.didReceiveData(result, device, dataMap, sn);
        Lg.e("liang", "接收到数据" + dataMap + " sn:" + sn);
        StringBuilder builder = new StringBuilder();
        for (String dataKey : dataMap.keySet()) {
            Object rssi_lvl = dataMap.get(dataKey);
            builder.append(dataKey).append(":" + rssi_lvl + "   ");
        }
        Lg.e("liang", "didReceiveData接收:" + dataMap + " sn:" + sn + "   builder:" + builder.toString());
        Toast.makeText(this,
                       "接收:" + dataMap + " sn:" + sn + "   builder:" + builder.toString(),
                       Toast.LENGTH_SHORT).show();
        /*if (sn == 5) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    updateUI(!sw_bool_switch);
                }
            });
        }*/
        if ((result == GizWifiErrorCode.GIZ_SDK_SUCCESS) && dataMap.get("data") != null) {
            getDataFromReceiveDataMap(dataMap);
        }
        if ((result == GizWifiErrorCode.GIZ_SDK_SUCCESS) && dataMap.get("binary") != null) {
            byte[] binary = (byte[]) dataMap.get("binary");
            String data   = HexStrUtils.bytesToHexString(binary);
            String cmd    = data.substring(0, 2);
            String flag   = data.substring(2, 4);
            Lg.e(cmd + "////" + flag);
            if (cmd.equals(Constant.CMD_1) && flag.equals(Constant.FLAG)) {
                String sw = data.substring(4, 6);
                if (sw.equals("01")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI(true);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI(false);
                        }
                    });
                }
                final int lvl = Integer.parseInt(data.substring(6, 8), 16);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateLvl(lvl);
                    }
                });
            }
        }

    }

    protected void onStop() {
        super.onStop();
        mVibrator.cancel();
    }

}