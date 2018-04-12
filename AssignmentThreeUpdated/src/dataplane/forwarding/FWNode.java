package dataplane.forwarding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FWNode {

    private static DataPlanePort dataPlanePort;
    private static List<PortUser> portUserList = new ArrayList<>();
    private DVNode dvNode;

    public static void main(String[] args) {

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

            //listen for incoming connections
            while (true) {
                System.out.println("Listening for a message...");
                MessageType receivedMessage = dataPlanePort.receive();
                int destinationNode = receivedMessage.getDestNode();
                System.out.println("Recieved a new message for destination node: " + destinationNode);

                if (destinationNode == nodeFromCoordinator.nodeNum) {
                    System.out.println("WOOHOO! The packet got to where it needed to get!");
                } else {
                    //forward packet according to forwarding table
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
