package weatherService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AggregationServerTest {

	private AggregationServer server;
	private ServerSocket mockServerSocket;
	private Socket mockSocket;
	private BufferedReader mockIn;

	@BeforeEach
	void setUp() throws Exception {
		server = new AggregationServer();
		
	}

}