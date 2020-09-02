import org.omg.CORBA.ORB;
import DEMSInterfaceApp.DEMSInterfacePOA;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class DEMSInterfaceImpl extends DEMSInterfacePOA {
	private ORB orb;
	static PrintWriter writer;
	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	@Override
	public  String addEvent(String eventID, String eventType, String bookingCapacity)  {
		String response = "Invalid input";
		HashMap<String, HashMap<String, Integer>> ServerHashDB=null;
		if (isValid(eventID, eventType, Integer.parseInt(bookingCapacity))) {
			if (eventID.contains("TOR")) {
				ServerHashDB = DEMSTorontoServer.torDb;

			} else if (eventID.contains("MTL")) {
				ServerHashDB = DEMSMontrealServer.mtlDb;
			} else if (eventID.contains("OTW")) {
				 ServerHashDB = DEMSOttawaServer.otwDb;
			}

			HashMap<String, Integer> temp = (HashMap<String, Integer>) ServerHashDB.get(eventType).clone();
			int bc = Integer.parseInt(bookingCapacity);

			if (temp.containsKey(eventID)) {
				int newCap = temp.get(eventID) + bc;
				temp.put(eventID, newCap);
				response = "Event was already added so booking capacity updated.";
			}else {
				temp.put(eventID, bc);
				response = "Event added successfully.";
			}
			ServerHashDB.put(eventType, temp);
			//displaytorDbContents();
			if(eventID.contains("TOR")) {
				DEMSTorontoServer.displayCustomerInfo();
			}
			else if(eventID.contains("MTL")){
				DEMSMontrealServer.displayCustomerInfo();
			}
			else if(eventID.contains("OTW")){
				DEMSOttawaServer.displayCustomerInfo();
			}
			logOperation("addEvent", eventID, eventType, "NA", "NA", response);
		}
		return response;
	}

	@Override
	public  String removeEvent(String eventID, String eventType) {
		String response = "Invalid input";
		HashMap<String, HashMap<String, Integer>> ServerHashDB=null;
		HashMap<String, ArrayList<String>> CustomerInfo = null;
		if(isValid(eventID, eventType)) {
			if (eventID.contains("TOR")) {
				ServerHashDB = DEMSTorontoServer.torDb;
				CustomerInfo = DEMSTorontoServer.torCustomerInfo;

			} else if (eventID.contains("MTL")) {
				ServerHashDB = DEMSMontrealServer.mtlDb;
				CustomerInfo = DEMSMontrealServer.mtlCustomerInfo;

			} else if (eventID.contains("OTW")) {
				ServerHashDB = DEMSOttawaServer.otwDb;
				CustomerInfo = DEMSOttawaServer.otwCustomerInfo;

			}


			HashMap<String, Integer> temp = (HashMap<String, Integer>) ServerHashDB.get(eventType).clone();

			if (!temp.containsKey(eventID)) {
				response = "The event with eventID you wanted to remove does not exit.";
			}
			for (ArrayList<String> al : CustomerInfo.values()) {
				if (al.contains(eventID)) {
					logOperation("Deleted Event which has been booked!", eventID, eventType, "NA", "NA", "Succeeded");
				}
			}
			temp.remove(eventID);
			ServerHashDB.put(eventType, temp);
			response = "Event was successfully removed.";
//		displayTorDbContents();
			if (eventID.contains("TOR")) {


				DEMSTorontoServer.displayCustomerInfo();
			} else if (eventID.contains("MTL")) {


				DEMSMontrealServer.displayCustomerInfo();
			} else if (eventID.contains("OTW")) {


				DEMSOttawaServer.displayCustomerInfo();
			}

			logOperation("removeEvent", eventID, eventType,"NA","NA", response);
		}
		return response;
	}
	
	@Override
	public  String listEventAvailability(String eventType) {


		if(eventType.contains("REQUEST")) {
			String[] find = eventType.split("\\|");
			HashMap<String, Integer> temp=null;
			if(eventType.contains("REQUESTTOR"))
				 temp = DEMSTorontoServer.torDb.getOrDefault(find[0], new HashMap<String, Integer>());
			else if(eventType.contains("REQUESTOTW"))
				temp = DEMSOttawaServer.otwDb.getOrDefault(find[0], new HashMap<String, Integer>());
			else if(eventType.contains("REQUESTMTL"))
				temp = DEMSMontrealServer.mtlDb.getOrDefault(find[0], new HashMap<String, Integer>());
			String ret = "";

			for(String key: temp.keySet()) {
				ret += (key + " " + temp.get(key) + ",");
			}
			logOperation("listEventAvailability", "NA", eventType,"NA","NA", "Succeeded");
			return ret;
		}
		else if(eventType.contains("TORM")||eventType.contains("OTWM")||eventType.contains("MTLM")){
				HashMap<String, Integer> temp=null;

				String strippedEventType = eventType.split(",")[0];

				if(eventType.contains(("TORM")))
					 temp = DEMSTorontoServer.torDb.getOrDefault(strippedEventType, new HashMap<String, Integer>());
				else if(eventType.contains(("OTWM")))
					temp = DEMSOttawaServer.otwDb.getOrDefault(strippedEventType, new HashMap<String, Integer>());
				else if(eventType.contains(("MTLM")))
					temp = DEMSMontrealServer.mtlDb.getOrDefault(strippedEventType, new HashMap<String, Integer>());


				ArrayList<String> ret = new ArrayList<>();
				for (String key : temp.keySet()) {
					ret.add(key + " " + temp.get(key));
				}
				String udpEvents=null;
				if(eventType.contains(("TORM")))
					 udpEvents = DEMSTorontoServer.UDPclient("MTL", "MTL", strippedEventType + "|REQUESTMTL", "listEventAvailability");
				else if(eventType.contains("OTWM"))
					udpEvents = DEMSOttawaServer.UDPclient("MTL", "MTL", strippedEventType + "|REQUESTMTL", "listEventAvailability");
				else if(eventType.contains("MTLM"))
					udpEvents = DEMSMontrealServer.UDPclient("OTW", "OTW", strippedEventType + "|REQUESTOTW", "listEventAvailability");

				for (String item : udpEvents.split(",")) {
					ret.add(item);
				}
				ret.remove(ret.size() - 1);

				if(eventType.contains(("TORM")))
					udpEvents = DEMSTorontoServer.UDPclient("OTW", "OTW", strippedEventType + "|REQUESTOTW", "listEventAvailability");
				else if(eventType.contains(("OTWM")))
					udpEvents = DEMSOttawaServer.UDPclient("TOR", "TOR", strippedEventType + "|REQUESTTOR", "listEventAvailability");
				else if(eventType.contains(("MTLM")))
					udpEvents = DEMSMontrealServer.UDPclient("TOR", "TOR", strippedEventType + "|REQUESTTOR", "listEventAvailability");
				for (String item : udpEvents.split(",")) {
					ret.add(item);
				}
				ret.remove(ret.size() - 1);
			logOperation("listEventAvailability", "NA", eventType,"NA","NA", "Succeeded");
				return ret.toString();


		}
		return "";
	}

	@Override
	public  String bookEvent(String customerID, String eventID, String eventType) {
		 HashMap<String, ArrayList<String>> CustomerInfo = null;
		 HashMap<String, HashMap<String, Integer>> ServerHashDB = null;
		HashMap<String, int[]> clientsMonths =null;
		if( !isValid(customerID))
			return "Invalid Customer ID!";
		//eventType=eventType.split("REQUEST")[0];
		if(isValid(eventID, eventType.split("REQUEST")[0])) {
			if(customerID.contains("TOR")) {
				ServerHashDB = DEMSTorontoServer.torDb;
				CustomerInfo = DEMSTorontoServer.torCustomerInfo;
				clientsMonths = DEMSTorontoServer.clientsMonths;
			}
			else if(customerID.contains("OTW")) {
				ServerHashDB = DEMSOttawaServer.otwDb;
				CustomerInfo = DEMSOttawaServer.otwCustomerInfo;
				clientsMonths = DEMSOttawaServer.clientsMonths;
			}
			else if(customerID.contains("MTL")) {
				ServerHashDB = DEMSMontrealServer.mtlDb;
				CustomerInfo = DEMSMontrealServer.mtlCustomerInfo;
				clientsMonths = DEMSMontrealServer.clientsMonths;
			}
		}

		if(eventType.contains("REQUEST")&&eventID.contains("MTL")){

			ServerHashDB = DEMSMontrealServer.mtlDb;
			CustomerInfo = DEMSMontrealServer.mtlCustomerInfo;
			clientsMonths = DEMSMontrealServer.clientsMonths;
		}
		else if(eventType.contains("REQUEST")&&eventID.contains("TOR")){
			ServerHashDB = DEMSTorontoServer.torDb;
			CustomerInfo = DEMSTorontoServer.torCustomerInfo;
			clientsMonths = DEMSMontrealServer.clientsMonths;
		}
		else if(eventType.contains("REQUEST")&&eventID.contains("OTW")){
			ServerHashDB = DEMSOttawaServer.otwDb;
			CustomerInfo = DEMSOttawaServer.otwCustomerInfo;
			clientsMonths = DEMSMontrealServer.clientsMonths;
		}
		//logOperation("bookEvent", eventID, eventType.split("REQUEST")[0],"NA","NA", "whatever");

		int month = Integer.parseInt(eventID.substring(6, 8)) - 1;
		if (CustomerInfo.containsKey(customerID) && CustomerInfo.get(customerID) != null
				&& CustomerInfo.get(customerID).contains(eventID)) {
			logOperation("bookEvent", eventID, eventType.split("REQUEST")[0],"NA","NA", "Client already registered for this event.");
			return "Client already registered for this event.";
		}

		if (eventType.contains("REQUEST")||eventID.contains("MTL")&&customerID.contains("MTL")||eventID.contains("TOR")&&customerID.contains("TOR")||eventID.contains("OTW")&&customerID.contains("OTW")) {
			eventType=eventType.split("REQUEST")[0];
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			String strippedEventType = eventType.split(",")[0];
			HashMap<String, Integer> temp = (HashMap<String, Integer>) ServerHashDB.getOrDefault(strippedEventType, map).clone();

			if (!temp.containsKey(eventID)){
				logOperation("bookEvent", eventID, eventType.split("REQUEST")[0],"NA","NA", "There is no event corresponding to the eventID you entered.");
				return "There is no event corresponding to the eventID you entered.";}
			if (temp.get(eventID) == 0){
				logOperation("bookEvent", eventID, eventType.split("REQUEST")[0],"NA","NA", "Sorry the event is full.");
				return "Sorry the event is full.";}

			temp.put(eventID, temp.get(eventID) - 1);
			ServerHashDB.put(strippedEventType, temp);
			ArrayList<String> temp1 = CustomerInfo.get(customerID) == null ? (new ArrayList<>())
					: CustomerInfo.get(customerID);
			temp1.add(eventID);
			CustomerInfo.put(customerID, temp1);
			logOperation("bookEvent", eventID, eventType.split("REQUEST")[0],"NA","NA", "Event booked successfully.");
			return "Event booked successfully.";



		} else {

			if (!clientsMonths.isEmpty() && clientsMonths.get(customerID) != null
					&& clientsMonths.get(customerID)[month] == 3) {
				logOperation("bookEvent", eventID, eventType.split("REQUEST")[0],"NA","NA", "User already registered for 3 events outside their city in this month.");
				return "User already registered for 3 events outside their city in this month.";
			}
			String response =null;
			if(customerID.contains("TOR"))
				response = DEMSTorontoServer.UDPclient(customerID, eventID, eventType+ "REQUEST", "book");
			if(customerID.contains("MTL"))
				response = DEMSMontrealServer.UDPclient(customerID, eventID, eventType+ "REQUEST", "book");
			if(customerID.contains("OTW"))
				response = DEMSOttawaServer.UDPclient(customerID, eventID, eventType+ "REQUEST", "book");
			if(response.contains("Event booked successfully.")) {
				int newCurrRemoteEvents = clientsMonths.isEmpty() || clientsMonths.get(customerID) == null ? 1
						: clientsMonths.get(customerID)[month] + 1;
				int[] updated = clientsMonths.isEmpty() || clientsMonths.get(customerID) == null ? new int[12]
						: clientsMonths.get(customerID);
				updated[month] = newCurrRemoteEvents;
				clientsMonths.put(customerID, updated);

				ArrayList<String> temp1 = CustomerInfo.get(customerID) == null ? (new ArrayList<>())
						: CustomerInfo.get(customerID);
				temp1.add(eventID);
				CustomerInfo.put(customerID, temp1);
				logOperation("bookEvent", eventID, eventType.split("REQUEST")[0],"NA","NA", response);
				return response;
			}
			logOperation("bookEvent", eventID, eventType.split("REQUEST")[0],"NA","NA", response);
			return response;

		}


	}

	@Override
	public  String getBookingSchedule(String customerID) {
		HashMap<String, ArrayList<String>> CustomerInfo = null;

		if(customerID.contains("TOR")) {
			CustomerInfo = DEMSTorontoServer.torCustomerInfo;


		}
		else if(customerID.contains("OTW")) {
			CustomerInfo = DEMSOttawaServer.otwCustomerInfo;


		}
		else if(customerID.contains("MTL")) {
			CustomerInfo = DEMSMontrealServer.mtlCustomerInfo;

		}
		if (!CustomerInfo.containsKey(customerID)) {
			return "No events to display.";
		}
		logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
		return customerID + ":" + CustomerInfo.get(customerID).toString();



	}

	@Override
	public  String cancelEvent(String customerID, String eventID) {
		HashMap<String, ArrayList<String>> CustomerInfo = null;
		HashMap<String, HashMap<String, Integer>> ServerHashDB = null;
		HashMap<String, int[]> clientsMonths =null;
		if(customerID.contains("TOR")) {
			ServerHashDB = DEMSTorontoServer.torDb;
			CustomerInfo = DEMSTorontoServer.torCustomerInfo;
			clientsMonths = DEMSTorontoServer.clientsMonths;
		}
		else if(customerID.contains("OTW")) {
			ServerHashDB = DEMSOttawaServer.otwDb;
			CustomerInfo = DEMSOttawaServer.otwCustomerInfo;
			clientsMonths = DEMSOttawaServer.clientsMonths;
		}
		else if(customerID.contains("MTL")){
			ServerHashDB = DEMSMontrealServer.mtlDb;
			CustomerInfo = DEMSMontrealServer.mtlCustomerInfo;
			clientsMonths = DEMSMontrealServer.clientsMonths;
		}
		if(eventID.contains("REQUEST")&&eventID.contains("MTL")){

			ServerHashDB = DEMSMontrealServer.mtlDb;
			CustomerInfo = DEMSMontrealServer.mtlCustomerInfo;
			clientsMonths = DEMSMontrealServer.clientsMonths;
		}
		else if(eventID.contains("REQUEST")&&eventID.contains("TOR")){
			ServerHashDB = DEMSTorontoServer.torDb;
			CustomerInfo = DEMSTorontoServer.torCustomerInfo;
			clientsMonths = DEMSMontrealServer.clientsMonths;
		}
		else if(eventID.contains("REQUEST")&&eventID.contains("OTW")){
			ServerHashDB = DEMSOttawaServer.otwDb;
			CustomerInfo = DEMSOttawaServer.otwCustomerInfo;
			clientsMonths = DEMSMontrealServer.clientsMonths;
		}

//		eventID=eventID.split("REQUEST")[0];
		int month = Integer.parseInt(eventID.split("REQUEST")[0].substring(6,8))-1;
		if (CustomerInfo.containsKey(customerID) && CustomerInfo.get(customerID) != null
				&& CustomerInfo.get(customerID).contains(eventID.split("REQUEST")[0])) {

			if (eventID.contains("REQUEST")||eventID.contains("MTL")&&customerID.contains("MTL")||eventID.contains("TOR")&&customerID.contains("TOR")||eventID.contains("OTW")&&customerID.contains("OTW")) {
				eventID=eventID.split("REQUEST")[0];
				for (String eType : ServerHashDB.keySet()) {
					for (String eID : ServerHashDB.get(eType).keySet()) {
						if (eID.equalsIgnoreCase(eventID)) {
							ArrayList<String> temp1 = CustomerInfo.get(customerID);
							temp1.remove(eventID);
							if (temp1.size() == 0)
								CustomerInfo.remove(customerID);
							else
								CustomerInfo.put(customerID, temp1);
							HashMap<String, Integer> temp = (HashMap<String, Integer>) ServerHashDB.get(eType).clone();
							temp.put(eID, temp.get(eID) + 1);
							ServerHashDB.put(eType, temp);
							logOperation("cancelEvent", eventID, "NA",customerID,"NA", "Customer has been removed from this event.");
							return "Customer has been removed from this event.";
						}
					}
				}
			} else {
				for (String eType : ServerHashDB.keySet()) {
					for (String eID : ServerHashDB.get(eType).keySet()) {
						if (eID.equalsIgnoreCase(eventID)) {
							HashMap<String, Integer> temp = (HashMap<String, Integer>) ServerHashDB.get(eType).clone();
							temp.put(eID, temp.get(eID) + 1);
							ServerHashDB.put(eType, temp);
						}
					}
				}
				String response=null;
				if(customerID.contains("TOR"))
					response = DEMSTorontoServer.UDPclient(customerID, eventID+ "REQUEST", "","cancel");
				if(customerID.contains("MTL"))
					response = DEMSMontrealServer.UDPclient(customerID, eventID+ "REQUEST", "","cancel");
				if(customerID.contains("OTW"))
					response = DEMSOttawaServer.UDPclient(customerID, eventID+ "REQUEST", "","cancel");

				if(response.contains("Customer has been removed from this event.")) {
					int newCurrRemoteEvents = clientsMonths.isEmpty() || clientsMonths.get(customerID) == null
							|| clientsMonths.get(customerID)[month] == 0 ? 0 : clientsMonths.get(customerID)[month] - 1;
					int[] updated = clientsMonths.isEmpty() || clientsMonths.get(customerID) == null ? new int[12]
							: clientsMonths.get(customerID);
					updated[month] = newCurrRemoteEvents;
					clientsMonths.put(customerID, updated);

					ArrayList<String> temp1 = CustomerInfo.get(customerID);
					temp1.remove(eventID);
					logOperation("cancelEvent", eventID, "NA",customerID,"NA", response);
					return response;
				}
				logOperation("cancelEvent", eventID, "NA",customerID,"NA", response);
				return response;
			}
		}
		logOperation("cancelEvent", eventID, "NA",customerID,"NA", "Customer is not registered in event corresponding to entered eventID and eventType.");
		return "Customer is not registered in event corresponding to entered eventID and eventType.";
	}
	
	@Override
	public  String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID,
			String oldEventType) {
		String res="";
		HashMap<String, ArrayList<String>> CustomerInfo = null;
		if(customerID.contains("TOR")) {
			CustomerInfo = DEMSTorontoServer.torCustomerInfo;

		}
		else if(customerID.contains("OTW")) {
			CustomerInfo = DEMSOttawaServer.otwCustomerInfo;

		}
		else if(customerID.contains("MTL")) {
			CustomerInfo = DEMSMontrealServer.mtlCustomerInfo;

		}

		if (CustomerInfo.containsKey(customerID) && CustomerInfo.get(customerID).contains(oldEventID)) {
			String book = bookEvent(customerID, newEventID, newEventType);
			if (book.equals("Event booked successfully.") || book.contains("Event booked successfully.")) {
				cancelEvent(customerID, oldEventID);
				res = "Event swapped successfully.";
			}
			else {
				res = "Events were not swapped since " + book;
			}
		}

		else{
			res= "Customer is not registered in entered event.";
		}
		logOperation("SwapEvent", newEventID, "NA",customerID,"NA", res);
		return  res;
	}
	
	private boolean isValid(String eventID, String eventType, int bookingCapacity) {
		return (eventType.equalsIgnoreCase("Conference") || eventType.equalsIgnoreCase("Seminar")
				|| eventType.equalsIgnoreCase("TradeShow")) 
				&& (eventID.length() == 10 && (eventID.substring(0, 3).equals("TOR") || eventID.substring(0, 3).equals("MTL")
						|| eventID.substring(0, 3).equals("OTW")) && (eventID.charAt(3) == 'M' || eventID.charAt(3) == 'A' || eventID.charAt(3) == 'E')) 
				&& (bookingCapacity > 0);
	}

	private boolean isValid( String eventID, String eventType) {
		return (eventType.equalsIgnoreCase("Conference") || eventType.equalsIgnoreCase("Seminar")
				|| eventType.equalsIgnoreCase("TradeShow")) 
				&& (eventID.length() == 10 && (eventID.substring(0, 3).equals("TOR") || eventID.substring(0, 3).equals("MTL")
						|| eventID.substring(0, 3).equals("OTW")) && (eventID.charAt(3) == 'M' || eventID.charAt(3) == 'A' || eventID.charAt(3) == 'E'));
	}
	private boolean isValid(String customerID) {
		return (!customerID.isEmpty() &&(customerID.substring(0, 3).equals("TOR") || customerID.substring(0, 3).equals("MTL")
						|| customerID.substring(0, 3).equals("OTW")) && customerID.charAt(3) == 'C' );
	}
	
	public void shutdown() {
		orb.shutdown(false);
	}

	public static void logOperation(String name, String eventID, String eventType,String customerID, String bookingCap, String status) {
		FileWriter fw=null;
		BufferedWriter bw = null;

		try {
			if(eventID.contains("TOR"))
				 fw = new FileWriter("TorontoLogs.txt", true);
			else if(eventID.contains("MTL"))
				 fw = new FileWriter("MontrealLogs.txt", true);
			else if(eventID.contains("OTW"))
				 fw = new FileWriter("OttawaLogs.txt", true);
			else if(name.contains("listEventAvailability")||name.contains("getBookingSchedule")){
				fw = new FileWriter("TorontoLogs.txt", true);
				bw = new BufferedWriter(fw);
				writer = new PrintWriter(bw);
				String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
				String log = "\n"+name + "Performed.\nTime: " + timeStamp
						+ "\nCustomerID: " + customerID+ "\nEventID: "
						+ eventID +  "\nEventType: " + eventType +  "\nBooking Capacity: " + bookingCap
						+"\nStatus: "+status ;
				writer.println(log);
				fw = new FileWriter("MontrealLogs.txt", true);
				bw = new BufferedWriter(fw);
				writer = new PrintWriter(bw);
				 timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
				 log = "\n"+name + "Performed.\nTime: " + timeStamp
						+ "\nCustomerID: " + customerID+ "\nEventID: "
						+ eventID +  "\nEventType: " + eventType +  "\nBooking Capacity: " + bookingCap
						+"\nStatus: "+status ;
				writer.println(log);
				fw = new FileWriter("OttawaLogs.txt", true);
				bw = new BufferedWriter(fw);
				writer = new PrintWriter(bw);
				 timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
				 log = "\n"+name + "Performed.\nTime: " + timeStamp
						+ "\nCustomerID: " + customerID+ "\nEventID: "
						+ eventID +  "\nEventType: " + eventType +  "\nBooking Capacity: " + bookingCap
						+"\nStatus: "+status ;
				writer.println(log);
			}

			 bw = new BufferedWriter(fw);
			writer = new PrintWriter(bw);
		} catch (IOException e) {
			e.printStackTrace();
		}String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
		String log = "\n"+name + "Performed.\nTime: " + timeStamp
				+ "\nCustomerID: " + customerID+ "\nEventID: "
				+ eventID +  "\nEventType: " + eventType +  "\nBooking Capacity: " + bookingCap
				+"\nStatus: "+status ;
		writer.println(log);
		writer.close();

	}



}
