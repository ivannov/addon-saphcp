package com.sap.cloud.forge.ui;

import java.io.File;

import javax.inject.Inject;

import org.jboss.forge.addon.configuration.facets.ConfigurationFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

import com.sap.cloud.forge.ui.config.ProjectConfiguration;

public abstract class AbstractSapHanaCloudCommand extends AbstractProjectCommand {

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.forCommand(getClass()).category(Categories.create("Cloud"));
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

    @Inject
    private ProjectFactory projectFactory;

    @Override
    protected ProjectFactory getProjectFactory() {
        return projectFactory;
    }

    @Inject
    private ResourceFactory resourceFactory;

    protected DirectoryResource toDirectoryResource(String directoryPath) {
        return resourceFactory.create(DirectoryResource.class, new File(directoryPath));
    }


    protected ProjectConfiguration getProjectConfig(UIExecutionContext context) {
        return new ProjectConfiguration(getSelectedProject(context)
                .getFacet(ConfigurationFacet.class).getConfiguration());
    }


    @Override
    public void initializeUI(UIBuilder arg0) throws Exception {
    }
}
