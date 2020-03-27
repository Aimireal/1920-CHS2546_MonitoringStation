import ClientAndServer.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.io.*;
import javax.swing.*;


class HelloServant extends HelloWorldPOA {
    private RelayServer parent;

    HelloServant(RelayServer parentGUI) {
        // store reference to parent GUI
        parent = parentGUI;
    }

    public String hello_world() {
        parent.addMessage("hello_world called by relay.\n    Replying with message...\n\n");

        return "Hello World!!";
    }
}


public class RelayServer extends JFrame {
    private JTextArea textarea;

    public RelayServer(String[] args){
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
            JPanel panel = new JPanel();

            panel.add(scrollpane);
            getContentPane().add(panel, "Center");

            setSize(400, 500);
            setTitle("Relay Demo Server");

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
                new  RelayServer(arguments).setVisible(true);
            }
        });
    }
}