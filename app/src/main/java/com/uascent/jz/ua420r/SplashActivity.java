package com.uascent.jz.ua420r;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.uascent.jz.ua420r.DeviceModule.GosDeviceListActivity;
import com.uascent.jz.ua420r.DeviceModule.GosDeviceModuleBaseActivity;
import com.uascent.jz.ua420r.DeviceModule.GosMainActivity;
import com.uascent.jz.ua420r.UserModule.GosUserLoginActivity;

public class SplashActivity extends GosDeviceModuleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                autoLogin();
                //startActivity(new Intent(SplashActivity.this, GosUserLoginActivity.class));
                //finish();
            }
        }, 2000);
    }


    private void autoLogin() {

        if (TextUtils.isEmpty(spf.getString("UserName", "")) || TextUtils.isEmpty(spf.getString("PassWord", ""))) {
            startActivity(new Intent(SplashActivity.this, GosUserLoginActivity.class));
            GosDeviceListActivity.sAutoLogin = false;
            finish();
            return;
        }

        Intent intent = new Intent(SplashActivity.this, GosMainActivity.class);
        startActivity(intent);
        GosDeviceListActivity.sAutoLogin = true;
        finish();
        //TODO:自动登录
        //baseHandler.sendEmptyMessageDelayed(handler_key.AUTO_LOGIN.ordinal(), 1000);
    }
}
