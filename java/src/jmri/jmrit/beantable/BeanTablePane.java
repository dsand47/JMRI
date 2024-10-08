package jmri.jmrit.beantable;

import java.awt.Component;

import javax.swing.*;
import javax.swing.table.TableRowSorter;

import jmri.NamedBean;
import jmri.swing.RowSorterUtil;

/**
 * Provide a JPanel to display a table of NamedBeans.
 * <p>
 * This frame includes the table itself at the top, plus a "bottom area" for
 * things like an Add... button and checkboxes that control display options.
 * <p>
 * The usual menus are also provided here.
 * <p>
 * Specific uses are customized via the BeanTableDataModel implementation they
 * provide, and by providing a {@link #extras} implementation that can in turn
 * invoke {@link #addToBottomBox} as needed.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @param <E> the Bean displayed in the Pane.................
 */
public class BeanTablePane<E extends NamedBean> extends jmri.util.swing.JmriPanel {

    private BeanTableDataModel<E> dataModel;
    private JTable dataTable;
    private JScrollPane dataScroll;
    private JPanel bottomBox;  // panel at bottom for extra buttons etc
    static final int bottomStrutWidth = 20;

    public void init(BeanTableDataModel<E> model) {

        dataModel = model;

        TableRowSorter<BeanTableDataModel<E>> sorter = new TableRowSorter<>(dataModel);
        dataTable = dataModel.makeJTable(dataModel.getMasterClassName(), dataModel, sorter);
        dataScroll = new JScrollPane(dataTable);

        // use NamedBean's built-in Comparator interface for sorting the system name column
        RowSorterUtil.setSortOrder(sorter, BeanTableDataModel.SYSNAMECOL, SortOrder.ASCENDING);
        this.dataTable.setRowSorter(sorter);

        // configure items for GUI
        dataModel.configureTable(dataTable);

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // install items in GUI
        add(dataScroll);
        bottomBox = new JPanel();
        bottomBox.setLayout(new jmri.util.swing.WrapLayout(jmri.util.swing.WrapLayout.LEFT, bottomStrutWidth, 5));

        add(bottomBox);

        // add extras, if desired by subclass
        extras();

        // set Viewport preferred size from size of table
        java.awt.Dimension dataTableSize = dataTable.getPreferredSize();
        // width is right, but if table is empty, it's not high
        // enough to reserve much space.
        dataTableSize.height = Math.max(dataTableSize.height, 400);
        dataScroll.getViewport().setPreferredSize(dataTableSize);

        // set preferred scrolling options
        dataScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        dataScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    }

    /**
     * Hook to allow sub-types to install more items in GUI.
     */
    void extras() {
    }

    /**
     * Add a component to the bottom box.
     * @param comp {@link Component} to add
     */
    protected void addToBottomBox(Component comp) {
        bottomBox.add(comp);
    }

    @Override
    public void dispose() {
        if (dataModel != null) {
            dataModel.dispose();
        }
        dataModel = null;
        dataTable = null;
        dataScroll = null;
    }
}
