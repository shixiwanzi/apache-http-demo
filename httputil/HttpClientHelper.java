package com.pagoda.nerp.uniseq.buyer.common.httputil;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import net.landzero.xlog.XLog;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClientHelper {

	private static final Logger logger = LoggerFactory.getLogger(HttpClientHelper.class);

	public static DefaultHttpClient httpclient;

	static {

		HttpParams httpParams = new BasicHttpParams();
		// 设置请求超时时长
		httpParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 200000);
		// 设置等待数据超时时间 - 读取时间
		httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
		httpParams.setLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, 500L);

		PoolingClientConnectionManager conMgr = new PoolingClientConnectionManager();
		conMgr.setMaxTotal(2000);
		conMgr.setDefaultMaxPerRoute(conMgr.getMaxTotal());
		httpclient = new DefaultHttpClient(conMgr,httpParams);
		httpclient = (DefaultHttpClient) HttpClientConnectionManager.getSSLInstance(httpclient); // 接受任何证书的浏览器客户端
	}

	public static String invoke(String url,String reqBody){
		String method = "POST";
		return invoke(url, reqBody, method);
	}

	public static String invoke(String url){
		if (url == null || StringUtils.isEmpty(url)){
			logger.warn("请求未携带请求地址，请求失败!!");
			return null;
		}
		HttpGet httpGet = HttpClientConnectionManager.getGetMethod(url);
		try {
			logger.info("推送给目标地址:" + XLog.K(url));
			HttpResponse response = HttpClientHelper.httpclient.execute(httpGet);
			String jsonStr = EntityUtils.toString(response.getEntity(), "utf-8");
			logger.info("请求服务响应数据为：" + XLog.K(jsonStr));
			httpGet.releaseConnection();
			return jsonStr;
		} catch (Exception e) {
			logger.error("请求目标地址异常！", e);
		}
		return null;

	}

	public static String invokeAuth(String url,String reqBody,String username,String password) {
		if (url == null || StringUtils.isEmpty(url)){
			logger.warn("请求未携带请求地址，请求失败!!");
			return null;
		}
		HttpPost httpost = HttpClientConnectionManager.getPostMethod(url);
		String baseStr = username + ":" + password;
		String base64Str = "";
		try {
			BASE64Encoder base64=new BASE64Encoder();
			base64Str = base64.encode(baseStr.getBytes("utf-8"));
			httpost.addHeader("Authorization"," Basic "+base64Str);
		}catch(Exception e){
			logger.error("Base64 加密失败!", e);
		}
		try {
			logger.info("推送给目标地址:" + XLog.K(url) + "的报文体内容为：{}" , reqBody);
			httpost.setEntity(new StringEntity(reqBody, "UTF-8"));
			HttpResponse response = httpclient.execute(httpost);
			String jsonStr = EntityUtils.toString(response.getEntity(), "utf-8");
			logger.info("请求服务响应数据为：" + XLog.K(jsonStr));
			httpost.releaseConnection();
			return jsonStr;
		} catch (Exception e) {
			logger.error("请求目标地址异常！", e);
		}
		return null;
	}

	/**
	 * 发起HTTP请求
	 * @param url 请求目标地址
	 * @param reqBody 请求体
	 * @param method 请求方式，默认为POST方式提交
	 * @return 返回响应结果
	 */
	private static String invoke(String url,String reqBody,String method) {
		if (StringUtils.isEmpty(url)){
			logger.warn("请求未携带请求地址，请求失败!!");
			return null;
		}
		HttpPost httpost = HttpClientConnectionManager.getPostMethod(url);
		try {
		    logger.info("推送给目标地址:" + XLog.K(url) + "的报文体内容为：{}" , reqBody);
			httpost.setEntity(new StringEntity(reqBody, "UTF-8"));
			HttpResponse response = HttpClientHelper.httpclient.execute(httpost);
			String jsonStr = EntityUtils.toString(response.getEntity(), "utf-8");
			logger.info("请求服务响应数据为：" + XLog.K(jsonStr));
			httpost.releaseConnection();
			return jsonStr;
		} catch (Exception e) {
			logger.error("请求目标地址异常！", e);
		}
		return null;
	}

	/**
	 * 文件上传
	 * @param srtPath 原路径
	 * @param url 上传服务器地址
	 * @return 返回文件上传结果
	 */
	public static String upLoadMedia(String srtPath, String url) {
		File file = new File(srtPath);
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			// 设置上传属性
			con.setRequestMethod("POST"); // 以Post方式提交表单，默认get方式
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			// 设置请求头信息
			con.setRequestProperty("Connection", "Keep-Alive");
			con.setRequestProperty("Charset", "UTF-8");
//			con.setRequestProperty(Constants.HTTP_CRID_HEADER, XLog.crid());
			// 设置边界
			String BOUNDARY = "----------" + System.currentTimeMillis();
			con.setRequestProperty("content-type", "multipart/form-data; boundary=" + BOUNDARY);
			// 第一部分：
			StringBuilder sb = new StringBuilder();
			sb.append("--"); 	// 必须多两道线
			sb.append(BOUNDARY);
			sb.append("\r\n");
			sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
			sb.append("Content-Type:application/octet-stream\r\n\r\n");
			byte[] head = sb.toString().getBytes("utf-8");
			// 获得文件上传的输出流
			OutputStream out = new DataOutputStream(con.getOutputStream());
			out.write(head);
			// 文件正文部分
			DataInputStream in = new DataInputStream(new FileInputStream(file));
			int bytes = 0;
			byte[] bufferOut = new byte[1024];
			while ((bytes = in.read(bufferOut)) != -1) {
				out.write(bufferOut, 0, bytes);
			}
			in.close();
			// 结尾部分
			byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
			out.write(foot);
			out.flush();
			out.close();

			/**
			 * 读取服务器响应，必须读取,否则提交不成功
			 */
			StringBuffer returnBuffer = new StringBuffer();
			int code = con.getResponseCode();
			System.out.println(code);
			if (code == 200) { // OK
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
					String line = null;
					while ((line = reader.readLine()) != null) {
						returnBuffer.append(line);
					}
					System.out.println(line);
				} catch (Exception e) {
					logger.error("微信上传多媒体资源时异常！", e);
				}
			}
			// 关闭连接
			con.disconnect();
			return returnBuffer.toString();
		} catch (Exception e) {
			logger.error("upLoadMedia到微信服务器异常：", e);
			return null;
		}
	}

	public static void main(String[] args){
//		String url = "http://localhost:8081/vendormgr/checkVendor";
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("vendorId",1);
//		String resp = HttpClientHelper.invoke(url, jsonObject.toString());
//		System.out.println(resp);
		//"203f90b5e9d5434081be52e1b472e8a1:changeme"
		String body = "{\"CUSTOMERCODE\": \"chendai001\", \"CELLPHONE\": \"18344016302\"}";
		invokeAuth("http://114.55.235.140:8003/api/pagoda/autoRegister",body,"203f90b5e9d5434081be52e1b472e8a1","changeme");

	}
	
}
