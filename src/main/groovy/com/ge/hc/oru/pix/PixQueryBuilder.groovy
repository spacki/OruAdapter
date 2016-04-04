package com.ge.hc.oru.pix

import com.ge.hc.oru.PixConfiguration
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openehealth.ipf.commons.ihe.hl7v2.definitions.pix.v25.message.QBP_Q21
import org.openehealth.ipf.modules.hl7dsl.MessageAdapter

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Created by karstenspakowski on 23/10/15.
 */
class PixQueryBuilder {

    private static final transient Log LOG = LogFactory.getLog(PixQueryBuilder.class);

    public static String createQBP(HashMap metaData, PixConfiguration pixConfiguration) {
        LOG.debug("Building the PixQuery")
        QBP_Q21 qbpMsg = new QBP_Q21()
        MessageAdapter qbpAdapter = new MessageAdapter(qbpMsg)
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss")
        Date now = new Date()
        String uniqueId = dateFormat.format(now)
        //LOG.info("pixTranslator.MBean:" + metaData.toString())

        qbpAdapter.MSH[1] = '|'
        qbpAdapter.MSH[2] = pixConfiguration.encodingChars
        qbpAdapter.MSH[3] = pixConfiguration.sendingApplication
        qbpAdapter.MSH[4] = pixConfiguration.sendingFacility
        qbpAdapter.MSH[5] = pixConfiguration.receivingApplication
        qbpAdapter.MSH[6] = pixConfiguration.receivingFacility
        //qbpAdapter.MSH[7] = metaData.get("creationTime")
        qbpAdapter.MSH[7] = uniqueId
        qbpAdapter.MSH[8] = ''
        qbpAdapter.MSH[9][1] = 'QBP'
        qbpAdapter.MSH[9][2] = 'Q23'
        qbpAdapter.MSH[9][3] = "QBP_Q21"
        qbpAdapter.MSH[10] = uniqueId
        //LOG.debug("pixTranslator.uniqueId:" + uniqueId)
        qbpAdapter.MSH[11] = 'P'
        qbpAdapter.MSH[12] = pixConfiguration.hl7Version
        qbpAdapter.MSH[15] = "AL"
        qbpAdapter.QPD[1][1] = pixConfiguration.pixQueryId_1
        qbpAdapter.QPD[1][2] = pixConfiguration.pixQueryId_2
        if (pixConfiguration.pixQueryId_3) qbpAdapter.QPD[1][3] = pixConfiguration.pixQueryId_3
        qbpAdapter.QPD[2] = 'QRY' + uniqueId
        qbpAdapter.QPD[3][1] = metaData.get("sourcePatientId") //SourcePatientId()
        qbpAdapter.QPD[3][2] = ''
        qbpAdapter.QPD[3][3] = ''
        if (metaData.get("sourcePatientIdLocalNameSpace")!= null) {
            qbpAdapter.QPD[3][4][1] = metaData.get("sourcePatientIdLocalNameSpace")
        }
        qbpAdapter.QPD[3][4][2] = metaData.get("sourcePatientDomainId")
        //LOG.debug("pixTranslator.bean.getSourcePatientOId():" + bean.getSourcePatientOId())
        qbpAdapter.QPD[3][4][3] = pixConfiguration.localUUtype

        //qbpAdapter.QPD[4][1] = ''
        //qbpAdapter.QPD[4][2] = ''
        //qbpAdapter.QPD[4][3] = ''
        if (pixConfiguration.globalAssigningAuthorityName) qbpAdapter.QPD[4][4][1] = pixConfiguration.globalAssigningAuthorityName
        qbpAdapter.QPD[4][4][2] = pixConfiguration.globalAssigningAuthority
        qbpAdapter.QPD[4][4][3] = pixConfiguration.localUUtype
        qbpAdapter.RCP[1] = 'I'
        qbpAdapter.RCP[2][1] = '10'
        qbpAdapter.RCP[2][2] = 'RD'
        //String pixQuery = qbpAdapter.toString()
        //String logMessage = pixQuery.replaceAll("\r", "\r\n")
        //LOG.debug("QBP Message -> \n" + logMessage);
        return qbpAdapter.toString()
    }

}
