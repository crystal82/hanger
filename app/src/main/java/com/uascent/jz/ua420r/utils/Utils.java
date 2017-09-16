package com.uascent.jz.ua420r.utils;

/**
 * Created by maxiao on 2017/7/27.
 */

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.gizwits.gizwifisdk.log.SDKLog;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class Utils {
    private static final String TAG = "GizWifiSDKClient-Utils";
    private static Integer sn = null;

    Utils() {
    }


    protected static boolean isApkBackground(Context context) {
        boolean isBackground = false;

        try {
            PackageManager e = context.getPackageManager();
            PackageInfo pi = e.getPackageInfo(context.getPackageName(), 0);
            String pkgName = pi.packageName;
            ActivityManager activityManager = (ActivityManager)context.getSystemService("activity");
            List appProcesses = activityManager.getRunningAppProcesses();
            if(appProcesses != null) {
                Iterator var7 = appProcesses.iterator();

                while(var7.hasNext()) {
                    ActivityManager.RunningAppProcessInfo appProcess = (ActivityManager.RunningAppProcessInfo)var7.next();
                    if(appProcess.processName.equals(pkgName)) {
                        if(appProcess.importance == 400) {
                            isBackground = true;
                        } else {
                            isBackground = false;
                        }
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException var9) {
            SDKLog.e("Retrieve package name exception " + var9);
        }

        SDKLog.d("is   background  :" + isBackground);
        return isBackground;
    }


    protected static boolean isNetFree(Context context) {
        boolean isFree = false;
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService("connectivity");
        if(connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if(info != null && info.getState() == NetworkInfo.State.CONNECTED && info.getType() == 1) {
                WifiManager wifiManager = (WifiManager)context.getSystemService("wifi");
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String wifiSSID = wifiInfo.getSSID();
                if(!TextUtils.isEmpty(wifiSSID) && !wifiSSID.equals("CMCC") && !wifiSSID.equals("ChinaNet") && !wifiSSID.equals("ChinaUnicom")) {
                    isFree = true;
                }
            }
        }

        return isFree;
    }


    public static int getSn() {
        if(sn == null) {
            sn = Integer.valueOf(0);
        } else {
            sn = Integer.valueOf(sn.intValue() + 1);
        }

        return sn.intValue();
    }

    public static String isEmpty(String ss) {
        return TextUtils.isEmpty(ss)?"null":"******";
    }

    public static boolean openUrl() {
        String myString = "";

        try {
            URL e = new URL("https://www.baidu.com/");
            HttpURLConnection urlCon = (HttpURLConnection)e.openConnection();
            urlCon.setRequestMethod("POST");
            urlCon.setRequestProperty("Accept-Encoding", "gzip");
            urlCon.setChunkedStreamingMode(0);
            urlCon.setConnectTimeout(20000);
            urlCon.setReadTimeout(20000);
            InputStream is = urlCon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(50);
            boolean current = false;

            int current1;
            while((current1 = bis.read()) != -1) {
                baf.append((byte)current1);
            }

            myString = EncodingUtils.getString(baf.toByteArray(), "UTF-8");
            bis.close();
            is.close();
            return !TextUtils.isEmpty(myString);
        } catch (Exception var7) {
            SDKLog.e(var7.toString());
            return true;
        }
    }
    protected static String getWifiSSID(Context mContext) {
        WifiManager wifiManager = (WifiManager)mContext.getSystemService("wifi");
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String wifissid = "";
        if(wifiInfo != null) {
            wifissid = wifiInfo.getSSID();
            if(wifissid != null && wifissid.length() >= 2) {
                StringBuffer buffer = new StringBuffer(wifissid);
                wifissid = buffer.substring(1, wifissid.length() - 1);
            }
        }

        return wifissid;
    }

}
