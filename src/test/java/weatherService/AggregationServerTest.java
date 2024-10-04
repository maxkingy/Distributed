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

  private AggregationServer aggregationServer;
  private ServerSocket mockServerSocket;
  private Socket mockSocket;
  private BufferedReader mockIn;

  @BeforeEach
  void setUp() throws Exception {
    aggregationServer = new AggregationServer();
    mockServerSocket = mock(ServerSocket.class);
    mockSocket = mock(Socket.class);
    mockIn = mock(BufferedReader.class);
    aggregationServer.serverSocket = mockServerSocket;
    aggregationServer.socket = mockSocket;
  }

  @Test
  @DisplayName("Start server test with default port")
  void startServerTest_defaultPort() {
    when(mockServerSocket.accept()).thenReturn(mockSocket);
    St
  }
}