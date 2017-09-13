package com.uascent.jz.ua420r.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class MyWebStateReceive extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 如果相等的话就说明网络状态发生了变化
        //if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) { }
        //TODO:打印网络状态
        //String netWorkState = getNetWorkState(context);
        // 接口回调传过去状态的类型
        //Log.e("MyWebStateReceive", "-------网络状态广播-----1--：" + netWorkState);
        Lg.e("MyWebStateReceive", "-------网络状态广播-----1--：");
        new NetStatusReceiver().onReceive(context);

    }

    /**
     * 没有连接网络
     */
    private static final String NETWORK_NONE   = "没有连接网络";
    /**
     * 移动网络
     */
    private static final String NETWORK_MOBILE = "移动网络";
    /**
     * 无线网络
     */
    private static final String NETWORK_WIFI   = "无线网络";

    public static String getNetWorkState(Context context) {
        // 得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            Log.e("MyWebStateReceive", "-------网络状态广播----2---：" + activeNetworkInfo.toString());

            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORK_MOBILE;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }

}

