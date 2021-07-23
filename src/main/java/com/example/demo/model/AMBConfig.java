package com.example.demo.model;

public class AMBConfig {

    public static final String REGION = "ap-northeast-2";

    // CA Properties
    public static final String ADMINUSER = "";
    public static final String ADMINPWD = "";
    public static final String CA_ORG1_URL = "";
    public static final String AMB_CERT_PATH = "";
    // Fabric user used by Lambda function
    public static final String LAMBDAUSER = "";
    public static final String LAMBDAUSERPWD ="";
    // Ordering service properties
    public static final String ORDERER_NAME = "";
    public static final String ORDERER_URL = "";
    // ORG1 properties
    public static final String ORG1 = ""; // affiliation
     public static final String ORG1_MSP = "";
    // ORG1 Peer properties
    public static final String ORG1_PEER_0 = "";
    public static final String ORG1_PEER_0_URL = "";

    // Channel & Chaincode info
    public static final String CHANNEL_NAME = "mychannel";
}
