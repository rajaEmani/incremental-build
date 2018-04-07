package com.ayra.incrementalbuild;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

class ConfigFileCompare {

    boolean compareConfigs(File presentFile, File previousFile) {
        String s1 = calculateCheckSum(presentFile);
        String s2 = calculateCheckSum(previousFile);
        return s1.equals(s2);
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

            for (byte mdbyte : mdbytes) sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

}
