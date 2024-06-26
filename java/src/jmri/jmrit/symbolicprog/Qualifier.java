package jmri.jmrit.symbolicprog;

/**
 * Define capability to watch other things and "Qualify" CVs and Variables.
 *
 * @author Bob Jacobsen Copyright (C) 2014
 *
 */
public interface Qualifier {

    /**
     * Process the current value and do whatever is needed.
     */
    void update();

    /**
     * Check whether this Qualifier is currently in the OK, qualified-to-act
     * state.
     *
     * @return true if this Qualifier is currently saying OK
     */
    boolean currentDesiredState();

    /**
     * Drive the available or not state of the qualified object.
     * <p>
     * Subclasses implement this to control a specific type of qualified object,
     * like a Variable or Pane.
     * @param enable true to drive, else false.
     */
    void setWatchedAvailable(boolean enable);

}
