package AssignmentTwo;


/**
* AssignmentTwo/stationListHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AssignmentTwo.idl
* Monday, 30 March 2020 15:54:25 o'clock BST
*/

abstract public class stationListHelper
{
  private static String  _id = "IDL:AssignmentTwo/stationList:1.0";

  public static void insert (org.omg.CORBA.Any a, AssignmentTwo.StationDetails[] that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static AssignmentTwo.StationDetails[] extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = AssignmentTwo.StationDetailsHelper.type ();
      __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
      __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (AssignmentTwo.stationListHelper.id (), "stationList", __typeCode);
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static AssignmentTwo.StationDetails[] read (org.omg.CORBA.portable.InputStream istream)
  {
    AssignmentTwo.StationDetails value[] = null;
    int _len0 = istream.read_long ();
    value = new AssignmentTwo.StationDetails[_len0];
    for (int _o1 = 0;_o1 < value.length; ++_o1)
      value[_o1] = AssignmentTwo.StationDetailsHelper.read (istream);
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, AssignmentTwo.StationDetails[] value)
  {
    ostream.write_long (value.length);
    for (int _i0 = 0;_i0 < value.length; ++_i0)
      AssignmentTwo.StationDetailsHelper.write (ostream, value[_i0]);
  }

}