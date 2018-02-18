/*
 * Robert Maxwell
 * CSCI 4311 - Spirng 2018
 * Assignment One
*/


// To Do:
// 1) Data to send should be array of 16 java ints?  Need to convert random int array to byte array?


import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataReceiver {

    public static void main(String[] args) throws IOException {

		if (args.length != 3) {
      System.out.println("Usage: java DataReceiver <ipAddress> <port> <interval>");
      return;
    }


    String sentence;
    String modifiedSentence;

    InetAddress address = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);
		int interval  = Integer.parseInt(args[2]);

    BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
    Socket clientSocket = new Socket(address, port);
    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    sentence = inFromUser.readLine();
    outToServer.writeBytes(sentence + '\n');
    modifiedSentence = inFromServer.readLine();
    System.out.println("FROM SERVER: " + modifiedSentence);

    DataInputStream byteInFromServer = new DataInputStream(clientSocket.getInputStream());
    byte[] buf = new byte[64];

    while(true) {
      int read = byteInFromServer.read(buf, 0, buf.length);
      System.out.println("FROM SERVER: " + read);
      System.out.println("Buffer:" + Arrays.toString(buf));

      //ByteBuffer bb = ByteBuffer.allocate(64).put(buf);
      //System.out.println("ByteBuffer:" + bb.toString());
      for (int i=0; i < 64 ; i+=4) {
        //int y = buf[i] << 24 | (buf[i+1] & 0xff) << 16 | (buf[i+2] & 0xff) << 8 | (buf[i+3] & 0xff);
        //System.out.println("y = " + y);

        ByteBuffer bb = ByteBuffer.wrap(buf);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        //System.outbb.getInt();


        int x = bb.getInt(i);
        System.out.println("x = " + x);
      }
    }

    //clientSocket.close();

		//Send a packet to connect to the server
		// DatagramSocket socket = new DatagramSocket();
		// byte[] buf = new byte[256];

		// DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
		// socket.send(packet);
    // socket.connect(address, port);

    // get response
		// packet = new DatagramPacket(buf, buf.length);
		// int packetCount = 0;
    // long startTime = System.currentTimeMillis();
    // long currentTime = 0;
    // System.out.println("Receiving packets...\n");
		// while(socket.isConnected())	{
		// 	socket.receive(packet);
    //
    //   //check if this is the last packet and disconnect from server if so
    //   if (packet.getLength() == 8)  {
    //     // if (packetCount > 0)  {
    //     //   System.out.println("Received " + packetCount + " packets in " + (currentTime - startTime) / 1000 + " seconds.");
    //     //   System.out.println("Received rate is: " + (packetCount / ((currentTime - startTime) / 1000)) + " packets per second for " + (currentTime - startTime) / 1000 + " seconds.\n");
    //     // }
    //     socket.disconnect();
    //   }
    //   packetCount++;
    //   currentTime = System.currentTimeMillis();
    //   if (currentTime - startTime > (interval*1000) ) {
    //     System.out.println("Received " + packetCount + " packets in " + interval + " seconds.");
    //     System.out.println("Received rate is: " + (packetCount / interval) + " packets per second for " + interval + " seconds.\n");
    //     packetCount = 0;
    //     startTime = System.currentTimeMillis();
    //   }
    //
		// }

    //socket.close();
    }
}
