package com.ayra.incrementalbuild;

import java.io.File;
import java.io.IOException;
import java.util.List;

class CommandRunner {

    void runCommand(List<String> dosCommand, File workingDirectory) {
        try {
            ProcessBuilder pb = new ProcessBuilder(dosCommand);
            pb.directory(workingDirectory);
            pb.redirectErrorStream(true);
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
