package AssignmentTwo;

/**
* AssignmentTwo/MonitoringStationHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AssignmentTwo.idl
* Monday, 30 March 2020 15:54:25 o'clock BST
*/

public final class MonitoringStationHolder implements org.omg.CORBA.portable.Streamable
{
  public AssignmentTwo.MonitoringStation value = null;

  public MonitoringStationHolder ()
  {
  }

  public MonitoringStationHolder (AssignmentTwo.MonitoringStation initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = AssignmentTwo.MonitoringStationHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    AssignmentTwo.MonitoringStationHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return AssignmentTwo.MonitoringStationHelper.type ();
  }

}