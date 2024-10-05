package weatherService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import org.json.JSONArray;
import org.json.JSONObject;

public class GETClient {

	/*
	 * Helper method to build header for response. Inputs arguments are the host,
	 * the station ID, the lamport timestamp and the process ID. Returns a String of
	 * the header.
	 */
	private String buildHeader(String host, String stationID, String timeStamp, String PID) {
		return "GET / HTTP/1.1\r\nHost: " + host + "\r\nAccept: application/json\r\nStationID: " + stationID
				+ "\r\nLamportTimestamp: " + timeStamp + "\r\nProcessID: " + PID + "\r\n\r\n";
	}

	/*
	 * Helper method to parse and display weather data. Input argument is the
	 * weather data in String form. This method uses a string builder, JSONObjects
	 * and a JSONArray to put together the weather data output to the GET client.
	 */
	private void parseAndDisplayWeatherData(String data) {
		try {
			JSONObject obj = new JSONObject(data);
			StringBuilder weatherData = new StringBuilder("\nStation ID: " + obj.getString("station_id") + "\n\n");
			JSONArray array = obj.getJSONArray("data");
			for (int i = 0; i < array.length(); i++) {
				if (i != 0) {
					weatherData.append("\n");
				}
				try {
					JSONObject json = array.getJSONObject(i);
					weatherData.append("Id: ").append(json.getString("id")).append("\n");
					weatherData.append("Name: ").append(json.getString("name")).append("\n");
					weatherData.append("State: ").append(json.getString("state")).append("\n");
					weatherData.append("Timezone: ").append(json.getString("time_zone")).append("\n");
					weatherData.append("Latitude: ").append(json.getDouble("lat")).append("\n");
					weatherData.append("Longitude: ").append(json.getDouble("lon")).append("\n");
					weatherData.append("Local Date Time: ").append(json.getString("local_date_time")).append("\n");
					weatherData.append("Local Date Time Full: ").append(json.getString("local_date_time_full"))
							.append("\n");
					weatherData.append("Air Temperature: ").append(json.getDouble("air_temp")).append("\n");
					weatherData.append("Apparent Temperature: ").append(json.getDouble("apparent_t")).append("\n");
					weatherData.append("Cloud: ").append(json.getString("cloud")).append("\n");
					weatherData.append("Dew Point: ").append(json.getDouble("dewpt")).append("\n");
					weatherData.append("Pressure: ").append(json.getDouble("press")).append("\n");
					weatherData.append("Relative Humidity: ").append(json.getDouble("rel_hum")).append("\n");
					weatherData.append("Wind Direction: ").append(json.getString("wind_dir")).append("\n");
					weatherData.append("Wind Speed Kmh: ").append(json.getDouble("wind_spd_kmh")).append("\n");
					weatherData.append("Wind Speed Kt: ").append(json.getDouble("wind_spd_kt")).append("\n");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			System.out.println(weatherData);
		} catch (Exception e) {
			System.err.println("Error parsing weather data: " + e.getMessage());
		}
	}

	/*
	 * Main method of the main.weatherService.GETClient class. Command-line input
	 * arguments are the server URI and optionally, a second input argument which
	 * tells the aggregation server what weather data to retrieve via station ID. If
	 * the second argument is not passed to main, the aggregation server will supply
	 * the most recent data.
	 * 
	 * Method sends GET requests to the aggregation server via the socket. The
	 * response received by the client is printed to the user in both this method
	 * and by calling the helper method "parseAndDisplayWeatherData".
	 */
	public static void main(String[] args) {
		GETClient client = new GETClient();
		URI uri;
		String serverName = "localhost";
		int serverPort = 4567;
		int PID = LamportClock.getNewPID();
		LamportClock clock = new LamportClock(PID, 0);
		if (args.length < 1 || args.length > 2) {
			System.out.println("Usage: java main.weatherService.GETClient <serverURI> [<stationID>]");
			System.exit(1);
		}
		try {
			uri = new URI(args[0]);
			serverName = uri.getHost();
			if (uri.getPort() != -1) {
				serverPort = uri.getPort();
			}
		} catch (URISyntaxException e) {
			System.out.println("Invalid server URI: " + e.getMessage());
			System.exit(1);
		}
		String stationID;
		if (args.length == 2) {
			stationID = args[1];
		} else {
			stationID = "-1";
		}
		try (Socket socket = new Socket(serverName, serverPort);
				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
			System.out.println("Connected to " + serverName + " on port " + serverPort);
			clock.tick();
			String header = client.buildHeader(serverName, stationID, Integer.toString(clock.getTimeStamp()),
					Integer.toString(clock.getProcessID()));
			pw.write(header);
			pw.flush();
			String response, data = null;
			boolean isOK = false, hasBody = false, notFound = false, noDataForRecent = false;
			while ((response = br.readLine()) != null) {
				if (response.contains("200")) {
					isOK = true;
				} else if (response.contains("404")) {
					notFound = true;
				} else if (response.contains("401")) {
					noDataForRecent = true;
				} else if (response.contains("Content-Length")) {
					hasBody = !response.split(":")[1].trim().equals("0");
				} else if (hasBody) {
					System.out.println(response);
					response = br.readLine();
					System.out.println(response);
					response = br.readLine();
					System.out.println(response);
					data = response;
					break;
				}
				System.out.println(response);
			}
			// Parse and display weather data from response or print errors
			if (isOK && hasBody) {
				client.parseAndDisplayWeatherData(data);
			} else if (notFound) {
				System.err.println("Data for station ID " + stationID + " not found.");
			} else if (noDataForRecent) {
				System.err.println("No data in database, cannot get most recent PUT.");
			} else {
				// Must have returned 400 status code
				System.err.println("Invalid request or response from the server.");
			}
		} catch (Exception e) {
			System.err.println("Error connecting to server: " + e.getMessage());
		}
	}
}