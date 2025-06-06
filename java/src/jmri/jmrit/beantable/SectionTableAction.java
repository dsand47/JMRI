package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jmri.*;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JmriJFrame;
import jmri.swing.NamedBeanComboBox;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.swing.JmriJOptionPane;

/**
 * Swing action to create and register a SectionTable GUI.
 * <p>
 * This file is part of JMRI.
 * <p>
 * JMRI is open source software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published by the
 * Free Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Dave Duchamp Copyright (C) 2008, 2011
 * @author GT 2009
 */
public class SectionTableAction extends AbstractTableAction<Section> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public SectionTableAction(String actionName) {
        super(actionName);
        // set manager - no need to use InstanceManager here
        sectionManager = InstanceManager.getNullableDefault(SectionManager.class);
        // disable ourself if there is no Section manager available
        if (sectionManager == null) {
            super.setEnabled(false);
        }
    }

    public SectionTableAction() {
        this(Bundle.getMessage("TitleSectionTable"));
    }

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.beantable.SectionTransitTableBundle");

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Section objects.
     */
    @Override
    protected void createModel() {
        m = new BeanTableDataModel<Section>() {

            static public final int BEGINBLOCKCOL = NUMCOLUMN;
            static public final int ENDBLOCKCOL = BEGINBLOCKCOL + 1;
            static public final int EDITCOL = ENDBLOCKCOL + 1;

            @Override
            public String getValue(String name) {
                return "";
            }

            @Override
            public Manager<Section> getManager() {
                return InstanceManager.getDefault(SectionManager.class);
            }

            @Override
            public Section getBySystemName(@Nonnull String name) {
                return InstanceManager.getDefault(SectionManager.class).getBySystemName(name);
            }

            @Override
            public Section getByUserName(@Nonnull String name) {
                return InstanceManager.getDefault(SectionManager.class).getByUserName(name);
            }

            @Override
            protected String getMasterClassName() {
                return getClassName();
            }

            @Override
            public void clickOn(Section t) {
            }

            @Override
            public int getColumnCount() {
                return EDITCOL + 1;
            }

            @Override
            public Object getValueAt(int row, int col) {
                // some error checking
                if (row >= sysNameList.size()) {
                    log.debug("row is greater than name list");
                    return "";
                }
                switch (col) {
                    case BEGINBLOCKCOL:
                    {
                        Section z = getBySystemName(sysNameList.get(row));
                        if (z != null) {
                            return z.getBeginBlockName();
                        }
                        return "  ";
                    }
                    case ENDBLOCKCOL:
                    {
                        Section z = getBySystemName(sysNameList.get(row));
                        if (z != null) {
                            return z.getEndBlockName();
                        }
                        return "  ";
                    }
                    case VALUECOL:
                        {
                            Section z = getBySystemName(sysNameList.get(row));
                            if (z == null) {
                                return "";
                            } else {
                                switch (z.getState()) {
                                    case Section.FREE:
                                        return (rbx.getString("SectionFree"));
                                    case Section.FORWARD:
                                        return (rbx.getString("SectionForward"));
                                    case Section.REVERSE:
                                        return (rbx.getString("SectionReverse"));
                                    default:
                                        break;
                                }
                            }       break;
                        }
                    case EDITCOL:
                        return Bundle.getMessage("ButtonEdit");
                    default:
                        return super.getValueAt(row, col);
                }
                return null;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if ((col == BEGINBLOCKCOL) || (col == ENDBLOCKCOL)) {
                } else if (col == EDITCOL) {
                    SwingUtilities.invokeLater(() -> {
                        editPressed(((Section) getValueAt(row, SYSNAMECOL)).getSystemName());
                    });
                } else if (col == DELETECOL) {
                    deleteSectionPressed(sysNameList.get(row));
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            @Override
            public String getColumnName(int col) {
                if (col == BEGINBLOCKCOL) {
                    return (rbx.getString("SectionFirstBlock"));
                }
                if (col == ENDBLOCKCOL) {
                    return (rbx.getString("SectionLastBlock"));
                }
                if (col == EDITCOL) {
                    return "";   // no namne on Edit column
                }
                return super.getColumnName(col);
            }

            @Override
            public Class<?> getColumnClass(int col) {
                switch (col) {
                    case VALUECOL: // not a button
                    case BEGINBLOCKCOL: // not a button
                    case ENDBLOCKCOL: // not a button
                        return String.class;
                    case EDITCOL:
                        return JButton.class;
                    default:
                        return super.getColumnClass(col);
                }
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                switch (col) {
                    case BEGINBLOCKCOL:
                    case ENDBLOCKCOL:
                    case VALUECOL:
                        return false;
                    case EDITCOL:
                        return true;
                    default:
                        return super.isCellEditable(row, col);
                }
            }

            @Override
            public int getPreferredWidth(int col) {
                // override default value for SystemName and UserName columns
                switch (col) {
                    case SYSNAMECOL:
                        return new JTextField(9).getPreferredSize().width;
                    case USERNAMECOL:
                        return new JTextField(17).getPreferredSize().width;
                    case VALUECOL:
                    case EDITCOL:
                        return new JTextField(6).getPreferredSize().width;
                    case BEGINBLOCKCOL:
                    case ENDBLOCKCOL:
                        return new JTextField(15).getPreferredSize().width;
                    default:
                        return super.getPreferredWidth(col);
                }
            }

            @Override
            public void configValueColumn(JTable table) {
                // value column isn't button, so config is null
            }

            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return true;
                // return (e.getPropertyName().indexOf("alue")>=0);
            }
        };
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleSectionTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SectionTable";
    }

    // instance variables
    ArrayList<Block> blockList = new ArrayList<>();
    private BlockTableModel blockTableModel = null;
    EntryPointTableModel entryPointTableModel = null;
    SectionManager sectionManager = null;
    BlockManager blockManager = InstanceManager.getDefault(BlockManager.class);
    boolean editMode = false;
    Section curSection = null;
    boolean addCreateActive = true;
    ArrayList<LayoutEditor> lePanelList = null;
    LayoutEditor curLayoutEditor = null;
    Block beginBlock = null;
    Block endBlock = null;
    Sensor fSensor = null;
    Sensor rSensor = null;
    Sensor fStopSensor = null;
    Sensor rStopSensor = null;
    ArrayList<EntryPoint> entryPointList = new ArrayList<>();
    boolean manualEntryPoints = true;

    // add/create variables
    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(15);
    JLabel sysNameFixed = new JLabel("");
    JTextField userName = new JTextField(17);
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    UserPreferencesManager pref;
    JButton create = null;
    JButton update = null;
    JButton addBlock = null;
    JButton deleteBlocks = null;
    JComboBox<String> layoutEditorBox = new JComboBox<>();
    NamedBeanComboBox<Block> blockBox;
    NamedBeanComboBox<Sensor> forwardSensorBox;
    NamedBeanComboBox<Sensor> reverseSensorBox;
    NamedBeanComboBox<Sensor> forwardStopSensorBox;
    NamedBeanComboBox<Sensor> reverseStopSensorBox;
    JRadioButton manually = new JRadioButton(rbx.getString("SetManually"), true);
    JRadioButton automatic = new JRadioButton(rbx.getString("UseConnectivity"), false);
    ButtonGroup entryPointOptions = null;
    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";
    JLabel generationStateLabel = new JLabel();

    /**
     * Responds to the Add...button and the Edit buttons in the Section Table
     * @param e event which has triggered action
     */
    @Override
    protected void addPressed(ActionEvent e) {
        editMode = false;
        if ((blockManager.getNamedBeanSet().size()) > 0) {
            addEditPressed();
        } else {
            JmriJOptionPane.showMessageDialog(null, rbx
                    .getString("Message1"), Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    void editPressed(String sName) {
        curSection = sectionManager.getBySystemName(sName);
        if (curSection == null) {
            // no section - should never happen, but protects against a $%^#@ exception
            return;
        }
        sysNameFixed.setText(sName);
        editMode = true;
        addEditPressed();
    }

    void addEditPressed() {
        pref = InstanceManager.getDefault(UserPreferencesManager.class);
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddSection"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SectionAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            // add system name
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(sysNameLabel);
            sysNameLabel.setLabelFor(sysName);
            p.add(sysNameFixed);
            p.add(sysName);
            p.add(_autoSystemName);
            _autoSystemName.addActionListener((ActionEvent e) -> {
                autoSystemName();
            });
            if (pref.getSimplePreferenceState(systemNameAuto)) {
                _autoSystemName.setSelected(true);
            }
            sysName.setToolTipText(rbx.getString("SectionSystemNameHint"));
            addFrame.getContentPane().add(p);
            // add user name
            JPanel pu = new JPanel();
            pu.setLayout(new FlowLayout());
            pu.add(userNameLabel);
            userNameLabel.setLabelFor(userName);
            pu.add(userName);
            userName.setToolTipText(rbx.getString("SectionUserNameHint"));
            addFrame.getContentPane().add(pu);

            JPanel pa = new JPanel();
            pa.setLayout(new FlowLayout());
            pa.add(generationStateLabel);
            addFrame.getContentPane().add(pa);
            addFrame.getContentPane().add(new JSeparator());
            JPanel p1 = new JPanel();
            p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
            JPanel p11 = new JPanel();
            p11.setLayout(new FlowLayout());
            p11.add(new JLabel(rbx.getString("BlockTableMessage")));
            p1.add(p11);
            JPanel p12 = new JPanel();
            // initialize table of Blocks
            blockTableModel = new BlockTableModel();
            JTable blockTable = new JTable(blockTableModel);
            blockTable.setRowSelectionAllowed(false);
            blockTable.setPreferredScrollableViewportSize(new java.awt.Dimension(350, 100));
            TableColumnModel blockColumnModel = blockTable.getColumnModel();
            TableColumn sNameColumn = blockColumnModel.getColumn(BlockTableModel.SNAME_COLUMN);
            sNameColumn.setResizable(true);
            sNameColumn.setMinWidth(90);
            sNameColumn.setMaxWidth(130);
            TableColumn uNameColumn = blockColumnModel.getColumn(BlockTableModel.UNAME_COLUMN);
            uNameColumn.setResizable(true);
            uNameColumn.setMinWidth(210);
            uNameColumn.setMaxWidth(260);
            JScrollPane blockTableScrollPane = new JScrollPane(blockTable);
            p12.add(blockTableScrollPane, BorderLayout.CENTER);
            p1.add(p12);
            JPanel p13 = new JPanel();
            p13.setLayout(new FlowLayout());
            p13.add(deleteBlocks = new JButton(rbx.getString("DeleteAllBlocksButton")));
            deleteBlocks.addActionListener(this::deleteBlocksPressed);
            deleteBlocks.setToolTipText(rbx.getString("DeleteAllBlocksButtonHint"));
            p13.add(new JLabel("     "));
            initializeBlockCombo();
            p13.add(blockBox);
            blockBox.setToolTipText(rbx.getString("BlockBoxHint"));
            p13.add(addBlock = new JButton(rbx.getString("AddBlockButton")));
            addBlock.addActionListener(this::addBlockPressed);
            addBlock.setToolTipText(rbx.getString("AddBlockButtonHint"));
            p1.add(p13);
            addFrame.getContentPane().add(p1);
            // add hint for order of blocks irt direction of travel
            JPanel p34 = new JPanel();
            p34.setLayout(new FlowLayout());
            JLabel direction = new JLabel(rbx.getString("DirectionNote")); // placed below first table
            direction.setFont(direction.getFont().deriveFont(0.9f * blockBox.getFont().getSize())); // a bit smaller
            direction.setForeground(Color.gray);
            p34.add(direction);
            addFrame.getContentPane().add(p34);
            addFrame.getContentPane().add(new JSeparator());
            JPanel p31 = new JPanel();
            p31.setLayout(new FlowLayout());
            p31.add(new JLabel(rbx.getString("EntryPointTable")));
            addFrame.getContentPane().add(p31);

            JPanel p32 = new JPanel();
            p32.setLayout(new FlowLayout());
            entryPointOptions = new ButtonGroup();
            p32.add(manually);
            entryPointOptions.add(manually);
            manually.addActionListener((ActionEvent e) -> {
                manualEntryPoints = true;
            });
            manually.setToolTipText(rbx.getString("SetManuallyHint"));
            p32.add(new JLabel("   "));
            p32.add(automatic);
            entryPointOptions.add(automatic);
            automatic.addActionListener((ActionEvent e) -> {
                manualEntryPoints = false;
            });
            automatic.setToolTipText(rbx.getString("SetAutomaticHint"));
            p32.add(layoutEditorBox);
            layoutEditorBox.setToolTipText(rbx.getString("LayoutEditorBoxHint"));
            layoutEditorBox.addActionListener((ActionEvent e) -> {
                layoutEditorSelectionChanged();
            });
            // djd debugging - temporarily hide these items until the automatic setting of entry point direction is ready
            //   addFrame.getContentPane().add(p32);
            // end djd debugging

            JPanel p33 = new JPanel();
            // initialize table of entry points
            entryPointTableModel = new EntryPointTableModel();
            JTable entryPointTable = new JTable(entryPointTableModel);
            entryPointTable.setRowSelectionAllowed(false);
            entryPointTable.setPreferredScrollableViewportSize(new java.awt.Dimension(550, 100));
            TableColumnModel entryPointColumnModel = entryPointTable.getColumnModel();
            // From-Block
            TableColumn fromBlockColumn = entryPointColumnModel.getColumn(EntryPointTableModel.BLOCK_COLUMN);
            fromBlockColumn.setResizable(true);
            fromBlockColumn.setMinWidth(250);
            fromBlockColumn.setMaxWidth(310);
            // To-Block
            TableColumn toBlockColumn = entryPointColumnModel.getColumn(EntryPointTableModel.TO_BLOCK_COLUMN);
            toBlockColumn.setResizable(true);
            toBlockColumn.setMinWidth(150);
            toBlockColumn.setMaxWidth(210);

            JComboBox<String> directionCombo = new JComboBox<>();
            directionCombo.addItem(rbx.getString("SectionForward"));
            directionCombo.addItem(rbx.getString("SectionReverse"));
            directionCombo.addItem(Bundle.getMessage("BeanStateUnknown"));
            TableColumn directionColumn = entryPointColumnModel.getColumn(EntryPointTableModel.DIRECTION_COLUMN);
            directionColumn.setCellEditor(new DefaultCellEditor(directionCombo));
            entryPointTable.setRowHeight(directionCombo.getPreferredSize().height);
            directionColumn.setPreferredWidth(directionCombo.getPreferredSize().width);
            directionColumn.setResizable(false);
            JScrollPane entryPointTableScrollPane = new JScrollPane(entryPointTable);
            p33.add(entryPointTableScrollPane, BorderLayout.CENTER);
            addFrame.getContentPane().add(p33);
            p33.setVisible(true);
            // hint p34 is displayed 1 table up
            addFrame.getContentPane().add(new JSeparator());
            // set up pane for direction sensors
            forwardSensorBox = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance());
            forwardSensorBox.setAllowNull(true);
            forwardSensorBox.setSelectedItem(null);
            reverseSensorBox = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance());
            reverseSensorBox.setAllowNull(true);
            reverseSensorBox.setSelectedItem(null);
            JComboBoxUtil.setupComboBoxMaxRows(forwardSensorBox);
            JComboBoxUtil.setupComboBoxMaxRows(reverseSensorBox);
            JPanel p20 = new JPanel();
            p20.setLayout(new FlowLayout());
            p20.add(new JLabel(rbx.getString("DirectionSensorLabel")));
            addFrame.getContentPane().add(p20);
            JPanel p21 = new JPanel();
            p21.setLayout(new FlowLayout());
            p21.add(new JLabel(rbx.getString("ForwardSensor")));
            p21.add(forwardSensorBox);
            forwardSensorBox.setToolTipText(rbx.getString("ForwardSensorHint"));
            p21.add(new JLabel("     "));
            p21.add(new JLabel(rbx.getString("ReverseSensor")));
            p21.add(reverseSensorBox);
            reverseSensorBox.setToolTipText(rbx.getString("ReverseSensorHint"));
            addFrame.getContentPane().add(p21);
            addFrame.getContentPane().add(new JSeparator());
            // set up for stopping sensors
            forwardStopSensorBox = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance());
            forwardStopSensorBox.setAllowNull(true);
            forwardStopSensorBox.setSelectedItem(null);
            reverseStopSensorBox = new NamedBeanComboBox<>(InstanceManager.sensorManagerInstance());
            reverseStopSensorBox.setAllowNull(true);
            reverseStopSensorBox.setSelectedItem(null);
            JComboBoxUtil.setupComboBoxMaxRows(forwardStopSensorBox);
            JComboBoxUtil.setupComboBoxMaxRows(reverseStopSensorBox);
            JPanel p40 = new JPanel();
            p40.setLayout(new FlowLayout());
            p40.add(new JLabel(rbx.getString("StoppingSensorLabel")));
            addFrame.getContentPane().add(p40);
            JPanel p41 = new JPanel();
            p41.setLayout(new FlowLayout());
            p41.add(new JLabel(rbx.getString("ForwardStopSensor")));
            p41.add(forwardStopSensorBox);
            forwardStopSensorBox.setToolTipText(rbx.getString("ForwardStopSensorHint"));
            p41.add(new JLabel("     "));
            p41.add(new JLabel(rbx.getString("ReverseStopSensor")));
            p41.add(reverseStopSensorBox);
            reverseStopSensorBox.setToolTipText(rbx.getString("ReverseStopSensorHint"));
            addFrame.getContentPane().add(p41);
            addFrame.getContentPane().add(new JSeparator());
            // set up bottom row of buttons
            JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
            JPanel pb = new JPanel();
            pb.setLayout(new FlowLayout());
            pb.add(cancel);
            cancel.addActionListener(this::cancelPressed);
            cancel.setToolTipText(rbx.getString("CancelButtonHint"));
            pb.add(create = new JButton(Bundle.getMessage("ButtonCreate")));
            create.addActionListener(this::createPressed);
            create.setToolTipText(rbx.getString("SectionCreateButtonHint"));
            pb.add(update = new JButton(Bundle.getMessage("ButtonUpdate")));
            update.addActionListener(this::updatePressed);
            update.setToolTipText(rbx.getString("SectionUpdateButtonHint"));
            addFrame.getContentPane().add(pb);
        }
        if (editMode) {
            // setup for edit window
            _autoSystemName.setVisible(false);
            sysNameLabel.setEnabled(true);
            create.setVisible(false);
            update.setVisible(true);
            sysName.setVisible(true);
            sysName.setVisible(false);
            sysNameFixed.setVisible(true);
            initializeEditInformation();
            addFrame.getRootPane().setDefaultButton(update);
            addFrame.setTitle(Bundle.getMessage("TitleEditSection"));
        } else {
            // setup for create window
            _autoSystemName.setVisible(true);
            create.setVisible(true);
            update.setVisible(false);
            sysName.setVisible(true);
            sysNameFixed.setVisible(false);
            autoSystemName();
            clearForCreate();
            addFrame.getRootPane().setDefaultButton(create);
            addFrame.setTitle(Bundle.getMessage("TitleAddSection"));
        }
        // initialize layout editor panels
        if (initializeLayoutEditorCombo(layoutEditorBox)) {
            manually.setVisible(true);
            automatic.setVisible(true);
            layoutEditorBox.setVisible(true);
        } else {
            manually.setVisible(false);
            automatic.setVisible(false);
            layoutEditorBox.setVisible(false);
        }
        // initialize block combo - first time
        initializeBlockCombo();
        addFrame.setEscapeKeyClosesWindow(true);
        addFrame.pack();
        addFrame.setVisible(true);
    }

    private void initializeEditInformation() {
        userName.setText(curSection.getUserName());
        switch (curSection.getSectionType()) {
            case SIGNALMASTLOGIC:
                generationStateLabel.setText(rbx.getString("SectionTypeSMLLabel"));
                break;
            case DYNAMICADHOC:
                generationStateLabel.setText(rbx.getString("SectionTypeDynLabel"));
                break;
            case USERDEFINED:
            default:
                generationStateLabel.setText("");
                break;
        }

        deleteBlocksPressed(null);
        int i = 0;
        while (curSection.getBlockBySequenceNumber(i) != null) {
            Block b = curSection.getBlockBySequenceNumber(i);
            blockList.add(b);
            i++;
            if (blockList.size() == 1) {
                beginBlock = b;
            }
            endBlock = b;
        }
        forwardSensorBox.setSelectedItem(curSection.getForwardBlockingSensor());
        reverseSensorBox.setSelectedItem(curSection.getReverseBlockingSensor());
        forwardStopSensorBox.setSelectedItem(curSection.getForwardStoppingSensor());
        reverseStopSensorBox.setSelectedItem(curSection.getReverseStoppingSensor());
        List<EntryPoint> list = curSection.getForwardEntryPointList();
        if (list.size() > 0) {
            for (int j = 0; j < list.size(); j++) {
                entryPointList.add(list.get(j));
            }
        }
        list = curSection.getReverseEntryPointList();
        if (list.size() > 0) {
            for (int j = 0; j < list.size(); j++) {
                entryPointList.add(list.get(j));
            }
        }
    }

    private void clearForCreate() {
        deleteBlocksPressed(null);
        curSection = null;
        forwardSensorBox.setSelectedItem(null);
        reverseSensorBox.setSelectedItem(null);
        forwardStopSensorBox.setSelectedItem(null);
        reverseStopSensorBox.setSelectedItem(null);
        generationStateLabel.setText("");
    }

    void createPressed(ActionEvent e) {
        if (!checkSectionInformation()) {
            return;
        }
        String uName = userName.getText();
        if (uName.isEmpty()) {
            uName = null;
        }

        // attempt to create the new Section
        String sName = sysName.getText();
        try {
            if (_autoSystemName.isSelected()) {
                curSection = sectionManager.createNewSection(uName);
            } else {
                curSection = sectionManager.createNewSection(sName, uName);
            }
        } catch (IllegalArgumentException ex) {
            // user input no good
            if (_autoSystemName.isSelected()) {
                handleCreateException(uName, ex);
            } else {
                handleCreateException(sName, ex);
            }
            return; // without creating any
        }
        sysName.setText(curSection.getSystemName());
        setSectionInformation();
        addFrame.setVisible(false);
        blockTableModel.dispose();
        addFrame.dispose();
        addFrame = null;
        pref.setSimplePreferenceState(systemNameAuto, _autoSystemName.isSelected());
    }

    void handleCreateException(String sysName, Exception ex) {
        JmriJOptionPane.showMessageDialog(addFrame,
                Bundle.getMessage("ErrorSectionAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck")
                + "\n" + ex.getLocalizedMessage(),
                Bundle.getMessage("ErrorTitle"),
                JmriJOptionPane.ERROR_MESSAGE);
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        blockTableModel.dispose();
        addFrame.dispose();
        addFrame = null;
    }

    void updatePressed(ActionEvent e) {
        if (!checkSectionInformation()) {
            return;
        }
        // check if user name has been changed
        String uName = userName.getText();
        if (uName.isEmpty()) {
            uName = null;
        }
        if ((uName != null) && (!uName.equals(curSection.getUserName()))) {
            // check that new user name is unique
            Section tSection = sectionManager.getByUserName(uName);
            if (tSection != null) {
                JmriJOptionPane.showMessageDialog(addFrame,
                        rbx.getString("Message2"),
                        Bundle.getMessage("ErrorTitle"),
                        JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        curSection.setUserName(uName);
        if (setSectionInformation()) {
            // successful update
            addFrame.setVisible(false);
            blockTableModel.dispose();
            addFrame.dispose();
            addFrame = null;
        }
    }

    private boolean checkSectionInformation() {
        if (blockList.isEmpty()) {
            JmriJOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message6"), Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        // check entry points
        boolean unknownPresent = false;
        for (int i = 0; i < entryPointList.size(); i++) {
            if (entryPointList.get(i).isUnknownType()) {
                unknownPresent = true;
            }
        }
        if (unknownPresent) {
            JmriJOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message10"), Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }

        // check direction sensors
        fSensor = forwardSensorBox.getSelectedItem();
        rSensor = reverseSensorBox.getSelectedItem();
        if ((fSensor != null) && (fSensor.equals(rSensor))) {
            JmriJOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message9"), Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }

        // check stopping sensors
        fStopSensor = forwardStopSensorBox.getSelectedItem();
        rStopSensor = reverseStopSensorBox.getSelectedItem();

        return true;
    }

    /**
     * Copy the user input to the Section.
     *
     * @return true if completed
     */
    private boolean setSectionInformation() {
        curSection.removeAllBlocksFromSection();
        for (int i = 0; i < blockList.size(); i++) {
            if (!curSection.addBlock(blockList.get(i))) {
                JmriJOptionPane.showMessageDialog(addFrame, rbx
                        .getString("Message4"), Bundle.getMessage("ErrorTitle"),
                        JmriJOptionPane.ERROR_MESSAGE);
            }
        }
        curSection.setForwardBlockingSensorName(forwardSensorBox.getSelectedItemDisplayName());
        curSection.setReverseBlockingSensorName(reverseSensorBox.getSelectedItemDisplayName());
        curSection.setForwardStoppingSensorName(forwardStopSensorBox.getSelectedItemDisplayName());
        curSection.setReverseStoppingSensorName(reverseStopSensorBox.getSelectedItemDisplayName());
        for (int j = 0; j < entryPointList.size(); j++) {
            EntryPoint ep = entryPointList.get(j);
            if (ep.isForwardType()) {
                curSection.addToForwardList(ep);
            } else if (ep.isReverseType()) {
                curSection.addToReverseList(ep);
            }
        }
        return true;
    }

    void deleteBlocksPressed(ActionEvent e) {
        for (int j = blockList.size(); j > 0; j--) {
            blockList.remove(j - 1);
        }
        beginBlock = null;
        endBlock = null;
        initializeBlockCombo();
        initializeEntryPoints();
        blockTableModel.fireTableDataChanged();
    }

    void addBlockPressed(ActionEvent e) {
        if (blockBox.getItemCount() == 0) {
            JmriJOptionPane.showMessageDialog(addFrame, rbx
                    .getString("Message5"), Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return;
        }
        Block b = blockBox.getSelectedItem();
        if (b != null) {
            blockList.add(b);
            if (blockList.size() == 1) {
                beginBlock = b;
            }
            endBlock = b;
            initializeBlockCombo();
            initializeEntryPoints();
            blockTableModel.fireTableDataChanged();
        }
    }

    private boolean initializeLayoutEditorCombo(JComboBox<String> box) {
        // get list of Layout Editor panels
        lePanelList = new ArrayList<>(InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class));
        if (lePanelList.isEmpty()) {
            return false;
        }
        box.removeAllItems();
        box.addItem("<" + Bundle.getMessage("None").toLowerCase() + ">");
        for (int i = 0; i < lePanelList.size(); i++) {
            box.addItem(lePanelList.get(i).getTitle());
        }
        box.setSelectedIndex(1);
        return true;
    }

    private void layoutEditorSelectionChanged() {
        int i = layoutEditorBox.getSelectedIndex();
        if ((i <= 0) || (i > lePanelList.size())) {
            curLayoutEditor = null;
        } else {
            curLayoutEditor = lePanelList.get(i - 1);
        }
    }

    /**
     * Build a combo box to select Blocks for this Section.
     */
    private void initializeBlockCombo() {
        if (blockBox == null) {
            blockBox = new NamedBeanComboBox<>(InstanceManager.getDefault(BlockManager.class));
        }
        blockBox.setSelectedItem(null);
        if (blockList.isEmpty()) {
            // No blocks selected, all blocks are eligible
            blockBox.setExcludedItems(new HashSet<>());
        } else {
            // limit combo list to Blocks bonded to the currently selected Block that are not already in the Section
            Set<Block> excludes = new HashSet<>(InstanceManager.getDefault(BlockManager.class).getNamedBeanSet());
            for (Block b : blockManager.getNamedBeanSet()) {
                if ((!inSection(b)) && connected(b, endBlock)) {
                    excludes.remove(b);
                }
            }
            blockBox.setExcludedItems(excludes);
        }
        if (blockBox.getItemCount()> 0) {
            blockBox.setSelectedIndex(0);
            JComboBoxUtil.setupComboBoxMaxRows(blockBox);
        }
    }

    private boolean inSection(Block b) {
        for (int i = 0; i < blockList.size(); i++) {
            if (blockList.get(i) == b) {
                return true;
            }
        }
        return false;
    }

    private boolean connected(Block b1, Block b2) {
        if ((b1 != null) && (b2 != null)) {
            List<Path> paths = b1.getPaths();
            for (int i = 0; i < paths.size(); i++) {
                if (paths.get(i).getBlock() == b2) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initializeEntryPoints() {
        // Copy old Entry Point List, if there are entries, and clear it.
        ArrayList<EntryPoint> oldList = new ArrayList<>();
        for (int i = 0; i < entryPointList.size(); i++) {
            oldList.add(entryPointList.get(i));
        }
        entryPointList.clear();
        if (blockList.size() > 0) {
            // cycle through Blocks to find Entry Points
            for (int i = 0; i < blockList.size(); i++) {
                Block sb = blockList.get(i);
                List<Path> paths = sb.getPaths();
                for (int j = 0; j < paths.size(); j++) {
                    Path p = paths.get(j);
                    if (!inSection(p.getBlock())) {
                        // this is path to an outside block, so need an Entry Point
                        String pbDir = Path.decodeDirection(p.getFromBlockDirection());
                        EntryPoint ep = getEntryPointInList(oldList, sb, p.getBlock(), pbDir);
                        if (ep == null) {
                            ep = new EntryPoint(sb, p.getBlock(), pbDir);
                        }
                        entryPointList.add(ep);
                    }
                }
            }
            // Set directions where possible
            ArrayList<EntryPoint> epList = getBlockEntryPointsList(beginBlock);
            if ((epList.size() == 2) && (blockList.size() == 1)) {
                if (((epList.get(0)).isUnknownType())
                        && ((epList.get(1)).isUnknownType())) {
                    (epList.get(0)).setTypeForward();
                    (epList.get(1)).setTypeReverse();
                }
            } else if (epList.size() == 1) {
                (epList.get(0)).setTypeForward();
            }
            epList = getBlockEntryPointsList(endBlock);
            if (epList.size() == 1) {
                (epList.get(0)).setTypeReverse();
            }
        }
// djd debugging
// here add code to use Layout Editor connectivity if desired in the future
/*  if (!manualEntryPoints) {
         // use Layout Editor connectivity to set directions of Entry Points that have UNKNOWN direction
         // check entry points for first Block
         ArrayList<EntryPoint> tEPList = getSubEPListForBlock(beginBlock);
         EntryPoint firstEP = null;
         int numUnknown = 0;
         for (int i = 0; i<tEPList.size(); i++) {
         if (tEPList.get(i).getDirection==EntryPoint.UNKNOWN) numUnknown ++;
         else if (firstEP==null) firstEP = tEPList.get(i);
         }
         if (numUnknown>0) {
         // first Block has unknown entry point(s)
         if ( (firstEP!=null) && (blockList.getSize()==1) ) {
         firstEP = tEPList.get(0);
         firstEP.setTypeForward();
         }
         else if (firstEP==null) {
         // find connection from the second Block
         }
         }   */
        entryPointTableModel.fireTableDataChanged();
    }
    /* private ArrayList<EntryPoint> getSubEPListForBlock(Block b) {
     ArrayList<EntryPoint> tList = new ArrayList<EntryPoint>0;
     for (int i=0; i<eplist.size(); i++) {
     EntryPoint tep = epList.get(i);
     if (epList.getBlock()==b) {
     tList.add(tep);
     }
     }
     return tList;
     } */
// end djd debugging

    private EntryPoint getEntryPointInList(ArrayList<EntryPoint> list, Block b, Block pb, String pbDir) {
        for (int i = 0; i < list.size(); i++) {
            EntryPoint ep = list.get(i);
            if ((ep.getBlock() == b) && (ep.getFromBlock() == pb)
                    && (pbDir.equals(ep.getFromBlockDirection()))) {
                return ep;
            }
        }
        return null;
    }

    private ArrayList<EntryPoint> getBlockEntryPointsList(Block b) {
        ArrayList<EntryPoint> list = new ArrayList<>();
        for (int i = 0; i < entryPointList.size(); i++) {
            EntryPoint ep = entryPointList.get(i);
            if (ep.getBlock() == b) {
                list.add(ep);
            }
        }
        return list;
    }

    /**
     * Special processing when user requests deletion of a Section.
     * <p>
     * Standard BeanTable processing results in misleading information.
     */
    private void deleteSectionPressed(String sName) {
        final Section s = InstanceManager.getDefault(SectionManager.class).getBySystemName(sName);
        if (s == null){
            throw new IllegalArgumentException("Not deleting Section :" + sName + ": , Not Found.");
        }
        String fullName = s.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        ArrayList<Transit> affectedTransits = InstanceManager.getDefault(TransitManager.class).getListUsingSection(s);
        final JDialog dialog = new JDialog();
        String msg;
        dialog.setTitle(Bundle.getMessage("WarningTitle"));
        dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        if (affectedTransits.size() > 0) {
            // special warning if Section is used in Transits
            JLabel iLabel = new JLabel(java.text.MessageFormat.format(rbx.getString("Message17"), fullName));
            p1.add(iLabel);
            dialog.add(p1);
            for (int i = 0; i < affectedTransits.size(); i++) {
                Transit aTransit = affectedTransits.get(i);
                String tFullName = aTransit.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
                p1 = new JPanel();
                p1.setLayout(new FlowLayout());
                iLabel = new JLabel("   " + tFullName);
                p1.add(iLabel);
                dialog.add(p1);
            }
            dialog.add(p1);
            JPanel p3 = new JPanel();
            p3.setLayout(new FlowLayout());
            JLabel question = new JLabel(rbx.getString("Message18"));
            p3.add(question);
            dialog.add(p3);
            JPanel p4 = new JPanel();
            p4.setLayout(new FlowLayout());
            question = new JLabel(rbx.getString("Message18a"));
            p4.add(question);
            dialog.add(p4);
        } else {
            msg = java.text.MessageFormat.format(rbx.getString("Message19"), fullName);
            JLabel question = new JLabel(msg);
            p1.add(question);
            dialog.add(p1);
        }
        JPanel p6 = new JPanel();
        p6.setLayout(new FlowLayout());
        JLabel quest = new JLabel(rbx.getString("Message20"));
        p6.add(quest);
        dialog.add(p6);
        JButton yesButton = new JButton(rbx.getString("YesDeleteIt"));
        JButton noButton = new JButton(rbx.getString("NoCancel"));
        JPanel button = new JPanel();
        button.add(yesButton);
        button.add(noButton);
        dialog.add(button);

        noButton.addActionListener((ActionEvent e) -> {
            // user cancelled delete request
            dialog.dispose();
        });

        yesButton.addActionListener((ActionEvent e) -> {
            InstanceManager.getDefault(SectionManager.class).deregister(s);
            s.dispose();
            dialog.dispose();
        });
        dialog.pack();
        dialog.setModal(true);
        dialog.setVisible(true);
    }

    /**
     * Insert 2 table specific menus.
     * Account for the Window and Help menus, which are already added to the menu bar
     * as part of the creation of the JFrame, by adding the menus 2 places earlier
     * unless the table is part of the ListedTableFrame, that adds the Help menu later on.
     *
     * @param f the JFrame of this table
     */
    @Override
    public void setMenuBar(BeanTableFrame<Section> f) {
        frame = f;
        JMenuBar menuBar = f.getJMenuBar();
        int pos = menuBar.getMenuCount() -1; // count the number of menus to insert the TableMenu before 'Window' and 'Help'
        int offset = 1;
        log.debug("setMenuBar number of menu items = {}", pos);
        for (int i = 0; i <= pos; i++) {
            if (menuBar.getComponent(i) instanceof JMenu) {
                if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
                    offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
                }
            }
        }
        JMenu toolsMenu = new JMenu(Bundle.getMessage("MenuTools"));
        menuBar.add(toolsMenu, pos + offset);
        JMenuItem validate = new JMenuItem(rbx.getString("ValidateAllSections") + "...");
        toolsMenu.add(validate);
        validate.addActionListener((ActionEvent e) -> {
            if (sectionManager != null) {
                int n = sectionManager.validateAllSections();
                if (n > 0) {
                    JmriJOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(
                            rbx.getString("Message14"), new Object[]{"" + n}),
                            Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                } else if (n == -2) {
                    JmriJOptionPane.showMessageDialog(frame, rbx.getString("Message16"),
                            Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                } else if (n == 0) {
                    JmriJOptionPane.showMessageDialog(frame, rbx.getString("Message15"),
                            Bundle.getMessage("MessageTitle"), JmriJOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        JMenuItem setDirSensors = new JMenuItem(rbx.getString("SetupDirectionSensors") + "...");
        toolsMenu.add(setDirSensors);
        setDirSensors.addActionListener((ActionEvent e) -> {
            if (sectionManager != null) {
                int n = sectionManager.setupDirectionSensors();
                if (n > 0) {
                    JmriJOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(
                            rbx.getString("Message27"), new Object[]{"" + n}),
                            Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                } else if (n == -2) {
                    JmriJOptionPane.showMessageDialog(frame, rbx.getString("Message30"),
                            Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                } else if (n == 0) {
                    JmriJOptionPane.showMessageDialog(frame, rbx.getString("Message28"),
                            Bundle.getMessage("MessageTitle"), JmriJOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        JMenuItem removeDirSensors = new JMenuItem(rbx.getString("RemoveDirectionSensors") + "...");
        toolsMenu.add(removeDirSensors);
        removeDirSensors.addActionListener((ActionEvent e) -> {
            if (sectionManager != null) {
                int n = sectionManager.removeDirectionSensorsFromSSL();
                if (n > 0) {
                    JmriJOptionPane.showMessageDialog(frame, java.text.MessageFormat.format(
                            rbx.getString("Message33"), new Object[]{"" + n}),
                            Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                } else if (n == -2) {
                    JmriJOptionPane.showMessageDialog(frame, rbx.getString("Message32"),
                            Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                } else if (n == 0) {
                    JmriJOptionPane.showMessageDialog(frame, rbx.getString("Message31"),
                            Bundle.getMessage("MessageTitle"), JmriJOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    JmriJFrame frame = null;

    /**
     * Table model for Blocks in Create/Edit Section window
     */
    public class BlockTableModel extends javax.swing.table.AbstractTableModel implements
            java.beans.PropertyChangeListener {

        public static final int SNAME_COLUMN = 0;

        public static final int UNAME_COLUMN = 1;

        public BlockTableModel() {
            super();
            init();
        }

        final void init(){
            blockManager.addPropertyChangeListener(this);
        }

        public void dispose(){
            blockManager.removePropertyChangeListener(this);
        }

        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new NamedBean is available in the manager
                fireTableDataChanged();
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return (blockList.size());
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return (false);
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case SNAME_COLUMN:
                    return Bundle.getMessage("LabelSystemName");
                case UNAME_COLUMN:
                    return Bundle.getMessage("LabelUserName");
                default:
                    return "";
            }
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case SNAME_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                case UNAME_COLUMN:
                    return new JTextField(17).getPreferredSize().width;
                default:
                    return new JTextField(5).getPreferredSize().width;
            }
        }

        @Override
        public Object getValueAt(int r, int c) {
            int rx = r;
            if (rx > blockList.size()) {
                return null;
            }
            switch (c) {
                case SNAME_COLUMN:
                    return blockList.get(rx).getSystemName();
                case UNAME_COLUMN: //
                    return blockList.get(rx).getUserName();
                default:
                    return Bundle.getMessage("BeanStateUnknown");
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
        }
    }

    private void autoSystemName() {
        if (_autoSystemName.isSelected()) {
            sysName.setEnabled(false);
            sysName.setText("");
            sysNameLabel.setEnabled(false);
        } else {
            sysName.setEnabled(true);
            sysNameLabel.setEnabled(true);
        }
    }

    /**
     * Table model for Entry Points in Create/Edit Section window.
     */
    public class EntryPointTableModel extends javax.swing.table.AbstractTableModel {

        public static final int BLOCK_COLUMN = 0;

        public static final int TO_BLOCK_COLUMN = 1;

        public static final int DIRECTION_COLUMN = 2;

        public EntryPointTableModel() {
            super();
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == DIRECTION_COLUMN) {
                return JComboBox.class;
            }
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return (entryPointList.size());
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if (c == DIRECTION_COLUMN) {
                if (!manualEntryPoints) {
                    return (false);
                } else if (r < entryPointList.size()) {
                    return (!entryPointList.get(r).isFixed());
                }
                return (true);
            }
            return (false);
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case BLOCK_COLUMN:
                    return rbx.getString("FromBlock");

                case TO_BLOCK_COLUMN:
                    return rbx.getString("ToBlock");

                case DIRECTION_COLUMN:
                    return rbx.getString("TravelDirection");
                default:
                    return "";
            }
        }

        public int getPreferredWidth(int col) {
            if (col == BLOCK_COLUMN || col == TO_BLOCK_COLUMN)
            {
                return new JTextField(37).getPreferredSize().width;
            }
            if (col == DIRECTION_COLUMN) {
                return new JTextField(9).getPreferredSize().width;
            }
            return new JTextField(5).getPreferredSize().width;
        }

        @Override
        public Object getValueAt(int r, int c) {
            int rx = r;
            if (rx >= entryPointList.size()) {
                return null;
            }
            switch (c) {
                case BLOCK_COLUMN:
                    return entryPointList.get(rx).getFromBlockName();

                case TO_BLOCK_COLUMN:
                    return entryPointList.get(rx).getBlock().getDisplayName();

                case DIRECTION_COLUMN: //
                    if (entryPointList.get(rx).isForwardType()) {
                        return rbx.getString("SectionForward");
                    } else if (entryPointList.get(rx).isReverseType()) {
                        return rbx.getString("SectionReverse");
                    } else {
                        return Bundle.getMessage("BeanStateUnknown");
                    }
                default:
                    // fall through
                    break;
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == DIRECTION_COLUMN) {
                if (((String) value).equals(rbx.getString("SectionForward"))) {
                    entryPointList.get(row).setTypeForward();
                } else if (((String) value).equals(rbx.getString("SectionReverse"))) {
                    entryPointList.get(row).setTypeReverse();
                } else if (((String) value).equals(Bundle.getMessage("BeanStateUnknown"))) {
                    entryPointList.get(row).setTypeUnknown();
                }
            }
        }

    }

    @Override
    protected String getClassName() {
        return SectionTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleSectionTable");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SectionTableAction.class);

}
