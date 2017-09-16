package com.uascent.jz.ua420r.ConfigModule;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiConfigureMode;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.enumration.GizWifiGAgentType;
import com.uascent.jz.ua420r.CommonModule.GosDeploy;
import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.utils.Constant;
import com.uascent.jz.ua420r.utils.Lg;
import com.uascent.jz.ua420r.view.ZzHorizontalProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;

@SuppressLint("HandlerLeak")
public class GosAirlinkConfigCountdownActivity extends
        GosConfigModuleBaseActivity {

    /**
     * The tv Time
     */
    private TextView tvTimer;
    private TextView tvTimer2;
    private TextView tvTimer3;

    /**
     * The rpb Config
     */

    /**
     * 倒计时
     */
    int secondleft = 120;

    /**
     * The timer
     */
    Timer timer;

    /**
     * 配置用参数
     */
    String workSSID, workSSIDPsw;

    /**
     * The String
     */
    String timerText;
    String MAC;
    boolean isgotobind = false;

    boolean isStartBind = false;

    List<GizWifiGAgentType> modeList, modeDataList;

    @Bind(R.id.pb3)
    public ZzHorizontalProgressBar bar;


    int bindNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gos_airlink_config_countdown);
        // 设置ActionBar
        setActionBar(false, false, R.string.configcountDown_title);
        ButterKnife.bind(this);
        initView();
        initData();
        Lg.e("TAG", "----------------------------开始配网-------------------");
        startAirlink();

    }

    private void initView() {
        tvTimer = (TextView) findViewById(R.id.tvTimer);
        tvTimer2 = (TextView) findViewById(R.id.tvTimer2);
        tvTimer3 = (TextView) findViewById(R.id.tvTimer3);
        //mIv_pic_pairing_run = (ImageView) findViewById(R.id.iv_pic_pairing_run);
    }

    private void initAni() {
        RotateAnimation rotate = new RotateAnimation(0f, 60f, Animation.RELATIVE_TO_SELF, 0.9f, Animation.RELATIVE_TO_SELF, 0.9f);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        rotate.setDuration(1500);//设置动画持续时间
        rotate.setRepeatCount(-1);//设置重复次数
        rotate.setRepeatMode(Animation.REVERSE);
        rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        rotate.setStartOffset(10);//执行前的等待时间
        //mIv_pic_pairing_run.setAnimation(rotate);
    }

    private void initData() {
        workSSID = spf.getString("workSSID", "");
        workSSIDPsw = spf.getString("workSSIDPsw", "");
        modeDataList = new ArrayList<GizWifiGAgentType>();
        modeDataList.add(GizWifiGAgentType.GizGAgentESP);
        modeDataList.add(GizWifiGAgentType.GizGAgentMXCHIP);
        modeDataList.add(GizWifiGAgentType.GizGAgentHF);
        modeDataList.add(GizWifiGAgentType.GizGAgentRTK);
        modeDataList.add(GizWifiGAgentType.GizGAgentWM);
        modeDataList.add(GizWifiGAgentType.GizGAgentQCA);
        modeDataList.add(GizWifiGAgentType.GizGAgentTI);
        modeDataList.add(GizWifiGAgentType.GizGAgentFSK);
        modeDataList.add(GizWifiGAgentType.GizGAgentMXCHIP3);
        modeDataList.add(GizWifiGAgentType.GizGAgentBL);
        modeDataList.add(GizWifiGAgentType.GizGAgentAtmelEE);
        modeDataList.add(GizWifiGAgentType.GizGAgentOther);
        modeList = new ArrayList<GizWifiGAgentType>();

        modeList.add(modeDataList
                .get(Constant.modeNum));
        bar.setMax(120);

    }

    private void startAirlink() {
        GizWifiSDK.sharedInstance().setDeviceOnboarding(workSSID, workSSIDPsw,
                GizWifiConfigureMode.GizWifiAirLink, null, 60, modeList);
        handler.sendEmptyMessage(handler_key.START_TIMER.ordinal());

    }

    private enum handler_key {

        /**
         * 倒计时提示
         */
        TIMER_TEXT,

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
        FAILED,

    }

    /**
     * The handler.
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler_key key = handler_key.values()[msg.what];
            switch (key) {
                case TIMER_TEXT:
                    int obj = (int) msg.obj;
                    if (obj == 118) {
                        tvTimer.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(GosAirlinkConfigCountdownActivity.this, R.drawable.circle_blue), null, null, null);
                        tvTimer.setTextColor(ContextCompat.getColor(GosAirlinkConfigCountdownActivity.this, R.color.primary));
                    } else if (obj == 90) {
                        tvTimer2.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(GosAirlinkConfigCountdownActivity.this, R.drawable.circle_blue), null, null, null);
                        tvTimer2.setTextColor(ContextCompat.getColor(GosAirlinkConfigCountdownActivity.this, R.color.primary));
                    } else if (obj == 60) {
                        tvTimer2.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(GosAirlinkConfigCountdownActivity.this, R.drawable.circle_blue), null, null, null);
                        tvTimer3.setTextColor(ContextCompat.getColor(GosAirlinkConfigCountdownActivity.this, R.color.primary));
                    }
                    break;

                case START_TIMER:
                    isStartTimer();
                    break;

                case SUCCESSFUL:
                    if (progressDialog.isShowing()){
                        progressDialog.cancel();
                    }
                    if (timer != null) {
                        timer.cancel();
                    }
                    Lg.d("----配置成功----");
                    handler.removeCallbacks(runnable);
                    Toast.makeText(GosAirlinkConfigCountdownActivity.this,
                            R.string.configuration_successful, toastTime).show();
                    finish();
                    break;

                case FAILED:
                    if (progressDialog.isShowing()){
                        progressDialog.cancel();
                    }
                    if (timer != null) {
                        timer.cancel();
                    }
                    Lg.d("----配置失败----");
                    handler.removeCallbacks(runnable);
                    Toast.makeText(GosAirlinkConfigCountdownActivity.this,
                            msg.obj.toString(), toastTime).show();
                    Intent intent = new Intent(
                            GosAirlinkConfigCountdownActivity.this,
                            GosConfigFailedActivity.class);
                    intent.putExtra("mode", "smartlink");
                    intent.putExtra("msg", msg.obj.toString());
                    startActivity(intent);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    finish();
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
            quitAlert(this, timer);
            return true;
        }
        return false;
    }

    public void onBack() {
        Intent intent = new Intent(
                GosAirlinkConfigCountdownActivity.this,
                GosAirlinkChooseDeviceWorkWiFiActivity.class);
        startActivity(intent);
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
                    Message message = new Message();
                    message.what = handler_key.TIMER_TEXT.ordinal();
                    message.obj = 118;
                    handler.sendMessage(message);
                } else if (secondleft == 90) {
                    Message message = new Message();
                    message.what = handler_key.TIMER_TEXT.ordinal();
                    message.obj = 90;
                    handler.sendMessage(message);
                } else if (secondleft == 60) {
                    Message message = new Message();
                    message.what = handler_key.TIMER_TEXT.ordinal();
                    message.obj = 60;
                    handler.sendMessage(message);

                }
            }
        }, 1000, 1000);
    }

    /**
     * 设备配置回调
     *
     * @param result     错误码
     * @param mac        MAC
     * @param did        DID
     * @param productKey PK
     */
    protected void didSetDeviceOnboarding(GizWifiErrorCode result, final String mac,
                                          final String did, final String productKey) {
        if (GizWifiErrorCode.GIZ_SDK_DEVICE_CONFIG_IS_RUNNING == result) {
            return;
        }
        MAC = mac;
        if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
            Lg.e("TAG", "----------------------------配网成功-------------------" + MAC);
            tvTimer2.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(GosAirlinkConfigCountdownActivity.this, R.drawable.circle_blue), null, null, null);
            tvTimer2.setTextColor(ContextCompat.getColor(GosAirlinkConfigCountdownActivity.this, R.color.primary));
            tvTimer3.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(GosAirlinkConfigCountdownActivity.this, R.drawable.circle_blue), null, null, null);
            tvTimer3.setTextColor(ContextCompat.getColor(GosAirlinkConfigCountdownActivity.this, R.color.primary));
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

        } else {
            Lg.e("TAG", "------------------------配网失败-------------------");
            Message message = new Message();
            message.what = handler_key.FAILED.ordinal();
            message.obj = getString(R.string.set_net_error);
            handler.sendMessage(message);
        }

        Log.i("Apptest", result.toString());
    }


    private void goBindDev(String mac) {
        isStartBind = true;
        if (!progressDialog.isShowing()) {
            progressDialog.setMessage(getString(R.string.binding_please_later));
            progressDialog.show();
        }
        bindNum = 0;
        handler.postDelayed(runnable, 50 * 1000);
        Lg.e("TAG", "----------------------------开始绑定-------------------" + MAC);
        String uid   = spf.getString("Uid", "");
        String token = spf.getString("Token", "");
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
        handler.removeCallbacks(runnable);
        ButterKnife.unbind(this);
    }

    public void didDiscovered(GizWifiErrorCode result, java.util.List<GizWifiDevice> deviceList) {

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
                        message.what = handler_key.SUCCESSFUL.ordinal();
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
                    progressDialog.cancel();
                    bindErrorDialog();
                }
            });

        }
    };

    private void bindErrorDialog() {
        isStartBind = false;
        final Dialog dialog = new AlertDialog.Builder(GosAirlinkConfigCountdownActivity.this)
                .setView(new EditText(GosAirlinkConfigCountdownActivity.this)).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_judge2);

        LinearLayout llNo, llSure;
        llNo = (LinearLayout) window.findViewById(R.id.tv_dialog_cancel);
        llSure = (LinearLayout) window.findViewById(R.id.tv_dialog_enter);
        ((TextView) window.findViewById(R.id.tv_title)).setText(getString(R.string.fail_heavy_tied));

        llNo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.cancel();
                Message message = new Message();
                message.what = handler_key.FAILED.ordinal();
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


    public void didBindDevice(GizWifiErrorCode result, java.lang.String did) {
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
