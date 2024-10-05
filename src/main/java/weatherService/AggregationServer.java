package weatherService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import org.json.JSONException;
import org.json.JSONObject;

public class AggregationServer extends Thread {

	// Initialise sockets
	ServerSocket serverSocket;
	Socket socket;

	// Boolean variable for running
	boolean running = false;

	// Default port
	int port = 4567;

	// Semaphore to ensure only one thread can access the database at a time
	Semaphore semaphore = new Semaphore(1);

	// Initialise lamport clock
	LamportClock clock = null;

	// Initialise class to store client request details
	ClientRequest clientRequest = null;

	/*
	 * Method to start the aggregation server. Get the weather data file, if it
	 * already exists then the server must have previously crashed, so restart the
	 * database, lamport clock and port. Otherwise, initialise the server without
	 * restarting database. Then start the thread, which invokes run(). Input
	 * arguments are the command-line arguments given to main by the user.
	 */
	public void startServer(String[] args) {
		try {
			File weatherFile = new File("weather.json");
			if (weatherFile.exists()) {
				Database.getInstance().restartDatabase();
				clock = LamportClock.initialiseLamportClock();
				if (args.length > 0) {
					port = Integer.parseInt(args[0]);
				}
				serverSocket = new ServerSocket(port);
				System.out.println("Aggregation server restarted, listening at port " + port);
				System.out.println("You can access http://localhost:4567 now.");
			} else {
				clock = LamportClock.initialiseLamportClock();
				if (args.length > 0) {
					port = Integer.parseInt(args[0]);
				}
				serverSocket = new ServerSocket(port);
				System.out.println("Aggregation server starting up, listening at port " + port);
				System.out.println("You can access http://localhost:4567 now.");
			}
			this.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Method to stop the server. Set running to false, delete lamport clock files,
	 * clear database and interrupt the thread, which stops run().
	 */
	public void stopServer() {
		running = false;
		clock.deleteFiles();
		Database.getInstance().clearDatabase();
		System.out.println("The aggregation server has been shutdown.");
		this.interrupt();
	}

	/*
	 * Override of thread run method. Wait until client connects to aggregation
	 * server first. Once connected read the input stream of the socket to parse in
	 * the request and details from the client.
	 * 
	 * If the request is invalid, send the appropriate response and break. If the
	 * request is a PUT request, read the lines up to and including Content-Length:,
	 * and then break. Otherwise, the request must be a GET request, so process each
	 * of the required GET request headers. After processing the necessary input
	 * lines, call the corresponding handle method.
	 * 
	 * Finally, call the queue and handle request method. If this point is reached,
	 * the request should be valid.
	 */
	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				socket = serverSocket.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				StringBuilder fullRequest = new StringBuilder();
				String line, stationID = "", lamportTimeStamp = "", PID = "";
				boolean getRequest = false, putRequest = false;
				int content_length = 0;

				// Read details
				while ((line = in.readLine()) != null) {
					if (line.contains("GET")) {
						getRequest = true;
						fullRequest.append(line);
						continue;
					} else if (line.contains("PUT")) {
						putRequest = true;
						fullRequest.append(line);
						fullRequest.append("\r\n");
						continue;
					}
					if (!getRequest && !putRequest) {
						Response response = new Response("Bad Request", "400", "0", "", "Invalid request type", socket);
						response.respond();
						in.close();
						break;
					} else if (putRequest) {
						if (line.contains("Content-Length:")) {
							fullRequest.append(line);
							String temp = line.substring("Content-Length:".length()).trim();
							content_length = Integer.parseInt(temp) + 13;
							break;
						} else {
							fullRequest.append(line).append("\r\n");
						}
					} else {
						if (line.isEmpty()) {
							fullRequest.append(line);
							break;
						} else if (line.contains("StationID:")) {
							fullRequest.append(line).append("\r\n");
							stationID = line.substring("StationID:".length()).trim();
						} else if (line.contains("LamportTimestamp:")) {
							fullRequest.append(line).append("\r\n");
							lamportTimeStamp = line.substring("LamportTimestamp:".length()).trim();
						} else if (line.contains("ProcessID:")) {
							fullRequest.append(line).append("\r\n");
							PID = line.substring("ProcessID:".length()).trim();
						} else {
							fullRequest.append(line).append("\r\n");
						}
					}
				}

				// Respond accordingly
				if (putRequest) {
					handlePUTRequest(content_length, in, fullRequest);
				} else if (getRequest) {
					handleGETRequest(stationID, lamportTimeStamp, PID, fullRequest);
				}
				if (fullRequest.length() > 0) {
					queueAndHandleRequest();
				}
				in.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/*
	 * Helper method to handle a PUT request. Input arguments are the content length
	 * of the request, the buffered reader which is still being used from the run()
	 * method, and the string builder of the full request, which will be further
	 * updated in this method.
	 * 
	 * This method will read in the body of the request and append it to the full
	 * request. Afterward, a new JSON object is made using the content of the body,
	 * and the data is then saved to a file for later use in the
	 * main.weatherService.Handler class (to process the request). Finally, the
	 * client request local variable is set with a new
	 * main.weatherService.ClientRequest object, which contains the request data.
	 */
	private void handlePUTRequest(Integer content_length, BufferedReader in, StringBuilder fullRequest)
			throws IOException {
		char[] body = new char[content_length];
		in.read(body, 0, content_length);
		String content = new String(body);
		fullRequest.append(content);
		try {
			JSONObject object = new JSONObject(content);
			saveJSONData(object);
			clientRequest = new ClientRequest(fullRequest.toString(), object.get("lamport_timestamp").toString(),
					object.get("process_id").toString(), object.get("station_id").toString(), "PUT", socket);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Helper method to save the JSON data to a file for main.weatherService.Handler
	 * class. Takes an input argument of a JSON object, which is converted to a
	 * string and stored in the created file.
	 */
	private static void saveJSONData(JSONObject object) {
		try {
			String contentServerID = object.get("station_id").toString();
			String PID = object.get("process_id").toString();
			String file = "cs" + contentServerID + "_p" + PID + ".json";
			FileWriter fw = new FileWriter(file);
			fw.write(object.toString());
			fw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Helper method to handle a GET request. Method takes in four input arguments;
	 * the station ID, the lamport timestamp, the process ID and the full request.
	 * These are used to set the client request local variable to a new
	 * main.weatherService.ClientRequest with the specified details. If the station
	 * ID received is -1, then no specific station was requested. Use the most
	 * recent PUT. In this case, there may be no data in the database (hence
	 * "putHistory" is empty), so return custom 401.
	 */
	private void handleGETRequest(String stationID, String lamportTimeStamp, String PID, StringBuilder fullRequest) {
		try {
			if (stationID.equals("-1")) {
				LinkedList<Integer> putHistory = Database.getInstance().getPutHistory();
				if (putHistory.isEmpty()) {
					Response response = new Response("Bad Request", "401", "0", "", "No data in database", socket);
					response.respond();
					return;
				}
				stationID = putHistory.getLast().toString();
			}
			clientRequest = new ClientRequest(fullRequest.toString(), lamportTimeStamp, PID, stationID, "GET", socket);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * Helper method to queue and handle a request. When called, the method ticks,
	 * synchronises and updates the lamport clock using the
	 * main.weatherService.LamportClock class's methods. Then it uses the
	 * main.weatherService.QueueHandler class to add the client request to the
	 * queue. After it is added, it retrieves the next request from the queue, and
	 * invokes run() from the main.weatherService.Handler class, giving the next
	 * request the semaphore.
	 */
	private void queueAndHandleRequest() {
		clock.tick();
		clock.synchroniseClock(Integer.parseInt(clientRequest.getLamportTimeStamp()));
		clock.updateLamportFile(clock.getTimeStamp());
		QueueHandler.getInstance().addRequestToQueue(clientRequest);
		ClientRequest req = QueueHandler.getInstance().getRequestFromQueue();
		Handler handler = new Handler(req, semaphore);
		handler.run();
	}

	/*
	 * Main method to start the server. Input arguments are from the command-line.
	 * Expects 0 or 1 input argument for the port number. If 0 are provided the
	 * default will be used. Further arguments will be ignored.
	 * 
	 * Method will start the server for two minutes. This is arbitrary, and is used
	 * for testing. After, the server will be stopped.
	 */
	public static void main(String[] args) {
		AggregationServer server = new AggregationServer();
		server.startServer(args);
		try {
			Thread.sleep(120000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		server.stopServer();
	}
}