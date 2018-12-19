package com.pagoda.nerp.uniseq.buyer.common.httputil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.landzero.xlog.XLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/*** 
 *  Copyright (C), pagoda 
 *  File name and path : uniseq-vendor.com.pagoda.nerp.uniseq.vendor.common.HttpHelper.java
 *  Author : Simba Zhou, create time : 2016年12月19日 上午10:54:42  
 *  Description :    http请求辅助类      
 *  Others:                                                      
 ***/
public final class HttpHelper {
	private static final Logger logger = LoggerFactory.getLogger(HttpHelper.class);
	
	/** 默认字符集 */
	public static final String UTF_8 = "utf-8";
	
	/**        
	 *  Author :  Simba Zhou, create time: 2016年10月26日 下午2:50:26;   
	 *  Description :      post请求                                                            
	 *  @param serverUrl   请求的url地址
	 *  @param params      请求的字符串
	 *  @return            服务器返回信息的字符串格式                 
	 **/
	public static String doPost(String serverUrl, String params) {
		HttpURLConnection connection = null;
		StringBuilder result = new StringBuilder();
		try {
			// 创建连接
			URL url = new URL(serverUrl);
			logger.info("推送给目标地址:" + XLog.K(url));

			connection = (HttpURLConnection) url.openConnection();

			// 设置http连接属性
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);

			// 设置http头 消息
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
//			connection.setRequestProperty(Constants.HTTP_CRID_HEADER, XLog.crid());
			// 连接
			connection.connect();

			// 获取结果
			OutputStream out = connection.getOutputStream();
			out.write(params.getBytes(UTF_8));
			out.flush();
			out.close();

			// 读取响应
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), UTF_8));
			String lines;
			while ((lines = reader.readLine()) != null) {
				result.append(lines);
//				result.append("\n");
			}
			reader.close();
		} catch (MalformedURLException e) {
			result.append("invoke url="+serverUrl+" occur an error, msg: "+e);
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			result.append("invoke url="+serverUrl+" occur an error, msg: "+e);
			e.printStackTrace();
		} catch (IOException e) {
			result.append("invoke url="+serverUrl+" occur an error, msg: "+e);
			e.printStackTrace();
		} finally {
			connection.disconnect();
		}
		logger.info("请求服务响应数据为：" + result);

		return result.toString();
	}
	
	
	/**        
	 *  Author :  Simba Zhou, create time: 2016年10月26日 下午2:56:48;   
	 *  Description :      get请求                                                            
	 *  @param serverUrl   请求的url地址
	 *  @return            服务器返回信息的字符串格式                     
	 **/
	public static String doGet(String serverUrl) {
		HttpURLConnection connection = null;
		StringBuilder result = new StringBuilder();
		try {
			// 创建连接
			URL url = new URL(serverUrl);
			connection = (HttpURLConnection) url.openConnection();

			// 连接
			connection.connect();

			// 读取响应
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), UTF_8));
			String lines;
			while ((lines = reader.readLine()) != null) {
				result.append(lines);
//				result.append("\n");
			}
			reader.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			connection.disconnect();
		}
		return result.toString();
	}
}
