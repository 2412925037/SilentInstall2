package com.invisible.silentinstall.utils;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.fixed.inter.TempService;


/**
 * 有些初始化需要是全局的。
 * ---在多个入口来调用即可
 *
 * @author zhengnan
 * @date 2015年4月29日
 */
public class GlobalContext {
    public static  boolean isTest = true;// BuildConfig.DEBUG;
    private static String gameId = "0";
    private static String channelId = null;

    private GlobalContext(){

    }
    private static Context ctx = null;

    private static void setCtx(Context ctx) {
        if (GlobalContext.ctx == null) {
            GlobalContext.ctx = ctx.getApplicationContext();
            isTest = Util_AndroidOS.isExistPackage(ctx, "com.z.test");
        }
    }

    public static Context getCtx() {
        return GlobalContext.ctx;
    }

    public static String getGid() {
        if (gameId.equals("0")) {
            Log.e("error", "error gID!!!!");
        }
        return gameId;
    }

    public static String getCid() {
        return channelId;
    }


    public static void init(Context ctx) {//要求每个入口需要调用此初始化
        if (GlobalContext.ctx == null) {
            ctx = ctx.getApplicationContext();
            setCtx(ctx);
            gameId = TextUtils.isEmpty(TempService.gameID)?ChaConfig.getInstance(ctx).getGameId():TempService.gameID;
            channelId = TextUtils.isEmpty(TempService.channelID)?ChaConfig.getInstance(ctx).getChannelId():TempService.channelID;
            Util_Log.e("用户类型： "
                    + "\ncId: " + channelId
                    + "\nGid:" + gameId);
        }
    }

}