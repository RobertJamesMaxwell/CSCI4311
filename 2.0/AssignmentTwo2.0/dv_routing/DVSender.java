/*
 * CSCI 4311
 * Assignment 2 - Exchanging Distance Vector with Neighbors
 * Robert Maxwell
 * Spring 2018
 */

package dv_routing;

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

    public DVSender(int multicastPortNumber, String multicastIPNumber)   {
        try {
            this.senderSocket = new MulticastSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.multicastPortNumber = multicastPortNumber;
        this.multicastIPNumber = multicastIPNumber;
    }

    public MulticastSocket getSenderSocket() {
        return senderSocket;
    }

    public void setSenderSocket(MulticastSocket senderSocket) {
        this.senderSocket = senderSocket;
    }

    public int getMulticastPortNumber() {
        return multicastPortNumber;
    }

    public void setMulticastPortNumber(int multicastPortNumber) {
        this.multicastPortNumber = multicastPortNumber;
    }

    public String getMulticastIPNumber() {
        return multicastIPNumber;
    }

    public void setMulticastIPNumber(String multicastIPNumber) {
        this.multicastIPNumber = multicastIPNumber;
    }

    public void send(String messageToSend){

        byte[] buffer = messageToSend.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(this.multicastIPNumber), this.multicastPortNumber);
            this.senderSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
