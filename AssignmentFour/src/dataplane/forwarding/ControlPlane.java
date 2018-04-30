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
    private static int dvNodePortNumber;
    public static DVNode nodeFromCoordinator;
    public static HashMap<Integer, Integer> realForwardingTable = new HashMap<>();
    public static DVSender dvSender;
    public static List<String> neighborNodesIPList;

    public static void main(String[] args) {

        // default port number
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
            neighborNodesIPList = (List<String>) ois.readObject();
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

            //create port users and count your number of neighbors
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


            // setup DV Receiver
            MulticastSocket socketForListening = new MulticastSocket(12345);
            for (int i = 0; i < neighborNodesIPList.size(); i++){
                // if IP is empty at this index, then we don't need to listen to that neighbor
                if (!neighborNodesIPList.get(i).equals(""))  {
                    socketForListening.joinGroup(InetAddress.getByName("230.0.0." + Integer.toString(i)));
                }
            }
            DVReceiver dvReceiver = new DVReceiver(socketForListening);

            // start DV Receiver thread to be ready to listen for sends from DV Sender
            dvReceiver.start();


            // do one send to your neighbors to kick off the network setup
            // when DV Receiver receives, it will also do a send if it has updated Control Plane's DV and forwarding table
            DV dvToSendOverNetwork = new DV(nodeFromCoordinator.nodeNum);
            dvToSendOverNetwork.setDv(nodeFromCoordinator.dv);
            dvSender.send(dvToSendOverNetwork);


            // wait for your forwarding table to be full before you are able to forward messages
            // note, when the forwarding table is full, you might not yet have the shortest path, but that will still be working itself out
            while (realForwardingTable.containsValue(null))    {
                System.out.println("Waiting for network to be setup.");
                Thread.sleep(2000);
            } //end while


            // forwarding table is setup, but sleep a little extra in case a faster path is found
            Thread.sleep(2000);


            // DV Network is setup
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
            System.out.println("I am Node: " + nodeFromCoordinator.nodeNum);
            System.out.println("My DV's are:");
            for(int j: nodeFromCoordinator.dv) {
                System.out.print(j + " ");
            }
            System.out.println();
            System.out.println("My forwarding table is: ");
            System.out.println(realForwardingTable);


            // get this node's forwarding table
            HashMap<Integer, Integer> myForwardingTable = realForwardingTable;

            int numberOfMessagesReceived = 0;
            long startTimeOfReceivedMessages = System.currentTimeMillis();
            long endTimeOfReceivedMessage = 0;
            System.out.println("\nListening for a message...");
            while (true) {

                // listen for incoming connections
                MessageType receivedMessage = dataPlanePort.receive();
                endTimeOfReceivedMessage = System.currentTimeMillis();
                numberOfMessagesReceived++;
                int destinationNode = receivedMessage.getDestNode();
                System.out.println("\n\nRecieved a new message from source node: " + receivedMessage.getSourceNode() + " for destination node: " + destinationNode);

                // accept message if it's for you
                if (destinationNode == nodeFromCoordinator.nodeNum) {
                    System.out.println("WOOHOO! The packet got to where it needed to get!");
                }

                // forward message according to forwarding table
                else {
                    int nodeToForwardTo = myForwardingTable.get(destinationNode);
                    System.out.println("Sending to next node: " + nodeToForwardTo + " a packet for destination node: " + destinationNode);
                    byte[] pack = new byte[1024];
                    Arrays.fill(pack, (byte)nodeFromCoordinator.nodeNum);
                    MessageType msg = new MessageType(nodeFromCoordinator.nodeNum, destinationNode, pack);

                    //find the correct portuser who is connected to the correct neighbor node
                    for (PortUser portUser : portUserList) {
                        if (portUser.nodeId == nodeToForwardTo) {
                            portUser.send(msg);
                        }
                    }
                    //Thread.sleep(1000);
                }

                // calculate rate, update dv every 10 seconds
                long secondsBetweenStartAndEnd = (endTimeOfReceivedMessage - startTimeOfReceivedMessages) / 1000;
                if (secondsBetweenStartAndEnd > 10) {
                    System.out.println("It's been at least 10 seconds since last change.");
                    System.out.println("Received " + numberOfMessagesReceived + " message in " + secondsBetweenStartAndEnd + " seconds.");
                    double rate = 1 + (double) numberOfMessagesReceived / (double) secondsBetweenStartAndEnd;
                    System.out.println("Rate is: " + rate);

                    boolean needToSendUpdate = false;
                    //set new weight for neighbors
                    for (int i = 0; i < neighborNodesIPList.size(); i++){
                        if (!neighborNodesIPList.get(i).equals("")
                                && realForwardingTable.get(i) == i)  {
                            int newWeight = (int) Math.round(nodeFromCoordinator.dv[i] * rate);
                            if (newWeight != nodeFromCoordinator.dv[i]) {
                                System.out.println("\nRATE CHANGE - My OLD DV is: ");
                                for(int j: ControlPlane.nodeFromCoordinator.dv) {
                                    System.out.print(j + " ");
                                }
                                nodeFromCoordinator.dv[i] = newWeight;
                                System.out.println("\nRATE CHANGE - My NEW DV is: ");
                                for(int j: ControlPlane.nodeFromCoordinator.dv) {
                                    System.out.print(j + " ");
                                }
                                System.out.println();
                                needToSendUpdate = true;
                            }
                        }
                    }

                    //send out your new DV
                    if (needToSendUpdate) {
                        dvSender.send(dvToSendOverNetwork);
                        System.out.println("PAUSE");
                        Thread.sleep(100 * 60 * 1000);
                    }

                    //reset variables
                    numberOfMessagesReceived = 0;
                    startTimeOfReceivedMessages = System.currentTimeMillis();
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

}
