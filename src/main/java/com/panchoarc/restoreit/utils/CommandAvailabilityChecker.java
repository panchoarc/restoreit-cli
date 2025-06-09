package com.panchoarc.restoreit.utils;

public class CommandAvailabilityChecker {


    public static boolean isCommandAvailable(String command) {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb = os.contains("win")
                ? new ProcessBuilder("where", command)
                : new ProcessBuilder("which", command);
        try {
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDockerAvailable() {
        return isCommandAvailable("docker");
    }
}
