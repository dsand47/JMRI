package jmri.jmrit.logixng;

import java.util.List;

import javax.annotation.Nonnull;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.jmrit.logixng.SymbolTable.InitialValueType;
import jmri.jmrit.logixng.SymbolTable.VariableData;

import org.slf4j.Logger;

/**
 * A LogixNG male socket.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public interface MaleSocket extends Debugable {

    enum ErrorHandlingType {

        Default(Bundle.getMessage("ErrorHandling_Default")),
        ShowDialogBox(Bundle.getMessage("ErrorHandling_ShowDialogBox")),
        LogError(Bundle.getMessage("ErrorHandling_LogError")),
        LogErrorOnce(Bundle.getMessage("ErrorHandling_LogErrorOnce")),
        ThrowException(Bundle.getMessage("ErrorHandling_ThrowException")),
        AbortExecution(Bundle.getMessage("ErrorHandling_AbortExecution")),
        AbortWithoutError(Bundle.getMessage("ErrorHandling_AbortWithoutError"));

        private final String _description;

        private ErrorHandlingType(String description) {
            _description = description;
        }

        @Override
        public String toString() {
            return _description;
        }
    }

    /**
     * Set whenether this male socket is enabled or disabled.
     * <P>
     * This method must call registerListeners() / unregisterListeners().
     *
     * @param enable true if this male socket should be enabled, false otherwise
     */
    void setEnabled(boolean enable);

    /**
     * Set whenether this male socket is enabled or disabled, without activating
     * the male socket. This is used when loading the xml file and when copying
     * an item.
     * <P>
     * This method must call registerListeners() / unregisterListeners().
     *
     * @param enable true if this male socket should be enabled, false otherwise
     */
    void setEnabledFlag(boolean enable);

    /**
     * Determines whether this male socket is enabled.
     *
     * @return true if the male socket is enabled, false otherwise
     */
    @Override
    boolean isEnabled();

    /**
     * Get whenether the node should listen to changes or not.
     * @return true if listen, false if not listen
     */
    boolean getListen();

    /**
     * Set whenether the node should listen to changes or not.
     * @param listen true if listen, false if not listen
     */
    void setListen(boolean listen);

    /**
     * Is the node locked?
     * @return true if locked, false otherwise
     */
    boolean isLocked();

    /**
     * Set if the node is locked or not.
     * @param locked true if locked, false otherwise
     */
    void setLocked(boolean locked);

    /**
     * Is the node a system node?
     * @return true if system, false otherwise
     */
    boolean isSystem();

    /**
     * Set if the node is system or not.
     * @param system true if system, false otherwise
     */
    void setSystem(boolean system);

    /**
     * Is the node catching AbortExecution or not?
     * @return true if catching, false otherwise
     */
    boolean getCatchAbortExecution();

    /**
     * Set if the node should catch AbortExecution or not.
     * @param catchAbortExecution true if catch, false otherwise
     */
    void setCatchAbortExecution(boolean catchAbortExecution);

    void addLocalVariable(
            String name,
            InitialValueType initialValueType,
            String initialValueData);

    void addLocalVariable(VariableData variableData);

    void clearLocalVariables();

    List<VariableData> getLocalVariables();

    default boolean isSupportingLocalVariables() {
        return true;
    }

    /**
     * Get the error handling type for this socket.
     * @return the error handling type
     */
    ErrorHandlingType getErrorHandlingType();

    /**
     * Set the error handling type for this socket.
     * @param errorHandlingType the error handling type
     */
    void setErrorHandlingType(ErrorHandlingType errorHandlingType);

    /**
     * Handle an error that has happened during execution or evaluation of
     * this item.
     * @param  item           the item that had the error
     * @param  message        the error message
     * @param  e              the exception that has happened
     * @param  log            the logger
     * @throws JmriException  if the male socket is configured to
     *                        throw an exception
     */
    void handleError(
            Base item,
            String message,
            JmriException e,
            Logger log)
            throws JmriException;

    /**
     * Handle an error that has happened during execution or evaluation of
     * this item.
     * @param  item           the item that had the error
     * @param  message        the error message
     * @param  messageList    a list of error messages
     * @param  e              the exception that has happened
     * @param  log            the logger
     * @throws JmriException  if the male socket is configured to
     *                        throw an exception
     */
    void handleError(
            Base item,
            String message,
            List<String> messageList,
            JmriException e,
            Logger log)
            throws JmriException;

    /**
     * Handle an error that has happened during execution or evaluation of
     * this item.
     * @param  item           the item that had the error
     * @param  message        the error message
     * @param  e              the exception that has happened
     * @param  log            the logger
     * @throws JmriException  if the male socket is configured to
     *                        throw an exception
     */
    void handleError(
            Base item,
            String message,
            RuntimeException e,
            Logger log)
            throws JmriException;

    /**
     * Get the object that this male socket holds.
     * This method is used when the object is going to be configured.
     *
     * @return the object this male socket holds
     */
    @Nonnull
    Base getObject();

    /**
     * Get the manager that stores this socket.
     * This method is used when the object is going to be configured.
     *
     * @return the manager
     */
    BaseManager<? extends NamedBean> getManager();

    /** {@inheritDoc} */
    @Override
    default void setup() {
        getObject().setup();
    }

    /**
     * Find a male socket of a particular type.
     * Male sockets can be stacked and this method travels thru the stacked
     * male sockets to find the desired male socket.
     * @param clazz the type of the male socket we are looking for
     * @return the found male socket or null if not found
     */
    default MaleSocket find(Class<?> clazz) {

        if (! MaleSocket.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("clazz is not a MaleSocket");
        }

        Base item = this;

        while ((item instanceof MaleSocket) && !clazz.isInstance(item)) {
            item = item.getParent();
        }

        if (clazz.isInstance(item)) return (MaleSocket)item;
        else return null;
    }

}
