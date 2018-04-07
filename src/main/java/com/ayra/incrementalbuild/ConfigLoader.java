package com.ayra.incrementalbuild;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

class ConfigLoader {

    private final static String PATH = "config/config.xml";
    private IConfig config = null;

    ConfigLoader() {
        getDataFromConfigXML(PATH);
    }

    private void getDataFromConfigXML(String xmlFile) {
        File file = new File(xmlFile);
        IConfig config_ = null;
        try {
            JAXBContext context = JAXBContext.newInstance(Config.class);
            Unmarshaller um = context.createUnmarshaller();
            config_ = (IConfig) um.unmarshal(file);
            setConfig(config_);
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
    }

    private void setConfig(IConfig config) {
        this.config = config;
    }

    IConfig getConfig() {
        return config;
    }
}
