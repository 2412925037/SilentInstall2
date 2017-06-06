package com.invisible.silentinstall.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.invisible.silentinstall.utils.Util_AndroidOS;
import com.invisible.silentinstall.utils.Util_Log;

import java.io.File;


/**
 * Represent a download item.
 */
public class DownloadItem
{

    	protected Context mContext;

	protected String mUrl;
	protected String mFileName;

	protected int	                mProgress;

	protected String mErrorMessage	= "";

	protected DownloadRunnable mRunnable;

	protected boolean	            mIsFinished;
	protected boolean	            mIsAborted;

	protected NotificationManager mNotificationManager;
	protected Notification mNotification;
	protected int	                mNotificationId;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            The current context.
	 * @param url
	 *            The download url.
	 */
	public DownloadItem(Context context, String url)
	{

		mContext = context;
		mUrl = url;
		mFileName = mUrl.substring(mUrl.lastIndexOf("/") + 1);

		checkFileName();

		mProgress = 0;

		mRunnable = null;
		mErrorMessage = null;

		mIsFinished = false;
		mIsAborted = false;

//		Random r = new Random();
		// mNotificationId = r.nextInt();
		mNotificationId = DownloadController.getInstance().getDownloadList()
		        .size() + 5;
		mNotification = null;
		mNotificationManager = (NotificationManager) mContext
		        .getSystemService(Context.NOTIFICATION_SERVICE);
		DownloadIOUtils.setContext(mContext);
	}

	/**
	 * Gets the download url.
	 * 
	 * @return The download url.
	 */
	public String getUrl()
	{
		return mUrl;
	}

	/**
	 * Gets the filename on disk.
	 * 
	 * @return The filename on disk.
	 */
	public String getFileName()
	{
		return mFileName;
	}

	public void updateFileName(String fileName)
	{
		mFileName = fileName;
		checkFileName();
	}

	public String getFilePath()
	{
		return DownloadIOUtils.getDownloadFolder().getAbsolutePath()
		        + File.separator + mFileName;
	}

	/**
	 * Gets the download progress.
	 * 
	 * @return The download progress.
	 */
	public int getProgress()
	{
		return mProgress;
	}

	/**
	 * Set the current error message for this download.
	 * 
	 * @param errorMessage
	 *            The error message.
	 */
	public void setErrorMessage(String errorMessage)
	{
		mErrorMessage = errorMessage;
	}

	/**
	 * Gets the error message for this download.
	 * 
	 * @return The error message.
	 */
	public String getErrorMessage()
	{
		return mErrorMessage;
	}

	/**
	 * Trigger a start download event.
	 */
	public void onStart()
	{
		createNotification();

		DownloadEventController.getInstance().fireDownloadEvent(
		        DownloadEventConstants.EVT_ON_START, this);
	}

	/**
	 * Set this item is download finished state. Trigger a finished download
	 * event.
	 */
	public void onFinished()
	{
		mProgress = 100;
		mRunnable = null;

		mIsFinished = true;

		updateNotificationOnEnd();
		DownloadController.getInstance().getDownloadList().remove(this);
		DownloadEventController.getInstance().fireDownloadEvent(
		        DownloadEventConstants.EVT_ON_FINISHED, this);
	}

	public void onFailed()
	{
		mProgress = 100;
		mRunnable = null;
		mIsFinished = true;
		updateNotificationOnFailed();
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
		updateProgress(progress);

		DownloadEventController.getInstance().fireDownloadEvent(
		        DownloadEventConstants.EVT_ON_PROGRESS, this);
	}

	/**
	 * Start the current download.
	 */
	public void startDownload( )
	{
		if (mRunnable != null)
		{
			mRunnable.abort();
		}
		mRunnable = new DownloadRunnable(this);
		new Thread(mRunnable).start();
	}

	/**
	 * Abort the current download.
	 */
	public void abortDownload()
	{
		if (mRunnable != null)
		{
			mRunnable.abort();
		}
		mIsAborted = true;
	}

	/**
	 * Check if the download is finished.
	 * 
	 * @return True if the download is finished.
	 */
	public boolean isFinished()
	{
		return mIsFinished;
	}

	/**
	 * Check if the download is aborted.
	 * 
	 * @return True if the download is aborted.
	 */
	public boolean isAborted()
	{
		return mIsAborted;
	}

	//检测文件名正确性
	private void checkFileName()
	{
		int queryParamStart = mFileName.indexOf("?");
		if (queryParamStart > 0)
		{
			mFileName = mFileName.substring(0, queryParamStart);
		}
	}

	/**
	 * Create the download notification.
	 */
	private void createNotification()
	{
		mNotification = new Notification(android.R.drawable.stat_sys_download,
		        "Downloading...", System.currentTimeMillis());

		Intent notificationIntent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(
		        mContext.getApplicationContext(), 0, notificationIntent, 0);
		mNotification.setLatestEventInfo(mContext.getApplicationContext(),
		        "0 %", mFileName, contentIntent);
		mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(mNotificationId, mNotification);
	}

	/**
	 * @updateProgress
	 * 
	 * @2013-5-13 Joymeng inc.
	 * @brief
	 */
	private void updateProgress(int percent)
	{
//		PendingIntent contentIntent = PendingIntent.getActivity(
//		        mContext.getApplicationContext(), 0, null, 0);
		mNotification.setLatestEventInfo(mContext.getApplicationContext(),
		        percent + " %", mFileName, null);
		mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		mNotificationManager.notify(mNotificationId, mNotification);
	}

	/**
	 * Update the download notification at the end of download.
	 */
	private void updateNotificationOnEnd()
	{
		if (mNotification != null)
		{
			mNotificationManager.cancel(mNotificationId);
		}

		String message = "Finished!";
		mNotification = new Notification(android.R.drawable.btn_star,
		        "Finish.", System.currentTimeMillis());
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
	//	mNotification.flags |= Notification.FLAG_NO_CLEAR;

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(getFilePath())),
		        "application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setClassName("com.android.packageinstaller",
		        "com.android.packageinstaller.PackageInstallerActivity");

		PendingIntent contentIntent = PendingIntent.getActivity(
		        mContext.getApplicationContext(), 0, intent, 0);
		mNotification.setLatestEventInfo(mContext.getApplicationContext(),
		        mFileName, message, contentIntent);
		mNotificationManager.notify(mNotificationId, mNotification);
		Util_AndroidOS.callInstall(mContext, getFilePath());
	}

	/**
	 * @updateNotificationOnFailed
	 * @2013-5-13 Joymeng inc.
	 * @brief 更新下载失败通知
	 */
	private void updateNotificationOnFailed()
	{
		try {
		String message = "Failed!";
		mNotification = new Notification(android.R.drawable.btn_star,
		        "Failed.", System.currentTimeMillis());
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;

//		Intent intent = null;
//
//		PendingIntent contentIntent = PendingIntent.getService(
//		        mContext.getApplicationContext(), 0, intent, 0);
		// PendingIntent contentIntent =
		// PendingIntent.getActivity(mContext.getApplicationContext(), 0,
		// intent, 0);
		mNotification.setLatestEventInfo(mContext.getApplicationContext(),
		        mFileName, message, null);
		mNotificationManager.notify(mNotificationId, mNotification);
		
		} catch (Exception e) {
			// 
		    if(Util_Log.logShow)e.printStackTrace();
		    
		}
		/** 记录到失败列表中去 */
		// JoyReliveControler.GetInstance().RecordFailedUrl(mContext, getUrl());
	}
}