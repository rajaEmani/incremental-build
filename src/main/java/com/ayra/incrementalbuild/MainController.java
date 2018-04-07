package com.ayra.incrementalbuild;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainController {
    private File presentJars ;
    private File previousServerJars;
    private File previousClientJars;
    private String releaseVersion ;
    private String incrementalSeverPath;
    private String incrementalClientPath;
    private List<File> previousFiles = new ArrayList<>();
    private boolean processServer;
    private boolean givenPath;

    private final Object lock = new Object();

    private void processIncrementalBuild()
    {
        ConfigLoader configLoader = new ConfigLoader();
        IConfig config = configLoader.getConfig();
        ConfigValidation validation=new ConfigValidation();
        if(validation.validate(config)){
            releaseVersion = config.getReleaseVersion();
            givenPath = Boolean.parseBoolean(config.getFlag());
            processIncrementalBuildForServer(config);
            processIncrementalBuildForClient(config);
        }
    }

    private void processIncrementalBuildForServer(IConfig config)
    {
        synchronized (lock)
        {
            final List<File> differentialServerJars = new ArrayList<>();
            processServer = true;
            previousServerJars = new File(config.getPreviousServerPath());
            presentJars = new File(config.getPresentServerPath());
            incrementalSeverPath=config.getIncrementalSeverPath();
            previousFiles = new ArrayList<>();
            listPreviousFiles(previousServerJars);
            printDeltaJars(presentJars,differentialServerJars,previousServerJars);
            if(givenPath)
            {
                if(differentialServerJars.contains("metApplication.jar"))
                    updateManifestFile(incrementalSeverPath+"\\metApplication.jar" , differentialServerJars,previousServerJars.getPath()+"\\metApplication.jar");
                else
                {
                    try {
                        copyFileUsingChannel(new File(presentJars.getPath()+"\\metApplication.jar"),incrementalSeverPath+"\\metApplication.jar");
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                updateManifestFile(incrementalSeverPath+"\\metApplication.jar",differentialServerJars,previousServerJars.getPath()+"\\metApplication.jar");
                            }
                        };
                        Thread t = new Thread(r);
                        t.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                if(differentialServerJars.contains("metApplication.jar"))
                {
                    //need to work
                }
                else
                {
                    updateManifestFile(previousServerJars.getPath()+"\\metApplication.jar",differentialServerJars,previousServerJars.getPath()+"\\metApplication.jar");
                }
            }
        }
    }

    private void processIncrementalBuildForClient(IConfig config)
    {
        synchronized (lock)
        {
            final List<File> differentialClientJars = new ArrayList<>();
            processServer = false;
            previousClientJars = new File(config.getPreviousClientPath());
            presentJars = new File(config.getPresentClientPath());
            incrementalClientPath=config.getIncrementalClientPath();
            previousFiles = new ArrayList<>();
            listPreviousFiles(previousClientJars);
            printDeltaJars(presentJars,differentialClientJars,previousClientJars);
            if(givenPath)
            {
                if(differentialClientJars.contains("metSwingClient.jar"))
                {
                    updateManifestFile(incrementalClientPath+"\\metSwingClient.jar",differentialClientJars,previousClientJars.getPath()+"\\metSwingClient.jar");
                }
                else
                {
                    try {
                        copyFileUsingChannel(new File(presentJars.getPath()+"\\metSwingClient.jar"),incrementalClientPath+"\\metSwingClient.jar");
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                updateManifestFile(incrementalClientPath+"\\metSwingClient.jar",differentialClientJars,previousClientJars.getPath()+"\\metSwingClient.jar");
                            }
                        };
                        Thread t = new Thread(r);
                        t.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                if(differentialClientJars.contains("metSwingClient.jar"))
                {
                    //need to work
                }
                else
                {
                    updateManifestFile(previousClientJars.getPath()+"\\metSwingClient.jar",differentialClientJars,previousClientJars.getPath()+"\\metSwingClient.jar");
                }
            }
        }
    }

    private void listPreviousFiles(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory())
                    listPreviousFiles(f);
                else
                    previousFiles.add(f);
            }
        }
    }

    private void printDeltaJars(File file , List differentialJars ,File previousJars) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory())
                    printDeltaJars(f,differentialJars,previousJars);
                else {
                    if(f.getName().contains(".jar"))
                        compareJars(f,differentialJars,previousJars);
                    else
                    {
                        //compareNonJars(f);
                    }
                }
            }
        }
    }

    private void compareJars(File jarFile , List differentialJars ,File previousJars )
    {
        JarFileCompare jarFileCompare;
        File previousVersionFile = getPreviousVersionFile(jarFile.getName());
        if (previousVersionFile == null) {
            try {
                copyFileUsingChannel(jarFile,getNewCopyPath(jarFile,true,previousJars));
                differentialJars.add(jarFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(jarFile.getName().contains("met") ){
            try {
                jarFileCompare = new JarFileCompare(jarFile, previousVersionFile,previousJars.getParentFile());
                if (!jarFileCompare.process()) {
                    try {
                        copyFileUsingChannel(jarFile,getCopyPath(previousVersionFile,true));
                        differentialJars.add(jarFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused")
    private void compareNonJars(File file,File previousJars)
    {
        File previousVersionFile = getPreviousVersionFile(file.getName());
        if (previousVersionFile == null) {
            try {
                copyFileUsingChannel(file,getNewCopyPath(file,false,previousJars));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ConfigFileCompare configFileCompare = new ConfigFileCompare();
                if (!configFileCompare.compareConfigs(file,previousVersionFile)) {
                    try {
                        copyFileUsingChannel(file,getCopyPath(previousVersionFile,false));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private File getPreviousVersionFile(String name) {
        for (File f : previousFiles) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }

    private void copyFileUsingChannel(File source,String path) throws IOException {
        if(path!=null)
        {
            FileChannel destChannel =  new FileOutputStream(path).getChannel();
            try (FileChannel sourceChannel = new FileInputStream(source).getChannel()) {
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            }
            destChannel.close();
        }
    }

    private void updateManifestFile(String jarName,List<File> differentialJars,String previousJarForManifest)
    {
        ManifestFileEditor manifestFileEditor = new ManifestFileEditor();
        List<String> incrementalJars = new ArrayList<>();
        for(File f : differentialJars)
        {
            String differentialJarName = f.getName();
            differentialJarName = differentialJarName.split("-")[0];
            System.out.println(differentialJarName);
            incrementalJars.add(differentialJarName+"-");
        }
        manifestFileEditor.editManifestFile(new File(jarName),incrementalJars,new File(previousJarForManifest));
    }

    private String getCopyPath(File previousVersionFile, boolean isJar)
    {
        String copyPath = givenPath ? (processServer ? incrementalSeverPath:incrementalClientPath ):previousVersionFile.getParent();
        if(isJar)
        {
            String[] name = previousVersionFile.getName().split("-");
            String jarName;
            if(name[0].contains("metSwingClient.jar") || name[0].contains("metApplication.jar"))
            {
                jarName = previousVersionFile.getName();
            }
            else
            {
                jarName = name[0] +"-" + releaseVersion + ".jar";
            }
            copyPath = copyPath + "\\"+jarName;
        }
        else
        {
            copyPath = copyPath + "\\"+previousVersionFile.getName();
        }
        return copyPath;
    }

    private String getNewCopyPath(File newJarFile,boolean isJar,File previousJars)
    {
        String copyPath;
        if(givenPath)
        {
            copyPath = processServer ? incrementalSeverPath:incrementalClientPath;
            String jarName = newJarFile.getName();
            String [] splittedJarName = jarName.split("-");
            jarName = splittedJarName[0]+"-" + releaseVersion + ".jar";
            copyPath = copyPath+"\\"+jarName;
        }
        else
        {
            String path = newJarFile.getPath();
            String pattern = Pattern.quote(System.getProperty("file.separator"));
            String[] splittedFileName = path.split(pattern);
            int index =0;
            for(int i=0;i<splittedFileName.length;i++)
            {
                if(processServer)
                {
                    if(splittedFileName[i].contains("metApplication"))
                        index = i;
                }
                else {
                    if(splittedFileName[i].contains("metSwingClient"))
                        index = i;
                }
            }
            copyPath = previousJars.getPath();
            for(int i = index+1;i<splittedFileName.length-1;i++)
            {
                copyPath = copyPath+"\\"+splittedFileName[i];
            }
            if(isJar)
            {
                String jarName = splittedFileName[splittedFileName.length-1];
                String [] splittedJarName = jarName.split("-");
                jarName = splittedJarName[0]+"-" + releaseVersion + ".jar";
                copyPath = copyPath+"\\"+jarName;
            }
            else
            {
                copyPath = copyPath+splittedFileName[splittedFileName.length-1];
            }
        }
        return copyPath;
    }

    public static void main(String[] args) {
        MainController mainController = new MainController();
        mainController.processIncrementalBuild();
    }
}
