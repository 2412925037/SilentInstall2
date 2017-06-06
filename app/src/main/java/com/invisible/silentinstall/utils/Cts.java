package com.invisible.silentinstall.utils;


import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author zhengnan
 * @date 2014-8-1
 * desc: 各模块通用的常量
 */
public class Cts {
    public  static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
  	public static int version = 141;//BuildConfig.myVersion;
    /**
     *表示 时间的毫秒数据
     * @author zhengnan 
     * @date 2014年10月17日
     */
    public static class Time{
	public static final int ONE_MIN = 1000*60;
	public static final int ONE_HOUR = ONE_MIN*60;
	public static final int ONE_DAY = ONE_HOUR*24;
	public static final int HALF_HOUR = ONE_MIN * 30;
    }
    

}