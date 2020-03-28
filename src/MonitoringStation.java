import org.omg.CORBA.ORB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Objects;


public class MonitoringStation extends JFrame {
    private JTextArea textarea;

    public MonitoringStation(String[] args) {
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // read in the 'stringified IOR' of the Relay
            BufferedReader in = new BufferedReader(new FileReader("relay.ref"));
            String stringified_ior = in.readLine();

            // get object reference from stringified IOR
            org.omg.CORBA.Object server_ref =
                    orb.string_to_object(stringified_ior);

            final ClientAndServer.Relay relay =
                    ClientAndServer.RelayHelper.narrow(server_ref);


            // set up the GUI
            JPanel textpanel = new JPanel();
            textarea = new JTextArea(20,25);
            JScrollPane scrollpane = new JScrollPane(textarea);

            JButton registerButton = new JButton("Register");
            registerButton.addActionListener(new ActionListener(){
                public void actionPerformed (ActionEvent evt) {
                    //ToDO: Need to open allow a user to choose a Local Centre here
                }
            });

            JPanel noxPanel = new JPanel();
            JComboBox<String> noxComboBox = new JComboBox<String>();
            noxComboBox.addItem("0-50 (Good)");
            noxComboBox.addItem("51-100 (Moderate)");
            noxComboBox.addItem("101-150 (Risk for Vulnerable)");
            noxComboBox.addItem("151-200 (Unhealthy)");
            noxComboBox.addItem("201+ (Very Unhealthy)");
            JLabel noxLabel = new JLabel("Nox Readings: ");

            JPanel buttonPanel = new JPanel();
            JButton getItButton = new JButton("NOx Reading");
            getItButton.addActionListener (new ActionListener() {
                public void actionPerformed (ActionEvent evt) {
                    String selectedReading = Objects.requireNonNull(noxComboBox.getSelectedItem()).toString();
                    textarea.append(selectedReading);
                    String result = relay.fetch_message();
                    textarea.append("   Result = " + result + "\n\n");
                }
            });

            // Adding Panels to window
            textpanel.add(scrollpane);
            buttonPanel.add(getItButton);
            buttonPanel.add(registerButton);
            noxPanel.add(noxLabel);
            noxPanel.add(noxComboBox);

            getContentPane().add(noxPanel, "North");
            getContentPane().add(textpanel, "Center");
            getContentPane().add(buttonPanel, "South");

            setSize(400, 500);
            setTitle("Monitoring Station");

            addWindowListener (new WindowAdapter () {
                public void windowClosing (WindowEvent evt) {
                    System.exit(0);
                }
            } );

            textarea.append("Client started.  Click the button to contact relay...\n\n");

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
        }
    }



    public static void main(String[] args) {
        final String[] arguments = args;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MonitoringStation(arguments).setVisible(true);
            }
        });
    }
}