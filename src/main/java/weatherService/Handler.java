package weatherService;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Handler implements Runnable {

  private final ClientRequest clientRequest;
  private final Semaphore semaphore;
  private Response response;

  /*
   Simple constructor used by aggregation server. Input arguments are the main.weatherService.ClientRequest object for
   details about the request and a semaphore for concurrency.
   */
  public Handler(ClientRequest clientRequest, Semaphore semaphore) {
    this.clientRequest = clientRequest;
    this.semaphore = semaphore;
  }

  /*
   Override run method from Runnable. Method will attempt to acquire the semaphore and then call
   the helper method to process the client request. Semaphore must be released afterward.
   */
  @Override
  public void run() {
    try {
      semaphore.acquire();
      processReq(this.clientRequest);
      semaphore.release();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  /*
   Helper method to process a request made by a client. Input argument is the main.weatherService.ClientRequest object.
   Method either calls the PUT or GET handler if the input specifies, otherwise a 400 response is
   returned.
   */
  private void processReq(ClientRequest req) {
    Socket socket = req.getSocket();
    if (req.getRequestType().equals("PUT")) {
      processPUTRequest(req, socket);
    } else if (req.getRequestType().equals("GET")) {
      processGETRequest(req, socket);
    } else {
      response = new Response("Bad Request", "400", "0", "", "Invalid request type", socket);
      response.respond();
    }
  }

  /*
   Helper method to process a PUT request. Input arguments are the main.weatherService.ClientRequest object and the
   socket. Method gets the saved json data (saved by the aggregation server) and uses it to form
   the correct response.
   */
  private void processPUTRequest(ClientRequest req, Socket socket) {
    String FileName = "cs" + req.getContentServerID() + "_p" + req.getPID() + ".json";
    File file = new File(FileName);
    if (file.exists()) {
      String content_length = file.length() + "";
      if (file.length() == 0) {
        response = new Response("No Content", "204", "0", "", "", socket);
        response.respond();
      } else {
        JSONObject obj = isValid(FileName);
        if (obj == null) {
          response = new Response("Internal Server Error", "500", "0", "", "Invalid JSON data",
              socket);
          response.respond();
        } else {
          upload(obj);
          if (!Database.getInstance().alreadyConnected(req.getContentServerID())) {
            // If the content server is not already connected, add to connectedContentServers
            Database.getInstance().addContentServer(Integer.parseInt(req.getContentServerID()));
            response = new Response("Created", "201", content_length, obj.toString(), "", socket);
            response.respond();
          } else {
            // If it is already connected, restart its timer
            Database.getInstance().restartTimer(req.getContentServerID());
            response = new Response("OK", "200", content_length, obj.toString(), "", socket);
            response.respond();
          }
        }
      }
      // Delete the current request file as it is no longer needed
      if (!file.delete()) {
        throw new RuntimeException("Failed to delete file for PUT request handling");
      }
    }
  }

  /*
   Helper method to check if saved json data is valid. This method is called by "processPUTRequest".
   Input argument is the name of the file containing the saved json data. Method returns a
   JSONObject containing the data if it is valid, otherwise will return null.
   */
  private JSONObject isValid(String fileName) {
    File file = new File(fileName);
    JSONObject obj;
    try {
      String info = new String(Files.readAllBytes(Paths.get(file.toURI())));
      obj = new JSONObject(info);
      JSONArray jArray = obj.getJSONArray("data");
      for (int i = 0; i < jArray.length(); i++) {
        try {
          JSONObject json = jArray.getJSONObject(i);
          if (!json.has("id") || !json.has("name") || !json.has("state") || !json.has("time_zone")
              || !json.has("lat") || !json.has("lon") || !json.has("local_date_time") ||
              !json.has("local_date_time_full") || !json.has("air_temp") || !json.has("apparent_t")
              || !json.has("cloud") || !json.has("dewpt") || !json.has("press") ||
              !json.has("rel_hum") || !json.has("wind_dir") || !json.has("wind_spd_kmh") ||
              !json.has("wind_spd_kt")) {
            // If data is missing from the JSON array
            return null;
          }
        } catch (JSONException e) {
          throw new RuntimeException(e);
        }
      }
    } catch (IOException | JSONException e) {
      throw new RuntimeException(e);
    }
    return obj;
  }

  /*
   Helper method to upload new data to the database. Input argument is the JSONObject containing
   the weather data.
   */
  private void upload(JSONObject obj) {
    try {
      int contentServerID = obj.getInt("station_id");
      Database.getInstance().addToDatabase(contentServerID, obj);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  /*
   Helper method to process a GET request. Input arguments are the main.weatherService.ClientRequest object and the
   socket. The response created by the method is dependent on whether there is data for the
   specified content server ID in the database.
   */
  private void processGETRequest(ClientRequest req, Socket socket) {
    if (Database.getInstance().isIDInDatabase(req.getContentServerID())) {
      String dataByID = Database.getInstance().getDataByID(req.getContentServerID());
      String content_length = dataByID.length() + "";
      response = new Response("OK", "200", content_length, dataByID, "", socket);
      response.respond();
    } else {
      response = new Response("Not Found", "404", "0", "", "Data not found in database", socket);
      response.respond();
    }
  }
}