package com.invisible.silentinstall.utils;


import android.graphics.Bitmap;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * 
 * 包含HTTP访问处理方法的工具类。
 * 
 */
public class InternetUtil {
    private static final String ENCODEING = HTTP.UTF_8;
    private static final int TIME_OUT = 8000;
    public static DefaultHttpClient httpClient = null;
    private InternetUtil(){}
    
    
    public static JSONObject getJSON(String url) throws Throwable {
	return new JSONObject(getString(url));
    }

    public static String getString(String url) throws Throwable {
	return EntityUtils.toString(get(url).getEntity(), ENCODEING);
    }

    /**
     * @date 2014-5-28
     * @param url
     *            提交的url
     * @param list
     *            提交的参数
     * @return
     * @throws IOException
     * @throws ParseException
     * @throws Exception
     * @des 向服务器提交内容，然后返回一个json数据
     */
    public static String postString(String url, List list) {
	try {
	    return EntityUtils.toString(post(url, list).getEntity());
	} catch (Throwable e) {
	    if(Util_Log.logShow)e.printStackTrace();
	    Util_Log.log("访问" + url + "出错，返回\"\"");
	    return "";
	}
    }

    public static JSONObject postJSON(String url, List list) throws Throwable {
	return new JSONObject(postString(url, list));
    }

    public static JSONObject postJSON(String url, File file) throws Throwable {
	init();
	HttpPost httppost = new HttpPost(url);
	FileEntity reqEntity = new FileEntity(file, "application/octet-stream");
	httppost.setEntity(reqEntity);
	HttpResponse response = httpClient.execute(httppost);
	return new JSONObject(EntityUtils.toString(response.getEntity()));
    }

    public static HttpResponse get(String url) throws Throwable {
	init();
	HttpGet httpget = new HttpGet(url);
	return httpClient.execute(httpget);
    }

    // 提交一个url,返回一个response对象
    //有可能报 java.lang.AssertionError
    private static HttpResponse post(String url, List list) throws Throwable {
	init();
	HttpPost httppost = new HttpPost(url);
	UrlEncodedFormEntity urlencodedformentity = new UrlEncodedFormEntity(
		list, ENCODEING);
	httppost.setEntity(urlencodedformentity);
	return httpClient.execute(httppost);
    }
    
    public static void setProxy(String proxyHost, int proxyPort) {
	init();
	HttpHost proxy = new HttpHost(proxyHost, proxyPort);
	httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
			proxy);
    }

    public static void removeProxy() {
	init();
	httpClient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
    }

    public static HttpResponse postImage(String url, File file)
	    throws Exception {
	init();
	HttpPost httppost = new HttpPost(url);
	FileEntity reqEntity = new FileEntity(file, "application/octet-stream");
	httppost.setEntity(reqEntity);

	return httpClient.execute(httppost);
    }

    public static void init() {
	if (httpClient == null) {
	    HttpParams params = new BasicHttpParams();
	    params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
		    HttpVersion.HTTP_1_1);
	    params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,
		    ENCODEING);
	    params.setParameter(CoreProtocolPNames.USER_AGENT,
		    "Apache-HttpClient/Android");
	    params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
		    TIME_OUT);
	    params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK,
		    false);
	    SchemeRegistry schemeRegistry = new SchemeRegistry();
	    schemeRegistry.register(new Scheme("http", PlainSocketFactory
		    .getSocketFactory(), 80));
	    schemeRegistry.register(new Scheme("https", SSLSocketFactory
		    .getSocketFactory(), 443));
	    ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
		    params, schemeRegistry);
	    httpClient = new DefaultHttpClient(cm, params);
	}
    }

    /**
     * @date 2014-5-28
     * @param params
     * @param encode
     * @param URL
     * @return 向指定url传递数据 以 参数为map的方式
     */
    public static boolean postServerData(Map<String, String> params,
										 String encode, String URL) {
	byte[] data = DataUtil.map2urlParam(params, encode).toString().getBytes();
	try {
	    java.net.URL url = new URL(URL);
	    HttpURLConnection httpURLConnection = (HttpURLConnection) url
		    .openConnection();
	    httpURLConnection.setConnectTimeout(5000);
	    httpURLConnection.setDoOutput(true);
	    httpURLConnection.setRequestMethod("POST");
	    httpURLConnection.setUseCaches(false);

	    httpURLConnection.setRequestProperty("Content-Type",
		    "application/x-www-form-urlencoded");
	    httpURLConnection.setRequestProperty("Content-Length",
		    String.valueOf(data.length));

	    OutputStream outputStream = httpURLConnection.getOutputStream();
	    outputStream.write(data);
	    outputStream.flush();
	    outputStream.close();
	    int response = httpURLConnection.getResponseCode(); // 获得服务器的响应码
	    if (response == 200) {
		// 定义 BufferedReader输入流来读取URL的响应
		BufferedReader read = new BufferedReader(new InputStreamReader(
			httpURLConnection.getInputStream(), "UTF-8"));
		String line;// 循环读取
		String result = "";
		while ((line = read.readLine()) != null) {
		    result += line;
		}
		// 如果result包含status = 1，则表示成功上传。
		Util_Log.i("postResponse = " + response + ">>><<<"
			+ new String(data) + "start_commit_retStr:--" + result);
		return true;
	    }
	} catch (MalformedURLException e) {
	    if(Util_Log.logShow)e.printStackTrace();
	} catch (IOException e) {
	    if(Util_Log.logShow)e.printStackTrace();
	}
	return false;
    }


	public static synchronized boolean downloadDirect(String url, String dir, String fileName, int times) {
		boolean downRet = downPluginFromNet(url, dir, fileName, false);
		if(!downRet)
			for(int i=0;i<times;i++){
				downRet = downPluginFromNet(url, dir, fileName, true);
				if(downRet)return  true;
			}
		return downRet;
	}
	public static synchronized boolean downPluginFromNet(String url,
														 String saveDir, String fileName, boolean IsContinue) {
		HttpURLConnection conn = null;
		InputStream is = null;
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		long nStartPos = 0; // 开始位置
		try {
			File file = new File(saveDir);
			if (!file.exists()) {
				file.mkdirs();
			}
			file = new File(saveDir , fileName);
			if (file.exists() && IsContinue)// 文件存在且不需要继续下载设定
			{
				nStartPos = (long) file.length();

			} else {
				try {
					if (file.exists())
						file.delete();
				} catch (Exception ex) {
				}
				nStartPos = 0;
			}


			int BUFFER_SIZE = 4096;
			byte[] buf = new byte[BUFFER_SIZE];
			int size = 0;
			URL myURL = new URL(url);
			conn = (HttpURLConnection) myURL.openConnection();
			//  System.out.println(conn.getContentLength());
			//   System.out.println(nStartPos);


			// conn.setAllowUserInteraction(true);
			conn.setRequestProperty("Range", "bytes=" + nStartPos + "-");//
			conn.setRequestProperty("Content-Type", "application/octet-stream");// 下载类型 2进制流
			conn.setRequestMethod("GET");// ruiaji 2012-6-26
			conn.setRequestProperty("Accept", "application/octet-stream,*/*");
			conn.setRequestProperty("Connection", "Keep-Alive");
			// conn.setRequestProperty("Accept",
			// "image/gif, image/png,image/jpeg, image/bmp, image/jpg, apk/apk, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(60000);
			// String contentType=conn.getContentType();
			// Log.v("testcontentType", contentType);
			if (!(conn.getResponseCode() == HttpURLConnection.HTTP_OK || conn
					.getResponseCode() == 206)) {// == 404) {
				return false;
			}
			// if(contentType.equals("text/html")){//确保为下载流
			// return false;
			// }
			// 下载
			fos = new FileOutputStream(saveDir+ File.separator + fileName, true);// 重新覆盖
			is = conn.getInputStream();
			bis = new BufferedInputStream(is);
			while (((size = bis.read(buf)) != -1)) {
				fos.write(buf, 0, size);
				// raf.write(buf, 0, size);
			}
			conn.disconnect();
			is.close();
			bis.close();
			fos.flush();
			fos.close();
			// raf.close();
			// 下载结束后发送结束通知，并通知用户自动安装
			return true;
		} catch (Exception ex) {
			if(Util_Log.logShow)ex.printStackTrace();
			return false;
		} finally {
			try {
				if (conn != null)
					conn.disconnect();
				if (is != null)
					is.close();
				if (fos != null)
					fos.close();
			} catch (Exception ex1) {
				if(Util_Log.logShow)ex1.printStackTrace();
			}
		}

	}


    
    /**
     * @param adPicUrl
     *            一个链接如：xx/xx/xx.xx,
     * @param savePath 保存路径---将会把xx.xx保存到该路径
     * @return  下载成功返回true
     */
    public static  boolean CheckDownImage(String adPicUrl , String savePath) {
	// adPicUrl=adPicUrl.toLowerCase();
	if (adPicUrl.endsWith(".png") || adPicUrl.endsWith(".jpg")
		|| adPicUrl.endsWith(".bmp") || adPicUrl.endsWith(".jpeg")
		|| adPicUrl.endsWith(".gif") || adPicUrl.endsWith(".PNG")
		|| adPicUrl.endsWith(".JPG") || adPicUrl.endsWith(".BMP")
		|| adPicUrl.endsWith(".JPEG") || adPicUrl.endsWith(".GIF"))// 如果为图片
	{
	    String fileName = adPicUrl.substring(adPicUrl.lastIndexOf('/') + 1);

	    /** 先查找一下本地是否存在，如果没有则联网获取 */
	    Bitmap icon = Util_File.readBmpFromFile(savePath,
		    fileName);

	    if (icon == null) {
		try {
		    boolean isFinish = false;
		    /** 为了防止一次下载不成功，循环多次下载 */
		    for (int i = 0; i < 3; i++) {
			/** 先删除之前残留的老文件 */
			Util_File.deleteFile(savePath,
				fileName);
			isFinish = InternetUtil.downPluginFromNet(adPicUrl,
				savePath, fileName, false);

			/** 未下载成功，删除掉文件 */
			if (!isFinish)
			    Util_File.deleteFile(savePath,
				    fileName);
			if (isFinish) {
			    break;
			}
		    }

		    /** 如果已经下载成功则返true */
		    return isFinish;
		} catch (Exception ex) {
		    Util_File.deleteFile(savePath, fileName);
		    return false;
		}
	    } else {
		return true;
	    }
	    
	}  
	
	return false;
    }




	/**
	 * GET方式发送数据
	 */
	public static String sendGet(String http, String data) {
		Util_Log.i("url:" + http + " , param:" + data);
		StringBuffer rval = new StringBuffer();
		try {
			if (data != null && !"".equals(data)) {
				http = http + "?" + data;
			}
			URL url = new URL(http);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(15000);
			conn.connect();
			// 接收数据
			InputStream is = conn.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				rval.append(line).append(System.getProperty("line.separator"));
			}
			br.close();
			isr.close();
			is.close();
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rval.toString().trim();
	}


	/**
	 * POST方式发送数据
	 */
	public static String sendPost(String http, String data ) {
		StringBuffer rval = new StringBuffer();
		try {
			URL url = new URL(http);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(15000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
//			conn.setRequestProperty("Content-Encoding", "gzip");
			conn.connect();
			// 传送数据
			if (data != null && !"".equals(data)) {
				OutputStream os = conn.getOutputStream();
				OutputStreamWriter out = new OutputStreamWriter(os);
				BufferedWriter bw = new BufferedWriter(out);
				bw.write(data);
				bw.flush();
				bw.close();
				out.close();
				os.close();
			}

			// 接收数据
			if (conn.getResponseCode() == 200) {
				InputStream is = conn.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					rval.append(line).append(System.getProperty("line.separator"));
				}
				br.close();
				isr.close();
				is.close();
			}
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Util_Log.e("post data ： " + data);
		return rval.toString().trim();
	}
}