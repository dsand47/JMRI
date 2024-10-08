package jmri.configurexml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 * Load and store scale xml data files.
 * <p>
 * Custom changes to scale information result in the scale data being stored
 * at the user files location.  Subsequent scale data loading uses the custom
 * data file.  The default scale data file is part of the JMRI distribution.
 * @author Dave Sand Copyright (C) 2018
 * @since 4.13.6
 */
public class ScaleConfigXML {

    /**
     * Prevent public Construction, class only supplies static methods.
     */
    private ScaleConfigXML(){}

    /**
     * Store the Scales to file.
     * @return true on success, else false.
     */
    public static boolean doStore() {
        ScaleXmlFile x = new ScaleXmlFile();
        File file = x.getStoreFile();
        if (file == null) {
            log.warn("Unable to create local scale file");
            return false;
        }

        // Create root element
        Element root = new Element("scale-data");  // NOI18N
        root.setAttribute("noNamespaceSchemaLocation",  // NOI18N
                "http://jmri.org/xml/schema/scale.xsd",  // NOI18N
                org.jdom2.Namespace.getNamespace("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance"));  // NOI18N
        Document doc = new Document(root);
        Element values = new Element("scales");

        root.addContent(values);
        for (jmri.Scale scale : jmri.ScaleManager.getScales()) {
            Element e = new Element("scale");  // NOI18N
            e.addContent(new Element("scale_name").addContent(scale.getScaleName()));  // NOI18N
            e.addContent(new Element("user_name").addContent(scale.getUserName()));  // NOI18N
            e.addContent(new Element("scale_ratio").addContent(Double.toString(scale.getScaleRatio())));  // NOI18N
            values.addContent(e);
        }

        try {
            x.writeXML(file, doc);
        } catch (FileNotFoundException ex) {
            log.error("File not found when writing", ex);  // NOI18N
            return false;
        } catch (IOException ex) {
            log.error("IO Exception when writing", ex);  // NOI18N
            return false;
        }

        return true;
    }

    /**
     * Load the Scales from file.
     * Checks userFilesPath then programPath for resources/scales/ScaleData.xml
     * @return true on success, else false.
     */
    public static boolean doLoad() {
        ScaleXmlFile x = new ScaleXmlFile();
        File file = x.getLoadFile();
        if ( file == null ) {
            log.error("Scale File {} not located", ScaleXmlFile.FILENAME);
            return false;
        }

        // Find root
        Element root;
        try {
            root = x.rootFromFile(file);
            if (root == null) {
                log.debug("File could not be read");  // NOI18N
                return false;
            }

            Element scales = root.getChild("scales");  // NOI18N
            if (scales == null) {
                log.error("Unable to find a scale entry");  // NOI18N
                return false;
            }
            Element e;
            for (Element scale : scales.getChildren("scale")) {  // NOI18N
                e = scale.getChild("scale_name");  // NOI18N
                String scaleName = (e == null) ? "" : e.getValue();
                e = scale.getChild("user_name");  // NOI18N
                String userName = (e == null) ? "" : e.getValue();
                e = scale.getChild("scale_ratio");  // NOI18N
                double scaleRatio = (e == null) ? 1.0 : Double.parseDouble(e.getValue());

                jmri.ScaleManager.addScale(scaleName, userName, scaleRatio);
            }

        } catch (JDOMException ex) {
            log.error("File invalid", ex);  // NOI18N
            return false;
        } catch (IOException ex) {
            log.error("Error reading file", ex);  // NOI18N
            return false;
        }

        return true;
    }

    private static class ScaleXmlFile extends XmlFile {
        private static final String PROG_PATH = FileUtil.getProgramPath() + "resources/scales/";  // NOI18N
        private static final String USER_PATH = FileUtil.getUserFilesPath() + "resources/scales/";  // NOI18N
        private static final String FILENAME = "ScaleData.xml";  // NOI18N

        static String getStoreFileName() {
            return USER_PATH + FILENAME;
        }

        @javax.annotation.CheckForNull
        File getStoreFile() {
            File chkdir = new File(USER_PATH);
            if (!chkdir.exists() && !chkdir.mkdirs()) {
                return null;
            }
            File file = findFile(getStoreFileName());
            if (file == null) {
                log.info("Create new scale file");  // NOI18N
                file = new File(getStoreFileName());
            } else {
                try {
                    FileUtil.rotate(file, 4, "bup");  // NOI18N
                } catch (IOException ex) {
                    log.warn("Rotate failed, reverting to xml backup");  // NOI18N
                    makeBackupFile(getStoreFileName());
                }
            }
            return file;
        }

        @javax.annotation.CheckForNull
        File getLoadFile() {
            File file = findFile(USER_PATH + FILENAME);
            if (file == null) {
                file = findFile(PROG_PATH + FILENAME);
            }
            return file;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScaleConfigXML.class);

}
