package jmri.jmrix;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.beans.Bean;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.SystemConnectionMemo;

/**
 * Manager for SystemConnectionMemos. Manages SystemConnectionMemos and
 * SystemConnectionMemo registration with the InstanceManager, ensuring that no
 * two SystemConnectionMemos have the same username or system connection prefix.
 * Also provides an object that other objects can listen to for notification of
 * changes in SystemConnectionMemos.
 *
 * @author Randall Wood      Copyright 2017
 * @author Daniel Bergqvist  Copyright 2022
 */
public class SystemConnectionMemoManager extends Bean implements InstanceManagerAutoDefault {

    /**
     * Property name change fired when a connection is registered. The fired
     * event has a null old value and the added connection as the new value.
     */
    public final static String CONNECTION_ADDED = "ConnectionAdded";
    /**
     * Property name change fired when a connection is deregistered. The fired
     * event has the removed connection as the old value and a null new value.
     */
    public final static String CONNECTION_REMOVED = "ConnectionRemoved";

    /**
     * Register a SystemConnectionMemo in the InstanceManager.
     *
     * @param memo the SystemConnectionMemo to register
     */
    public void register(SystemConnectionMemo memo) {
        log.debug("registering connection {}", memo.getUserName());

        // check for special case
        List<SystemConnectionMemo> list = InstanceManager.getList(SystemConnectionMemo.class);
        int size = list.size();
        if (size > 0 && (list.get(size - 1) instanceof InternalSystemConnectionMemo)) {
            // last is internal, so insert before that one
            log.debug("   putting one before end");
            SystemConnectionMemo internal = list.get(size - 1);
            InstanceManager.deregister(internal, SystemConnectionMemo.class);
            InstanceManager.store(memo, SystemConnectionMemo.class);
            InstanceManager.store(internal, SystemConnectionMemo.class);
        } else {
            // just add on end
            InstanceManager.store(memo, SystemConnectionMemo.class);
        }
        log.trace("fire CONNECTION_ADDED for {}", memo);
        this.firePropertyChange(CONNECTION_ADDED, null, memo);
    }

    public void deregister(SystemConnectionMemo memo) {
        InstanceManager.deregister(memo, SystemConnectionMemo.class);
        log.trace("fire CONNECTION_REMOVED for {}", memo);
        firePropertyChange(CONNECTION_REMOVED, memo, null);
    }

    /**
     * For a given System UserName AND System Prefix, get the Connection Memo.
     * Both must match to return the memo.
     * @param systemPrefix System Prefix to search for.
     * @param userName system UserName to search for.
     * @return connection memo, else null if no memo located.
     */
    @CheckForNull
    public synchronized SystemConnectionMemo getSystemConnectionMemo(@Nonnull String systemPrefix, @Nonnull String userName) {
        for (SystemConnectionMemo memo: InstanceManager.getList(SystemConnectionMemo.class)) {
            if (memo.getSystemPrefix().equals(systemPrefix) && memo.getUserName().equals(userName)) {
                return memo;
            }
        }
        return null;
    }

    /**
     * For a given System UserName, get the Connection Memo.
     * @param userName system UserName to search for.
     * @return connection memo, else null if no memo located.
     */
    @CheckForNull
    public synchronized SystemConnectionMemo getSystemConnectionMemoForUserName(@Nonnull String userName) {
        for (SystemConnectionMemo memo: InstanceManager.getList(SystemConnectionMemo.class)) {
            if (memo.getUserName().equals(userName)) {
                return memo;
            }
        }
        return null;
    }

    /**
     * For a given System Prefix, get the Connection Memo.
     * @param systemPrefix System Prefix to search for.
     * @return connection memo, else null if no memo located.
     */
    @CheckForNull
    public synchronized SystemConnectionMemo getSystemConnectionMemoForSystemPrefix(@Nonnull String systemPrefix) {
        for (SystemConnectionMemo memo: InstanceManager.getList(SystemConnectionMemo.class)) {
            if (memo.getSystemPrefix().equals(systemPrefix)) {
                return memo;
            }
        }
        return null;
    }

    /**
     * Check if a system connection user name is available to be used.
     *
     * @param userName the user name to check
     * @return true if available; false if already in use
     */
    public synchronized boolean isUserNameAvailable(@Nonnull String userName) {
        return InstanceManager.getList(SystemConnectionMemo.class).stream().noneMatch((memo) -> (userName.equals(memo.getUserName())));
    }

    /**
     * Check if a system connection prefix for the system names of other objects
     * is available to be used.
     *
     * @param systemPrefix the system prefix to check
     * @return true if available; false if already in use
     */
    public synchronized boolean isSystemPrefixAvailable(@Nonnull String systemPrefix) {
        return InstanceManager.getList(SystemConnectionMemo.class).stream().noneMatch((memo) -> (memo.getSystemPrefix().equals(systemPrefix)));
    }

    /**
     * Get the default instance of this manager.
     *
     * @return the default instance, created if needed
     */
    public static SystemConnectionMemoManager getDefault() {
        return InstanceManager.getDefault(SystemConnectionMemoManager.class);
    }

    /**
     * Find the connection by its name.
     * <p>
     * Example:<br>
     * LocoNetSystemConnectionMemo memo = getConnection("L2", LocoNetSystemConnectionMemo.class);
     * </p>
     * @param <T>                   The type of connection
     * @param systemConnectionName  The connection name
     * @param clazz                 The class of the connection type
     * @return                      The memo if found, null otherwise
     */
    @CheckForNull
    public static <T extends SystemConnectionMemo> T getConnection(
            @Nonnull String systemConnectionName,
            @Nonnull Class<T> clazz) {

        List<T> systemConnections = jmri.InstanceManager.getList(clazz);

        for (T memo : systemConnections) {
            if (memo.getSystemPrefix().equals(systemConnectionName)) {
                return memo;
            }
        }

        // Connection is not found
        return null;
    }

    /**
     * Find the connection by its user name.
     * <p>
     * Example:<br>
     * LocoNetSystemConnectionMemo memo = getConnectionByUserName("LocoNet", LocoNetSystemConnectionMemo.class);
     * </p>
     * @param <T>       The type of connection
     * @param userName  The connection user name
     * @param clazz     The class of the connection type
     * @return          The memo if found, null otherwise
     */
    @CheckForNull
    public static <T extends SystemConnectionMemo> T getConnectionByUserName(
            @Nonnull String userName,
            @Nonnull Class<T> clazz) {

        List<T> systemConnections = jmri.InstanceManager.getList(clazz);

        for (T memo : systemConnections) {
            if (memo.getUserName().equals(userName)) {
                return memo;
            }
        }

        // Connection is not found
        return null;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SystemConnectionMemoManager.class);
}
