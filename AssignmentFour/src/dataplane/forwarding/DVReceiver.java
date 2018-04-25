/*
 * CSCI 4311
 * Assignment 2 - Exchanging Distance Vector with Neighbors
 * Robert Maxwell
 * Spring 2018
 */

package dataplane.forwarding;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class DVReceiver extends Thread {

    //List<MulticastSocket> socketsForListening = new ArrayList<>();
    MulticastSocket receiverSocket;
    ChangeListener changeListener;
    public static DV receivedDV;

    public DVReceiver(MulticastSocket receiverSocket, ChangeListener changeListener) {
        this.receiverSocket = receiverSocket;
        this.changeListener = changeListener;
    }



    public DV receive(byte[] buffer) throws IOException {
        //for(MulticastSocket currentListeningSocket: this.getSocketsForListening())    {
        DatagramPacket listeningPacket = new DatagramPacket(buffer, buffer.length);
        this.receiverSocket.receive(listeningPacket);
        DV dvReceivedOverNetwork = DV.bytes2DV(listeningPacket.getData());
        //String stringReceivedFromNeighbor = new String(listeningPacket.getData(), 0, listeningPacket.getLength());
        System.out.println("Received a DV Object with node number: " + dvReceivedOverNetwork.node_num);
        return dvReceivedOverNetwork;
        //}
    }

    public void run() {
        byte[] listenBuf = new byte[256];
        try {
            while(true) {
             receivedDV = this.receive(listenBuf);
             System.out.println("Inside RUN: DV is: " + receivedDV.node_num);
             this.changeListener.changed();
             ControlPlane.needToChange = true;
             System.out.println("\nDVReceiver Thread is sleeping for 20 seconds.");
             Thread.sleep(20000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
