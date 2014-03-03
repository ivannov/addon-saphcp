package com.sap.cloud.forge;

import static com.sap.cloud.forge.ConfigurationConstants.HANA_CLOUD_ACCOUNT;
import static com.sap.cloud.forge.ConfigurationConstants.HANA_CLOUD_SDK;
import static com.sap.cloud.forge.ConfigurationConstants.HANA_CLOUD_USER_NAME;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.addon.configuration.Configuration;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.furnace.util.OperatingSystemUtils;

@FacetConstraint(SapHanaCloudFacet.class)
public class DeployCommand extends AbstractSapHanaCloudCommand {

    static final String STOP_CMD = "stop";
    static final String DEPLOY_CMD = "deploy";
    static final String START_CMD = "start";
    static final String CLIENT_LIBS_SUBDIR = "tools/lib/cmd";
    static final String CLIENT_MAIN_CLASS = "com.sap.jpaas.infrastructure.console.ConsoleClient";
    static final String DEFAULT_HOST = "https://nwtrial.ondemand.com";

    @Inject
    @WithAttributes(label = "Password", required = true, type = InputType.SECRET)
    private UIInput<String> password;

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.from(super.getMetadata(context), this.getClass()).name("SAPHCP: Deploy");
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        builder.add(password);
    }

    @Override
    public Result execute(UIExecutionContext context) {
        Configuration projectConfig = getProjectConfig(context);
        String sdkLocation = projectConfig.getString(HANA_CLOUD_SDK);
        String account = projectConfig.getString(HANA_CLOUD_ACCOUNT);
        String userName = projectConfig.getString(HANA_CLOUD_USER_NAME);
        String password = this.password.getValue();

        try {
            String[] parameters = getHanaCloudClientParams(getSelectedProject(context), STOP_CMD,
                    sdkLocation, account, userName, password);
            runHanaCloudClient(getSelectedProject(context), parameters);

            parameters = getHanaCloudClientParams(getSelectedProject(context), DEPLOY_CMD,
                    sdkLocation, account, userName, password);
            runHanaCloudClient(getSelectedProject(context), parameters);

            parameters = getHanaCloudClientParams(getSelectedProject(context), START_CMD,
                    sdkLocation, account, userName, password);
            runHanaCloudClient(getSelectedProject(context), parameters);
        } catch (Exception e) {
            return Results.fail(e.getMessage());
        }

        return Results.success("Application was deployed successfully to the cloud");
    }

    private String runHanaCloudClient(Project project, String[] parameters) throws Exception {
        String command = "java";
        if (OperatingSystemUtils.isWindows()) {
            command += ".exe";
        }
        OsProcessExecutor.execute(project.getRootDirectory().getUnderlyingResourceObject(),
                command, parameters);
        return command;
    }

    private String[] getHanaCloudClientParams(Project project, String command, String sdkLocation,
            String account, String userName, String password) {
        String applicationName = project.getFacet(MetadataFacet.class).getProjectName();
        String archiveLocation = project.getFacet(PackagingFacet.class).getFinalArtifact()
                .getFullyQualifiedName().replace("\\/", "/");
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