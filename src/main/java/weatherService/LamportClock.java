package weatherService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LamportClock {

  private int processID;
  private int timeStamp;

  // Files to save state of lamport clock in case of a crash
  private static final String serverLamportFile = "LAMPORT_CLOCK.txt";
  private static final String PIDFile = "PID_COUNTER.txt";

  public LamportClock(int processID, int timeStamp) {
    this.processID = processID;
    this.timeStamp = timeStamp;
  }

  /*
   Method to initialise the lamport clock, or re-initialise in the case of a crash. Method returns
   the main.weatherService.LamportClock object.
   */
  public static LamportClock initialiseLamportClock() {
    File savedClock = new File(serverLamportFile);
    LamportClock clock;
    if (savedClock.exists()) {
      // If exists then the server must have crashed, reinitialise to saved values
      clock = new LamportClock(0, 0);
      int[] lamportValues = clock.getLamportData();
      if (lamportValues != null) {
        clock.setProcessID(lamportValues[0]);
        clock.setTimeStamp(lamportValues[1]);
      }
    } else {
      // If it does not exist than it must be a new server, initialise values and files
      clock = new LamportClock(0, 0);
      clock.initialiseServerLamportFile();
      clock.initialisePIDFile();
    }
    return clock;
  }

  // Method to delete lamport files on intentional server shutdown.
  public void deleteFiles() {
    File savedClock = new File(serverLamportFile);
    if (savedClock.exists()) {
      if (!savedClock.delete()) {
        System.err.println("Could not delete lamport file");
      }
    } else {
      System.err.println("Error: cannot delete " + serverLamportFile);
    }
    File savedPID = new File(PIDFile);
    if (savedPID.exists()) {
      if (!savedPID.delete()) {
        System.err.println("Could not delete PID file");
      }
    } else {
      System.err.println("Error: cannot delete " + PIDFile);
    }
  }

  // Method to increment the Lamport Clock's timestamp
  public void tick() {
    this.timeStamp++;
  }

  /*
   Method to synchronise the Lamport Clock. Input argument is the received timestamp, which is used
   to correctly update the clock.
   */
  public void synchroniseClock(int receivedTimeStamp) {
    this.timeStamp = Math.max(this.timeStamp, receivedTimeStamp) + 1;
  }

  /*
   Method to initialise the server's lamport clock file. Method will write a timestamp and PID of 0
   to the file on initialisation.
   */
  private void initialiseServerLamportFile() {
    FileWriter serverLamportFileWriter = null;
    try {
      File savedClock = new File(serverLamportFile);
      if (!savedClock.exists()) {
        if (!savedClock.createNewFile()) {
          System.err.println("Could not create new " + serverLamportFile);
        }
      }
      serverLamportFileWriter = new FileWriter(savedClock);
      serverLamportFileWriter.write("PID: 0\nTimeStamp: 0");
      serverLamportFileWriter.flush();
      serverLamportFileWriter.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (serverLamportFileWriter != null) {
        try {
          serverLamportFileWriter.close();
        } catch (IOException e) {
          System.err.println("Could not close file writer: " + e.getMessage());
        }
      }
    }
  }

  /*
   Method to initialise the server's PID counter file. Method will write a current PID count of 0
   to the file on initialisation.
   */
  private void initialisePIDFile() {
    FileWriter PIDFileWriter = null;
    try {
      File savedCounter = new File(PIDFile);
      if (!savedCounter.exists()) {
        if (!savedCounter.createNewFile()) {
          System.err.println("Could not create new " + PIDFile);
        }
      }
      PIDFileWriter = new FileWriter(savedCounter);
      PIDFileWriter.write("Current PID count: 0");
      PIDFileWriter.flush();
      PIDFileWriter.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (PIDFileWriter != null) {
        try {
          PIDFileWriter.close();
        } catch (IOException e) {
          System.err.println("Could not close file writer: " + e.getMessage());
        }
      }
    }
  }

  /*
   Helper method to get lamport clock data from "serverLamportFile" file. Method returns an array
   of two integers, the PID and the timestamp.
   */
  private int[] getLamportData() {
    int pid = -1;
    int time = -1;
    BufferedReader bufferedValueReader = null;
    try {
      File savedClock = new File(serverLamportFile);
      if (savedClock.exists()) {
        FileReader read = new FileReader(savedClock);
        bufferedValueReader = new BufferedReader(read);
        String line;
        while ((line = bufferedValueReader.readLine()) != null) {
          int getNum = Integer.parseInt(line.substring(line.indexOf(":") + 2).trim());
          if (line.startsWith("PID:")) {
            pid = getNum;
          } else if (line.startsWith("TimeStamp:")) {
            time = getNum;
          }
        }
        bufferedValueReader.close();
        if (pid != -1 && time != -1) {
          return new int[]{pid, time};
        } else {
          System.err.println("Error: Lamport clock file incorrect");
        }
      } else {
        System.err.println("Error: Lamport clock file not found");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (bufferedValueReader != null) {
        try {
          bufferedValueReader.close();
        } catch (IOException e) {
          System.err.println("Could not close buffered reader: " + e.getMessage());
        }
      }
    }
    return null;
  }

  /*
   Method to update the server's lamport clock file with new values. Input argument is the
   timestamp to which the server lamport file needs to be updated to. The process ID variable is
   not changed.
   */
  public void updateLamportFile(int timestamp) {
    FileWriter updateFileWriter = null;
    try {
      File savedClock = new File(serverLamportFile);
      if (!savedClock.exists()) {
        if (!savedClock.createNewFile()) {
          System.err.println("Could not create new " + serverLamportFile);
        }
      }
      updateFileWriter = new FileWriter(serverLamportFile);
      updateFileWriter.write("PID: " + this.processID + "\nTimeStamp: " + timestamp);
      updateFileWriter.flush();
      updateFileWriter.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (updateFileWriter != null) {
        try {
          updateFileWriter.close();
        } catch (IOException e) {
          System.err.println("Could not close file writer: " + e.getMessage());
        }
      }
    }
  }

  /*
   Method to get a new unique PID for allocation to content server or client. Method returns an int
   corresponding to the new process ID. Method reads the "PIDFile" file which contains the current
   PID count. This is incremented for the next available PID. This method also updates this file
   accordingly.
   */
  public static int getNewPID() {
    int newPID = -1;
    BufferedReader bufferedValueReader = null;
    FileWriter updateFileWriter = null;
    try {
      File savedCounter = new File(PIDFile);
      if (savedCounter.exists()) {
        // Get new PID
        FileReader read = new FileReader(savedCounter);
        bufferedValueReader = new BufferedReader(read);
        String line;
        while ((line = bufferedValueReader.readLine()) != null) {
          if (line.startsWith("Current PID count:")) {
            newPID = Integer.parseInt(line.substring(line.indexOf(":") + 2).trim());
            newPID++;
            break;
          }
        }
        bufferedValueReader.close();

        // Update "PIDFile" file
        updateFileWriter = new FileWriter(savedCounter);
        updateFileWriter.write("Current PID count: " + newPID);
        updateFileWriter.flush();
        updateFileWriter.close();

        System.out.println("New PID allocated: " + newPID);
        return newPID;
      } else {
        System.err.println("Error: PID file not found");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (bufferedValueReader != null) {
        try {
          bufferedValueReader.close();
        } catch (IOException e) {
          System.err.println("Could not close buffered reader: " + e.getMessage());
        }
      }
      if (updateFileWriter != null) {
        try {
          updateFileWriter.close();
        } catch (IOException e) {
          System.err.println("Could not close file writer: " + e.getMessage());
        }
      }
    }
    return newPID;
  }

  // Helper function to set the timestamp. Input argument is the lamport clock timestamp as an int.
  private void setTimeStamp(int timestamp) {
    this.timeStamp = timestamp;
  }

  // Helper function to set the process ID. Input argument is the process ID as an int.
  private void setProcessID(int processID) {
    this.processID = processID;
  }

  // Helper function to get the timestamp. Returns an int.
  public int getTimeStamp() {
    return timeStamp;
  }

  // Helper function to get the process ID. Returns an int.
  public int getProcessID() {
    return processID;
  }
}