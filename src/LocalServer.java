import AssignmentTwo.*;
import AssignmentTwo.MonitoringStation;
import com.sun.media.sound.AlawCodec;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.stream.Collectors;


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
        return connectedStations.toArray(new StationDetails[0]);
    }

    @Override
    public ServerDetails get_centre_info()
    {
        return serverDetails;
    }

    @Override
    public void set_info(ServerDetails info)
    {
        this.serverDetails = serverDetails;
    }

    @Override
    public Reading[] all_readings()
    {
        for(StationDetails server : connectedStations)
        {
            String stationName = server.station_name;
            try
            {
                //Station reference
                MonitoringStation monitoringStation = MonitoringStationHelper.narrow(namingService.resolve_str(stationName));
                ArrayList<Reading> allReadings = new ArrayList<>(Arrays.asList(monitoringStation.reading_log()));

                //Check if we have any new readings
                //ToDo: We might only need to check Date/Time and station name. Probably can do this in line?
                if(allReadings.size() != 0)
                {
                    for (Reading nextReading : allReadings)
                    {
                        if (!readingReader(readings, nextReading))
                        {
                            readings.add(nextReading);
                        }
                    }
                }
            } catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        return readings.toArray(new Reading[0]);
    }

    @Override
    public Reading[] get_readings(String station_name)
    {
        try
        {
            MonitoringStation monitoringStation = MonitoringStationHelper.narrow(namingService.resolve_str(station_name));
            return monitoringStation.reading_log();
        } catch(Exception e)
        {
            return new Reading[0];
        }
    }

    @Override
    public Reading[] get_current_readings()
    {
        ArrayList<Reading> readings = new ArrayList<>();
        for(ServerDetails s: connectedStations())
        {
            try
            {
                MonitoringStation monitoringStation = MonitoringStationHelper.narrow(namingService.resolve_str(s.server_name));
                readings.add(monitoringStation.reading());
            } catch(NotFound | CannotProceed | InvalidName e)
            {
                e.printStackTrace();
                return new Reading[0];
            }
        }
        return readings.toArray(new Reading[0]);
    }

    @Override
    public Reading[] alerts()
    {
        return alarms.toArray(new Reading[0]);
    }

    @Override
    public void register_monitoring_station(StationDetails info)
    {
        connectedStations.add(info);
    }

    //ToDo: Look for a new way to do this stuff and fix monitoringCentre not being known. It might be monitoringStation it wants even
    @Override
    public void send_alert(Reading reading)
    {
        ArrayList<Reading> alerts = alarms.stream().filter(
                r->!r.station_name.equals(reading.station_name)
                && r.date == reading.date
                && (r.time + timePeriod >= reading.time || r.time == reading.time))
                .collect(Collectors.toCollection(ArrayList::new));

        if(!readingReader(alarms, reading))
        {
            alarms.add(reading);
            alerts.add(reading);
        }

        if(alerts.size() > 1)
        {
            Alert alert = new Alert(this.get_centre_info().server_name, alerts.toArray(new Reading[0]));
            //parent.monitoringCentre.send_alert(alert);
        }
    }

    public boolean readingReader(ArrayList<Reading> list, Reading r)
    {
        for(Reading reading: list)
        {
            if((reading.reading_level == r.reading_level)
                    && (reading.station_name.equals(r.station_name))
                    && (reading.date == r.date)
                    && (reading.time == r.time))
            {
                return true;
            }
        }
        return false;
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
