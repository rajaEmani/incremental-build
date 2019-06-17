package com.ayra.incrementalbuild;

import javax.inject.Singleton;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

@SuppressWarnings("unused")
@Singleton
class ManifestFileEditor {

    private List<String> editedClasspath = new ArrayList<>();
    private List<String> editedClasspathForTraversing = new ArrayList<>();

    void editManifestFile(File jarFile, List<String> changedJars, File jarFileForManifest, String releaseVersion) {
        if (changedJars.size() > 0) {
            try {
                FileInputStream fileInputStream = new FileInputStream(jarFileForManifest);
                JarInputStream jarStream = new JarInputStream(fileInputStream);
                Manifest mf = jarStream.getManifest();
                String[] classPaths = mf.getMainAttributes().getValue("Class-Path").split(" ");
                for (String s : classPaths) {
                    editedClasspath.add(s);
                    editedClasspathForTraversing.add(s);
                }
                for (String s : changedJars) {
                    for (String p : editedClasspathForTraversing) {
                        String oldClasspath = p;
                        if (p.contains(s)) {
                            String[] jarAttributes = p.split("-");
                            jarAttributes[jarAttributes.length - 1] = releaseVersion + "." + "jar";
                            StringBuilder jarName = new StringBuilder();
                            for (String f : jarAttributes) {
                                if (jarName.toString().equals("")) {
                                    jarName = new StringBuilder(f);
                                } else {
                                    jarName.append("-").append(f);
                                }
                            }
                            p = jarName.toString();
                            editedClasspath.remove(oldClasspath);
                            editedClasspath.add(p);
                        }
                    }
                }
                StringBuilder classPathValue = new StringBuilder();
                for (String s : editedClasspath) {
                    classPathValue.append(" ").append(s);
                }
                mf.getMainAttributes().putValue("Class-Path", classPathValue.toString());
                File newManifestFile = writeToManifestFile(jarFile.getParentFile(), mf);
                updateJarWithNewManifest(jarFile, newManifestFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private File writeToManifestFile(File folder, Manifest manifest) {
        File manifestFile = new File(folder.getPath() + "/MANIFEST.MF");
        try (FileWriter fileWriter = new FileWriter(manifestFile);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter out = new PrintWriter(bufferedWriter)) {
            Attributes map = manifest.getMainAttributes();
            out.println("Manifest-Version: " + map.getValue("Manifest-Version"));
            out.println("Archiver-Version: " + map.getValue("Archiver-Version"));
            out.println("Created-By: " + map.getValue("Created-By"));
            out.println("Built-By: " + map.getValue("Built-By"));
            out.println("Build-Jdk: " + map.getValue("Build-Jdk"));
            out.println("Main-Class: " + map.getValue("Main-Class"));
            out.print("Class-Path: ");
            String classPathValue = map.getValue("Class-Path");
            for (int i = 11; i < classPathValue.length() + 10; i++) {
                out.print(classPathValue.charAt(i - 10));
                if (i % 69 == 0) {
                    out.print("\n");
                    out.print(" ");
                }
            }
            out.print("\n");
            out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return manifestFile;
    }

    private void updateJarWithNewManifest(File jarToUpdate, File manifest) {
        List<String> dosCommand = new ArrayList<>();
        dosCommand.add("jar");
        dosCommand.add("umf");
        dosCommand.add(manifest.getPath());
        dosCommand.add(jarToUpdate.getName());

        CommandRunner commandRunner = new CommandRunner();
        commandRunner.runCommand(dosCommand, jarToUpdate.getParentFile());
    }
}
