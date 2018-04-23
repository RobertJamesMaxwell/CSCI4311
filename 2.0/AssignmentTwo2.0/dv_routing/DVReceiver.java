/*
 * CSCI 4311
 * Assignment 2 - Exchanging Distance Vector with Neighbors
 * Robert Maxwell
 * Spring 2018
 */

package dv_routing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class DVReceiver {

    List<MulticastSocket> socketsForListening = new ArrayList<>();

    public DVReceiver() {
    }

    public List<MulticastSocket> getSocketsForListening() {
        return socketsForListening;
    }

    public void setSocketsForListening(List<MulticastSocket> socketsForListening) {
        this.socketsForListening = socketsForListening;
    }

    public void receive(byte[] buffer) throws IOException {
        for(MulticastSocket currentListeningSocket: this.getSocketsForListening())    {
            DatagramPacket listeningPacket = new DatagramPacket(buffer, buffer.length);
            currentListeningSocket.receive(listeningPacket);
            String stringReceivedFromNeighbor = new String(listeningPacket.getData(), 0, listeningPacket.getLength());
            System.out.println("Received data from a neighbor. Data is: '" + stringReceivedFromNeighbor + "'");
        }
    }
}
