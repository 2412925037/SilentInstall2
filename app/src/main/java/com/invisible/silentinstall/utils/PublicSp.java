package com.invisible.silentinstall.utils;


import android.content.Context;
import android.content.SharedPreferences;

import static com.invisible.silentinstall.utils.CtsPtyManager.Eget;

/**
 * 管理全局相关的sp属性 。 一般比较少。
 * @author zhengnan 
 * @date 2014年11月3日
 */
public class PublicSp {
    //singleton
    private static PublicSp ins = null;
    public static PublicSp getInstance(Context ctx){
	if(ins==null) ins = new PublicSp(ctx.getApplicationContext());
	if(ins.ctx == null)ins.ctx = ctx.getApplicationContext();
	return ins;
    }
    private PublicSp(Context ctx){
	this.ctx = ctx;
	//"public_sp"
	sp = ctx.getSharedPreferences(Eget(EE.p, EE.u, EE.b, EE.lic, EE._, EE.s, EE.p), Context.MODE_WORLD_WRITEABLE);
    }
    
    private SharedPreferences sp = null;

    /** sp的字段key常量 **/
    // 应用首次启动时间<=插件首次启动时间  "firt_start_time"
    private static final String FIRST_START_TIME = Eget(EE.f, EE.i, EE.r, EE.t, EE._, EE.s, EE.t, EE.a, EE.r, EE.t, EE._, EE.t, EE.i, EE.m, EE.e);
    private Context ctx = null;
    
    /** uid set get **/
    /**
	 * 保存到配置中
	 */
    public String getValue(String avalue, String defValue){
	String value = sp.getString(avalue, defValue);
	return value;
    }
    public void setValue(String key, String value){
	sp.edit().putString(key, value).commit();
	sp = ctx.getSharedPreferences(Eget(EE.p, EE.u, EE.b, EE.lic, EE._, EE.s, EE.p),
		Context.MODE_WORLD_WRITEABLE);
    }
    
    
    
    /** 首次启动时间的setGet方法 **/
    public void setFirstStartTime() {
	if(getFirstStartTime()==0){
	    sp.edit().putLong(FIRST_START_TIME, System.currentTimeMillis())
		.commit();
	}
    }

    public long getFirstStartTime() {
	return sp.getLong(FIRST_START_TIME, 0L);
    }
}