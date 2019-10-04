package hudson.plugins.virtualbox;

import hudson.util.Secret;
import org.virtualbox_6_0.*;
import java.util.ArrayList;
import java.util.List;

public final class VirtualBoxControlV60 implements VirtualBoxControl {

    private final VirtualBoxManager manager;
    private final IVirtualBox vbox;

    public VirtualBoxControlV60(String hostUrl, String userName, Secret password) {
        manager = VirtualBoxManager.createInstance(null);
        manager.connect(hostUrl, userName, password.getPlainText());
        vbox = manager.getVBox();
    }

    public synchronized void disconnect() {
        try {
            manager.disconnect();
        } catch (VBoxException e) {}
    }

    public synchronized boolean isConnected() {
        try {
            vbox.getVersion();
            return true;
        } catch (VBoxException e) {
            return false;
        }
    }


    /**
     * Get virtual machines installed on specified host.
     *
     * @param host VirtualBox host
     * @return list of virtual machines installed on specified host
     */
    public synchronized List<VirtualBoxMachine> getMachines(VirtualBoxCloud host, VirtualBoxLogger log) {
        List<VirtualBoxMachine> result = new ArrayList<VirtualBoxMachine>();
        for (IMachine machine : vbox.getMachines()) {
            result.add(new VirtualBoxMachine(host, machine.getName()));
        }
        return result;
    }

    /**
     * Starts specified VirtualBox virtual machine.
     *
     * @param vbMachine virtual machine to start
     * @param type      session type (can be headless, vrdp, gui, sdl)
     * @param log
     * @return result code
     */
    public synchronized long startVm(VirtualBoxMachine vbMachine, String type, VirtualBoxLogger log) {
        IMachine machine = vbox.findMachine(vbMachine.getName());
        if (null == machine) {
            log.logFatalError("Cannot find node: " + vbMachine.getName());
            return -1;
        }

        // states diagram: https://www.virtualbox.org/sdkref/_virtual_box_8idl.html#80b08f71210afe16038e904a656ed9eb
        MachineState state = machine.getState();
        ISession session;
        IProgress progress;

        // wait for transient states to finish
        while (state.value() >= MachineState.FirstTransient.value() && state.value() <= MachineState.LastTransient.value()) {
            log.logInfo("node " + vbMachine.getName() + " in state " + state.toString());
            try {
                wait(1000);
            } catch (InterruptedException e) {}
            state = machine.getState();
        }

        if (MachineState.Running == state) {
            log.logInfo("node " + vbMachine.getName() + " in state " + state.toString());
            log.logInfo("node " + vbMachine.getName() + " started");
            return 0;
        }

        if (MachineState.Stuck == state || MachineState.Paused == state) {
            log.logInfo("starting node " + vbMachine.getName() + " from state " + state.toString());
            try {
                session = getSession(machine);
            } catch (Exception e) {
                log.logFatalError("node " + vbMachine.getName() + " openMachineSession: " + e.getMessage());
                return -1;
            }

            progress = null;
            if (MachineState.Stuck == state) {
                // for Stuck state call powerDown and go to PoweredOff state
                progress = session.getConsole().powerDown();
            } else if (MachineState.Paused == state) {
                // from Paused call resume
                session.getConsole().resume();
            }

            long result = 0; // success
            if (null != progress) {
                progress.waitForCompletion(-1);
                result = progress.getResultCode();
            }

            releaseSession(session, machine);
            if (0 != result) {
                log.logFatalError("node " + vbMachine.getName() + " error: " + getVBProcessError(progress));
                return -1;
            }

            if (MachineState.Stuck != state) {
                log.logInfo("node " + vbMachine.getName() + " started");
                return 0;
            }
            // continue from PoweredOff state
            state = machine.getState(); // update state
        }

        log.logInfo("starting node " + vbMachine.getName() + " from state " + state.toString());

        // powerUp from Saved, Aborted or PoweredOff states
        session = getSession(null);
        String env = "";
        progress = machine.launchVMProcess(session, type, env);
        progress.waitForCompletion(-1);
        long result = progress.getResultCode();
        releaseSession(session, machine);

        if (0 != result) {
            log.logFatalError("node " + vbMachine.getName() + " error: " + getVBProcessError(progress));
        } else {
            log.logInfo("node " + vbMachine.getName() + " started");
        }

        return result;
    }

    /**
     * Stops specified VirtualBox virtual machine.
     *
     * @param vbMachine virtual machine to stop
     * @param log
     * @return result code
     */
    public synchronized long stopVm(VirtualBoxMachine vbMachine, String stopMode, VirtualBoxLogger log) {
        IMachine machine = vbox.findMachine(vbMachine.getName());
        VirtualBoxCloud cloud = vbMachine.getHost();

        if (null == machine) {
            log.logFatalError("Cannot find node: " + vbMachine.getName());
            return -1;
        }

        // states diagram: https://www.virtualbox.org/sdkref/_virtual_box_8idl.html#80b08f71210afe16038e904a656ed9eb
        MachineState state = machine.getState();
        ISession session;
        IProgress progress;

        // wait for transient states to finish
        while (state.value() >= MachineState.FirstTransient.value() && state.value() <= MachineState.LastTransient.value()) {
            log.logInfo("node " + vbMachine.getName() + " in state " + state.toString());
            try {
                wait(1000);
            } catch (InterruptedException e) {}
            state = machine.getState();
        }

        log.logInfo("stopping node " + vbMachine.getName() + " from state " + state.toString());

        if (MachineState.Aborted == state || MachineState.PoweredOff == state
                || MachineState.Saved == state) {
            log.logInfo("node " + vbMachine.getName() + " stopped");
            return 0;
        }

        try {
            session = getSession(machine);
        } catch (Exception e) {
            log.logFatalError("node " + vbMachine.getName() + " openMachineSession: " + e.getMessage());
            return -1;
        }

        if (MachineState.Stuck == state || "powerdown".equals(stopMode)) {
            // for Stuck state call powerDown and go to PoweredOff state
            progress = session.getConsole().powerDown();
        } else {
            // Running or Paused
            progress = session.getMachine().saveState();
        }

        progress.waitForCompletion(-1);
        long result = progress.getResultCode();

        releaseSession(session, machine);

        if (0 != result) {
            log.logFatalError("node " + vbMachine.getName() + " error: " + getVBProcessError(progress));
        } else {
            log.logInfo("node " + vbMachine.getName() + " stopped");
            cloud.decrementActiveMachines();
        }

        return result;
    }

    /**
     * MAC Address of specified virtual machine.
     *
     * @param vbMachine virtual machine
     * @return MAC Address of specified virtual machine
     */
    public synchronized String getMacAddress(VirtualBoxMachine vbMachine, VirtualBoxLogger log) {
        IMachine machine = vbox.findMachine(vbMachine.getName());
        String macAddress = machine.getNetworkAdapter(0L).getMACAddress();
        return macAddress;
    }

    private String getVBProcessError(IProgress progress) {
        if (0 == progress.getResultCode()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("");
        IVirtualBoxErrorInfo errInfo = progress.getErrorInfo();
        while (null != errInfo) {
            sb.append(errInfo.getText());
            sb.append("\n");
            errInfo = errInfo.getNext();
        }
        return sb.toString();
    }

    private boolean isTransientState(SessionState state) {
        return SessionState.Spawning == state || SessionState.Unlocking == state;
    }

    private ISession getSession(IMachine machine) {
        ISession s = manager.getSessionObject();
        if (null != machine) {
            machine.lockMachine(s, LockType.Shared);
            while (isTransientState(machine.getSessionState())) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}
            }
        }

        while (isTransientState(s.getState())) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
        }

        return s;
    }

    private void releaseSession(ISession s, IMachine machine) {
        while (isTransientState(machine.getSessionState()) || isTransientState(s.getState())) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
        }

        try {
            s.unlockMachine();
        } catch (VBoxException e) {}

        while (isTransientState(machine.getSessionState()) || isTransientState(s.getState())) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
        }
    }
}
