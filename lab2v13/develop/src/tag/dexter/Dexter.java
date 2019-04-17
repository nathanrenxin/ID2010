// Dexter.java
// 2018-08-15/fki Refactored from v11

package tag.dexter;

import java.io.*;        // TODO remove the asterisk
import java.util.*;

import net.jini.core.lookup.*;
import net.jini.lookup.*;

import tag.bailiff.BailiffInterface;

/**
 * Dexter jumps around randomly among the Bailiffs. Dexter can be used
 * to test that the system is operating, and as a template for more
 * evolved agents. Since objects of class Dexter move between JVMs, it
 * must be implement the Serializable marker interface.
 */
public class Dexter implements Serializable {
    /**
     * The string name of the Bailiff service interface, used when
     * querying the Jini lookup server.
     */
    protected static final String bfiName = "tag.bailiff.BailiffInterface";
    private final UUID uuid;
    /**
     * The debug flag controls the amount of diagnostic info we put out.
     */
    protected boolean debug = false;
    /**
     * Dexter uses a Jini ServiceDiscoveryManager to find Bailiffs. The
     * SDM is not serializable so it must recreated each time a Dexter
     * moves to a different Bailiff. By marking the reference variable
     * as transient, we indicate to the compiler that we are aware of
     * that whatever the variable refers to, it will not be serialized.
     */
    protected transient ServiceDiscoveryManager SDM;
    /**
     * This Jini service template is created in Dexter's constructor and
     * used in the topLevel method to find Bailiffs. The service
     * template IS serializable so Dexter only needs to instantiate it
     * once.
     */
    protected ServiceTemplate bailiffTemplate;
    /**
     * Identification string used in debug messages.
     */
    private String id = "anon";
    /**
     * Default sleep time so that we have time to track what it does.
     */
    private long restraintSleepMs = 5000;
    /**
     * The jump count variable is incremented each time method topLevel
     * is entered. Its value is printed by the debugMsg routine.
     */
    private int jumpCount = 0;
    /**
     * The default sleep time between subsequent queries of a Jini
     * lookup server.
     */
    private long retrySleep = 20 * 1000; // 20 seconds
    /**
     * The maximum number of results we are interested when asking the
     * Jini lookup server for present bailiffs.
     */
    private int maxResults = 8;
    private Boolean tagged = false;
    private Boolean isMoving = false;
    private BailiffInterface myBailiff = null;
    private BailiffInterface exBailiff = null;
    private long stayPeriod = 12 * 1000;

    /**
     * Creates a new Dexter. All the constructor needs to do is to
     * instantiate the service template.
     *
     * @throws ClassNotFoundException Thrown if the class for the Bailiff
     *                                service interface could not be found.
     */
    public Dexter()
            throws
            java.lang.ClassNotFoundException {

        // The Jini service template bailiffTemplate is used to query the
        // Jini lookup server for services which implement the
        // BailiffInterface. The string name of that interface is passed
        // in the bfi argument. At this point we only create and configure
        // the service template, no query has yet been issued.

        bailiffTemplate =
                new ServiceTemplate
                        (null,
                                new Class[]{java.lang.Class.forName(bfiName)},
                                null);

        uuid = UUID.randomUUID();
    }

    private static void showUsage() {
        String[] msg = {
                "Usage: {?,-h,-help}|[-debug][-id string][-rs ms][-qs ms][-mr n]",
                "? -h -help   Show this text",
                "-debug       Enable trace and diagnostic messages",
                "-id  string  Set the id string printed by debug messages",
                "-rs  ms      Set the restraint sleep in milliseconds",
                "-qs  ms      Set the Jini lookup query retry delay",
                "-sp  ms      Set staying period on current bailiff",
                "-it          Set this agent it",
                "-mr  n       Set the Jini lookup query max results limit"
        };
        for (String s : msg)
            System.out.println(s);
    }

    public static void main(String[] argv)
            throws
            java.io.IOException, java.lang.ClassNotFoundException {

        // Make a new Dexter and configure it from commandline arguments.

        Dexter dx = new Dexter();

        // Parse and act on the commandline arguments.

        int state = 0;

        for (String av : argv) {

            switch (state) {

                case 0:
                    if (av.equals("?") || av.equals("-h") || av.equals("-help")) {
                        showUsage();
                        return;
                    } else if (av.equals("-debug"))
                        dx.setDebug(true);
                    else if (av.equals("-id"))
                        state = 1;
                    else if (av.equals("-rs"))
                        state = 2;
                    else if (av.equals("-qs"))
                        state = 3;
                    else if (av.equals("-mr"))
                        state = 4;
                    else if (av.equals("-sp"))
                        state = 5;
                    else if (av.equals("-it"))
                        dx.setTag(true);
                    else {
                        System.err.println("Unknown commandline argument: " + av);
                        return;
                    }
                    break;

                case 1:
                    dx.setId(av);
                    state = 0;
                    break;

                case 2:
                    dx.setRestraintSleep(Long.parseLong(av));
                    state = 0;
                    break;

                case 3:
                    dx.setRetrySleep(Long.parseLong(av));
                    state = 0;
                    break;

                case 4:
                    dx.setMaxResults(Integer.parseInt(av));
                    state = 0;
                    break;

                case 5:
                    dx.setStayPeriod(Integer.parseInt(av));
                    state = 0;
                    break;
            }    // switch
        }    // for all commandline arguments

        dx.topLevel();        // Start the Dexter

    } // main

    /**
     * Sets the id string of this Dexter.
     *
     * @param id The id string. A null argument is replaced with the
     *           empty string.
     */
    public void setId(String id) {
        this.id = (id != null) ? id : "";
    }

    /**
     * Sets the restraint sleep duration.
     *
     * @param ms The number of milliseconds in restraint sleep.
     */
    public void setRestraintSleep(long ms) {
        restraintSleepMs = Math.max(0, ms);
    }

    /**
     * Sets the query retry sleep duration.
     *
     * @param ms The number of milliseconds between each query.
     */
    public void setRetrySleep(long ms) {
        retrySleep = Math.max(0, ms);
    }

    public void setStayPeriod(long ms) {
        stayPeriod = Math.max(0, ms);
    }

    /**
     * Sets the maximum number of results accepted from the Jini lookup
     * server.
     *
     * @param n The maximum number of results.
     */
    public void setMaxResults(int n) {
        maxResults = Math.max(0, n);
    }

    /**
     * Sets or clears the global debug flag. When enabled, trace and
     * diagnostic messages are printed on stdout.
     */
    public void setDebug(boolean isDebugged) {
        debug = isDebugged;
    }

    /**
     * Outputs a diagnostic message on standard output. This will be on
     * the host of the launching JVM before Dexter moves. Once he has migrated
     * to another Bailiff, the text will appear on the console of that Bailiff.
     *
     * @param msg The message to print.
     */
    protected void debugMsg(String msg) {
        if (debug)
            System.out.printf("%s(%d):%s%n", id, jumpCount, msg);
    }

    protected void logging(String msg) {
        System.out.printf("%s(%d):%s%n", id, jumpCount, msg);
    }

    /**
     * Sleep for the given number of milliseconds.
     *
     * @param ms The number of milliseconds to sleep.
     */
    protected void snooze(long ms) {
        try {
            Thread.currentThread().sleep(ms);
        } catch (java.lang.InterruptedException e) {
        }
    }

    /**
     * This is Dexter's main program once he is on his way. In short, he
     * gets himself a service discovery manager and asks it about Bailiffs.
     * If the list is long enough, he then selects one randomly and pings it.
     * If the ping returned without a remote exception, Dexter then tries
     * to migrate to that Bailiff. If the ping or the migration fails, Dexter
     * gives up on that Bailiff and tries another.
     */

    public void topLevel()
            throws
            java.io.IOException {
        isMoving = false;
        jumpCount++;

        if(jumpCount > 1)
            logging("I am here, jumped to a new bailiff!");

        Random rnd = new Random();

        // Create a Jini service discovery manager to help us interact with
        // the Jini lookup service.
        SDM = new ServiceDiscoveryManager(null, null);

        // Loop forever until we have successfully jumped to a Bailiff.
        for (; ; ) {

            if(tagged && myBailiff != null){
                logging("I am IT, hunting...");
                try {
                    List<UUID> targets = new ArrayList<>(myBailiff.getDexters());
                    Collections.shuffle(targets);
                    if(targets.size()>1){
                        for (UUID target: targets){
                            if(!target.equals(getUuid())){
                                if(myBailiff.tag(target)){
                                    this.setTag(false);
                                    break;
                                }
                            }
                        }
                    }
                } catch (java.rmi.RemoteException rex) {
                    if (debug)
                        rex.printStackTrace();
                } catch (java.lang.NoSuchMethodException nmx) {
                    if (debug)
                        nmx.printStackTrace();
                }
            }

            ServiceItem[] svcItems;    // holds results from the Jini lookup server

            long retryInterval = 0;    // incremented when no Bailiffs are found

            // Sleep a bit so that humans can keep up.

            debugMsg("Is here - entering restraint sleep.");
            snooze(restraintSleepMs);
            debugMsg("Leaving restraint sleep.");

            // Try to find Bailiffs using the Jini lookup service.
            // The loop keeps going until we get a non-empty response.
            // If no results are found, we sleep a bit between attempts.

            do {

                if (0 < retryInterval) {
                    debugMsg("No Bailiffs detected - sleeping.");
                    snooze(retryInterval);
                    debugMsg("Waking up, looking for Bailiffs.");
                }

                // Put our query, expressed as a service template, to the Jini
                // service discovery manager.

                svcItems = SDM.lookup(bailiffTemplate, maxResults, null);
                retryInterval = retrySleep;

                // If no lookup servers or bailiffs are found, go back up to
                // the beginning of the loop, sleep a bit, and then try again.

            } while (svcItems.length == 0);

            // Now, at least one Bailiff has been found.

            debugMsg("Found " + svcItems.length + " Bailiffs");

            // Enter a loop in which we:
            // - randomly pick one Bailiff
            // - pings it to see if it is alive
            // - migrate to it, or try another one

            int nofItems = svcItems.length; // nof items remaining

            if (0 < nofItems) {

                BailiffInterface targetBailiff = getTargetBailiff(svcItems, myBailiff);

                try {
                    if (targetBailiff==null) {
                        debugMsg("All Bailiffs failed.");
                        //debugMsg("Entering restraint sleep.");
                        //snooze(restraintSleepMs);
                        //debugMsg("Leaving restraint sleep.");
                    } else if (targetBailiff.equals(myBailiff)){
                        debugMsg("Staying at current Bailiff");
                        snooze(stayPeriod);
                    } else if (tagged && myBailiff != null && myBailiff.numberOfDexters()>1){
                        debugMsg("Got tagged while searching for bailiff to jump!");
                        snooze(restraintSleepMs);
                    }
                    else {

                        logging("Trying to jump...");

                        try {
                            if (myBailiff != null) {
                                myBailiff.delDexter(this);
                            }
                            exBailiff = myBailiff;
                            myBailiff = targetBailiff;
                            isMoving = true;
                            targetBailiff.migrate(this, "topLevel", new Object[]{});
                            // SUCCESS
                            SDM.terminate();    // shut down Service Discovery Manager
                            return;        // return and end this thread
                        } catch (java.rmi.RemoteException rex) {
                            if (debug)
                                rex.printStackTrace();
                        } catch (java.lang.NoSuchMethodException nmx) {
                            if (debug)
                                nmx.printStackTrace();
                        }

                        try {
                            myBailiff = exBailiff;
                            isMoving = false;
                            if (myBailiff != null) {
                                myBailiff.addDexter(this);
                            }
                            debugMsg("Jump failed!");
                            //debugMsg("Entering restraint sleep.");
                            //snooze(restraintSleepMs);
                            //debugMsg("Leaving restraint sleep.");
                        } catch (java.rmi.RemoteException rex) {
                            if (debug)
                                rex.printStackTrace();
                        } catch (java.lang.NoSuchMethodException nmx) {
                            if (debug)
                                nmx.printStackTrace();
                        }
                    }
                } catch (java.rmi.RemoteException rex) {
                    if (debug)
                        rex.printStackTrace();
                } catch (java.lang.NoSuchMethodException nmx) {
                    if (debug)
                        nmx.printStackTrace();
                }
            }    // while candidates remain
        } // for ever
    }   // topLevel

    public UUID getUuid() {
        return uuid;
    }

    public Boolean isTagged() {
        return tagged;
    }
    // The main method is only used by the initial launch. After the
    // first jump, Dexter always restarts in method topLevel.

    public Boolean setTag(Boolean tag) {
        if (tag){
            if(isMoving){
                logging("I am moving, you can not tag me!");
                return false;
            } else {
                this.tagged = tag;
                logging("I am tagged!");
                return true;
            }
        } else {
            this.tagged = tag;
            logging("I am untagged!");
            return true;
        }
    }

    public BailiffInterface getTargetBailiff(ServiceItem[] svcItems, BailiffInterface currentBfi) {
        Map<BailiffInterface, Integer> bailiffs = new HashMap<>();
        Integer counter = 0;
        BailiffInterface unsafeBfi = null;

        for (ServiceItem svcItem: svcItems){
            Boolean accepted = false;
            Object obj = svcItem.service; // Get the service object
            BailiffInterface bfi = null;

            debugMsg("Trying to ping...");

            if (obj instanceof BailiffInterface) {
                bfi = (BailiffInterface) obj;
                try {
                    String response = bfi.ping(); // Ping it
                    debugMsg(response);
                    accepted = true;    // It worked!
                    counter += 1;
                } catch (java.rmi.RemoteException rex) {
                    debugMsg("Ping fail: " + bfi);
                }
            }

            debugMsg(accepted ? "Accepted." : "Not accepted.");

            try {
                if(tagged) {
                    if (accepted) {
                        if(bfi.equals(currentBfi))
                            bailiffs.put(bfi, bfi.numberOfDexters() -1);
                        else
                            bailiffs.put(bfi, bfi.numberOfDexters());
                    }
                } else {
                    if (accepted && bfi.isSafe()) {
                        if(bfi.equals(currentBfi))
                            bailiffs.put(bfi, bfi.numberOfDexters() -1);
                        else
                            bailiffs.put(bfi, bfi.numberOfDexters());
                    } else if (accepted && !bfi.isSafe()){
                        unsafeBfi = bfi;
                    }
                }
            } catch (java.rmi.RemoteException rex) {
                if (debug)
                    rex.printStackTrace();
            } catch (java.lang.NoSuchMethodException nmx) {
                if (debug)
                    nmx.printStackTrace();
            }
        }

        if (counter == 0) {
            return null;
        } else if (bailiffs.size()==0 && currentBfi != null){
            return currentBfi;
        } else if (bailiffs.size()==0 && currentBfi == null){
            return unsafeBfi;
        } else {

            List<BailiffInterface> targets = new ArrayList<>(bailiffs.keySet());
            Collections.shuffle(targets);

            if (tagged) {
                BailiffInterface result = bailiffs.containsKey(currentBfi) ? currentBfi : null;
                Integer max = bailiffs.containsKey(currentBfi) ? bailiffs.get(currentBfi) :-1;
                for (BailiffInterface b: targets){
                    if (bailiffs.get(b)> max){
                        result = b;
                        max = bailiffs.get(b);
                    }
                }
                return result;
            }
            else {
                BailiffInterface result = bailiffs.containsKey(currentBfi) ? currentBfi : null;
                Integer min = bailiffs.containsKey(currentBfi) ? bailiffs.get(currentBfi) :99;
                for (BailiffInterface b: targets){
                    if (bailiffs.get(b)< min){
                        result = b;
                        min = bailiffs.get(b);
                    }
                }
                return result;
            }

        }
    }
}
