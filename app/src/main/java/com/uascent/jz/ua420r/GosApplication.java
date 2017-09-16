package com.uascent.jz.ua420r;

import android.app.Application;

import com.uascent.jz.ua420r.utils.SpHelper;

public class GosApplication extends Application {

	public static int flag = 0;

	@Override
	public void onCreate(){
		super.onCreate();
		initErrorHandler();

		SpHelper.initSP(this);
	}


	private void initErrorHandler(){
		CrashHandler handler = CrashHandler.getInstance();
		handler.init(this);
	}


}
