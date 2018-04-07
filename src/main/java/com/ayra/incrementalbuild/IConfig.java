package com.ayra.incrementalbuild;

interface IConfig {

   String getPreviousServerPath();
   String getPreviousClientPath();
   String getPresentServerPath();
   String getPresentClientPath();
   String getReleaseVersion();
   String getIncrementalSeverPath();
   String getIncrementalClientPath();
   String getFlag();
   String getManifestChangeForVasco();
}
