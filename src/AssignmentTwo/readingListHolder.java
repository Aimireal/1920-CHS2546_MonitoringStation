package AssignmentTwo;


/**
* AssignmentTwo/readingListHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AssignmentTwo.idl
* Tuesday, 31 March 2020 19:26:32 o'clock BST
*/

public final class readingListHolder implements org.omg.CORBA.portable.Streamable
{
  public AssignmentTwo.Reading value[] = null;

  public readingListHolder ()
  {
  }

  public readingListHolder (AssignmentTwo.Reading[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = AssignmentTwo.readingListHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    AssignmentTwo.readingListHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return AssignmentTwo.readingListHelper.type ();
  }

}
