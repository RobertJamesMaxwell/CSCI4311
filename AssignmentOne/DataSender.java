/*
 * Robert Maxwell
 * CSCI 4311 - Spring 2018
 * Assignment One
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class DataSender {

    public static void main(String[] args) throws Exception {

      //error message to user about correct usage of DataSender
  		if (args.length < 3) {
        System.out.println("Usage: java DataSender <port> <inverval> <packetRate1> ... <packetRateN>");
        return;
      }

      //get port, interval to send packets, and variable number of packet rates from command line
  		int port = Integer.parseInt(args[0]);
  		int interval = Integer.parseInt(args[1]);
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

      //establish output stream to client
      DataOutputStream outToClient = new DataOutputStream(client.getOutputStream());
      String welcomeMessageToClient = "Hello, thanks for connecting.\n"
          + "Preparing data to send...";
      outToClient.writeBytes(welcomeMessageToClient + '\n');
      Thread.sleep(2000);

      //prep data to send
      int[] data = {43, 11, 14, 31, 13, 44, 55, 66, 77, 88, 99, 76, 54, 32, 1, 2};
      byte[] buf = new byte[64];
      //System.out.println("Buffer:" + Arrays.toString(buf));
      for (int i = 0; i < data.length; i++) {
        Integer integer = data[i];
        buf[i*4] = integer.byteValue();
      }
      System.out.println("\nBuffer to send is:\n" + Arrays.toString(buf) + "\n");

      //send data for each packet rate
      int numberOfPacketRates = packetRates.size();
      int currentPacketRateIndex = 0;
      while (currentPacketRateIndex < numberOfPacketRates)  {
        int currentPacketRate = packetRates.get(currentPacketRateIndex);
        System.out.println("Writing data at " + currentPacketRate + " packets per second for " + interval + " seconds...\n");
        for (int i = 0; i < interval; i++) 	{
          for (int j = 0; j < currentPacketRate; j++) {
            outToClient.write(buf, 0, buf.length);
            Thread.sleep(1000 / currentPacketRate);
          }
        }
        currentPacketRateIndex++;
      } //end while

      //send a signal to client that the process is over
      //signal is two packets in quick succession to change the rate and then sleep for 1 second
      //one packet of a different size to denote the 'last packet'
      //outToClient.write(buf, 0, buf.length);
      //outToClient.write(buf, 0, buf.length);
      //Thread.sleep(1000);
      buf = new byte[8];
      outToClient.write(buf, 0, buf.length);
      client.close();
      serverSocket.close();
      System.out.println("Goodbye.");
    }

}
