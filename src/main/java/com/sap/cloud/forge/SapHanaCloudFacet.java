package com.sap.cloud.forge;

import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.AbstractFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.projects.facets.DependencyFacet;

public class SapHanaCloudFacet extends AbstractFacet<Project> implements ProjectFacet {

    static final String SDK_DEPENDENCY = "com.sap.cloud:neo-javaee6-wp-api:2.25.7";

    @Inject
    private DependencyInstaller dependencyInstaller;

    @Override
    public boolean install() {
        if (!isInstalled()) {
            dependencyInstaller.install(getFaceted(), buildHanaSdkDependency());
        }
        return true;
    }

    @Override
    public boolean isInstalled() {
        return getFaceted().getFacet(DependencyFacet.class).hasDirectDependency(
                buildHanaSdkDependency());
    }

    private Dependency buildHanaSdkDependency() {
        return DependencyBuilder.create(SDK_DEPENDENCY);
    }

}
