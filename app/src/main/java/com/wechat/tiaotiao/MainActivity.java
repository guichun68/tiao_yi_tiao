package com.wechat.tiaotiao;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
	private int screenHeight,screenWidth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button startFloatWindow = (Button) findViewById(R.id.start_float_window);

		screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindowManager().getDefaultDisplay().getHeight();

		findViewById(R.id.btn_click).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.clickScreenNew(screenWidth,screenHeight);
			}
		});

		findViewById(R.id.btn_test).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.sayHello();
			}
		});

		findViewById(R.id.btn_stop).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.setNeedClick(false);
				Toast.makeText(MainActivity.this, "已停止点击", Toast.LENGTH_SHORT).show();
			}
		});
		startFloatWindow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent in = new Intent(MainActivity.this, FloatWindowService.class);
//				startService(intent);
				//为了拿到中间人对象
				bindService(in, serviceConnection, BIND_AUTO_CREATE);
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.addCategory(Intent.CATEGORY_HOME);
				startActivity(intent);
			}
		});
	}
	ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			FloatWindowService.MyBinder binder = (FloatWindowService.MyBinder) service;
			if(mService ==null){
				mService = (FloatWindowService) binder.getServiceInstance();
			}
			if(mService.getmScreenHeight()!=0 && mService.getmScreenWidth()!=0){
				return;
			}else{
				mService.setmScreenHeight(screenHeight);
				mService.setmScreenWidth(screenWidth);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(MainActivity.this,"服务已断开",Toast.LENGTH_SHORT).show();
		}
	};
	private FloatWindowService mService;
/*	class MusicServiceConn implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			Toast.makeText(MainActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
			mService = (IFloatWinInterface) service;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Toast.makeText(MainActivity.this, "绑定失败", Toast.LENGTH_SHORT).show();
		}

	}*/
}
