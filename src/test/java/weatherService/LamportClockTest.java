package weatherService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LamportClockTest {

	private LamportClock clock;

	@BeforeEach
	void setUp() {
		new File("LAMPORT_CLOCK.txt").delete();
		new File("PID_COUNTER.txt").delete();
		clock = LamportClock.initialiseLamportClock();
	}

	@AfterEach
	void tearDown() {
		clock.deleteFiles();
	}

	/*
	 * Test initialisation of the lamport clock for a new server. Just need to check
	 * that the files exist and the process ID and timestamp are 0.
	 */
	@Test
	@DisplayName("Test the initialisation of the lamport clock with a new server")
	void testInitialiseLamportClock_newServer() {
		assertNotNull(clock);
		assertEquals(0, clock.getProcessID());
		assertEquals(0, clock.getTimeStamp());

		assertTrue(new File("LAMPORT_CLOCK.txt").exists());
		assertTrue(new File("PID_COUNTER.txt").exists());
	}

	/*
	 * Test initialisation of the lamport clock after the server has restarted.
	 * Write arbitrary values to the specified files and then initialise the lamport
	 * clock to check if the assigned values are correct. This simulates what would
	 * occur in the case of a crash and restart.
	 */
	@Test
	@DisplayName("Test the initialisation of the lamport clock after restarting")
	void testInitialiseLamportClock_restartServer() throws IOException {
		File file = new File("LAMPORT_CLOCK.txt");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write("PID: 1\nTimeStamp: 2");
		fileWriter.close();

		File pidFile = new File("PID_COUNTER.txt");
		if (pidFile.exists()) {
			pidFile.delete();
		}
		pidFile.createNewFile();
		fileWriter = new FileWriter(pidFile);
		fileWriter.write("Current PID count: 1");
		fileWriter.close();

		clock = LamportClock.initialiseLamportClock();
		assertEquals(1, clock.getProcessID());
		assertEquals(2, clock.getTimeStamp());
		assertEquals(2, LamportClock.getNewPID());
	}

	@Test
	@DisplayName("Test tick method")
	void testTick() {
		int initialTimeStamp = clock.getTimeStamp();
		clock.tick();
		assertEquals(initialTimeStamp + 1, clock.getTimeStamp());
	}

	/*
	 * Method to test the synchronisation of the lamport clock, which will set the
	 * time stamp to max(current, new) + 1 as described in the Lamport Clock class.
	 */
	@Test
	@DisplayName("Test synchronise clock")
	void testSynchroniseClock() {
		clock.tick();
		clock.synchroniseClock(4);
		assertEquals(5, clock.getTimeStamp());
	}

	/*
	 * Method to test updating the lamport clock file. Simply tick the clock and
	 * then update.
	 */
	@Test
	@DisplayName("Test updating the lamport file")
	void testUpdateLamportFile() {
		clock.tick();
		clock.updateLamportFile(clock.getTimeStamp());

		assertEquals(0, clock.getProcessID());
		assertEquals(1, clock.getTimeStamp());
	}

	/*
	 * Method to test the get new PID method. new PID should be 1 greater than the
	 * previous PID.
	 */
	@Test
	@DisplayName("Test getting a new PID")
	void testGetNewPID() {
		int pid1 = LamportClock.getNewPID();
		int pid2 = LamportClock.getNewPID();
		assertEquals(pid1 + 1, pid2);
	}

	@Test
	@DisplayName("Test delete files method")
	void testDeleteFiles() {
		clock.deleteFiles();
		assertFalse(new File("LAMPORT_CLOCK.txt").exists());
		assertFalse(new File("PID_COUNTER.txt").exists());
	}
}