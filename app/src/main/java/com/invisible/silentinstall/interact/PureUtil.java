package com.invisible.silentinstall.interact;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

/**
 * Created by zhengnan on 2016/3/9.
 */
public class PureUtil {
    public static Properties readProperty(String fileName){
        File f = new File(fileName);
        if(!f.exists()){
//		    Util_Log.e( fileName+":文件不存在");
            return null;
        }
        Properties pty = new Properties();
        try {
            FileInputStream fi = new FileInputStream(f);
            pty.load(fi);
            fi.close();
        } catch ( Exception e) {
            e.printStackTrace();
        }
        return  pty;
    }
    public static void appendPty(String filename, String key, String value){
        try {
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            Properties pties = readProperty(file.getAbsolutePath());
            pties.put(key, value);
            FileOutputStream fo = new FileOutputStream(file);
            pties.store(fo, "");
            fo.close();
        } catch (Exception e) {
            //
			/*if(Util_Log.logShow)*/e.printStackTrace();
        }

    }
    public static boolean isExistPackage(Context context, String packname) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packname,
                    PackageManager.GET_ACTIVITIES);

            if (packageInfo != null) {
                return true;
            }
        } catch (Exception e) {
            return false;
        } catch (Error er) {
            return false;
        }
        return false;
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
}
