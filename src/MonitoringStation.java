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


public class MonitoringStation extends JFrame
{
    //Initialise GUI elements
    JPanel panel;
    JScrollPane scrollPane;
    JTextArea textarea;
    JButton readingButton;
    JButton activateButton;
    JButton deactivateButton;
    JButton resetButton;
    JSlider readingSlider;
    JTextField sliderValue;

    MonitoringStationServant servant = new MonitoringStationServant(this);
    LocalServer localServant;

    public MonitoringStation(String[] args)
    {
        if(args.length > 3)
        {
            System.out.println("You have too many arguments");
            return;
        }

        String name = args[0];
        String location = args[1];
        String localServer = args[2];

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
                servant.set_details(newStationDetails);

                //Bind our object in the naming service against the object it is part of
                NameComponent[] nsName = nameService.to_name(name);
                nameService.rebind(nsName, narrowRef);

                localServant = LocalServerHelper.narrow(nameService.resolve_str(localServer));
                if(localServant == null)
                {
                    System.out.println("Failed to find LocalServer");
                    return;
                }

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

    public void setupGUI()
    {
        //Draw GUI
        textarea = new JTextArea(20, 25);
        scrollPane = new JScrollPane(textarea);
        panel = new JPanel();

        sliderValue = new JTextField(5);
        sliderValue.setEditable(false);

        readingSlider = new JSlider(0, 100);
        readingButton = new JButton("Get Reading");
        activateButton = new JButton("Activate");
        deactivateButton = new JButton("Deactivate");
        resetButton = new JButton("Reset");

        readingSlider.setMajorTickSpacing(25);
        readingSlider.setMinorTickSpacing(5);
        readingSlider.setPaintTicks(true);
        readingSlider.setPaintLabels(true);
        readingSlider.setValue(0);
        readingSlider.addChangeListener(changeEvent -> sliderValue.setText("" + readingSlider.getValue()));

        panel.add(sliderValue);
        panel.add(readingSlider);
        panel.add(readingButton);
        panel.add(scrollPane);

        panel.add(activateButton);
        panel.add(deactivateButton);
        panel.add(resetButton);
        getContentPane().add(panel, "Center");

        setSize(300, 450);
        setTitle("Monitoring Station: " + servant.get_details().station_name + " - " + servant.get_details().location);

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
                servant.get_reading();
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

    public int getReadingValue()
    {
        return readingSlider.getValue();
    }

    public static void main(String[] args)
    {
        //Build our initialisation screen
        JFrame frame = new JFrame();
        String namePrompt = "Please enter the stations name";
        String name = JOptionPane.showInputDialog(frame, namePrompt);

        String locationPrompt = "Please enter the stations location";
        String location = JOptionPane.showInputDialog(frame, locationPrompt);

        String serverPrompt = "Please enter the stations server";
        String server = JOptionPane.showInputDialog(frame, serverPrompt);

        args = new String[]{name, location, server};

        if(name != null && location != null && server != null)
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
        } else
        {
            JOptionPane.showMessageDialog(frame, "Please provide all details", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}

class MonitoringStationServant extends MonitoringStationPOA
{
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
        int readingValue = parent.getReadingValue();
        String stationName = get_details().station_name;
        int time = timeAsInt();
        int date = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

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
    public void get_reading()
    {
        readings.add(reading());
    }

    @Override
    public StationDetails get_details()
    {
        return stationDetails;
    }

    @Override
    public void set_details(StationDetails info)
    {
        this.stationDetails = info;
    }

    @Override
    public void activate()
    {
        timer = new Timer(10000, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                get_reading();
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

    public int timeAsInt()
    {
        int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        return hours * 60 + minute;
    }
}

