package weatherService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatabaseTest {

	private static final String VALID_JSON_CONTENT = "[{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\""
			+ "wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\""
			+ ",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\""
			+ "20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  "
			+ "ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\""
			+ ":-34.9}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":1}]";

	@BeforeEach
	void setUp() {
		new File("PUT_HISTORY.txt").delete();
	}

	@AfterEach
	void tearDown() {
		Database.getInstance().clearDatabase();
	}

	/*
	 * Test get put history with populated putHistory variable
	 */
	@Test
	@DisplayName("Get put history test when it is not empty")
	void testGetPutHistory_PutHistoryNotEmpty() {
		Database.getInstance().getPutHistory().add(3);
		Database.getInstance().getPutHistory().add(4);

		LinkedList<Integer> putHistory = Database.getInstance().getPutHistory();
		assertEquals(2, putHistory.size());
		assertEquals(3, putHistory.get(0));
		assertEquals(4, putHistory.get(1));
	}

	/*
	 * Test get put history after crash, rewrite from PUT_HISTORY.txt file. This
	 * also tests the read put history file method.
	 */
	@Test
	@DisplayName("Get put history test when the variable is empty, but file is not")
	void testGetPutHistory_emptyButFileExists() throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("PUT_HISTORY.txt"))) {
			writer.write("1 2");
		}
		LinkedList<Integer> putHistory = Database.getInstance().getPutHistory();
		assertEquals(2, putHistory.size());
		assertEquals(1, putHistory.get(0));
		assertEquals(2, putHistory.get(1));
	}

	/*
	 * Test get put history with empty file and variable.
	 */
	@Test
	@DisplayName("Get put history test when both the file and variable are empty")
	void testGetPutHistory_FileDoesNotExist() {
		File putHistoryFile = new File("PUT_HISTORY.txt");
		putHistoryFile.delete();

		LinkedList<Integer> putHistory = Database.getInstance().getPutHistory();
		assertTrue(putHistory.isEmpty());
	}

	/*
	 * Test if ID is present in database method.
	 */
	@Test
	@DisplayName("Test is ID in database method")
	void isIDInDatabaseTest() {
		Database.getInstance().clearDatabase();
		assertEquals(false, Database.getInstance().isIDInDatabase("1"));

		String expected = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\""
				+ "wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\""
				+ ",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\""
				+ "20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  "
				+ "ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\""
				+ ":-34.9}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":1}";
		JSONObject json = new JSONObject(expected);
		Database.getInstance().addToDatabase(1, json);
		assertEquals(true, Database.getInstance().isIDInDatabase("1"));
	}

	/*
	 * Test update put history file method. This occurs when calling add content server.
	 */
	@Test
	@DisplayName("Add content server and update put history file test")
	void addContentServerAndUpdateHistoryTest() throws IOException {
		Database.getInstance().addContentServer(1);
		Database.getInstance().addContentServer(2);
		File putHistoryFile = new File("PUT_HISTORY.txt");
		try (BufferedReader reader = new BufferedReader(new FileReader(putHistoryFile))) {
            String line = reader.readLine();
            assertEquals("1 2 ", line);
        }
		Database.getInstance().addContentServer(3);
		try (BufferedReader reader = new BufferedReader(new FileReader(putHistoryFile))) {
            String line = reader.readLine();
            assertEquals("1 2 3 ", line);
        }
	}

	/*
	 * Test to see if data is still stored and correct after restarting the
	 * database. This simulates what would happen if a crash occurred.
	 */
	@Test
	@DisplayName("Restart database test")
	void restartDatabaseTest() throws IOException {
		Database.getInstance().clearDatabase();
		File file = new File("weather.json");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileWriter fw = new FileWriter("weather.json");
		fw.write(VALID_JSON_CONTENT);
		fw.close();

		Database.getInstance().restartDatabase();
		String output = Database.getInstance().getDataByID("1");
		String expected = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\""
				+ "wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\""
				+ ",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\""
				+ "20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  "
				+ "ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\""
				+ ":-34.9}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":1}";
		assertEquals(expected, output);
		Database.getInstance().clearDatabase();
		file.delete();
	}

	/*
	 * Test if a connected server is first connection
	 */
	@Test
	@DisplayName("Content server first connection test")
	void isAlreadyConnectedTest_false() {
		Database.getInstance().clearDatabase();
		boolean output = Database.getInstance().alreadyConnected("1");
		assertEquals(false, output);
	}

	/*
	 * Test if a server is the first connection when it is not. Also tests
	 * addContentServer
	 */
	@Test
	@DisplayName("Content server first connection test, not first connection")
	void isAlreadyConnectedTest_true() {
		Database.getInstance().clearDatabase();
		Database.getInstance().addContentServer(1);
		boolean output = Database.getInstance().alreadyConnected("1");
		assertEquals(true, output);
	}

	/*
	 * Test if a content server's timer is properly restarted. Wait for 15 seconds
	 * and then restart the timer. Wait for 16 seconds. The content server should be
	 * disconnected from the database. This test is commented out as each time it is
	 * run it takes 31 seconds. Uncomment to test. Also tests addContentServer.
	 */
//	@Test
//	@DisplayName("Test restarting content server timer")
//	void testRestartContentServerTimer() {
//		Database.getInstance().clearDatabase();
//		Database.getInstance().addContentServer(1);
//		try {
//			Thread.sleep(15000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		Database.getInstance().restartTimer("1");
//		try {
//			Thread.sleep(16000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		boolean result = Database.getInstance().alreadyConnected("1");
//		boolean expected = true;
//		assertEquals(expected, result);
//	}

	/*
	 * Test if a content server is properly deleted from the database.
	 */
	@Test
	@DisplayName("Delete content server test")
	void deleteContentServerTest() {
		Database.getInstance().clearDatabase();
		Database.getInstance().addContentServer(1);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Database.getInstance().deleteContentServer(1);
		boolean result = Database.getInstance().alreadyConnected("1");
		boolean expected = false;
		assertEquals(expected, result);
	}
}