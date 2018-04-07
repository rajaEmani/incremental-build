package com.ayra.incrementalbuild;

public class ConfigValidation {

    public boolean validate(IConfig config) {
        try {
            if (config.getPreviousServerPath() == null || config.getPreviousServerPath().isEmpty() || config.getPreviousServerPath().trim().equals("")) {
                System.out.println("Invalid or empty Previous server path.");
                return false;
            } else if (config.getPreviousClientPath() == null || config.getPreviousClientPath().isEmpty() || config.getPreviousClientPath().trim().equals("")) {
                System.out.println("Invalid or empty Previous client path.");
                return false;
            } else if (config.getPresentServerPath() == null || config.getPresentServerPath().isEmpty() || config.getPresentServerPath().trim().equals("")) {
                System.out.println("Invalid or empty Present server path.");
                return false;
            } else if (config.getPresentClientPath() == null || config.getPresentClientPath().isEmpty() || config.getPreviousServerPath().trim().equals("")) {
                System.out.println("Invalid or empty Present client path.");
                return false;
            }
            else if (config.getReleaseVersion() == null || config.getReleaseVersion().isEmpty() || config.getReleaseVersion().trim().equals("")) {
                System.out.println("provide release version.");
                return false;
            }
            else  {
                try
                {
                    boolean flag = Boolean.parseBoolean(config.getFlag());
                    if (flag)
                    {
                        if (config.getIncrementalClientPath() == null || config.getIncrementalClientPath().isEmpty() || config.getIncrementalClientPath().trim().equals("")) {
                            System.out.println("Invalid or empty incremental jars client path.");
                            return false;
                        }
                        else if (config.getIncrementalSeverPath() == null || config.getIncrementalSeverPath().isEmpty() || config.getIncrementalSeverPath().trim().equals("")) {
                            System.out.println("Invalid or empty incremental jars server path.");
                            return false;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("The Entry for flag should be either true or false");
                    return false;
                }
                try
                {
                    Boolean.parseBoolean(config.getManifestChangeForVasco());
                    return true;
                } catch (Exception e) {
                    System.out.println("The Entry for manifestChangeForVasco should be either true or false");
                    return false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
