package com.ge.hc.oru.xds

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AssigningAuthority
import org.openehealth.ipf.commons.ihe.xds.core.metadata.AvailabilityStatus
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Identifiable
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.FindDocumentsQuery
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.QueryReturnType

/**
 * Created by 100026806 on 3/2/14.
 */
class MetadataToIti18Translator {

    private final static Log LOG = LogFactory.getLog(MetadataToIti18Translator.class)

    public QueryRegistry convert(HashMap metaData) {
        LOG.debug("Entered MetdataToIti18EbxmlTranslator convert")
        def queryReg = null
        def sID = metaData.get("globalPatientId")
        def sOID = metaData.get("globalPatientOid");

        //def sID = patient.patientId
        //def sOID = patient.patientOid


        //For quick testing
        //sID = "1000042000"
        //sOID = "2.16.840.1.113883.3.1.3.2112.1.1.1.3.1"


        LOG.debug("Global ID: " + sID)
        LOG.debug("Signing Authority: " + sOID)


        //Creates Query
        def query = new FindDocumentsQuery()
        def patID = new Identifiable()


        //Create patient object
        patID.id = sID;
        patID.assigningAuthority = new AssigningAuthority(sOID);


        //Assign patient to query
        query.patientId = patID
        //query.status = [AvailabilityStatus.APPROVED, AvailabilityStatus.SUBMITTED, AvailabilityStatus.DEPRECATED] //this is set to approved but might be changed in future
        query.status = [AvailabilityStatus.APPROVED]
        queryReg = new QueryRegistry(query)

        queryReg.setReturnType(QueryReturnType.LEAF_CLASS)

        //queryReg.returnLeafObjects = true
        LOG.debug("Query:  "+ query)
        LOG.debug("QueryReg: " +queryReg)
        return queryReg
    }


}
