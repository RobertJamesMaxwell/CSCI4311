/*
 * Robert Maxwell
 * CSCI 4311 - Spring 2018
 * Assignment One
*/

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.lang.Math;

public class DataReceiver {

    private static byte[] buf = new byte[64];
    private static int numberOfReadBytes = 0;
    private static int numberOfPacketsReceived = 0;
    private static int packetRateNumber = 1;

    private static long timeOfPacketReceived = 0;
    private static long currentPacketRateStartTime = 0;
    private static long timeOfPreviousPacket = 0;

    private static long timeBetweenPackets = 0;
    private static long timeBetweenPreviousPackets = 0;

    private static boolean newPacketRate = false;
    private static long currentPacketRateEndTime = 0;


    public static void main(String[] args) throws IOException {


      //error message to user about correct usage of DataSender
      if (args.length != 3) {
        System.out.println("Usage: java DataReceiver <ipAddress> <port> <interval>");
        return;
      }


      //get IP address, port, and interval to accept packets from command line
      InetAddress address = InetAddress.getByName(args[0]);
      int port = Integer.parseInt(args[1]);


      //establish connection to server
      Socket clientSocket = new Socket(address, port);
      BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      String successfulConnection = inFromServer.readLine();
      System.out.println("=====================================================");
      System.out.println("\nData Receiver has successfully connected to a Data Sender Server and received the following message:");
      System.out.println("'"+ successfulConnection + "'\n");
      System.out.println("=====================================================\n\n");


      //accept data from server
      DataInputStream byteInFromServer = new DataInputStream(clientSocket.getInputStream());


      //read first two packets to establish the rate
      //first packet
      System.out.println("Receiving packets... at packet rate number: " + packetRateNumber );
      numberOfReadBytes = byteInFromServer.read(buf, 0, buf.length);
      timeOfPacketReceived = System.currentTimeMillis();
      currentPacketRateStartTime = System.currentTimeMillis();
      timeOfPreviousPacket = timeOfPacketReceived;

      //second packet
      numberOfReadBytes = byteInFromServer.read(buf, 0, buf.length);
      timeOfPacketReceived = System.currentTimeMillis();
      timeBetweenPackets = timeOfPacketReceived - timeOfPreviousPacket;
      timeBetweenPreviousPackets = timeBetweenPackets;
      timeOfPreviousPacket = timeOfPacketReceived;

      numberOfPacketsReceived = 2;


      //receive packets
      while(numberOfReadBytes != 4) {

        //read a packet of bytes
        numberOfReadBytes = byteInFromServer.read(buf, 0, buf.length);

        //calculate packet rates
        timeOfPacketReceived = System.currentTimeMillis();
        timeBetweenPackets = timeOfPacketReceived - timeOfPreviousPacket;
        timeOfPreviousPacket = timeOfPacketReceived;

        //allow for error of 5ms in calculation
        if ( (Math.abs(timeBetweenPackets - timeBetweenPreviousPackets) > 5) ) {
          newPacketRate = true;
        }
        timeBetweenPreviousPackets = timeBetweenPackets;

        //if the rate of packets changes, print packet rate, and print out the data to make sure it's correct
        if(newPacketRate)   {
          printRateChangeInfo();
          packetRateNumber++;
          System.out.println("Receiving packets... at packet rate number: " + packetRateNumber );
        }
        numberOfPacketsReceived++;
      } // end while

      printRateChangeInfo();
      System.out.println("Done receiving.");
      System.out.println("Goodbye from Data Receiver.");
      clientSocket.close();
    }

    private static void printRateChangeInfo()  {
        System.out.println("=====================================================");
        currentPacketRateEndTime = System.currentTimeMillis();
        double secondsThatPacketRateLasted = (double)(currentPacketRateEndTime - currentPacketRateStartTime) / 1000;
        System.out.println("PACKET RATE HAS CHANGED");
        System.out.println("Previous packet rate was: " + numberOfPacketsReceived + " packets received in " + secondsThatPacketRateLasted + " seconds." );
        System.out.println("Packet Rate " + packetRateNumber + " = " + ((double)numberOfPacketsReceived / secondsThatPacketRateLasted ) + " packets per second.");
        currentPacketRateStartTime = System.currentTimeMillis();
        numberOfPacketsReceived = 0;
        newPacketRate = false;

        //convert bytes read back into ints to make sure we got the correct data
        List<Integer> data = new ArrayList();
        for (int i=0; i < 64 ; i+=4) {
          ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
          byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
          int x = byteBuffer.getInt(i);
          data.add(x);
        }
        System.out.println("Data received is: " + data.toString());
        System.out.println("=====================================================\n\n");
    }
}
