/*
 * Robert Maxwell
 * CSCI 4311 - Spring 2018
 * Assignment One
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class DataSender {

    public static void main(String[] args) {


      //error message to user about correct usage of DataSender
  		if (args.length < 3) {
        System.out.println("Usage: java DataSender <port> <inverval> <packetRate1> <packetRate2> ... <packetRateN>");
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
      ServerSocket serverSocket = null;
      try {
        serverSocket = new ServerSocket(port);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      System.out.println("=====================================================");
      System.out.println("Hello, Data Sender has Started.");
      System.out.println("\r\nRunning Server: " +
        "\nHost = " + (serverSocket != null ? serverSocket.getInetAddress().getHostAddress() : null) +
        "\nPort = " + (serverSocket != null ? serverSocket.getLocalPort() : 0));
      System.out.println("=====================================================");

      //get connection from a client
      Socket client = null;
      try {
        client = serverSocket != null ? serverSocket.accept() : null;
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
      System.out.println("=====================================================");
      System.out.println("New connection from: " +
        "\nHost = " + (client != null ? client.getInetAddress().getHostAddress() : null) +
        "\nPort = " + (client != null ? client.getLocalPort() : 0));
      System.out.println("=====================================================");

      //establish output stream to client
      String welcomeMessageToClient = "Hello, thanks for connecting to the Data Sender Server. \t"
              + "Preparing data to send...";
      DataOutputStream outToClient = null;
      try {
        outToClient = new DataOutputStream(client != null ? client.getOutputStream() : null);
        outToClient.writeBytes(welcomeMessageToClient + '\n');
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }


      //prep data to send
      int[] data = {43, 11, 14, 31, 13, 44, 55, 66, 77, 88, 99, 76, 54, 32, 1, 2};
      byte[] buf = new byte[64];
      for (int i = 0; i < data.length; i++) {
        Integer integer = data[i];
        buf[i*4] = integer.byteValue();
      }
      System.out.println("=====================================================");
      System.out.println("Data to send is:\n" + Arrays.toString(data));
      System.out.println("\nBuffer to send is:\n" + Arrays.toString(buf));
      System.out.println("=====================================================");


      //send data for each packet rate
      int numberOfPacketRates = packetRates.size();
      int currentPacketRateIndex = 0;
      while (currentPacketRateIndex < numberOfPacketRates)  {
        int currentPacketRate = packetRates.get(currentPacketRateIndex);
        System.out.println("=====================================================");
        System.out.println("\nWriting data at " + currentPacketRate + " packets per second for " + interval + " seconds...\n");
        System.out.println("=====================================================");
        for (int i = 0; i < interval; i++) 	{
          for (int j = 0; j < currentPacketRate; j++) {
            try {
              if (outToClient != null) {
                outToClient.write(buf, 0, buf.length);
              }
              Thread.sleep(1000 / currentPacketRate);
            } catch (IOException | InterruptedException ioe) {
              ioe.printStackTrace();
            }

          }
        }
        currentPacketRateIndex++;
      } //end while


      //send a signal to client that the process is over
      //one packet of a different size (size 1) to denote the 'last packet'
      buf = new byte[4];
      try {
        if (outToClient != null) {
          outToClient.write(buf, 0, buf.length);
        }
        if (client != null) {
          client.close();
        }
        if (serverSocket != null) {
          serverSocket.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      System.out.println("=====================================================");
      System.out.println("\nData Sender Server is finished and shutting down. \nGoodbye.");
    }

}
