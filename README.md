# Distributed

mvn exec:java -Dexec.mainClass=weatherService.AggregationServer -Dexec.args="4567"

mvn exec:java -Dexec.mainClass=weatherService.ContentServer -Dexec.args="http://localhost:4567 1 cs1.txt"

mvn exec:java -Dexec.mainClass=weatherService.GETClient -Dexec.args="http://localhost:4567 1"
