package com.uascent.jz.ua420r.DeviceModule;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizDeviceSharing;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizDeviceSharingListener;
import com.larksmart7618.sdk.communication.tools.commen.ToastTools;
import com.uascent.jz.ua420r.CommonModule.GosDeploy;
import com.uascent.jz.ua420r.CommonModule.TipsDialog;
import com.uascent.jz.ua420r.ConfigModule.GosAddDeviceListActivity;
import com.uascent.jz.ua420r.ConfigModule.GosAirlinkChooseDeviceWorkWiFiActivity;
import com.uascent.jz.ua420r.ConfigModule.GosCheckDeviceWorkWiFiActivity;
import com.uascent.jz.ua420r.PushModule.GosPushManager;
import com.uascent.jz.ua420r.R;
import com.uascent.jz.ua420r.SettingsModule.GosSettiingsActivity;
import com.uascent.jz.ua420r.UserModule.GosUserLoginActivity;
import com.uascent.jz.ua420r.hangerPrj.HangerControlActivity;
import com.uascent.jz.ua420r.sharingdevice.gosZxingDeviceSharingActivity;
import com.uascent.jz.ua420r.timer.Constant;
import com.uascent.jz.ua420r.utils.Lg;
import com.uascent.jz.ua420r.utils.NetUtils;
import com.uascent.jz.ua420r.view.SlideListView2;
import com.uascent.jz.ua420r.view.VerticalSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import zxing.CaptureActivity;

@SuppressLint("HandlerLeak")
public class GosDeviceListActivity extends GosDeviceModuleBaseActivity implements OnClickListener, OnRefreshListener {

    public static boolean ACTION_CONFIG_DEVICE = false;
    public static String CONFIG_DEVICE_MAC;

    /**
     * The ll NoDevice
     */
    private ScrollView llNoDevice;

    /**
     * The img NoDevice
     */
    private ImageView imgNoDevice;

    /**
     * The btn NoDevice
     */
    private Button btnNoDevice;

    /**
     * The ic BoundDevices
     */
    private View icBoundDevices;


    /**
     * The tv BoundDevicesListTitle
     */
    private TextView tvBoundDevicesListTitle;


    /**
     * The ll NoBoundDevices
     */
    private LinearLayout llNoBoundDevices;


    /**
     * The slv BoundDevices
     */
    private SlideListView2 slvBoundDevices;


    /**
     * The sv ListGroup
     */
    private ScrollView svListGroup;

    /**
     * 适配器
     */
    GosDeviceListAdapter myadapter;

    List<GizWifiDevice> gizDevicesList = new ArrayList<>();

    /**
     * 设备热点名称列表
     */
    ArrayList<String> softNameList;

    /**
     * 与APP绑定的设备的ProductKey
     */
    private List<String> ProductKeyList;

    Intent intent;

    String softssid, uid, token;


    public static List<String> boundMessage;


    /**
     * 判断用户登录状态 0：未登录 1：实名用户登录 2：匿名用户登录 3：匿名用户登录中 4：匿名用户登录中断
     */
    public static int loginStatus;

    int threeSeconds = 3;

    /**
     * 获取设备列表
     */
    protected static final int GETLIST = 0;

    /**
     * 刷新设备列表
     */
    protected static final int UPDATALIST = 1;

    /**
     * 订阅成功前往控制页面
     */
    protected static final int TOCONTROL = 2;

    /**
     * 通知
     */
    protected static final int TOAST = 3;

    /**
     * 设备绑定
     */
    protected static final int BOUND = 9;

    /**
     * 设备解绑
     */
    protected static final int UNBOUND = 99;

    /**
     * 新设备提醒
     */
    protected static final int SHOWDIALOG = 999;

    private static final int PULL_TO_REFRESH = 888;

    private VerticalSwipeRefreshLayout mSwipeLayout;

    private VerticalSwipeRefreshLayout mSwipeLayout1;


    Handler handler = new Handler() {
        private AlertDialog myDialog;
        private TextView dialog_name;

        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GETLIST:

                    if (!uid.isEmpty() && !token.isEmpty()) {
                        GizWifiSDK.sharedInstance().getBoundDevices(uid, token, ProductKeyList);
                    }

                    //if (loginStatus == 0 && GosDeploy.setAnonymousLogin()) {
                    //    loginStatus = 3;
                    //    GizWifiSDK.sharedInstance().userLoginAnonymous();
                    //}

                    break;

                case UPDATALIST:
                    if (progressDialog.isShowing()) {
                        progressDialog.cancel();
                    }
                    UpdateUI();
                    break;

                case BOUND:

                    break;

                case UNBOUND:
                    progressDialog.show();
                    GizWifiSDK.sharedInstance().unbindDevice(uid, token, msg.obj.toString());
                    break;

                case TOCONTROL:
                    intent = new Intent(GosDeviceListActivity.this, HangerControlActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("GizWifiDevice", (GizWifiDevice) msg.obj);
                    bundle.putInt("CurrentAction", mCurrentAction);
                    intent.putExtras(bundle);
                    // startActivity(intent);
                    startActivityForResult(intent, 1);
                    break;

                case TOAST:

                    Toast.makeText(GosDeviceListActivity.this, msg.obj.toString(), 2000).show();
                    break;

                case PULL_TO_REFRESH:
                    handler.sendEmptyMessage(GETLIST);
                    mSwipeLayout.setRefreshing(false);
                    mSwipeLayout1.setRefreshing(false);

                    break;

                case SHOWDIALOG:

                    if (!softNameList.toString()
                            .contains(GosMessageHandler.getSingleInstance().getNewDeviceList().toString())) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(GosDeviceListActivity.this);
                        View                view    = View.inflate(GosDeviceListActivity.this, R.layout.alert_gos_new_device, null);
                        Button              diss    = (Button) view.findViewById(R.id.diss);
                        Button              ok      = (Button) view.findViewById(R.id.ok);
                        dialog_name = (TextView) view.findViewById(R.id.dialog_name);
                        String foundOneDevice, foundManyDevices;
                        foundOneDevice = (String) getText(R.string.not_text);
                        foundManyDevices = (String) getText(R.string.found_many_devices);
                        if (GosMessageHandler.getSingleInstance().getNewDeviceList().size() < 1) {
                            return;
                        }
                        if (GosMessageHandler.getSingleInstance().getNewDeviceList().size() == 1) {
                            String ssid = GosMessageHandler.getSingleInstance().getNewDeviceList().get(0);
                            if (!TextUtils.isEmpty(ssid)
                                    && ssid.equalsIgnoreCase(NetUtils.getCurentWifiSSID(GosDeviceListActivity.this))) {
                                return;
                            }
                            if (softNameList.toString().contains(ssid)) {
                                return;
                            }
                            softNameList.add(ssid);
                            dialog_name.setText(ssid + foundOneDevice);
                            softssid = ssid;
                        } else {
                            for (String s : GosMessageHandler.getSingleInstance().getNewDeviceList()) {
                                if (!softNameList.toString().contains(s)) {
                                    softNameList.add(s);
                                }
                            }
                            dialog_name.setText(foundManyDevices);
                        }
                        myDialog = builder.create();
                        Window window = myDialog.getWindow();
                        myDialog.setView(view);
                        myDialog.show();
                        window.setGravity(Gravity.BOTTOM);
                        ok.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (GosMessageHandler.getSingleInstance().getNewDeviceList().size() == 1) {
                                    Intent intent = new Intent(GosDeviceListActivity.this,
                                                               GosCheckDeviceWorkWiFiActivity.class);
                                    intent.putExtra("softssid", softssid);
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(GosDeviceListActivity.this,
                                                               GosCheckDeviceWorkWiFiActivity.class);
                                    startActivity(intent);
                                }
                                myDialog.cancel();
                            }
                        });
                        diss.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myDialog.cancel();
                            }
                        });
                    }
                    break;
            }
        }

        ;
    };
    private       int mCurrentAction = 0;
    public static int ACTION_CLOCK   = 1;
    public static int ACTION_NORMAL  = 0;

    public static boolean mThreadLogin = false;
    public static boolean sAutoLogin   = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gos_device_list);


        // 设置ActionBar
        // setActionBar(true, true, R.string.devicelist_title);
        // actionBar.setIcon(R.drawable.button_refresh);
        if (sAutoLogin) {
            progressDialog.show();
            GosDeviceListActivity.loginStatus = 0;
            GizWifiSDK.sharedInstance().userLogin(spf.getString("UserName", ""), spf.getString("PassWord", ""));
        }
        Lg.d("-----GosDeviceListActivity:----" + sAutoLogin + "   mThreadLogin:" + mThreadLogin);

        handler.sendEmptyMessage(GETLIST);

        softNameList = new ArrayList<String>();
        initData();
        initView();
        initEvent();
    }


	/*
     * @Override public void onWindowFocusChanged(boolean hasFocus) {
	 * super.onWindowFocusChanged(hasFocus); if (hasFocus && isFrist) {
	 * progressDialog.show();
	 * 
	 * isFrist = false; } }
	 */

    @Override
    protected void onResume() {
        super.onResume();
        GizDeviceSharing.setListener(new GizDeviceSharingListener() {

            @Override
            public void didCheckDeviceSharingInfoByQRCode(GizWifiErrorCode result, String userName, String productName,
                                                          String deviceAlias, String expiredAt) {
                // TODO Auto-generated method stub
                super.didCheckDeviceSharingInfoByQRCode(result, userName, productName, deviceAlias, expiredAt);

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                int errorcode = result.ordinal();

                if (8041 <= errorcode && errorcode <= 8050 || errorcode == 8308) {
                    Toast.makeText(GosDeviceListActivity.this, getResources().getString(R.string.sorry), 1).show();
                } else if (errorcode != 0) {
                    Toast.makeText(GosDeviceListActivity.this, getResources().getString(R.string.verysorry), 1).show();
                } else {
                    Intent tent = new Intent(GosDeviceListActivity.this, gosZxingDeviceSharingActivity.class);

                    tent.putExtra("userName", userName);
                    tent.putExtra("productName", productName);
                    tent.putExtra("deviceAlias", deviceAlias);
                    tent.putExtra("expiredAt", expiredAt);
                    tent.putExtra("code", boundMessage.get(2));

                    startActivity(tent);

                }
            }

        });
        if (!sAutoLogin) {
            deviceslist = GizWifiSDK.sharedInstance().getDeviceList();
        }
        UpdateUI();
        // TODO GosMessageHandler.getSingleInstance().SetHandler(handler);
        if (boundMessage.size() != 0) {
            progressDialog.show();
            if (boundMessage.size() == 2) {
                GizWifiSDK.sharedInstance().bindDevice(uid, token, boundMessage.get(0), boundMessage.get(1), null);
            } else if (boundMessage.size() == 1) {
                GizWifiSDK.sharedInstance().bindDeviceByQRCode(uid, token, boundMessage.get(0));
            } else if (boundMessage.size() == 3) {

                GizDeviceSharing.checkDeviceSharingInfoByQRCode(spf.getString("Token", ""), boundMessage.get(2));
            } else {
                Log.i("Apptest", "ListSize:" + boundMessage.size());
            }
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        boundMessage.clear();
        // TODO GosMessageHandler.getSingleInstance().SetHandler(null);

    }

    private void initView() {
        svListGroup = (ScrollView) findViewById(R.id.svListGroup);
        llNoDevice = (ScrollView) findViewById(R.id.llNoDevice);
        imgNoDevice = (ImageView) findViewById(R.id.imgNoDevice);
        btnNoDevice = (Button) findViewById(R.id.btnNoDevice);

        icBoundDevices = findViewById(R.id.icBoundDevices);

        slvBoundDevices = (SlideListView2) icBoundDevices.findViewById(R.id.slideListView1);

        llNoBoundDevices = (LinearLayout) icBoundDevices.findViewById(R.id.llHaveNotDevice);

        tvBoundDevicesListTitle = (TextView) icBoundDevices.findViewById(R.id.tvListViewTitle);

        String boundDevicesListTitle = (String) getText(R.string.bound_divices);
        tvBoundDevicesListTitle.setText(boundDevicesListTitle);

        // 下拉刷新

        mSwipeLayout = (VerticalSwipeRefreshLayout) findViewById(R.id.id_swipe_ly);

        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                                    android.R.color.holo_orange_light, android.R.color.holo_red_light);

        mSwipeLayout1 = (VerticalSwipeRefreshLayout) findViewById(R.id.id_swipe_ly1);
        mSwipeLayout1.setOnRefreshListener(this);
        mSwipeLayout1.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                                     android.R.color.holo_orange_light, android.R.color.holo_red_light);

    }

    private void initEvent() {

        imgNoDevice.setOnClickListener(this);
        btnNoDevice.setOnClickListener(this);

        slvBoundDevices.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                mCurrentAction = ACTION_NORMAL;
                goToControlPage(position);
            }
        });
        slvBoundDevices.initSlideMode(SlideListView2.MOD_FORBID);
    }

    //订阅，进入控制
    private void goToControlPage(int position) {

        GizWifiDevice device = gizDevicesList.get(position);
        if (device.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceOnline
                || device.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled) {
            progressDialog.show();
            slvBoundDevices.setEnabled(false);
            slvBoundDevices.postDelayed(new Runnable() {
                @Override
                public void run() {
                    slvBoundDevices.setEnabled(true);
                }
            }, 3000);
            device.setListener(getGizWifiDeviceListener());
            String productKey = device.getProductKey();
            if (productKey.equals(ProductKeyList.get(0))) {
                device.setSubscribe(GosDeploy.setProductSecret(), true);
            } else {
                device.setSubscribe(true);
            }
        } else {
            ToastTools.short_Toast(GosDeviceListActivity.this, getString(R.string.is_out_line));
        }
        }

    private void initData() {
        boundMessage = new ArrayList<String>();
        ProductKeyList = GosDeploy.setProductKeyList();
        uid = spf.getString("Uid", "");
        token = spf.getString("Token", "");

        if (uid.isEmpty() && token.isEmpty()) {
            loginStatus = 0;
        }
    }

    protected void didDiscovered(GizWifiErrorCode result, java.util.List<GizWifiDevice> deviceList) {
        //没登录不设置数据
        Lg.d("---didDiscovered:---" + mThreadLogin);
        if (!mThreadLogin) {
            return;
        }
        deviceslist.clear();
        for (GizWifiDevice gizWifiDevice : deviceList) {
            deviceslist.add(gizWifiDevice);
            Lg.e("TAG", gizWifiDevice.toString());
        }
        Log.e("TAG", "有数据更新 deviceslist.size=" + deviceslist.size());
        handler.sendEmptyMessage(UPDATALIST);

    }

    protected void didUserLogin(GizWifiErrorCode result, java.lang.String uid, java.lang.String token) {
        Lg.d("---didUserLogin4---:" + result);
        progressDialog.cancel();
        if (GizWifiErrorCode.GIZ_SDK_SUCCESS == result) {
            Toast.makeText(GosDeviceListActivity.this, R.string.toast_login_successful, toastTime).show();
            loginStatus = 1;

            this.uid = uid;
            this.token = token;
            spf.edit().putString("Uid", this.uid).commit();
            spf.edit().putString("Token", this.token).commit();
            handler.sendEmptyMessage(GETLIST);
            // TODO 绑定推送
            GosPushManager.pushBindService(token);
            mThreadLogin = true;
        } else {
            Toast.makeText(GosDeviceListActivity.this, R.string.toast_login_failed, toastTime).show();
            loginStatus = 0;
            //TODO:返回Login
            startActivity(new Intent(GosDeviceListActivity.this, GosUserLoginActivity.class));
            finish();
            //TODO:尝试匿名登录
            //if (GosDeploy.setAnonymousLogin()) {
            //    tryUserLoginAnonymous();
            //}

        }
    }

    protected void didUnbindDevice(GizWifiErrorCode result, java.lang.String did) {
        progressDialog.cancel();
        if (GizWifiErrorCode.GIZ_SDK_SUCCESS != result) {
            Toast.makeText(this, toastError(result), 2000).show();
        }
    }

    @Override
    protected void didSetSubscribe(GizWifiErrorCode result, GizWifiDevice device, boolean isSubscribed) {
        // TODO 控制页面跳转
        Log.e("liang", "接收到数据didSetSubscribe:" + result);
        progressDialog.cancel();
        if (ACTION_CONFIG_DEVICE) {
            ACTION_CONFIG_DEVICE = false;
            return;
        }
        Message msg = new Message();
        if (GizWifiErrorCode.GIZ_SDK_SUCCESS == result) {
            msg.what = TOCONTROL;
            msg.obj = device;
        } else {
            if (device.isBind()) {
                msg.what = TOAST;
                // String setSubscribeFail = (String)
                // getText(R.string.setsubscribe_failed);
                msg.obj = toastError(result);// setSubscribeFail + "\n" + arg0;
            }
        }
        handler.sendMessage(msg);
    }


    /**
     * @param result
     */
    @Override
    protected void didChannelIDBind(GizWifiErrorCode result) {
        Log.i("Apptest", result.toString());
        if (GizWifiErrorCode.GIZ_SDK_SUCCESS != result) {
            Toast.makeText(this, toastError(result), 2000).show();
        }
    }

    /**
     * 设备绑定回调(旧)
     *
     * @param error
     * @param errorMessage
     * @param did
     */
    protected void didBindDevice(int error, String errorMessage, String did) {
        progressDialog.cancel();
        if (error != 0) {

            String toast = getResources().getString(R.string.bound_failed) + "\n" + errorMessage;
            Toast.makeText(this, toast, 2000).show();
            // Toast.makeText(this, R.string.bound_failed + "\n" + errorMessage,
            // 2000).show();
        } else {

            Toast.makeText(this, R.string.bound_successful, 2000).show();
        }

    }

    /**
     * 设备绑定回调
     *
     * @param result
     * @param did
     */
    protected void didBindDevice(GizWifiErrorCode result, java.lang.String did) {
        //progressDialog.cancel();
        //if (result != GizWifiErrorCode.GIZ_SDK_SUCCESS) {
        //    Toast.makeText(this, toastError(result), 2000).show();
        //} else {
        //    Toast.makeText(this, R.string.add_successful, 2000).show();
        //}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (!TextUtils.isEmpty(spf.getString("UserName", "")) && !TextUtils.isEmpty(spf.getString("PassWord", ""))) {
            getMenuInflater().inflate(R.menu.devicelist_logout, menu);
        } else {
            if (mThreadLogin) {
                getMenuInflater().inflate(R.menu.devicelist_logout, menu);
            } else {
                getMenuInflater().inflate(R.menu.devicelist_login, menu);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                if (checkNetwork(GosDeviceListActivity.this)) {
                    progressDialog.show();
                    handler.sendEmptyMessage(GETLIST);
                }
                break;
            case R.id.action_QR_code:

                intent = new Intent(GosDeviceListActivity.this, CaptureActivity.class);
                startActivity(intent);
                break;
            case R.id.action_change_user:
                if (item.getTitle() == getText(R.string.login)) {
                    logoutToClean();
                    break;
                }
                final Dialog dialog = new AlertDialog.Builder(this).setView(new EditText(this)).create();
                dialog.show();

                Window window = dialog.getWindow();
                window.setContentView(R.layout.alert_gos_logout);

                LinearLayout llNo, llSure;
                llNo = (LinearLayout) window.findViewById(R.id.llNo);
                llSure = (LinearLayout) window.findViewById(R.id.llSure);

                llNo.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                llSure.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        logoutToClean();
                    }
                });

                break;
            case R.id.action_addDevice:
                if (!checkNetwork(GosDeviceListActivity.this)) {
                    Toast.makeText(GosDeviceListActivity.this, R.string.network_error, 2000).show();
                } else {
                    //GosAirlinkChooseDeviceWorkWiFiActivity
                    intent = new Intent(GosDeviceListActivity.this, GosAirlinkChooseDeviceWorkWiFiActivity.class);
                /*
                 * intent = new Intent(GosDeviceListActivity.this,
				 * GosChooseDeviceActivity.class);
				 */
                    startActivity(intent);
                }
                break;
            case R.id.action_site:
                intent = new Intent(GosDeviceListActivity.this, GosSettiingsActivity.class);
                startActivityForResult(intent, 600);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UpdateUI() {
        List<GizWifiDevice> boundDevicesList   = new ArrayList<GizWifiDevice>();
        List<GizWifiDevice> offlineDevicesList = new ArrayList<GizWifiDevice>();
        List<String>        attrs              = new ArrayList<>();
        attrs.add(Constant.RSSI_LVL);

        for (final GizWifiDevice gizWifiDevice : deviceslist) {
            if (GizWifiDeviceNetStatus.GizDeviceOnline == gizWifiDevice.getNetStatus()
                    || GizWifiDeviceNetStatus.GizDeviceControlled == gizWifiDevice.getNetStatus()) {
                if (gizWifiDevice.isBind()) {
                    boundDevicesList.add(gizWifiDevice);
                }
            } else {
                offlineDevicesList.add(gizWifiDevice);
            }
        }
        //TODO:将离线在线设备合并
        if (boundDevicesList.isEmpty() && offlineDevicesList.isEmpty()) {
            slvBoundDevices.setVisibility(View.GONE);
            llNoBoundDevices.setVisibility(View.GONE); //VISIBLE

            svListGroup.setVisibility(View.GONE);
            llNoDevice.setVisibility(View.VISIBLE);
            mSwipeLayout1.setVisibility(View.VISIBLE);
        } else {
            gizDevicesList.clear();
            llNoDevice.setVisibility(View.GONE);
            mSwipeLayout1.setVisibility(View.GONE);
            svListGroup.setVisibility(View.VISIBLE);
            boundDevicesList.addAll(offlineDevicesList);
            gizDevicesList.addAll(boundDevicesList);
            myadapter = new GosDeviceListAdapter(this, gizDevicesList);//boundDevicesList
            myadapter.setHandler(handler);
            slvBoundDevices.setAdapter(myadapter);
            llNoBoundDevices.setVisibility(View.GONE);
            slvBoundDevices.setVisibility(View.VISIBLE);
            //设置点击闹钟，垃圾桶监听器
            setIconClickListener();
        }

    }


    private void setIconClickListener() {
        myadapter.setOnClockClickListener(new GosDeviceListAdapter.OnClockClickListener() {
            @Override
            public void onClick(int position) {
                mCurrentAction = ACTION_CLOCK;
                Log.d("GosDeviceList", "点击条目：" + position + "  定时操作");
                goToControlPage(position);
            }
        });
        myadapter.setOnTrashClickListener(new GosDeviceListAdapter.OnTrashClickListener() {
            @Override
            public boolean onClick() {
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgNoDevice:
            case R.id.btnNoDevice:
                if (!checkNetwork(GosDeviceListActivity.this)) {
                    Toast.makeText(GosDeviceListActivity.this, R.string.network_error, 2000).show();
                    return;
                }
                intent = new Intent(GosDeviceListActivity.this, GosAirlinkChooseDeviceWorkWiFiActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    private void tryUserLoginAnonymous() {
        threeSeconds = 3;
        final Timer tsTimer = new Timer();
        tsTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                threeSeconds--;
                if (threeSeconds <= 0) {
                    tsTimer.cancel();
                    handler.sendEmptyMessage(GETLIST);
                } else {
                    if (loginStatus == 4) {
                        tsTimer.cancel();
                    }
                }
            }
        }, 1000, 1000);
    }

    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click(); // 调用双击退出函数

        }
        return false;
    }

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;

    public void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出；
            String doubleClick;
            if (!TextUtils.isEmpty(spf.getString("UserName", ""))
                    && !TextUtils.isEmpty(spf.getString("PassWord", ""))) {
                doubleClick = (String) getText(R.string.doubleclick_logout);
            } else {
                if (mThreadLogin) {
                    doubleClick = (String) getText(R.string.doubleclick_logout);
                } else {
                    doubleClick = (String) getText(R.string.doubleclick_back);
                }
            }

            Toast.makeText(this, doubleClick, 2000).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
            //logoutToClean();
        }
    }

    /**
     * 注销函数
     */
    void logoutToClean() {
        spf.edit().putString("UserName", "").commit();
        spf.edit().putString("PassWord", "").commit();
        spf.edit().putString("Uid", "").commit();
        spf.edit().putString("Token", "").commit();
        GosPushManager.pushUnBindService(token);
        finish();
        if (loginStatus == 1) {
            loginStatus = 0;
        } else {
            loginStatus = 4;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 666) {
            finish();
        } else if (resultCode == 98765) {
            TipsDialog dialog = new TipsDialog(GosDeviceListActivity.this,
                                               getResources().getString(R.string.devicedisconnected));

            dialog.show();
        }
    }

    public Handler getMyHandler() {

        return handler;

    }

    @Override
    public void onRefresh() {
        handler.sendEmptyMessageDelayed(PULL_TO_REFRESH, 2000);

    }


}
