package jmri;

import java.beans.PropertyChangeListener;
import java.util.EnumSet;

/**
 * Interface for allocating {@link Throttle} objects.
 * <p>
 * "Address" is interpreted in the context of the DCC implementation. Different
 * systems will distinguish between short and long addresses in different ways.
 * <p>
 * When the allocated Throttle is no longer needed, it is told that it's
 * released. If a specific ThrottleManager and/or Throttle implementation needs
 * to keep track of that operation, it is handled internally.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Glen Oberhauser
 * @author Bob Jacobsen Copyright 2006
 */
public interface ThrottleManager {

    /**
     * Request a throttle from a given RosterEntry. When the decoder address is
     * located, the ThrottleListener gets a callback via the
     * ThrottleListener.notifyThrottleFound method.
     *
     * @param re desired RosterEntry
     * @param l  ThrottleListener awaiting notification of a found throttle
     * @param canHandleDecisions true if the ThrottleListener has a mechanism for dealing with
     *        Share / Steal decisions, else false
     * @return true if the request will continue, false if the request will not
     *         be made; false may be returned if a the throttle is already in
     *         use
     */
    boolean requestThrottle(BasicRosterEntry re, ThrottleListener l, boolean canHandleDecisions);


    /**
     * Request a throttle, given a decoder address. When the decoder address is
     * located, the ThrottleListener gets a callback via the
     * ThrottleListener.notifyThrottleFound method.
     * <p>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form,
     * and assumes that the hardware makes and steal / share decisions.
     *
     * @param address desired decoder address
     * @param l       ThrottleListener awaiting notification of a found throttle
     * @return true if the request will continue, false if the request will not
     *         be made; false may be returned if a the throttle is already in
     *         use
     */
    boolean requestThrottle(int address, ThrottleListener l);

    /**
     * Request a throttle, given a decoder address. When the decoder address is
     * located, the ThrottleListener gets a callback via the
     * ThrottleListener.notifyThrottleFound method.
     * <p>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form,
     * and assumes that the hardware makes and steal / share decisions.
     * @param canHandleDecisions true if the ThrottleListener has a mechanism for dealing with
     *        Share / Steal decisions, else false
     * @param address desired decoder address
     * @param l       ThrottleListener awaiting notification of a found throttle
     
     * @return true if the request will continue, false if the request will not
     *         be made; false may be returned if a the throttle is already in
     *         use
     */
    boolean requestThrottle(int address, ThrottleListener l, boolean canHandleDecisions);

    /**
     * Request a throttle, given a decoder address and whether it is a long or
     * short DCC address. When the decoder address is located, the
     * ThrottleListener gets a callback via the
     * ThrottleListener.notifyThrottleFound method.
     *
     * @param address desired decoder address
     * @param isLong  true if requesting a DCC long (extended) address
     * @param l       ThrottleListener awaiting notification of a found throttle
     * @param canHandleDecisions true if the ThrottleListener has a mechanism for dealing with
     *                Share / Steal decisions, else false
     * @return true if the request will continue, false if the request will not
     *         be made; false may be returned if a the throttle is already in
     *         use
     */
    boolean requestThrottle(int address, boolean isLong, ThrottleListener l, boolean canHandleDecisions);

    /**
     * Request a throttle, given a LocoAddress. When the address is
     * located, the ThrottleListener gets a callback via the
     * ThrottleListener.notifyThrottleFound method.
     *
     * @param address desired loco address
     * @param l       ThrottleListener awaiting notification of a found throttle
     * @param canHandleDecisions true if the ThrottleListener has a mechanism for dealing with
     *        Share / Steal decisions, else false
     * @return true if the request will continue, false if the request will not
     *         be made; false may be returned if a the throttle is already in
     *         use
     */
    boolean requestThrottle(LocoAddress address, ThrottleListener l, boolean canHandleDecisions);
    
    /**
     * Cancel a request for a throttle.
     * <p>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     *
     * @param re desired Roster Entry
     * @param l  ThrottleListener canceling the request for a throttle
     */
    void cancelThrottleRequest(BasicRosterEntry re, ThrottleListener l);
    
    /**
     * Cancel a request for a throttle.
     * <p>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     *
     * @param address desired decoder address
     * @param l       ThrottleListener canceling request for a throttle
     */
    void cancelThrottleRequest(int address, ThrottleListener l);
    
    /**
     * Cancel a request for a throttle.
     *
     * @param address desired decoder address
     * @param isLong  true if requesting a DCC long (extended) address
     * @param l       ThrottleListener canceling request for a throttle
     */
    void cancelThrottleRequest(int address, boolean isLong, ThrottleListener l);
    
    /**
     * Cancel a request for a throttle.
     *
     * @param address unwanted Loco address
     * @param l       ThrottleListener canceling request for a throttle
     */
    void cancelThrottleRequest(LocoAddress address, ThrottleListener l);

    /**
     * Steal or Share a requested throttle.
     * <p>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     *
     * @param address desired decoder address
     * @param l  ThrottleListener requesting the throttle steal occur.
     * @param decision from the ThrottleListener, STEAL or SHARE.
     */
    void responseThrottleDecision(int address, ThrottleListener l, ThrottleListener.DecisionType decision);
    
    /**
     * Steal or Share a requested throttle.
     * <p>
     * This is a convenience version of the call, which uses system-specific
     * logic to tell whether the address is a short or long form.
     *
     * @param address desired decoder address
     * @param isLong  true if requesting a DCC long (extended) address
     * @param l  ThrottleListener requesting the throttle steal occur.
     * @param decision from the ThrottleListener, STEAL or SHARE.
     */
    void responseThrottleDecision(int address, boolean isLong, ThrottleListener l, ThrottleListener.DecisionType decision);
    
    /**
     * Steal or Share a requested throttle.
     *
     * @param address desired LocoAddress
     * @param l The ThrottleListener which has made the decision
     * @param decision from the ThrottleListener, STEAL or SHARE.
     * @since 4.9.2
     */
    void responseThrottleDecision(LocoAddress address, ThrottleListener l, ThrottleListener.DecisionType decision);
    
    /**
     * Test if the Silent Steal Throttles preference option should be enabled.
     *
     * @return true if steal is possible; false otherwise
     */
    boolean enablePrefSilentStealOption();
    
    /**
     * Test if the Silent Share Throttles preference option should be enabled.
     *
     * @return true if steal is possible; false otherwise
     */
    boolean enablePrefSilentShareOption();
    
    /**
     * Test if the Dispatch Button should be enabled or not.
     *
     * @return true if dispatch is possible; false otherwise
     */
    boolean hasDispatchFunction();

    /**
     * Test if a specific number is a valid long address on this system.
     *
     * @param address address number to test
     * @return true if address can be long; false otherwise
     */
    boolean canBeLongAddress(int address);

    /**
     * Test if a specific number is a valid short address on this system.
     *
     * @param address address number to test
     * @return true if address can be short; false otherwise
     */
    boolean canBeShortAddress(int address);

    /**
     * Test if ambiguous addresses (short vs long) are not allowed on this
     * system. Also indicates support for multi-protocol decoders.
     *
     * @return true if ambiguous addresses are not allowed; false otherwise
     */
    boolean addressTypeUnique();

    /**
     * This returns a list of the different protocols that are supported by the
     * system, to include short vs long or DCC vs Selectrix vs Motorola.
     *
     * @return the list of supported address protocols
     */
    String[] getAddressTypes();

    /**
     * Get a string value for a given protocol value.
     *
     * @param prot the protocol
     * @return a human-readable, possibly localized, description of the protocol
     */
    String getAddressTypeString(LocoAddress.Protocol prot);

    /**
     * Get a list of different protocols supported by the system, to include
     * short vs long or DCC vs Selectrix vs Motorola.
     *
     * @return a list of supported address protocols
     */
    LocoAddress.Protocol[] getAddressProtocolTypes();

    /**
     * Get a protocol given a description.
     *
     * @param selection human-readable, possibly localized, description of the
     *                  protocol
     * @return the protocol
     */
    LocoAddress.Protocol getProtocolFromString(String selection);

    /**
     * Get the object representing a particular address.
     *
     * @param value    address in protocol-specific format
     * @param protocol specific protocol string, see the specific throttle
     *                 manager for values
     * @return the address, possibly as a protocol-specific subclass
     */
    LocoAddress getAddress(String value, String protocol);

    /**
     * Get the object representing a particular address.
     *
     * @param value    address in protocol-specific format
     * @param protocol the control protocol
     * @return the address, possibly as a protocol-specific subclass
     */
    LocoAddress getAddress(String value, LocoAddress.Protocol protocol);

    /**
     * Get the supported speed modes.
     *
     * @return an XOR of the possible modes specified in the throttle interface
     */
    EnumSet<SpeedStepMode> supportedSpeedModes();

    /**
     * Provides a Proxy method to return the SpeedSetting, Direction, Function
     * Settings, of a throttle, where the requesting code has used
     * {@link #attachListener(LocoAddress, java.beans.PropertyChangeListener) attachListener}
     * to only be notified of changes in the throttle and not control it.
     * <p>
     * Valid values for item are IsForward SpeedSetting SpeedIncrement
     * SpeedStepMode F0-F28
     *
 * @param la   LocoAddress that we wish interrogate
     * @param item A string of the item we wish to know the value of.
     * @return the value as an object, if the loco address has not been assigned
     *         to a throttle or the item value is not valid, null is returned.
     */
    Object getThrottleInfo(LocoAddress la, String item);

    /**
     * 
     * @param la Loco address to test
     * @return true, its still required, false its not.
     */
    boolean addressStillRequired(LocoAddress la);

    /**
     * 
     * @param address Loco number to test.
     * @param addressIsLong true if long address.
     * @return true, its still required, false its not.
     */
    boolean addressStillRequired(int address, boolean addressIsLong);
    
    /**
     * 
     * @param address Loco number to test
     * @return true, its still required, false its not.
     */
    boolean addressStillRequired(int address);

    /**
     * 
     * @param re roster entry to test
     * @return true, its still required, false its not.
     */
    boolean addressStillRequired(BasicRosterEntry re);
    
    /**
     * The specified Throttle Listener has finished using a given throttle and
     * no longer requires access to it.
     * <p>
     * After releasing the throttle, the manager will perform further checks to
     * see if it is in use by any other listeners or if there are any
     * PropertyChangeListeners attached. If there are no other uses of the
     * throttle then it is disposed of.
     * <p>
     * Normally, release ends with a call to dispose.
     *
     * @param t Throttle being released
     * @param l Throttle Listener releasing the throttle
     */
    void releaseThrottle(DccThrottle t, ThrottleListener l);

    /**
     * Not for general use, see
     * {@link #releaseThrottle(DccThrottle, ThrottleListener) releaseThrottle}
     * and
     * {@link #dispatchThrottle(DccThrottle, ThrottleListener) dispatchThrottle}.
     * <p>
     * Dispose of object when finished it. This will free up hardware resource
     * <p>
     * Used for handling certain internal error conditions, where the object
     * still exists but hardware is not associated with it.
     * <p>
     * After this, further usage of this Throttle object will result in a
     * JmriException.
     *
     * @param t Throttle being released
     * @param l Throttle Listener releasing the throttle
     * @return true if the throttle has been disposed of.
     */
    boolean disposeThrottle(DccThrottle t, ThrottleListener l);

    /**
     * The throttle listener has finished with the specific Throttle and is is
     * available for reuse/reallocation by somebody else. If possible, tell the
     * layout that this locomotive has been dispatched to another user. Not all
     * layouts will implement this, in which case it is synomous with release();
     * <p>
     * Normally, dispatch ends with a call to dispose.
     *
     * @param t Throttle being released
     * @param l Throttle Listener releasing the throttle
     */
    void dispatchThrottle(DccThrottle t, ThrottleListener l);

    /**
     * Attach a PropertyChangeListener to a specific loco address, where the
     * requesting code does not need or require control over the loco. If the
     * loco address is not in the active in the list, then a new throttle will
     * be created by the manager and the listener attached.
     * <p>
     * The PropertyChangeListener will be notified if it has been attached to a
     * loco address or not, via a PropertyChange notification.
     *
     * @param la LocoAddress of the loco we wish to monitor
     * @param p  PropertyChangeListener to attach to the throttle
     */
    void attachListener(LocoAddress la, PropertyChangeListener p);

    /**
     * Remove a PropertyChangeListener to a specific loco address, where the
     * requesting code has used
     * {@link #attachListener(LocoAddress, java.beans.PropertyChangeListener) attachListener}
     * to get notification of changes in a throttle.
     * <p>
     * The PropertyChangeListener will be notified if it has been removed via a
     * PropertyChange notification.
     *
     * @param la LocoAddress of the loco we wish to monitor
     * @param p  PropertyChangeListener to remove from the throttle
     */
    void removeListener(LocoAddress la, PropertyChangeListener p);

    /**
     * Get the user name of the system that the programmer is associated with.
     *
     * @return the user name for the system
     */
    String getUserName();

    /**
     * Get the number of Throttles sharing the throttle for a ddcaddress.
     *
     * @param la LocoAddress of the loco you want the throttle usage count for.
     * @return number of throttles for this address, or 0 if throttle does not exist
     */
    int getThrottleUsageCount(LocoAddress la);
    
     /**
     * Get the number of Throttles sharing the throttle for a ddcaddress.
     *
     * @param address number of the loco you want the throttle usage count for.
     * @param isLongAddress indicates whether the address is long or not.
     * @return number of throttles for this address, or 0 if throttle does not exist
     */
     int getThrottleUsageCount(int address, boolean isLongAddress);

    /**
     * Get the number of Throttles sharing the throttle for a ddcaddress.
     *
     * @param address number of the loco you want the throttle usage count for.
     * @return number of throttles for this address, or 0 if throttle does not exist
     */
    int getThrottleUsageCount(int address);

    /**
     * Get the number of Throttles sharing the throttle for a ddcaddress.
     *
     * @param re BasicRosterEntry of the loco you want the throttle usage count for.
     * @return number of throttles for this address, or 0 if throttle does not exist
     */
    int getThrottleUsageCount(BasicRosterEntry re);

    /**
     * Allow to cleanly release the traffic controller in ThrottleManager Tests
     * <ul>
     *     <li>remove listeners, if any</li>
     *     <li>stop timers, is any</li>
     * </ul>
     */
    void dispose();

}
