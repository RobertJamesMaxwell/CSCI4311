/*
 * CSCI 4311
 * Assignment 2 - Exchanging Distance Vector with Neighbors
 * Robert Maxwell
 * Spring 2018
 */

package dv_routing;

import java.io.*;
import java.net.*;
import java.util.*;

public class DVCoordinator {

    private static List<List<HashMap<Integer, Integer>>> adjacencyList = new ArrayList<>();
    private static HashMap<Integer, String> tableForDVNodeIPMapping = new HashMap<>();
    private static HashMap<Integer, Integer> tableForDVNodePortMapping = new HashMap<>();
    private static List<DVNode> dvNodes = new ArrayList<>();
    private static DatagramSocket socket = null;

    public static void main(String[] args) {

        //error message to user about correct usage of DVCoordinator
        if (args.length != 2) {
            System.out.println("Usage: java DVCoordinator <AdjacencyListInputTextFile> <port number> ");
            return;
        }

        //read in file with initial dvNode adjacency data
        File inputAdjacencyList = new File(args[0]);
        System.out.println("\nInput File is: " + inputAdjacencyList);
        readDataFromFile(inputAdjacencyList);
        System.out.println("AdjacencyList is: " + adjacencyList);

        createDVNodeNetwork();

        //create socket
        try {
            socket = new DatagramSocket(Integer.parseInt(args[1]));
        } catch (SocketException se) {
            se.printStackTrace();
        }

        connectToEveryDVNode();
        sendIPTableToEachDVNode();

        System.out.println("\n\nDV Coordinator has finished. Exiting...");

    }


    private static void readDataFromFile(File inputAdjacencyList) {
        try {
            Scanner inputScanner = new Scanner(inputAdjacencyList);
            while(inputScanner.hasNextLine()){
                String newLine = inputScanner.nextLine();
                if (newLine.contains("[") && newLine.contains("]")) {
                    readDataFromLine(newLine);
                }
            }
            inputScanner.close();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }

    private static void readDataFromLine(String newLine) {

        List<HashMap<Integer, Integer>> currentIndexAdjacencyList = new ArrayList<>();

        newLine = newLine.replace("[", "");
        newLine = newLine.replace("]", "");
        String[] neighborNodeAndDistanceVectors = newLine.split(",");

        for(String neighborNodeAndDistanceVector: neighborNodeAndDistanceVectors){

            //separate neighbor node from distance amount
            String[] neighborAndDistance = neighborNodeAndDistanceVector.split(":");

            //assign each to a new HashMap (need to trim whitespace for Integer.parseInt to work)
            HashMap<Integer, Integer> neighborNodeAndDistanceVectorMap = new HashMap();
            neighborNodeAndDistanceVectorMap.put(Integer.parseInt(neighborAndDistance[0].trim()), Integer.parseInt(neighborAndDistance[1].trim()));

            //add new HashMap to currentAdjacencyList which is the instanceVariable adjacencyList at index
            currentIndexAdjacencyList.add(neighborNodeAndDistanceVectorMap);
        }

        adjacencyList.add(currentIndexAdjacencyList);

    }

    private static void createDVNodeNetwork()   {

        System.out.println("Creating DVNode Network...");
        //set the DV nodes
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

        //print successful creation message
        System.out.println("\n\n\nYou have successfully created the following DV nodes.");
        for (DVNode node : dvNodes) {
            System.out.println("\n\nNode number: " + node.nodeNum);
            System.out.print("With dv's: ");
            for(int i: node.dv) {
                System.out.print(i + " ");
            }
        }
        System.out.println("\n\n");
    }

    private static void connectToEveryDVNode() {

        for(int i = 0; i < dvNodes.size(); i++) {
            try {

                // receive connection
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // unpack packet and send welcome message
                InetAddress nodeAddress = packet.getAddress();
                System.out.println("\n\nIP of packet number " + i + " received is: " + nodeAddress);
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
                System.out.println("Sending DV Node " + i + " its information: ");

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
    }

    private static void sendIPTableToEachDVNode() {
        // send each DVNode it's neighbor IP Mappings
        for(int i = 0; i < dvNodes.size(); i++) {
            try {
                byte[] buf = new byte[256];

                // calculate neighbor IP List
                List<String> neighborIPList = new ArrayList<>();
                for (int j = 0; j < dvNodes.size(); j++)    {
                    if(dvNodes.get(i).dv[j] == Integer.MAX_VALUE || dvNodes.get(i).dv[j] == 0)   {
                        // add blank string at index if that index is NOT a neighbor or yourself
                        neighborIPList.add("");
                    } else {
                        neighborIPList.add(tableForDVNodeIPMapping.get(j));
                    }
                }

                // send packet with neighbor IP table
                ByteArrayOutputStream baos = new ByteArrayOutputStream(6400);
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(neighborIPList);
                byte[] data = baos.toByteArray();
                InetAddress currentNodeIP = InetAddress.getByName(tableForDVNodeIPMapping.get(i).replace("/", ""));
                System.out.println("\n\nDV Node number " + i + " has IP: "+ currentNodeIP);
                int currentNodePort = tableForDVNodePortMapping.get(i);
                System.out.println("DV Node number " + i + " has Port: "+ currentNodePort);
                DatagramPacket packet = new DatagramPacket(data, data.length, currentNodeIP, currentNodePort);
                System.out.println("Sending the following IP Table to Node: " + i);
                System.out.println(neighborIPList);
                socket.send(packet);


            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
