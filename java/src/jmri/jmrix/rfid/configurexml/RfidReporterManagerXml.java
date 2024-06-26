package jmri.jmrix.rfid.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring RfidReporterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class RfidReporterManagerXml extends jmri.managers.configurexml.AbstractReporterManagerConfigXML {

    public RfidReporterManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual sensors
        return loadReporters(shared);
    }

//    private static final Logger log = LoggerFactory.getLogger(RfidReporterManagerXml.class);
}
