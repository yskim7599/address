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
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/*
 * 
 * 
 */

public class GetGeoCode{
	static Logger  log = Logger.getLogger("GetGeoCode");
	
	static String DAUM_APIKEY="2b7f183051a0cc805e084c318c023f2525c279e5";
	static String NAVER_CLIENTID="PlnTff1iE54U70Pu9g_f";
	static String GOOGLE_KEY="AIzaSyChCMZElQ5jm9CmsXqJlmyf8344DxRu7TM";

	
	
	public static void main(String args[]) throws Exception {
		//muiltProcess();
		oneProcess();
	}

	private static void muiltProcess() throws Exception{
		 String filePath ="D:/temp/a111.txt";
		 try {
		     
             BufferedReader in = new BufferedReader(new FileReader(filePath));
 	         String str = "";
 	    	 String addressTemp = "";
 	    	 String[] posTemp = {"","","","",""};
 	         
		      while ((str = in.readLine()) != null) {
		        //System.out.println(str);
				String address = str;

				
				String[] pos = {"","","","",""};
				if(!address.equals(addressTemp)){
					pos = getGeoCode(address);
				    
				    posTemp = pos;
				    addressTemp = address;
				}else{
				    pos = posTemp;
				}
				
				String outStr = "";
				String linkUrl ="http://www.hifive.go.kr/daumMap1.jsp?x_pos="+pos[0]+"&y_pos="+pos[1];
				outStr =  "\t" + pos[0] + "\t" + pos[1]+ "\t" + pos[2]+ "\t" + pos[3]+ "\t" + linkUrl+ "\t" + pos[4];
			    
			    
			    log.info(outStr);
		      }
		      in.close();
		    } catch (IOException e) {
		        System.err.println(e); // 에러가 있다면 메시지 출력
		        System.exit(1);
		    }		 		
	}
	
	
	private static void oneProcess() throws Exception{
	     
		
		String str = "제주특별자치도 서귀포시 서홍로 146";
    	
		
		String addressTemp = "";
    	String[] posTemp = {"","","","",""};
         
		String address = str;

		
		String[] pos = {"","","","",""};
		if(!address.equals(addressTemp)){
//			System.out.print(str + "\t");
			
			pos = getGeoCode(address);

			posTemp = pos;
		    addressTemp = address;
		}else{
		    pos = posTemp;
		}
		
		System.out.print(str + "\t");
		String linkUrl ="http://www.hifive.go.kr/daumMap1.jsp?x_pos="+pos[0]+"&y_pos="+pos[1];
	    System.out.print( pos[0] + "\t" + pos[1]+ "\t" + pos[2]+ "\t" + pos[3]+ "\t" + linkUrl+ "\t" + pos[4]);
	    System.out.println("");

	 		
	}
	
	
	
	public static String[] getGeoCode(String address) throws Exception {
		String[] pos = {"","","","",""};

		String[] A_address = address.split(" ");
		
		
		
		pos = getDaumGeoCode(address);
		if(pos[0].length() == 0){
		    pos = getNaverGeoCode(address);
			if(pos[0].length() == 0){
				pos = getGoogleGeoCode(address);
			}
		}
		
		
		if(pos[0].length() == 0 && address.length() > 0){
			String addTemp = "";
			for(int i=0;i<A_address.length-1;i++){
				addTemp += A_address[i]+" ";
			}
			pos = getGeoCode(addTemp);
		}
		pos[4] = address;
		return pos;
	}
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 다음API로 찾기
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public static String[] getDaumGeoCode(String address) throws Exception {
	

		// Trust own CA and all self-signed certs
		SSLContext sslcontext = SSLContexts.custom().build();
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		
		String encodeAddress = URLEncoder.encode(address, "utf-8");

		//다음으로
		String webUrl = "http://apis.daum.net/local/geo/addr2coord?apikey="+DAUM_APIKEY + "&output=xml&q="+ encodeAddress;

		HttpGet httpost = new HttpGet(webUrl);
		
		//System.out.println("Executing request " + httpost.getRequestLine());
		CloseableHttpResponse response = httpclient.execute(httpost);
		

//		System.out.println("----------------------------------------");
//		System.out.println(response.getStatusLine());

		String responseAsString = EntityUtils.toString(response.getEntity());
		//System.out.println(responseAsString);		

		String[] pos = {"","","","",""};
		
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
				
				expression = "/channel/item/title";
				String add = xpath.compile(expression).evaluate(document);
				
				pos[2] = add;
				pos[3] = "DAUM";
				
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
	public static String[] getNaverGeoCode(String address) throws Exception {

		// Trust own CA and all self-signed certs
		SSLContext sslcontext = SSLContexts.custom().build();
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		
		String encodeAddress = URLEncoder.encode(address, "utf-8");

		
		
		
		//네이버로
		String webUrl = "https://openapi.naver.com/v1/map/geocode?clientId="+NAVER_CLIENTID+"&query="+ encodeAddress;
		HttpGet httpost = new HttpGet(webUrl);
		
		
		httpost.setHeader("X-Naver-Client-Id", "DI2ChvuX3nsmn0Hjeqa8");
		httpost.setHeader("X-Naver-Client-Secret", "JJ3cAt7A5u");				
		
		//System.out.println("address " + address);
		//System.out.println("Executing request " + httpost.getRequestLine());
		CloseableHttpResponse response = httpclient.execute(httpost);
		

//		System.out.println("----------------------------------------");
//		System.out.println(response.getStatusLine());

		String responseAsString = EntityUtils.toString(response.getEntity());
		//System.out.println(address);		
		//System.out.println(responseAsString);		

		String[] pos = {"","","","",""};
		
		try {

			String json = responseAsString;
			
			JSONObject object = (JSONObject)JSONValue.parse(json);
			//System.out.println(object);
			
			JSONObject result =  (JSONObject)object.get("result");
			
			if(result != null){
				JSONArray items =  (JSONArray) result.get("items");
				
				JSONObject item = (JSONObject)items.get(0);
				Object add =  (Object)item.get("address");

				
				JSONObject point =  (JSONObject)item.get("point");
				Object x =  (Object)point.get("x");
				Object y =  (Object)point.get("y");
				
				pos[0] = x.toString();
				pos[1] = y.toString();
				pos[2] = add.toString();
				pos[3] = "NAVER";
			}
			
			
			
		} finally {
			response.close();
		}

		return pos;
	}
	
	
	

	/**
	 * Google API로 찾기
	 * 
	 * @param address
	 * @return
	 * @throws Exception
	 */
	public static String[] getGoogleGeoCode(String address) throws Exception {

		// Trust own CA and all self-signed certs
		SSLContext sslcontext = SSLContexts.custom().build();
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		
		String encodeAddress = URLEncoder.encode(address, "utf-8");

		
		
		
		//Google로
		//String webUrl = "https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyChCMZElQ5jm9CmsXqJlmyf8344DxRu7TM&address="+ encodeAddress;
		
		//영상꺼
		String webUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?language=ko&key="+ GOOGLE_KEY + "&query="+ encodeAddress;

		//승훈꺼
		//String webUrl = "https://maps.googleapis.com/maps/api/place/textsearch/json?language=ko&key=AIzaSyAyxzn6S8_YaXnKzSdYfjQRustyGY2usGc&query="+ encodeAddress;
		
		
		HttpGet httpost = new HttpGet(webUrl);
		
		
		//System.out.println("Google address " + address);
		//System.out.println("Executing request " + httpost.getRequestLine());
		CloseableHttpResponse response = httpclient.execute(httpost);
		

//		System.out.println("----------------------------------------");
//		System.out.println(response.getStatusLine());

		String responseAsString = EntityUtils.toString(response.getEntity());
		//System.out.println(address);		
		//System.out.println(responseAsString);		

		String[] pos = {"","","","",""};
		
		try {

			String json = responseAsString;
			
			JSONObject object = (JSONObject)JSONValue.parse(json);
			//System.out.println(object);
			
			JSONArray results =  (JSONArray)object.get("results");
			
			if(results.size() > 0){
				JSONObject item = (JSONObject)results.get(0);
				
				//System.out.println(item);
				
				Object formatted_address =  (Object) item.get("formatted_address");
				
				JSONObject geometry =  (JSONObject) item.get("geometry");

				//System.out.println(geometry);

				JSONObject location =  (JSONObject) geometry.get("location");

				//System.out.println("location"+location);

				
				Object y =  (Object) location.get("lat");
				
				//System.out.println(y);
				
				Object x =  (Object) location.get("lng");
				
				//System.out.println(x);
				
				
				pos[0] = x.toString();
				pos[1] = y.toString();
				pos[2] = formatted_address.toString();
				pos[3] = "GOOGLE";
			}
			
			
			
		} finally {
			response.close();
		}

		return pos;
	}
	
	
	

}
