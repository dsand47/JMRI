package jmri.jmrit.operations;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;

import jmri.InstanceManager;
import jmri.implementation.swing.SwingShutDownTask;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JmriJFrame;

/**
 * Frame for operations
 *
 * @author Dan Boudreau Copyright (C) 2008, 2012
 */
public class OperationsFrame extends JmriJFrame {

    public static final String NEW_LINE = "\n"; // NOI18N
    public static final String NONE = ""; // NOI18N

    public OperationsFrame(String s) {
        this(s, new OperationsPanel());
    }

    public OperationsFrame() {
        this(new OperationsPanel());
    }

    public OperationsFrame(OperationsPanel p) {
        super();
        this.setContentPane(p);
        this.setEscapeKeyClosesWindow(true);
    }

    public OperationsFrame(String s, OperationsPanel p) {
        super(s);
        this.setContentPane(p);
        this.setEscapeKeyClosesWindow(true);
    }

    @Override
    public void initComponents() {
        // default method does nothing, but fail to call super.initComponents,
        // so that Exception does not need to be caught
    }

    public void initMinimumSize() {
        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight250));
    }

    public void initMinimumSize(Dimension dimension) {
        setMinimumSize(dimension);
        pack();
        setVisible(true);
    }

    protected void addItem(JComponent c, int x, int y) {
        this.getContentPane().addItem(c, x, y);
    }

    protected void addItem(JPanel p, JComponent c, int x, int y) {
        this.getContentPane().addItem(p, c, x, y);
    }

    protected void addItemLeft(JPanel p, JComponent c, int x, int y) {
        this.getContentPane().addItemLeft(p, c, x, y);
    }

    protected void addItemTop(JPanel p, JComponent c, int x, int y) {
        this.getContentPane().addItemTop(p, c, x, y);
    }

    protected void addItemWidth(JPanel p, JComponent c, int width, int x, int y) {
        this.getContentPane().addItemWidth(p, c, width, x, y);
    }

    /**
     * Gets the number of checkboxes(+1) that can fix in one row see
     * OperationsFrame.MIN_CHECKBOXES and OperationsFrame.MAX_CHECKBOXES
     *
     * @return the number of checkboxes, minimum is 5 (6 checkboxes)
     */
    protected int getNumberOfCheckboxesPerLine() {
        return this.getContentPane().getNumberOfCheckboxesPerLine(this.getPreferredSize());
    }

    protected void addButtonAction(JButton b) {
        b.addActionListener(this::buttonActionPerformed);
    }

    protected void buttonActionPerformed(ActionEvent ae) {
        this.getContentPane().buttonActionPerformed(ae);
    }

    protected void addRadioButtonAction(JRadioButton b) {
        b.addActionListener(this::radioButtonActionPerformed);
    }

    protected void radioButtonActionPerformed(ActionEvent ae) {
        this.getContentPane().radioButtonActionPerformed(ae);
    }

    protected void addCheckBoxAction(JCheckBox b) {
        b.addActionListener(this::checkBoxActionPerformed);
    }

    protected void checkBoxActionPerformed(ActionEvent ae) {
        this.getContentPane().checkBoxActionPerformed(ae);
    }

    protected void addSpinnerChangeListerner(JSpinner s) {
        s.addChangeListener(this::spinnerChangeEvent);
    }

    protected void spinnerChangeEvent(ChangeEvent ae) {
        this.getContentPane().spinnerChangeEvent(ae);
    }

    protected void addComboBoxAction(JComboBox<?> b) {
        b.addActionListener(this::comboBoxActionPerformed);
    }

    protected void comboBoxActionPerformed(ActionEvent ae) {
        this.getContentPane().comboBoxActionPerformed(ae);
    }

    protected void selectNextItemComboBox(JComboBox<?> b) {
        this.getContentPane().selectNextItemComboBox(b);
    }

    /**
     * Will modify the character column width of a TextArea box to 90% of a
     * panels width. ScrollPane is set to 95% of panel width.
     *
     * @param scrollPane the pane containing the textArea
     * @param textArea   the textArea to adjust
     */
    protected void adjustTextAreaColumnWidth(JScrollPane scrollPane, JTextArea textArea) {
        this.getContentPane().adjustTextAreaColumnWidth(scrollPane, textArea, this.getPreferredSize());
    }
    
    protected void adjustTextAreaColumnWidth(JScrollPane scrollPane, JTextArea textArea, Dimension size) {
        this.getContentPane().adjustTextAreaColumnWidth(scrollPane, textArea, size);
    }

    /**
     * Load the table width, position, and sorting status from the user
     * preferences file.
     *
     * @param table The table to be adjusted.
     *
     */
    public void loadTableDetails(JTable table) {
        this.getContentPane().loadTableDetails(table);
    }

    protected void clearTableSort(JTable table) {
        this.getContentPane().clearTableSort(table);
    }

    /**
     * Checks at shutdown if operations files need to be saved
     */
    protected synchronized void createShutDownTask() {
        InstanceManager.getDefault(OperationsManager.class)
                .setShutDownTask(new SwingShutDownTask("Operations Train Window Check", // NOI18N
                        Bundle.getMessage("PromptQuitWindowNotWritten"), Bundle.getMessage("PromptSaveQuit"), this) {
                    @Override
                    public boolean checkPromptNeeded() {
                        setModifiedFlag(false);
                        if (Setup.isAutoSaveEnabled()) {
                            storeValues();
                            return true; // no prompt needed
                        }
                        return !OperationsXml.areFilesDirty();
                    }

                    @Override
                    public void didPrompt() {
                        storeValues();
                    }
                });
    }
    
    @Override
    protected void storeValues() {
        this.getContentPane().storeValues();
    }

    @Override
    public void dispose() {
        this.getContentPane().dispose();
        super.dispose();
    }

    /*
     * Kludge fix for horizontal scrollbar encroaching buttons at bottom of a scrollable window.
     */
    protected void addHorizontalScrollBarKludgeFix(JScrollPane pane, JPanel panel) {
        this.getContentPane().addHorizontalScrollBarKludgeFix(pane, panel);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation only accepts the content pane if it is an
     * {@link OperationsPanel}.
     *
     * @throws java.lang.IllegalArgumentException if the content pane is not an
     *                                            OperationsPanel
     */
    @Override
    public void setContentPane(Container contentPane) {
        if (contentPane instanceof OperationsPanel) {
            super.setContentPane(contentPane);
        } else {
            throw new IllegalArgumentException("OperationsFrames can only use an OperationsPanel as the contentPane");
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation only returns the content pane if it is an
     * {@link OperationsPanel}.
     *
     * @throws java.lang.IllegalArgumentException if the content pane is not an
     *                                            OperationsPanel
     */
    @Override
    public OperationsPanel getContentPane() {
        Container c = super.getContentPane();
        if (c instanceof OperationsPanel) {
            return (OperationsPanel) c;
        }
        throw new IllegalArgumentException("OperationsFrames can only use an OperationsPanel as the contentPane");
    }
}
