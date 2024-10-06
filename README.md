# Distributed

Running the project manually:

First run mvn clean install in one terminal.
To run the aggregation server on the default port, in the same terminal run the following:

```bash
mvn exec:java -Dexec.mainClass=weatherService.AggregationServer
```

To run the content server and PUT the local content into the database with station ID 1, run the following command in 
a separate terminal:

```bash
mvn exec:java -Dexec.mainClass=weatherService.ContentServer -Dexec.args="http://localhost:4567 1 cs1.txt"
```

In another separate terminal, run the following command to GET the weather data. Ensure that 30 seconds have no elapsed.

```bash
mvn exec:java -Dexec.mainClass=weatherService.GETClient -Dexec.args="http://localhost:4567 1"
```

Please see the integrated tests for different tests. This is just an example of how to run the project.

Testing:

Testing the modules working alongside the Aggregation Server has been done using JUnit and Maven. These can all be run 
using mvn clean install, which will also compile the code for manual running as explained above. It should be noted 
that the Response class was not tested separately, as the Handler class tests incorporate every possible test for the 
Response class (every possible response is tested).

The AggregationServer class itself as well as the ContentServer have had their start and stop server functionality 
tested, however the overall functionality of these two classes in combination with the GETClient class has been tested 
with integrated tests. The integrated tests can be found in the integratedTests folder, where each file contains 
instructions on how to perform the test and the expected results.