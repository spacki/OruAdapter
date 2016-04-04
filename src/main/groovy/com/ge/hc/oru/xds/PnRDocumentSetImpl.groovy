package com.ge.hc.oru.xds

import com.ge.hc.oru.commons.MessageStrategyAbs
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.ProvideAndRegisterDocumentSetRequestType

/**
 * Created by 100026806 on 2/26/14.
 */
class PnRDocumentSetImpl {

    Log log = LogFactory.getLog(PnRDocumentSetImpl.class)

    public ProvideAndRegisterDocumentSetRequestType getPnRRequest(MessageStrategyAbs msgStrategy, String cdaEncoding){
        log.debug("entered PNR implementation")

        XdsRequestHelper xdsHelper = new XdsRequestHelper()
        //Create new ProvideAndRegisterDocumentSetRequestType

        ProvideAndRegisterDocumentSetRequestType request = new ProvideAndRegisterDocumentSetRequestType()

        //Use helper class to create SubmitObjectsRequest
        request.submitObjectsRequest =
                xdsHelper.createSubmitObjectsRequest("XDS_b Submission", msgStrategy.metaInput)

        //Get document content as String from MessageStrategy
        request.document.add(
                xdsHelper.createDocumentContent("Document01", msgStrategy.getDocContent(), cdaEncoding))
        return request
    }
}
