package com.sap.cloud.forge.client;

import java.io.File;

class OsProcessExecutor {

    static void execute(File workingDirectory, String command, String[] parameters, boolean waitFor)
            throws Exception {
        String[] commandTokens = parameters == null ? new String[1]
                : new String[parameters.length + 1];
        commandTokens[0] = command;

        if (commandTokens.length > 1) {
            System.arraycopy(parameters, 0, commandTokens, 1, parameters.length);
        }

        ProcessBuilder builder = new ProcessBuilder(commandTokens);
        builder.directory(workingDirectory);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        if (waitFor) {
            p.waitFor();
        }
    }
}
