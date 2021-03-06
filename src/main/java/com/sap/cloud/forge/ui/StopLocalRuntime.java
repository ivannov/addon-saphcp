package com.sap.cloud.forge.ui;

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
public class StopLocalRuntime extends AbstractSapHanaCloudCommand {


    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.from(super.getMetadata(context), this.getClass()).name("SAPHCP: Stop Local Runtime");
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        try {
            String sdkLocation = getProjectConfig(context).getSdkLocation();
            SapHanaCloudClient client = new SapHanaCloudCommandLineClient(sdkLocation);
            client.stopLocal();
        } catch (SapHanaCloudClientException shcce) {
            return Results.fail("Could not stop local runtime: " + shcce.getMessage());
        }
        return Results.success("Local runtime was stopped successfully");
    }


}
