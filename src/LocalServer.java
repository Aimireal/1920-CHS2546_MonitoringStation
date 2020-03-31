import AssignmentTwo.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Calendar;


class LocalServerServant extends LocalServerPOA
{
    //ArrayLists to hold our stations, readings and alarms
    private ArrayList<StationDetails> connectedStations;
    private ArrayList<Reading> readings;
    private ArrayList<Reading> alarms;

    //Args
    private ServerDetails serverDetails;
    private LocalServer parent;
    private ORB orb;
    private NamingContextExt namingService;

    private int timePeriod = 1;

    public LocalServerServant(ORB orbValue, LocalServer parent)
    {
        this.parent = parent;
        connectedStations = new ArrayList<>();
        readings = new ArrayList<>();
        alarms = new ArrayList<>();

        try
        {
            orb = orbValue;

            org.omg.CORBA.Object namingServiceObj = orb.resolve_initial_references("NameService");
            if(namingServiceObj == null)
                return;

            namingService = NamingContextExtHelper.narrow(namingServiceObj);
        } catch(Exception e)
        {
            System.err.println("Error: " + e);
            e.printStackTrace(System.out);
        }
    }

    @Override
    public StationDetails[] connected_servers()
    {
        return new StationDetails[0];
    }

    @Override
    public ServerDetails get_centre_info()
    {
        return null;
    }

    @Override
    public void set_info(ServerDetails info)
    {

    }

    @Override
    public Reading[] all_readings()
    {
        return new Reading[0];
    }

    @Override
    public Reading[] get_readings(String station_name)
    {
        return new Reading[0];
    }

    @Override
    public Reading[] get_current_readings()
    {
        return new Reading[0];
    }

    @Override
    public Reading[] alerts()
    {
        return new Reading[0];
    }

    @Override
    public void register_monitoring_station(StationDetails info)
    {

    }

    @Override
    public void send_alert(Reading reading)
    {

    }
}

public class LocalServer
{
    //Args
    private String name;
    private String location;
    private String monitoringCentre;
    private LocalServerServant servant;
    MonitoringCentre monitoringServant;

    public LocalServer(String[] args)
    {
        if(args.length >= 3)
        {
            name = args[0];
            location = args[1];
            monitoringCentre = args[2];
        }
        try
        {
            //Create and initialize the ORB
            ORB orb = ORB.init(args, null);

            //Get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            //Create servant
            servant = new LocalServerServant(orb,this);

            //Get the 'stringified IOR'
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            AssignmentTwo.LocalServer narrowRef = LocalServerHelper.narrow(ref);

            //Naming service setup
            org.omg.CORBA.Object namingServiceObj = orb.resolve_initial_references("NameService");
            if(namingServiceObj == null)
                return;

            org.omg.CosNaming.NamingContextExt nameService = NamingContextExtHelper.narrow(namingServiceObj);

            //Bind our object in the naming service against the object it is part of
            NameComponent[] nsName = nameService.to_name(name);
            nameService.rebind(nsName, narrowRef);

            //monitoringServant = MonitoringCentreHelper.narrow(nameService.resolve_str(monitoringCentre));
            //monitoringServant.register_local_server(newLocalServer);

        } catch(Exception e)
        {
            System.err.println("Error: " + e);
            e.printStackTrace(System.out);
        }
    }
}
