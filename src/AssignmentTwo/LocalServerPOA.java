package AssignmentTwo;


/**
* AssignmentTwo/LocalServerPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AssignmentTwo.idl
* Tuesday, 31 March 2020 19:26:32 o'clock BST
*/

public abstract class LocalServerPOA extends org.omg.PortableServer.Servant
 implements AssignmentTwo.LocalServerOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("_get_connected_servers", new java.lang.Integer (0));
    _methods.put ("get_centre_info", new java.lang.Integer (1));
    _methods.put ("set_info", new java.lang.Integer (2));
    _methods.put ("all_readings", new java.lang.Integer (3));
    _methods.put ("get_readings", new java.lang.Integer (4));
    _methods.put ("get_current_readings", new java.lang.Integer (5));
    _methods.put ("alerts", new java.lang.Integer (6));
    _methods.put ("register_monitoring_station", new java.lang.Integer (7));
    _methods.put ("send_alert", new java.lang.Integer (8));
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
       case 0:  // AssignmentTwo/LocalServer/_get_connected_servers
       {
         AssignmentTwo.StationDetails $result[] = null;
         $result = this.connected_servers ();
         out = $rh.createReply();
         AssignmentTwo.stationListHelper.write (out, $result);
         break;
       }

       case 1:  // AssignmentTwo/LocalServer/get_centre_info
       {
         AssignmentTwo.ServerDetails $result = null;
         $result = this.get_centre_info ();
         out = $rh.createReply();
         AssignmentTwo.ServerDetailsHelper.write (out, $result);
         break;
       }

       case 2:  // AssignmentTwo/LocalServer/set_info
       {
         AssignmentTwo.ServerDetails info = AssignmentTwo.ServerDetailsHelper.read (in);
         this.set_info (info);
         out = $rh.createReply();
         break;
       }

       case 3:  // AssignmentTwo/LocalServer/all_readings
       {
         AssignmentTwo.Reading $result[] = null;
         $result = this.all_readings ();
         out = $rh.createReply();
         AssignmentTwo.readingListHelper.write (out, $result);
         break;
       }

       case 4:  // AssignmentTwo/LocalServer/get_readings
       {
         String station_name = in.read_string ();
         AssignmentTwo.Reading $result[] = null;
         $result = this.get_readings (station_name);
         out = $rh.createReply();
         AssignmentTwo.readingListHelper.write (out, $result);
         break;
       }

       case 5:  // AssignmentTwo/LocalServer/get_current_readings
       {
         AssignmentTwo.Reading $result[] = null;
         $result = this.get_current_readings ();
         out = $rh.createReply();
         AssignmentTwo.readingListHelper.write (out, $result);
         break;
       }

       case 6:  // AssignmentTwo/LocalServer/alerts
       {
         AssignmentTwo.Reading $result[] = null;
         $result = this.alerts ();
         out = $rh.createReply();
         AssignmentTwo.readingListHelper.write (out, $result);
         break;
       }

       case 7:  // AssignmentTwo/LocalServer/register_monitoring_station
       {
         AssignmentTwo.StationDetails info = AssignmentTwo.StationDetailsHelper.read (in);
         this.register_monitoring_station (info);
         out = $rh.createReply();
         break;
       }

       case 8:  // AssignmentTwo/LocalServer/send_alert
       {
         AssignmentTwo.Reading reading = AssignmentTwo.ReadingHelper.read (in);
         this.send_alert (reading);
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:AssignmentTwo/LocalServer:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public LocalServer _this() 
  {
    return LocalServerHelper.narrow(
    super._this_object());
  }

  public LocalServer _this(org.omg.CORBA.ORB orb) 
  {
    return LocalServerHelper.narrow(
    super._this_object(orb));
  }


} // class LocalServerPOA
