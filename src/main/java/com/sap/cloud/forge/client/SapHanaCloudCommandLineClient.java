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
    static final String INSTALL_LOCALL_CMD = "install-local";
    static final String START_LOCALL_CMD = "start-local";
    static final String STOP_LOCALL_CMD = "stop-local";

    private final File workDir;
    
    public SapHanaCloudCommandLineClient(String sdkLocation) {
        this.workDir = new File(sdkLocation, "tools");
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
        runHanaCloudClient(getHanaCloudClientParams(application, archiveLocation, DEPLOY_CMD, 
                account, userName, password));
    }
    
    @Override
    public void installLocal() throws SapHanaCloudClientException {
        runHanaCloudClient(getHanaCloudClientParams(INSTALL_LOCALL_CMD));
    }
    

    @Override
    public void startLocal() throws SapHanaCloudClientException {
        runHanaCloudClient(getHanaCloudClientParams(START_LOCALL_CMD), !OperatingSystemUtils.isWindows());
    }

    @Override
    public void stopLocal() throws SapHanaCloudClientException {
        runHanaCloudClient(getHanaCloudClientParams(STOP_LOCALL_CMD));
    }

    private String runHanaCloudClient(String[] parameters) throws SapHanaCloudClientException {
        return runHanaCloudClient(parameters, true);
    }

    private String runHanaCloudClient(String[] parameters, boolean waitFor) throws SapHanaCloudClientException {
        String command = "./neo.sh";
        if (OperatingSystemUtils.isWindows()) {
            command = "neo.bat";
        } 
        try {
            OsProcessExecutor.execute(workDir, command, parameters, waitFor);
        } catch (Exception e) {
            throw new SapHanaCloudClientException("Could not execute command. Reason: " + e.getMessage(), e);
        }
        return command;
    }

    private String[] getHanaCloudClientParams(String command) {
        return new String[] {command};
    }
    
    private String[] getHanaCloudClientParams(String applicationName, String archiveLocation,
            String command, String account, String userName, String password) {
        List<String> parameters = new ArrayList<>();
        parameters.addAll(Arrays.asList(command, "--host", DEFAULT_HOST, "--account", account, "--user",
                userName, "--application", applicationName, "--password", password));
        if (command.equals(DEPLOY_CMD)) {
            if (OperatingSystemUtils.isWindows()) {
                archiveLocation = archiveLocation.replace('/', '\\');
            }
            parameters.add("--source");
            parameters.add(archiveLocation);
        } else if (command.equals(START_CMD) || command.equals(STOP_CMD)) {
            parameters.add("--synchronous");
        }

        return parameters.toArray(new String[parameters.size()]);
    }
}
