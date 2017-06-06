
package com.invisible.silentinstall.download;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.invisible.silentinstall.utils.GlobalContext;
import com.invisible.silentinstall.utils.Util_File;
import com.invisible.silentinstall.utils.Util_Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import static com.invisible.silentinstall.utils.CtsPtyManager.Eget;
import static com.invisible.silentinstall.utils.EE.$;
import static com.invisible.silentinstall.utils.EE.a;
import static com.invisible.silentinstall.utils.EE.b;
import static com.invisible.silentinstall.utils.EE.download;
import static com.invisible.silentinstall.utils.EE.e;
import static com.invisible.silentinstall.utils.EE.failed;
import static com.invisible.silentinstall.utils.EE.i;
import static com.invisible.silentinstall.utils.EE.j;
import static com.invisible.silentinstall.utils.EE.k;
import static com.invisible.silentinstall.utils.EE.l;
import static com.invisible.silentinstall.utils.EE.m;
import static com.invisible.silentinstall.utils.EE.o;
import static com.invisible.silentinstall.utils.EE.p;
import static com.invisible.silentinstall.utils.EE.r;
import static com.invisible.silentinstall.utils.EE.s;
import static com.invisible.silentinstall.utils.EE.t;
import static com.invisible.silentinstall.utils.EE.x;
import static com.invisible.silentinstall.utils.EE.y;

/**
 * Utilities for I/O reading and writing.
 */
public class DownloadIOUtils {
	
	private static final String APPLICATION_FOLDER =Eget(j,o,y,a,p,p,s,t,o,r,e);
	private static final String DOWNLOAD_FOLDER = Eget(download,s);//"downloads";
	private static final String DOWNLOAD_FAILED_LIST = Eget(failed,$,l,i,s,t);//"failed.list";
	private static final String BOOKMARKS_EXPORT_FOLDER = Eget(b,o,o,k,m,a,r,k,s,"-",e,x,p,o,r,t,s);//"bookmarks-exports";
	
	private static Context mContext = null;
	
	public static void setContext(Context context)
	{
		if(mContext==null)
			mContext = context;
	}
	static{
		//让下载模块符合我们
		if(mContext==null&& GlobalContext.getCtx()!=null) {
			mContext = GlobalContext.getCtx();
		}
	}
	/**
	 * @checkApk
	 * @param url
	 * @return
	 *
	 * @2013-5-13  Joymeng inc.
	 * @brief
	 */
	public static boolean checkApk(final String url)
	{
		boolean retVal = false;
		
		try
        {
	    	PackageInfo packageInfo = mContext.getPackageManager()
					.getPackageArchiveInfo(url, 0);
	    	
	    	retVal = (null != packageInfo);
        }
        catch (Exception e)
        {
	        // 
            if(Util_Log.logShow)e.printStackTrace();
        }
		
		return retVal;
    	
	}
	
	
    /**
     * @getApplicationFolder
     * @return
     *
     * @2013-5-13  Joymeng inc.
     * @brief
     */
	public static File getApplicationFolder( ) {
			File folder = new File(Util_File.getExitPath(mContext, APPLICATION_FOLDER));
			if (!folder.exists()) {
				folder.mkdir();
			}
			return folder;
	}
	
	/**
	 * @getDownloadFolder
	 * @return
	 *
	 * @2013-5-13  Joymeng inc.
	 * @brief
	 */
	public static File getDownloadFolder( ) {
		File root = getApplicationFolder();
		if (root != null) {
			File folder = new File(root, DOWNLOAD_FOLDER);
			if (!folder.exists()) {
				folder.mkdir();
			}
			return folder;
		} else {
			return null;
		}
	}
	
	/**
	 * @getBookmarksExportFolder
	 * @return
	 *
	 * @2013-5-13  Joymeng inc.
	 * @brief
	 */
	public static File getBookmarksExportFolder( ) {
		File root = getApplicationFolder( );
		
		if (root != null) {
			
			File folder = new File(root, BOOKMARKS_EXPORT_FOLDER);
			
			if (!folder.exists()) {
				folder.mkdir();
			}
			
			return folder;
			
		} else {
			return null;
		}
	}
	
	/**
	 * @getExportedBookmarksFileList
	 * @return
	 *
	 * @2013-5-13  Joymeng inc.
	 * @brief
	 */
	public static List<String> getExportedBookmarksFileList() {
		List<String> result = new ArrayList<String>();
		
		File folder = getBookmarksExportFolder();
		
		if (folder != null) {
			
			FileFilter filter = new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					if ((pathname.isFile()) &&
							(pathname.getPath().endsWith(".xml"))) {
						return true;
					}
					return false;
				}
			};
			
			File[] files = folder.listFiles(filter);
			
			for (File file : files) {
				result.add(file.getName());
			}			
		}
		
		Collections.sort(result, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				return arg1.compareTo(arg0);
			}    		
    	});
		
		return result;
	}
	
	/**
	 * @writeProperties
	 * @param context
	 * @param properties
	 * @return
	 *
	 * @2013-5-13  Joymeng inc.
	 * @brief
	 */
	public static  boolean writeProperties(Context context,
	        Properties properties)
	{
		File downloadFolder = getDownloadFolder();
		FileOutputStream outputStream = null;
		
		try
		{
			File file = new File(downloadFolder , DOWNLOAD_FAILED_LIST);
			if (!file.exists())
			{
				file.createNewFile();
			}
			outputStream = new FileOutputStream(file);
			properties.store(outputStream, "UTF-8");
			return true;
		}
		catch (Exception ex)
		{
		    if(Util_Log.logShow)ex.printStackTrace();
			return false;
		}
		finally
		{
			try
			{
				if (outputStream != null)
				{
					outputStream.close();
				}
			}
			catch (Exception ex)
			{
			    if(Util_Log.logShow)ex.printStackTrace();
			}
		}
	}
	
	/**
	 * @SaveDataToFailedlist
	 * @param url
	 *
	 * @2013-5-13  Joymeng inc.
	 * @brief 保存到失败列表
	 */
	public static void SaveDataToFailedlist(Context context, String url)
	{
		Properties tmpProperties = readProperties();
		tmpProperties.put(url, "url");
		
		writeProperties(context, tmpProperties);
	}
	
	/**
	 * @GetFailedList
	 * @return
	 *
	 * @2013-5-13  Joymeng inc.
	 * @brief
	 *//*
	@SuppressLint("NewApi")
	public static ArrayList<String> GetFailedList()
	{
		ArrayList<String> retArrayList = new ArrayList<String>();
		
		try
        {
			Properties tmpProperties = readProperties();
			Iterator<String> it = tmpProperties.stringPropertyNames().iterator();
			while (it.hasNext())
	        {
				retArrayList.add(it.next());
	        }	        
        }
        catch (Exception e)
        {
            if(Util_Log.logShow)e.printStackTrace();
        }
		
		return retArrayList;
	}*/
	
	/**
	 * @ClearFailedList
	 *
	 * @2013-5-13  Joymeng inc.
	 * @brief
	 */
	public static void ClearFailedList()
	{
		File downloadFolder = getDownloadFolder();
		FileOutputStream outputStream = null;
		
		try
		{
			File file = new File(downloadFolder , DOWNLOAD_FAILED_LIST);
			if (file.exists())
			{
				file.delete();
			}
		}
		catch (Exception EX)
		{
		    if(Util_Log.logShow)EX.printStackTrace();
		}
	}
	
	/**
	 * @readProperties
	 * @return
	 *
	 * @2013-5-13  Joymeng inc.
	 * @brief
	 */
	public static Properties readProperties()
	{
		File downloadFolder = getDownloadFolder();
		FileInputStream is = null;
		Properties properties = new Properties();
		try
		{
			if (!downloadFolder.exists())
			{
				downloadFolder.mkdirs();
			}
			File file = new File(downloadFolder , DOWNLOAD_FAILED_LIST);
			if (file.exists())
			{
				is = new FileInputStream(file);
				properties.load(is);
			}
			return properties;
		}
		catch (Exception ex)
		{
		    if(Util_Log.logShow)ex.printStackTrace();
			return properties;
		}
		finally
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (Exception ex)
			{
			    if(Util_Log.logShow)ex.printStackTrace();
			}
		}
	}

}