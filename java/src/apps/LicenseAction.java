package apps;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import jmri.util.FileUtil;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to display the JMRI license
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2010
 */
public class LicenseAction extends jmri.util.swing.JmriAbstractAction {

    public LicenseAction() {
        super("License");
    }

    public LicenseAction(String s, Icon i, WindowInterface w) {
        super(s, i, w);
    }

    public LicenseAction(String s, WindowInterface w) {
        super(s, w);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        // here we can set a title

        JFrame frame = new jmri.util.JmriJFrame(); // to ensure fits
        frame.setTitle(Bundle.getMessage("TitleLicense"));

        JPanel pane = new JPanel();
        // pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        // insert stuff from makePanel() here
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));

        JTextPane textPane = new JTextPane();
        pane.add(textPane);
        JScrollPane scroll = new JScrollPane(pane);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));

        // get the file
        InputStream is = FileUtil.findInputStream("resources/COPYING", FileUtil.Location.INSTALLED); // NOI18N

        String t;

        try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.US_ASCII); // file stored as ASCII // NOI18N
             BufferedReader r = new BufferedReader(isr)) {
            StringBuilder buf = new StringBuilder();
            while (r.ready()) {
                buf.append(r.readLine());
                buf.append("\n");
            }
            t = buf.toString();
        } catch (IOException ex) {
            t = "JMRI is distributed under a license. For license information, see the JMRI website http://jmri.org";
        }
        textPane.setText(t);
        // set up display
        textPane.setEditable(false);
        // start scrolled to top
        textPane.setCaretPosition(0);

        frame.getContentPane().add(scroll);

        frame.pack();
        frame.setVisible(true);
    }

        @Override
        public JmriPanel makePanel() {
            // do nothing
            return null;
        }

}

