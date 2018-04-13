package dataplane.forwarding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FWNode {

    private static DataPlanePort dataPlanePort;
    private static List<PortUser> portUserList = new ArrayList<>();
    private DVNode dvNode;
    private static List<HashMap<Integer, Integer>> entireForwardingTable = new ArrayList<>();

    public static void main(String[] args) {

        //setup forwarding table
        setupForwardingTable();

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
            DVNode nodeFromCoordinator = (DVNode) ois.readObject();
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

            //get this node's forwarding table
            HashMap<Integer, Integer> myForwardingTable = entireForwardingTable.get(nodeFromCoordinator.nodeNum);


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
}
