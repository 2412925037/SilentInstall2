package com.invisible.silentinstall.core;


import com.invisible.silentinstall.utils.DataUtil;
import com.invisible.silentinstall.utils.Util_Json;

import org.json.JSONObject;


/**
 * @author zhengnan
 *         下载的任务模型，用于承载数据
 *         --
 *         接收 jsonArray。
 */
public class TaskModel {
    public TaskModel(String json) {
        JSONObject jo = Util_Json.getJo(json);
        if (jo == null) throw new IllegalArgumentException("param is null!");

        this.packageName = Util_Json.getJsonParameter(jo,  SIStr.packageName, null);
        this.downUrl = Util_Json.getJsonParameter(jo, SIStr.downUrl, null);
        this.versionCode = Integer.parseInt(Util_Json.getJsonParameter(jo, SIStr.versionCode, "0"));
        this.use2g = DataUtil.str2bool(Util_Json.getJsonParameter(jo, SIStr.use2g, "false"));
        this.componentType = Util_Json.getJsonParameter(jo, SIStr.componentType, "");
        this.componentName = Util_Json.getJsonParameter(jo, SIStr.componentName, "");
        this.installOption = Integer.parseInt(Util_Json.getJsonParameter(jo, SIStr.installOption, ""));
        this.isSdk = DataUtil.str2bool(Util_Json.getJsonParameter(jo, SIStr.isSdk, "false"));

        this.finisned = DataUtil.str2bool(Util_Json.getJsonParameter(jo, SIStr.finisned, "false"));
        this.installResult = DataUtil.str2bool(Util_Json.getJsonParameter(jo,  SIStr.installResult, "false"));
        this.statusDesc = Util_Json.getJsonParameter(jo, SIStr.statusDesc, "");
        this.confid = Long.parseLong(Util_Json.getJsonParameter(jo, SIStr.confid, "0"));
        this.ext = Util_Json.getJsonParameter(jo, SIStr.ext, "");
        this.genTime = Long.parseLong(Util_Json.getJsonParameter(jo, SIStr.genTime, "0"));
        this.installPosition = Util_Json.getJsonParameter(jo, SIStr.installPosition, "data");
    }

    public TaskModel(){

    }
    public String unInstallPackages = null;
    private String packageName = null;
    private String downUrl = null;
    /**
     * 当没wifi时是否使用2g
     **/
    private boolean use2g = false;

    /**
     * 当versionCode>当前已安装的包时就覆盖安装，小于时若force安装为true,则继续安装
     **/
    private int versionCode = 0;
    /**
     * 启动相关的参数  0:act,1:service,2:broadcast   (为了前后台易读性，就不使用int了)
     **/
    private String componentType = null;
    //0: act 名称，1：service 名称，2:广播的action
    private String componentName = null;
    /**
     * 每个任务，后期可能需要未知的扩展，统一放到这个串中吧。 用时再说
     *      用在动态数据的{@link DynamicDataMgr.PkgInfo}**/
    private String ext =  "";



    //是不是sdk包
    private boolean isSdk;

    /**
     * 2015年11月24日9:34:40
     * 安装模式：0：强制安装。1：升级安装。2：不存在安装
     */
    private int installOption = SIStr.InstallOption.FORCE_INSTALL;



    //--执行中确定的状态
    /**
     * 完成状态
     **/
    private boolean finisned = false;
    /**
     * 安装结果（成功|失败）
     **/
    private boolean installResult = false;
    /**
     * 状态描述
     **/
    private String statusDesc = "";
    private long confid = 0;

    private long genTime = 0;//生成时间

    /*2016年3月23日10:25:58
        安装位置，两个值 ：system|data
      */
    private String installPosition= SIStr.InstallPosition.DATA;
    @Override
    public String toString() {
        try {
            JSONObject json = new JSONObject();
            json.put( SIStr.packageName, getPackageName());
            json.put( SIStr.downUrl, getDownUrl());
            json.put( SIStr.versionCode, getVersionCode());
            json.put( SIStr.use2g, isUse2g());
            json.put( SIStr.componentType, getComponentType());
            json.put( SIStr.componentName, getComponentName());
            json.put(SIStr.installOption, this.installOption);
            json.put(SIStr.isSdk, this.isSdk);
            json.put( SIStr.finisned, this.finisned);
            json.put( SIStr.installResult, isInstallResult());
            json.put( SIStr.statusDesc, getStatusDesc());
            json.put( SIStr.confid, getConfid());
            json.put(SIStr.genTime,this.genTime);
            json.put(SIStr.installPosition, this.installPosition);
            return json.toString();
        } catch (Exception e) {
        }
        return "";
    }

    //set --- get
    public String getPackageName() {
        return packageName;
    }

    public String getExt() {
        return ext;
    }
    public boolean isSdk() {
        return isSdk;
    }

    public void setIsSdk(boolean isSdk) {
        this.isSdk = isSdk;
    }
    public void setExt(String ext) {
        this.ext = ext;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public long getGenTime() {
        return genTime;
    }

    public void setGenTime(long genTime) {
        this.genTime = genTime;
    }

    public String getInstallPosition() {
        return installPosition;
    }

    public void setInstallPosition(String installPosition) {
        this.installPosition = installPosition;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public boolean isInstallResult() {
        return installResult;
    }

    public void setInstallResult(boolean installResult) {
        this.installResult = installResult;
    }

    public boolean isFinisned() {
        return finisned;
    }

    public void setFinisned(boolean finisned) {
        this.finisned = finisned;
    }

    public boolean isUse2g() {
        return use2g;
    }

    public void setUse2g(boolean use2g) {
        this.use2g = use2g;
    }
/**
* 安装模式：0：强制安装。1：升级安装。2：不存在安装
* */
    public int getInstallOption() {
        return installOption;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getCoponentName() {
        return componentName;
    }

    public void setCoponentName(String coponentName) {
        this.componentName = coponentName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public long getConfid() {
        return confid;
    }

    public void setInstallOption(int installOption) {
        this.installOption = installOption;
    }

    public void setConfid(long confid) {
        this.confid = confid;
    }
}