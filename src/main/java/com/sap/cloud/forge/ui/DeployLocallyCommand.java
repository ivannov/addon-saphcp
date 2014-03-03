package com.sap.cloud.forge.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;

import com.sap.cloud.forge.SapHanaCloudFacet;

@FacetConstraint(SapHanaCloudFacet.class)
public class DeployLocallyCommand extends AbstractSapHanaCloudCommand {

    static final String PICKUP_DIRECTORY = "server/pickup";

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.from(super.getMetadata(context), this.getClass()).name(
                "SAPHCP: Deploy Locally");
    }

    @Override
    public Result execute(UIExecutionContext context) {
        DirectoryResource sdkLocation = toDirectoryResource(getProjectConfig(context).getSdkLocation());
        PackagingFacet packagingFacet = getSelectedProject(context).getFacet(PackagingFacet.class);
        File deployableArchive = new File(packagingFacet.getFinalArtifact().getFullyQualifiedName());
        if (!deployableArchive.exists()) {
            return Results.fail("You should first build the project before trying to deploy it");
        }
        FileResource<?> deployFile = (FileResource<?>) sdkLocation.getChild(PICKUP_DIRECTORY)
                .getChild(deployableArchive.getName());
        try {
            deployFile.setContents(new FileInputStream(deployableArchive));
        } catch (FileNotFoundException e) {
            return Results.fail(e.getMessage());
        }

        return Results.success("The project was deployed successfully on the local runtime");
    }
}