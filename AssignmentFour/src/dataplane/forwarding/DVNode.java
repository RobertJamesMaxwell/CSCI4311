/*
 * CSCI 4311
 * Assignment 4 - DV Algorithm
 * Robert Maxwell, Chris Schilling
 * Spring 2018
 */

package dataplane.forwarding;

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

    public static void main(String[] args)  {

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
            DVNode nodeFromCoordinator = (DVNode)ois.readObject();
            System.out.println("Received DVNode from server!");
            System.out.println("\n\nNode number: " + nodeFromCoordinator.nodeNum);
            System.out.println("With dv's: ");
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

            // setup sending multicast to neighbors
            int multicastSendPort = 12110 + nodeFromCoordinator.nodeNum;
            System.out.println("\n\nMultiCast Sending Port for node " + nodeFromCoordinator.nodeNum + " is " + multicastSendPort);
            String multicastSendIP = "230.0.0." + nodeFromCoordinator.nodeNum;
            System.out.println("\n\nMultiCast IP for node " + nodeFromCoordinator.nodeNum + " is " + multicastSendIP);
            MulticastSocket sendSocket = new MulticastSocket();
            byte[] sendBuf = new byte[256];
            String hello = "Hello from node" + nodeFromCoordinator.nodeNum;
            sendBuf = hello.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length, InetAddress.getByName(multicastSendIP), multicastSendPort);


            // setup receiving multicast from neighbors
            List<Integer> portsToListenOn = new ArrayList<>();
            List<String> ipsToListenOn = new ArrayList<>();
            List<MulticastSocket> socketsForListening = new ArrayList<>();
            for (int i = 0; i < neighborNodesIPList.size(); i++){
                // if IP is empty at this index, then we don't need to listen to that neighbor
                if (neighborNodesIPList.get(i).equals(""))  {

                } else {
                    portsToListenOn.add(12110 + i);
                    ipsToListenOn.add("230.0.0." + Integer.toString(i));
                    MulticastSocket listeningSocket = new MulticastSocket(12110 + i);
                    listeningSocket.joinGroup(InetAddress.getByName("230.0.0." + Integer.toString(i)));
                    socketsForListening.add(listeningSocket);
                }
            }
            byte[] listenBuf = new byte[256];

            while(true) {
                // send broadcast out to everyone listening
                System.out.println("Sending Data...");
                sendSocket.send(sendPacket);
                Thread.sleep(2000);

                //receive broadcasts from neighbors

                for(MulticastSocket currentListeningSocket: socketsForListening)    {
                    DatagramPacket listeningPacket = new DatagramPacket(listenBuf, listenBuf.length);
                    currentListeningSocket.receive(listeningPacket);
                    String ReceivedFromNeighbor = new String(listeningPacket.getData(), 0, listeningPacket.getLength());
                    System.out.println("From Neighborclear: " + ReceivedFromNeighbor);
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
