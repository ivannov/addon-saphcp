package com.sap.cloud.forge.ui.config;

import static com.sap.cloud.forge.ui.config.ConfigurationConstants.*;

import org.jboss.forge.addon.configuration.Configuration;

public class ProjectConfiguration {

    private Configuration projectConfig;

    public ProjectConfiguration(Configuration projectConfig) {
        this.projectConfig = projectConfig;
    }
    
    public String getSdkLocation() {
        return projectConfig.getString(HANA_CLOUD_SDK);
    }
    
    public void setSdkLocation(String sdkLocation) {
        projectConfig.setProperty(HANA_CLOUD_SDK, sdkLocation);
    }
    
    public String getAccount() {
        return projectConfig.getString(HANA_CLOUD_ACCOUNT);
    }
    
    public void setAccount(String account) {
        projectConfig.setProperty(HANA_CLOUD_ACCOUNT, account);
    }
    
    public String getUserName() {
        return projectConfig.getString(HANA_CLOUD_USER_NAME);
    }
    
    public void setUserName(String userName) {
        projectConfig.setProperty(HANA_CLOUD_USER_NAME, userName);
    }
}
