import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import DEMSInterfaceApp.DEMSInterface;
import DEMSInterfaceApp.DEMSInterfaceHelper;

public class DEMSTorontoServer {

	public static HashMap<String, HashMap<String, Integer>> torDb = new HashMap<>();
	public static HashMap<String, ArrayList<String>> torCustomerInfo = new HashMap<>();
	public static HashMap<String, int[]> clientsMonths = new HashMap<>();
	static DEMSTorontoServer obj;
	static String[] eventTypes = { "Conference", "Seminar", "TradeShow" };
	static PrintWriter writer;
	static DEMSInterfaceImpl iml = new DEMSInterfaceImpl();
	DEMSTorontoServer(){
		HashMap<String, Integer> dummyValsConf = new HashMap<>();
		dummyValsConf.put("TORM100519", 5);
		dummyValsConf.put("TORA100519", 10);
		dummyValsConf.put("TORE100519", 15);
		torDb.put(eventTypes[0], dummyValsConf);

		HashMap<String, Integer> dummyValsSem = new HashMap<>();
		dummyValsSem.put("TORM110519", 5);
		dummyValsSem.put("TORA110519", 10);
		dummyValsSem.put("TORE110519", 15);
		torDb.put(eventTypes[1], dummyValsSem);

		HashMap<String, Integer> dummyValsTS = new HashMap<>();
		dummyValsTS.put("TORM120519", 1);
		dummyValsTS.put("TORA120519", 1);
		dummyValsTS.put("TORE120519", 1);
		torDb.put(eventTypes[2], dummyValsTS);
	}
	public static void main(String args[]) {

//		Seminar, Conference and TradeShow keys are populated with dummy event data
		obj = new DEMSTorontoServer();
		try {
			// create and initialize the ORB //
			ORB orb = ORB.init(args, null);
			
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			DEMSInterfaceImpl remoteObj = new DEMSInterfaceImpl();
			remoteObj.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(remoteObj);
			
			
			// and cast the reference to a CORBA reference
			DEMSInterface href = DEMSInterfaceHelper.narrow(ref);

			// get the root naming context
			// NameService invokes the transient name service
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			
			// Use NamingContextExt, which is part of the
			// Interoperable Naming Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// bind the Object Reference in Naming
			NameComponent path[] = ncRef.to_name("toronto");
			ncRef.rebind(path, href);

			System.out.println("Toronto Server ready and waiting ...");
			displayTorDbContents();
			// wait for invocations from clients
			
			Runnable task2 = () -> {
				montrealListener();
			};
			Runnable task3 = () -> {
				ottawaListener();
			};

			Thread thread2 = new Thread(task2);
			Thread thread3 = new Thread(task3);
			thread2.start();
			thread3.start();

			for (;;) {
				orb.run();
			}
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println("HelloServer Exiting ...");

	}




//	TODO: make hashmap into arraylist in all servers







	


//	Helper Method to print contents of the modified DB after an operation done on it
	public static void displayTorDbContents() {
		System.out.println("\n------------DATABASE CONT.------------");
		for (String et : eventTypes) {
			System.out.println(et + ": ");
			HashMap<String, Integer> temp = torDb.getOrDefault(et, new HashMap<String, Integer>());
			System.out.println(temp.keySet().toString());
			System.out.println(temp.values());
		}
		System.out.println("-----------------------------------");
	}

	public static void displayCustomerInfo() {
		System.out.println("\n------------CLIENT INFO------------");
		for (String cID : torCustomerInfo.keySet()) {
			System.out.print(cID + ": ");
			System.out.println(torCustomerInfo.get(cID).toString());
		}
		System.out.println("-----------------------------------");
	}


	
	public static String UDPclient(String customerID, String eventID, String eventType, String action) {
		int serverPort;
		if (eventID.contains("MTL")) {
			System.out.println("\n\nRequesting Montreal Server...");
			serverPort = 1001;
		} else if (eventID.contains("OTW")) {
			System.out.println("\n\nRequesting Ottawa Server...");
			serverPort = 2002;
		} else {
			return "Something went wrong in UDPCommunication";
		}
		String send = action + "," + customerID + "," + eventID + "," + eventType + ",";
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(); // reference of the original socket
			byte[] message = send.getBytes(); // message to be passed is stored in byte array
			InetAddress aHost = InetAddress.getByName("localhost"); // Host name is specified and the IP address of
			DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);// request packet
			aSocket.send(request);// request sent out
			byte[] buffer = new byte[1000];// to store the received data, it will be populated by what receive method
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);// reply packet ready but not populated.
			aSocket.receive(reply);// reply received and will populate reply packet now.
			
			return (new String(reply.getData()));	
			
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();// now all resources used by the socket are returned to the OS, so that there is
		}
		return "Something went wrong in UDPCommunication";
	}
	
	public static boolean ottawaListener() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(3002);
			byte[] buffer = new byte[1000];// to stored the received data from
			// the client.
			System.out.println("Listener Server Started for Ottawa...");
			while (true) {// non-terminating loop as the server is always in listening mode.
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);

				aSocket.receive(request);// request received

				System.out.println("Request received from client: " + new String(request.getData()));
				String message = new String(request.getData());

				String[] list = message.split(",");
				DatagramPacket reply = null;
				if (list[0].equals("book")) {
					
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					
					String buff = iml.bookEvent(list[1], list[2], list[3]);
//					 = list[3] + ":" + list[2] + " successfully booked for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply																								// packet
																													// ready
				} else if (list[0].equals("cancel")) {
					
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					
					String buff = iml.cancelEvent(list[1], list[2]);
//					 = list[3] + ":" + list[2] + " successfully cancelled for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
				
				}else if (list[0].equals("listEventAvailability")) {
					
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					
					String ans = iml.listEventAvailability(list[3]);
//					String buff = list[3] + ":" + list[2] + "Availability displayed for " + list[3];
					byte[] replied = ans.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
				
				}
				aSocket.send(reply);// reply sent
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();

		}
		return false;
	}

	public static boolean montrealListener() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(3001);
			byte[] buffer = new byte[1000];// to stored the received data from
			// the client.
			System.out.println("Listener Server Started for Montreal...");
			while (true) {// non-terminating loop as the server is always in listening mode.
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);

				// Server waits for the request to come
				aSocket.receive(request);// request received

				System.out.println("Request received from client: " + new String(request.getData()));
				String message = new String(request.getData());

				String[] list = message.split(",");
				DatagramPacket reply = null;
				if (list[0].equals("book")) {
					
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					
					String buff = iml.bookEvent(list[1], list[2], list[3]);
//					 = list[3] + ":" + list[2] + " successfully Booked! for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
																													// ready
				} else if (list[0].equals("cancel")) {
					
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					
					String buff = iml.cancelEvent(list[1], list[2]);
//					 = list[3] + ":" + list[2] + " successfully cancelled for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
				
				}else if (list[0].equals("listEventAvailability")) {
					
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					
					String buff = iml.listEventAvailability(list[3]);
//					 = list[3] + ":" + list[2] + "Availability displayed for " + list[3];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
				
				}

				aSocket.send(reply);// reply sent
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();

		}
		return false;
	}

}
