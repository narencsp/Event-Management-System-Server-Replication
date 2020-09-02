package DEMSInterfaceApp;


/**
* DEMSInterfaceApp/DEMSInterfacePOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Users/khanna/Desktop/Projects/eclipse-workspace/Replica1/src/DEMSInterface.idl
* Thursday, August 1, 2019 5:01:13 o'clock PM EDT
*/

public abstract class DEMSInterfacePOA extends org.omg.PortableServer.Servant
 implements DEMSInterfaceApp.DEMSInterfaceOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("addEvent", new java.lang.Integer (0));
    _methods.put ("removeEvent", new java.lang.Integer (1));
    _methods.put ("listEventAvailability", new java.lang.Integer (2));
    _methods.put ("bookEvent", new java.lang.Integer (3));
    _methods.put ("getBookingSchedule", new java.lang.Integer (4));
    _methods.put ("cancelEvent", new java.lang.Integer (5));
    _methods.put ("swapEvent", new java.lang.Integer (6));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // DEMSInterfaceApp/DEMSInterface/addEvent
       {
         String eventID = in.read_string ();
         String eventType = in.read_string ();
         String bookingCapacity = in.read_string ();
         String $result = null;
         $result = this.addEvent (eventID, eventType, bookingCapacity);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }


  // change integer to string in bookcapacity
       case 1:  // DEMSInterfaceApp/DEMSInterface/removeEvent
       {
         String eventID = in.read_string ();
         String eventType = in.read_string ();
         String $result = null;
         $result = this.removeEvent (eventID, eventType);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 2:  // DEMSInterfaceApp/DEMSInterface/listEventAvailability
       {
         String eventType = in.read_string ();
         String $result = null;
         $result = this.listEventAvailability (eventType);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // DEMSInterfaceApp/DEMSInterface/bookEvent
       {
         String customerID = in.read_string ();
         String eventID = in.read_string ();
         String eventType = in.read_string ();
         String $result = null;
         $result = this.bookEvent (customerID, eventID, eventType);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 4:  // DEMSInterfaceApp/DEMSInterface/getBookingSchedule
       {
         String customerID = in.read_string ();
         String $result = null;
         $result = this.getBookingSchedule (customerID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // DEMSInterfaceApp/DEMSInterface/cancelEvent
       {
         String customerID = in.read_string ();
         String eventID = in.read_string ();
         String $result = null;
         $result = this.cancelEvent (customerID, eventID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 6:  // DEMSInterfaceApp/DEMSInterface/swapEvent
       {
         String customerID = in.read_string ();
         String newEventID = in.read_string ();
         String newEventType = in.read_string ();
         String oldEventID = in.read_string ();
         String oldEventType = in.read_string ();
         String $result = null;
         $result = this.swapEvent (customerID, newEventID, newEventType, oldEventID, oldEventType);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:DEMSInterfaceApp/DEMSInterface:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public DEMSInterface _this() 
  {
    return DEMSInterfaceHelper.narrow(
    super._this_object());
  }

  public DEMSInterface _this(org.omg.CORBA.ORB orb) 
  {
    return DEMSInterfaceHelper.narrow(
    super._this_object(orb));
  }


} // class DEMSInterfacePOA
