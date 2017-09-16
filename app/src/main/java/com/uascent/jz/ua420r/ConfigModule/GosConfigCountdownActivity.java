package com.uascent.jz.ua420r.ConfigModule;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiConfigureMode;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.uascent.jz.ua420r.CommonModule.GosDeploy;
import com.uascent.jz.ua420r.CommonModule.WifiAutoConnectManager;
import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.utils.Lg;
import com.uascent.jz.ua420r.utils.NetUtils;
import com.uascent.jz.ua420r.view.LinkAPTipDialog;
import com.uascent.jz.ua420r.view.ZzHorizontalProgressBar;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("HandlerLeak")
public class GosConfigCountdownActivity extends GosConfigModuleBaseActivity {

    private GosWifiChangeReciver broadcase;

    /**
     * The tv Time
     */
    private TextView tvTimer;


    /**
     * 倒计时
     */
    int secondleft = 120;

    /**
     * The timer
     */
    Timer timer;

    /**
     * The Frist
     */
    boolean isFrist = true;

    /**
     * The isChecked
     */
    boolean isChecked = false;

    String presentSSID, workSSID, workSSIDPsw;

    String MAC;
    boolean isgotobind = false;

    boolean isStartBind = false;
    int bindNum = 0;

    public ZzHorizontalProgressBar bar;
    LinkAPTipDialog linkAPTipDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gos_config_countdown);
        // 设置ActionBar
        setActionBar(false, false, R.string.configcountDown_title);
        initView();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFrist) {
            linkAPTipDialog = new LinkAPTipDialog(this, new LinkAPTipDialog.EventListener() {
                @Override
                public void onClickChanged() {
                    registerWifiReceiver();
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); //直接进入手机中的wifi网络设置界面
                }

                @Override
                public void onBackListener() {
                    linkAPTipDialog.dismiss();
                    onBack();
                }
            });
            isFrist = false;
        }
    }

    private void initView() {
        tvTimer = (TextView) findViewById(R.id.tvTimer);
        bar = (ZzHorizontalProgressBar) findViewById(R.id.pb3);
        bar.setMax(120);
    }


    // 监听网络
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, final Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                String presentSSID = NetUtils.getCurentWifiSSID(GosConfigCountdownActivity.this);
                if (!TextUtils.isEmpty(presentSSID) && presentSSID.contains(SoftAP_Start)) {
                    linkAPTipDialog.dismiss();
                    readyToSoftAP();
                }
            }
        }
    };

    private void registerWifiReceiver() {
        try {
            registerReceiver(mBroadcastReceiver, new IntentFilter(
                    WifiManager.NETWORK_STATE_CHANGED_ACTION));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unRegisterWifiReceiver() {
        try {
            if (mBroadcastReceiver != null) {
                unregisterReceiver(mBroadcastReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private enum handler_key {

        /**
         * 倒计时通知
         */
        TICK_TIME,

        /**
         * 倒计时开始
         */
        START_TIMER,

        /**
         * 配置成功
         */
        SUCCESSFUL,

        /**
         * 配置失败
         */
        FAILED, OFFTIME,

    }

    /**
     * The handler.
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler_key key = handler_key.values()[msg.what];
            switch (key) {

                case TICK_TIME:
                    String timerText = (String) getText(R.string.timer_text);
                    tvTimer.setText(timerText);
                    break;
                case START_TIMER:
                    isStartTimer();
                    break;

                case SUCCESSFUL:
                    if (progressDialog.isShowing()) {
                        progressDialog.cancel();
                    }
                    if (timer != null) {
                        timer.cancel();
                    }
                    handler.removeCallbacks(runnable);
                    Toast.makeText(GosConfigCountdownActivity.this, R.string.configuration_successful, toastTime)
                            .show();
                    finish();
                    break;

                case FAILED:
                    if (progressDialog.isShowing()) {
                        progressDialog.cancel();
                    }
                    if (timer != null) {
                        timer.cancel();
                    }
                    handler.removeCallbacks(runnable);
                    Toast.makeText(GosConfigCountdownActivity.this, msg.obj.toString(), toastTime)
                            .show();
                    Intent intent = new Intent(GosConfigCountdownActivity.this, GosConfigFailedActivity.class);
                    intent.putExtra("mode", "softAP");
                    intent.putExtra("msg", msg.obj.toString());
                    startActivity(intent);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    finish();
                    break;

                case OFFTIME:
                    GizWifiSDK.sharedInstance().setDeviceOnboarding(workSSID, workSSIDPsw,
                                                                    GizWifiConfigureMode.GizWifiSoftAP, presentSSID, 60, null);
                    break;

                default:
                    break;

            }
        }

    };

    // 屏蔽掉返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (linkAPTipDialog.isShowing()) {
                linkAPTipDialog.dismiss();
            }
            quitAlert(this, timer);
            return true;
        }
        return false;
    }

    public void onBack() {
        Intent intent = new Intent(
                GosConfigCountdownActivity.this,
                GosAirlinkChooseDeviceWorkWiFiActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    // 倒计时
    public void isStartTimer() {
        secondleft = 120;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                secondleft--;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float p = (120 - secondleft) * 1.0f;
                        if (p <= 120 && bar != null)
                            bar.setProgress(p);
                    }
                });
                if (secondleft == 118) {
                    handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
                }
                if (secondleft < 0) {

                }

            }
        }, 1000, 1000);
    }

    private void readyToSoftAP() {
        unRegisterWifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        workSSID = spf.getString("workSSID", "");
        workSSIDPsw = spf.getString("workSSIDPsw", "");
        handler.sendEmptyMessage(handler_key.START_TIMER.ordinal());
        presentSSID = NetUtils.getCurentWifiSSID(GosConfigCountdownActivity.this);
        Lg.e("--------------------///" + presentSSID);
        GizWifiSDK.sharedInstance().setDeviceOnboarding(workSSID, workSSIDPsw,
                                                        GizWifiConfigureMode.GizWifiSoftAP, presentSSID, 60, null);

        if (broadcase == null) {
            Lg.e("-----------------------------GosWifiChangeReciver//" + NetUtils.getConnectWifiSsid(this));
            broadcase = new GosWifiChangeReciver();
            registerReceiver(broadcase, filter);
        }

    }

    /**
     * 设备配置回调
     *
     * @param result     错误码
     * @param mac        MAC
     * @param did        DID
     * @param productKey PK
     */
    protected void didSetDeviceOnboarding(GizWifiErrorCode result, final String mac, String did, String productKey) {
        if (GizWifiErrorCode.GIZ_SDK_DEVICE_CONFIG_IS_RUNNING == result) {
            return;
        }
        MAC = mac;
        Log.e("TAG", MAC + "----------------" + mac);
        if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
            tvTimer.setText(getString(R.string.search_join));
            secondleft = 60;
            progressDialog.setMessage(getString(R.string.binding_please_later));
            progressDialog.show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            goBindDev(mac);
                        }
                    });
                }
            }, 1000 * 10);

            //若没有连接上指定网络  则连接
            String presentSSID = NetUtils.getCurentWifiSSID(GosConfigCountdownActivity.this);
            Lg.e("presentSSID======" + presentSSID);
            if (TextUtils.isEmpty(presentSSID) || !presentSSID.contains(workSSID)) {
                Lg.e("连接网络" + workSSID);
                WifiManager                           wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiAutoConnectManager.WifiCipherType cipherType  = WifiAutoConnectManager.getCipherType(GosConfigCountdownActivity.this, workSSID);
                WifiAutoConnectManager                manager     = new WifiAutoConnectManager(wifiManager);
                manager.connect(workSSID, workSSIDPsw, cipherType);
            }
        } else {
            Message message = new Message();
            message.what = handler_key.FAILED.ordinal();
            message.obj = getString(R.string.set_net_error);
            handler.sendMessage(message);
        }
    }

    private void goBindDev(String mac) {
        isStartBind = true;
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage(getString(R.string.binding_please_later));
            progressDialog.show();
        }
        bindNum = 0;
        handler.postDelayed(runnable, 50 * 1000);
        String uid = spf.getString("Uid", "");
        String token = spf.getString("Token", "");
        Lg.e("TAG", "----------------------------开始绑定-------------------");
        GizWifiSDK.sharedInstance().
                bindRemoteDevice(uid, token, mac, GosDeploy.setProductKeyList().get(0), GosDeploy.setProductSecret());
        GizWifiSDK.sharedInstance().
                bindRemoteDevice(uid, token, mac, GosDeploy.setProductKeyList().get(0), GosDeploy.setProductSecret());
        GizWifiSDK.sharedInstance().
                bindRemoteDevice(uid, token, mac, GosDeploy.setProductKeyList().get(0), GosDeploy.setProductSecret());
        isgotobind = true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isChecked = false;

        if (broadcase != null) {
            unregisterReceiver(broadcase);
            broadcase = null;
        }
        if (linkAPTipDialog.isShowing()) {
            linkAPTipDialog.dismiss();
        }
        unRegisterWifiReceiver();

    }

    public void didDiscovered(GizWifiErrorCode result, List<GizWifiDevice> deviceList) {
        if (!isgotobind || bindNum >= 4) {
            return;
        }
        if (!TextUtils.isEmpty(MAC)) {
            Lg.e("TAG", "----------------------------didDiscovered-------------------" + deviceList.size());
            for (GizWifiDevice device : deviceList) {
                Lg.e("TAG", "-------device------" + device.toString());
                if (device.getMacAddress().equals(MAC)) {

                    if (!device.isBind()) {
                        bindNum = 0;
                        String uid = spf.getString("Uid", "");
                        String token = spf.getString("Token", "");
                        if (isStartBind) {
                            GizWifiSDK.sharedInstance().
                                    bindRemoteDevice(uid, token, MAC, GosDeploy.setProductKeyList().get(0), GosDeploy.setProductSecret());
                        }
                        return;
                    }
                    Lg.e("TAG", "----------------bindNum=" + bindNum);
                    if (bindNum == 3) {
                        bindNum++;
                        Message message = new Message();
                        message.what = GosConfigCountdownActivity.handler_key.SUCCESSFUL.ordinal();
                        handler.sendMessage(message);
                        Lg.e("TAG", "--------success--------" + device.getMacAddress());
                        return;
                    } else if (bindNum < 3) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bindNum++;
                        String uid = spf.getString("Uid", "");
                        String token = spf.getString("Token", "");
                        List<String> ProductKeyList = GosDeploy.setProductKeyList();
                        GizWifiSDK.sharedInstance().getBoundDevices(uid, token, ProductKeyList);
                    }
                }
            }
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Lg.e("TAG", "-----------------------runnable error-----------------");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog.isShowing())
                        progressDialog.cancel();
                    bindErrorDialog();
                }
            });
        }
    };

    private void bindErrorDialog() {
        isStartBind = false;
        final Dialog dialog = new AlertDialog.Builder(GosConfigCountdownActivity.this)
                .setView(new EditText(GosConfigCountdownActivity.this)).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_judge2);

        LinearLayout llNo, llSure;
        llNo = (LinearLayout) window.findViewById(R.id.tv_dialog_cancel);
        llSure = (LinearLayout) window.findViewById(R.id.tv_dialog_enter);
        ((TextView) window.findViewById(R.id.tv_title)).setText("绑定失败，是否重绑？");

        llNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.cancel();
                Message message = new Message();
                message.what = GosConfigCountdownActivity.handler_key.FAILED.ordinal();
                message.obj = getString(R.string.device_bind_error);
                handler.sendMessage(message);
            }
        });
        llSure.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                goBindDev(MAC);
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            }
        });
    }

    public void didBindDevice(GizWifiErrorCode result, String did) {
        if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
            if (bindNum <= 3) {
                String uid = spf.getString("Uid", "");
                String token = spf.getString("Token", "");
                List<String> ProductKeyList = GosDeploy.setProductKeyList();
                GizWifiSDK.sharedInstance().getBoundDevices(uid, token, ProductKeyList);
            }
            Lg.e("TAG", "-----------------------bind success-----------------" + bindNum);
        } else {
            Lg.e("TAG", "-----------------------bind fail-----------------" + did);
            String uid = spf.getString("Uid", "");
            String token = spf.getString("Token", "");
            if (isStartBind) {
                GizWifiSDK.sharedInstance().
                        bindRemoteDevice(uid, token, MAC, GosDeploy.setProductKeyList().get(0), GosDeploy.setProductSecret());
            }
        }
    }

}
