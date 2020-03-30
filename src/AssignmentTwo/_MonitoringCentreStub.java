package AssignmentTwo;


/**
* AssignmentTwo/_MonitoringCentreStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AssignmentTwo.idl
* Monday, 30 March 2020 15:54:25 o'clock BST
*/

public class _MonitoringCentreStub extends org.omg.CORBA.portable.ObjectImpl implements AssignmentTwo.MonitoringCentre
{

  public AssignmentTwo.Reading[] all_readings ()
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("all_readings", true);
                $in = _invoke ($out);
                AssignmentTwo.Reading $result[] = AssignmentTwo.readingListHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return all_readings (        );
            } finally {
                _releaseReply ($in);
            }
  } // all_readings

  public AssignmentTwo.Reading[] get_readings (String server_name)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("get_readings", true);
                $out.write_string (server_name);
                $in = _invoke ($out);
                AssignmentTwo.Reading $result[] = AssignmentTwo.readingListHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return get_readings (server_name        );
            } finally {
                _releaseReply ($in);
            }
  } // get_readings

  public AssignmentTwo.ServerDetails[] connected_servers ()
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("_get_connected_servers", true);
                $in = _invoke ($out);
                AssignmentTwo.ServerDetails $result[] = AssignmentTwo.serverListHelper.read ($in);
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return connected_servers (        );
            } finally {
                _releaseReply ($in);
            }
  } // connected_servers

  public void register_local_server (AssignmentTwo.ServerDetails info)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("register_local_server", true);
                AssignmentTwo.ServerDetailsHelper.write ($out, info);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                register_local_server (info        );
            } finally {
                _releaseReply ($in);
            }
  } // register_local_server

  public void unregister_local_server (AssignmentTwo.ServerDetails info)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("unregister_local_server", true);
                AssignmentTwo.ServerDetailsHelper.write ($out, info);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                unregister_local_server (info        );
            } finally {
                _releaseReply ($in);
            }
  } // unregister_local_server

  public void send_alert (AssignmentTwo.Alert alert)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("send_alert", true);
                AssignmentTwo.AlertHelper.write ($out, alert);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                send_alert (alert        );
            } finally {
                _releaseReply ($in);
            }
  } // send_alert

  public void register_agency (AssignmentTwo.Agency agency)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("register_agency", true);
                AssignmentTwo.AgencyHelper.write ($out, agency);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                register_agency (agency        );
            } finally {
                _releaseReply ($in);
            }
  } // register_agency

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:AssignmentTwo/MonitoringCentre:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     org.omg.CORBA.Object obj = orb.string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
   } finally {
     orb.destroy() ;
   }
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     String str = orb.object_to_string (this);
     s.writeUTF (str);
   } finally {
     orb.destroy() ;
   }
  }
} // class _MonitoringCentreStub