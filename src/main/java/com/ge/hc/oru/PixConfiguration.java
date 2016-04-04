package com.ge.hc.oru;

/**
 * Created by karstenspakowski on 23/10/15.
 */
public class PixConfiguration {

    private static String enablePix;
    private static String pixManagerInfo;
    private static String localNamespace;
    private static String localAssigningAuthority;
    private static String localUUtype;
    private static String globalAssigningAuthorityName;
    private static String globalAssigningAuthority;
    private static String pixQueryId_1;
    private static String pixQueryId_2;
    private static String pixQueryId_3;
    // values that might be used for PIX query
    private static String encodingChars;
    private static String sendingFacility;
    private static String sendingApplication;
    private static String receivingFacility;
    private static String receivingApplication;
    private static String hl7Version;

    public String getEnablePix() {
        return enablePix;
    }

    public void setEnablePix(String enablePix) {
        this.enablePix = enablePix;
    }

    public String getPixManagerInfo() {
        return pixManagerInfo;
    }

    public void setPixManagerInfo(String pixManagerInfo) {
        this.pixManagerInfo = pixManagerInfo;
    }

    public String getLocalNamespace() {
        return localNamespace;
    }

    public void setLocalNamespace(String localNamespace) {
        this.localNamespace = localNamespace;
    }

    public String getLocalAssigningAuthority() {
        return localAssigningAuthority;
    }

    public void setLocalAssigningAuthority(String localAssigningAuthority) {
        this.localAssigningAuthority = localAssigningAuthority;
    }

    public String getLocalUUtype() {
        return localUUtype;
    }

    public void setLocalUUtype(String localUUtype) {
        this.localUUtype = localUUtype;
    }

    public String getGlobalAssigningAuthorityName() {
        return globalAssigningAuthorityName;
    }

    public void setGlobalAssigningAuthorityName(String globalAssigningAuthorityName) {
        this.globalAssigningAuthorityName = globalAssigningAuthorityName;
    }

    public String getGlobalAssigningAuthority() {
        return globalAssigningAuthority;
    }

    public void setGlobalAssigningAuthority(String globalAssigningAuthority) {
        this.globalAssigningAuthority = globalAssigningAuthority;
    }

    public String getPixQueryId_1() {
        return pixQueryId_1;
    }

    public void setPixQueryId_1(String pixQueryId_1) {
        this.pixQueryId_1 = pixQueryId_1;
    }

    public String getPixQueryId_2() {
        return pixQueryId_2;
    }

    public void setPixQueryId_2(String pixQueryId_2) {
        this.pixQueryId_2 = pixQueryId_2;
    }

    public String getPixQueryId_3() {
        return pixQueryId_3;
    }

    public void setPixQueryId_3(String pixQueryId_3) {
        this.pixQueryId_3 = pixQueryId_3;
    }

    public String getEncodingChars() {
        return encodingChars;
    }

    public void setEncodingChars(String encodingChars) {
        this.encodingChars = encodingChars;
    }

    public String getSendingFacility() {
        return sendingFacility;
    }

    public void setSendingFacility(String sendingFacility) {
        this.sendingFacility = sendingFacility;
    }

    public String getSendingApplication() {
        return sendingApplication;
    }

    public void setSendingApplication(String sendingApplication) {
        this.sendingApplication = sendingApplication;
    }

    public String getReceivingFacility() {
        return receivingFacility;
    }

    public void setReceivingFacility(String receivingFacility) {
        this.receivingFacility = receivingFacility;
    }

    public String getReceivingApplication() {
        return receivingApplication;
    }

    public void setReceivingApplication(String receivingApplication) {
        this.receivingApplication = receivingApplication;
    }

    public String getHl7Version() {
        return hl7Version;
    }

    public void setHl7Version(String hl7Version) {
        this.hl7Version = hl7Version;
    }

    @Override
    public String toString() {
        return "PixConfiguration{" +
                "enablePix='" + enablePix + '\'' +
                ", pixManagerInfo='" + pixManagerInfo + '\'' +
                ", localNamespace='" + localNamespace + '\'' +
                ", localAssigningAuthority='" + localAssigningAuthority + '\'' +
                ", localUUtype='" + localUUtype + '\'' +
                ", globalAssigningAuthorityName='" + globalAssigningAuthorityName + '\'' +
                ", globalAssigningAuthority='" + globalAssigningAuthority + '\'' +
                ", pixQueryId_1='" + pixQueryId_1 + '\'' +
                ", pixQueryId_2='" + pixQueryId_2 + '\'' +
                ", pixQueryId_3='" + pixQueryId_3 + '\'' +
                ", encodingChars='" + encodingChars + '\'' +
                ", sendingFacility='" + sendingFacility + '\'' +
                ", sendingApplication='" + sendingApplication + '\'' +
                ", receivingFacility='" + receivingFacility + '\'' +
                ", receivingApplication='" + receivingApplication + '\'' +
                ", hl7Version='" + hl7Version + '\'' +
                '}';
    }
}
