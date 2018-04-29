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

import static dataplane.forwarding.ControlPlane.nodeFromCoordinator;

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
            //System.out.println("Received a DV Object with node number: " + dvReceivedOverNetwork.node_num);
        return dvReceivedOverNetwork;
        //}
    }

    public void run() {
        byte[] listenBuf = new byte[256];
        try {
            while(true) {
                receivedDV = this.receive(listenBuf);
//                    System.out.println("Inside RUN: DVs for this node are is: ");
//                    for(int j: receivedDV.dv) {
//                        System.out.print(j + " ");
//                    }
//                    System.out.println();

                this.changeListener.changed();
                ControlPlane.needToChange = true;
                updateStuff();
//                System.out.println("\nDVReceiver Thread is sleeping for 12 ms.");
//                Thread.sleep(12);
            }
        } catch (IOException e) {
            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
        }

    }

    public void updateStuff() {
        ControlPlane.numberOfReceives++;
        if (ControlPlane.needToChange) {
            ControlPlane.needToChange = false;
            //System.out.println("CHAAANGE NUMBER ");
            //update your forwarding table
            DV newDV = DVReceiver.receivedDV;

            boolean printTable = false;

            //update your forwarding table
            for (int i = 0; i < ControlPlane.realForwardingTable.size(); i++) {
                if (ControlPlane.nodeFromCoordinator.dv[newDV.node_num] + newDV.dv[i] < nodeFromCoordinator.dv[i]
                        && nodeFromCoordinator.dv[i] != 0
                        && newDV.dv[i] != 0
                        //&& nodeFromCoordinator.dv[i] != Integer.MAX_VALUE
                        && newDV.dv[i] != Integer.MAX_VALUE) {

                    ControlPlane.realForwardingTable.put(i, newDV.node_num);

//                    System.out.println("\n\nMy DV is: ");
//                    for (int j : nodeFromCoordinator.dv) {
//                        System.out.print(j + " ");
//                    }
//
//                    System.out.println("\nDV I just received from node '" + newDV.node_num + "' is:");
//                    for (int j : newDV.dv) {
//                        System.out.print(j + " ");
//                    }

                    //update nodeFromCoordinator.dv
                    nodeFromCoordinator.dv[i] = newDV.dv[i] + nodeFromCoordinator.dv[newDV.node_num];
                    System.out.println("\nMy updated DVs from DV RECEIVER are: ");
                    for (int j : nodeFromCoordinator.dv) {
                        System.out.print(j + " ");
                    }
                    System.out.println();
//                    System.out.println("Need to send to neighbors");
                    ControlPlane.needToSendToNeighbors = true;
                    System.out.println("\nMy NEW Real Forwarding Table is: " + ControlPlane.realForwardingTable);
                    printTable = true;
                    DV dvToSendOverNetwork = new DV(nodeFromCoordinator.nodeNum);
                    dvToSendOverNetwork.setDv(nodeFromCoordinator.dv);
                    ControlPlane.dvSender.send(dvToSendOverNetwork);
                }

            }

            if (printTable) {
//                System.out.println("\nMy NEW Real Forwarding Table is: " + ControlPlane.realForwardingTable);
            }

        }
    }
}
