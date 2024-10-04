package weatherService;

import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

public class Response {

  private final String status; // Status message
  private final String statusCode; // HTTP status code
  private final String contentLength; // Length of the response body
  private final String body; // main.weatherService.Response body content
  private final String error; // Error message, if applicable
  private final Socket socket; // Socket to send the response through
  private final ArrayList<String> headers = new ArrayList<>(); // Array for response headers

  // main.weatherService.Response constructor
  public Response(String status, String statusCode, String contentLength, String body,
      String error, Socket socket) {
    this.status = status;
    this.statusCode = statusCode;
    this.contentLength = contentLength;
    this.body = body;
    this.error = error;
    this.socket = socket;
  }

  // Helper method to create headers for the response. Input argument is the content type.
  private void createHeaders() {
    headers.clear();
    headers.add("HTTP/1.1 " + this.statusCode + " " + this.status);
    headers.add("Content-Type: " + "application/json");
    headers.add("Content-Length: " + this.contentLength);
    headers.add("Connection: close");
  }

  /*
   Helper method to build the error page. Input arguments are the error code, name and description.
   Method returns the error page in String form.
   */
  private String buildErrorPage(String code, String name, String description) {
    return "HTTP/1.1 " + code + " " + name + "\r\n" +
        "Content-Type: text/html\r\n\r\n" +
        "<!DOCTYPE html>\r\n" +
        "<html>\r\n" +
        "<head>\r\n" +
        "<title>" + code + " " + name + "</title>\r\n" +
        "</head>\r\n" +
        "<body>\r\n" +
        "<h1>" + code + " " + name + "</h1>\r\n" +
        "<p>" + description + "</p>\r\n" +
        "</body>\r\n" +
        "</html>\r\n";
  }

  /*
   Method to send HTTP response and HTML page to client via printing through the socket.
   */
  public void respond() {
    try {
      PrintStream ps = new PrintStream(socket.getOutputStream());
      createHeaders();
      ps.println(headers.get(0)); // Status line
      ps.println(headers.get(1)); // Content-Type
      ps.println(headers.get(2)); // Content-Length
      ps.println(headers.get(3)); // Connection
      ps.println();
      // Print body based on status code
      if (statusSuccess(statusCode)) {
        ps.println(body);
      } else {
        ps.println(buildErrorPage(statusCode, status, error));
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /*
   Helper method to check if the status code indicates success. Input argument is the status code,
   and the method returns a boolean.
   */
  private boolean statusSuccess(String statusCode) {
    return "200".equals(statusCode) || "201".equals(statusCode) || "204".equals(statusCode);
  }
}