package DEMSInterfaceApp;

/**
* DEMSInterfaceApp/DEMSInterfaceHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Users/khanna/Desktop/Projects/eclipse-workspace/Replica1/src/DEMSInterface.idl
* Thursday, August 1, 2019 5:01:13 o'clock PM EDT
*/

public final class DEMSInterfaceHolder implements org.omg.CORBA.portable.Streamable
{
  public DEMSInterfaceApp.DEMSInterface value = null;

  public DEMSInterfaceHolder ()
  {
  }

  public DEMSInterfaceHolder (DEMSInterfaceApp.DEMSInterface initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = DEMSInterfaceApp.DEMSInterfaceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    DEMSInterfaceApp.DEMSInterfaceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return DEMSInterfaceApp.DEMSInterfaceHelper.type ();
  }

}