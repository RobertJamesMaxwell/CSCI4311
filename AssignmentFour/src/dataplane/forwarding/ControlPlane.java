/*
 * CSCI 4311
 * Assignment 4 - DV Algorithm
 * Robert Maxwell, Chris Schilling
 * Spring 2018
 */

package dataplane.forwarding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ControlPlane {

    private static DataPlanePort dataPlanePort;
    private static List<PortUser> portUserList = new ArrayList<>();
    private static List<HashMap<Integer, Integer>> entireForwardingTable = new ArrayList<>();
    private static int dvNodePortNumber;
    //public static boolean needToChange = false;
    public static DVNode nodeFromCoordinator;
    public static HashMap<Integer, Integer> realForwardingTable = new HashMap<>();
    public static DVSender dvSender;

    public static void main(String[] args) {

        //setup forwarding table
        setupForwardingTable();


        dvNodePortNumber = 12110;

        try {
            // send connection request to coordinator
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = new byte[256];
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 12100);
            socket.send(packet);

            // get welcome message response
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("From Server: " + received);

            //receive DVNode info from DVCoordinator
            socket.receive(packet);
            buf = packet.getData();
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(bais);
            nodeFromCoordinator = (DVNode) ois.readObject();
            System.out.println("Received DVNode from server!");
            System.out.println("\n\nNode number: " + nodeFromCoordinator.nodeNum);
            System.out.println("With dv's: ");
            for (int i : nodeFromCoordinator.dv) {
                System.out.print(i + " ");
            }

            //receive neighbor nodes IP addresses from DVCoordinator
            socket.receive(packet);
            buf = packet.getData();
            bais = new ByteArrayInputStream(buf);
            ois = new ObjectInputStream(bais);
            List<String> neighborNodesIPList = (List<String>) ois.readObject();
            System.out.println("\n\nReceived the following IP neighbor mapping from server!");
            System.out.println(neighborNodesIPList);

            //receive neighbor nodes Port numbers from DVCoordinator
            socket.receive(packet);
            buf = packet.getData();
            bais = new ByteArrayInputStream(buf);
            ois = new ObjectInputStream(bais);
            List<String> neighborNodesPortList = (List<String>) ois.readObject();
            System.out.println("\n\nReceived the following Port neighbor mapping from server!");
            System.out.println(neighborNodesPortList);

            int numberOfNeighbors = 0;
            for (int i = 0; i < nodeFromCoordinator.dv.length; i++) {
                if (nodeFromCoordinator.dv[i] != Integer.MAX_VALUE && nodeFromCoordinator.dv[i] != 0) {
                    System.out.println("Iteration number i = " + i);
                    numberOfNeighbors++;
                    portUserList.add(new PortUser(i, neighborNodesIPList.get(i).replace("/", ""), 12100 + i));
                }
            }
            System.out.println("Number of neighbors is: " + numberOfNeighbors);

            //setup dataplane port for listening
            System.out.println("Running DataPlane Port stuff....");
            dataPlanePort = new DataPlanePort(12100 + nodeFromCoordinator.nodeNum, numberOfNeighbors + 1);  // add extra 1 for DVCoordinator to connect to send messages
            new Thread(dataPlanePort).start();

            //setup portUser for sending (one for each neighbor)
            System.out.println("Running Port User stuff....");
            for (PortUser portUser: portUserList){
                System.out.println(("Port User IP: " + portUser.ip));
                System.out.println(("Port User Port: " + portUser.port));
                portUser.initialize();
            }

            //setup your real forwarding table
            for (int i = 0; i < nodeFromCoordinator.dv.length; i++) {
                if (nodeFromCoordinator.dv[i] != Integer.MAX_VALUE)   {
                    realForwardingTable.put(i, i);
                } else {
                    realForwardingTable.put(i, null);
                }
            }
            System.out.println("My Real Forwarding Table is: " + realForwardingTable);




            // setup DV Sender
            dvSender = new DVSender();
            int multicastSendPort = dvNodePortNumber + nodeFromCoordinator.nodeNum;
            System.out.println("\n\nMultiCast Sending Port for node " + nodeFromCoordinator.nodeNum + " is " + multicastSendPort);
            dvSender.setMulticastPortNumber(12345);  // must be the same as multicast receiver port!!!
            String multicastSendIP = "230.0.0." + nodeFromCoordinator.nodeNum;
            System.out.println("MultiCast IP for node " + nodeFromCoordinator.nodeNum + " is " + multicastSendIP);
            dvSender.setMulticastIPNumber(multicastSendIP);


            // setup DV Receiver --needs to be threaded???
            MulticastSocket socketForListening = new MulticastSocket(12345);
            //List<MulticastSocket> socketsForListening = new ArrayList<>();
            for (int i = 0; i < neighborNodesIPList.size(); i++){
                // if IP is empty at this index, then we don't need to listen to that neighbor
                if (!neighborNodesIPList.get(i).equals(""))  {
                    //MulticastSocket listeningSocket = new MulticastSocket(dvNodePortNumber + i);
                    socketForListening.joinGroup(InetAddress.getByName("230.0.0." + Integer.toString(i)));
                    //socketsForListening.add(listeningSocket);
                }
            }

            DVReceiver dvReceiver = new DVReceiver(socketForListening);
            //dvReceiver.setSocketsForListening(socketsForListening);

            dvReceiver.start();

            DV dvToSendOverNetwork = new DV(nodeFromCoordinator.nodeNum);
            dvToSendOverNetwork.setDv(nodeFromCoordinator.dv);
            dvSender.send(dvToSendOverNetwork);


            //send your DV Object to your neighbors so they can update their tables -- loop this

            //if DV Receiver updates your own DV based on what it receives, re-update your DV and send it
            //int numberOfSendsToSetupNetwork = 0;
            while (realForwardingTable.containsValue(null))    {
                System.out.println("Waiting for network to be setup.");
                Thread.sleep(2000);


                //send your DV Object to your neighbors so they can update their tables -- loop this
//                System.out.println("Sending and sleeping for 2 seconds");
//                dvSender.send(dvToSendOverNetwork);
//                Thread.sleep(2 * 1000);
                //if (needToChange)   {
                  //  needToChange = false;
                   // System.out.println("CHAAANGE NUMBER " + loopCount);
                    //update your forwarding table
                   // DV newDV = DVReceiver.receivedDV;



//                    System.out.println("Sleeping for 10 seconds");
//                    Thread.sleep(10*1000);
                    //check if your forwarding table is full, if it is, then break, because you're ready to move to the next loop to listening for traffic


//                if (realForwardingTable.containsValue(null))  {
//                    System.out.println("Still don't have all values...");
//                } else {
//                    System.out.println("Sending extra send");
//                    //for (int i = 0; i < 100; i++)   {
//                        dvSender.send(dvToSendOverNetwork);
//                    //    Thread.sleep(30);
//                    //}
//                    System.out.println("Breaking!");
//                    break;
//                }

                //}
                //numberOfSendsToSetupNetwork++;
            } //end while


//            System.out.println("SLEEPING FOREVER");
//            Thread.sleep(100 * 60 * 1000);

            //dvReceiver.stop();
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("========================================================");
            System.out.println("DV NETWORK IS SETUP!!!!");
            //System.out.println("Number of Sends needed to setup network: " + numberOfSendsToSetupNetwork);
            System.out.println("I am Node: " + nodeFromCoordinator.nodeNum);
            System.out.println("My DV's are:");
            for(int j: nodeFromCoordinator.dv) {
                System.out.print(j + " ");
            }
            System.out.println();
            System.out.println("My forwarding table is: ");
            System.out.println(realForwardingTable);





            //get this node's forwarding table
            //HashMap<Integer, Integer> myForwardingTable = entireForwardingTable.get(nodeFromCoordinator.nodeNum);
            HashMap<Integer, Integer> myForwardingTable = realForwardingTable;

            //listen for incoming connections
            while (true) {
                System.out.println("\nListening for a message...");
                MessageType receivedMessage = dataPlanePort.receive();
                int destinationNode = receivedMessage.getDestNode();
                System.out.println("Recieved a new message from source node: " + receivedMessage.getSourceNode() + " for destination node: " + destinationNode);

                if (destinationNode == nodeFromCoordinator.nodeNum) {
                    System.out.println("WOOHOO! The packet got to where it needed to get!");
                } else {
                    //forward packet according to forwarding table
                    int nodeToForwardTo = myForwardingTable.get(destinationNode);
                    System.out.println("Sending to next node: " + nodeToForwardTo + " a packet for destination node: " + destinationNode);
                    byte[] pack = new byte[1024];
                    Arrays.fill(pack, (byte)nodeFromCoordinator.nodeNum);
                    MessageType msg = new MessageType(nodeFromCoordinator.nodeNum, destinationNode, pack);

                    //find the correct portuser who is connected to the correct neighbor node
                    for (PortUser portUser : portUserList) {
                        if (portUser.nodeId == nodeToForwardTo) {
                            System.out.println("This is the correct PortUser to forward to: " + portUser.nodeId);
                            System.out.println("Forwarding to next node: " + nodeToForwardTo);
                            portUser.send(msg);
                        }
                    }
                    Thread.sleep(1000);

                    //update your DV because cost has changeed?
                    //if cost has changed enough, do DVSender.send
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void setupForwardingTable() {

        //node 0
        HashMap<Integer, Integer> nodeZero = new HashMap<Integer, Integer>();
        nodeZero.put(0,0);
        nodeZero.put(1,1);
        nodeZero.put(2,3);
        nodeZero.put(3,3);
        nodeZero.put(4,3);
        nodeZero.put(5,3);
        nodeZero.put(6,3);
        nodeZero.put(7,1);
        nodeZero.put(8,8);
        entireForwardingTable.add(nodeZero);

        //node 1
        HashMap<Integer, Integer> nodeOne = new HashMap<Integer, Integer>();
        nodeOne.put(0,0);
        nodeOne.put(1,1);
        nodeOne.put(2,7);
        nodeOne.put(3,0);
        nodeOne.put(4,0);
        nodeOne.put(5,7);
        nodeOne.put(6,7);
        nodeOne.put(7,7);
        nodeOne.put(8,0);
        entireForwardingTable.add(nodeOne);

        //node 2
        HashMap<Integer, Integer> nodeTwo = new HashMap<Integer, Integer>();
        nodeTwo.put(0,3);
        nodeTwo.put(1,7);
        nodeTwo.put(2,2);
        nodeTwo.put(3,3);
        nodeTwo.put(4,3);
        nodeTwo.put(5,5);
        nodeTwo.put(6,5);
        nodeTwo.put(7,7);
        nodeTwo.put(8,3);
        entireForwardingTable.add(nodeTwo);

        //node 3
        HashMap<Integer, Integer> nodeThree = new HashMap<Integer, Integer>();
        nodeThree.put(0,0);
        nodeThree.put(1,0);
        nodeThree.put(2,2);
        nodeThree.put(3,3);
        nodeThree.put(4,4);
        nodeThree.put(5,2);
        nodeThree.put(6,2);
        nodeThree.put(7,2);
        nodeThree.put(8,0);
        entireForwardingTable.add(nodeThree);

        //node 4
        HashMap<Integer, Integer> nodeFour = new HashMap<Integer, Integer>();
        nodeFour.put(0,3);
        nodeFour.put(1,3);
        nodeFour.put(2,3);
        nodeFour.put(3,3);
        nodeFour.put(4,4);
        nodeFour.put(5,3);
        nodeFour.put(6,3);
        nodeFour.put(7,3);
        nodeFour.put(8,8);
        entireForwardingTable.add(nodeFour);

        //node 5
        HashMap<Integer, Integer> nodeFive = new HashMap<Integer, Integer>();
        nodeFive.put(0,2);
        nodeFive.put(1,2);
        nodeFive.put(2,2);
        nodeFive.put(3,2);
        nodeFive.put(4,2);
        nodeFive.put(5,5);
        nodeFive.put(6,6);
        nodeFive.put(7,2);
        nodeFive.put(8,2);
        entireForwardingTable.add(nodeFive);

        //node 6
        HashMap<Integer, Integer> nodeSix = new HashMap<Integer, Integer>();
        nodeSix.put(0,5);
        nodeSix.put(1,5);
        nodeSix.put(2,5);
        nodeSix.put(3,5);
        nodeSix.put(4,5);
        nodeSix.put(5,5);
        nodeSix.put(6,6);
        nodeSix.put(7,5);
        nodeSix.put(8,5);
        entireForwardingTable.add(nodeSix);

        //node 7
        HashMap<Integer, Integer> nodeSeven = new HashMap<Integer, Integer>();
        nodeSeven.put(0,1);
        nodeSeven.put(1,1);
        nodeSeven.put(2,2);
        nodeSeven.put(3,2);
        nodeSeven.put(4,2);
        nodeSeven.put(5,2);
        nodeSeven.put(6,2);
        nodeSeven.put(7,7);
        nodeSeven.put(8,1);
        entireForwardingTable.add(nodeSeven);

        //node 8
        HashMap<Integer, Integer> nodeEight = new HashMap<Integer, Integer>();
        nodeEight.put(0,0);
        nodeEight.put(1,0);
        nodeEight.put(2,0);
        nodeEight.put(3,0);
        nodeEight.put(4,0);
        nodeEight.put(5,0);
        nodeEight.put(6,0);
        nodeEight.put(7,0);
        nodeEight.put(8,8);
        entireForwardingTable.add(nodeEight);
    }

    public boolean changed() {
        return true;
    }
}
