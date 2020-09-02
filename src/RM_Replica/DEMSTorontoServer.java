package RM_Replica;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

	private static HashMap<String, HashMap<String, Integer>> torDb = new HashMap<>();
	private static HashMap<String, ArrayList<String>> torCustomerInfo = new HashMap<>();
	private static HashMap<String, int[]> clientsMonths = new HashMap<>();
	static String[] eventTypes = { "Conference", "Seminar", "TradeShow" };
	static PrintWriter writer;

	public static void main(String args[]) {

//		Seminar, Conference and TradeShow keys are populated with dummy event data
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
		try {
			displayTorDbContents();
			// wait for invocations from clients
			
			Runnable task1 = () -> {
				ReplicaManagerListener();
			};
			Runnable task2 = () -> {
				montrealListener();
			};
			Runnable task3 = () -> {
				ottawaListener();
			};
			
			Thread thread1 = new Thread(task1);
			Thread thread2 = new Thread(task2);
			Thread thread3 = new Thread(task3);	
			thread1.start();
			thread2.start();
			thread3.start();

		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println("HelloServer Exiting ...");

	}

	public  static String addEvent(String eventID, String eventType, String bc) {
		String response = "";
		HashMap<String, Integer> temp = (HashMap<String, Integer>) torDb.get(eventType).clone();
		int bookingCapacity = Integer.parseInt(bc);

		if (temp.containsKey(eventID)) {
			int newCap = temp.get(eventID) + bookingCapacity;
			temp.put(eventID, newCap);
			response = "Event was already added so booking capacity updated.";
		}else {
			temp.put(eventID, bookingCapacity);
			response = "Event added successfully.";
		}
		torDb.put(eventType, temp);
//		displaytorDbContents();
		displayCustomerInfo();
		return response;
	}

	public  static String removeEvent(String eventID, String eventType) {
//		dummyVals always stored in the DB so no need to check for an empty sub-HashMap for any eventType
		String response = "";
		HashMap<String, Integer> temp = (HashMap<String, Integer>) torDb.get(eventType).clone();

		if (!temp.containsKey(eventID)) {
			response = "The event with eventID you wanted to remove does not exit.";
		}
		for (ArrayList<String> al : torCustomerInfo.values()) {
			if (al.contains(eventID)) {
				logOperation("Deleted Event which has been booked!", eventID, eventType, "NA", "NA", "Succeeded");
			}
		}
		temp.remove(eventID);
		torDb.put(eventType, temp);
		response = "Event was successfully removed.";
//		displayTorDbContents();
		displayCustomerInfo();
		return response;
	}

//	TODO: make hashmap into arraylist in all servers
	public  static String listEventAvailability(String eventType) {
		if(eventType.contains("REQUEST")) {
			String[] find = eventType.split("\\|");
			HashMap<String, Integer> temp = torDb.getOrDefault(find[0], new HashMap<String, Integer>());
			String ret = "";
			
			for(String key: temp.keySet()) {
				ret += (key + " " + temp.get(key) + ",");
			}
			
			return ret;
		}
		else {
			System.out.println(eventType + "request Initiated in TOR");
			HashMap<String, Integer> temp = torDb.getOrDefault(eventType, new HashMap<String, Integer>());
			ArrayList<String> ret = new ArrayList<>();
			
			for(String key: temp.keySet()) {
				ret.add(key + " " + temp.get(key));
			}
			String udpEvents = UDPclient("MTL", "MTL", eventType+"|REQUEST", "listEventAvailability");
			for(String item: udpEvents.split(",")) {
				ret.add(item);
			}
			ret.remove(ret.size()-1);
			udpEvents = UDPclient("OTW", "OTW", eventType+"|REQUEST", "listEventAvailability");
			for(String item: udpEvents.split(",")) {
				ret.add(item);
			}
			ret.remove(ret.size()-1);
			return ret.toString();
		}
	}

	public  static String bookEvent(String customerID, String eventID, String eventType) {
		int month = Integer.parseInt(eventID.substring(6, 8)) - 1;
		if (torCustomerInfo.containsKey(customerID) && torCustomerInfo.get(customerID) != null
				&& torCustomerInfo.get(customerID).contains(eventID)) {
			return "Client already registered for this event.";
		}
		if (eventID.contains("TOR")) {
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			HashMap<String, Integer> temp = (HashMap<String, Integer>) torDb.getOrDefault(eventType, map).clone();
			
			if (!temp.containsKey(eventID))
				return "There is no event corresponding to the eventID you entered.";
			if (temp.get(eventID) == 0)
				return "Sorry the event is full.";
			
			temp.put(eventID, temp.get(eventID) - 1);
			torDb.put(eventType, temp);
			ArrayList<String> temp1 = torCustomerInfo.get(customerID) == null ? (new ArrayList<>())
					: torCustomerInfo.get(customerID);
			temp1.add(eventID);
			torCustomerInfo.put(customerID, temp1);
			return "Event booked successfully.";
		} else {
			if (!clientsMonths.isEmpty() && clientsMonths.get(customerID) != null
					&& clientsMonths.get(customerID)[month] == 3) {
				return "User already registered for 3 events outside their city in this month.";
			}
			String response = UDPclient(customerID, eventID, eventType, "book");
			if(response.contains("Event booked successfully.")) {
				int newCurrRemoteEvents = clientsMonths.isEmpty() || clientsMonths.get(customerID) == null ? 1
						: clientsMonths.get(customerID)[month] + 1;
				int[] updated = clientsMonths.isEmpty() || clientsMonths.get(customerID) == null ? new int[12]
						: clientsMonths.get(customerID);
				updated[month] = newCurrRemoteEvents;
				clientsMonths.put(customerID, updated);

				ArrayList<String> temp1 = torCustomerInfo.get(customerID) == null ? (new ArrayList<>())
						: torCustomerInfo.get(customerID);
				temp1.add(eventID);
				torCustomerInfo.put(customerID, temp1);
				return response;
			}
			return response;
		}
	}

	public  static String cancelEvent(String customerID, String eventID) {
		int month = Integer.parseInt(eventID.substring(6,8))-1;
		if (torCustomerInfo.containsKey(customerID) && torCustomerInfo.get(customerID) != null
				&& torCustomerInfo.get(customerID).contains(eventID)) {

			if (eventID.contains("TOR")) {
				for (String eType : torDb.keySet()) {
					for (String eID : torDb.get(eType).keySet()) {
						if (eID.equalsIgnoreCase(eventID)) {
							ArrayList<String> temp1 = torCustomerInfo.get(customerID);
							temp1.remove(eventID);
							if (temp1.size() == 0)
								torCustomerInfo.remove(customerID);
							else
								torCustomerInfo.put(customerID, temp1);
							HashMap<String, Integer> temp = (HashMap<String, Integer>) torDb.get(eType).clone();
							temp.put(eID, temp.get(eID) + 1);
							torDb.put(eType, temp);
							return "Customer has been removed from this event.";
						}
					}
				}
			} else {
				for (String eType : torDb.keySet()) {
					for (String eID : torDb.get(eType).keySet()) {
						if (eID.equalsIgnoreCase(eventID)) {
							HashMap<String, Integer> temp = (HashMap<String, Integer>) torDb.get(eType).clone();
							temp.put(eID, temp.get(eID) + 1);
							torDb.put(eType, temp);
						}
					}
				}
				String response = UDPclient(customerID, eventID, "","cancel");
				if(response.contains("Customer has been removed from this event.")) {
					int newCurrRemoteEvents = clientsMonths.isEmpty() || clientsMonths.get(customerID) == null
							|| clientsMonths.get(customerID)[month] == 0 ? 0 : clientsMonths.get(customerID)[month] - 1;
					int[] updated = clientsMonths.isEmpty() || clientsMonths.get(customerID) == null ? new int[12]
							: clientsMonths.get(customerID);
					updated[month] = newCurrRemoteEvents;
					clientsMonths.put(customerID, updated);

					ArrayList<String> temp1 = torCustomerInfo.get(customerID);
					temp1.remove(eventID);
					return response;
				}
				return response;
			}
		}
		return "Customer is not registered in event corresponding to entered eventID and eventType.";
	}

	public  static String getBookingSchedule(String customerID) {
		if (!torCustomerInfo.containsKey(customerID)) {
			return "No events to display.";
		}
		return customerID + ":" + torCustomerInfo.get(customerID).toString();
	}
	
	public  static String swapEvent (String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType){
		if (torCustomerInfo.containsKey(customerID) && torCustomerInfo.get(customerID).contains(oldEventID)) {
			String book = bookEvent(customerID, newEventID, newEventType);
			if (book.equals("Event booked successfully.") || book.contains("Event booked successfully.")) {
				cancelEvent(customerID, oldEventID);
				return "Event swapped successfully.";
			}
			return "Events were not swapped since " + book;
		}
		return "Customer is not registered in entered event.";
	}

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

	public static void logOperation(String name, String eventID, String eventType, String customerID, String bookingCap,
			String status) {

		try {
			FileWriter fw = new FileWriter("TorontoLogs.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			writer = new PrintWriter(bw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
		String log = "\n" + name + "Performed.\nTime: " + timeStamp + "\nCustomerID: " + customerID + "\nEventID: "
				+ eventID + "\nEventType: " + eventType + "\nBooking Capacity: " + bookingCap + "\nStatus: " + status;
		writer.println(log);
		writer.close();
	}
	
	private static String UDPclient(String customerID, String eventID, String eventType, String action) {
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
					
					String buff = bookEvent(list[1], list[2], list[3]);
//					 = list[3] + ":" + list[2] + " successfully booked for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply																								// packet
																													// ready
				} else if (list[0].equals("cancel")) {
					
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					
					String buff = cancelEvent(list[1], list[2]);
//					 = list[3] + ":" + list[2] + " successfully cancelled for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
				
				}else if (list[0].equals("listEventAvailability")) {
					
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					
					String ans = listEventAvailability(list[3]);
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
					
					String buff = bookEvent(list[1], list[2], list[3]);
//					 = list[3] + ":" + list[2] + " successfully Booked! for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
																													// ready
				} else if (list[0].equals("cancel")) {
					
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					
					String buff = cancelEvent(list[1], list[2]);
//					 = list[3] + ":" + list[2] + " successfully cancelled for " + list[1];
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());// reply
				
				}else if (list[0].equals("listEventAvailability")) {
					
					System.out.println("listener booked for :" + list[1] + list[2] + list[3]);
					
					String buff = listEventAvailability(list[3]);
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

	public static boolean ReplicaManagerListener() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(Ports.RM1TorontoPortNumber);
			byte[] buffer = new byte[1000];
			System.out.println("Listener Server Started for RM...");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				System.out.println("Request received from client: " + new String(request.getData()));
				String message = new String(request.getData());
				String[] list = message.split("\\s+");
				DatagramPacket reply = null;
				
				if (list[3].equals("1")) {
					
					System.out.println("listener booked for :" + list[4] + list[5] + list[6]);
					String buff = bookEvent(list[4].trim(), list[5].trim(), list[6].trim());
					byte[] replied = buff.getBytes(); 
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());
					
				} else if (list[3].equals("2")) {
					
					System.out.println("listener booked for :" + list[4]);
					String buff = getBookingSchedule(list[4].trim());
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());

				}else if (list[3].equals("3")) {
					
					System.out.println("listener booked for :" + list[4] + list[5] + list[6]);
					String buff = cancelEvent(list[4].trim(), list[5].trim());
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());
				
				}
				else if (list[3].equals("4")) {
					
					System.out.println("listener booked for :" + list[4] + list[5] + list[6]);
					String buff = addEvent(list[4].trim(), list[5].trim(), list[6].trim());
					byte[] replied = buff.getBytes(); 
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());
					
				} else if (list[3].equals("5")) {
					
					System.out.println("listener booked for :" + list[4] + list[5]);
					String buff = removeEvent(list[4].trim(), list[5].trim());
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());

				}else if (list[3].equals("6")) {
					
					System.out.println("listener booked for :" + list[4]);
					String buff = listEventAvailability(list[4].trim());
					byte[] replied = buff.getBytes();
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());
				
				}
				else if (list[3].equals("7")) {
					
					System.out.println("listener booked for :" + list[4] + list[5] + list[6] + list[7] + list[8]);
					String buff = swapEvent(list[4].trim(), list[5].trim(), list[6].trim(), list[7].trim(), list[8].trim());
					byte[] replied = buff.getBytes(); 
					reply = new DatagramPacket(replied, replied.length, request.getAddress(), request.getPort());
				
				}

				aSocket.send(reply);
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
	

} // end class
