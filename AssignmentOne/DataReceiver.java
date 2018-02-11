/*
 * Robert Maxwell
 * CSCI 4311 - Spirng 2018
 * Assignment One
*/


// To Do:
// 1) Data to send should be array of 16 java ints?  Need to convert random int array to byte array?
// 2) Possibly use Executor Service instead of Thread.sleep in the Data Sender?
// 3) Need to account for extra packets when intervals don't match
// 4) Need to adjust for port 121 - need admin rights SUDO

import java.io.*;
import java.net.*;
import java.util.*;

public class DataReceiver {

    public static void main(String[] args) throws IOException {

		if (args.length != 3) {
      System.out.println("Usage: java DataReceiver <ipAddress> <port> <interval>");
      return;
    }

		//Send a packet to connect to the server
		DatagramSocket socket = new DatagramSocket();
		byte[] buf = new byte[256];
		InetAddress address = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);
		int interval  = Integer.parseInt(args[2]);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
		socket.send(packet);
    socket.connect(address, port);

    // get response
		packet = new DatagramPacket(buf, buf.length);
		int packetCount = 0;
    long startTime = System.currentTimeMillis();
    long currentTime = 0;
    System.out.println("Receiving packets...\n");
		while(socket.isConnected())	{
			socket.receive(packet);

      //check if this is the last packet and disconnect from server if so
      if (packet.getLength() == 8)  {
        // if (packetCount > 0)  {
        //   System.out.println("Received " + packetCount + " packets in " + (currentTime - startTime) / 1000 + " seconds.");
        //   System.out.println("Received rate is: " + (packetCount / ((currentTime - startTime) / 1000)) + " packets per second for " + (currentTime - startTime) / 1000 + " seconds.\n");
        // }
        socket.disconnect();
      }
      packetCount++;
      currentTime = System.currentTimeMillis();
      if (currentTime - startTime > (interval*1000) ) {
        System.out.println("Received " + packetCount + " packets in " + interval + " seconds.");
        System.out.println("Received rate is: " + (packetCount / interval) + " packets per second for " + interval + " seconds.\n");
        packetCount = 0;
        startTime = System.currentTimeMillis();
      }

		}

    socket.close();
    }
}
