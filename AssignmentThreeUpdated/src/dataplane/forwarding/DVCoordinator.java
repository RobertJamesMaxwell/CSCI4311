/*
 * CSCI 4311
 * Assignment 2 - Exchanging Distance Vector with Neighbors
 * Robert Maxwell
 * Spring 2018
 */

package dataplane.forwarding;

import java.io.*;
import java.net.*;
import java.util.*;

public class DVCoordinator {

    public static List<List<HashMap<Integer, Integer>>> adjacencyList = new ArrayList<>();
    public static HashMap<Integer, String> tableForDVNodeIPMapping = new HashMap<>();
    public static HashMap<Integer, Integer> tableForDVNodePortMapping = new HashMap<>();

    public static void main(String[] args) {

        //error message to user about correct usage of DVCoordinator
        if (args.length != 1) {
            System.out.println("Usage: java DVCoordinator <AdjacencyListInputTextFile>");
            return;
        }

        //read in file with initial dvNode adjacency data
        File inputAdjacencyList = new File(args[0]);
        System.out.println("Input File is: " + inputAdjacencyList);
        readDataFromFile(inputAdjacencyList);
        System.out.println("\n\nAdjacencyList is: " + adjacencyList);

        //set the DV nodes
        List<DVNode> dvNodes = new ArrayList<>();
        for (int i = 0; i < adjacencyList.size(); i++)  {
            DVNode node = new DVNode();
            node.nodeNum = i;
            node.dv = new int[adjacencyList.size()];

            //set the distance vectors from the list of neighbors
            for (int j = 0; j < node.dv.length; j++)  {
                for (HashMap<Integer, Integer> neighborDV: adjacencyList.get(i))    {
                    if (i == j) {
                        //set the distance vector to zero for your own nodeNum
                        node.dv[i] = 0;
                    } else if (neighborDV.get(j) == null )  {
                        //if you don't have a neighbor at this index, set it to infinity
                        node.dv[j] = Integer.MAX_VALUE;
                    } else {
                        //set your neighbors distance
                        node.dv[j] = neighborDV.get(j);
                        break;
                    }
                }
            }
            dvNodes.add(node);
        }

        System.out.println("\nYou have successfully created the following DV nodes.\n");
        for (DVNode node : dvNodes) {
            System.out.println("\n\nNode number: " + node.nodeNum);
            System.out.println("With dv's: ");
            for(int i: node.dv) {
                System.out.print(i + " ");
            }
        }
        System.out.println("\n\n");


        //Wait for a connection for each of DV Nodes in dvNodes list
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(12100);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        for(int i = 0; i < dvNodes.size(); i++) {
            try {

                // receive connection
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // unpack packet and send welcome message
                InetAddress nodeAddress = packet.getAddress();
                System.out.println("\n\nIP of packet received is: " + nodeAddress);
                int nodePort = packet.getPort();
                String welcome = "Hello From the DV Coordinator!";
                buf = welcome.getBytes();
                packet = new DatagramPacket(buf, buf.length, nodeAddress, nodePort);
                socket.send(packet);

                // store DVNode's IP address in IP table and Port in port table
                tableForDVNodeIPMapping.put(i, nodeAddress.toString());
                tableForDVNodePortMapping.put(i, nodePort);

                System.out.println("IP Table: " + tableForDVNodeIPMapping);
                System.out.println("Port Table: " + tableForDVNodePortMapping);

                // send packet with DV Node object
                DVNode nodeToSendOverNetwork = dvNodes.get(i);
                ByteArrayOutputStream baos = new ByteArrayOutputStream(6400);
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(nodeToSendOverNetwork);
                byte[] data = baos.toByteArray();
                packet = new DatagramPacket(data, data.length, nodeAddress, nodePort);
                socket.send(packet);


            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }


        // send each DVNode it's neighbor IP Mappings
        for(int i = 0; i < dvNodes.size(); i++) {
            try {
                byte[] buf = new byte[256];

                // calculate neighbor IP List
                List<String> neighborIPList = new ArrayList<>();
                List<String> neighborPortList = new ArrayList<>();
                for (int j = 0; j < dvNodes.size(); j++)    {
                    if(dvNodes.get(i).dv[j] == Integer.MAX_VALUE)   {
                        // add blank string at index if that index is NOT a neighbor
                        neighborIPList.add("");
                        neighborPortList.add("");
                    } else {
                        neighborIPList.add(tableForDVNodeIPMapping.get(j));
                        neighborPortList.add(tableForDVNodePortMapping.get(j).toString());
                    }
                }


                // send packet with neighbor IP table
                ByteArrayOutputStream baos = new ByteArrayOutputStream(6400);
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(neighborIPList);
                byte[] data = baos.toByteArray();
                InetAddress currentNodeIP = InetAddress.getByName(tableForDVNodeIPMapping.get(i).replace("/", ""));
                System.out.println("Current Node IP: "+ currentNodeIP);
                int currentNodePort = tableForDVNodePortMapping.get(i);
                System.out.println("Current Node Port: "+ currentNodePort);
                DatagramPacket packet = new DatagramPacket(data, data.length, currentNodeIP, currentNodePort);
                System.out.println("Sending the following IP Table to Node: " + i);
                System.out.println(neighborIPList);
                socket.send(packet);

                // send packet with neighbor Port table
                baos = new ByteArrayOutputStream(6400);
                oos = new ObjectOutputStream(baos);
                oos.writeObject(neighborPortList);
                data = baos.toByteArray();
                packet = new DatagramPacket(data, data.length, currentNodeIP, currentNodePort);
                System.out.println("Sending the following Port Table to Node: " + i);
                System.out.println(neighborPortList);
                socket.send(packet);


            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        System.out.println("\n---------------------------------");
        System.out.println("-----------------------------------");
        System.out.println("-----------------------------------");
        System.out.println("DVCoordinator will connect to all nodes in 10 seconds...");
        try {
            Thread.sleep(10000);

            //setup connection to all FWNodes
            List<PortUser> allNodesAsPortUsers = new ArrayList<>();
            for (int i = 0 ; i < dvNodes.size(); i++ ) {
                String nodeIP = tableForDVNodeIPMapping.get(i).replace("/", "");
                int nodePort = 12100 + i;
                PortUser nodeAsPortUser = new PortUser(i, nodeIP, nodePort);
                nodeAsPortUser.initialize();
                allNodesAsPortUsers.add(nodeAsPortUser);
            }

            System.out.println("\nDVCoordinator will begin sending random packets in 10 seconds...");
            Thread.sleep(10000);

            //send random messages
            for (int i = 0; i < 1; i++)    {

                //pick a random node as a source and destination
                System.out.println("\nSending message number: " + i);
                int sourceNode = (int)Math.floor( Math.random() * (dvNodes.size()) );
                int destinationNode = (int)Math.floor( Math.random() * (dvNodes.size()) );

                //send message
                System.out.println("Sending to source node " + sourceNode + " a packet for destination node " + destinationNode);
                byte[] pack = new byte[1024];
                Arrays.fill(pack, (byte)i);
                MessageType msg = new MessageType(sourceNode, destinationNode, pack);
                allNodesAsPortUsers.get(sourceNode).send(msg);
                //sourcePortUser.close();
                Thread.sleep(10000);
            }

            //close all connections
            for(PortUser portUser: allNodesAsPortUsers) {
              //  portUser.close();
            }
            while(true) {
                System.out.println("WAITING...");
                Thread.sleep(10000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private static void readDataFromFile(File inputAdjacencyList) {
        try {
            Scanner inputScanner = new Scanner(inputAdjacencyList);
            while(inputScanner.hasNextLine()){
                String newLine = inputScanner.nextLine();
                if (newLine.contains("[") && newLine.contains("]")) {
                    System.out.println("New Line: " + newLine);
                    transformTextLineIntoNodeData(newLine);
                }
            }
            inputScanner.close();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }

    private static void transformTextLineIntoNodeData(String newLine) {

        List<HashMap<Integer, Integer>> currentIndexAdjacencyList = new ArrayList<>();

        //remove brackets from string
        newLine = newLine.replace("[", "");
        newLine = newLine.replace("]", "");

        //split string on commas to get a string array of each mapping of neighborNode:distanceVector
        String[] neighborNodeAndDistanceVectors = newLine.split(",");

        //loop through each neighborNode:distanceVector, and add the data to the adjacencyList instance variable
        for(String neighborNodeAndDistanceVector: neighborNodeAndDistanceVectors){
            System.out.println("Neighbor Node and Distance Vector : " + neighborNodeAndDistanceVector);

            //separate neighbor node from distance amount
            //neighborAndDistance with have length two with index 0 = neighbor and index 1 = distance
            String[] neighborAndDistance = neighborNodeAndDistanceVector.split(":");

            //assign each to a new HashMap (need to trim whitespace for Integer.parseInt to work)
            HashMap<Integer, Integer> neighborNodeAndDistanceVectorMap = new HashMap();
            neighborNodeAndDistanceVectorMap.put(Integer.parseInt(neighborAndDistance[0].trim()), Integer.parseInt(neighborAndDistance[1].trim()));
            System.out.println(neighborNodeAndDistanceVectorMap);

            //add new HashMap to currentAdjacencyList which is the instanceVariable adjacencyList at index
            currentIndexAdjacencyList.add(neighborNodeAndDistanceVectorMap);
        }

        //add the new adjacencyList for this index to the full adjacencyList instance variable
        adjacencyList.add(currentIndexAdjacencyList);

    }

}
