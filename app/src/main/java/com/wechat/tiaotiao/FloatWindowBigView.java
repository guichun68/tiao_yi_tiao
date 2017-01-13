package com.wechat.tiaotiao;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FloatWindowBigView extends LinearLayout {

	/**
	 * 记录大悬浮窗的宽度
	 */
	public static int viewWidth;

	/**
	 * 记录大悬浮窗的高度
	 */
	public static int viewHeight;

	private double calcDistance(double startX, double startY, double endX, double endY){
		double x_= Math.pow((startX - endX),2);
		double y_ = Math.pow((startY-endY),2);
		return Math.sqrt(x_+y_);
	}

	public FloatWindowBigView(final Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.float_window_big, this);
		View view = findViewById(R.id.big_window_layout);
		final MyDrawView myDrawBoard = view.findViewById(R.id.mdv);
		view.findViewById(R.id.btn_content).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(context,"触发了自己",Toast.LENGTH_SHORT).show();
			}
		});
		Button btnClear = view.findViewById(R.id.btn_clear);
		final TextView tvDistance = view.findViewById(R.id.tv_distance);
		tvDistance.setText(String.valueOf(myDrawBoard.getDistance()));
		myDrawBoard.setTag(tvDistance.getText().toString());
		view.findViewById(R.id.btn_jump).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FloatWindowService.mJumpListener.onJumpClicked(myDrawBoard.getDistance());
			}
		});

		view.findViewById(R.id.btn_delay_jump).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(tvDistance == null || TextUtils.isEmpty(tvDistance.getText().toString()) || tvDistance.getText().toString().equals("0.0")){
					Toast.makeText(context,"未知距离",Toast.LENGTH_SHORT).show();
				}else{
					FloatWindowService.mJumpListener.onDelayJumpClicked(Float.valueOf(tvDistance.getText().toString()));
				}
			}
		});
		Button btnTrigger = view.findViewById(R.id.btn_trigger);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		Button close = (Button) findViewById(R.id.close);
		Button back = (Button) findViewById(R.id.back);


		btnTrigger.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				xc
			}
		});
		myDrawBoard.setOnDrawFinishListener(new MyDrawView.OnDrawLineFinish() {
			@Override
			public void onFinish(float startX, float startY, float endX, float endY) {
				//d=√【(x1-x2)^2+(y1-y2)^2】
				double x_= Math.pow((startX - endX),2);
				double y_ = Math.pow((startY-endY),2);
				double result = Math.sqrt(x_+y_);
				tvDistance.setText(String.valueOf(result));
			}
		});

		btnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//清除内容
				myDrawBoard.reset();
			}
		});
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击关闭悬浮窗的时候，移除所有悬浮窗，并停止Service
				MyWindowManager.removeBigWindow(context);
				MyWindowManager.removeSmallWindow(context);
				Intent intent = new Intent(getContext(), FloatWindowService.class);
				context.stopService(intent);
			}
		});
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击返回的时候，移除大悬浮窗，创建小悬浮窗
				MyWindowManager.removeBigWindow(context);
				MyWindowManager.createSmallWindow(context);
			}
		});
	}

}
