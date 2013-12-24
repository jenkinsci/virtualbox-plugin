package hudson.plugins.virtualbox;

/* CJ: Extensions for RunListener*/
import hudson.Extension;
import hudson.model.listeners.RunListener;
import hudson.slaves.*;
import hudson.model.Node;

import java.io.Serializable;
import hudson.model.*;


/* CJ: Start normal import*/
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;

import org.kohsuke.stapler.DataBoundConstructor;

// http://jenkins.361315.n4.nabble.com/Take-a-slave-off-line-if-a-job-fails-td377500.html

/**
 * @author Christian John
 */
@Extension
public class VirtualBoxListener extends RunListener<Run> implements Serializable {

    public VirtualBoxListener() {
        super(Run.class);
    }


	/* onCompleted EP: RunListener*/
    @Override
    public void onCompleted(Run run, TaskListener listener) {
	  // Aktuelles Computer-Objekt abgreifen
	  Computer computer = run.getExecutor().getOwner();
	  // Versuchen zu beenden
	  try{
		// Erst einmal den Build-Server offline-Setzen
	    computer.setTemporarilyOffline(true,null);
		// Und noch mal zur Sicherheit hinterher
	    computer.cliDisconnect("Sicherheit");
		} catch(Exception e)
		{
		  listener.getLogger().println("Fehler beim Shutdown!");
		}
		
	   	
    //	VirtualBoxMachine machine = VirtualBoxPlugin.getVirtualBoxMachine(super.getHostName(), super.getVirtualMachineName());
     //VirtualBoxUtils.stopVm(run.getExecutor().getOwner().currentComputer(), new VirtualBoxTaskListenerLog(listener, "[VirtualBox] "));
	  //slave.getComputer().setTemporarilyOffline(true,null);
	  listener.getLogger().println("Bye bye VM!");

	}

}
