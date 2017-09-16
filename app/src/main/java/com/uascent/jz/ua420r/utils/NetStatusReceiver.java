package com.uascent.jz.ua420r.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.gizwits.gizwifisdk.api.MessageHandler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxiao on 2017/7/27.
 */

public class NetStatusReceiver {
    private boolean internetReachable;
    private boolean netFree;
    private boolean netDisable;
    private boolean background;
    private Context mContext;
    static final String Net2G = "2G";
    static final String Net3G = "3G";
    static final String Net4G = "4G";
    static final String WIFI = "WIFI";
    static final String FreeWIFI = "LAN";
    static final String PublicWIFI = "WLAN";
    static final String CMCC = "CMCC";
    static final String ChinaNet = "ChinaNet";
    static final String ChinaUnicom = "ChinaUnicom";
    private NetStatusReceiver.BaiDuHandler pingHandler;

    NetStatusReceiver() {
    }

    public void onReceive(Context context) {
        this.mContext = context;
        Lg.e("NetStatusReceiver============> 666 ");

        try {
            this.background = false;
            this.netDisable = false;
            this.netFree = true;
            this.internetReachable = true;
            int e = Utils.getSn();
            JSONObject obj = new JSONObject();
            obj.put("cmd", 1005);
            obj.put("sn", e);
            obj.put("background", Utils.isApkBackground(context));
            ConnectivityManager manager = (ConnectivityManager)context.getSystemService("connectivity");
            NetworkInfo info = manager.getActiveNetworkInfo();
            if(info == null || !manager.getBackgroundDataSetting()) {
                this.netDisable = true;
                obj.put("netDisable", this.netDisable);
                this.sendMessage2Deamon(obj);
                Constant.netdisable = true;
                return;
            }

            Constant.netdisable = false;
            int netType = info.getType();
            int netSubtype = info.getSubtype();
            if(netType == 1) {
                String wifiSSID = Utils.getWifiSSID(context);
                if(TextUtils.isEmpty(Constant.wifissid)) {
                    Constant.wifissid = wifiSSID;
                }

                if(!Constant.wifissid.equalsIgnoreCase(Utils.getWifiSSID(context))) {
                    Constant.wifissid = Utils.getWifiSSID(context);
                    this.netDisable = true;
                    obj.put("netDisable", this.netDisable);
                    this.sendMessage2Deamon(obj);
                }

                this.PingBaiDu();
            } else if(netType == 0) {
                this.netDisable = true;
                obj.put("netDisable", this.netDisable);
                this.sendMessage2Deamon(obj);
                this.netDisable = false;
                Constant.wifissid = "unknow";
                obj.put("netDisable", this.netDisable);
                obj.put("netFree", false);
                obj.put("internetReachable", true);
                this.sendMessage2Deamon(obj);
            }
        } catch (JSONException var10) {
            var10.printStackTrace();
        }
        Lg.e("NetStatusReceiver============> 777 ");
    }

    private void sendMessage2Deamon(JSONObject obj) {
        MessageHandler.getSingleInstance().send(obj.toString());
    }

    private void PingBaiDu() {
        HandlerThread connectDaemonThread = new HandlerThread("ping144thread");
        if(this.pingHandler == null) {
            connectDaemonThread.start();
            this.pingHandler = new NetStatusReceiver.BaiDuHandler(connectDaemonThread.getLooper());
        }

        this.pingHandler.sendEmptyMessage(11111);
    }

    class BaiDuHandler extends Handler {
        public BaiDuHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            try {
                int e = Utils.getSn();
                JSONObject obj = new JSONObject();
                boolean netFree = Utils.isNetFree(NetStatusReceiver.this.mContext);
                obj.put("cmd", 1005);
                obj.put("sn", e);
                obj.put("netDisable", false);
                obj.put("background", Utils.isApkBackground(NetStatusReceiver.this.mContext));
                obj.put("netFree", netFree);
                obj.put("internetReachable", Utils.openUrl());
                MessageHandler.getSingleInstance().send(obj.toString());
            } catch (JSONException var5) {
                var5.printStackTrace();
            }

        }
    }
}
