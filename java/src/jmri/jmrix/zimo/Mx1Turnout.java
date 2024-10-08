package jmri.jmrix.zimo;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * New Zimo Binary implementation of the Turnout interface
 *
 * @author Kevin Dickerson Copyright (C) 2014
 */
public class Mx1Turnout extends AbstractTurnout /*implements Mx1TrafficListener*/ {

    // Private data member to keep track of what turnout we control.
    int _number;
    private final Mx1TrafficController tc;

    /**
     * Mx1 turnouts use any address allowed as an accessory decoder address on
     * the particular command station.
     *
     * @param number the address
     * @param tc     the controller for traffic to the layout
     * @param p      the system connection prefix
     */
    public Mx1Turnout(int number, Mx1TrafficController tc, String p) {
        super(p + "T" + number);
        _number = number;
        this.tc = tc;
        //tc.addMx1Listener(Mx1Interface.TURNOUTS, null);
    }

    public int getNumber() {
        return _number;
    }

    /**
     * {@inheritDoc}
     * Sends a formatted DCC packet
     */
    @Override
    protected void forwardCommandChangeToLayout(int newState) {
        // sort out states
        if ((newState & Turnout.CLOSED) != 0) {
            // first look for the double case, which we can't handle
            if ((newState & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN {}", newState); // NOI18N
            } else {
                // send a CLOSED command
                forwardToCommandStation(jmri.Turnout.THROWN);
            }
        } else {
            // send a THROWN command
            forwardToCommandStation(jmri.Turnout.CLOSED);
        }
    }

    void forwardToCommandStation(int state) {
        Mx1Message m = Mx1Message.getSwitchMsg(_number, state, true);
        tc.sendMx1Message(m, null);
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean pushButtonLockout) {
    }

    private final static Logger log = LoggerFactory.getLogger(Mx1Turnout.class);

}
