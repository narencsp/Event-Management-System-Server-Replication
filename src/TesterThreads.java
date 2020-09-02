import java.rmi.Remote;
import java.rmi.RemoteException;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import DEMSInterfaceApp.DEMSInterface;
import DEMSInterfaceApp.DEMSInterfaceHelper;

public class TesterThreads {

	public static void perform1(DEMSInterface callingObject) throws RemoteException, InterruptedException {
		String student_ID = "MTLC1231";
		String eventID = "TORM110519";
		String eventType = "Seminars";
		System.out.println(callingObject.bookEvent(student_ID, eventID, eventType) + "---T1 - MTLC1231");
		eventID = "OTWM110519";
		eventType = "Seminars";
		System.out.println(callingObject.bookEvent(student_ID, eventID, eventType) + "---T1 - MTLC1231");
		String oldEventID = "TORM110519";
		String oldEventType = "Seminars";
		String newEventID = "OTWA120519";
		String newEventType = "Trade Shows";
		System.out.println(
				callingObject.swapEvent(student_ID, newEventID, newEventType, oldEventID, oldEventType) + "---T1 - MTLC1231");

		oldEventID = "OTWM110519";
		oldEventType = "Seminars";
		newEventID = "TORM100519";
		newEventType = "Conferences";

		System.out.println(
				callingObject.swapEvent(student_ID, newEventID, newEventType, oldEventID, oldEventType) + "---T1 - MTLC1231");
		System.out.println(callingObject.getBookingSchedule(student_ID) + "---T1 - MTLC1231");

	}

//	public static void perform2(DEMSInterface callingObject) throws RemoteException, InterruptedException {
//		String student_ID = "MTLC1231";
//		String eventID = "OTWM110519";
//		String eventType = "Seminars";
//		System.out.println(callingObject.bookEvent(student_ID, eventID, eventType) + "---T2");
//	}

	public static void perform5(DEMSInterface callingObject) throws RemoteException, InterruptedException {
		String student_ID = "TORC1231";
		String eventID = "MTLM100519";
		String eventType = "Conferences";
		System.out.println(callingObject.bookEvent(student_ID, eventID, eventType) + "---T5 - TORC1231");
		eventID = "OTWM100519";
		eventType = "Conferences";
		System.out.println(callingObject.bookEvent(student_ID, eventID, eventType) + "---T5 - TORC1231");
		String oldEventID = "MTLM100519";
		String oldEventType = "Conferences";
		String newEventID = "OTWA120519";
		String newEventType = "Trade Shows";

		System.out.println(
				callingObject.swapEvent(student_ID, newEventID, newEventType, oldEventID, oldEventType) + "---T5 - TORC1231");
		oldEventID = "OTWM100519";
		oldEventType = "Conferences";
		newEventID = "OTWE120519";
		newEventType = "Trade Shows";

		Thread.sleep(1000);
		System.out.println(
				callingObject.swapEvent(student_ID, newEventID, newEventType, oldEventID, oldEventType) + "---T5 - TORC1231");
		System.out.println(callingObject.getBookingSchedule(student_ID) + "---T5 - TORC1231");
	}

	public static void perform6(DEMSInterface callingObject) throws RemoteException, InterruptedException{
		//Thread.sleep(2000);
		String student_ID = "OTWC7890";
		String eventID1 = "MTLE100519";
		String eventID2 = "MTLE110519";
		String eventID3 = "MTLA110519";
		String eventID4 = "MTLM110519";
		String eventType1="Conferences";
		String eventType2="Seminars";
		String eventType3="Seminars";
		String eventType4="Seminars";

		System.out.println(callingObject.bookEvent(student_ID, eventID1, eventType1) + "---T6 - OTWC7890 -MTLE100519 -Conferences");
		//Thread.sleep(2000);
		System.out.println(callingObject.bookEvent(student_ID, eventID2, eventType2) + "---T6 - OTWC7890 -MTLE110519 -Seminars");

		System.out.println(callingObject.bookEvent(student_ID, eventID3, eventType3) + "---T6 - OTWC7890 -MTLA110519 -Seminars");

		System.out.println(callingObject.bookEvent(student_ID, eventID4, eventType4) + "---T6 - OTWC7890 -MTLM110519 -Seminars");

	}


//	public static void perform6(DEMSInterface callingObject) throws RemoteException, InterruptedException {
//		Thread.sleep(1500);
//		
//		String student_ID = "TORC1231";
//		System.out.println(callingObject.cancelEvent(student_ID, "OTWM110519") + "---T6 - Resetting");
//		System.out.println(callingObject.cancelEvent(student_ID, "MTLM100519") + "---T6 - Resetting");
//		System.out.println(callingObject.cancelEvent(student_ID, "OTWA120519") + "---T6 - Resetting");
//		System.out.println(callingObject.cancelEvent(student_ID, "OTWM100519") + "---T6 - Resetting");
//		
//		student_ID = "MTLC1231";
//		System.out.println(callingObject.cancelEvent(student_ID, "TORM100519") + "---T6 - Resetting");
//		System.out.println(callingObject.cancelEvent(student_ID, "OTWM110519") + "---T6 - Resetting");
//		System.out.println(callingObject.cancelEvent(student_ID, "OTWA120519") + "---T6 - Resetting");
//		System.out.println(callingObject.cancelEvent(student_ID, "TORM110519") + "---T6 - Resetting");
//
//	}

//	both correct bookings want to swap for the same event with just 1 capacity.
//	public static void perform7(DEMSInterface callingObject) throws RemoteException, InterruptedException {
//		String oldEventID = "TORM110519";
//		String oldEventType = "Seminars";
//		String newEventID = "OTWA120519";
//		String newEventType = "Trade Shows";
//		String student_ID = "MTLC1231";
//
//		Thread.sleep(500);
//		System.out.println(
//				callingObject.swapEvent(student_ID, newEventID, newEventType, oldEventID, oldEventType) + "---T7");
//
//	}

//	public static void perform8(DEMSInterface callingObject) throws RemoteException, InterruptedException {
//		String oldEventID = "MTLM100519";
//		String oldEventType = "Conferences";
//		String newEventID = "OTWA120519";
//		String newEventType = "Trade Shows";
//		String student_ID = "TORC1231";
//
//		Thread.sleep(500);
//		System.out.println(
//				callingObject.swapEvent(student_ID, newEventID, newEventType, oldEventID, oldEventType) + "---T8");
//
//	}

//	swapping for different events
//	public static void perform9(DEMSInterface callingObject) throws RemoteException, InterruptedException {
//		String oldEventID = "OTWM110519";
//		String oldEventType = "Seminars";
//		String newEventID = "TORM100519";
//		String newEventType = "Conferences";
//		String student_ID = "MTLC1231";
//
//		Thread.sleep(1000);
//		System.out.println(
//				callingObject.swapEvent(student_ID, newEventID, newEventType, oldEventID, oldEventType) + "---T9");
//
//	}

//	public static void perform10(DEMSInterface callingObject) throws RemoteException, InterruptedException {
//		String oldEventID = "OTWM100519";
//		String oldEventType = "Conferences";
//		String newEventID = "OTWE120519";
//		String newEventType = "Trade Shows";
//		String student_ID = "TORC1231";
//
//		Thread.sleep(1000);
//		System.out.println(
//				callingObject.swapEvent(student_ID, newEventID, newEventType, oldEventID, oldEventType) + "---T10");
//
//	}

//	public static void perform11(DEMSInterface callingObject) throws RemoteException, InterruptedException {
//		String student_ID = "MTLC1231";
//
//		Thread.sleep(5000);
//		System.out.println(callingObject.getBookingSchedule(student_ID) + "---T11");
//
//	}
//
//	public static void perform12(DEMSInterface callingObject) throws RemoteException, InterruptedException {
//		String student_ID = "TORC1231";
//
//		Thread.sleep(5000);
//		System.out.println(callingObject.getBookingSchedule(student_ID) + "---T12");
//
//	}

	public static void main(String args[]) throws Exception {
		ORB orb = ORB.init(args, null);
		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

		DEMSInterface montreal = (DEMSInterface) DEMSInterfaceHelper.narrow(ncRef.resolve_str("montreal"));
		DEMSInterface toronto = (DEMSInterface) DEMSInterfaceHelper.narrow(ncRef.resolve_str("toronto"));
		DEMSInterface ottawa = (DEMSInterface) DEMSInterfaceHelper.narrow(ncRef.resolve_str("ottawa"));

		Runnable task = () -> {

			try {
				perform1(montreal);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};

//		Runnable task2 = () -> {
//
//			try {
//				perform2(montreal);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		};

		new Thread(task).start();
//		new Thread(task2).start();

		Runnable task5 = () -> {

			try {
				perform5(toronto);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};

//		Runnable task6 = () -> {
//
//			try {
//				perform6(toronto);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		};

		new Thread(task5).start();
		Runnable task6 = () -> {

			try {
				perform6(ottawa);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
		new Thread(task6).start();
//		new Thread(task6).start();
//
//		Runnable task7 = () -> {
//
//			try {
//				perform7(montreal);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		};
//
//		Runnable task8 = () -> {
//
//			try {
//				perform8(toronto);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		};
//
//		new Thread(task7).start();
//		new Thread(task8).start();
//
//		Runnable task9 = () -> {
//
//			try {
//				perform9(montreal);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		};
//
//		Runnable task10 = () -> {
//
//			try {
//				perform10(toronto);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		};
//
//		new Thread(task9).start();
//		new Thread(task10).start();
//
//		Runnable task11 = () -> {
//
//			try {
//				perform11(montreal);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		};
//
//		Runnable task12 = () -> {
//
//			try {
//				perform12(toronto);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		};
//
//		new Thread(task11).start();
//		new Thread(task12).start();
//
	}

}