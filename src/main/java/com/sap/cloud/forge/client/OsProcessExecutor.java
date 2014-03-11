package com.sap.cloud.forge.client;

import static org.jboss.forge.furnace.util.OperatingSystemUtils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

class OsProcessExecutor {

    private static final Logger logger = Logger.getLogger(OsProcessExecutor.class.getName());

    static void execute(File workingDirectory, String command, String[] parameters, boolean waitFor)
            throws Exception {
        String[] commandTokens = new String[getParametersArrayLength(parameters)];

        if (isWindows()) {
            commandTokens[0] = "cmd";
            commandTokens[1] = "/c";
            commandTokens[2] = command;
        } else {
            commandTokens[0] = command;
        }

        if (parameters != null && parameters.length > 0) {
            System.arraycopy(parameters, 0, commandTokens, getStartIndex(), parameters.length);
        }

        logger.info("Executing process: " + maskPassword(commandTokens));
        ProcessBuilder builder = new ProcessBuilder(commandTokens);
        builder.directory(workingDirectory);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        new ProcessStreamReaderThread(p.getInputStream()).start();
        if (waitFor) {
            p.waitFor();
        }
    }

    private static int getStartIndex() {
        return isWindows() ? 3 : 1;
    }

    private static int getParametersArrayLength(String[] parameters) {
        if (parameters == null) {
            return 1;
        }

        if (isWindows()) {
            return parameters.length + 3;
        } else {
            return parameters.length + 1;
        }
    }

    private static final class ProcessStreamReaderThread extends Thread {

        private InputStream processInputStream;

        public ProcessStreamReaderThread(InputStream processInputStream) {
            this.processInputStream = processInputStream;
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(processInputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line); // TODO
                }
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        }
    }

    private static String maskPassword(String[] commandTokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commandTokens.length; i++) {
            sb.append(commandTokens[i] + " ");
            if (commandTokens[i].equals("--password")) {
                sb.append("<password> ");
                i++;
            }
        }
        return sb.toString();
    }
}
