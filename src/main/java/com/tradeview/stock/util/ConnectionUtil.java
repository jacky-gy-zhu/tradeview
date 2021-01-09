package com.tradeview.stock.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class ConnectionUtil {
	
	private ConnectionUtil(){
	}
	
	private static ConnectionUtil instance = null;
	
	private static synchronized void syncInit() {  
        if (instance == null) {  
            instance = new ConnectionUtil();  
        }  
    }
	
	public static ConnectionUtil getInstance() {  
        if (instance == null) {  
            syncInit();  
        }  
        return instance;  
    }

	public JSONObject getJsonObject(String url, String encode){
		String result = get(url,encode);
		if(result != null && result.startsWith("{")){
			return new JSONObject(result);
		}else{
			return null;
		}
	}
	
	public JSONArray getJsonArray(String url, String encode){
		String result = get(url,encode);
		if(result != null && result.startsWith("[")){
			return new JSONArray(result);
		}else{
			return null;
		}
	}
	
	public String get(String url, String encode){
		int readTimeout = 3000;
		try{
			return getInternal(url, encode, readTimeout);
		}catch(SocketTimeoutException e){
			return null;
		}catch(Exception e){
			System.out.println("一次链接失败：");
			return null;
		}
	}

	private String getInternal(String url, String encode, int readTimeout)throws MalformedURLException, IOException, ProtocolException, UnsupportedEncodingException {
		return getInternal(url,encode,readTimeout,1);
	}

	private String getInternal(String url, String encode, int readTimeout, int agent)
			throws MalformedURLException, IOException, ProtocolException, UnsupportedEncodingException {
		URL tirc;
		HttpURLConnection connection;
		StringBuilder content = new StringBuilder();
		System.setProperty("sun.net.client.defaultConnectTimeout", "20000"); 
		System.setProperty("sun.net.client.defaultReadTimeout", ""+readTimeout);
		url = url.trim().replaceAll(" ", "%20");
		tirc = new URL(url);
		connection = (HttpURLConnection) tirc.openConnection();
		if(agent == 1){
			connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		}else{
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
		}
		connection.setConnectTimeout(20000);
		connection.setReadTimeout(readTimeout);
		connection.setDoOutput(false);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-type","application/x-java-serialized-object");
		connection.setRequestMethod("GET");
		int responseCode = connection.getResponseCode();
		if(responseCode == 200){
			connection.connect();
			try(InputStream inStrm = connection.getInputStream();InputStreamReader isr = new InputStreamReader(inStrm,encode);BufferedReader in = new BufferedReader(isr);){
				String thisLine;
				while ((thisLine = in.readLine()) != null) {
					content.append(thisLine);
				}
			}
		}
		return content.toString();
	}
	
	private String getInternal2(String url, String encode, int readTimeout, int agent)
			throws MalformedURLException, IOException, ProtocolException, UnsupportedEncodingException {
		URL tirc;
		HttpURLConnection connection;
		StringBuilder content = new StringBuilder();
		System.setProperty("sun.net.client.defaultConnectTimeout", "20000"); 
		System.setProperty("sun.net.client.defaultReadTimeout", ""+readTimeout);
		url = url.trim().replaceAll(" ", "%20");
		tirc = new URL(url);
		connection = (HttpURLConnection) tirc.openConnection();
		if(agent == 1){
			connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
		}else{
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
		}
		connection.setConnectTimeout(20000);
		connection.setReadTimeout(readTimeout);
		connection.setDoOutput(false);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-type","application/x-java-serialized-object");
		connection.setRequestMethod("GET");
		int responseCode = connection.getResponseCode();
		if(responseCode == 200){
			connection.connect();
			try(InputStream inStrm = connection.getInputStream();InputStreamReader isr = new InputStreamReader(inStrm,encode);BufferedReader in = new BufferedReader(isr);){
				String thisLine;
				while ((thisLine = in.readLine()) != null) {
					content.append(thisLine+"\n");
				}
			}
		}
		return content.toString();
	}
}
