package weatherService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Database {

	// Name of weather data file
	private static final String weatherDataFile = "weather.json";

	// Name of put history file
	private static final String putHistoryFile = "PUT_HISTORY.txt";

	// main.weatherService.Database instance
	private static final Database databaseInstance = new Database();

	// Linked list for put history with ids
	private static final LinkedList<Integer> putHistory = new LinkedList<>();

	// Set up timer
	Timer timer = new Timer();

	// Hashmap for timer tasks, with key as integer id
	private static final HashMap<Integer, TimerTask> connectedContentServers = new HashMap<>();

	// Hash map for database of JSONObjects, key is integer id
	private static final HashMap<Integer, JSONObject> database = new HashMap<>();

	// Helper method to get the instance of the database. Returns the
	// main.weatherService.Database instance.
	public static Database getInstance() {
		return databaseInstance;
	}

	/*
	 * Method to get the data of a content server by its ID from the database. Input
	 * argument is the specified ID, and it returns a String of the data from the
	 * database.
	 */
	public String getDataByID(String id) {
		return database.get(Integer.parseInt(id)).toString();
	}

	/*
	 * Method to get the PUT history. Returns a linked list of integers.
	 */
	public LinkedList<Integer> getPutHistory() {
		if (putHistory.isEmpty()) {
			File historyFile = new File(putHistoryFile);
			if (historyFile.exists()) {
				try {
					readPUTHistoryFile();
				} catch (IOException e) {
					throw new RuntimeException("Error reading put history file: " + e.getMessage());
				}
			}
		}
		return putHistory;
	}

	/*
	 * Helper method to return whether a content server is already connected. Input
	 * argument is the specified ID, and it returns a boolean which is true if the
	 * content server is already connected.
	 */
	public boolean alreadyConnected(String id) {
		return connectedContentServers.containsKey(Integer.parseInt(id));
	}

	/*
	 * Method to check if there is JSON data for a given station ID in the database.
	 * Input argument is the specified content server's ID, and it returns a boolean
	 * which is true if the database contains data under this ID.
	 */
	public boolean isIDInDatabase(String ID) {
		return database.get(Integer.parseInt(ID)) != null;
	}

	/*
	 * Method to restart a content server's timer. The input argument is the id of
	 * the content server whose timer is to be restarted. This method is triggered
	 * when a content server which is already connected to the database (i.e.
	 * database contains station ID) tries to reconnect.
	 * 
	 * The old timer is cancelled and new one of 30 seconds is started. This is
	 * achieved using a TimerTask. The content server must be removed and re-added
	 * to the "connectedContentServers" HashMap.
	 */
	public void restartTimer(String id) {
		TimerTask oldScheduleDelete = connectedContentServers.get(Integer.parseInt(id));
		oldScheduleDelete.cancel();
		TimerTask newScheduleDelete = new TimerTask() {
			@Override
			public void run() {
				deleteContentServer(Integer.parseInt(id));
			}
		};
		timer.schedule(newScheduleDelete, 30000);
		connectedContentServers.put(Integer.parseInt(id), newScheduleDelete);
		putHistory.add(Integer.parseInt(id));
		updatePutHistoryFile();
	}

	/*
	 * Method to restart the database in the case of a crash. Method will put all
	 * the weather data stored in the weatherDataFile back into the database, and
	 * the put history stored in the put history file back into the "putHistory"
	 * linked list.
	 */
	public void restartDatabase() {
		File weatherFile = new File(weatherDataFile);
		if (weatherFile.exists()) {
			try {
				JSONArray weatherData = new JSONArray(new String(Files.readAllBytes(Paths.get(weatherDataFile))));
				for (int i = 0; i < weatherData.length(); i++) {
					JSONObject weather = weatherData.getJSONObject(i);
					database.put(weather.getInt("station_id"), weather);
				}
			} catch (JSONException | IOException e) {
				throw new RuntimeException(e);
			}
		}
		File historyFile = new File(putHistoryFile);
		if (historyFile.exists()) {
			try {
				readPUTHistoryFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/*
	 * Helper method to read the PUT history file and store it in the "putHistory"
	 * variable.
	 */
	private static void readPUTHistoryFile() throws IOException {
		String historyData = new String(Files.readAllBytes(Paths.get(putHistoryFile)));
		String[] historyIds = historyData.split("\\s+");
		for (String idStr : historyIds) {
			if (!idStr.trim().isEmpty()) {
				putHistory.add(Integer.parseInt(idStr.trim()));
			}
		}
	}

	/*
	 * Method to add a new content server to the connected content servers. Input
	 * argument is an Integer for the ID of the content server to be added. If there
	 * are more than 20 content servers connected, then the oldest is deleted to
	 * keep the program efficient. A new TimerTask is created for the content
	 * server, which will delete it after 30 seconds.
	 */
	public void addContentServer(Integer id) {
		Integer oldestServer;
		while (putHistory.size() >= 20) {
			oldestServer = putHistory.removeFirst();
			deleteContentServer(oldestServer);
		}
		putHistory.add(id);
		updatePutHistoryFile();
		TimerTask scheduleDelete = new TimerTask() {
			@Override
			public void run() {
				deleteContentServer(id);
			}
		};
		timer.schedule(scheduleDelete, 30000);
		connectedContentServers.put(id, scheduleDelete);
	}

	/*
	 * Method to delete a specific content server from the connected content servers
	 * HashMap. Method takes a single input argument, the id of the target content
	 * server.
	 * 
	 * To properly delete the content server, the data which it put into the
	 * database must also be removed. Once removed the "weatherDataFile" must be
	 * updated, as it no longer should contain data from the target content server.
	 * Once removing from the database, the method will simply rewrite the file
	 * using the updated database. A similar process is used to update
	 * "putHistoryFile".
	 */
	public void deleteContentServer(int id) {
		TimerTask scheduledDelete = connectedContentServers.get(id);
		scheduledDelete.cancel();
		putHistory.removeAll(Collections.singleton(id));
		updatePutHistoryFile();
		connectedContentServers.remove(id);
		database.remove(id);
		FileWriter weatherDataFileWriter = null;
		try {
			File weatherFile = new File(weatherDataFile);
			if (weatherFile.exists()) {
				weatherDataFileWriter = new FileWriter(weatherDataFile);
				JSONArray weatherData = new JSONArray();
				for (int key : database.keySet()) {
					weatherData.put(database.get(key));
				}
				weatherDataFileWriter.write(weatherData.toString());
				weatherDataFileWriter.flush();
				weatherDataFileWriter.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (weatherDataFileWriter != null) {
				try {
					weatherDataFileWriter.close();
				} catch (IOException e) {
					System.err.println("Failed to close weather data file writer: " + e.getMessage());
				}
			}
		}
	}

	/*
	 * Helper method to update PUT_HISTORY.txt file.
	 */
	private void updatePutHistoryFile() {
		try (FileWriter putHistoryFileWriter = new FileWriter(putHistoryFile)) {
			for (int id : putHistory) {
				putHistoryFileWriter.write(id + " ");
			}
			putHistoryFileWriter.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Method to add new JSON data to the database and weatherData file. Input
	 * arguments are the id of the content server and a JSONObject of the weather
	 * data entry.
	 * 
	 * The method will first add the id and data to the database, and then write to
	 * the file. If the file does not exist it will create a new file for the
	 * weather data. Then, if the file is empty, it will simply put the data into
	 * the file. On the other hand, if it is not empty, the file must be updated. If
	 * there is no data for this id, the data is simply added, otherwise it is
	 * updated.
	 */
	public void addToDatabase(int id, JSONObject data) {
		database.remove(id);
		database.put(id, data);
		try {
			File weatherFile = new File(weatherDataFile);
			if (!weatherFile.exists()) {
				if (!weatherFile.createNewFile()) {
					throw new RuntimeException("Could not create weather file");
				}
			}
			Path weatherFilePath = Paths.get(weatherDataFile);
			if (weatherFile.length() == 0) {
				// File is empty
				try {
					JSONArray weatherData = new JSONArray();
					weatherData.put(data);
					Files.write(weatherFilePath, weatherData.toString().getBytes());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				// File already has data
				try {
					JSONArray weatherData = new JSONArray(new String(Files.readAllBytes(weatherFilePath)));
					boolean containsID = false;
					for (int i = 0; i < weatherData.length(); i++) {
						JSONObject weather = weatherData.getJSONObject(i);
						if (weather.getInt("station_id") == id) {
							containsID = true;
							weatherData.remove(i);
							weatherData.put(data);
							break;
						}
					}
					if (!containsID) {
						weatherData.put(data);
					}
					Files.write(weatherFilePath, weatherData.toString().getBytes());
				} catch (JSONException | IOException e) {
					throw new RuntimeException(e);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// Method to clear the database entirely
	public void clearDatabase() {
		if (!putHistory.isEmpty()) {
			putHistory.clear();
		}
		File historyFile = new File(putHistoryFile);
		if (historyFile.exists()) {
			if (!historyFile.delete()) {
				throw new RuntimeException("Could not delete put history file");
			}
		}
		if (!connectedContentServers.isEmpty()) {
			for (TimerTask task : connectedContentServers.values()) {
				task.cancel();
			}
			connectedContentServers.clear();
		}
		if (!database.isEmpty()) {
			database.clear();
		}
		File weatherFile = new File(weatherDataFile);
		if (weatherFile.exists()) {
			if (!weatherFile.delete()) {
				throw new RuntimeException("Failed to delete weather file");
			}
		}
		this.timer = new Timer();
	}
}