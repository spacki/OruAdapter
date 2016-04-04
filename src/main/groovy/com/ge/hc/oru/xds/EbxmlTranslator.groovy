package com.ge.hc.oru.xds

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.EbXMLFactory30
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.EbXMLQueryResponse30
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.ProvideAndRegisterDocumentSetRequestType
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry
import org.openehealth.ipf.commons.ihe.xds.core.responses.QueryResponse
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.query.AdhocQueryResponse
import org.openehealth.ipf.commons.ihe.xds.core.transform.responses.QueryResponseTransformer

/**
 * Created by 100026806 on 2/26/14.
 */
class EbxmlTranslator {

    private final static Log log = LogFactory.getLog(EbxmlTranslator.class)

    public ProvideAndRegisterDocumentSetRequestType convert(String cda, HashMap<String,String> metaData, String cdaEncoding)   {
        log.debug("Entered EbxmlTranslator convert method with document")
        def mdmStrategy = null
        def pnrDocumentSet = null
        def pnrDocRequest = null

            mdmStrategy = new OruMessageStrategy(cda, metaData)
            pnrDocumentSet = new PnRDocumentSetImpl()
            pnrDocRequest = pnrDocumentSet.getPnRRequest(mdmStrategy, cdaEncoding)
            return pnrDocRequest;
    }

    public static HashMap getSubmissionMode(AdhocQueryResponse response,
                                            String accessionNumber,
                                            String examinationCode,
                                            String reportMode,
                                            String finalFormatCodeRepresentation)   {

        log.debug("Iti18SubmissionMode")
        log.debug("looking for documents submitted for XDS DocumentEntry ends with uniqueId " + accessionNumber)
        def map = [:]
        String mode = "ORIGINAL"
        map.put("mode", mode)
        EbXMLFactory30 ebFactory = new EbXMLFactory30();
        QueryResponseTransformer qTransformer = new QueryResponseTransformer(ebFactory);
        EbXMLQueryResponse30 ebResponse = new EbXMLQueryResponse30(response);
        QueryResponse qResponse = qTransformer.fromEbXML(ebResponse);
        log.debug("patient has " +  qResponse.documentEntries.asList().size() + " entries" )
        for (int counter = 0; counter < qResponse.documentEntries.asList().size(); counter++) {
            log.debug("working on counter : " + counter)
            DocumentEntry docEntry = (qResponse.documentEntries.asList()).get(counter)
            log.debug("Unique Doc ID: " + docEntry.getEntryUuid())
            log.debug("XDS DocumentEntry uniqueID: " + docEntry.uniqueId);
            if (docEntry.uniqueId.endsWith(accessionNumber))  {
                log.debug("found a document which ends with the same accessionNumber")
                // check if the typeCode match
                log.debug("check typeCode is equal ${examinationCode}")
                def typeCode = docEntry.typeCode

                log.debug("TypeCode  ${typeCode}")
                String typeRepresentation = typeCode.code
                log.debug("found TypeCode: ${typeRepresentation}")

                if (typeRepresentation == examinationCode) {
                    // if then report from type preliminary we must verify that the old report is preliminary as well
                    def classCode = docEntry.classCode
                    log.debug("ClassCode  ${classCode}")
                    def classCodeRepresentation = classCode.code
                    /* to find out if a report is already final we use formatCode no more classCode*/
                    def formatCode = docEntry.formatCode
                    log.debug("FormatCode: ${formatCode}")
                    def formatCodeRepresentation = formatCode.code
                    log.debug ("looking for preliminary: " + formatCodeRepresentation)
                   // if ((reportMode == "preliminary") && (classCodeRepresentation == finalClassCodeRepresentation)) {
                    if ((reportMode == "preliminary") && (formatCodeRepresentation == finalFormatCodeRepresentation)) {
                        log.warn("receive a preliminary update for a finalized document")
                        map.put("mode", "INVALID")
                    } else {
                        map.put("mode", "REPLACE")
                    }
                    map.put("UUID", docEntry.entryUuid)
                    map.put("creationTime", docEntry.creationTime)
                }
            }
        }
        log.debug("SubmissionMode: " + map.get("mode"))
        map
    }

    public static boolean checkTimestamp(HashMap submissionMode, String creationTime ) {
        boolean valid = false
        if (submissionMode.get("mode") == "INVALID") {
            log.warn "try to submit a preliminary document, there a final is already available "
            valid = true
        }
        if (submissionMode.get("mode") == "REPLACE") {
            if (creationTime < submissionMode.get("creationTime")) {
                log.debug("creationTime of the new document: " + creationTime)
                log.debug("creationTime of the registered document: " + submissionMode.get("creationTime"))
                log.warn("message won't be processed since a newer version is already registered")
                valid = true
            } else {
                log.debug("creationTime of the new document: " + creationTime)
                log.debug("creationTime of the registered document: " + submissionMode.get("creationTime"))
                log.debug("message will be replace the older version after creation timestamp check is successfully passed ")
            }
        }
        valid
    }

}
