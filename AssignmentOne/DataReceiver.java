/*
 * Robert Maxwell
 * CSCI 4311 - Spirng 2018
 * Assignment One
*/

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
		while(socket.isConnected())	{
			socket.receive(packet);
			System.out.println("Received packet " + packetCount);
			packetCount++;
		}

    socket.close();
    }
}
