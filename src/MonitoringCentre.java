import ClientAndServer.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;


class HelloServant extends MonitoringSystemPOA {
    private MonitoringCentre parent;

    HelloServant(MonitoringCentre parentGUI) {
        // store reference to parent GUI
        parent = parentGUI;
    }

    public String alertMessage() {
        parent.addMessage("Alert called by relay.\n    Replying with message...\n\n");
        return "ping";
    }
}


public class MonitoringCentre extends JFrame {
    private JTextArea textarea;

    public MonitoringCentre(String[] args){
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            HelloServant helloRef = new HelloServant(this);

            // get the 'stringified IOR'
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(helloRef);
            String stringified_ior = orb.object_to_string(ref);

            // Save IOR to file
            BufferedWriter out = new BufferedWriter(new FileWriter("server.ref"));
            out.write(stringified_ior);
            out.close();

            // set up the GUI
            textarea = new JTextArea(20,25);
            JScrollPane scrollpane = new JScrollPane(textarea);
            JPanel textPanel = new JPanel();

            JPanel buttonPanel = new JPanel();
            JButton pollButton = new JButton("Poll");
            pollButton.addActionListener(new ActionListener(){
                public void actionPerformed (ActionEvent evt) {
                    //ToDO: Need to open allow a user to choose a Local Centre here
                }
            });

            textPanel.add(scrollpane);
            buttonPanel.add(pollButton);

            getContentPane().add(textPanel, "Center");
            getContentPane().add(buttonPanel, "South");

            setSize(400, 500);
            setTitle("Monitoring Centre");

            addWindowListener (new java.awt.event.WindowAdapter () {
                public void windowClosing (java.awt.event.WindowEvent evt) {
                    System.exit(0);
                }
            } );


            // wait for invocations from clients
            textarea.append("Server started.  Waiting for clients...\n\n");

            // remove the "orb.run()" command,
            // or the server will run but the GUI will not be visible
            // orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

    }


    void addMessage(String message){
        textarea.append(message);
    }


    public static void main(String[] args) {
        final String[] arguments = args;
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new  MonitoringCentre(arguments).setVisible(true);
            }
        });
    }
}