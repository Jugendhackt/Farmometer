import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HttpClient {
	private static final String USER_AGENT = "Mozilla/5.0";
	
	// HTTP GET request
	static HttpResponse sendGet(String url) throws Exception {
		//String url = "http://www.google.com/search?q=developer";
		
		org.apache.http.client.HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		
		// add request header
		request.addHeader("User-Agent", USER_AGENT);
		request.addHeader("X-AIO-Key", Keys.X_AIO_KEY); //TODO USELESS IF USED IN SOMETHING ELSE THAN FARMOMETER
		
		HttpResponse response = client.execute(request);
		
		//Log.status("Sending 'GET' request to URL : " + url);
		return response;
		
	}
	
	// HTTP POST request
	static HttpResponse sendPost(String url) throws IOException {
		org.apache.http.client.HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		
		// add header
		post.setHeader("User-Agent", USER_AGENT);
		
		HttpResponse response = client.execute(post);
		return response;
	}
	
	static void sendPost(String url, HashMap<String, String> httpParameters) throws IOException {
		org.apache.http.client.HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		
		// add header
		post.setHeader("User-Agent", USER_AGENT);
		
		//LEGACY
		//List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		//urlParameters.add(new BasicNameValuePair("sn", "C02G8416DRJM"));
		//urlParameters.add(new BasicNameValuePair("cn", ""));
		//urlParameters.add(new BasicNameValuePair("locale", ""));
		//urlParameters.add(new BasicNameValuePair("caller", ""));
		//urlParameters.add(new BasicNameValuePair("num", "12345"));
		List<NameValuePair> urlParameters = new ArrayList<>();
		String[] httpParamsKeys = (String[]) httpParameters.entrySet().toArray();
		
		for (int i = 0; i < httpParameters.size(); i++) {
			urlParameters.add(new BasicNameValuePair(httpParamsKeys[i], httpParameters.get(httpParamsKeys[i])));
		}
		
		post.setEntity(new UrlEncodedFormEntity(urlParameters));
		
		HttpResponse response = client.execute(post);
		Log.status("Sending 'POST' request to URL : " + url);
		Log.status("Post parameters : " + post.getEntity());
		Response(response);
		
	}
	
	private static void Response(HttpResponse response) throws IOException {
		Log.status("Response Code : " +
				response.getStatusLine().getStatusCode());
		
		BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));
		
		StringBuffer result = new StringBuffer();
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		
		Log.status(result.toString());
	}
	
	static JSONObject responseAsJsonO(HttpResponse response) throws IOException {
		return
				new JSONObject(
				new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent()
						)
				).readLine());
	}
	
	static JSONArray responseAsJsonA(HttpResponse response) throws IOException {
		return
				new JSONArray(
						new BufferedReader(
								new InputStreamReader(
										response.getEntity().getContent()
								)
						).readLine());
	}
	
}
