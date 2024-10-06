package weatherService;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QueueHandlerTest {

	@BeforeEach
	void setUp() {
		QueueHandler.getInstance().clearQueue();
	}

	/*
	 * Test normal retrieval of elements in the queue. The request with the lower
	 * lamport timestamp should be returned first.
	 */
	@Test
	@DisplayName("Test if queue elements are retrieved in correct order, different timestamps")
	void queueRetrievalTest_diffTimestamps() {
		ClientRequest request1 = new ClientRequest(null, "10", "1", null, null, null);
		ClientRequest request2 = new ClientRequest(null, "5", "2", null, null, null);
		QueueHandler.getInstance().addRequestToQueue(request1);
		QueueHandler.getInstance().addRequestToQueue(request2);

		assertEquals(request2, QueueHandler.getInstance().getRequestFromQueue());
		assertEquals(request1, QueueHandler.getInstance().getRequestFromQueue());
	}

	/*
	 * Test retrieval of elements with the same lamport timestamp from the queue.
	 * The request with the lower process ID should be retrieved first.
	 */
	@Test
	@DisplayName("Test if queue elements are retrieved in correct order, same timestamp")
	void queueRetrievalTest_sameTimestamps() {
		ClientRequest request1 = new ClientRequest(null, "5", "1", null, null, null);
		ClientRequest request2 = new ClientRequest(null, "5", "2", null, null, null);
		ClientRequest request3 = new ClientRequest(null, "5", "3", null, null, null);
		QueueHandler.getInstance().addRequestToQueue(request1);
		QueueHandler.getInstance().addRequestToQueue(request2);
		QueueHandler.getInstance().addRequestToQueue(request3);

		assertEquals(request1, QueueHandler.getInstance().getRequestFromQueue());
		assertEquals(request2, QueueHandler.getInstance().getRequestFromQueue());
		assertEquals(request3, QueueHandler.getInstance().getRequestFromQueue());
	}

	/*
	 * Test clearing the queue. After try and get a request from the queue. As the
	 * queue is cleared it should throw a runtime exception.
	 */
	@Test
	@DisplayName("Test clearing the queue and getting request from an empty queue")
	public void testClearQueue() {
		ClientRequest request1 = new ClientRequest(null, "1", "100", null, null, null);
		QueueHandler.getInstance().addRequestToQueue(request1);

		QueueHandler.getInstance().clearQueue();

		assertThrows(RuntimeException.class, () -> QueueHandler.getInstance().getRequestFromQueue());
	}
}