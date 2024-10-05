package weatherService;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContentServer extends Thread {

	URI uri; // Uniform resource identifier
	String serverName = "localhost"; // Default server name
	String stationID = ""; // Initialise string for station ID
	int port = 4567; // Default port
	WeatherDataEntry[] entries = null; // Initialise weather data entry array
	LamportClock clock = null; // Initialise lamport clock to null
	int PID = -1; // Initialise PID

	/*
	 * Helper method to start the server. The input arguments to this method are the
	 * command-line inputs given to main and the main.weatherService.ContentServer
	 * instance. The first command-line input is the server URI, the second is the
	 * station ID, and the third is the input file containing weather data.
	 * 
	 * This method sets up the lamport clock, server host and port, station ID as
	 * well as the data entries before starting the thread (which invokes run()).
	 */
	private void startServer(String[] args, ContentServer server) throws URISyntaxException {
		PID = LamportClock.getNewPID();
		clock = new LamportClock(PID, 0);
		uri = new URI(args[0]);
		serverName = uri.getHost();
		if (uri.getPort() != -1) {
			port = uri.getPort();
		}
		stationID = args[1];
		entries = server.getEntries(args[2], server);
		if (entries == null) {
			throw new RuntimeException(
					"Invalid input file from station: " + stationID + " with " + "filename: " + args[2]);
		}
		this.start();
	}

	// Helper method to stop the server. Interrupts the thread.
	private void stopServer() {
		this.interrupt();
	}

	/*
	 * Helper method to get the weather data entries. Method inputs are the file
	 * name of the weather data which is supplied as a command-line input to main,
	 * and the instance of the Content Server. Method returns an array of
	 * main.weatherService.WeatherDataEntry objects.
	 * 
	 * Method reads the weather data from the input file. Will return null if the
	 * file is invalid.
	 */
	private WeatherDataEntry[] getEntries(String fileName, ContentServer server) {
		File file = new File(fileName);
		try (FileInputStream fileInputStream = new FileInputStream(file);
				InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

			int numEntries = server.getNumEntries(fileName);
			entries = new WeatherDataEntry[numEntries];
			String line;
			int index = 0;
			boolean isStartOfEntry = true;

			while ((line = bufferedReader.readLine()) != null) {
				if (isStartOfEntry) {
					if (!line.contains("id:")) {
						System.err.println("Error: Invalid file, missing id field.");
						bufferedReader.close();
						return null;
					}
					entries[index] = new WeatherDataEntry();
					parseID(entries[index], line);
					isStartOfEntry = false;
				} else {
					if (!parseField(entries[index], line)) {
						System.err.println("Error: Invalid field name.");
						bufferedReader.close();
						return null;
					}
					if (line.contains("wind_spd_kt:")) {
						// Final part of entry, go to next entry
						index++;
						isStartOfEntry = true;
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return entries;
	}

	/*
	 * Helper method to get the number of entries in the input file. Method takes a
	 * single input argument which is the name of the input file given to main from
	 * the command-line. Method will simply read the number of times "id:" occurs in
	 * the file, which corresponds to the number of separate entries.
	 */
	private int getNumEntries(String fileName) {
		int length = 0;
		try (FileInputStream fileInputStream = new FileInputStream(fileName);
				DataInputStream dataInputStream = new DataInputStream(fileInputStream);
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.contains("id:")) {
					length++;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return length;
	}

	/*
	 * Helper method to parse the ID from an entry. Input arguments are the
	 * main.weatherService.WeatherDataEntry object and the current line. Method will
	 * set the ID in the specified entry.
	 */
	private void parseID(WeatherDataEntry entry, String line) {
		String[] splitLine = line.split(":");
		entry.setID(splitLine[1]);
	}

	/*
	 * Helper method to parse fields from an entry, will return false if invalid.
	 * Input arguments are the main.weatherService.WeatherDataEntry object and the
	 * current line. Method will set the parameter found within the given line for
	 * the specified entry.
	 */
	private boolean parseField(WeatherDataEntry entry, String line) {
		String[] splitLine;
		if (line.contains("name:")) {
			splitLine = line.split(":");
			entry.setName(splitLine[1]);
		} else if (line.contains("state:")) {
			splitLine = line.split(":");
			entry.setState(splitLine[1]);
		} else if (line.contains("time_zone:")) {
			splitLine = line.split(":");
			entry.setTime_zone(splitLine[1]);
		} else if (line.contains("lat:")) {
			splitLine = line.split(":");
			entry.setLat(Double.parseDouble(splitLine[1]));
		} else if (line.contains("lon:")) {
			splitLine = line.split(":");
			entry.setLon(Double.parseDouble(splitLine[1]));
		} else if (line.contains("local_date_time_full:")) {
			splitLine = line.split(":");
			entry.setLocal_date_time_full(splitLine[1]);
		} else if (line.contains("local_date_time:")) {
			splitLine = line.split(":");
			entry.setLocal_date_time(splitLine[1] + ":" + splitLine[2]);
		} else if (line.contains("air_temp:")) {
			splitLine = line.split(":");
			entry.setAir_temp(Double.parseDouble(splitLine[1]));
		} else if (line.contains("apparent_t:")) {
			splitLine = line.split(":");
			entry.setApparent_t(Double.parseDouble(splitLine[1]));
		} else if (line.contains("cloud:")) {
			splitLine = line.split(":");
			entry.setCloud(splitLine[1]);
		} else if (line.contains("dewpt:")) {
			splitLine = line.split(":");
			entry.setDewpt(Double.parseDouble(splitLine[1]));
		} else if (line.contains("press:")) {
			splitLine = line.split(":");
			entry.setPress(Double.parseDouble(splitLine[1]));
		} else if (line.contains("rel_hum:")) {
			splitLine = line.split(":");
			entry.setRel_hum(Double.parseDouble(splitLine[1]));
		} else if (line.contains("wind_dir:")) {
			splitLine = line.split(":");
			entry.setWind_dir(splitLine[1]);
		} else if (line.contains("wind_spd_kmh:")) {
			splitLine = line.split(":");
			entry.setWind_spd_kmh(Double.parseDouble(splitLine[1]));
		} else if (line.contains("wind_spd_kt:")) {
			splitLine = line.split(":");
			entry.setWind_spd_kt(Double.parseDouble(splitLine[1]));
		} else {
			return false;
		}
		return true;
	}

	/*
	 * Override thread run method. Will print the response information from the
	 * aggregation server to user. The header and body of the response from the
	 * aggregation server are found using the helper methods
	 * "convertWeatherDataEntryToJSON" and "buildHeader".
	 */
	@Override
	public void run() {
		try (Socket socket = new Socket(serverName, port);
				PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
			System.out.println("Connected to " + serverName + " at port " + port);
			clock.tick();
			JSONObject obj = this.convertWeatherEntryToJSON(entries, stationID, Integer.toString(clock.getTimeStamp()),
					Integer.toString(clock.getProcessID()));
			String header = buildHeader(serverName, Integer.toString((obj.toString()).length()));
			String body = obj.toString();
			output.write(header);
			output.write(body);
			output.flush();
			String aggregationServerResponse;
			while ((aggregationServerResponse = input.readLine()) != null) {
				System.out.println(aggregationServerResponse);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Helper method to convert weather entries to a JSON object. Input arguments
	 * are the weather data entries in a main.weatherService.WeatherDataEntry array,
	 * the station ID, the lamport timestamp and the process ID. Method returns a
	 * JSONObject which is the weather entry in JSON format.
	 * 
	 * This is done by looping over each entry, converting it to a JSONObject using
	 * the helper method "getJSONObject", and then adding it to a JSONArray. Once
	 * done, the resulting JSONObject containing the data, station ID, lamport
	 * timestamp, process ID and number of entries is created and returned.
	 */
	private JSONObject convertWeatherEntryToJSON(WeatherDataEntry[] entries, String stationID, String lamportTimeStamp,
			String PID) {
		JSONObject obj = new JSONObject();
		JSONArray jArray = new JSONArray();
		try {
			for (WeatherDataEntry entry : entries) {
				if (entry == null) {
					break;
				}
				JSONObject entryJSON = getJSONObject(entry);
				jArray.put(entryJSON);
			}
			obj.put("data", jArray);
			obj.put("station_id", stationID);
			obj.put("lamport_timestamp", lamportTimeStamp);
			obj.put("process_id", PID);
			obj.put("number_of_entries", jArray.length());
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return obj;
	}

	/*
	 * Helper method to create a JSONObject containing the weather data from a given
	 * entry. The input argument of this method is a single
	 * main.weatherService.WeatherDataEntry object. This method returns a
	 * JSONObject, which contains all the data from the entry.
	 */
	private static JSONObject getJSONObject(WeatherDataEntry entry) {
		JSONObject entryJSON = new JSONObject();
		entryJSON.put("id", entry.getID());
		entryJSON.put("name", entry.getName());
		entryJSON.put("state", entry.getState());
		entryJSON.put("time_zone", entry.getTime_zone());
		entryJSON.put("lat", entry.getLat());
		entryJSON.put("lon", entry.getLon());
		entryJSON.put("local_date_time", entry.getLocal_date_time());
		entryJSON.put("local_date_time_full", entry.getLocal_date_time_full());
		entryJSON.put("air_temp", entry.getAir_temp());
		entryJSON.put("apparent_t", entry.getApparent_t());
		entryJSON.put("cloud", entry.getCloud());
		entryJSON.put("dewpt", entry.getDewpt());
		entryJSON.put("press", entry.getPress());
		entryJSON.put("rel_hum", entry.getRel_hum());
		entryJSON.put("wind_dir", entry.getWind_dir());
		entryJSON.put("wind_spd_kmh", entry.getWind_spd_kmh());
		entryJSON.put("wind_spd_kt", entry.getWind_spd_kt());
		return entryJSON;
	}

	/*
	 * Helper method to build response header. Input arguments are the host name and
	 * the content length. Method returns the header in String form.
	 */
	private String buildHeader(String host, String contentLength) {
		return "PUT" + " " + "/weather.json" + " HTTP/1.1\r\n" + "Host: " + host + "\r\n" + "User-Agent: ATOMClient/1/0"
				+ "\r\n" + "Content-Type: " + "application/json" + "\r\n" + "Content-Length: " + contentLength
				+ "\r\n\r\n";
	}

	/*
	 * Main method of the main.weatherService.ContentServer class. Input arguments
	 * from the command-line are the server URI, the station ID and the input file
	 * containing the weather data. Method simply creates a new instance of the
	 * main.weatherService.ContentServer and then starts it. Will stop the server
	 * after run() has printed the aggregation server response to the user.
	 */
	public static void main(String[] args) {
		ContentServer server = new ContentServer();
		try {
			server.startServer(args, server);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		server.stopServer();
	}
}