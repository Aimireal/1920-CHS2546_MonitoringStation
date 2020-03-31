import AssignmentTwo.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Calendar;


class MonitoringCentreServant extends MonitoringCentrePOA
{
    private ArrayList<StationDetails> connectedStations;
    private ArrayList<Reading> readings;
    private ArrayList<Reading> alarms;

    //Args
    private ServerDetails serverDetails;
    private MonitoringCentre parent;
    private ORB orb;
    private NamingContextExt namingService;

    private int timePeriod = 1;

    public MonitoringCentreServant(ORB orbValue, MonitoringCentre parent)
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

    //ToDo Overrides
    @Override
    public Reading[] all_readings()
    {
        return new Reading[0];
    }

    @Override
    public Reading[] get_readings(String server_name)
    {
        return new Reading[0];
    }

    @Override
    public ServerDetails[] connected_servers()
    {
        return new ServerDetails[0];
    }

    @Override
    public void register_local_server(ServerDetails info)
    {

    }

    @Override
    public void unregister_local_server(ServerDetails info)
    {

    }

    @Override
    public void send_alert(Alert alert)
    {

    }

    @Override
    public void register_agency(Agency agency)
    {

    }
}

public class MonitoringCentre extends JFrame
{
    public MonitoringCentre(String[] args)
    {
        try
        {
            //Create and initialize the ORB
            ORB orb = ORB.init(args, null);

            //Get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            /* ToDO: MonitoringCentreServant does not exist
            //Create servant
            servant = new MonitoringCentreServant(this, orb);

            //Get the 'stringified IOR'
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            AssignmentTwo.MonitoringCentre narrowRef = MonitoringCentreHelper.narrow(ref);

            //Naming service setup
            org.omg.CORBA.Object namingServiceObj = orb.resolve_initial_references("NameService");
            if(namingServiceObj == null)
                return;

            org.omg.CosNaming.NamingContextExt nameService = NamingContextExtHelper.narrow(namingServiceObj);

            //Bind our monitoring station object in the naming service against our local server
            NameComponent[] nsName = nameService.to_name(name);
            nameService.rebind(nsName, narrowRef);
             */

            //SetupGUI and button functionality
            setupGUI();

        } catch (Exception e)
        {
            System.err.println("Error: " + e);
            e.printStackTrace();
        }
    }

    public void setupGUI()
    {
        //Draw GUI
        JTextArea textarea = new JTextArea(20, 25);
        JScrollPane scrollPane = new JScrollPane(textarea);
        JPanel panel = new JPanel();

        //Centres Panel

        //Stations Panel

        //Agency Panel

    }
}
