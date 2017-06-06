package com.invisible.silentinstall.download;

import android.util.Log;

import com.invisible.silentinstall.utils.Util_Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author xiedalu
 *
 *  -已存在相同的包，也会认为下载成功
 */
public class DownloadRunnable implements Runnable {

    /** 下载缓冲 512K */
    private static final int BUFFER_SIZE = 1024 * 512;

    private DownloadItem mParent;

    private boolean mAborted;

    /**
     * Contructor.
     * 
     * @param parent
     *            The item to download.
     */
    public DownloadRunnable(DownloadItem parent) {
	mParent = parent;
	mAborted = false;
    }

//    private Handler mHandler = new Handler() {
//
//	public void handleMessage(Message msg) {
//	    switch (msg.what) {
//	    /** ok finish */
//	    case 0:
//		mParent.onFinished();
//		break;
//
//	    case 1:
//		mParent.onFailed();
//		break;
//	    default:
//		break;
//	    }
//
//	}
//    };

    /**
     * Compute the file name given the url.
     * 
     * @return The file name.
     */
    private String getFileNameFromUrl() {
	String fileName = mParent.getUrl().substring(
		mParent.getUrl().lastIndexOf("/") + 1);

	int queryParamStart = fileName.indexOf("?");
	if (queryParamStart > 0) {
	    fileName = fileName.substring(0, queryParamStart);
	}

	return fileName;
    }

    /**
     * Get a file object representation of the file name, in th right folder of
     * the SD card.
     * 
     * @return A file object.
     */
    private File getFile() {

	File downloadFolder = DownloadIOUtils.getDownloadFolder();

	if (downloadFolder != null) {

	    return new File(downloadFolder, getFileNameFromUrl());

	} else {
	    mParent.setErrorMessage("Unable to get download folder from SD Card.");
	    return null;
	}
    }
    
    
    @Override
    public void run() {
	File downloadFile = getFile();
	long startPos = 0;
	if (downloadFile != null) {
	    if (downloadFile.exists()) {
		if (downloadFile.getAbsolutePath().endsWith(".apk")) {
		    if (DownloadIOUtils
			    .checkApk(downloadFile.getAbsolutePath())) {
			/** 如果apk 可用 直接弹出安装 */
//			mHandler.sendEmptyMessage(0);
			mParent.onFinished();
			return;
		    } else {
			startPos = downloadFile.length();
		    }
		} else {
		    startPos = downloadFile.length();
		}
	    }

	    BufferedInputStream bis = null;
	    BufferedOutputStream bos = null;

	    try {

		mParent.onStart();

		System.setProperty("http.keepAlive", "false");  
		URL url = new URL(mParent.getUrl());
		URLConnection conn = url.openConnection();

		conn.setConnectTimeout(30000);
		conn.setReadTimeout(60000);
		conn.setRequestProperty("Range", "bytes=" + startPos + "-");
		

		int size = conn.getContentLength();
		Log.i("Funny", "startPos=" + startPos);
		Log.i("Funny", "size=" + size);
		String fileHeader = conn.getHeaderField("Content-Disposition");
		if (fileHeader != null) {
		    fileHeader = fileHeader.toLowerCase();
		    int index = fileHeader.indexOf("filename");
		    if (index != -1) {
			String name = fileHeader.substring(
				index + "filename".length() + 1,
				fileHeader.length());

			name = name.replace("'", "").replace("\"", "");

			if (downloadFile != null) {
			    downloadFile = new File(
				    DownloadIOUtils.getDownloadFolder(), name);
			    mParent.updateFileName(name);
			}
		    }
		}

		double oldCompleted = 0;
		double completed = 0;
		InputStream is = conn.getInputStream();
		bis = new BufferedInputStream(is);
		bos = new BufferedOutputStream(new FileOutputStream(
			downloadFile, true));

		boolean downLoading = true;
		byte[] buffer = new byte[BUFFER_SIZE];
		int downloaded = 0;
		int read;

		oldCompleted = (((downloaded + startPos) * 100f) / (size + startPos));
		mParent.onProgress((int) oldCompleted);

		while ((downLoading) && (!mAborted)) {

		    if ((size - downloaded < BUFFER_SIZE)
			    && (size - downloaded > 0)) {
			buffer = new byte[size - downloaded];
		    }
//		    System.out.println(Arrays.toString(buffer));
		    read = bis.read(buffer);

		    if (read > 0) {
			bos.write(buffer, 0, read);
			downloaded += read;

			completed = (((downloaded + startPos) * 100f) / (size + startPos));

		    } else {
			downLoading = false;
		    }

		    // Notify each 5% or more.
		    if (oldCompleted + 5 < completed) {
			mParent.onProgress((int) completed);
			oldCompleted = completed;
		    }
		}
	    } catch (Throwable E) {
		
		
		E.printStackTrace();
		mParent.setErrorMessage("EXCPTION:"+E.getMessage());
	    }  finally {
		Log.i("Funny", "finally close...");
		if (bis != null) {
		    try {
			bis.close();
		    } catch (IOException ioe) {
			if (Util_Log.logShow)
			    ioe.printStackTrace();
			mParent.setErrorMessage(ioe.getMessage());
		    }
		}
		if (bos != null) {
		    try {
			bos.close();
		    } catch (IOException ioe) {
			if (Util_Log.logShow)
			    ioe.printStackTrace();
			mParent.setErrorMessage(ioe.getMessage());
		    }
		}
	    }

	    if (mAborted) {
		/** 小于1M的包 删除掉 */
		if (downloadFile.exists()
			&& (downloadFile.length() < 1 * 1024 * 1024)) {
		    downloadFile.delete();
		}
	    }

	}

	if ((null == mParent.getErrorMessage())
		|| (mParent.getErrorMessage().equals(""))) {
	    /* 没有错误消息，下载成功，否则下载失败 */
//	    mHandler.sendEmptyMessage(0);

		//下载完的不是正确的包的话，删除重新下

 	    mParent.onFinished();
	} else {
//	    mHandler.sendEmptyMessage(1);
	    mParent.onFailed();
	}
    }

    /**
     * Abort this download.
     */
    public void abort() {
	mAborted = true;
    }

}