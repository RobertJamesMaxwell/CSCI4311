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

public class DVNode implements Serializable {

    int nodeNum;
    int[] dv;

    public static void main(String[] args)  {

        try {
            // send connection request to coordinator
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = new byte[256];
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 12110);
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

            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
