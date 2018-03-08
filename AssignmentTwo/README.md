How to run this program:

//compile
javac dv_routing/*.java

//run coordinator
java dv_routing.DVCoordinator <textFile>

//run nodes, each is run in a separate window. You must run one DV node for each entry in the text file you passed in to the coordinator
//once you've started enough nodes, the connections will begin to happen
java -Djava.net.preferIPv4Stack=true dv_routing.DVNode
