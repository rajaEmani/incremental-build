package com.ayra.incrementalbuild.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@SuppressWarnings("unused")
@XmlRootElement(name = "config")
public class Config implements IConfig {

    @XmlElementWrapper(name = "modules")
    @XmlElement(name = "module")
    private List<Module> modules;

    public List<Module> getModules() {
        return modules;
    }
}
