import ClientAndServer.*;

import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import org.omg.CORBA.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;



class RelayServant extends RelayPOA {

    private ORB orb;
    private ClientAndServer.MonitoringSystem server;
    private LocalServer parent;

    RelayServant(LocalServer parentGUI, ORB orb_val) {
        // store reference to parent GUI
        parent = parentGUI;

        // store reference to ORB
        orb = orb_val;


        // look up the server
        try {
            // read in the 'stringified IOR'
            BufferedReader in = new BufferedReader(new FileReader("server.ref"));
            String stringified_ior = in.readLine();

            // get object reference from stringified IOR
            org.omg.CORBA.Object server_ref =
                    orb.string_to_object(stringified_ior);
            server = ClientAndServer.MonitoringSystemHelper.narrow(server_ref);
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
        }
    }

    public String fetch_message() {
        parent.addMessage("fetch_message called by client.  Calling server..\n");

        String messageFromServer = server.alertMessage();

        parent.addMessage("message from server = " + messageFromServer + "\n"
                + "   Now forwarding to client..\n\n");

        return messageFromServer;
    }
}


public class LocalServer extends JFrame {
    private JTextArea textarea;

    public LocalServer(String[] args) {
        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            RelayServant relayRef = new RelayServant(this, orb);

            // Get the 'stringified IOR'
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(relayRef);
            String stringified_ior = orb.object_to_string(ref);

            // Save IOR to file
            BufferedWriter out = new BufferedWriter(new FileWriter("relay.ref"));
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
            setTitle("Local Server");

            addWindowListener (new java.awt.event.WindowAdapter () {
                public void windowClosing (java.awt.event.WindowEvent evt) {
                    System.exit(0);
                }
            } );


            // wait for invocations from clients
            textarea.append("Relay started.  Waiting for clients...\n\n");

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
                new  LocalServer(arguments).setVisible(true);
            }
        });
    }

}
