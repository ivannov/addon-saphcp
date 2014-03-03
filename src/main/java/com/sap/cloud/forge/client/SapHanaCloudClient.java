package com.sap.cloud.forge.client;

public interface SapHanaCloudClient {

    public void stopRemote(String application, String account, String userName, 
            String password) throws SapHanaCloudClientException;
    
    public void startRemote(String application, String account, String userName, 
            String password) throws SapHanaCloudClientException;
    
    public void deployRemote(String application, String archiveLocation, String account, 
            String userName, String password) throws SapHanaCloudClientException;
    
    public void installLocal() throws SapHanaCloudClientException;
    
    public void startLocal() throws SapHanaCloudClientException;
    
    public void stopLocal() throws SapHanaCloudClientException;
}
