package com.invisible.silentinstall.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller implementation.
 */
public final class DownloadController {
	
	private SharedPreferences mPreferences;
	private List<DownloadItem> mDownloadList;
	private List<String> mAdBlockWhiteList = null;
	private List<String> mMobileViewUrlList = null;
	
	/**
	 * Holder for singleton implementation.
	 */
	private static final class ControllerHolder {
		private static final DownloadController INSTANCE = new DownloadController();
		/**
		 * Private Constructor.
		 */
		private ControllerHolder() { }
	}
	
	/**
	 * Get the unique instance of the Controller.
	 * @return The instance of the Controller
	 */
	public static DownloadController getInstance() {
		return ControllerHolder.INSTANCE;
	}
	
	/**
	 * Private Constructor.
	 */
	private DownloadController() {
		mDownloadList = new ArrayList<DownloadItem>();
	}

	/**
	 * Get a SharedPreferences instance.
	 * @return The SharedPreferences instance.
	 */
	public SharedPreferences getPreferences() {
		return mPreferences;
	}

	/**
	 * Set the SharedPreferences instance.
	 * @param preferences The SharedPreferences instance.
	 */
	public void setPreferences(SharedPreferences preferences) {
		this.mPreferences = preferences;
	}
	
	/**
	 * Get the current download list.
	 * @return The current download list.
	 */
	public List<DownloadItem> getDownloadList() {
		return mDownloadList;
	}
	
	/**
	 * Add an item to the download list.
	 * @param item The new item.
	 */
	public void addToDownload(DownloadItem item) {
		mDownloadList.add(item);
	}
	
	public synchronized void clearCompletedDownloads() {
		List<DownloadItem> newList = new ArrayList<DownloadItem>();
		
		for (DownloadItem item : mDownloadList) {
			if (!item.isFinished()) {
				newList.add(item);
			}
		}
		
		mDownloadList.clear();
		mDownloadList = newList;
	}
	
	/**
	 * Reset the mobile view url list, so that it will be reloaded.
	 */
	public void resetMobileViewUrlList() {
		mMobileViewUrlList = null;
	}
	
	


	public  boolean checkCardState(Context context, boolean showMessage) {
		// Check to see if we have an SDCard
      String status = Environment.getExternalStorageState();
      if (!status.equals(Environment.MEDIA_MOUNTED)) {

          return false;
      }
      
      return true;
	}
	
	public void doDownloadStart(Context context , String url){
	    DownloadItem item = new DownloadItem(context, url);
	    doDownloadStart(item);
	}
	
	/**
	 * @doDownloadStart
	 * @2013-5-13  Joymeng inc.
	 * @brief
	 */
	  public void doDownloadStart(DownloadItem Ditem) 
	  {
		
	      //有sdcard才下载
	      if (checkCardState(Ditem.mContext, true)) 
	      {
	    	boolean isContinue = true;
	    	//如果目标任务已在任务列表，则不执行
	    	for (DownloadItem item : DownloadController.getInstance().getDownloadList())
	        {
	    		if (item.getUrl().equals(Ditem.mUrl))
	            {
		            isContinue = false;
		            break;
	            }
	        }

	    	if (isContinue)
	        {	
	          	DownloadController.getInstance().addToDownload(Ditem);
	          	Ditem.startDownload();
	        }
	      }
	  }
	
}