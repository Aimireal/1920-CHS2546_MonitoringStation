package AssignmentTwo;


/**
* AssignmentTwo/MonitoringStationOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from AssignmentTwo.idl
* Tuesday, 31 March 2020 19:26:32 o'clock BST
*/

public interface MonitoringStationOperations 
{
  AssignmentTwo.Reading reading ();
  AssignmentTwo.Reading[] reading_log ();
  void take_reading ();
  void set_info (AssignmentTwo.StationDetails info);
  AssignmentTwo.StationDetails get_info ();
  void activate ();
  void deactivate ();
  void reset ();
} // interface MonitoringStationOperations
