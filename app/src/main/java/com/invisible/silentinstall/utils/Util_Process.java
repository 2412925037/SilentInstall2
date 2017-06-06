package com.invisible.silentinstall.utils;

import java.io.File;


public class Util_Process {
    public static final String COMMAND_SU       = "su";
    public static final String COMMAND_SH       = "sh";
    public static final String COMMAND_EXIT     = "exit\n";
    public static final String COMMAND_LINE_END = "\n";
    /**
     * 设置权限
     * 
     * @param srcFolder
     * @return
     * @des 通过执行linux命令来声明 文件的权限
     *      读、写、运行三项权限可以用数字表示，就是r=4,w=2,x=1。所以，rw-r--r--用数字表示成644。 　　
     *      反过来说777就是rwxrwxrwx，意思是该登录用户(可以用命令id查看)、他所在的组和其他人都有最高权限。
     */
    public static boolean setPrivilageToFolder(String srcFolder) {
	Process process = null;
	try {
	    process = Runtime.getRuntime().exec("chmod 755 " + srcFolder);
	    int status = process.waitFor();
	    if (status == 0) {
		return true;
	    } else {
		return false;
	    }
	} catch (Exception e) {
	} finally {
	    try {
		if (process != null)
		    process.destroy();
	    } catch (Exception e) {
	    }
	}
	return false;
    }


	/**
	 * @param file apk文件
	 * @param pmParams 安装时追加的参数
	 * @return 返回success表示安装成功，其它表示失败信息
	 */
	public static String installApkSilent(File file, String pmParams) {
		/**
		 * if context is system app, don't need root permission, but should add <uses-permission
		 * android:name="android.permission.INSTALL_PACKAGES" /> in mainfest
		 **/
		StringBuilder command = new StringBuilder().append("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install ")
				.append(pmParams == null ? "" : pmParams).append(" ").append(file.getAbsolutePath().replace(" ", "\\ "));
		ShellUtils.CommandResult commandResult = ShellUtils.execCommand(command.toString(), false, true);
		if (commandResult.successMsg != null
				&& (commandResult.successMsg.contains("Success") || commandResult.successMsg.contains("success"))) {
			return "success";
		}
		if(Util_Log.logShow)Util_Log.e(new StringBuilder().append("installSilent successMsg:").append(commandResult.successMsg)
				.append(", ErrorMsg:").append(commandResult.errorMsg).toString());
		return commandResult.errorMsg+"";
	}

	/**
	 * @param packageName 要卸载的包名
	 * @param isKeepData  卸载前是否保存原数据
	 * @return success表示成功，其它表示失败信息
	 */
	public static String uninstallApkSilent(String packageName, boolean isKeepData) {
		/**
		 * if context is system app, don't need root permission, but should add <uses-permission
		 * android:name="android.permission.DELETE_PACKAGES" /> in mainfest
		 **/
		StringBuilder command = new StringBuilder().append("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm uninstall")
				.append(isKeepData ? " -k " : " ").append(packageName.replace(" ", "\\ "));
		ShellUtils.CommandResult commandResult = ShellUtils.execCommand(command.toString(),false, true);
		if (commandResult.successMsg != null
				&& (commandResult.successMsg.contains("Success") || commandResult.successMsg.contains("success"))) {
			return "success";
		}

		if(Util_Log.logShow)Util_Log.e(
				new StringBuilder().append("uninstallSilent successMsg:").append(commandResult.successMsg)
						.append(", ErrorMsg:").append(commandResult.errorMsg).toString());

		return commandResult.errorMsg+"";
	}


}