package com.ayra.incrementalbuild;

import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;


class JarFileCompare {

    JarFileCompare(File jar1, File jar2, File space) {
        this.jar1 = jar1;
        this.jar2 = jar2;
        this.space = space;
    }

    private File jar1;
    private File jar2;
    private File space;
    private List<File> jar2Files = new ArrayList<>();
    private List<String> differentialFiles = new ArrayList<>();
    private String jarName;

    private File decompileJar(File jarFile, String num) {
        List<String> dosCommand = new ArrayList<>();
        dosCommand.add("jar");
        dosCommand.add("xf");
        dosCommand.add(jarFile.getPath());
        
        if (jarFile.getName().equals("metSwingClient.jar") || jarFile.getName().equals("metApplication.jar")) {
            jarName = jarFile.getName().split("\\.")[0];
        } else {
            jarName = jarFile.getName().split("-")[0];
        }
        String jarDirectory = space.getPath() + "\\" + jarName + num;
        File file = new File(jarDirectory);
        boolean mkdir = file.mkdir();

        CommandRunner commandRunner = new CommandRunner();
        commandRunner.runCommand(dosCommand, file);

        return file;
    }

    @SuppressWarnings("all")
    private boolean deleteDir(File dir) throws IOException {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir (new File(dir, children[i]));

                if (!success) {
                    return false;
                }
                else{
                    FileUtils.deleteDirectory(dir);
                    return true;
                }
            }
        }
        return true;
    }

    boolean process() {
        File jarDecompileFolder1 = decompileJar(jar1, "1");
        File jarDecompileFolder2 = decompileJar(jar2, "2");
        System.out.println("Comparing : " + jarName);
        try {
            TimeUnit.SECONDS.sleep(1);
            listPreviousFiles(jarDecompileFolder2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        addDifferentialFilesToList(jarDecompileFolder1);
        try {
            deleteDir(jarDecompileFolder1);
            deleteDir(jarDecompileFolder2);
        } catch (Exception e) {

        }

        differentialFiles.remove("build.properties");
        differentialFiles.remove("pom.properties");
        differentialFiles.remove("versionReport.class");
        differentialFiles.remove("MANIFEST.MF");
        differentialFiles.remove("VersionReport.class");
        differentialFiles.remove("ObjectFactory.class");

        return !(differentialFiles.size() > 0);
    }

    private String calculateCheckSum(File currentJavaJarFile) {
        String filepath = currentJavaJarFile.getAbsolutePath();
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(filepath);
            byte[] dataBytes = new byte[1024];
            int nread = 0;

            while ((nread = fis.read(dataBytes)) != -1)
                md.update(dataBytes, 0, nread);

            byte[] mdbytes = md.digest();

            for (int i = 0; i < mdbytes.length; i++)
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private long calculateCRC(File filename) {
        final int SIZE = 16 * 1024;
        try (FileInputStream in = new FileInputStream(filename);) {
            FileChannel channel = in .getChannel();
            CRC32 crc = new CRC32();
            int length = (int) channel.size();
            MappedByteBuffer mb = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);
            byte[] bytes = new byte[SIZE];
            int nGet;
            while (mb.hasRemaining()) {
                nGet = Math.min(mb.remaining(), SIZE);
                mb.get(bytes, 0, nGet);
                crc.update(bytes, 0, nGet);
            }
            return crc.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("unknown IO error occurred ");
    }

    private void addDifferentialFilesToList(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory())
                    addDifferentialFilesToList(f);
                else {
                    File previousVersionFile = getPreviousVersionFile(f);
                    if (previousVersionFile == null) {
                        differentialFiles.add(f.getName());
                    } else {
                        try {
                            String s1 = calculateCheckSum(f).trim();
                            String s2 = calculateCheckSum(previousVersionFile).trim();
                            long m1=calculateCRC(f);
                            long m2=calculateCRC(previousVersionFile);
                            if (!s1.equals(s2) && m1!=m2) {
                                try {
                                    differentialFiles.add(f.getName());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private File getPreviousVersionFile(File file) {
        for (File f : jar2Files) {
            String path1 = file.getPath().replace(space.getPath() + "\\" + jarName + "1", "");
            String path2 = f.getPath().replace(space.getPath() + "\\" + jarName + "2", "");
            if (f.getName().equals(file.getName()) && path1.equals(path2)) {
                return f;
            }
        }
        return null;
    }

    private void listPreviousFiles(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory())
                    listPreviousFiles(f);
                else
                    jar2Files.add(f);
            }
        }
    }
}
