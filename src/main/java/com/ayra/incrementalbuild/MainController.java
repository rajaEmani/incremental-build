package com.ayra.incrementalbuild;

import com.ayra.incrementalbuild.config.IConfig;
import com.ayra.incrementalbuild.config.Module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    private final ConfigLoader configLoader;
    private List<File> previousFiles;
    private File previousBuildDirectory;
    private File incrementalFilesOutputDirectory;
    private String releaseVersion;
    private List<File> differentialJars;

    private MainController(ConfigLoader configLoader) {
        this.configLoader = configLoader;
        processIncrementalBuild();
    }

    private void processIncrementalBuild() {
        IConfig config = configLoader.getConfig();
        for (Module module : config.getModules()) {
            processIncrementalBuildForModule(module);
        }
    }

    private void processIncrementalBuildForModule(Module module) {
        previousBuildDirectory = new File(module.getReleasePathPrevious());
        File presentBuildDirectory = new File(module.getReleasePathPresent());
        incrementalFilesOutputDirectory = new File(module.getIncrementalJarsOutput());
        releaseVersion = module.getReleaseVersion();
        differentialJars = new ArrayList<>();
        previousFiles = new ArrayList<>();
        listPreviousFiles(previousBuildDirectory);
        printDeltaJars(presentBuildDirectory);
        differentialJars.forEach(System.out::println);
    }

    private void listPreviousFiles(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    listPreviousFiles(f);
                } else {
                    previousFiles.add(f);
                }
            }
        }
    }

    private void printDeltaJars(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory())
                    printDeltaJars(f);
                else {
                    if (f.getName().contains(".jar"))
                        compareJars(f);
                }
            }
        }
    }

    private void compareJars(File jarFile) {
        JarFileCompare jarFileCompare;
        File previousVersionFile = getPreviousVersionFile(jarFile.getName());
        if (previousVersionFile == null) {
            try {
                copyFileUsingChannel(jarFile, getNewCopyPath(jarFile, true));
                differentialJars.add(jarFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                jarFileCompare = new JarFileCompare(jarFile, previousVersionFile, previousBuildDirectory.getParentFile());
                if (!jarFileCompare.process()) {
                    try {
                        copyFileUsingChannel(jarFile, getCopyPath(previousVersionFile, true, incrementalFilesOutputDirectory.getPath()));
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
    private void compareNonJars(File file, File previousJars, File incrementalJarsOutput) {
        File previousVersionFile = getPreviousVersionFile(file.getName());
        if (previousVersionFile == null) {
            try {
                copyFileUsingChannel(file, getNewCopyPath(file, false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ConfigFileCompare configFileCompare = new ConfigFileCompare();
                if (!configFileCompare.compareConfigs(file, previousVersionFile)) {
                    try {
                        copyFileUsingChannel(file, getCopyPath(previousVersionFile, false, incrementalJarsOutput.getPath()));
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

    private void copyFileUsingChannel(File source, String path) throws IOException {
        if (path != null) {
            FileChannel destChannel = new FileOutputStream(path).getChannel();
            try (FileChannel sourceChannel = new FileInputStream(source).getChannel()) {
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            }
            destChannel.close();
        }
    }


    private String getCopyPath(File previousVersionFile, boolean isJar, String incrementalJarOutputPath) {
        String copyPath = incrementalJarOutputPath;
        if (isJar) {
            String[] name = previousVersionFile.getName().split("-");
            String jarName = name[0] + "-" + releaseVersion + ".jar";
            copyPath = copyPath + "\\" + jarName;
        } else {
            copyPath = copyPath + "\\" + previousVersionFile.getName();
        }
        return copyPath;
    }

    private String getNewCopyPath(File file, boolean isJar) {
        String copyPath = incrementalFilesOutputDirectory.getPath();
        String fileName = file.getName();
        if (isJar) {
            String[] splittedJarName = fileName.split("-");
            fileName = splittedJarName[0] + "-" + releaseVersion + ".jar";
        }
        copyPath = copyPath + "\\" + fileName;
        return copyPath;
    }

    public static void main(String[] args) {
        MainController mainController = new MainController(new ConfigLoader());
        mainController.processIncrementalBuild();
    }
}

