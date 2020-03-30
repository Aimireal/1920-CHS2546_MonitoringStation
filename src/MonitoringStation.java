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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;

class MonitoringStationServant extends MonitoringStationPOA
{
    public StationDetails stationDetails;
    private MonitoringStation parent;
    private ArrayList<Reading> readings;
    private Timer timer;
    private static final int readingAlarm = 25;

    public MonitoringStationServant(MonitoringStation parentGUI)
    {
        parent = parentGUI;
        readings = new ArrayList<>();
    }

    @Override
    public Reading reading()
    {
        //Get Reading
        int readingValue = parent.getReadingValue();
        String stationName = get_info().station_name;
        int time = 0;
        int date = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        //Create and check value of reading
        Reading reading = new Reading(time, date, stationName, readingValue);
        if(readingValue >= readingAlarm)
        {
            parent.localServant.send_alert(reading);
        }
        parent.populateLog();
        return reading;
    }

    @Override
    public Reading[] reading_log()
    {
        return readings.toArray(new Reading[0]);
    }

    @Override
    public void take_reading()
    {
        readings.add(reading());
    }

    @Override
    public void set_info(StationDetails info)
    {
        this.stationDetails = info;
    }

    @Override
    public StationDetails get_info()
    {
        return stationDetails;
    }

    @Override
    public void activate()
    {
        timer = new Timer(5000, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                take_reading();
                parent.populateLog();
            }
        });
        timer.start();
    }

    @Override
    public void deactivate()
    {
        timer.stop();
    }

    @Override
    public void reset()
    {
        parent.clearLog();
        readings.clear();
    }
}

public class MonitoringStation extends JFrame
{
    //Args
    private String name;
    private String location;
    private String localServer;

    AssignmentTwo.LocalServer localServant;

    //Servant setup
    public MonitoringStation(String[] args)
    {
        //Check input and start servant
        if(args.length >= 3)
        {
            //Populate args
            name = args[0];
            location = args[1];
            localServer = args[2];

            if(name == null || location == null || localServer == null)
            {
                System.out.println("Ensure values entered correctly");
                return;
            }
            try
            {
                //Create and initialize the ORB
                ORB orb = ORB.init(args, null);

                //Get reference to rootpoa & activate the POAManager
                POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
                rootpoa.the_POAManager().activate();

                //Create servant and register with the ORB
                MonitoringStationServant servant = new MonitoringStationServant(this);

                //Get the 'stringified IOR'
                //org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
                //String stringifiedIOR = orb.object_to_string(ref);

                //ToDo: Example does commented code instead of Save IOR to file check if this works.
                //Save IOR to file
                //BufferedWriter out = new BufferedWriter(new FileWriter("Server.Ref"));
                //out.write(stringifiedIOR);
                //out.close();

                //Get the 'stringified IOR' and bind to naming service
                org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
                AssignmentTwo.MonitoringStation narrowRef = MonitoringStationHelper.narrow(ref);
                StationDetails newStationDetails = new StationDetails(name, location);
                servant.set_info(newStationDetails);

                //Naming service setup
                org.omg.CORBA.Object namingServiceObj = orb.resolve_initial_references("NameService");
                if(namingServiceObj == null)
                    return;

                org.omg.CosNaming.NamingContextExt nameService = NamingContextExtHelper.narrow(namingServiceObj);

                //Bind our monitoring station object in the naming service against our local server
                NameComponent[] nsName = nameService.to_name(name);
                nameService.rebind(nsName, narrowRef);

                localServant = LocalServerHelper.narrow(nameService.resolve_str(localServer));
                localServant.register_monitoring_station(newStationDetails);

                //Setup GUI
                setupGUI();

            } catch(Exception e)
            {
                System.err.println("Error: " + e);
                e.printStackTrace(System.out);
            }
        } else
        {
            System.out.println("You need to specify all details");
        }
    }

    public static void main(String[] args)
    {
        final String[] arguments = args;
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                new MonitoringStation(arguments).setVisible(true);
            }
        });
    }

    public void setupGUI()
    {
        //ToDo Setup GUI


        setupListeners();
    }

    public void setupListeners()
    {
        //ToDo Setup Listeners
    }

    public int getReadingValue()
    {
        //ToDo: Control to set readingvalue to return what is here
        return 0;
    }

    private void initListeners()
    {
        //Button and window operations
    }

    public void populateLog()
    {

    }

    public void clearLog()
    {

    }
}

