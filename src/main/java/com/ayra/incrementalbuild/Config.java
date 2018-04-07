package com.ayra.incrementalbuild;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "config")
public class Config implements IConfig {

    private String previousServerPath;
    private String presentServerPath;
    private String previousClientPath;
    private String presentClientPath;
    private String incrementalSeverPath;
    private String incrementalClientPath;
    private String releaseVersion;
    private String flag;
    private String manifestChangeForVasco;

    @SuppressWarnings("unused")
    public void setPresentServerPath(String presentServerPath) {
        this.presentServerPath = presentServerPath;
    }

    @SuppressWarnings("unused")
    public void setPreviousServerPath(String previousServerPath) {
        this.previousServerPath = previousServerPath;
    }

    @SuppressWarnings("unused")
    public void setPreviousClientPath(String previousClientPath) {
        this.previousClientPath = previousClientPath;
    }

    @SuppressWarnings("unused")
    public void setPresentClientPath(String presentClientPath) {
        this.presentClientPath = presentClientPath;
    }

    @SuppressWarnings("unused")
    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    @SuppressWarnings("unused")
    public void setIncrementalSeverPath(String incrementalSeverPath) {
        this.incrementalSeverPath = incrementalSeverPath;
    }

    @SuppressWarnings("unused")
    public void setIncrementalClientPath(String incrementalClientPath) {
        this.incrementalClientPath = incrementalClientPath;
    }

    @SuppressWarnings("unused")
    public void setFlag(String flag) {
        this.flag = flag;
    }

    @SuppressWarnings("unused")
    public void setManifestChangeForVasco(String manifestChangeForVasco) {
        this.manifestChangeForVasco = manifestChangeForVasco;
    }

    @Override
    public String getPreviousServerPath() {
        return previousServerPath;
    }

    @Override
    public String getPreviousClientPath() {
        return previousClientPath;
    }

    @Override
    public String getPresentServerPath() {
        return presentServerPath;
    }

    @Override
    public String getPresentClientPath() {
        return presentClientPath;
    }

    @Override
    public String getReleaseVersion() {
        return releaseVersion;
    }

    @Override
    public String getIncrementalSeverPath() {
        return incrementalSeverPath;
    }

    @Override
    public String getIncrementalClientPath() {
        return incrementalClientPath;
    }

    @Override
    public String getFlag() {
        return flag;
    }

    @Override
    public String getManifestChangeForVasco() {
        return manifestChangeForVasco;
    }
}
