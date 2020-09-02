package RM_Replica;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.HashMap;


public class Sequencer {

	public static HashMap<Integer, String> listOfSequence = new HashMap<Integer, String>();

	public static void fromFrontEnd() {
		DatagramSocket req = null;
		try {
			// COMP_IMPL work = new COMP_IMPL();
			req = new DatagramSocket(Ports.SequencerPortNumber);
			byte[] data = new byte[1000];

			while (true) {
				DatagramPacket request = new DatagramPacket(data, data.length);
				req.receive(request);

				String incomingMessage = new String(request.getData(), 0, request.getLength());
				System.out.print(incomingMessage);
				String[]  FE_ip_address=InetAddress.getLocalHost().toString().split("/");
								
				String seqNumber = Integer.toString(listOfSequence.size() + 1);
				listOfSequence.put(Integer.parseInt(seqNumber), incomingMessage);

				String data1 = seqNumber+" " +FE_ip_address[1]+ " " + incomingMessage;
				String group = "238.255.255.255";
				send(data1, group, Ports.RM1PortNumber);
				send(data1, group, Ports.RM2PortNumber);
				send(data1, group, Ports.RM3PortNumber);
				send(data1, group, Ports.RM4PortNumber);

				
/*				
				DatagramPacket sending = new DatagramPacket(data, data.length, rem_RM1, Ports.RM1PortNumber);
				req.send(sending);
				
				DatagramPacket sending2 = new DatagramPacket(data, data.length, rem_RM2, Ports.RM2PortNumber);
				req.send(sending2);
				
				DatagramPacket sending3 = new DatagramPacket(data, data.length, rem_RM3, Ports.RM3PortNumber);
				req.send(sending3);
				
				DatagramPacket sending4 = new DatagramPacket(data, data.length, rem_RM4, Ports.RM4PortNumber);
				req.send(sending4);
*/
				

			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (req != null)
				req.close();
		}
	}

	public int Sequence_req() {
		return listOfSequence.size() + 1;
	}
	
	public static void send(String message, String ipAddress, int port) throws IOException{
		MulticastSocket socket = new MulticastSocket();
		InetAddress group = InetAddress.getByName(ipAddress);
		socket.setBroadcast(true);
		socket.joinGroup(group);
		byte[] msg = message.getBytes();
		DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
		socket.send(packet);
		socket.close();
	}

	public static void main(String args[]) {

		// UDP related task
		Runnable task = () -> {

			fromFrontEnd();
		};

		new Thread(task).start();

		System.out.println("Thread is up");

	}

}
