package com.invisible.silentinstall.core;

import android.content.Context;

import com.invisible.silentinstall.download.DownloadController;
import com.invisible.silentinstall.download.DownloadEventConstants;
import com.invisible.silentinstall.download.DownloadEventController;
import com.invisible.silentinstall.download.DownloadItem;
import com.invisible.silentinstall.download.DownloadRunnable;
import com.invisible.silentinstall.utils.Util_Log;


/**
 * 改写
 * 1，不使用新线程执行--在我们当前线程去执行
 * 2，不进行通知弹出
 * 3，taskModel的特殊关联
 * @author zhengnan
 * @date 2015年7月20日
 */
public class SIDownloadItem extends DownloadItem {
    public SIDownloadItem(Context context, String url) {
	super(context, url);
    }
    private TaskModel taskModel = null;
    
    /**
	 * Trigger a start download event.
	 */
	public void onStart()
	{
	    	Util_Log.log("download onStart!");
		DownloadEventController.getInstance().fireDownloadEvent(
		        DownloadEventConstants.EVT_ON_START, this);
	}
	
	public void startDownload()
	{
	    	//重写后在原线程执行,防止service死亡后被回收
		if (mRunnable != null)
		{
			mRunnable.abort();
		}
		mRunnable = new DownloadRunnable(this);
		mRunnable.run();
		//new Thread(mRunnable).start();
	}
	/**
	 * Set this item is download finished state. Trigger a finished download
	 * event.
	 */
	public void onFinished()
	{
	    	Util_Log.log("download onFinished!");
		mProgress = 100;
		mRunnable = null;
		mIsFinished = true;
//		updateNotificationOnEnd();
		DownloadController.getInstance().getDownloadList().remove(this);
		DownloadEventController.getInstance().fireDownloadEvent(
		        DownloadEventConstants.EVT_ON_FINISHED, this);
	}

	public void onFailed()
	{
	    	Util_Log.log("download failed!");
		mProgress = 100;
		mRunnable = null;
		mIsFinished = true;
//		updateNotificationOnFailed();
		DownloadController.getInstance().getDownloadList().remove(this);
		DownloadEventController.getInstance().fireDownloadEvent(
		        DownloadEventConstants.EVT_ON_FAILED, this);
	}

	/**
	 * Set the current progress. Trigger a progress download event.
	 * 
	 * @param progress
	 *            The current progress.
	 */
	public void onProgress(int progress)
	{
		mProgress = progress;
		/** 显示下载百分比 */
		//updateProgress(progress);
		Util_Log.logSI("progress:"+progress);
		DownloadEventController.getInstance().fireDownloadEvent(
		        DownloadEventConstants.EVT_ON_PROGRESS, this);
	}

	public TaskModel getTaskModel() {
	    return taskModel;
	}

	public void setTaskModel(TaskModel taskModel) {
	    this.taskModel = taskModel;
	}
}