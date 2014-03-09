package com.sap.cloud.forge.client;

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
        String[] commandTokens = parameters == null ? new String[1]
                : new String[parameters.length + 1];
        commandTokens[0] = command;

        if (commandTokens.length > 1) {
            System.arraycopy(parameters, 0, commandTokens, 1, parameters.length);
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
