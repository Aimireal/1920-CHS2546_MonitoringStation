package ClientAndServer;

/**
* ClientAndServer/MonitoringSystemHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from relay.idl
* Friday, 27 March 2020 19:21:11 o'clock GMT
*/

public final class MonitoringSystemHolder implements org.omg.CORBA.portable.Streamable
{
  public ClientAndServer.MonitoringSystem value = null;

  public MonitoringSystemHolder ()
  {
  }

  public MonitoringSystemHolder (ClientAndServer.MonitoringSystem initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ClientAndServer.MonitoringSystemHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ClientAndServer.MonitoringSystemHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ClientAndServer.MonitoringSystemHelper.type ();
  }

}
