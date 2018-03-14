package com.wechat.tiaotiao;

import android.content.Context;
import android.util.Log;



public class DensityUtil {
	static  String LINE;
	private static String TAG = "zyzx";
	static final String LINE_CHAR="=";
	static final String BOARD_CHAR="|";
	static final int LENGTH = 80;
	// 当测试阶段时true
	private static final boolean isShow = true;


	/** dip转换px */
	public static int dip2px(Context context,int dip) {
		final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}

	/** pxz转换dip */
	public static int px2dip(Context context,int px) {
		final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

	public static void showTestLog(String TAG, String str) {
		if (isShow) {
			Log.i(TAG, str);
		}
	}
	public static void showTestLog(String str) {
		showTestLog(TAG,str);
	}

}
