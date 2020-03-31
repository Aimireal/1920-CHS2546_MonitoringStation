package AssignmentTwo;


/**
* AssignmentTwo/LocalServerOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AssignmentTwo.idl
* Tuesday, 31 March 2020 19:26:32 o'clock BST
*/

public interface LocalServerOperations 
{
  AssignmentTwo.StationDetails[] connected_servers ();
  AssignmentTwo.ServerDetails get_centre_info ();
  void set_info (AssignmentTwo.ServerDetails info);
  AssignmentTwo.Reading[] all_readings ();
  AssignmentTwo.Reading[] get_readings (String station_name);
  AssignmentTwo.Reading[] get_current_readings ();
  AssignmentTwo.Reading[] alerts ();
  void register_monitoring_station (AssignmentTwo.StationDetails info);
  void send_alert (AssignmentTwo.Reading reading);
} // interface LocalServerOperations
