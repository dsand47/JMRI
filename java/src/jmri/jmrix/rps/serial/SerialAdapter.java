package jmri.jmrix.rps.serial;

import java.util.Arrays;
import java.io.IOException;

import jmri.InvokeOnGuiThread;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.RpsSystemConnectionMemo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Implements SerialPortAdapter for the RPS system.
 * <p>
 * Unlike many other SerialPortAdapters, this also converts the input stream
 * into Readings that can be passed to the Distributor.
 * <p>
 * This version expects that the "A" command will send back "DATA,," followed by
 * a list of receivers numbers, and data lines will be "0,0,0,0": A value for
 * each address up to the max receiver, even if some are missing (0 in that
 * case)
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2008
 */
public class SerialAdapter extends jmri.jmrix.AbstractSerialPortController {

    public SerialAdapter() {
        super(new RpsSystemConnectionMemo());
        option1Name = "Protocol"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ProtocolVersionLabel"), validOptions1));
        this.manufacturerName = jmri.jmrix.rps.RpsConnectionTypeList.NAC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RpsSystemConnectionMemo getSystemConnectionMemo() {
        return (RpsSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    /**
     * Set up all of the other objects to operate.
     */
    @Override
    public void configure() {
        // Connect the control objects:
        log.debug("configure() connecting RPS objects");
        // connect an Engine to the Distributor
        Engine e = Engine.instance();
        Distributor.instance().addReadingListener(e);
        // start the reader
        readerThread = new Thread(new Reader());
        readerThread.start();

        this.getSystemConnectionMemo().configureManagers();
    }

    @Override
    public synchronized String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect RPS to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting RPS to {} {}", portName, currentSerialPort);
        
        // try to set it for communication via SerialDriver
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        setBaudRate(currentSerialPort, baud);
        configureLeads(currentSerialPort, true, true);
        setFlowControl(currentSerialPort, FlowControl.NONE);

        // report status
        reportPortStatus(log, portName);

        opened = true;
        
        // capture streams
        serialStream = getInputStream();
        ostream = getOutputStream();

        return null; // indicates OK return
    }

    java.io.DataInputStream serialStream;
    java.io.OutputStream ostream;
    
    /**
     * Send output bytes, e.g. characters controlling operation, with small
     * delays between the characters. This is used to reduce overrrun problems.
     * @param bytes Array of characters to be sent one at a time
     */
    synchronized void sendBytes(byte[] bytes) {
        try {
            for (int i = 0; i < bytes.length - 1; i++) {
                ostream.write(bytes[i]);
                wait(3);
            }
            final byte endbyte = bytes[bytes.length - 1];
            ostream.write(endbyte);
        } catch (java.io.IOException e) {
            log.error("Exception on output: ", e);
        } catch (java.lang.InterruptedException e) {
            Thread.currentThread().interrupt(); // retain if needed later
            log.error("Interrupted output: ", e);
        }
    }

    // base class methods for the PortController interface
    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return Arrays.copyOf(validSpeedValues, validSpeedValues.length);
    }

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud115200")};
    protected int[] validSpeedValues = new int[]{115200};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    String[] validOptions1 = new String[]{Bundle.getMessage("Version1Choice"), Bundle.getMessage("Version2Choice")};

    /**
     * Set the second port option.
     */
    @Override
    public void configureOption1(String value) {
        setOptionState(option1Name, value);
        if (value.equals(validOptions1[0])) {
            version = 1;
        } else if (value.equals(validOptions1[1])) {
            version = 2;
        }
    }

    // private control members
    int[] offsetArray = null;

    // code for handling the input characters
    Thread readerThread;

    // flag for protocol version
    int version = 1;

    /**
     * Internal class to handle the separate character-receive thread.
     */
    class Reader implements Runnable {

        /**
         * Handle incoming characters. This is a permanent loop, looking for
         * input messages in character form on the stream connected to the
         * PortController via <code>connectPort</code>. Terminates with the
         * input stream breaking out of the try block.
         */
        @Override
        public void run() {
            // have to limit verbosity!

            while (true) {   // loop permanently, stream close will exit via exception
                try {
                    handleIncomingData();
                } catch (java.io.IOException e) {
                    log.warn("run: Exception: ", e);
                }
            }
        }

        static final int maxMsg = 200;
        StringBuffer msg;
        String msgString;

        void handleIncomingData() throws java.io.IOException {
            // we sit in this until the message is complete, relying on
            // threading to let other stuff happen

            // Create output message
            msg = new StringBuffer(maxMsg);
            // message exists, now fill it
            int i;
            for (i = 0; i < maxMsg; i++) {
                char char1;

                synchronized (SerialAdapter.this) {
                    char1 = (char) serialStream.readByte();
                }

                if (char1 == 13) {  // 13 is the CR at the end; done this
                    // way to be coding-independent
                    break;
                }
                // Strip off the CR and LF
                if (char1 != 10) {
                    msg.append(char1);
                }
            }

            // create the String to display (as String has .equals)
            msgString = msg.toString();
            log.debug("Msg <{}>", msgString);

            // return a notification via the queue to ensure end
            Runnable r = new Runnable() {

                // retain a copy of the message at startup
                String msgForLater = msgString;

                @Override
                public void run() {
                    nextLine(msgForLater);
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);
        }

    } // end class Reader

    /**
     * Handle a new line from the device.
     * <p>
     * This needs to execute on the Swing GUI thread. It forwards via the
     * Distributor object.
     *
     * @param s The new message to distribute
     */
    @InvokeOnGuiThread
    protected void nextLine(String s) {
        // check for startup lines we ignore
        if (s.length() < 5) {
            return;
        }
        if (s.startsWith("DATA,,")) {
            // skip reply to A
            setReceivers(s);
            return;
        }

        Reading r;
        try {
            r = makeReading(s);
        } catch (IOException e) {
            log.error("Exception formatting input line \"{}\": ", s, e);
            // r = new Reading(-1, new double[]{-1, -1, -1, -1} );
            // skip handling this line
            return;
        }

        if (r == null) {
            return;  // nothing useful
        }
        // forward
        try {
            Distributor.instance().submitReading(r);
        } catch (Exception e) {
            log.error("Exception forwarding reading: ", e);
        }
    }

    /**
     * Handle the message which lists the receiver numbers. Just makes an array
     * of those, which is not actually used.
     * @param s Input line
     */
    void setReceivers(String s) {
        try {
            // parse string
            CSVParser c = CSVParser.parse(s, CSVFormat.DEFAULT);
            CSVRecord r = c.getRecords().get(0);
            c.close();

            // first two are "Data, ," so are ignored.
            // rest are receiver numbers
            // Find how many
            int n = r.size() - 2;
            log.debug("Found {} receivers", n);

            // find max receiver number
            int max = Integer.parseInt(r.get(r.size() - 1));
            log.debug("Highest receiver address is {}", max);

            offsetArray = new int[n];
            for (int i = 0; i < n; i++) {
                offsetArray[i] = Integer.parseInt(r.get(i + 2));
            }

        } catch (IOException e) {
            log.debug("Did not handle init message <{}> due to {}", s, e);
        }
    }

    static private final int SKIPCOLS = 0; // used to skip DATA,TIME; was there a trailing "'"?
    private boolean first = true;

    /**
     * Convert input line to Reading object.
     * @param s The line of input
     * @return A Reading object with content parsed from the input line
     * @throws IOException from underlying I/O
     */
    Reading makeReading(String s) throws IOException {
        if (first) {
            log.info("RPS starts, using protocol version {}", version);
            first = false;
        }

        if (version == 1) {
            // parse string
            CSVParser c = CSVParser.parse(s, CSVFormat.DEFAULT);
            CSVRecord record = c.getRecords().get(0);
            c.close();

            // values are stored in 1-N of the output array; 0 not used
            int count = record.size() - SKIPCOLS;
            double[] vals = new double[count + 1];
            for (int i = 1; i < count + 1; i++) {
                vals[i] = Double.valueOf(record.get(i + SKIPCOLS - 1));
            }

            return new Reading(Engine.instance().getPolledID(), vals, s);
        } else if (version == 2) {
            // parse string
            CSVParser c = CSVParser.parse(s, CSVFormat.DEFAULT);
            CSVRecord record = c.getRecords().get(0);
            c.close();

            int count = (record.size() - 2) / 2;  // skip 'ADR, DAT,'
            double[] vals = new double[Engine.instance().getMaxReceiverNumber() + 1]; // Receiver 2 goes in element 2
            for (int i = 0; i < vals.length; i++) {
                vals[i] = 0.0;
            }
            try {
                for (int i = 0; i < count; i++) {  // i is zero-based count of input pairs
                    int index = Integer.parseInt(record.get(2 + i * 2));  // index is receiver number
                    // numbers are from one for valid receivers
                    // the null message starts with index zero
                    if (index < 0) {
                        continue;
                    }
                    if (index >= vals.length) { // data for undefined Receiver
                        log.warn("Data from unexpected receiver {}, creating receiver", index);
                        Engine.instance().setMaxReceiverNumber(index + 1);
                        //
                        // Originally, we made vals[] longer if we got
                        // a response from an unexpected receiver.
                        // This caused terrible trouble at Kesen's layout,
                        // so was commented-out here.
                        //
                        //double[] temp = new double[index+1];
                        //for (int j = 0; j<vals.length; j++) temp[j] = vals[j];
                        //vals = temp;
                    }
                    if (index < vals.length) {
                        vals[index] = Double.valueOf(record.get(2 + i * 2 + 1));
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("Exception handling input.", e);
                return null;
            }
            return new Reading(Engine.instance().getPolledID(), vals, s);
        } else {
            log.error("can't handle version {}", version);
            return null;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialAdapter.class);

}
