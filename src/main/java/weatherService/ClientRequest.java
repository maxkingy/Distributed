package weatherService;

import java.net.Socket;

public class ClientRequest {

  private final String fullRequest; // String representation of the complete client request
  private final String lamportTimeStamp;
  private final String PID;
  private final String contentServerID;
  private final String requestType; // GET or PUT
  private final Socket socket; // Socket to send response

  // main.weatherService.ClientRequest constructor
  public ClientRequest(String fullRequest, String lamportTimeStamp, String PID,
      String contentServerID, String requestType, Socket socket) {
    this.fullRequest = fullRequest;
    this.lamportTimeStamp = lamportTimeStamp;
    this.PID = PID;
    this.contentServerID = contentServerID;
    this.requestType = requestType;
    this.socket = socket;
  }

  // Method to get the full request. Returns a String.
  public String getFullRequest() { return fullRequest; }

  // Method to get the lamport clock timestamp. Returns a String.
  public String getLamportTimeStamp() {
    return lamportTimeStamp;
  }

  // Method to get the process ID. Returns a String.
  public String getPID() {
    return PID;
  }

  // Method to get the content server ID. Returns a String.
  public String getContentServerID() {
    return contentServerID;
  }

  // Method to get the request type. Returns a String.
  public String getRequestType() {
    return requestType;
  }

  // Method to get the socket. Returns a Socket.
  public Socket getSocket() {
    return socket;
  }
}