package jmri.jmrit.catalog;

import jmri.NamedBean;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the CatalogTreeIndex class
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class CatalogTreeIndexTest {

    // class carries its own implementation of the
    // get/set parameter code, so we test that here
    @Test
    public void testSetProperty() {
        NamedBean n = new CatalogTreeIndex("sys", "usr") {
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }
        };

        n.setProperty("foo", "bar");
    }

    @Test
    public void testGetParameter() {
        NamedBean n = new CatalogTreeIndex("sys", "usr") {
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }
        };

        n.setProperty("foo", "bar");
        Assert.assertEquals("bar", n.getProperty("foo"));
    }

    @Test
    public void testGetSetNull() {
        NamedBean n = new CatalogTreeIndex("sys", "usr") {
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }
        };

        n.setProperty("foo", "bar");
        Assert.assertEquals("bar", n.getProperty("foo"));
        n.setProperty("foo", null);
        Assert.assertEquals(null, n.getProperty("foo"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
