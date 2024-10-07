package weatherService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ClientRequestTest {

	private ClientRequest clientRequest;
	private String fullRequest;
	private String lamportTimeStamp;
	private String PID;
	private String contentServerID;
	private String requestType;
	private Socket mockSocket;

	@BeforeEach
	void setUp() {
		fullRequest = "GET /weatherData HTTP/1.1";
		lamportTimeStamp = "123456789";
		PID = "1";
		contentServerID = "server-1";
		requestType = "GET";
		mockSocket = new Socket();

		clientRequest = new ClientRequest(fullRequest, lamportTimeStamp, PID, contentServerID, requestType, mockSocket);
	}

	@Test
	@DisplayName("Get full request test")
	void testGetFullRequest() {
		assertEquals(fullRequest, clientRequest.getFullRequest());
	}

	@Test
	@DisplayName("Get lamport timestamp test")
	void testGetLamportTimeStamp() {
		assertEquals(lamportTimeStamp, clientRequest.getLamportTimeStamp());
	}

	@Test
	@DisplayName("Get process ID test")
	void testGetPID() {
		assertEquals(PID, clientRequest.getPID());
	}

	@Test
	@DisplayName("Get content server ID test")
	void testGetContentServerID() {
		assertEquals(contentServerID, clientRequest.getContentServerID());
	}

	@Test
	@DisplayName("Get request type test")
	void testGetRequestType() {
		assertEquals(requestType, clientRequest.getRequestType());
	}

	@Test
	@DisplayName("Get socket test")
	void testGetSocket() {
		assertNotNull(clientRequest.getSocket());
		assertSame(mockSocket, clientRequest.getSocket());
	}
}
