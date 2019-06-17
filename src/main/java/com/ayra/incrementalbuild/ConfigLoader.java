package com.ayra.incrementalbuild;

import com.ayra.incrementalbuild.config.Config;
import com.ayra.incrementalbuild.config.IConfig;

import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

@Singleton
class ConfigLoader {

    private final static String PATH = "config/config.xml";
    private IConfig config;

    ConfigLoader() {
        getDataFromConfigXML();
    }

    private void getDataFromConfigXML() {
        File file = new File(PATH);
        try {
            JAXBContext context = JAXBContext.newInstance(Config.class);
            Unmarshaller um = context.createUnmarshaller();
            this.config = (IConfig) um.unmarshal(file);
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
    }

    IConfig getConfig() {
        return this.config;
    }
}
