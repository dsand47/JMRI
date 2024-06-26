package jmri.util.swing;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JmriPanelTest {

    protected JmriPanel panel = null;
    protected String helpTarget = null;
    protected String title = null;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", panel);
    }

    @Test
    public void testInitComponents() throws Exception{
        // for now, just make sure there isn't an exception.
        panel.initComponents();
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAccessibility() throws Exception{
        panel.initComponents();
        jmri.util.AccessibilityChecks.check(panel);
    }

    @Test
    public void testGetHelpTarget(){
        Assert.assertEquals("help target", helpTarget, panel.getHelpTarget());
    }

    @Test
    public void testGetTitle(){
        Assert.assertEquals("title", title, panel.getTitle());
    }

    @Test
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    public void testGetMenus() {
        Assertions.assertNotNull(panel.getMenus());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new JmriPanel();
        helpTarget = "package.jmri.util.swing.JmriPanel";
    }

    @AfterEach
    public void tearDown() {
        if (panel!=null) {
           panel.dispose();
        }
        panel = null;
        helpTarget = null;
        title = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JmriPanelTest.class);

}
