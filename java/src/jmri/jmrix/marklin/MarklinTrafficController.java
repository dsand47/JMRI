package jmri.jmrix.marklin;

import java.util.concurrent.ConcurrentLinkedQueue;
import jmri.CommandStation;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;

/**
 * Converts Stream-based I/O to/from Marklin CS2 messages. The
 * "MarklinInterface" side sends/receives message objects.
 * <p>
 * The connection to a MarklinPortController is via a pair of UDP Streams, which
 * then carry sequences of characters for transmission. Note that this
 * processing is handled in an independent thread.
 * <p>
 * This handles the state transitions, based on the necessary state in each
 * message.
 *
 * Based on work by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class MarklinTrafficController extends AbstractMRTrafficController implements MarklinInterface, CommandStation {

    /**
     * Create a new MarklinTrafficController instance.
     */
    public MarklinTrafficController() {
        super();
        log.debug("creating a new MarklinTrafficController object");
        // set as command station too
        jmri.InstanceManager.store(MarklinTrafficController.this, CommandStation.class);
        this.setAllowUnexpectedReply(true);
    }

    public void setAdapterMemo(MarklinSystemConnectionMemo memo) {
        adaptermemo = memo;
    }

    MarklinSystemConnectionMemo adaptermemo;
    protected String defaultUserName = "Marklin-CS2";

    // The methods to implement the MarklinInterface
    @Override
    public synchronized void addMarklinListener(MarklinListener l) {
        this.addListener(l);
    }

    @Override
    public synchronized void removeMarklinListener(MarklinListener l) {
        this.removeListener(l);
    }

    @Override
    protected int enterProgModeDelayTime() {
        // we should to wait at least a second after enabling the programming track
        return 1000;
    }

    /**
     * CommandStation implementation, not yet supported.
     * {@inheritDoc }
     */
    @Override
    public boolean sendPacket(byte[] packet, int count) {

        return true;
    }

    /**
     * Forward a MarklinMessage to all registered MarklinInterface listeners.
     */
    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        ((MarklinListener) client).message((MarklinMessage) m);
    }

    /**
     * Forward a MarklinReply to all registered MarklinInterface listeners.
     * {@inheritDoc }
     */
    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply r) {
        ((MarklinListener) client).reply((MarklinReply) r);
    }

    /**
     * Forward a preformatted message to the actual interface.
     * {@inheritDoc }
     */
    @Override
    public void sendMarklinMessage(MarklinMessage m, MarklinListener reply) {
        sendMessage(m, reply);
    }

    /**
     * Marklin doesn't support this function.
     * @return empty Marklin Message.
     */
    @Override
    protected AbstractMRMessage enterProgMode() {
        return MarklinMessage.getProgMode();
    }

    /**
     * Marklin doesn't support this function.
     * @return empty Marklin Message.
     */
    @Override
    protected AbstractMRMessage enterNormalMode() {
        return MarklinMessage.getExitProgMode();
    }

    @Override
    protected AbstractMRReply newReply() {
        return new MarklinReply();
    }

    // for now, receive always OK
    @Override
    protected boolean canReceive() {
        return true;
    }

    //In theory the replies should only be 13bytes long, so the EOM is completed when the reply can take no more data
    @Override
    protected boolean endOfMessage(AbstractMRReply msg) {
        return false;
    }

    private static class PollMessage {

        MarklinListener ml;
        MarklinMessage mm;

        PollMessage(MarklinMessage mm, MarklinListener ml) {
            this.mm = mm;
            this.ml = ml;
        }

        MarklinListener getListener() {
            return ml;
        }

        MarklinMessage getMessage() {
            return mm;
        }
    }

    private final ConcurrentLinkedQueue<PollMessage> pollQueue = new ConcurrentLinkedQueue<>();

    private boolean disablePoll = false;

    public boolean getPollQueueDisabled() {
        return disablePoll;
    }

    public void setPollQueueDisabled(boolean poll) {
        disablePoll = poll;
    }

    /**
     * As we have to poll the system to get updates we put request into a
     * queue and allow the abstract traffic controller to handle them when it
     * is free.
     * @param mm marklin message to add.
     * @param ml marklin listener.
     */
    public void addPollMessage(MarklinMessage mm, MarklinListener ml) {
        mm.setTimeout(500);
        for (PollMessage pm : pollQueue) {
            if (pm.getListener() == ml && pm.getMessage().toString().equals(mm.toString())) {
                log.debug("Message is already in the poll queue so will not add");
                return;
            }
        }
        PollMessage pm = new PollMessage(mm, ml);
        pollQueue.offer(pm);
    }

    /**
     * Removes a message that is used for polling from the queue.
     * @param mm marklin message to remove.
     * @param ml marklin listener.
     */
    public void removePollMessage(MarklinMessage mm, MarklinListener ml) {
        for (PollMessage pm : pollQueue) {
            if (pm.getListener() == ml && pm.getMessage().toString().equals(mm.toString())) {
                pollQueue.remove(pm);
            }
        }
    }

    /**
     * Check Tams MC for updates.
     */
    @Override
    protected AbstractMRMessage pollMessage() {
        if ( !disablePoll && !pollQueue.isEmpty()) {
            PollMessage pm = pollQueue.peek();
            if (pm != null) {
                return pm.getMessage();
            }
        }
        return null;
    }

    @Override
    protected AbstractMRListener pollReplyHandler() {
        if ( !disablePoll && !pollQueue.isEmpty()) {
            PollMessage pm = pollQueue.poll();
            if (pm != null) {
                pollQueue.offer(pm);
                return pm.getListener();
            }
        }
        return null;
    }

    @Override
    public String getUserName() {
        if (adaptermemo == null) {
            return defaultUserName;
        }
        return adaptermemo.getUserName();
    }

    @Override
    public String getSystemPrefix() {
        if (adaptermemo == null) {
            return "M";
        }
        return adaptermemo.getSystemPrefix();
    }

    public void dispose() {
        this.terminateThreads();
        jmri.InstanceManager.deregister(MarklinTrafficController.this, CommandStation.class);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MarklinTrafficController.class);

}
