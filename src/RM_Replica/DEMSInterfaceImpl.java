package RM_Replica;
import org.omg.CORBA.ORB;
import DEMSInterfaceApp.DEMSInterfacePOA;

public class DEMSInterfaceImpl extends DEMSInterfacePOA {
	private ORB orb;

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	@Override
	public  String addEvent(String eventID, String eventType, String bookingCapacity) {
		String res = "Invalid input";
		if (isValid(eventID, eventType, Integer.parseInt(bookingCapacity))) {
			if (eventID.contains("TOR")) {
				res = DEMSTorontoServer.addEvent(eventID, eventType, bookingCapacity);
				DEMSTorontoServer.logOperation("addEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("MTL")) {
				res = DEMSMontrealServer.addEvent(eventID, eventType, bookingCapacity);
				DEMSMontrealServer.logOperation("addEvent", eventID, eventType, "NA","NA",res);
			} else if (eventID.contains("OTW")) {
				res = DEMSOttawaServer.addEvent(eventID, eventType, bookingCapacity);
				DEMSOttawaServer.logOperation("addEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}

	@Override
	public  String removeEvent(String eventID, String eventType) {
		String res = "Invalid input";
		if(isValid(eventID, eventType)) {
			if (eventID.contains("TOR")) {
				res = DEMSTorontoServer.removeEvent(eventID, eventType);
				DEMSTorontoServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("MTL")) {
				res = DEMSMontrealServer.removeEvent(eventID, eventType);
				DEMSMontrealServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			} else if (eventID.contains("OTW")) {
				res = DEMSOttawaServer.removeEvent(eventID, eventType);
				DEMSOttawaServer.logOperation("removeEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}
	
	@Override
	public  String listEventAvailability(String eventType) {
		String res = "Invalid Input";
		if(eventType.contains("TOR")) {
			System.out.println("request --> TOR server");
			res = DEMSTorontoServer.listEventAvailability(eventType.split(",")[0]);
			DEMSTorontoServer.logOperation("listEventAvailability", "NA", eventType,"NA","NA", "Succeeded");
		}
		else if(eventType.contains("MTL")) {
			System.out.println("request --> MTL server");
			res = DEMSMontrealServer.listEventAvailability(eventType.split(",")[0]);
			DEMSMontrealServer.logOperation("listEventAvailability", "NA", eventType,"NA","NA", "Succeeded");
		}
		else if(eventType.contains("OTW")) {
			System.out.println("request --> OTW server");
			res = DEMSOttawaServer.listEventAvailability(eventType.split(",")[0]);
			DEMSOttawaServer.logOperation("listEventAvailability", "NA", eventType,"NA","NA", "Succeeded");
		}
		return res;
	}

	@Override
	public  String bookEvent(String customerID, String eventID, String eventType) {
		String res = "Invalid input";
		if( !isValid(customerID))
			return "Invalid Customer ID!";
		if(isValid(eventID, eventType)) {
			if(customerID.contains("TOR")) {
				res = DEMSTorontoServer.bookEvent(customerID, eventID, eventType);
				DEMSTorontoServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
			else if(customerID.contains("OTW")) {
				res = DEMSOttawaServer.bookEvent(customerID, eventID, eventType);
				DEMSOttawaServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
			else if(customerID.contains("MTL")) {
				res = DEMSMontrealServer.bookEvent(customerID, eventID, eventType);
				DEMSMontrealServer.logOperation("bookEvent", eventID, eventType,"NA","NA", res);
			}
		}
		return res;
	}

	@Override
	public  String getBookingSchedule(String customerID) {
		if(customerID.contains("TOR")) {
			DEMSTorontoServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
			return DEMSTorontoServer.getBookingSchedule(customerID);
		}
		else if(customerID.contains("OTW")) {
			DEMSOttawaServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
			return DEMSOttawaServer.getBookingSchedule(customerID);
		}
		else if(customerID.contains("MTL")) {
			DEMSMontrealServer.logOperation("getBookingSchedule", "NA", "NA",customerID,"NA", "Succeeded");
			return DEMSMontrealServer.getBookingSchedule(customerID);
		}
		return "Invalid Input";

	}

	@Override
	public  String cancelEvent(String customerID, String eventID) {
		String res = "Invalid input";
		if(customerID.contains("TOR")) {
			res = DEMSTorontoServer.cancelEvent(customerID, eventID);
			DEMSTorontoServer.logOperation("cancelEvent", eventID, "NA",customerID,"NA", res);
		}
		else if(customerID.contains("OTW")) {
			res = DEMSOttawaServer.cancelEvent(customerID, eventID);
			DEMSOttawaServer.logOperation("cancelEvent",eventID, "NA",customerID,"NA", res);
		}
		else if(customerID.contains("MTL")){
			res = DEMSMontrealServer.cancelEvent(customerID, eventID);
			DEMSMontrealServer.logOperation("cancelEvent", eventID, "NA",customerID,"NA", res);
		}
		return res;
	}
	
	@Override
	public  String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID,
			String oldEventType) {
		String res = "Invalid input";
		if(customerID.contains("TOR")) {
			res = DEMSTorontoServer.swapEvent(customerID, newEventID, newEventType, oldEventID, oldEventType);
			DEMSTorontoServer.logOperation("SwapEvent", newEventID, "NA",customerID,"NA", res);
		}
		else if(customerID.contains("OTW")) {
			res = DEMSOttawaServer.swapEvent(customerID, newEventID, newEventType, oldEventID, oldEventType);
			DEMSOttawaServer.logOperation("SwapEvent",newEventID, "NA",customerID,"NA", res);
		}
		else if(customerID.contains("MTL")){
			res = DEMSMontrealServer.swapEvent(customerID, newEventID, newEventType, oldEventID, oldEventType);
			DEMSMontrealServer.logOperation("SwapEvent", newEventID, "NA",customerID,"NA", res);
		}
		return res;
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
	
}
