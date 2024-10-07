package weatherService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
 * Class to test starting and stopping aggregation server. Integrated tests are 
 * done separately.
 */
class AggregationServerTest {

	private final PrintStream originalOut = System.out;
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	private AggregationServer aggregationServer;

	@BeforeEach
	void setUp() {
		aggregationServer = new AggregationServer();
		System.setOut(new PrintStream(outputStream));
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
	}

	/*
	 * Testing the start server method with no input arguments. Port should be set
	 * to default of 4567, and need to check that the socket is initialised. This
	 * test also is for an existing weather file, which occurs when the server is
	 * restarted. Also test for the correct output to user.
	 */
	@Test
	@DisplayName("Test start server with default port after restart and an existing weather file")
	void startServerTest_existingWeatherFile() throws Exception {
		File weatherFile = new File("weather.json");
		assertTrue(weatherFile.createNewFile(), "Should create weather file.");

		// Write mocked file
		try (FileWriter writer = new FileWriter(weatherFile)) {
			writer.write("[{\"station_id\":\"1\",\"temperature\":22.5,\"humidity\":60}]");
		}

		aggregationServer.startServer(new String[] {});
		String startOutput = outputStream.toString();
		String startExpected = "Aggregation server restarted, listening at port 4567\nYou can access "
				+ "http://localhost:4567 now.\n";
		assertEquals(startExpected, startOutput);
		outputStream.reset();

		assertNotNull(aggregationServer.serverSocket, "Server socket should be initialised.");
		assertEquals(4567, aggregationServer.port, "Port should be set to default of 4567.");

		weatherFile.delete();
		aggregationServer.stopServer();
		String stopOutput = outputStream.toString();
		String stopExpected = "The aggregation server has been shutdown.\n";
		assertEquals(stopExpected, stopOutput);
	}

	/*
	 * Testing the start server method with an input argument. Port should be set to
	 * custom value, and the server should start as expected even without the
	 * weather file existing. Also test for the correct output to user.
	 */
	@Test
	@DisplayName("Test start server with custom port when weather file does not exist")
	void startServerTest_withoutWeatherFile() throws Exception {
		File weatherFile = new File("weather.json");
		if (weatherFile.exists()) {
			weatherFile.delete();
		}

		String[] args = new String[] { "8000" };
		aggregationServer.startServer(args);
		String startOutput = outputStream.toString();
		String startExpected = "Aggregation server starting up, listening at port 8000\nYou can access "
				+ "http://localhost:8000 now.\n";
		assertEquals(startExpected, startOutput);
		outputStream.reset();

		assertNotNull(aggregationServer.serverSocket, "Server socket should be initialized.");
		assertEquals(8000, aggregationServer.port, "Port should be set to 8000.");

		aggregationServer.stopServer();
		String stopOutput = outputStream.toString();
		String stopExpected = "The aggregation server has been shutdown.\n";
		assertEquals(stopExpected, stopOutput);
	}

	/*
	 * Testing the start server method with an invalid input port argument. Method
	 * should throw a runtime exception.
	 */
	@Test
	@DisplayName("Test start server with invalid port")
	void startServerTest_withInvalidPort() {
		String[] args = new String[] { "invalidPort" };

		Exception exception = assertThrows(RuntimeException.class, () -> {
			aggregationServer.startServer(args);
		});

		String expectedMessage = "java.lang.NumberFormatException: For input string: \"invalidPort\"";
		assertTrue(exception.getMessage().contains(expectedMessage), "Should throw an exception for invalid port.");
	}

	/*
	 * Test whether running is set to false and the socket is closed. Also check
	 * that the thread is interrupted.
	 */
	@Test
	@DisplayName("Test stop server")
	void stopServerTest() {
		aggregationServer.startServer(new String[] {});
		outputStream.reset();

		aggregationServer.stopServer();
		String stopOutput = outputStream.toString();
		String stopExpected = "The aggregation server has been shutdown.\n";
		assertEquals(stopExpected, stopOutput);

		assertNull(aggregationServer.serverSocket, "Server socket should be null after stopping the server.");
		assertFalse(aggregationServer.isAlive(), "Thread should be interrupted.");
	}
}
