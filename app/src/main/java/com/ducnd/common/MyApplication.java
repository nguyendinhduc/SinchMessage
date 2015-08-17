package com.ducnd.common;

import com.parse.Parse;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.enableLocalDatastore(this);
		Parse.initialize(this, CommonVL.APPLICATION_ID_PARSE, CommonVL.CLIENT_KEY_PARSE);
	}
}
