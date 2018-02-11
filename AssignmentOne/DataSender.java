
import java.io.*;
import java.net.*;
import java.util.*;

public class DataSender extends Thread {

    private DatagramSocket socket = null;
    private int interval;
    private int packetsPerSecond;

    public DataSender(int port, int timeToSend, int packetRate) throws IOException {
		socket = new DatagramSocket(port);
		interval = timeToSend;
		packetsPerSecond = packetRate;
    }
	
    public static void main(String[] args) throws IOException {
		int port = Integer.parseInt(args[0]);
		int timeToSend = Integer.parseInt(args[1]);
		int packetRate = Integer.parseInt(args[2]);
		try {
		       Thread t = new DataSender(port, timeToSend, packetRate);
		        t.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void run() {
        boolean keepgoing = true;
        while (keepgoing) {
            try {
                byte[] buf = new byte[256];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
	      System.out.println("Waiting for a connection...");
                socket.receive(packet);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
	       System.out.println("Recieved packet from:" + 
			  "\nAddress: " + address +
		           "\nPort: "+ port + "\n");
		 
                System.out.println("Sending Data..."); 
                System.out.println("\nRate is: " + packetsPerSecond + " packets per second for " + interval + " seconds."); 
				
				for (int i = 0; i < interval; i++) 	{
	                			packet = new DatagramPacket(buf, buf.length, address, port);
	                			socket.send(packet);
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						System.out.println(e);
					}
				}


            } catch (IOException e) {
                e.printStackTrace();
            } // finally{
//             	socket.close();
//             }
        }

    }
    
    
}





