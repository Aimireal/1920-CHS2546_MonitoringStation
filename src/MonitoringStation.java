import AssignmentTwo.*;
import AssignmentTwo.LocalServer;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

class MonitoringStationServant extends MonitoringStationPOA
{
    //Args
    public StationDetails stationDetails;
    private MonitoringStation parent;
    private ArrayList<Reading> readings;

    private Timer timer;
    private static final int readingAlert= 25;

    public MonitoringStationServant(MonitoringStation parentGUI)
    {
        parent = parentGUI;
        readings = new ArrayList<>();
    }

    @Override
    public Reading reading()
    {
        //ToDo: Time format
        //Get Reading
        int readingValue = parent.getReadingValue();
        String stationName = get_info().station_name;
        int time = getTime();
        int date = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        //Create and check value of reading
        Reading reading = new Reading(time, date, stationName, readingValue);
        if(readingValue >= readingAlert)
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

    public int getTime()
    {
        //ToDo: Change this or make it generic or do it in where we call this method.
        int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        return hours * 60 + minute;
    }
}

public class MonitoringStation extends JFrame
{
    //Args
    private String name;
    private String location;
    private String localServer;

    //Initialise GUI elements
    JPanel panel;
    JScrollPane scrollPane;
    JTextArea textarea;
    JButton readingButton;
    JButton activateButton;
    JButton deactivateButton;
    JButton resetButton;
    JSlider readingSlider;

    MonitoringStationServant servant = new MonitoringStationServant(this);
    LocalServer localServant;

    //Servant setup
    public MonitoringStation(String[] args)
    {
        //Check input and start servant
        if(args.length > 3)
        {
            System.out.println("You have too many arguments");
            return;
        }
            name = args[0];
            location = args[1];
            localServer = args[2];

            if(name == null || location == null || localServer == null)
            {
                System.out.println("Ensure values entered correctly");
            } else
            {
                try
                {
                    //Create and initialize the ORB
                    Properties properties = new Properties();
                    properties.put("org.omg.CORBA.ORBInitialPort", "1050");
                    ORB orb = ORB.init(args, properties);

                    //Get reference to rootpoa & activate the POAManager
                    POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
                    rootpoa.the_POAManager().activate();

                    //Naming service setup
                    org.omg.CORBA.Object namingServiceObj = orb.resolve_initial_references("NameService");
                    if(namingServiceObj == null)
                        return;

                    org.omg.CosNaming.NamingContextExt nameService = NamingContextExtHelper.narrow(namingServiceObj);

                    servant = new MonitoringStationServant(this);

                    //Get the 'stringified IOR' and bind to naming service
                    org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
                    AssignmentTwo.MonitoringStation narrowRef = MonitoringStationHelper.narrow(ref);
                    StationDetails newStationDetails = new StationDetails(name, location);
                    servant.set_info(newStationDetails);

                    //Bind our object in the naming service against the object it is part of
                    NameComponent[] nsName = nameService.to_name(name);
                    nameService.rebind(nsName, narrowRef);

                    localServant = LocalServerHelper.narrow(nameService.resolve_str(localServer));
                    localServant.register_monitoring_station(newStationDetails);

                    //Setup GUI and button functionality
                    setupGUI();

                } catch(Exception e)
                {
                    System.err.println("Error: " + e);
                    e.printStackTrace(System.out);
                }
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
        //Draw GUI
        textarea = new JTextArea(20, 25);
        scrollPane = new JScrollPane(textarea);
        panel = new JPanel();

        readingSlider = new JSlider(0, 50);
        readingButton = new JButton("Get Reading");
        activateButton = new JButton("Activate");
        deactivateButton = new JButton("Deactivate");
        resetButton = new JButton("Reset");

        readingSlider.setMajorTickSpacing(10);
        readingSlider.setPaintTicks(true);
        readingSlider.setPaintLabels(true);

        panel.add(readingSlider);
        panel.add(readingButton);
        panel.add(activateButton);
        panel.add(deactivateButton);
        panel.add(resetButton);
        panel.add(scrollPane);
        getContentPane().add(panel, "Center");

        setSize(400, 450);
        setTitle("Monitoring Station: " + servant.get_info().station_name + " - " + servant.get_info().location);

        textarea.append("Station activated");

        //Setup button functionality
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                System.exit(0);
            }
        });

        readingButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                servant.take_reading();
                populateLog();
            }
        });

        activateButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                servant.activate();
                System.out.println("Servant Activated");
            }
        });

        deactivateButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                servant.deactivate();
                System.out.println("Servant Deactivated");
            }
        });

        resetButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                servant.reset();
                System.out.println("Servant Reset");
            }
        });
    }

    public int getReadingValue()
    {
        return readingSlider.getValue();
    }

    public void populateLog()
    {
        clearLog();
        for(Reading reading: servant.reading_log())
        {
            textarea.append("Reading value: " + reading.reading_level + "\n");
        }
    }

    public void clearLog()
    {
        textarea.setText("");
    }
}

