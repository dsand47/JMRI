package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrackLoadEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        TrackLoadEditFrame t = new TrackLoadEditFrame();
        Assert.assertNotNull("exists",t);
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);
        
        t.initComponents(loc, null);
        Assert.assertTrue(t.isVisible());
        
        JUnitUtil.dispose(t);

    }
    
    @Test
    public void testFrameButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        TrackLoadEditFrame tlef = new TrackLoadEditFrame();
        Assert.assertNotNull("exists",tlef);
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);
        
        Track track = loc.getTrackByName("NI Yard", null);
        
        tlef.initComponents(loc, track);
        Assert.assertTrue(tlef.isVisible());
        
        JemmyUtil.enterClickAndLeave(tlef.loadNameInclude);
        JemmyUtil.enterClickAndLeave(tlef.loadAndTypeCheckBox);
        JemmyUtil.enterClickAndLeaveThreadSafe(tlef.saveButton);
        // error dialog window show appear
        JemmyUtil.pressDialogButton(tlef, Bundle.getMessage("ErrorNoLoads"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(tlef);
        
        // now add a load "Boxcar & E"
        JemmyUtil.enterClickAndLeave(tlef.addLoadButton);
        JemmyUtil.enterClickAndLeave(tlef.saveButton);
        
        Assert.assertTrue(track.isLoadNameAndCarTypeAccepted("E", "Boxcar"));
        Assert.assertFalse(track.isLoadNameAndCarTypeAccepted("L", "Boxcar"));
        
        Assert.assertFalse(track.isLoadNameAccepted("L"));
        Assert.assertFalse(track.isLoadNameAccepted("E"));
        
        JUnitUtil.dispose(tlef);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Track track = loc.addTrack("Yard", Track.YARD);
        TrackLoadEditFrame f = new TrackLoadEditFrame();
        f.initComponents(loc, track);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackLoadEditFrameTest.class);

}
