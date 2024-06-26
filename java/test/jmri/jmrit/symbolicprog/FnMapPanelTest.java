package jmri.jmrit.symbolicprog;

import java.util.List;

import javax.swing.JLabel;

import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Base for tests of classes inheriting from FnMapPanel abstract class
 *
 * @author Bob Jacobsen, Copyright 2009
 */
public class FnMapPanelTest {

    @Test
    public void testCtor() {
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer();
        VariableTableModel tableModel = new VariableTableModel(
                new JLabel(""),
                new String[]{"Name", "Value"},
                new CvTableModel(new JLabel(""), p)
        );
        List<Integer> varsUsed = new java.util.ArrayList<>();
        Element model = new Element("model");

        FnMapPanel t = new FnMapPanel(tableModel, varsUsed, model);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testLargeNumbers() {
        jmri.Programmer p = jmri.InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer();
        VariableTableModel tableModel = new VariableTableModel(
                new JLabel(""),
                new String[]{"Name", "Value"},
                new CvTableModel(new JLabel(""), p)
        );
        List<Integer> varsUsed = new java.util.ArrayList<>();
        Element model = new Element("model");
        model.setAttribute("numFns", "28");

        FnMapPanel t = new FnMapPanel(tableModel, varsUsed, model);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDebugProgrammerManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
