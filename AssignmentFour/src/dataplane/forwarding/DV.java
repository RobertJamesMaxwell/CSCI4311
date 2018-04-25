package dataplane.forwarding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class DV implements Serializable {
	int node_num;
	int[] dv; 

	public DV(int node_num) { 
		this.node_num = node_num; 
	}

	public int getNode_num() {
		return node_num;
	}

	public void setNode_num(int node_num) {
		this.node_num = node_num;
	}

	public int[] getDv() {
		return dv;
	}

	public void setDv(int[] dv) {
		this.dv = dv;
	} 
	
	public byte[] getBytes() {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try {
			ObjectOutputStream os = new ObjectOutputStream(byteOut);
			os.writeObject(this); 	// marshal an object into byte stream
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		byte[] bytes = byteOut.toByteArray();
		return bytes;
	}

	public static DV bytes2DV(byte[] bytearray) {
		ByteArrayInputStream in = new ByteArrayInputStream(bytearray); 
		DV newDV = null; 
		try {
			ObjectInputStream is = new ObjectInputStream(in);
			newDV = (DV) is.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return newDV;
	}

	public String toString() { 
		return "node_no=" + node_num + "\n" + Arrays.toString(dv); 
	} 
	
	// tester 
	public static void main(String args[]) { 
		DV obj = new DV(9); 
		int[] dv = new int[24]; 
		Arrays.fill(dv, 88);
		obj.setDv(dv); 
		byte[] bytearray = obj.getBytes(); 
		System.out.println(obj);
		DV newObj = DV.bytes2DV(bytearray); 
		System.out.println("Recovered DV object = " + newObj); 

	}
}
