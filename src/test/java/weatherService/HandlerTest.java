package weatherService;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HandlerTest {

	private static final String VALID_JSON_CONTENT = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\""
			+ "wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\""
			+ ",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\""
			+ "20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  "
			+ "ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\""
			+ ":-34.9}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":1}";

	// Invalid data missing pressure
	private static final String INVALID_JSON_CONTENT = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\""
			+ "wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\""
			+ ",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\""
			+ "20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  "
			+ "ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"lat\""
			+ ":-34.9}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":1}";

	private File weatherFile;
	private Semaphore semaphore;
	private ByteArrayOutputStream byteArrayOutputStream;
	private MockSocket mockSocket;

	@BeforeEach
	void setUp() throws IOException {
		semaphore = new Semaphore(1);
		weatherFile = new File("weather.json");
		weatherFile.createNewFile();
		byteArrayOutputStream = new ByteArrayOutputStream();
		mockSocket = new MockSocket(byteArrayOutputStream);
	}

	@AfterEach
	void tearDown() throws IOException {
		Database.getInstance().clearDatabase();
		if (weatherFile.exists()) {
			weatherFile.delete();
		}
		byteArrayOutputStream.close();
		mockSocket.close();
	}

	/*
	 * Test an invalid request type. Should return status 400 Bad Request.
	 */
	@Test
	@DisplayName("Invalid request type test")
	void invalidRequestTypeTest() throws IOException {
		ClientRequest invalid = new ClientRequest(null, null, null, null, "invalid", mockSocket);
		Handler handler = new Handler(invalid, semaphore);
		handler.run();

		String output = byteArrayOutputStream.toString();
		String expected = "HTTP/1.1 400 Bad Request\n" + "Content-Type: application/json\n" + "Content-Length: 0\n"
				+ "Connection: close\n\n" + "HTTP/1.1 400 Bad Request\r\n" + "Content-Type: text/html\r\n\r\n"
				+ "<!DOCTYPE html>\r\n" + "<html>\r\n" + "<head>\r\n" + "<title>400 Bad Request</title>\r\n"
				+ "</head>\r\n" + "<body>\r\n" + "<h1>400 Bad Request</h1>\r\n" + "<p>Invalid request type</p>\r\n"
				+ "</body>\r\n" + "</html>\r\n\n";
		assertEquals(expected.trim(), output.trim());
	}

	/*
	 * Test a PUT request with a valid JSON input. Should return status 201 Created.
	 */
	@Test
	@DisplayName("Handle PUT request test with a valid input")
	void handlePUTRequestTest_validJSONNewServer() throws IOException {
		File jsonFile = new File("cs1_p1.json");
		try (FileWriter fw = new FileWriter(jsonFile)) {
			fw.write(VALID_JSON_CONTENT);
		}

		String fullRequest = "PUT" + " " + "/weather.json" + " HTTP/1.1\r\n" + "Host: localhost\r\n"
				+ "User-Agent: ATOMClient/1/0" + "\r\n" + "Content-Type: application/json" + "\r\n"
				+ "Content-Length: 429\r\n\r\n" + VALID_JSON_CONTENT;
		ClientRequest PUTRequest = new ClientRequest(fullRequest, "1", "1", "1", "PUT", mockSocket);
		Handler handler = new Handler(PUTRequest, semaphore);
		handler.run();

		String output = byteArrayOutputStream.toString();
		String expected = "HTTP/1.1 201 Created\n" + "Content-Type: application/json\n" + "Content-Length: 429\n"
				+ "Connection: close\n\n" + VALID_JSON_CONTENT + "\n";
		assertEquals(expected.trim(), output.trim());

		jsonFile.delete();
	}

	/*
	 * Test a PUT request with an invalid JSON input. Should return status 500
	 * Internal Server Error.
	 */
	@Test
	@DisplayName("Handle PUT request test with an invalid input")
	void handlePUTRequestTest_invalidJSON() throws IOException {
		File jsonFile = new File("cs2_p1.json");
		try (FileWriter fw = new FileWriter(jsonFile)) {
			fw.write(INVALID_JSON_CONTENT);
		}

		String fullRequest = "PUT" + " " + "/weather.json" + " HTTP/1.1\r\n" + "Host: localhost\r\n"
				+ "User-Agent: ATOMClient/1/0" + "\r\n" + "Content-Type: application/json" + "\r\n"
				+ "Content-Length: 417\r\n\r\n" + INVALID_JSON_CONTENT;
		ClientRequest PUTRequest = new ClientRequest(fullRequest, "1", "1", "2", "PUT", mockSocket);
		Handler handler = new Handler(PUTRequest, semaphore);
		handler.run();

		String output = byteArrayOutputStream.toString();
		String expected = "HTTP/1.1 500 Internal Server Error\n" + "Content-Type: application/json\n"
				+ "Content-Length: 0\n" + "Connection: close\n\n" + "HTTP/1.1 500 Internal Server Error\r\n"
				+ "Content-Type: text/html\r\n\r\n" + "<!DOCTYPE html>\r\n" + "<html>\r\n" + "<head>\r\n"
				+ "<title>500 Internal Server Error</title>\r\n" + "</head>\r\n" + "<body>\r\n"
				+ "<h1>500 Internal Server Error</h1>\r\n" + "<p>Invalid JSON data</p>\r\n" + "</body>\r\n"
				+ "</html>\r\n\n";
		assertEquals(expected.trim(), output.trim());

		jsonFile.delete();
	}

	/*
	 * Test a PUT request with no content in the temporary JSON file. Should return
	 * status 204 No Content.
	 */
	@Test
	@DisplayName("Handle PUT request test with no content")
	void handlePUTRequestTest_noContent() throws IOException {
		File jsonFile = new File("cs3_p1.json");
		jsonFile.createNewFile();

		String fullRequest = "PUT" + " " + "/weather.json" + " HTTP/1.1\r\n" + "Host: localhost\r\n"
				+ "User-Agent: ATOMClient/1/0" + "\r\n" + "Content-Type: application/json" + "\r\n"
				+ "Content-Length: 0\r\n\r\n";
		ClientRequest PUTRequest = new ClientRequest(fullRequest, "1", "1", "3", "PUT", mockSocket);
		Handler handler = new Handler(PUTRequest, semaphore);
		handler.run();

		String output = byteArrayOutputStream.toString();
		String expected = "HTTP/1.1 204 No Content\n" + "Content-Type: application/json\n" + "Content-Length: 0\n"
				+ "Connection: close\n\n";
		assertEquals(expected.trim(), output.trim());

		jsonFile.delete();
	}

	/*
	 * Test a PUT request when adding the server twice. Since the server is already
	 * connected, this test should return status 200 OK.
	 */
	@Test
	@DisplayName("Handle PUT request test with the server already connected")
	void handlePUTRequestTest_validJSONSameServer() throws IOException {
		Database.getInstance().addContentServer(4);
		File jsonFile = new File("cs4_p1.json");
		try (FileWriter fw = new FileWriter(jsonFile)) {
			fw.write(VALID_JSON_CONTENT);
		}

		String fullRequest = "PUT" + " " + "/weather.json" + " HTTP/1.1\r\n" + "Host: localhost\r\n"
				+ "User-Agent: ATOMClient/1/0" + "\r\n" + "Content-Type: application/json" + "\r\n"
				+ "Content-Length: 429\r\n\r\n" + VALID_JSON_CONTENT;
		ClientRequest PUTRequest = new ClientRequest(fullRequest, "1", "1", "4", "PUT", mockSocket);
		Handler handler = new Handler(PUTRequest, semaphore);
		handler.run();

		String output = byteArrayOutputStream.toString();
		String expected = "HTTP/1.1 200 OK\n" + "Content-Type: application/json\n" + "Content-Length: 429\n"
				+ "Connection: close\n\n" + VALID_JSON_CONTENT + "\n";
		assertEquals(expected.trim(), output.trim());

		jsonFile.delete();
	}

	/*
	 * Test a GET request when trying the retrieve data which is in the database.
	 * Should return status 200 OK.
	 */
	@Test
	@DisplayName("Handles GET request test with correct input")
	void handleGETRequestTest_contentServerConnected() {
		JSONObject json = new JSONObject(VALID_JSON_CONTENT);
		Database.getInstance().addToDatabase(1, json);

		String fullRequest = "GET / HTTP/1.1\r\nHost: localhost\r\nAccept: application/json\r\nStationID: "
				+ "1\r\nLamportTimestamp: 1\r\nProcessID: 1\r\n\r\n";
		ClientRequest GETRequest = new ClientRequest(fullRequest, "1", "1", "1", "GET", mockSocket);
		Handler handler = new Handler(GETRequest, semaphore);
		handler.run();

		String output = byteArrayOutputStream.toString();
		String expected = "HTTP/1.1 200 OK\n" + "Content-Type: application/json\n" + "Content-Length: 429\n"
				+ "Connection: close\n\n" + VALID_JSON_CONTENT + "\n";
		assertEquals(expected.trim(), output.trim());
	}

	/*
	 * Test a GET request when trying to retrieve data which is not in the database.
	 * Should return status 404 Not Found.
	 */
	@Test
	@DisplayName("Handle GET request test with incorrect input")
	void handleGETRequestTest_noDataForID() {
		String fullRequest = "GET / HTTP/1.1\r\nHost: localhost\r\nAccept: application/json\r\nStationID: "
				+ "1\r\nLamportTimestamp: 1\r\nProcessID: 1\r\n\r\n";
		ClientRequest GETRequest = new ClientRequest(fullRequest, "1", "1", "1", "GET", mockSocket);
		Handler handler = new Handler(GETRequest, semaphore);
		handler.run();

		String output = byteArrayOutputStream.toString();
		String expected = "HTTP/1.1 404 Not Found\n" + "Content-Type: application/json\n"
				+ "Content-Length: 0\n" + "Connection: close\n\n" + "HTTP/1.1 404 Not Found\r\n"
				+ "Content-Type: text/html\r\n\r\n" + "<!DOCTYPE html>\r\n" + "<html>\r\n" + "<head>\r\n"
				+ "<title>404 Not Found</title>\r\n" + "</head>\r\n" + "<body>\r\n"
				+ "<h1>404 Not Found</h1>\r\n" + "<p>Data not found in database</p>\r\n" + "</body>\r\n"
				+ "</html>\r\n\n";
		assertEquals(expected.trim(), output.trim());
	}

	/*
	 * MockSocket class used to mock the socket and get the output stream for
	 * verification.
	 */
	private static class MockSocket extends Socket {
		private final OutputStream outputStream;

		public MockSocket(OutputStream outputStream) {
			this.outputStream = outputStream;
		}

		@Override
		public OutputStream getOutputStream() {
			return outputStream;
		}
	}
}
