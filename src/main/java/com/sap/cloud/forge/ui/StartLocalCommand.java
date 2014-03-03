package com.sap.cloud.forge.ui;

import java.io.File;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;

import com.sap.cloud.forge.SapHanaCloudFacet;
import com.sap.cloud.forge.client.SapHanaCloudClient;
import com.sap.cloud.forge.client.SapHanaCloudClientException;
import com.sap.cloud.forge.client.SapHanaCloudCommandLineClient;

@FacetConstraint(SapHanaCloudFacet.class)
public class StartLocalCommand extends AbstractSapHanaCloudCommand {

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.from(super.getMetadata(context), this.getClass()).name("SAPHCP: Start Local Runtime");
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        try {
            String sdkLocation = getProjectConfig(context).getSdkLocation();
            SapHanaCloudClient client = new SapHanaCloudCommandLineClient(new File(sdkLocation), 
                    sdkLocation);
            client.startLocal();
        } catch (SapHanaCloudClientException shcce) {
            return Results.fail("Could not start local runtime: " + shcce.getMessage());
        }
        return Results.success("Local runtime was started successfully");
    }

}
