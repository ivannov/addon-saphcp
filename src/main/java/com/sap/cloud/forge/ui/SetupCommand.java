package com.sap.cloud.forge.ui;

import static com.sap.cloud.forge.ui.ConfigurationConstants.HANA_CLOUD_ACCOUNT;
import static com.sap.cloud.forge.ui.ConfigurationConstants.HANA_CLOUD_SDK;
import static com.sap.cloud.forge.ui.ConfigurationConstants.HANA_CLOUD_USER_NAME;

import javax.inject.Inject;

import org.jboss.forge.addon.configuration.Configuration;
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
        Configuration projectConfig = getProjectConfig(context);
        projectConfig.setProperty(HANA_CLOUD_SDK, sdkLocation.getValue().getFullyQualifiedName()
                .replace("\\/", "/"));
        projectConfig.setProperty(HANA_CLOUD_ACCOUNT, account.getValue());
        projectConfig.setProperty(HANA_CLOUD_USER_NAME, userName.getValue());
        facetFactory.install(selectedProject, SapHanaCloudFacet.class);
        return Results.success("SAP HANA Cloud configured successfully for this project!");
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