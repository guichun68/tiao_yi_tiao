package com.wechat.tiaotiao;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FloatWindowService extends Service {

	public static BigViewOnclickListener mJumpListener;
	private final String TAG = FloatWindowService.class.getSimpleName();
	private int mScreenWidth,mScreenHeight;
	public static long[] mHits = new long[2];
	public static boolean isCMDJump;

	private MyBinder mMyBinder;
	public class MyBinder extends Binder
	{
		public FloatWindowService getServiceInstance()
		{
			return FloatWindowService.this;
		}
	}

	/**
	 * 用于在线程中创建或移除悬浮窗。
	 */
	private Handler handler = new Handler();

	/**
	 * 定时器，定时进行检测当前应该创建还是移除悬浮窗。
	 */
	private Timer timer;
	private boolean runningTask = true;

	@Override
	public IBinder onBind(Intent intent) {
		if(mMyBinder == null){
			mMyBinder = new MyBinder();
		}
		return mMyBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 开启定时器，每隔0.5秒刷新一次
		if (timer == null) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new RefreshTask(), 0, 500);
		}


		/*new Thread(){
			@Override
			public void run() {
				super.run();
				while (runningTask){
					try {
						Thread.sleep(500);
						// 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
						if (*//*isHome() &&*//* !MyWindowManager.isWindowShowing()) {
							handler.post(new Runnable() {
								@Override
								public void run() {
									MyWindowManager.createSmallWindow(getApplicationContext());
								}
							});
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();*/
//		startThread();
		initDataBase();
		initListener();
	}

	public void testThread(){
		new Thread(){
			@Override
			public void run() {
				Log.e(TAG,"Thread is running!!");
			}
		}.start();
	}

	public void startThread(){
		new Thread(){
			@Override
			public void run() {
				super.run();
				while (runningTask){
					try {
						Thread.sleep(500);
						// 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
						if (/*isHome() &&*/ !MyWindowManager.isWindowShowing()) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								MyWindowManager.createSmallWindow(getApplicationContext());
							}
						});
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}.start();
	}

	public void sayHello(){
		Toast.makeText(this,"HElolo",Toast.LENGTH_SHORT).show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}

	private void initListener(){
		mJumpListener = new BigViewOnclickListener() {
			@Override
			public void onJumpClicked(double distance) {
				float spendTime = getSpendTime(distance);
				if(spendTime == 0.0f){
					Toast.makeText(FloatWindowService.this,"未能匹配到合适的按键时间，距离："+distance,Toast.LENGTH_SHORT).show();
					return;
				}
				isCMDJump = true;
				int realSpendTime = (int) spendTime;
				final String cmd = "input swipe "+mScreenWidth/2+" "+mScreenHeight/2+" "+mScreenWidth/2+" "+mScreenHeight/2+" "+realSpendTime;
//				final String cmd = "input tap "+mScreenWidth/2+" "+mScreenHeight/2;
				Log.e(TAG+"len->cmd","distance:"+distance+" cmd:"+cmd);

				// 点击jump的时候，移除大悬浮窗，创建小悬浮窗
				MyWindowManager.removeBigWindow(FloatWindowService.this);
				MyWindowManager.createSmallWindow(FloatWindowService.this);
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						execShellCmd(cmd);
						//线程睡眠10s
					}
				}).start();
			}

			@Override
			public void onDelayJumpClicked(double distance) {
				MyWindowManager.removeBigWindow(FloatWindowService.this);
				MyWindowManager.createSmallWindow(FloatWindowService.this);

				float spendTime = getSpendTime(distance);
				if(spendTime == 0.0f){
					Toast.makeText(FloatWindowService.this,"未能匹配到合适的按键时间，距离："+distance,Toast.LENGTH_SHORT).show();
					return;
				}
				final String cmd = "input swipe "+mScreenWidth/2+" "+mScreenHeight/2+" "+mScreenWidth/2+" "+mScreenHeight/2+" "+(spendTime+3000);
				Log.e(TAG+"->cmd:",cmd);
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						execShellCmd(cmd);
						//线程睡眠10s
					}
				}).start();
			}
		};
	}

	/**
	 * 匹配指定跳跃距离 最佳的按下时间（ms)
	 * @param distance
	 * @return
	 */
	private float getSpendTime(double distance){
		float tempFloorValue=500f,tempMaxValue=500f;
		float result;
		//从二维数组中查找 与distance最接近的key ，取其value
		for(int i=0;i<mJumpTimes.length;i++){
			int keyDistance = mJumpTimes[i][0];
			int valueTime = mJumpTimes[i][1];
			tempMaxValue = valueTime;
			int temp = (int) distance;
			if(temp == keyDistance){
//                return mJumpTimes[i][1];
				Log.e(TAG, mJumpTimes[i][1]+"");
				return mJumpTimes[i][1];
			}
			if(distance<keyDistance){
				if(i==0){
					Log.e(TAG, mJumpTimes[i][1]+"");
					return valueTime;
				}
				result = (tempFloorValue + tempMaxValue)/2.0f;
				Log.e(TAG,result+"");
				return result;
			}
			if(distance>keyDistance){
				tempFloorValue = valueTime;
			}
			if(i==mJumpTimes.length-1){
				System.out.println(mJumpTimes[i][1]);
				Log.e(TAG,mJumpTimes[i][1]+"");
				return mJumpTimes[i][1];
			}
		}
		Log.e(TAG,"距离："+distance+"未能匹配到相关耗时");
		return 0.0f;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Service被终止的同时也停止定时器继续运行
		timer.cancel();
		timer = null;
		mJumpListener = null;
		runningTask = false;
	}

	class RefreshTask extends TimerTask {

		@Override
		public void run() {
			// 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
			if (/*isHome() &&*/ !MyWindowManager.isWindowShowing()) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						MyWindowManager.createSmallWindow(getApplicationContext());
					}
				});
			}
			// 当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗。
			/*else if (!isHome() && MyWindowManager.isWindowShowing()) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						MyWindowManager.removeSmallWindow(getApplicationContext());
						MyWindowManager.removeBigWindow(getApplicationContext());
					}
				});
			}*/
			// 当前界面是桌面，且有悬浮窗显示，则更新内存数据。
			/*else if (isHome() && MyWindowManager.isWindowShowing()) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						MyWindowManager.updateUsedPercent(getApplicationContext());
					}
				});
			}*/
		}

	}

	/**
	 * 判断当前界面是否是桌面
	 */
	private boolean isHome() {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		return getHomes().contains(rti.get(0).topActivity.getPackageName());
	}

	/**
	 * 获得属于桌面的应用的应用包名称
	 * 
	 * @return 返回包含所有包名的字符串列表
	 */
	private List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo) {
			names.add(ri.activityInfo.packageName);
		}
		return names;
	}

	public boolean needClick = true;

	public void clickScreenNew(int windowWidth,int windowHeight){
		//input swipe 500 500 500 500 300  //在500,500处长按300ms

		//生成点击坐标
		int x = (int) (windowWidth * 0.5 );
		int y = (int) (windowHeight * 0.5);
		final String cmd = "input tap "+x+" "+y;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				execShellCmd(cmd);
				//线程睡眠10s
			}
		}).start();

	}

	public void clickScreen(final int windowWidth, final int windowHeight){

		//每10s产生一次点击事件，点击的点坐标为(0.2W - 0.8W,0.2H - 0.8 H),W/H为手机分辨率的宽高.
		new Thread(new Runnable() {
			@Override
			public void run() {

				while (needClick) {
					//生成点击坐标
					int x = (int) (windowWidth * 0.5 );
					int y = (int) (windowHeight * 0.5);
					//利用ProcessBuilder执行shell命令
					String[] order = {
							"input",
							"tap",
							"" + x,
							"" + y
					};
					try {
						new ProcessBuilder(order).start();
					} catch (IOException e) {
						e.printStackTrace();
					}
					//线程睡眠10s
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * 执行shell命令
	 *
	 * @param cmd
	 */
	private void execShellCmd(String cmd) {

		try {
			Log.e(TAG+"CMD",cmd);
			// 申请获取root权限，这一步很重要，不然会没有作用
//			Process process = Runtime.getRuntime().exec("sh");
			Process process = Runtime.getRuntime().exec("su");
			// 获取输出流
			OutputStream outputStream = process.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(
					outputStream);
			dataOutputStream.writeBytes(cmd);
			dataOutputStream.flush();
			dataOutputStream.close();
			outputStream.close();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					MyWindowManager.removeSmallWindow(FloatWindowService.this);
					MyWindowManager.createBigWindow(FloatWindowService.this);
				}
			},1000);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		isCMDJump = false;
	}

	public boolean isNeedClick() {
		return needClick;
	}

	public void setNeedClick(boolean needClick) {
		this.needClick = needClick;
	}

	int[][] mJumpTimes;
	private void initDataBase(){

		/*mJumpTimes= new int[][]{
				{260,340},//待定

				{280,380},//

				{340,535},
				{360,580},//
				{400,620},
				{430,630},
				{450,650},

				{470,670},//

				{500,705},
				{510,715},
				{545,755},
				{555,760},
				{570,780},
				{580,790},

				{600,830},//
				{620,840},
				{650,850},
				{670,880},
		};*/
		//第一维度：跳跃距离（px); 第二维度：对应的耗时（ms)
		mJumpTimes= new int[][]{
				{74,100},
				{113,230},
				{124,252},
				{140,260},
				{141,261},
				{143,261},
				{154,265},
				{156,265},
				{162,280},
				{164,284},
				{170,284},
				{173,284},
				{182,290},
				{184,295},
				{195,310},
				{210,310},
				{212,310},
				{231,325},
				{234,330},
				{253,340},
				{260,340},
				{266,354},
				{270,354},
				{274,358},
				{275,358},
				{277,367},
				{280,380},
				{282,393},
				{286,413},
				{288,450},
				{289,463},
				{290,463},
				{291,463},
				{292,463},
				{295,463},
				{296,463},
				{297,468},
				{298,468},
				{300,468},
				{301,468},
				{302,468},
				{305,468},
				{306,468},
				{307,468},
				{313,468},
				{312,468},
				{315,468},
				{319,468},
				{320,472},
				{322,480},
				{323,480},
				{324,483},
				{326,483},
				{327,486},
				{328,486},
				{332,493},
				{333,493},
				{337,510},
				{338,512},
				{339,514},
				{340,514},
				{341,514},
				{343,514},
				{344,514},
				{345,514},
				{347,516},
				{348,516},
				{351,525},
				{352,530},
				{354,534},
				{355,534},
				{356,542},
				{357,542},
				{358,543},
				{359,543},
				{360,545},
				{361,545},
				{363,545},
				{364,547},
				{365,547},
				{366,547},
				{367,547},
				{368,547},
				{369,547},
				{370,550},
				{371,550},
				{372,555},
				{374,555},
				{376,565},
				{378,569},
				{381,569},
				{382,569},
				{386,569},
				{387,569},
				{388,577},
				{389,577},
				{393,577},
				{397,580},
				{398,590},
				{399,593},
				{400,593},
				{401,593},
				{402,593},
				{405,602},
				{406,602},
				{407,603},
				{409,603},
				{410,603},
				{411,606},
				{414,606},
				{416,610},
				{418,610},
				{419,610},
				{422,610},
				{424,610},
				{425,610},
				{427,612},
				{430,626},
				{434,628},
				{436,628},
				{437,628},
				{438,633},
				{446,633},
				{449,639},
				{450,650},
				{454,655},
				{467,657},
				{469,657},//
				{470,666},
				{472,666},
				{484,671},
				{487,671},
				{497,675},
				{498,680},
				{500,705},
				{502,705},
				{508,705},
				{510,714},
				{514,714},
				{516,720},
				{520,725},
				{522,725},
				{523,730},
				{526,730},
				{527,733},
				{528,733},
				{529,740},
				{539,740},
				{545,750},
				{546,755},
				{555,760},
				{556,760},
				{570,780},
				{576,784},
				{578,784},
				{580,790},
				{581,791},
				{584,800},
				{595,808},
				{600,825},
				{617,832},
				{620,840},
				{631,851},
				{634,852},
				{640,852},
				{643,854},
				{646,858},
				{647,858},
				{649,861},
				{650,861},
				{652,866},
				{663,869},
				{660,869},
				{667,870},
				{670,880},
				{747,899}
		};
	}

	public int getmScreenWidth() {
		return mScreenWidth;
	}

	public void setmScreenWidth(int mScreenWidth) {
		this.mScreenWidth = mScreenWidth;
	}

	public int getmScreenHeight() {
		return mScreenHeight;
	}

	public void setmScreenHeight(int mScreenHeight) {
		this.mScreenHeight = mScreenHeight;
	}
}
