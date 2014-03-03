package com.sap.cloud.forge.ui;

import static com.sap.cloud.forge.ui.ConfigurationConstants.HANA_CLOUD_ACCOUNT;
import static com.sap.cloud.forge.ui.ConfigurationConstants.HANA_CLOUD_SDK;
import static com.sap.cloud.forge.ui.ConfigurationConstants.HANA_CLOUD_USER_NAME;

import java.io.File;

import javax.inject.Inject;

import org.jboss.forge.addon.configuration.Configuration;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
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

import com.sap.cloud.forge.SapHanaCloudFacet;
import com.sap.cloud.forge.client.SapHanaCloudClient;
import com.sap.cloud.forge.client.SapHanaCloudClientException;
import com.sap.cloud.forge.client.SapHanaCloudCommandLineClient;

@FacetConstraint(SapHanaCloudFacet.class)
public class DeployCommand extends AbstractSapHanaCloudCommand {

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
        
        SapHanaCloudClient hcpClient = new SapHanaCloudCommandLineClient(
                getSelectedProject(context).getRootDirectory().getUnderlyingResourceObject(),
                sdkLocation);

        try {
            String application = getSelectedProject(context).getFacet(MetadataFacet.class).getProjectName();
            String archiveLocation = getSelectedProject(context).getFacet(PackagingFacet.class).getFinalArtifact()
                    .getFullyQualifiedName().replace("\\/", "/");
            if (!new File(archiveLocation).exists()) {
                return Results.fail("You should first build the project before trying to deploy it");
            }
            hcpClient.stopRemote(application, account, userName, password);
            hcpClient.deployRemote(application, archiveLocation, account, userName, password);
            hcpClient.startRemote(application, account, userName, password);
        } catch (SapHanaCloudClientException shcce) {
            return Results.fail(shcce.getMessage());
        }

        return Results.success("Application was deployed successfully to the cloud");
    }


}