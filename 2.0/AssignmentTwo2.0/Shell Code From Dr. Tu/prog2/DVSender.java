/**
 * 
 */
package prog2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * @author 
 *
 */
class DVSender extends Thread { 
	
	MulticastSocket multOutSocket; 
	
	/**
	 * 
	 * @param multOutSocket -- this socket already joins with every neighbor's multicast IPs
	 */
	public DVSender(MulticastSocket multOutSocket) { 
		this.multOutSocket = multOutSocket; 
		// ...
		
	}
	
	public void send2Neighbor(byte[] payload) { 
		//...
		
	} 
	
	public void run() { 
		// create DV object 
		DV myDv = null; // = new DV(...) 
		
		for (int j=0; j<10; j++) { 
			byte[] dvBA = myDv.getBytes(); 
			send2Neighbor(dvBA); 
			try {sleep(1000);} catch (Exception e) {e.printStackTrace();} 
		}
	}

}

class DVReceiver extends Thread {
	MulticastSocket multiInSocket; 

	public DVReceiver(MulticastSocket multiInSocket) { 
		// …
	}
	
	public byte[] receiveFromNeighbor(int packSize) { 
		DatagramPacket inPack = null; 
		byte[] buffer = new byte[packSize]; 
		try {
			inPack = new DatagramPacket(buffer, buffer.length); 
			multiInSocket.receive(inPack); 
			// ... 
			
		} catch (UnknownHostException uhe) {
			uhe.printStackTrace();
		} catch (IOException ioe) { 
			ioe.printStackTrace();
		} 
		byte[] data = inPack.getData(); 
		return data; 
	} 

	public void run() { 
		// create DV object 
		DV myDv = null; // = new DV(...) 
		
		for (int j=0; j<10; j++) { 
			// ...
			byte[] dvBA = this.receiveFromNeighbor(512); 
			DV dv = DV.bytes2DV(dvBA); 
			System.out.println(dv);
		}
	}
}
