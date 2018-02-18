/*
 * Robert Maxwell
 * CSCI 4311 - Spring 2018
 * Assignment One
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class DataSender extends Thread {

    //private static ServerSocket serverSocket;
    // public DataSender(int port, int timeToSend, List<Integer> packetRate) throws IOException {
  	// 	serverSocket = new ServerSocket(port);
  	// 	interval = timeToSend;
  	// 	packetsPerSecond = packetRate;
    // }

    public static void main(String[] args) throws Exception {

      //error message to user about correct usage of DataSender
  		if (args.length < 3) {
        System.out.println("Usage: java DataSender <port> <inverval> <packetRate1> ... <packetRateN>");
        return;
      }

      //get port, intever to send packets, and variable number of packet rates from command line
  		int port = Integer.parseInt(args[0]);
  		int timeToSend = Integer.parseInt(args[1]);
  		List<Integer> packetRates = new ArrayList<>();
  		for (int i = 2; i < args.length; i++)	{
  			packetRates.add(Integer.parseInt(args[i]));
  		}

      //establish server running on port given as args[0]
      ServerSocket serverSocket = new ServerSocket(port);
      System.out.println("\r\nRunning Server: " +
        "\n Host=" + serverSocket.getInetAddress().getHostAddress() +
        "\n Port=" + serverSocket.getLocalPort());

      //get connection from a client
      Socket client = serverSocket.accept();
      System.out.println("\r\nNew connection from: " +
        "\n Host=" + client.getInetAddress().getHostAddress() +
        "\n Port=" + client.getLocalPort());


      BufferedReader inFromClient =
          new BufferedReader(new InputStreamReader(client.getInputStream()));
      DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());
      String clientSentence = inFromClient.readLine();
      System.out.println("\nReceived: " + clientSentence);
      String capitalizedSentence = clientSentence.toUpperCase() + '\n';
      outToClient.writeBytes(capitalizedSentence + '\n');

      //prep data to send
      int[] data = {43, 11, 14, 31, 13, 44, 55, 66, 77, 88, 99, 76, 54, 32, 1, 2};
      byte[] buf = new byte[64];
      System.out.println("Buffer:" + Arrays.toString(buf));
      for (int i = 0; i < data.length; i++) {
        //System.out.println("Data at i:" + data[i]);
        Integer integer = data[i];
        //System.out.println("Buff at i*4:" + buf[i*4]);
        buf[i*4] = integer.byteValue();
        //System.out.println("Buff at i*4:" + buf[i*4]);
      }

      System.out.println("Buffer:" + Arrays.toString(buf));

      while (true)  {
        System.out.println("Writing data");
        outToClient.write(buf, 0, buf.length);
        Thread.sleep(1000);
      }


  		// try {
      //   Thread t = new DataSender(port, timeToSend, packetRates);
      //   t.start();
  		// } catch (IOException e) {
  		// 	e.printStackTrace();
  		// }
    }



}

        // BufferedReader in = new BufferedReader(
        //         new InputStreamReader(client.getInputStream()));
        // while ( (data = in.readLine()) != null ) {
        //     System.out.println("\r\nMessage from " + clientAddress + ": " + data);
        // }


      //   int numberOfPacketRates = packetsPerSecond.size();
      //   int currentPacketRate = 0;
      //       try {
      //
      //         byte[] buf = new byte[256];
      //
      //         // List<Integer> randomData = new ArrayList<Integer>();
      //         // for (int i = 0; i < 16; i++ ) {
      //         //   randomData.add(i);
      //         // }
      //
      //
      //
      //         buf = new
      //
      //         // receive a connection from a client
      //         DatagramPacket packet = new DatagramPacket(buf, buf.length);
      //         System.out.println("Waiting for a connection...\n");
      //         socket.receive(packet);
      //
      //         InetAddress address = packet.getAddress();
      //         int port = packet.getPort();
      //         System.out.println("Recieved connection from:" +
      //         "\nAddress: " + address +
      //         "\nPort: "+ port + "\n");
      //
      //         while (currentPacketRate < numberOfPacketRates) {
      //
      //           //Send data to client
      //           System.out.println("Sending Data...");
      //           System.out.println("Rate is: " + packetsPerSecond.get(currentPacketRate) + " packets per second for " + interval + " seconds.\n");
      //           packet = new DatagramPacket(buf, buf.length, address, port);
      //           for (int i = 0; i < interval; i++) 	{
      //             for (int j = 0; j < packetsPerSecond.get(currentPacketRate); j++) {
      //               socket.send(packet);
      //             }
      //             //socket.send(packet);
      //             try {
      //               Thread.sleep(1000);
      //             } catch (Exception e) {
      //               System.out.println(e);
      //             }
      //           }
      //
      //           currentPacketRate++;
      //         }//end while
      //
      //         //Send a signal to client that the process is over
      //         buf = new byte[8];
      //         packet = new DatagramPacket(buf, buf.length, address, port);
      //         socket.send(packet);
      //
      //     } catch (IOException e) {
      //       e.printStackTrace();
      //     } finally {
		  //         System.out.println("Goodbye.");
      //         socket.close();
      //     }
      // }
