import AssignmentTwo.*;
import AssignmentTwo.LocalServer;
import AssignmentTwo.MonitoringStation;
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
import java.util.Properties;
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
            {
                System.out.println("Naming Service not registered");
                return;
            }

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
                    Iterator<Reading> newIterator = localReadings.iterator();
                    while(newIterator.hasNext())
                    {
                        Reading newReading = newIterator.next();
                        if(!readingReader(readings, newReading))
                        {
                            readings.add(newReading);
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
    public void register_local_server(ServerDetails serverDetails)
    {
        connectedServers.add(serverDetails);
        parent.addToServerList(serverDetails);
    }

    @Override
    public void unregister_local_server(ServerDetails serverDetails)
    {
        connectedServers.removeIf(server -> server.server_name.equals(serverDetails.server_name));
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
                parent.alertListModel.addElement(alert);
                StringBuilder alertMessage = new StringBuilder("Alarm triggered at local server:" + alert.server_name);

                for(int i = 0; i < alert.alert_readings.length; i++)
                {
                    alertMessage.append(alert.alert_readings[i].station_name).append(" - Reading Level: ").append(alert.alert_readings[i].reading_level).append(" - Time: ").append(alert.alert_readings[i].time).append("/").append(alert.alert_readings[i].date);
                }

                //Contact the agencies
                ArrayList<Agency> localAgencies = agencies.stream().filter(
                        a->a.agency_region.equals(alert.server_name)).collect(Collectors.toCollection(ArrayList::new));

                if(localAgencies.size() > 0)
                {
                    for(Agency agency: localAgencies)
                    {
                        alertMessage.append(agency.agency_name).append(" is to be notified via: ").append(agency.agency_contact);
                    }
                }
                JOptionPane.showMessageDialog(parent, alertMessage.toString());
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
    //Initialise GUI elements
    private String name;
    NamingContextExt namingService;
    MonitoringCentreServant servant;

    private JPanel panel = new JPanel();
    private JPanel serverPanel = new JPanel();
    private JPanel stationPanel = new JPanel();
    private JPanel readingPanel = new JPanel();
    private JPanel alertPanel = new JPanel();
    private JPanel agencyPanel = new JPanel();

    private JButton getCentreStations;
    private JButton getCentreReadings;
    private JButton getStationReadings;
    private JButton getAllReadings;
    private JButton registerAgency;
    private JButton getCurrentConnectedReadings;

    private JList<ServerDetails> serverList;
    private JList<StationDetails> stationList;
    private JList<Reading> readingList;
    private JList<Alert> alertList;

    DefaultListModel<ServerDetails> serverListModel;
    DefaultListModel<StationDetails> stationListModel;
    DefaultListModel<Reading> readingListModel;
    DefaultListModel<Alert> alertListModel;

    private JTextField agencyName;
    private JTextField locationField;
    private JTextField contactField;

    public MonitoringCentre(String[] args)
    {
        name = args[0];
        try
        {
            if (name == null)
            {
                System.out.println("No MonitoringCentre name");
                return;
            }

            //Create and initialize the ORB
            Properties properties = new Properties();
            properties.put("org.omg.CORBA.ORBInitialPort", "1050");
            ORB orb = ORB.init(args, properties);

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
            if (namingServiceObj == null)
                return;

            namingService = NamingContextExtHelper.narrow(namingServiceObj);

            //Bind our monitoring station object in the naming service against our local server
            NameComponent[] nsName = namingService.to_name(name);
            namingService.rebind(nsName, narrowRef);

            //SetupGUI and button functionality
            setupGUI();
            setupButtons();
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
        panel = new JPanel();
        serverPanel = new JPanel();
        stationPanel = new JPanel();
        readingPanel = new JPanel();
        alertPanel = new JPanel();
        agencyPanel = new JPanel();

        //Server Panel
        serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.PAGE_AXIS));
        serverPanel.setPreferredSize(new Dimension(250, 200));

        serverListModel = new DefaultListModel<>();
        serverList = new JList<>(serverListModel);
        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        serverList.setVisibleRowCount(-1);

        ServerListCellRenderer serverRenderer = new ServerListCellRenderer();
        serverList.setCellRenderer(serverRenderer);

        JScrollPane serverListScroll = new JScrollPane(serverList);
        serverListScroll.setPreferredSize((new Dimension(250, 80)));
        JLabel serverListLabel = new JLabel("Connected Servers");
        getCentreStations = new JButton("Stations at Server");

        serverPanel.add(serverListLabel);
        serverPanel.add(serverListScroll);

        //Station Panel
        stationPanel.setLayout(new BoxLayout(stationPanel, BoxLayout.PAGE_AXIS));
        stationPanel.setPreferredSize(new Dimension(250, 200));

        stationListModel = new DefaultListModel<>();
        stationList = new JList<>(stationListModel);
        stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stationList.setVisibleRowCount(-1);

        StationListCellRenderer stationRenderer = new StationListCellRenderer();
        stationList.setCellRenderer(stationRenderer);

        JScrollPane stationListScroll = new JScrollPane(stationList);
        stationListScroll.setPreferredSize((new Dimension(250, 80)));
        JLabel stationListLabel = new JLabel("Stations for Server");
        getStationReadings = new JButton("Station Readings");
        getAllReadings = new JButton("All Readings");

        serverPanel.add(stationListLabel);
        serverPanel.add(stationListScroll);

        //Reading Panel
        readingPanel.setLayout(new BoxLayout(readingPanel, BoxLayout.PAGE_AXIS));
        readingPanel.setPreferredSize(new Dimension(250, 200));

        readingListModel = new DefaultListModel<>();
        readingList = new JList<>(readingListModel);
        readingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        readingList.setVisibleRowCount(-1);

        ReadingListCellRenderer readingRenderer = new ReadingListCellRenderer();
        readingList.setCellRenderer(readingRenderer);

        JScrollPane readingListScroll = new JScrollPane(readingList);
        readingListScroll.setPreferredSize((new Dimension(250, 80)));
        JLabel readingLabel = new JLabel("Readings");

        readingPanel.add(readingLabel);
        readingPanel.add(readingListScroll);

        //Alert Panel
        alertPanel.setLayout(new BoxLayout(alertPanel, BoxLayout.PAGE_AXIS));
        alertPanel.setPreferredSize(new Dimension(250, 200));

        alertListModel = new DefaultListModel<>();
        alertList = new JList<>(alertListModel);
        alertList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        alertList.setVisibleRowCount(-1);

        AlertListCellRenderer alertRenderer = new AlertListCellRenderer();
        alertList.setCellRenderer(alertRenderer);

        JScrollPane alertListScroll = new JScrollPane(alertList);
        alertListScroll.setPreferredSize((new Dimension(250, 80)));
        JLabel alertLabel = new JLabel("Alerts");

        alertPanel.add(alertLabel);
        alertPanel.add(alertListScroll);

        getCentreReadings = new JButton("Server Readings");
        getCurrentConnectedReadings = new JButton("Poll Connected Server Stations");

        //Build Panel
        panel.add(serverPanel);
        panel.add(stationPanel);
        panel.add(readingPanel);
        panel.add(alertPanel);

        //ToDo: Name the buttons more consistently
        panel.add(getCentreStations);
        panel.add(getStationReadings);
        panel.add(getAllReadings);
        panel.add(getCentreReadings);
        panel.add(getCurrentConnectedReadings);


        //Agency Panel
        agencyPanel.setLayout(new BoxLayout(agencyPanel, BoxLayout.PAGE_AXIS));
        JLabel agencyLabel = new JLabel("Agency Name");
        agencyName = new JTextField();
        JLabel agencyLocation = new JLabel("Location");
        locationField = new JTextField();
        JLabel agencyContact = new JLabel("Contact Information");
        contactField = new JTextField();
        registerAgency = new JButton("Register Agency");

        agencyPanel.add(agencyLabel);
        agencyPanel.add(agencyName);
        agencyPanel.add(agencyLocation);
        agencyPanel.add(locationField);
        agencyPanel.add(agencyContact);
        agencyPanel.add(contactField);
        agencyPanel.add(registerAgency);


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
    }

    public void setupButtons()
    {
        //ToDo: Might be worth initialising stuff at class level and making methods to do setup GUI for each panel, then one to do listeners and put GUI together
        //Button Listeners
        getCentreStations.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                stationListModel.clear();
                ServerDetails server = serverList.getSelectedValue();
                if(server != null)
                {
                    try
                    {
                        //Find the servant for the server
                        LocalServer localServerServant = LocalServerHelper.narrow(namingService.resolve_str(server.server_name));
                        StationDetails[] stationList = localServerServant.connected_servers();

                        for(int i = 0; i < stationList.length; i++)
                        {
                            stationListModel.addElement(stationList[i]);
                        }
                    } catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }else
                {
                    System.out.println("Server not provided");
                }
            }
        });

        getCentreReadings.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                readingListModel.clear();
                ServerDetails server = serverList.getSelectedValue();
                try
                {
                    LocalServer localServer = LocalServerHelper.narrow(namingService.resolve_str(server.server_name));
                    Reading[] serverReadings = localServer.all_readings();

                    for(int i = 0; i < serverReadings.length; i++)
                    {
                        readingListModel.addElement(serverReadings[i]);
                    }
                } catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        getStationReadings.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                readingListModel.clear();
                try
                {
                    LocalServer localServerServant = LocalServerHelper.narrow(namingService.resolve_str(serverList.getSelectedValue().server_name));
                    Reading[] stationReadings = localServerServant.get_readings(stationList.getSelectedValue().station_name);
                    if(stationReadings != null)
                    {
                        for(int i = 0; i < stationReadings.length; i++)
                        {
                            readingListModel.addElement(stationReadings[i]);
                        }
                    } else
                    {
                        System.out.println("StationReadings failed. No station found");
                    }
                } catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        getAllReadings.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                readingListModel.clear();
                Reading[] readings = servant.all_readings();
                for(int i = 0; i < readings.length; i++)
                {
                    readingListModel.addElement(readings[i]);
                }
            }
        });

        getCurrentConnectedReadings.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                readingListModel.clear();
                try
                {
                    LocalServer localServerServant = LocalServerHelper.narrow(namingService.resolve_str(serverList.getSelectedValue().server_name));
                    StationDetails[] stations = localServerServant.connected_servers();
                    ArrayList<Reading> pollReadings = new ArrayList<>();

                    for(int i = 0; i < stations.length; i++)
                    {
                        MonitoringStation station = MonitoringStationHelper.narrow(namingService.resolve_str(stations[i].station_name));
                        pollReadings.add(station.reading());
                    }
                    for(Reading reading: pollReadings)
                    {
                        readingListModel.addElement(reading);
                    }
                } catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        registerAgency.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                String name = agencyName.getText();
                String location = locationField.getText();
                String contact = contactField.getText();

                if(name.isEmpty() || location.isEmpty() || contact.isEmpty())
                    return;

                Agency agency = new Agency(name, location, contact);
                servant.register_agency(agency);
                agencyName.setText("");
                locationField.setText("");
                contactField.setText("");
            }
        });

    }

    public void addToServerList(ServerDetails serverDetails)
    {
        serverListModel.addElement(serverDetails);
    }

    public static void main(String[] args)
    {
        final String[] arguments = args;
        java.awt.EventQueue.invokeLater(() -> new MonitoringCentre(arguments).setVisible(true));
    }

    //Renderer classes to format our lists
    class ServerListCellRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList<?> jList, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            super.getListCellRendererComponent(jList, value, index, isSelected, cellHasFocus);
            if(value instanceof ServerDetails)
            {
                ServerDetails server = (ServerDetails)value;
                setText(server.server_name);
            }
            return this;
        }
    }

    class StationListCellRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList<?> jList, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            super.getListCellRendererComponent(jList, value, index, isSelected, cellHasFocus);
            if(value instanceof StationDetails)
            {
                StationDetails station = (StationDetails)value;
                setText(station.station_name + " - " + station.location);
            }
            return this;
        }
    }

    class ReadingListCellRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList<?> jList, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            super.getListCellRendererComponent(jList, value, index, isSelected, cellHasFocus);
            if(value instanceof Reading)
            {
                Reading readings = (Reading)value;
                setText(readings.station_name + " - " + readings.reading_level + " - " + readings.time + "/" + readings.date);
            }
            return this;
        }
    }

    class AlertListCellRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList<?> jList, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            super.getListCellRendererComponent(jList, value, index, isSelected, cellHasFocus);
            if(value instanceof Alert)
            {
                Alert alert = (Alert) value;

                String alertMessage = alert.server_name + " at station(s): ";
                for(int i = 0; i < alert.alert_readings.length; i++)
                {
                    alertMessage += alert.alert_readings[i].station_name + (", ");
                }
                setText(alertMessage);
            }
            return this;
        }
    }

}
