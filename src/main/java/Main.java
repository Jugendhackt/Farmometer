import net.freeutils.httpserver.HTTPServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Scanner;

public class Main {
	static public JSONArray blocks = new JSONArray();
	static Database db = null;
	
	public static void main(String[] args) throws Exception {
		Log.status("[1/5] connect to database");
		db = new Database();
		Log.status("[2/5] start background thread");
		Thread blocksUpdate = new Thread(() -> {
			String last_update = null;
			boolean noNewUpdate = false;
			
			while (true) {
				try {
					blocks = HttpClient.responseAsJsonA(HttpClient.sendGet(
							"https://io.adafruit.com/api/v2/m4schini/dashboards/hackathon1/blocks"));
					if (getLastUpdate().equals(last_update)) {
						if (!noNewUpdate)
						Log.warning("No new update. Last update: " + last_update);
						noNewUpdate = true;
					} else {
						last_update = getLastUpdate();
						addToDB();
						//Log.status("updated information\nTemp: " + getTemp() + "\nHumidity: " + getHum());
						noNewUpdate = false;
					}
					
					
					//System.out.println(blocks);
					Thread.sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
					Log.critical("Something happened");
				}
			}
		});
		blocksUpdate.start();
		
		Log.status("[3/5] initialize http server");
		HTTPServer server = new HTTPServer(1337);
		HTTPServer.VirtualHost host = server.getVirtualHost(null);
		//All responses have statuses in header to check for errors
		
		//param: name, email, ...
		host.addContext("/get/data", new getDataHandler());
		
		
		try {
			Log.status("[4/5] start http server");
			server.start();
		} catch (IOException e) {
			Log.critical("httpserver start failed");
			Log.critical("Aborting Server");
			System.exit(-1);
		}
		Log.success("[5/5] Everything initalized\n");
		
		Log.success("Hello Admin");
		Scanner scanner = new Scanner(System.in);
		while (true) {
			String consoleInput = scanner.nextLine();
			try {
				switch (consoleInput) {
					case "temp":
						System.out.println(getTemp());
						break;
					case "humidity":
						System.out.println(getHum());
						break;
					case "exit":
						System.exit(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(blocks);
			}
		}
	}
	
	private static class getDataHandler implements HTTPServer.ContextHandler {
		@Override
		public int serve(HTTPServer.Request request, HTTPServer.Response response) throws IOException {
			JSONObject responseObject = new JSONObject();
			JSONObject header = new JSONObject();
			JSONObject infos = new JSONObject();
			
			header.put("last-updated", /*new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date())*/ getLastUpdate());
			infos.put("temp", getTemp());
			infos.put("humidity", getHum());
			
			
			responseObject.put("header", header);
			responseObject.put("data", infos);
			
			sendResponse(response, 200, responseObject);
			return 0;
		}
	}
	
	static void sendResponse(HTTPServer.Response response, int status, JSONObject responseObject) {
		response.getHeaders().add("Content-Type", "application/json");
		response.getHeaders().add("Access-Control-Allow-Origin", "*");
		try {
			response.send(status, responseObject.toString());
		} catch (IOException e) {
			Log.error("Response cannot be sent");
		}
	}
	
	/**
	 * @param response httpserver response
	 * @return json with status 400, http status 400
	 * @see      HTTPServer.Response
	 */
	private static Integer sendBadApiReq(HTTPServer.Response response) {
		Log.error("[API] bad request");
		
		JSONObject object = new JSONObject();
		object.put("header", new JSONObject().put("status", 400));
		
		sendResponse(response, 400, object);
		return 400;
	}
	
	private static void addToDB() {
		try {
			db.execute("INSERT INTO vokTrainer.history VALUES (?,?,?)",
					getLastUpdate(),
					getTemp(),
					getHum()
			);
		} catch (Exception e) {
			Log.error("SQLIntegrityConstraintViolationException");
		}
	}
	
	private static Float getTemp() {
		return blocks
				.getJSONObject(1)
				.getJSONArray("block_feeds")
				.getJSONObject(0)
				.getJSONObject("feed")
				.getFloat("last_value");
	}
	
	private static Float getHum() {
		return blocks
				.getJSONObject(0)
				.getJSONArray("block_feeds")
				.getJSONObject(0)
				.getJSONObject("feed")
				.getFloat("last_value");
	}
	
	private static String getLastUpdate() {
		return blocks
				.getJSONObject(0)
				.getJSONArray("block_feeds")
				.getJSONObject(0)
				.getJSONObject("feed")
				.getString("updated_at");
	}
}
