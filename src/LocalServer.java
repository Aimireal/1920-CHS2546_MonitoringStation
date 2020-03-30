import AssignmentTwo.*;
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

public class LocalServer
{
    //args
    private String serverName;
    private String serverLocation;
    private String monitoringCentre;

    MonitoringCentre monitoringCentreServant;

    public LocalServer(String[] args)
    {
        if(args.length >= 3)
        {
            serverName = args[0];
            serverLocation = args[1];
            monitoringCentre = args[2];
        }
        try
        {
            //Create and initialize the ORB
            ORB orb = ORB.init(args, null);

            //Get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

        } catch(Exception e)
        {
            System.err.println("Error: " + e);
            e.printStackTrace(System.out);
        }
    }
}
