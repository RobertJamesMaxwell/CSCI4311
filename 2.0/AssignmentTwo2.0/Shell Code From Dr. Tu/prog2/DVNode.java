/**
 * 
 */
package prog2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author 
 *
 */
public class DVNode { 
	
	String myIP; 
	int myNodeNum; 
	String coordIP; 
	int coordPort; 
	DatagramSocket mySoc; 
	MulticastSocket multiOutSoc; 
	MulticastSocket multiInSoc; 
	
	public DVNode(String coordIP, int coordPort) { 
		this.coordIP = coordIP; 
		this.coordPort = coordPort; 
		try { 
			mySoc = new DatagramSocket(); 
		} catch (SocketException se) {
			se.printStackTrace(); 
		}
	} 
	
	void initialize() {
		
		// get the IP number of the residing computer (virtual machine) 
		myIP = null; // ... 
		
		// send myIP to coordinator 
		byte[] myIPba = myIP.getBytes();  
		InetAddress coordAddress = null; 
		try {
			coordAddress = InetAddress.getByName(coordIP); 
		} catch (UnknownHostException ukhe) { 
			ukhe.printStackTrace();
		}
		DatagramPacket dp = new DatagramPacket(myIPba, myIPba.length, coordAddress, coordPort); 
		// ... 
		try { 
			mySoc.send(dp);
		} catch (IOException ioe) { 
			ioe.printStackTrace();
		}
		
		// ...
		
	}

	void operate() { 
		DVSender dvSender = new DVSender(multiOutSoc); 
		DVReceiver dvReceiver = new DVReceiver(multiInSoc); 
		dvReceiver.start();
		dvSender.start(); 
	} 
	
	// tester 
	public static void main(String args[]) { 
		if (args.length != 2) { 
			System.out.println("Usage: java DVNode coordinator-IP coordinator-port"); 
			System.exit(0);
		} 
		int port = Integer.parseInt(args[1]); 
		DVNode dvNode = new DVNode(args[0], port); 
		dvNode.initialize(); 
		dvNode.operate(); 
	}
}
