// BailiffInterface.java
// 2018-08-16/fki Refactored for v13.

package tag.bailiff;

import tag.dexter.Dexter;
import java.util.*;

/**
 * This interface is for the Bailiff's clients. The clients are mobile
 * code which move into the Bailiff's JVM for execution.
 */
public interface BailiffInterface
        extends
        java.rmi.Remote {
    /**
     * Returns a string which confirms communication with the Bailiff
     * service instance.
     */
    public String ping()
            throws
            java.rmi.RemoteException;

    /**
     * Returns a property of the Bailiff.
     *
     * @param key The case-insensitive property key to retrieve.
     * @return The property string or null.
     */
    public String getProperty(String key)
            throws
            java.rmi.RemoteException;

    /**
     * The entry point for mobile code.
     * The client sends and object (itself perhaps), a string
     * naming the callback method and an array of arguments which must
     * map against the parameters of the callback method.
     *
     * @param obj  The object (to execute).
     * @param cb   The name of the method to call as the program of obj.
     * @param args The parameters for the callback method. Note that if
     *             the method has a signature without arguments the value of args
     *             should be an empty array. Setting args to null will not work.
     * @throws java.rmi.RemoteException        Thrown if there is an RMI problem.
     * @throws java.lang.NoSuchMethodException Thrown if the proposed
     *                                         callback is not found (which happen if the name is spelled wrong,
     *                                         the number of arguments is wrong or are of the wrong type).
     */
    public void migrate(Object obj, String cb, Object[] args)
            throws
            java.rmi.RemoteException,
            java.lang.NoSuchMethodException;

    public void addDexter(Dexter dexter)
            throws
            java.rmi.RemoteException,
            java.lang.NoSuchMethodException;

    public void delDexter(Dexter dexter)
            throws
            java.rmi.RemoteException,
            java.lang.NoSuchMethodException;

    public Boolean isSafe()
            throws
            java.rmi.RemoteException,
            java.lang.NoSuchMethodException;

    public Integer numberOfDexters()
            throws
            java.rmi.RemoteException,
            java.lang.NoSuchMethodException;

    public HashSet<UUID> getDexters()
            throws
            java.rmi.RemoteException,
            java.lang.NoSuchMethodException;

    public Boolean tag(UUID uuid)
            throws
            java.rmi.RemoteException,
            java.lang.NoSuchMethodException;
}
