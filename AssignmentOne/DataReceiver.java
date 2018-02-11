
import java.io.*;
import java.net.*;
import java.util.*;

public class DataReceiver {
    public static void main(String[] args) throws IOException {

		// get a datagram socket
		 DatagramSocket socket = new DatagramSocket();

		// send request
		byte[] buf = new byte[256];
		InetAddress address = InetAddress.getByName(args[0]);
		int port = Integer.parseInt(args[1]);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
		socket.send(packet);
    
        		// get response
		packet = new DatagramPacket(buf, buf.length);
		
		int packetCount = 0;
		while(true)	{
			socket.receive(packet);
			System.out.println("Received packet " + packetCount);
			packetCount++;
		}
    
        //socket.close();
    }
} 




