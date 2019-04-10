import org.json.JSONArray;

import java.util.Scanner;

public class Main {
	static public JSONArray blocks = new JSONArray();
	
	public static void main(String[] args) throws Exception {
		
		//float Temp = blocks
		//		.getJSONObject(1)
		//		.getJSONArray("block_feeds")
		//		.getJSONObject(0).getJSONObject("feed")
		//		.getFloat("last_value");
		//System.out.println(Temp);
		
		Thread blocksUpdate = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						blocks = HttpClient.responseAsJsonA(HttpClient.sendGet("https://io.adafruit.com/api/v2/m4schini/dashboards/hackathon1/blocks"));
						Thread.sleep(5000);
					} catch (Exception e) {
						e.printStackTrace();
						Log.critical("Something happened");
					}
					Log.status("updated information\nTemp: " + getTemp() + "\nHumidity: " + getHum());
				}
			}
		});
		blocksUpdate.start();
		
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
	
	private static float getTemp() {
		return blocks
				.getJSONObject(1)
				.getJSONArray("block_feeds")
				.getJSONObject(0)
				.getJSONObject("feed")
				.getFloat("last_value");
	}
	
	private static float getHum() {
		return blocks
				.getJSONObject(0)
				.getJSONArray("block_feeds")
				.getJSONObject(0)
				.getJSONObject("feed")
				.getFloat("last_value");
	}
}
