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
                System.out.println("Inside RUN: DVs for this node are is: ");
                for(int j: receivedDV.dv) {
                    System.out.print(j + " ");
                }
                System.out.println();
                this.changeListener.changed();
                ControlPlane.needToChange = true;
                boolean dvAlgorithmUpdate = false;

                //update your forwarding table and DV
                for (int i = 0; i < ControlPlane.realForwardingTable.size(); i++)    {
                    if (ControlPlane.nodeFromCoordinator.dv[receivedDV.node_num] + receivedDV.dv[i] < ControlPlane.nodeFromCoordinator.dv[i]
                            && ControlPlane.nodeFromCoordinator.dv[i] != 0
                            && receivedDV.dv[i] != 0
                            //&& nodeFromCoordinator.dv[i] != Integer.MAX_VALUE
                            && receivedDV.dv[i] != Integer.MAX_VALUE){

                        ControlPlane.realForwardingTable.put(i, receivedDV.node_num);

                        System.out.println("\n\nMy DV is: ");
                        for(int j: ControlPlane.nodeFromCoordinator.dv) {
                            System.out.print(j + " ");
                        }

                        System.out.println("\nDV I just received from node '" + receivedDV.node_num + "' is:");
                        for(int j: receivedDV.dv) {
                            System.out.print(j + " ");
                        }

                        //update nodeFromCoordinator.dv
                        ControlPlane.nodeFromCoordinator.dv[i] = receivedDV.dv[i] + ControlPlane.nodeFromCoordinator.dv[receivedDV.node_num];
                        System.out.println("\nMy NEW DV is: ");
                        for(int j: ControlPlane.nodeFromCoordinator.dv) {
                            System.out.print(j + " ");
                        }
                        System.out.println();
                        dvAlgorithmUpdate = true;

                    }
                }
                System.out.println("\nMy NEW Real Forwarding Table is: " + ControlPlane.realForwardingTable);
                if (dvAlgorithmUpdate)   {
                    System.out.println("Sending update to neighbors.");
                    DV dvToSendOverNetwork = new DV(ControlPlane.nodeFromCoordinator.nodeNum);
                    dvToSendOverNetwork.setDv(ControlPlane.nodeFromCoordinator.dv);
                    ControlPlane.dvSender.send(dvToSendOverNetwork);
                }


//                System.out.println("\nDVReceiver Thread is sleeping for 12 ms.");
//                Thread.sleep(12);
            }
        } catch (IOException e) {
            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
        }

    }
}
