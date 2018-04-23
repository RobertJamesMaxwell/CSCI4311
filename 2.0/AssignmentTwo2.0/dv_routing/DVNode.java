/*
 * CSCI 4311
 * Assignment 2 - Exchanging Distance Vector with Neighbors
 * Robert Maxwell
 * Spring 2018
 */

package dv_routing;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class DVNode implements Serializable {

    int nodeNum;
    int[] dv;

    private static int coordinatorPortNumber;
    private static int dvNodePortNumber;

    public static void main(String[] args)  {

        //error message to user about correct usage of DVCoordinator
        if (args.length != 2) {
            System.out.println("Usage: java DVNode <ip address> <port number> ");
            return;
        }

        try {
            // send connection request to coordinator
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = new byte[256];
            InetAddress address = InetAddress.getByName(args[0]);
            coordinatorPortNumber = Integer.parseInt(args[1]);
            dvNodePortNumber = coordinatorPortNumber + 10;
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, coordinatorPortNumber);
            socket.send(packet);

            // get welcome message response
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("\nReceived the following message from the server: ");
            System.out.println("'" + received + "'");

            //receive DVNode info from DVCoordinator
            socket.receive(packet);
            buf = packet.getData();
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(bais);
            DVNode nodeFromCoordinator = (DVNode)ois.readObject();
            System.out.println("\n\nReceived my DV Node info from the server!");
            System.out.println("My node number: " + nodeFromCoordinator.nodeNum);
            System.out.print("My dv's: ");
            for(int i: nodeFromCoordinator.dv) {
                System.out.print(i + " ");
            }

            //receive neighbor nodes IP addresses from DVCoordinator
            socket.receive(packet);
            buf = packet.getData();
            bais = new ByteArrayInputStream(buf);
            ois = new ObjectInputStream(bais);
            List<String> neighborNodesIPList =  (List<String>) ois.readObject();
            System.out.println("\n\nReceived the following IP neighbor mapping from server!");
            System.out.println(neighborNodesIPList);

            // setup DV Sender
            DVSender dvSender = new DVSender();
            int multicastSendPort = dvNodePortNumber + nodeFromCoordinator.nodeNum;
            System.out.println("\n\nMultiCast Sending Port for node " + nodeFromCoordinator.nodeNum + " is " + multicastSendPort);
            dvSender.setMulticastPortNumber(multicastSendPort);
            String multicastSendIP = "230.0.0." + nodeFromCoordinator.nodeNum;
            System.out.println("MultiCast IP for node " + nodeFromCoordinator.nodeNum + " is " + multicastSendIP);
            dvSender.setMulticastIPNumber(multicastSendIP);


            // setup DV Receiver
            List<MulticastSocket> socketsForListening = new ArrayList<>();
            for (int i = 0; i < neighborNodesIPList.size(); i++){
                // if IP is empty at this index, then we don't need to listen to that neighbor
                if (!neighborNodesIPList.get(i).equals(""))  {
                    MulticastSocket listeningSocket = new MulticastSocket(dvNodePortNumber + i);
                    listeningSocket.joinGroup(InetAddress.getByName("230.0.0." + Integer.toString(i)));
                    socketsForListening.add(listeningSocket);
                }
            }
            byte[] listenBuf = new byte[256];
            DVReceiver dvReceiver = new DVReceiver();
            dvReceiver.setSocketsForListening(socketsForListening);

            // Send and Receive data
            while(true) {
                // send broadcast out to everyone listening
                System.out.println("\n\nBroadcasting data to neighbors...");
                String multiCastHelloMessage = "Hello from node " + nodeFromCoordinator.nodeNum;
                dvSender.send(multiCastHelloMessage);
                Thread.sleep(2000);

                //receive broadcasts from neighbors
                dvReceiver.receive(listenBuf);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
