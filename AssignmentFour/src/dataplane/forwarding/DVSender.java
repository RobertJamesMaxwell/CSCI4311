/*
 * CSCI 4311
 * Assignment 4 - DV Algorithm
 * Robert Maxwell, Chris Schilling
 * Spring 2018
 */

package dataplane.forwarding;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class DVSender {

    private MulticastSocket senderSocket;
    private int multicastPortNumber;
    private String multicastIPNumber;

    public DVSender() {
        try {
            this.senderSocket = new MulticastSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMulticastPortNumber(int multicastPortNumber) {
        this.multicastPortNumber = multicastPortNumber;
    }

    public void setMulticastIPNumber(String multicastIPNumber) {
        this.multicastIPNumber = multicastIPNumber;
    }

    public void send(DV dvToSendOverNetwork){
        byte[] buffer = dvToSendOverNetwork.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(this.multicastIPNumber), this.multicastPortNumber);
            this.senderSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
