import AssignmentTwo.*;
import AssignmentTwo.LocalServer;
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
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;


class MonitoringCentreServant extends MonitoringCentrePOA
{
    //Args
    private MonitoringCentre parent;
    private ArrayList<ServerDetails> connectedServers;
    private ArrayList<Reading> readings;
    private ArrayList<Alert> alerts;
    private ArrayList<Agency> agencies;

    private ORB orb;
    private NamingContextExt namingService;


    public MonitoringCentreServant(MonitoringCentre parentGUI, ORB orbValue)
    {
        this.parent = parentGUI;
        connectedServers = new ArrayList<>();
        readings = new ArrayList<>();
        alerts = new ArrayList<>();
        agencies = new ArrayList<>();

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
    public Reading[] all_readings()
    {
        readings.clear();
        for (ServerDetails connectedServer : connectedServers)
        {
            String name = connectedServer.server_name;
            try
            {
                //Server Reference
                LocalServer localServerServant = LocalServerHelper.narrow(namingService.resolve_str(name));
                ArrayList<Reading> localReadings = new ArrayList<>(Arrays.asList(localServerServant.all_readings()));

                //Check if we have any new readings
                //ToDo: We might only need to check Date/Time and station name. Probably can do this in line?
                if (localReadings.size() != 0)
                {
                    for (Reading nextReading : localReadings)
                    {
                        if (!readingReader(readings, nextReading))
                        {
                            readings.add(nextReading);
                        }
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return readings.toArray(new Reading[0]);
    }

    @Override
    public Reading[] get_readings(String server_name)
    {
        try
        {
            LocalServer localServer = LocalServerHelper.narrow(namingService.resolve_str(server_name));
            return localServer.all_readings();
        } catch(Exception e)
        {
            return new Reading[0];
        }
    }

    @Override
    public ServerDetails[] connected_servers()
    {
        return connectedServers.toArray(new ServerDetails[0]);
    }

    @Override
    public void register_local_server(ServerDetails info)
    {
        connectedServers.add(info);
        //ToDo: Implement addToCentreList
    }

    @Override
    public void unregister_local_server(ServerDetails info)
    {
        for(ServerDetails server: connectedServers)
        {
            if(server.server_name.equals(info.server_name))
            {
                connectedServers.remove(server);
            }
        }
    }

    @Override
    public void send_alert(Alert alert)
    {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                alerts.add(alert);
                //ToDo: Finishing this method
            }
        });
    }

    @Override
    public void register_agency(Agency agency)
    {
        agencies.add(agency);
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

public class MonitoringCentre extends JFrame
{
    private String name;
    NamingContextExt namingService;
    MonitoringCentreServant servant;

    private JList<ServerDetails> serverList;
    private JList<StationDetails> stationList;
    private JList<Reading> readingList;
    private JList<Alert> alertList;



    DefaultListModel<ServerDetails> serverListModel;
    DefaultListModel<StationDetails> stationListModel;
    DefaultListModel<Reading> readingListModel;
    DefaultListModel<Alert> alertListModel;

    public MonitoringCentre(String[] args)
    {
        name = args[0];
        try
        {
            //Create and initialize the ORB
            ORB orb = ORB.init(args, null);

            //Get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

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

            //SetupGUI and button functionality
            setupGUI();

        } catch (Exception e)
        {
            System.err.println("Error: " + e);
            e.printStackTrace();
        }
    }

    //ToDo: Check use of ServerListModel and finish addToCentreList function. Change name of it too
    public void setupGUI()
    {
        //Draw GUI
        JPanel panel = new JPanel();
        JPanel serverPanel = new JPanel();
        JPanel stationPanel = new JPanel();
        JPanel readingPanel = new JPanel();
        JPanel alertPanel = new JPanel();
        JPanel agencyPanel = new JPanel();

        //Server Panel
        serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.PAGE_AXIS));
        serverPanel.setPreferredSize(new Dimension(250, 200));

        serverListModel = new DefaultListModel<>();
        serverList = new JList<>();
        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverList.setVisibleRowCount(-1);
        /*
        ToDo: ServerListCellRenderer Class
        serverList.setCellRenderer(serverRenderer);
         */
        JScrollPane serverListScroll = new JScrollPane(serverList);
        serverListScroll.setPreferredSize((new Dimension(250, 80)));
        JLabel serverListLabel = new JLabel("Connected Servers");
        JButton centreButton = new JButton("Get Stations for Centre");

        serverPanel.add(serverListLabel);
        serverPanel.add(serverListScroll);

        //Station Panel
        stationPanel.setLayout(new BoxLayout(stationPanel, BoxLayout.PAGE_AXIS));
        stationPanel.setPreferredSize(new Dimension(250, 200));

        stationListModel = new DefaultListModel<>();
        stationList = new JList<>();
        stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stationList.setVisibleRowCount(-1);
        /*
        ToDo: StationListCellRenderer Class
        stationList.setCellRenderer(stationRenderer);
         */
        JScrollPane stationListScroll = new JScrollPane(stationList);
        stationListScroll.setPreferredSize((new Dimension(250, 80)));
        JLabel stationListLabel = new JLabel("Active Stations");
        JButton stationReadingsButton = new JButton("Station Readings");
        JButton allReadingsButton = new JButton("All Readings");

        serverPanel.add(stationListScroll);
        serverPanel.add(stationListLabel);

        //Reading Panel
        readingPanel.setLayout(new BoxLayout(readingPanel, BoxLayout.PAGE_AXIS));
        readingPanel.setPreferredSize(new Dimension(250, 200));

        readingListModel = new DefaultListModel<>();
        readingList = new JList<>();
        readingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        readingList.setVisibleRowCount(-1);
        /*
        ToDo: ReadingListCellRenderer Class
        readingList.setCellRenderer(readingRenderer);
         */
        JScrollPane readingListScroll = new JScrollPane(readingList);
        readingListScroll.setPreferredSize((new Dimension(250, 80)));
        JLabel readingLabel = new JLabel("Readings");

        readingPanel.add(readingLabel);
        readingPanel.add(readingListScroll);

        //Alert Panel
        alertPanel.setLayout(new BoxLayout(alertPanel, BoxLayout.PAGE_AXIS));
        alertPanel.setPreferredSize(new Dimension(250, 200));

        alertListModel = new DefaultListModel<>();
        alertList = new JList<>();
        alertList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        alertList.setVisibleRowCount(-1);
        /*
        ToDo: ReadingListCellRenderer Class
        readingList.setCellRenderer(readingRenderer);
         */
        JScrollPane alertListScroll = new JScrollPane(alertList);
        alertListScroll.setPreferredSize((new Dimension(250, 80)));
        JLabel alertLabel = new JLabel("Alerts");

        alertPanel.add(alertLabel);
        alertPanel.add(alertListScroll);

        JButton readingsButton = new JButton("Get Readings");
        JButton connectedReadingsButton = new JButton("Readings of Connected Stations");

        //Agency Panel
        agencyPanel.setLayout(new BoxLayout(alertPanel, BoxLayout.PAGE_AXIS));
        JLabel agencyLabel = new JLabel("Agency Name");
        JTextField agencyName = new JTextField();
        JLabel agencyLocation = new JLabel("Location");
        JTextField locationField = new JTextField();
        JLabel agencyContact = new JLabel("Contact Information");
        JTextField contactField = new JTextField();
        JButton registerButton = new JButton("Register Agency");

        agencyPanel.add(agencyLabel);
        agencyPanel.add(agencyName);
        agencyPanel.add(agencyLocation);
        agencyPanel.add(locationField);
        agencyPanel.add(agencyContact);
        agencyPanel.add(contactField);
        agencyPanel.add(registerButton);

        //Build Panel
        panel.add(serverPanel);
        panel.add(stationPanel);
        panel.add(readingPanel);
        panel.add(alertPanel);

        //ToDo: Name the buttons more consistently
        panel.add(centreButton);
        panel.add(stationReadingsButton);
        panel.add(allReadingsButton);
        panel.add(readingsButton);
        panel.add(connectedReadingsButton);

        panel.add(agencyPanel);

        getContentPane().add(panel, "Center");
        setSize(600, 700);

        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                System.exit(0);
            }
        });


        //ToDo: Might be worth initialising stuff at class level and making methods to do setup GUI for each panel, then one to do listeners and put GUI together
        //Button Listeners
        centreButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {

            }
        });

        stationReadingsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {

            }
        });

        allReadingsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {

            }
        });

        readingsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {

            }
        });

        connectedReadingsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {

            }
        });

        registerButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {

            }
        });
    }

    public static void main(String args[])
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

}
