package com.sap.cloud.forge;

import static com.sap.cloud.forge.ConfigurationConstants.HANA_CLOUD_SDK;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.inject.Inject;

import org.jboss.forge.addon.configuration.Configuration;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;

@FacetConstraint(SapHanaCloudFacet.class)
public class DeployLocallyCommand extends AbstractProjectCommand {

    static final String PICKUP_DIRECTORY = "server/pickup";

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
	return Metadata.forCommand(DeployLocallyCommand.class).name(
		"SAPHCP: Deploy Locally");
    }

    @Override
    public void initializeUI(UIBuilder builder) throws Exception {
    }

    @Inject
    private Configuration projectConfig;
    
    @Inject
    private ResourceFactory resourceFactory;
    
    @Override
    public Result execute(UIExecutionContext context) {
	DirectoryResource sdkLocation = resourceFactory.create(DirectoryResource.class, new File(projectConfig.getString(HANA_CLOUD_SDK)));
        PackagingFacet packagingFacet = getSelectedProject(context).getFacet(PackagingFacet.class);
        File deployableArchive = new File(packagingFacet.getFinalArtifact().getFullyQualifiedName());
        FileResource<?> deployFile = (FileResource<?>) sdkLocation.getChild(PICKUP_DIRECTORY).getChild(
                deployableArchive.getName());
        try {
	    deployFile.setContents(new FileInputStream(deployableArchive));
	} catch (FileNotFoundException e) {
	    Results.fail(e.getMessage());
	}
	
	return Results.success("The project was deployed successfully on the local runtime");
    }

    @Override
    protected boolean isProjectRequired() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    protected ProjectFactory getProjectFactory() {
	// TODO Auto-generated method stub
	return null;
    }
}