/*
 * Robert Maxwell
 * CSCI 4311 - Spring 2018
 * Assignment One
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class DataSender extends Thread {

    private DatagramSocket socket;
    private int interval;
    private List<Integer> packetsPerSecond;

    public DataSender(int port, int timeToSend, List<Integer> packetRate) throws IOException {
  		socket = new DatagramSocket(port);
  		interval = timeToSend;
  		packetsPerSecond = packetRate;
    }

    public static void main(String[] args) throws IOException {
  		if (args.length < 3) {
        System.out.println("Usage: java DataSender <port> <inverval> <packetRate1> ... <packetRateN>");
        return;
      }

  		int port = Integer.parseInt(args[0]);
  		int timeToSend = Integer.parseInt(args[1]);
  		List<Integer> packetRates = new ArrayList<>();
  		for (int i = 2; i < args.length; i++)	{
  			packetRates.add(Integer.parseInt(args[i]));
  		}
  		//int packetRate = Integer.parseInt(args[2]);
  		try {
        Thread t = new DataSender(port, timeToSend, packetRates);
        t.start();
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
    }

    public void run() {
        int numberOfPacketRates = packetsPerSecond.size();
        int currentPacketRate = 0;
            try {

              byte[] buf = new byte[256];

              // receive a connection from a client
              DatagramPacket packet = new DatagramPacket(buf, buf.length);
              System.out.println("Waiting for a connection...\n");
              socket.receive(packet);

              InetAddress address = packet.getAddress();
              int port = packet.getPort();
              System.out.println("Recieved connection from:" +
              "\nAddress: " + address +
              "\nPort: "+ port + "\n");

              while (currentPacketRate < numberOfPacketRates) {

                //Send data to client
                System.out.println("Sending Data...");
                System.out.println("Rate is: " + packetsPerSecond.get(currentPacketRate) + " packets per second for " + interval + " seconds.\n");
                packet = new DatagramPacket(buf, buf.length, address, port);
                for (int i = 0; i < interval; i++) 	{
                  for (int j = 0; j < packetsPerSecond.get(currentPacketRate); j++) {
                    socket.send(packet);
                  }
                  //socket.send(packet);
                  try {
                    Thread.sleep(1000);
                  } catch (Exception e) {
                    System.out.println(e);
                  }
                }

                currentPacketRate++;
              }//end while

          } catch (IOException e) {
            e.printStackTrace();
          } finally {
		          System.out.println("Goodbye.");
              socket.close();
          }
      }

}
