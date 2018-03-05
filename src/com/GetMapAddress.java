package com;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * This example demonstrates how to create secure connections with a custom SSL
 * context.
 */
public class GetMapAddress {

	public static void main(String args[]) throws Exception {
		//muiltProcess();
		oneProcess();
	}

	private static void muiltProcess() throws Exception{
		 String filePath ="D:/temp/address.txt";
		 try {
		     
             BufferedReader in = new BufferedReader(new FileReader(filePath));
 	         String str = "";
 	    	 String addressTemp = "";
 	    	 String[] posTemp = {"",""};
 	         
		      while ((str = in.readLine()) != null) {
		        //System.out.println(str);
				String address = str;//"강원도 태백시 황지동 664-1";

				
				String[] pos = {"",""};
				if(!address.equals(addressTemp)){
//					System.out.print(str + "\t");
					
					//다음API
					pos = addr2coord(address);
					//네이버API
				    //pos = geocode(address);				    
//				    System.out.print( pos[0] + "\t" + pos[1]);
//				    System.out.println("");
				    
				    posTemp = pos;
				    addressTemp = address;
				}else{
				    pos = posTemp;
				}
				
				System.out.print(str + "\t");
			    System.out.print( pos[0] + "\t" + pos[1]);
			    System.out.println("");
		      }
		      in.close();
		    } catch (IOException e) {
		        System.err.println(e); // 에러가 있다면 메시지 출력
		        System.exit(1);
		    }		 		
	}
	
	
	private static void oneProcess() throws Exception{
	     String str = "경기도 과천시 주암동 106-6";
    	 String addressTemp = "";
    	 String[] posTemp = {"",""};
         
		String address = str;

		
		String[] pos = {"",""};
		if(!address.equals(addressTemp)){
//			System.out.print(str + "\t");
			
			//다음API
			pos = addr2coord(address);
			//네이버API
		    //pos = geocode(address);
		    
//		    System.out.print( pos[0] + "\t" + pos[1]);
//		    System.out.println("");
		    
		    posTemp = pos;
		    addressTemp = address;
		}else{
		    pos = posTemp;
		}
		
		System.out.print(str + "\t");
	    System.out.print( pos[0] + "\t" + pos[1]);
	    System.out.println("");

	 		
	}
	
	/**
	 * 
	 * 다음API로 찾기
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public static String[] addr2coord(String address) throws Exception {

		// Trust own CA and all self-signed certs
		SSLContext sslcontext = SSLContexts.custom().build();
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		
		String encodeAddress = URLEncoder.encode(address, "utf-8");

		//다음으로
		String webUrl = "http://apis.daum.net/local/geo/addr2coord?apikey=2b7f183051a0cc805e084c318c023f2525c279e5&output=xml&q="+ encodeAddress;

		HttpGet httpost = new HttpGet(webUrl);
		
		//System.out.println("Executing request " + httpost.getRequestLine());
		CloseableHttpResponse response = httpclient.execute(httpost);
		

//		System.out.println("----------------------------------------");
//		System.out.println(response.getStatusLine());

		String responseAsString = EntityUtils.toString(response.getEntity());
		System.out.println(responseAsString);		

		String[] pos = {"",""};
		
		try {

			String xml = responseAsString;
			InputSource is = new InputSource(new StringReader(xml));
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);

			// xpath 생성
			XPath xpath = XPathFactory.newInstance().newXPath();

			String expression = "//*/item";
			NodeList cols = (NodeList) xpath.compile(expression).evaluate(document, XPathConstants.NODESET);
			for (int idx = 0; idx < cols.getLength(); idx++) {

				expression = "//*/point_x";
				String point_x = xpath.compile(expression).evaluate(document);
//				System.out.println("point_x................" + point_x);
				pos[0] = point_x;
				expression = "//*/point_y";
				String point_y = xpath.compile(expression).evaluate(document);
//				System.out.println("point_y................" + point_y);
				pos[1] = point_y;
			}
			
			if(pos[0].length() == 0 && address.length() > 0){
				 String str2 = address.substring(0, address.length()-1);
				 pos = addr2coord(str2);
			}

		} finally {
			response.close();
		}

		return pos;
	}
	

	/**
	 * 네이버API로 찾기
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public static String[] geocode(String address) throws Exception {

		// Trust own CA and all self-signed certs
		SSLContext sslcontext = SSLContexts.custom().build();
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		
		String encodeAddress = URLEncoder.encode(address, "utf-8");

		
		
		
		//네이버로
		String webUrl = "https://openapi.naver.com/v1/map/geocode?clientId=PlnTff1iE54U70Pu9g_f&query="+ encodeAddress;
		HttpGet httpost = new HttpGet(webUrl);
		
		
		httpost.setHeader("X-Naver-Client-Id", "DI2ChvuX3nsmn0Hjeqa8");
		httpost.setHeader("X-Naver-Client-Secret", "JJ3cAt7A5u");				
		
		
		//System.out.println("Executing request " + httpost.getRequestLine());
		CloseableHttpResponse response = httpclient.execute(httpost);
		

//		System.out.println("----------------------------------------");
//		System.out.println(response.getStatusLine());

		String responseAsString = EntityUtils.toString(response.getEntity());
		//System.out.println(address);		
		System.out.println(responseAsString);		

		String[] pos = {"",""};
		
		try {

			String json = responseAsString;
			
			JSONObject object = (JSONObject)JSONValue.parse(json);
			//System.out.println(object);
			
			JSONObject result =  (JSONObject)object.get("result");
			
			if(result != null){
				JSONArray items =  (JSONArray) result.get("items");
				
				JSONObject item = (JSONObject)items.get(0);
				
				JSONObject point =  (JSONObject)item.get("point");
				Object x =  (Object)point.get("x");
				Object y =  (Object)point.get("y");
				
				pos[0] = x.toString();
				pos[1] = y.toString();
			}
			
			if(pos[0].length() == 0 && address.length() > 0){
				 String str2 = address.substring(0, address.length()-1);
				 pos = geocode(str2);
			}
			
		} finally {
			response.close();
		}

		return pos;
	}
	
	

}
