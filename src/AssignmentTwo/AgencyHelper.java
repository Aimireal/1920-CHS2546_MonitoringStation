package AssignmentTwo;


/**
* AssignmentTwo/AgencyHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AssignmentTwo.idl
* Monday, 13 April 2020 17:15:40 o'clock BST
*/

abstract public class AgencyHelper
{
  private static String  _id = "IDL:AssignmentTwo/Agency:1.0";

  public static void insert (org.omg.CORBA.Any a, AssignmentTwo.Agency that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static AssignmentTwo.Agency extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  private static boolean __active = false;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      synchronized (org.omg.CORBA.TypeCode.class)
      {
        if (__typeCode == null)
        {
          if (__active)
          {
            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
          }
          __active = true;
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [3];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[0] = new org.omg.CORBA.StructMember (
            "agency_name",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[1] = new org.omg.CORBA.StructMember (
            "agency_region",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[2] = new org.omg.CORBA.StructMember (
            "agency_contact",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (AssignmentTwo.AgencyHelper.id (), "Agency", _members0);
          __active = false;
        }
      }
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static AssignmentTwo.Agency read (org.omg.CORBA.portable.InputStream istream)
  {
    AssignmentTwo.Agency value = new AssignmentTwo.Agency ();
    value.agency_name = istream.read_string ();
    value.agency_region = istream.read_string ();
    value.agency_contact = istream.read_string ();
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, AssignmentTwo.Agency value)
  {
    ostream.write_string (value.agency_name);
    ostream.write_string (value.agency_region);
    ostream.write_string (value.agency_contact);
  }

}
