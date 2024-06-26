package jmri.jmrix.jmriclient;

/**
 * Layout interface, similar to command station
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface JMRIClientInterface {

    void addJMRIClientListener(JMRIClientListener l);

    void removeJMRIClientListener(JMRIClientListener l);

    boolean status();   // true if the implementation is operational

    void sendJMRIClientMessage(JMRIClientMessage m, JMRIClientListener l);  // 2nd arg gets the reply

}
