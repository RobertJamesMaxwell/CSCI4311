/*
 * CSCI 4311
 * Assignment 4 - DV Algorithm
 * Robert Maxwell, Chris Schilling
 * Spring 2018
 */

package dataplane.forwarding;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class DVReceiver extends Thread {

    MulticastSocket receiverSocket;
    public static DV receivedDV;


    public DVReceiver(MulticastSocket receiverSocket) {
        this.receiverSocket = receiverSocket;
    }


    public DV receive(byte[] buffer) throws IOException {
        DatagramPacket listeningPacket = new DatagramPacket(buffer, buffer.length);
        this.receiverSocket.receive(listeningPacket);
        DV dvReceivedOverNetwork = DV.bytes2DV(listeningPacket.getData());
        return dvReceivedOverNetwork;
    }


    public void run() {
        byte[] listenBuf = new byte[256];
        try {
            while(true) {
                receivedDV = this.receive(listenBuf);
                System.out.println("\n\nDVReceiver received a DV Object with node number: " + receivedDV.node_num);
                System.out.println("DVReceiver received a DV Object with dvs: ");
                for(int j: receivedDV.dv) {
                    System.out.print(j + " ");
                }
                System.out.println();
                ControlPlane.needToChange = true;
                boolean dvAlgorithmUpdate = false;

                //update Control Plane's DV object and forwarding table
                for (int i = 0; i < ControlPlane.realForwardingTable.size(); i++)    {
                    if (ControlPlane.nodeFromCoordinator.dv[receivedDV.node_num] + receivedDV.dv[i] < ControlPlane.nodeFromCoordinator.dv[i]
                            && ControlPlane.nodeFromCoordinator.dv[i] != 0
                            && receivedDV.dv[i] != 0
                            && receivedDV.dv[i] != Integer.MAX_VALUE){

                        //update forwarding table
                        System.out.println("\nMy OLD Forwarding Table is: " + ControlPlane.realForwardingTable);
                        ControlPlane.realForwardingTable.put(i, receivedDV.node_num);
                        System.out.println("My NEW Forwarding Table is: " + ControlPlane.realForwardingTable);


                        //update nodeFromCoordinator.dv
                        System.out.println("\nMy OLD DV is: ");
                        for(int j: ControlPlane.nodeFromCoordinator.dv) {
                            System.out.print(j + " ");
                        }
                        System.out.println("\nDV I just received from node '" + receivedDV.node_num + "' is:");
                        for(int j: receivedDV.dv) {
                            System.out.print(j + " ");
                        }
                        ControlPlane.nodeFromCoordinator.dv[i] = receivedDV.dv[i] + ControlPlane.nodeFromCoordinator.dv[receivedDV.node_num];
                        System.out.println("\nMy NEW DV is: ");
                        for(int j: ControlPlane.nodeFromCoordinator.dv) {
                            System.out.print(j + " ");
                        }
                        System.out.println();
                        dvAlgorithmUpdate = true;
                    }
                }

                if (dvAlgorithmUpdate)   {
                    System.out.println("Sending update to neighbors...\n\n");
                    DV dvToSendOverNetwork = new DV(ControlPlane.nodeFromCoordinator.nodeNum);
                    dvToSendOverNetwork.setDv(ControlPlane.nodeFromCoordinator.dv);
                    ControlPlane.dvSender.send(dvToSendOverNetwork);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
