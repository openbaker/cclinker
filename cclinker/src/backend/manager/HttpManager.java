/**
 * 
 */
package backend.manager;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author schiend
 *
 */
public class HttpManager {
	
	// Http access types
	public static String TYPE_JSON_GET = "json_get";
	public static String TYPE_JSON_POST = "json_post";
	public static String TYPE_POST = "post";
	public static String TYPE_GET = "get";
	
	/**
	 * Convert map to NameValuePair
	 * 
	 * @return
	 */
	public static List<NameValuePair> convertMapToNameValuePair(Map<String, String> pairs, boolean encode) {
		// convert to name value
		List<NameValuePair> nvpList = new ArrayList<>(pairs.size());
		
		// go through params, encode URL ready and add to nvp list
		for (Map.Entry<String, String> entry: pairs.entrySet()) {
			try {
				String value = entry.getValue();
				
				// encode if needed
				if (encode == true) {
					value = URLEncoder.encode(value,
							ConfigManager.getInstance().getManagerHttpURLEncoding());
				}
				
				pairs.replace(entry.getKey(), value);
				
				nvpList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return nvpList;
	}
	
	/**
	 * @see convertMapToNameValuePair(Map<String, String> pairs, boolean encode)
	 * 
	 * @param pairs
	 * @return
	 */
	public static List<NameValuePair> convertMapToNameValuePair(Map<String, String> pairs) {
		return convertMapToNameValuePair(pairs, true);
	}
	
	/**
	 * Build URL
	 * 
	 * @param scheme
	 * @param host
	 * @param path
	 * @param params
	 * @param encode
	 * @return
	 */
	public static String buildURL(String scheme, String host,
			String path, Map<String, String> params, boolean encode) {
		String url = new String();
		
		try {
			// build base and path
			URIBuilder uriBuilder = new URIBuilder();
			uriBuilder.setScheme(scheme);
			uriBuilder.setHost(host);
			uriBuilder.setPath(path);
			
			ConfigManager.getInstance().getManagerHttpURLEncoding();
			
			if (params != null) {
				// add params
				uriBuilder.addParameters(
						HttpManager.convertMapToNameValuePair(params, encode));
			}

			// build URL
			url = uriBuilder.build().toURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return url;
	}
	
	/**
	 * @see buildURL(String scheme, String host, String path, Map<String, String> params, boolean encode)
	 * 
	 * @param scheme
	 * @param host
	 * @param path
	 * @param params
	 * @return
	 */
	public static String buildURL(String scheme, String host, String path, Map<String, String> params) {
		return HttpManager.buildURL(scheme, host, path, params, true);
	}
	
	/**
	 * @see buildURL(String scheme, String host, String path, Map<String, String> params, boolean encode)
	 * 
	 * @param scheme
	 * @param host
	 * @param path
	 * @return
	 */
	public static String buildURL(String scheme, String host, String path) {
		return HttpManager.buildURL(scheme, host, path, null, true);
	}
	
	/**
	 * Get HttpEntity from URL
	 * 
	 * @param url
	 * @param type
	 * @param args
	 * @param encode
	 * @return
	 */
	public static HttpEntity getHttpEntityFromURL(
			String url, String type, Map<String, String> args, boolean encode) {
		HttpEntity entity = null;
		HttpResponse response;
		
		try {
			// retrieve abstracts from Pubmed
			HttpClient httpclient = HttpClients.createDefault();
			
			if (type.equals(TYPE_JSON_POST)) {
				HttpPost httppost = new HttpPost(url);
				httppost.setHeader("Content-Type",
						ConfigManager.getInstance().getManagerHttpHttppostJson());
				//httppost.setHeader("Content-Type", "Accept-Encoding: gzip");
				
				response = httpclient.execute(httppost);
			} else if (type.equals(TYPE_JSON_GET)) {
					HttpGet httpget = new HttpGet(url);
					httpget.setHeader("Accept",
							ConfigManager.getInstance().getManagerHttpHttpgetJson());
					
					response = httpclient.execute(httpget);
			} else if (type.equals(TYPE_POST)) {
				HttpPost httppost = new HttpPost(url);
				httppost.setHeader("Content-Type",
						ConfigManager.getInstance().getManagerHttpHttppostDefault());
				
				// add arguments
				httppost.setEntity(new UrlEncodedFormEntity(
						HttpManager.convertMapToNameValuePair(args, encode),
						ConfigManager.getInstance().getManagerHttpURLEncoding()));
				
				response = httpclient.execute(httppost);
			} else {
				HttpGet httpget = new HttpGet(url);
				response = httpclient.execute(httpget);
			}

			entity = response.getEntity();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return entity;
	}
	
	/**
	 * @see getHttpEntityFromURL(String url, String type, Map<String, String> args, boolean encode)
	 * 
	 * @param url
	 * @param type
	 * @param args
	 * @return
	 */
	public static HttpEntity getHttpEntityFromURL(
			String url, String type, Map<String, String> args) {
		return getHttpEntityFromURL(url, type, args, true);
	}
	
	/**
	 * @see getHttpEntityFromURL(String url, String type, Map<String, String> args, boolean encode)
	 * 
	 * @param url
	 * @param type
	 */
	public static HttpEntity getHttpEntityFromURL(String url, String type) {
		return getHttpEntityFromURL(url, type, null, true);
	}
	
	/**
	 * @see getHttpEntityFromURL(String url, String type, Map<String, String> args, boolean encode)
	 * 
	 * @param url
	 */
	public static HttpEntity getHttpEntityFromURL(String url) {
		return getHttpEntityFromURL(url, TYPE_GET);
	}
	
	/**
	 * Match pattern
	 * 
	 * @param pattern
	 * @param input
	 * @return
	 */
	public static String matchPattern(String to_find, String input) {
		String result = null;
		
		Pattern pattern = Pattern.compile(to_find);
		Matcher m = pattern.matcher(input); m.find();
		result = m.group(0);
		
		return result;
	}
	
	/**
	 * Get a http response and convert to XML document
	 * 
	 * @param url
	 * @return
	 */
	public static Document getXMLfromURL(String url) {
		Document document = null;
		
		try {
			// retrieve XML
			HttpEntity entity = getHttpEntityFromURL(url);
			
			// parse XML
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
			        .newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			
			String entityToParse = EntityUtils.toString(entity);
			document = documentBuilder.parse(
					new InputSource(new StringReader(entityToParse)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return document;
	}
	
	/**
	 * Get JSON document from url
	 * 
	 * @param url
	 * @param type
	 * @param replace
	 * @return
	 */
	public static JSONObject getJSONfromURL(String url, String type, List<String[]> toReplace) {
		JSONObject json = null;

		try {
			// retrieve JSON
			HttpEntity entity = getHttpEntityFromURL(url, type);
			
			String strEntity = EntityUtils.toString(entity);
			
			// replace string
			if (toReplace != null) {
				for (String[] pair: toReplace) {
					strEntity = strEntity.replace(pair[0], pair[1]);
				}
			}
			
			// parse JSON
			json = new JSONObject(strEntity);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	/**
	 * @see getJSONfromURL(String url, String type, Map<String, String> replace)
	 * 
	 * @param url
	 * @param type
	 * @param replace
	 * @return
	 */
	public static JSONObject getJSONfromURL(String url, String type) {
		return getJSONfromURL(url, type, null);
	}
	
}
