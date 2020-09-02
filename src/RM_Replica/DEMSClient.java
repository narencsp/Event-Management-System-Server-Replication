package RM_Replica;
import java.io.*;
import java.rmi.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Scanner;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import DEMSInterfaceApp.DEMSInterface;
import DEMSInterfaceApp.DEMSInterfaceHelper;

public class DEMSClient {

	String userID, city, registryURL;
	String res;
	int RMIPort;
	static DEMSInterface callingObj;
	Scanner sc;
	PrintWriter writer;
	static ORB orb;
	static HashSet<String> eventTypes = new HashSet<String>();
	
	public DEMSClient() {
		this.sc = new Scanner(System.in);
	}

	public static void main(String args[]) throws InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName, IOException, NotBoundException {
		orb = ORB.init(args, null);
		DEMSClient obj = new DEMSClient();
		obj.getID(orb);
		obj.init(orb);
	}

	private void getID(ORB orb) throws IOException, NotBoundException, InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

		System.out.println("\nLOGIN");
		System.out.print("Enter your ID:");
		this.userID = this.sc.nextLine();
		
		if(this.userID.startsWith("MTL")) {
			this.city = "Montreal";
			callingObj = (DEMSInterface) DEMSInterfaceHelper.narrow(ncRef.resolve_str("montreal"));
		}
		else if(this.userID.startsWith("OTW")) {
			this.city = "Ottawa";
			callingObj = (DEMSInterface) DEMSInterfaceHelper.narrow(ncRef.resolve_str("ottawa"));
		}
		else if(this.userID.startsWith("TOR")) {
			this.city = "Toronto";
			callingObj = (DEMSInterface) DEMSInterfaceHelper.narrow(ncRef.resolve_str("toronto"));
		}
		else {
			System.out.println("Invalid Input");
			getID(orb);
			init(orb);
		}	
	}

	private void init(ORB orb) throws NotBoundException, IOException, InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
		eventTypes.add("TradeShow");
		eventTypes.add("Conference");
		eventTypes.add("Seminar");
		
		if (this.userID.toUpperCase().charAt(3) == 'M') {
			System.out.print("\nYou are logged in Manager:" + city);
			manager(orb);
			
		} else if (this.userID.toUpperCase().charAt(3) == 'C') {
			System.out.print("\nYou are logged in as client:" + city);
			client(orb);
			
		} else {
			System.out.println("Invalid ID");
			getID(orb);
			init(orb);
			return;
		}
	}

	private void client(ORB orb) throws IOException, NotBoundException, InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
		boolean menu = true;
		while (menu) {
			System.out.print("\n\n1.Book Event \n2.Get Booking Schedule \n3.Cancel Event \n4.Swap Event \n5.Log out \nEnter Choice:");
			String ch;
			ch = this.sc.nextLine();
			switch (ch) {
			case "1":
				bookEvent(true);
				break;
			case "2":
				getBookingSchedule(true);
				break;
			case "3":
				cancelEvent(true);
				break;
			case "4":
				swapEvent(true);
				break;
			case "5":
				menu = false;
				break;
			default:
				System.out.println("Invalid choice!");
			}
		}
		getID(orb);
		init(orb);
	}

	private void manager(ORB orb) throws NotBoundException, IOException, InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
		boolean menu = true;
		while (menu) {
			System.out.print(
					"\n\n1.Add Event \n2.Remove Event \n3.List Event Availablity \n4.Book Event \n5.Swap Event \n6.Get Booking Schedule \n7.Cancel Event \n8.Log out \nEnter Choice:");
			String ch;
			ch = this.sc.nextLine();
			switch (ch) {
			case "1":
				addEvent();
				break;
			case "2":
				removeEvent();
				break;
			case "3":
				listEventAvailability();
				break;
			case "4":
				bookEvent(false);
				break;
			case "5":
				swapEvent(false);
				break;
			case "6":
				getBookingSchedule(false);
				break;
			case "7":
				cancelEvent(false);
				break;
			case "8":
				menu = false;
				break;
			default:
				System.out.println("Invalid choice!");
			}
		}
		getID(orb);
		init(orb); 
	}
	
	private void swapEvent(boolean check) {
		
		String newEventID, newEventType, oldEventID, oldEventType, customerID;
		System.out.println("\nEnter event details:");
		if(!check) {
			System.out.print("CustomerID:");
			customerID = this.sc.nextLine();
		}
		else {
			customerID = this.userID;
		}
		System.out.print("Old event ID:");
		oldEventID = this.sc.nextLine();
		System.out.print("Old Event Type:");
		oldEventType = this.sc.nextLine();
		System.out.print("New event ID:");
		newEventID = this.sc.nextLine();
		System.out.print("New Event Type:");
		newEventType = this.sc.nextLine();
		
		System.out.println(DEMSClient.callingObj.swapEvent(customerID, newEventID, newEventType, oldEventID, oldEventType));
		logOperation("SwapEvent", newEventID, newEventType, this.userID, this.res);
	}
	
	private void bookEvent(boolean check) {
		
		String eventID, eventType, customerID;
		System.out.println("\nEnter event details:");
		if(!check) {
			System.out.print("CustomerID:");
			customerID = this.sc.nextLine(); // fix null
		}
		else {
			customerID = this.userID;
		}
		System.out.print("Event ID:");
		eventID = this.sc.nextLine();
		System.out.print("Event Type:");
		eventType = this.sc.nextLine();
		
		this.res = DEMSClient.callingObj.bookEvent(customerID, eventID, eventType);
		logOperation("bookEvent", eventID, eventType, this.userID, this.res);
		System.out.println(this.res);
	}

	private void cancelEvent(boolean check) {
		String eventID, customerID;
		System.out.println("\nEnter event details:");
		System.out.print("Event ID:");
		eventID = this.sc.nextLine();
		if(!check) {
			System.out.print("CustomerID:");
			customerID = this.sc.nextLine();
		}
		else {
			customerID = this.userID;
		}
		this.res = DEMSClient.callingObj.cancelEvent(customerID, eventID);
		logOperation("cancelEvent", eventID, "NA", this.userID, this.res);
		System.out.println(this.res);
	}

	private void getBookingSchedule(boolean check) {
		String customerID;
		if(!check) {
			System.out.print("CustomerID:");
			customerID = this.sc.nextLine();
		}
		else {
			customerID = this.userID;
		}
		System.out.println(DEMSClient.callingObj.getBookingSchedule(customerID));
		logOperation("getBookingSchedule", "NA", "NA", this.userID, "Succeeded");
	}

	public void removeEvent() throws NotBoundException, IOException, InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
		String eventID, eventType;
		System.out.print("\nEvent ID to remove:");
		eventID = this.sc.nextLine();
		if(!eventID.substring(0,3).equals(this.userID.substring(0,3))) {
			System.out.println("Event Manager of this city cannot remove event in the another city");
			manager(orb);
		}
		System.out.print("Event Type:");
		eventType = this.sc.nextLine();
		this.res = DEMSClient.callingObj.removeEvent(eventID, eventType);
		logOperation("removeEvent", eventID, eventType, this.userID, this.res);
		System.out.println(this.res);
	}

	public void addEvent() throws IOException, NotBoundException, InvalidName, NotFound, CannotProceed, org.omg.CosNaming.NamingContextPackage.InvalidName {
		String eventID, eventType, bookingCapacity;
		System.out.println("\nEnter event details:");
		System.out.print("Event ID:");
		eventID = this.sc.nextLine();
		if(eventID.isEmpty()) {
			System.out.println("\nEvent ID cannot be empty!");
			addEvent();
			return;
		}
		if(!eventID.substring(0,3).equals(this.userID.substring(0,3))) {
			System.out.println("Sorry you cannot add the event for other cities.");
			manager(orb);
		}
		System.out.print("Event Type:");
		eventType = this.sc.nextLine();
		System.out.print("Booking Capacity:");
		try {
			bookingCapacity = this.sc.nextLine();
		} catch(NumberFormatException e) {
			System.out.println("Enter a number for the booking capacity.");
			addEvent();
			return;
		}
		this.res = DEMSClient.callingObj.addEvent(eventID, eventType, bookingCapacity);
		logOperation("addEvent", eventID, eventType, this.userID, this.res);
		System.out.println(this.res);
	}
	
	public void listEventAvailability() {
		String eventType;
		System.out.print("\nEvent Type:");
		eventType = this.sc.nextLine();
		if(!eventType.equals("Conference") && !eventType.equals("Seminar")
				&& !eventType.equals("TradeShow")) {
			System.out.println("Invalid input!");
			listEventAvailability();
			return;
		}
		System.out.println(DEMSClient.callingObj.listEventAvailability(eventType+","+this.userID));
		logOperation("listEventAvailability", "NA", eventType, this.userID, "Succeeded");
	}

	public void logOperation(String name, String eventID, String eventType, String customerID, String status) {
		try {
			FileWriter fw = new FileWriter("ClientLogs.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			writer = new PrintWriter(bw);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
		String log = "\n" + name + "Performed.\nTime: " + timeStamp + "\nCustomerID: " + customerID + "\nEventID: "
				+ eventID + "\nEventType: " + eventType + "\nStatus: " + status;
		writer.println(log);
		writer.close();
	}

}
