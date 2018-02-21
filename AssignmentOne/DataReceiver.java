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

    public static void main(String[] args) throws IOException {


      //error message to user about correct usage of DataSender
  		if (args.length != 3) {
        System.out.println("Usage: java DataReceiver <ipAddress> <port> <interval>");
        return;
      }


      //get IP address, port, and interval to accept packets from command line
      InetAddress address = InetAddress.getByName(args[0]);
  		int port = Integer.parseInt(args[1]);
  		int interval  = Integer.parseInt(args[2]);


      //establish connection to server
      Socket clientSocket = new Socket(address, port);
      BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      String successfulConnection = inFromServer.readLine();
      System.out.println("FROM SERVER: " + successfulConnection);


      //accept data from server
      DataInputStream byteInFromServer = new DataInputStream(clientSocket.getInputStream());
      byte[] buf = new byte[64];

      //read first two packets to establish the rate
      //first packet
      int numberOfReadBytes = byteInFromServer.read(buf, 0, buf.length);
        //System.out.println("First numberOfReadBytes:" + numberOfReadBytes);
      long timeOfPacketReceived = System.currentTimeMillis();
        //System.out.println("First timeOfPacketReceived:" + timeOfPacketReceived);
      long currentPacketRateStartTime = System.currentTimeMillis();
        //System.out.println("First currentPacketRateStartTime:" + currentPacketRateStartTime);
      long timeOfPreviousPacket = timeOfPacketReceived;
        //System.out.println("First timeOfPreviousPacket:" + timeOfPreviousPacket);

      //second packet
      numberOfReadBytes = byteInFromServer.read(buf, 0, buf.length);
      timeOfPacketReceived = System.currentTimeMillis();
        //System.out.println("Second timeOfPacketReceived:" + timeOfPacketReceived);
      long timeBetweenPackets = timeOfPacketReceived - timeOfPreviousPacket;
        //System.out.println("Second timeBetweenPackets:" + timeBetweenPackets);
      long timeBetweenPreviousPackets = timeBetweenPackets;
        //System.out.println("Second timeBetweenPreviousPackets:" + timeBetweenPreviousPackets);
      timeOfPreviousPacket = timeOfPacketReceived;
        //System.out.println("Second timeOfPreviousPacket:" + timeOfPreviousPacket);

      System.out.println("Receiving packets...");
      boolean newPacketRate = false;
      long currentPacketRateEndTime = 0;
      int numberOfPacketsReceived = 2;

      while(true) {

        //read a packet of bytes
        numberOfReadBytes = byteInFromServer.read(buf, 0, buf.length);

        //calculate packet rates
        timeOfPacketReceived = System.currentTimeMillis();
          //System.out.println("\n\n timeOfPacketReceived:" + timeOfPacketReceived);
        timeBetweenPackets = timeOfPacketReceived - timeOfPreviousPacket;
          //System.out.println(" timeBetweenPackets:" + timeBetweenPackets);
          //System.out.println("Time between packets: " + timeBetweenPackets);
        timeOfPreviousPacket = timeOfPacketReceived;
          //System.out.println(" timeOfPreviousPacket:" + timeOfPreviousPacket);

        //allow for error of 10ms in calculation
        if ( (Math.abs(timeBetweenPackets - timeBetweenPreviousPackets) > 10) ) {
          newPacketRate = true;
        }
          //System.out.println(" timeBetweenPreviousPackets:" + timeBetweenPreviousPackets);
        timeBetweenPreviousPackets = timeBetweenPackets;


        //account for if server has stopped sending packetRates
        if (numberOfReadBytes == 1) {
          newPacketRate = true;
        }


        //if the rate of packets changes, alert user, print previous packet rate, and print out the data to make sure it's still correct
        if(newPacketRate)   {
          System.out.println("\n\n-----------------------------------------");
          currentPacketRateEndTime = System.currentTimeMillis();
          System.out.println("PACKET RATE HAS CHANGED");
          System.out.println("Previous packet rate was: \n" +
            numberOfPacketsReceived + " packets received in " +
                  (double)(currentPacketRateEndTime - currentPacketRateStartTime) / 1000 + " seconds." );
          System.out.println("Packet Rate = " + ((double)numberOfPacketsReceived / ((double)(currentPacketRateEndTime - currentPacketRateStartTime) / 1000) ) + " packets per second.");
          currentPacketRateStartTime = currentPacketRateEndTime;
          numberOfPacketsReceived = 0;
          newPacketRate = false;

          //System.out.println("\n\n-----------------------------------------");
          System.out.println("Bytes read from Server: " + numberOfReadBytes);
          System.out.println("Buffer received is:" + Arrays.toString(buf));
          //convert bytes read back into ints to make sure we got the correct data
          List<Integer> data = new ArrayList<Integer>();
          for (int i=0; i < 64 ; i+=4) {
            ByteBuffer bb = ByteBuffer.wrap(buf);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            int x = bb.getInt(i);
            data.add(x);
          }
          System.out.println("Data received is: " + data.toString() + "\n");

          //exit loop if received a packet of 1 bytes (1 byte is signal from server that process is over)
          if (numberOfReadBytes == 1) {
            System.out.println("Done receiving.");
            System.out.println("Goodbye.");
            break;
          }
          System.out.println("Receiving packets...");
        }

        numberOfPacketsReceived++;


    } // end while

    clientSocket.close();
    }
}
