package com.sap.cloud.forge.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.forge.furnace.util.OperatingSystemUtils;

public class SapHanaCloudCommandLineClient implements SapHanaCloudClient {

    static final String STOP_CMD = "stop";
    static final String DEPLOY_CMD = "deploy";
    static final String START_CMD = "start";
    static final String CLIENT_LIBS_SUBDIR = "tools/lib/cmd";
    static final String CLIENT_MAIN_CLASS = "com.sap.jpaas.infrastructure.console.ConsoleClient";
    static final String DEFAULT_HOST = "https://nwtrial.ondemand.com";

    private File workDir;
    private String sdkLocation;
    
    public SapHanaCloudCommandLineClient(File workDir, String sdkLocation) {
        this.workDir = workDir;
        this.sdkLocation = sdkLocation;
    }

    @Override
    public void stopRemote(String application, String account, String userName, String password) {
        runHanaCloudClient(getHanaCloudClientParams(application, null, STOP_CMD, 
                account, userName, password));

    }

    @Override
    public void startRemote(String application, String account, String userName, String password) throws SapHanaCloudClientException{
        runHanaCloudClient(getHanaCloudClientParams(application, null, START_CMD, 
                account, userName, password));
    }

    @Override
    public void deployRemote(String application, String archiveLocation, String account,
            String userName, String password) throws SapHanaCloudClientException {
        runHanaCloudClient(getHanaCloudClientParams(application, null, DEPLOY_CMD, 
                account, userName, password));
    }

    private String runHanaCloudClient(String[] parameters) throws SapHanaCloudClientException {
        String command = "java";
        if (OperatingSystemUtils.isWindows()) {
            command += ".exe";
        }
        try {
            OsProcessExecutor.execute(workDir, command, parameters);
        } catch (Exception e) {
            throw new SapHanaCloudClientException("Could not execute command. Reason: " + e.getMessage(), e);
        }
        return command;
    }

    private String[] getHanaCloudClientParams(String applicationName, String archiveLocation,
            String command, String account, String userName, String password) {
        if (OperatingSystemUtils.isWindows()) {
            archiveLocation = archiveLocation.replace('/', '\\');
        }

        List<String> commonParameters = Arrays.asList("-cp", getClassPath(sdkLocation),
                CLIENT_MAIN_CLASS, command, "--host", DEFAULT_HOST, "--account", account, "--user",
                userName, "--application", applicationName, "--password", password);

        List<String> parameters = new ArrayList<String>(commonParameters);
        if (command.equals(DEPLOY_CMD)) {
            parameters.add("--source");
            parameters.add(archiveLocation);
        } else if (command.equals(START_CMD) || command.equals(STOP_CMD)) {
            parameters.add("--synchronous");
        }

        return parameters.toArray(new String[parameters.size()]);
    }

    private String getClassPath(String sdkLocation) {
        StringBuilder classpath = new StringBuilder();

        File libsDir = new File(sdkLocation, CLIENT_LIBS_SUBDIR);
        File[] content = libsDir.listFiles();

        for (File libFile : content) {
            if (libFile.isFile() && libFile.getName().endsWith(".jar")) {
                classpath.append(libFile.getAbsolutePath()).append(File.pathSeparator);
            }
        }

        return classpath.toString();
    }
}
