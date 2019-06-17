package com.ayra.incrementalbuild.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("unused")
@XmlRootElement(name = "module")
public class Module {

    @XmlElement(name = "releasePathPrevious")
    private String releasePathPrevious;
    @XmlElement(name = "releasePathPresent")
    private String releasePathPresent;
    @XmlElement(name = "incrementalJarsPath")
    private String incrementalJarsOutput;
    @XmlElement(name = "releaseVersion")
    private String releaseVersion;

    public String getReleasePathPrevious() {
        return releasePathPrevious;
    }

    public String getReleasePathPresent() {
        return releasePathPresent;
    }

    public String getIncrementalJarsOutput() {
        return incrementalJarsOutput;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }
}
