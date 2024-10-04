package weatherService;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class QueueHandler {

  // Singleton instance of main.weatherService.QueueHandler
  private static final QueueHandler instance = new QueueHandler();

  // Queue to hold client requests with a capacity of 100.
  private static BlockingQueue<ClientRequest> queue = new ArrayBlockingQueue<>(100);

  // Private constructor
  private QueueHandler() {
  }

  // Static method to get main.weatherService.QueueHandler instance
  public static QueueHandler getInstance() {
    return instance;
  }

  /*
   Method to add a request to the queue. Input argument is the main.weatherService.ClientRequest object. Queue has a
   capacity of 100 requests to limit memory and resource usage (This could be altered depending on
   the requirements of the system). Client request is added to the queue depending on it's lamport
   timestamp primarily and then its PID.
   */
  public void addRequestToQueue(ClientRequest req) {
    try {
      BlockingQueue<ClientRequest> newQueue = new ArrayBlockingQueue<>(100);
      ClientRequest currReq = req;
      for (ClientRequest cr : queue) {
        int lamportCompare = Integer.parseInt(cr.getLamportTimeStamp())
            - Integer.parseInt(currReq.getLamportTimeStamp());
        if (lamportCompare < 0 || (lamportCompare == 0 && Integer.parseInt(cr.getPID()) <
            Integer.parseInt(currReq.getPID()))) {
          /* Add the existing request to the new queue if the timestamp is lower, or if equal and
          PID is lower */
          newQueue.add(cr);
        } else {
          // Otherwise add current request
          newQueue.add(currReq);
          currReq = cr;
        }
      }
      newQueue.add(currReq);
      queue.clear();
      queue = newQueue;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /*
   Method to retrieve and remove the next request from the queue. Method returns the next client
   request to handle.
   */
  public ClientRequest getRequestFromQueue() {
    ClientRequest req;
    try {
      req = queue.take();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return req;
  }
}