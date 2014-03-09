package com.sap.cloud.forge.ui;

import java.io.File;

import javax.inject.Inject;

import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.ResourceException;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;

import com.sap.cloud.forge.SapHanaCloudFacet;
import com.sap.cloud.forge.client.SapHanaCloudClient;
import com.sap.cloud.forge.client.SapHanaCloudCommandLineClient;
import com.sap.cloud.forge.ui.config.ProjectConfiguration;

public class SetupCommand extends AbstractSapHanaCloudCommand {

    @Inject
    private FacetFactory facetFactory;

    @Inject
    @WithAttributes(label = "SDK directory", required = true)
    private UIInput<DirectoryResource> sdkLocation;

    @Inject
    @WithAttributes(label = "User name", required = true)
    private UIInput<String> userName;

    @Inject
    @WithAttributes(label = "Account", required = true)
    private UIInput<String> account;

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.from(super.getMetadata(context), this.getClass()).name("SAPHCP: Setup");
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
        builder.add(sdkLocation).add(userName).add(account);
    }

    @Override
    public Result execute(UIExecutionContext context) {
        Project selectedProject = getSelectedProject(context);
        ProjectConfiguration projectConfig = getProjectConfig(context);
        String sdkPath = sdkLocation.getValue().getFullyQualifiedName()
                .replace("\\/", "/");
        projectConfig.setSdkLocation(sdkPath);
        projectConfig.setAccount(account.getValue());
        projectConfig.setUserName(userName.getValue());
        
        context.getProgressMonitor().beginTask("Setup SAP HCP", 2);
        
        context.getProgressMonitor().subTask("Installing local runtime");
        installLocalRuntime(sdkPath);
        context.getProgressMonitor().worked(1);
        
        context.getProgressMonitor().subTask("Adding dependencies");
        facetFactory.install(selectedProject, SapHanaCloudFacet.class);
        context.getProgressMonitor().worked(1);
        
        context.getProgressMonitor().done();
        return Results.success("SAP HANA Cloud configured successfully for this project!");
    }

    private void installLocalRuntime(String sdkPath) {
        if (!new File(sdkPath, "server").exists()) {
            SapHanaCloudClient client = new SapHanaCloudCommandLineClient(sdkPath);
            client.installLocal();
        }        
    }

    @Override
    public boolean isEnabled(UIContext context) {
        return super.isEnabled(context)
                && "war".equals(getSelectedProject(context).getFacet(PackagingFacet.class)
                        .getPackagingType());
    }

    private static final String SDK_VALIDATION_ERROR_MSG = "The specified directory is not a valid SAP HCP SDK";

    @Override
    public void validate(UIValidationContext validator) {
        super.validate(validator);

        DirectoryResource sdk = sdkLocation.getValue();
        if (sdk != null) {
            try {
                DirectoryResource toolsDir = sdk.getChildDirectory("tools");
                if (!toolsDir.exists()) {
                    validator.addValidationError(sdkLocation, SDK_VALIDATION_ERROR_MSG);
                }

                if (!toolsDir.getChild("neo.sh").exists()) {
                    validator.addValidationError(sdkLocation, SDK_VALIDATION_ERROR_MSG);
                }
            } catch (ResourceException re) {
                validator.addValidationError(sdkLocation, SDK_VALIDATION_ERROR_MSG);
            }
        }
    }
}