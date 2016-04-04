package com.ge.hc.oru;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTimeZone;

import java.util.Set;

/**
 * Created by 100026806 on 2/24/14.
 */
public class OruConfiguration {

    private static final transient Log LOG = LogFactory.getLog(OruConfiguration.class);

    private static String inputUrl;
    private static String iti18Endpoint;
    private static String iti41Endpoint;
    private static String repositoryUniqueId;
    private static String submissionsSourceId;
    private static String timezoneId;
    private static boolean cdaBase64Encoding;
    private static String cdaEncoding;
    private static boolean cdaForceB64Representation;
    private static boolean enableUniqueIdPrefix;
    private static int uniqueIdPrefix;
    private static String accessionNumberDomain;
    private static String accessionNumberUrn;
    private static String orderNumberDomain;
    private static String orderNumberUrn;


    public String getAccessionNumberDomain() {
        return accessionNumberDomain;
    }

    public void setAccessionNumberDomain(String accessionNumberDomain) {
        this.accessionNumberDomain = accessionNumberDomain;
    }

    public String getAccessionNumberUrn() {
        return accessionNumberUrn;
    }

    public void setAccessionNumberUrn(String accessionNumberUrn) {
        this.accessionNumberUrn = accessionNumberUrn;
    }

    public String getOrderNumberDomain() {
        return orderNumberDomain;
    }

    public void setOrderNumberDomain(String orderNumberDomain) {
        this.orderNumberDomain = orderNumberDomain;
    }

    public String getOrderNumberUrn() {
        return orderNumberUrn;
    }

    public void setOrderNumberUrn(String orderNumberUrn) {
        this.orderNumberUrn = orderNumberUrn;
    }

    public boolean isEnableUniqueIdPrefix() {
        return enableUniqueIdPrefix;
    }

    public void setEnableUniqueIdPrefix(boolean enableUniqueIdPrefix) {
        this.enableUniqueIdPrefix = enableUniqueIdPrefix;
    }

    public int getUniqueIdPrefix() {
        return uniqueIdPrefix;
    }

    public void setUniqueIdPrefix(int uniqueIdPrefix) {
        this.uniqueIdPrefix = uniqueIdPrefix;
    }

    public boolean isCdaForceB64Representation() {
        return cdaForceB64Representation;
    }

    public void setCdaForceB64Representation(boolean cdaForceB64Representation) {
        this.cdaForceB64Representation = cdaForceB64Representation;
    }

    public String getCdaEncoding() {
        return cdaEncoding;
    }

    public void setCdaEncoding(String cdaEncoding) {
        this.cdaEncoding = cdaEncoding;
    }

    public boolean isCdaBase64Encoding() {
        return cdaBase64Encoding;
    }

    public void setCdaBase64Encoding(boolean cdaBase64Encoding) {
        this.cdaBase64Encoding = cdaBase64Encoding;
    }

    public String getInputUrl() {
        return inputUrl;
    }

    public void setInputUrl(String inputUrl) {
        this.inputUrl = inputUrl;
    }

    public String getIti18Endpoint() {
        return iti18Endpoint;
    }

    public void setIti18Endpoint(String iti18Endpoint) {
        this.iti18Endpoint = iti18Endpoint;
    }

    public String getIti41Endpoint() {
        return iti41Endpoint;
    }

    public void setIti41Endpoint(String iti41Endpoint) {
        this.iti41Endpoint = iti41Endpoint;
    }

    public String getRepositoryUniqueId() {
        return repositoryUniqueId;
    }

    public void setRepositoryUniqueId(String repositoryUniqueId) {
        this.repositoryUniqueId = repositoryUniqueId;
    }

    public String getSubmissionsSourceId() {
        return submissionsSourceId;
    }

    public void setSubmissionsSourceId(String submissionsSourceId) {
        this.submissionsSourceId = submissionsSourceId;
    }

    public static String getTimezoneId() { return timezoneId; }

    public void setTimezoneId(String timezoneId) {
        OruConfiguration.timezoneId = timezoneId;
        if (validateTimeZone(timezoneId)) {
            LOG.debug(" timezone configured to " + timezoneId);
            OruConfiguration.timezoneId=timezoneId;
        } else {
            LOG.warn(" no valid timezone configured, default is taken: UTC");
            OruConfiguration.timezoneId = "UTC";
        }

    }

    private boolean validateTimeZone(String timezoneId) {
        boolean valid = false;
        Set<String> timezoneIds = DateTimeZone.getAvailableIDs();
        /*
        for (String tz : timezoneIDs) {
            System.out.println("ID: " + tz);
        } */
        if (timezoneIds.contains(timezoneId)) valid=true;
        return valid;
    }

    @Override
    public String toString() {
        return "MdmConfiguration{" +
                "inputUrl='" + inputUrl + '\'' +
                ", iti18Endpoint='" + iti18Endpoint + '\'' +
                ", iti41Endpoint='" + iti41Endpoint + '\'' +
                ", repositoryUniqueId='" + repositoryUniqueId + '\'' +
                ", submissionsSourceId='" + submissionsSourceId + '\'' +
                ", timezoneId ='" + timezoneId + '\'' +
                '}';
    }
}
