package com.ge.hc.oru

import ca.uhn.hl7v2.model.v24.message.ACK
import ca.uhn.hl7v2.model.v24.message.ORM_O01
import ca.uhn.hl7v2.model.v24.message.ORU_R01
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openehealth.ipf.modules.hl7dsl.MessageAdapter
import com.ge.hc.oru.util.IdHelper

import static org.openehealth.ipf.modules.hl7dsl.MessageAdapters.make

/**
 * Created by 100026806 on 2/24/14.
 */
class OruAdapter {

    private static final transient Log LOG = LogFactory.getLog(OruAdapter.class)


    public static HashMap<String,String> extractData(MessageAdapter oruMessage, String timezone) {

        LOG.debug("all timestamps will be converted to: " + timezone)
        HashMap<String, String> map = new HashMap<String, String>();
        MessageAdapter<ORU_R01> message = oruMessage
        // take submissionTime from MSH-7
        String localsubmissionTime = message.MSH[7].value.substring(0, 14)
        def submissionTime = IdHelper.getUTCTimestamp(localsubmissionTime, timezone)
        LOG.debug("submissiontime: ${submissionTime}")
        map.put("submissionTime",submissionTime)

        // now get the author information from OBR
        String localAuthorTime = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[22][1]
        def authorTime = IdHelper.getUTCTimestamp(localAuthorTime, timezone)
        LOG.debug("authorTime: " + authorTime)
        map.put("localAuthorTime", localAuthorTime)
        map.put("authorTime", authorTime)
        def author = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[32].encode()
        LOG.debug("author from OBR[32]: ${author}")


        /*
        EventCodeList from OBR19
        */
        String eventCodeInformation = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[19]
        if (eventCodeInformation == null || eventCodeInformation.isEmpty()) {
            LOG.debug("message does not contain any eventCode information");
        }  else {
            String[] events = eventCodeInformation.split(';')
            if (events.length == 3) {
                map.put("eventCodeRepresenation",events[1]);
                map.put("eventCodeDisplayName", events[2]);
                map.put("eventCodeScheme", events[0]);
            }  else {
                LOG.debug("eventCode list array does not have exactly 3 informations |" + eventCodeInformation + "| there should be three information separated by ; but the message contains: " + events.length);
            }

        }

        String authorId = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[33][1][1]
        LOG.debug("authorId: " + authorId)
        map.put("cdaAuthorId", authorId)
        String authorDomainId = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[33][1][9]
        LOG.debug("authorDomainId: " + authorDomainId)
        map.put("cdaAuthorDomainId", authorDomainId)
        map.put("authorDomainId", authorDomainId)
        String familyName = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[33][1][2]
        LOG.debug("familyName: " + familyName )
        String givenName = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[33][1][3]
        String prefix = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[33][1][6]
        String suffix = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[33][1][5]
        String middleName = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[33][1][4]
        String authorFamilyName = familyName ?: ""
        String authorGivenName  = givenName ?: ""
        String authorMiddleName = middleName ?: ""
        String authorSuffix = suffix ?: ""
        String authorPrefix = prefix ?: ""
        String authorPerson =  authorId  + "^"  + authorFamilyName + "^" + authorGivenName + "^" +
                authorMiddleName + "^" + authorSuffix + "^" + authorPrefix
        map.put("authorGivenName", authorGivenName)
        map.put("authorFamilyName", authorFamilyName)
        map.put("authorPrefix", authorPrefix)
        LOG.debug("authorName: ${authorPerson}")
        map.put("authorName", authorPerson)
        String assignedAuthorRepresentationOrganizationIdExtension = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[33][7][1]
        String assignedAuthorRepresentationOrganizationIdRoot = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[33][7][2]
        LOG.debug("principalResultInterpreterFacilityId: " + assignedAuthorRepresentationOrganizationIdExtension);
        LOG.debug("cdaAssignedAuthorRepresentationOrganizationIdRoot: " + assignedAuthorRepresentationOrganizationIdRoot);
        map.put("cdaAssignedAuthorRepresentationOrganizationIdExtension" ,assignedAuthorRepresentationOrganizationIdExtension);
        map.put("principalResultInterpreterFacilityDomainId", assignedAuthorRepresentationOrganizationIdRoot)
        String authorOrganizationName = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[33][4]
        LOG.debug("authorInstitutionName: " + authorOrganizationName)
        map.put("cdaAuthorOrganizationName", authorOrganizationName)
        /* Legal Author*/
        String legalAuthorId = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[32][1][1]
        LOG.debug("legalAuthorId: " + legalAuthorId)
        map.put("cdaLegalAuthorId", legalAuthorId)
        String legalAuthorDomainId = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[32][1][9]
        LOG.debug("legalAuthorDomainId: " + legalAuthorDomainId)
        map.put("cdaLegalAuthorDomainId", legalAuthorDomainId)
        String legalAuthorFamilyName = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[32][1][2]
        LOG.debug("legalAuthorFamilyName: " + legalAuthorFamilyName)
        String legalAuthorGivenName = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[32][1][3]
        String legalAuthorPrefix = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[32][1][6]
        map.put("cdaLegalAuthorGivenName", (legalAuthorGivenName ?: ""))
        map.put("cdaLegalAuthorFamilyName", (legalAuthorFamilyName ?: ""))
        map.put("cdaLegalAuthorPrefix", (legalAuthorPrefix ?: ""))

        /*
          Paticiapant
         */

        /*
        * HealthcareFacilityTypeCode from ORC.17 (CodingScheme^CodeValue^CodeMeaning
         */

        String healthcareFacilityCodeCodingScheme = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[17][1].value
        String healthcareFacilityCodeValue  = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[17][2].value
        String healthcareFacilityCodeMeaning = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[17][3].value
        if (healthcareFacilityCodeCodingScheme?.trim()) {
            LOG.debug("healthcareFacilityCodeCodingScheme is neither null nor empty: " + healthcareFacilityCodeCodingScheme)
            map.put("healthcareFacilityCodeCodingScheme", healthcareFacilityCodeCodingScheme)
        } else {
            LOG.debug("healthcareFacilityCodeCodingScheme is either null or empty: |" + healthcareFacilityCodeCodingScheme + "|")
            map.put("healthcareFacilityCodeCodingScheme", null)
        }

        if (healthcareFacilityCodeValue?.trim()) {
            LOG.debug("healthcareFacilityCodeValue is neither null nor empty: " + healthcareFacilityCodeValue)
            map.put("healthcareFacilityCodeNodeRepresentation", healthcareFacilityCodeValue)
        } else {
            LOG.debug("healthcareFacilityCodeValue is either null or empty: |" + healthcareFacilityCodeValue + "|")
            map.put("healthcareFacilityCodeNodeRepresentation", null)
        }

        if (healthcareFacilityCodeMeaning?.trim()) {
            LOG.debug("healthcareFacilityCodeMeaning is neither null nor empty: " + healthcareFacilityCodeMeaning)
            map.put("healthcareFacilityCodeDisplayName", healthcareFacilityCodeMeaning)
        } else {
            LOG.debug("healthcareFacilityCodeMeaning is either null or empty: |" + healthcareFacilityCodeMeaning + "|")
            map.put("healthcareFacilityCodeDisplayName", null)
        }





        //Ordering facility

        String orderFacilityStreet = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[22][1].value
        String orderFacilityCity   = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[22][3].value
        String orderFacilityCountry = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[22][6].value
        String orderfacilityPostalCode = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[22][5].value
        map.put("cdaOrderFacilityStreet",orderFacilityStreet);
        map.put("cdaOrderFacilityCity",orderFacilityCity);
        map.put("cdaOrderFacilityCountry",orderFacilityCountry);
        map.put("cdaOrderFacilityPostalCode",orderfacilityPostalCode);


        String  localParticipantTime = null
        String  participantTime = null
        try {
            localParticipantTime = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[9][1].value.substring(0, 14)
            participantTime = IdHelper.getUTCTimestamp(localParticipantTime, timezone)
        } catch (Exception e) {
            LOG.debug("ORC 9 seems to be empty")
        }
        LOG.debug("participantTime ${participantTime}")
        map.put("participantTime", participantTime)
        LOG.debug("localParticipantTime ${localParticipantTime}")
        map.put("localParticipantTime", localParticipantTime)
        String orderFacilityName = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[21][1].value
        if (!orderFacilityName) orderFacilityName = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[17][2]
        LOG.debug("cdaOrderFacilityName" + orderFacilityName)
        map.put("cdaOrderFacilityName", orderFacilityName )
        String orderFacilityPhone = orderFacilityName = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[23][1]
        map.put("cdaOrderFacilityPhone", orderFacilityPhone)
        String participantId = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[12][1]
        LOG.debug("participantId: " + participantId)
        map.put("cdaScopingOrganizationIdExtension", participantId)
        String participantGivenName = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[12][3]
        LOG.debug("participantGivenName: " + participantGivenName)
        map.put("participantGivenName", participantGivenName)
        String participantFamilyName  = message.PATIENT_RESULT.ORDER_OBSERVATION.ORC[12][2][1]
        LOG.debug("participantFamilyName: " + participantFamilyName)
        map.put("participantFamilyName", participantFamilyName)
        String orderNumber = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[2][1].value
        LOG.debug("orderNumber: " + orderNumber)
        map.put("orderNumber", orderNumber)
        String accessionNumber = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[3][1].value
        LOG.debug("accessionNumber: " + accessionNumber)
        map.put("accessionNumber",accessionNumber)
        String referralId = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[3][1]
        LOG.debug("referralId: " + referralId)
        map.put("referralId",referralId)
        String examinationCode = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[4][1]
        LOG.debug("examinationCode: " + examinationCode)
        map.put("examinationCode",examinationCode)
        String examinationName = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[4][2]
        LOG.debug("examinationName: " + examinationName)
        map.put("examinationName",examinationName )
        /*
        String authorInstitute = message.TXA[5][8] // "Odense Universitetshospital – Svendborg^^^^^^^^^1.2.208.183.1.8071000016009"
        LOG.debug("authorInstitute: ${authorInstitute}")
        map.put("authorInstitute", authorInstitute)
        String authorRole = message.TXA[5][9][1]//"Referring Physician"
        LOG.debug("authorRole: ${authorRole}")
        map.put("authorRole", authorRole)
        */

        // must changed root + timeStamp +  accessionNumber
        String uniqueId = message.MSH[10]
        LOG.debug("uniqueId: ${uniqueId}")
        map.put("uniqueId", uniqueId)
        // source id from ini file


        def pid   = message.PATIENT_RESULT(0).PATIENT.PID

        String sourcePatientId = pid[3][1]
        String sourcePatientIdLocalNameSpace = pid[3][4][1]
        String sourcePatientDomainId = pid[3][4][2]
        String sourcePatientDomainUniversalIdType = pid[3][4][3]
        LOG.debug("patientId: ${sourcePatientId}")
        LOG.debug("patientLocalNameSpace: ${sourcePatientIdLocalNameSpace}")
        LOG.debug("patientDomain: ${sourcePatientDomainId}")
        LOG.debug("patient universal ID type ${sourcePatientDomainUniversalIdType}")
        map.put("sourcePatientId", sourcePatientId)
        map.put("sourcePatientIdLocalNameSpace", sourcePatientIdLocalNameSpace)
        map.put("sourcePatientDomainId", sourcePatientDomainId)
        map.put("sourcePatientDomainUniversalIdType", sourcePatientDomainUniversalIdType)
        //def sourcePatientGivenName = sourcePatientName.getGivenName()  is equal, message.PID[5][2]

        String patientGivenName = pid[5][2]
        String sourcePatientGivenName =   patientGivenName ?: ""
        String patientFamilyName = pid[5][1][1] // no idea why the second 1 is needed but otherwise we will get  org.openehealth.ipf.modules.hl7dsl.CompositeAdapter
        String sourcePatientFamilyName = patientFamilyName ?: ""
        String patientMiddleName = pid[5][3]
        String sourcePatientMiddleInitialOrName = patientMiddleName ?: ""
        String patientSuffix = pid[5][4]
        String sourcePatientNameSuffix = patientSuffix ?: ""
        String patientPrefix = pid[5][5]
        def sourcePatientNamePrefix = patientPrefix ?: ""
        String sourcePatientName = sourcePatientFamilyName  + "^"  + sourcePatientGivenName + "^" +
                sourcePatientMiddleInitialOrName + "^" + sourcePatientNameSuffix + "^" + sourcePatientNamePrefix
        LOG.debug("sourcePatientGivenName ${sourcePatientGivenName}")
        LOG.debug("sourcePatientFamilyName ${sourcePatientFamilyName}")
        LOG.debug("sourcePatientMiddleInitialOrName ${sourcePatientMiddleInitialOrName}")
        LOG.debug("sourcePatientNameSuffix ${sourcePatientNameSuffix}")
        LOG.debug("sourcePatientNamePrefix ${sourcePatientNamePrefix}")
        LOG.debug("sourcePatientName ${sourcePatientName}")
        map.put("lastName", sourcePatientFamilyName)
        map.putAt("firstName", sourcePatientGivenName)
        map.put("sourcePatientName", sourcePatientName)

        def sourcePatientBirthdate = pid[7][1].value.substring(0, 8)
        LOG.debug("sourcePatientBirthdate ${sourcePatientBirthdate}")
        map.put("sourcePatientBirthdate", sourcePatientBirthdate)
        String sourcePatientSexCode = pid[8]
        LOG.debug("sourcePatientSexCode ${sourcePatientSexCode}")
        map.put("sourcePatientSexCode", sourcePatientSexCode)
        String sourcePatientSexDisplayName = genderLookup(sourcePatientSexCode)
        LOG.debug("sourcePatientSexDisplayName " + sourcePatientSexDisplayName)
        map.put("sourcePatientSexDisplayName",sourcePatientSexDisplayName)
        String sourcePatientAddress = pid[11][1][1]
        LOG.debug("AddressStreet: " + sourcePatientAddress)
        map.put("sourcePatientAddress", sourcePatientAddress)
        String sourcePatientAddressCity = pid[11][3]
        LOG.debug("AddressCity: " + sourcePatientAddressCity)
        map.put("sourcePatientAddressCity",sourcePatientAddressCity)
        String sourcePatientAddressState = pid[11][4]
        LOG.debug("AddressState: " + sourcePatientAddressState)
        map.put("sourcePatientAddressState", sourcePatientAddressState)
        String sourcePatientAddressZipCode = pid[11][5]
        LOG.debug("AddressZipCode: " + sourcePatientAddressZipCode)
        map.put("sourcePatientAddressZipCode", sourcePatientAddressZipCode)
        String otherGeographicDesignation = pid[11][8]
        LOG.debug("OtherGeographicDesignation: " + otherGeographicDesignation)
        map.put("otherGeographicDesignation", otherGeographicDesignation)
        String telephoneNumber = pid[13][1]
        LOG.debug("Telephone: " + telephoneNumber)
        map.put("telephoneNumber", telephoneNumber)
        def reportTitle    = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[4][2]
        LOG.debug("ReportTitle taken from OBR.4.2 -----> " + reportTitle)
        map.put("reportTitle", reportTitle)
        def localEffectiveTime = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[7][1].value.substring(0, 14)
        def effectiveTime = IdHelper.getUTCTimestamp(localEffectiveTime, timezone)
        LOG.debug("effectiveTime ${effectiveTime}")
        map.put("localEffectiveTime", localEffectiveTime)
        map.put("effectiveTime", effectiveTime)
        //map.put("creationTime", effectiveTime)
        map.put("localServiceStartTime", localEffectiveTime)
        map.put("serviceStartTime", effectiveTime)
        def localServiceStopTime = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[8][1].value.substring(0, 14)
        def serviceStopTime = IdHelper.getUTCTimestamp(localServiceStopTime, timezone)
        LOG.debug("serviceStopTime" + serviceStopTime)
        map.put("localServiceStopTime", localServiceStopTime)
        map.put("serviceStopTime", serviceStopTime)
        // get reportMode from OBR25 P=Prelimanry, F=Final , C=Addendum
        String mode = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[25]
        LOG.debug("reportMode: |" + mode + "|")
        String reportMode  = orderModeLookup(mode)
        map.put("reportMode", reportMode)
        String localPerformerTime = null
        String performerTime  = null
        try {
            localPerformerTime = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[6][1].value.substring(0,14)
            performerTime = IdHelper.getUTCTimestamp(localPerformerTime, timezone)
        } catch (Exception e) {
            LOG.debug("OBR6 is empty")

        }
        map.put("cdaLocalPerformerTime", localPerformerTime)
        map.put("cdaPerformerTime", performerTime)
        String performerAuthorIdRoot = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[32][1][1].value
        String performerAuthorIdExtension = message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[32][1][9].value
        LOG.debug("performerAuthorIdRoot: " + performerAuthorIdRoot)
        LOG.debug("performerAuthorIdExtension:" + performerAuthorIdExtension)
        map.put("cdaPerformerAuthorIdRoot", performerAuthorIdRoot)
        map.put("cdaPerformerAuthorIdExtension", performerAuthorIdExtension)
        // dangerCode
        String dangerCode = null
        def dangerCodes = []
        if (message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[12][1].value) dangerCodes.add(message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[12][1].value)
        if (message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[12][2].value) dangerCodes.add(message.PATIENT_RESULT.ORDER_OBSERVATION.OBR[12][2].value)
        if (dangerCodes.size() == 2) {
            dangerCode = dangerCodes[0] + " " + dangerCodes[1]
            map.put("dangers", dangerCodes)
        } else {
            if (dangerCodes.size() == 1 ) {
                dangerCode = dangerCodes[0]
            }
        }
        LOG.debug("dangerCode: " + dangerCode)
        map.put("cdaDangerCode", dangerCode)

        String localClinicalTime = null
        try {
            localClinicalTime =message.EVN[2].value
        } catch (Exception e)  {
            LOG.error("EVN Segment is missing")
        }
        if (!localClinicalTime) localClinicalTime = message.MSH[7].value
        def clinicalTime = IdHelper.getUTCTimestamp(localClinicalTime, timezone)
        LOG.debug("localClinicalTime: " + localClinicalTime)
        map.put("clinicalTime", clinicalTime)
        map.put("localClinicalTime", localClinicalTime.substring(0,14))

        //def creationTime = message.MSH[7].value
        //map.put("creationTime", creationTime.substring(0,14))


        String localcreationTime = message.MSH[7].value.substring(0, 14)
        def creationTime = IdHelper.getUTCTimestamp(localcreationTime, timezone)
        LOG.debug("creationTime: ${creationTime}")
        map.put("creationTime",creationTime)
        return map;
    }

    public static void sortedPrintMap(Map map, String whatCovered) {
        System.out.println("++++++++++++++++++++ Begin Metadata from " + whatCovered +" +++++++++++++++++++++++++++++++");
        List<String> list = new ArrayList<String>();
        for (Object str : map.keySet()) {
            list.add(str.toString());
        }
        Collections.sort(list);
        for (String key : list) {
            System.out.println(key + " = " + map.get(key));
        }
        System.out.println("++++++++++++++++++++ End Metadata from " + whatCovered +" +++++++++++++++++++++++++++++++");
    }

    public static String getBinaryDoc(MessageAdapter mdmMessage) {
        MessageAdapter<ORU_R01> message = mdmMessage
        //def document = message.OBX[5][4]
        //LOG.debug("found  document" )
        //LOG.debug("document " + document)
        //return document
        def observationCode = message.OBX[5][5]
        LOG.debug("found  " + observationCode + " document" )
        return observationCode

    }


    public static byte[] getReport(MessageAdapter oruMessage, boolean base64Encoding) {
        MessageAdapter<ORU_R01> message = oruMessage
        def repeatingGroup = message.PATIENT_RESULT;
        def group = repeatingGroup(0);
        def obsGroup = group.ORDER_OBSERVATION(0);
        StringBuilder report = new StringBuilder();

        for (int i = 0; i < obsGroup.getOBSERVATIONReps(); i++) {
            def obx5 = obsGroup.getOBSERVATION(i).OBX[5];
            if (obx5?.value != null) {
                //def obx5Report = obx5.value.replaceAll("XD", "\n")
                def obx5Report = obx5.value
                report.append(obx5Report + "\n");
                //report.append(obx5?.value + "\n");
            }

        }
        if (base64Encoding) {
            def r = report.toString()
            return r.bytes.encodeBase64().toString().getBytes()
        } else {
            return report.toString().getBytes()
        }


    }

    public static String getReportText(MessageAdapter oruMessage, boolean base64Encoding) {
        MessageAdapter<ORU_R01> message = oruMessage
        def repeatingGroup = message.PATIENT_RESULT;
        def group = repeatingGroup(0);
        def obsGroup = group.ORDER_OBSERVATION(0);
        StringBuilder report = new StringBuilder();

            for (int i = 0; i < obsGroup.getOBSERVATIONReps(); i++) {
                def obx5 = obsGroup.getOBSERVATION(i).OBX[5];
                if (obx5?.value != null) {
                    //def obx5Report = obx5.value.replaceAll("XD", "\n")
                    def obx5Report = obx5.value
                    // get rid of hl7 new line escpae  sequence \.br\
                    obx5Report = obx5Report.replaceAll("\\\\.br\\\\", "\n");
                    report.append(obx5Report + "\n");
                    //report.append(obx5?.value + "\n");
                }

             }
        if (base64Encoding) {
            def r = report.toString()
            return r.bytes.encodeBase64().toString()
        } else {
            return report.toString()
        }

    }
    /*
     used as a test method if all segments are accessible
     */


    public static String  parseMessage(MessageAdapter oruMessage) {

        MessageAdapter<ORU_R01> message = oruMessage
        //check access to MSH segment
        def msh = message.MSH
        def primitive = message.MSH[3][1]
        // check access to PID segment
        def pid = message.PID[3][1]
        LOG.debug("test pid access")
        // check access to EVN
        def eventTypeCode = message.EVN[1]
        LOG.debug("Event type code: " + eventTypeCode)
        // check access to PV1
        def patientIdClass = message.PV1[2]
        LOG.debug("Patient ID Class: " + patientIdClass)
        // check access to TXA
        def docType = message.TXA[2]
        def docPresentation = message.TXA[3]
        LOG.debug("Doc Type: " + docType + "/" + docPresentation)
        // check access to OBX segment
        def observationIdentifier = message.OBX[3].getIdentifier()
        LOG.debug("Observation Identifier: " + observationIdentifier)
        def observationCode = message.OBX[5][3] //.encode()
        def document = message.OBX[5][4]
        LOG.debug("found  " + observationCode + " document" )
        LOG.debug("document " + document)
        return  pid
    }


    private static String message = "MSH|^~\\&|GE3|GE4|GE5|GE6|20140301134529.78+0100||ACK^T01|280|P^T|2.4\r" +
                                    "MSA|AA|100\r"

    public static MessageAdapter<ACK> createAck(MessageAdapter<ORU_R01> mdmMessage) {
        MessageAdapter<ACK> ack = make(message);
        ack.MSH[3][1] = mdmMessage.MSH[5][1]
        ack.MSH[4][1] = mdmMessage.MSH[6][1]
        ack.MSH[5][1] = mdmMessage.MSH[3][1]
        ack.MSH[6][1] = mdmMessage.MSH[4][1]
        ack.MSH[7] = IdHelper.createUTCTime()
        ack.MSH[10] = IdHelper.createUniqueID()
        ack.MSA[2] = mdmMessage.MSH[10]
        ack

    }


    public static MessageAdapter<ACK> createNack(MessageAdapter<ORU_R01> mdmMessage, ArrayList<String> errorList) {

        // store the error list in a file
        String msa3 = errorList.each { it }
        MessageAdapter<ACK> ack = make(message);
        ack.MSH[3][1] = mdmMessage.MSH[5][1]
        ack.MSH[4][1] = mdmMessage.MSH[6][1]
        ack.MSH[5][1] = mdmMessage.MSH[3][1]
        ack.MSH[6][1] = mdmMessage.MSH[4][1]
        ack.MSH[7] = IdHelper.createUTCTime()
        ack.MSH[10] = IdHelper.createUniqueID()
        ack.MSA[1] = "AE"
        ack.MSA[2] = mdmMessage.MSH[10]
        ack.MSA[3] = msa3
        ack.ERR[1][4] = msa3
        ack
    }



    private static String genderLookup(sexCode) {
        def result
        switch (sexCode) {
            case ["M","m"]:
                result = "male"
                break
            case ["F","f"]:
                result = "female"
                break
            default:
                result = 'unknown'
                break
        }
        result
    }


    private static String orderModeLookup(mode) {
        def result
        switch (mode) {
            case ["F","f"]:
                result = "final"
                break
            case ["C","c"]:
                result = "addendum"
                break
            case ["P","p"]:
                result = "preliminary"
                break
            default:
                result = 'final'
                break
        }
        result
    }

    public static List<String> getOrderAsList(MessageAdapter oruMessage) {
        MessageAdapter<ORU_R01> message = oruMessage
        def repeatingGroup = message.PATIENT_RESULT;
        def group = repeatingGroup(0);
        def obsGroup = group.ORDER_OBSERVATION(0);
        StringBuilder report = new StringBuilder();
        ArrayList<String> reportList = new ArrayList<String>()
        for (int i = 0; i < obsGroup.getOBSERVATIONReps(); i++) {
         //   LOG.debug(i + " --> " + obsGroup.getOBSERVATIONReps() )
            def obx5 = obsGroup.getOBSERVATION(i).OBX[5];
            if (obx5?.value != null) {
                //def obx5Report = obx5.value.replaceAll("XD", "\n")
                def obx5Report = obx5.value
                // get rid of hl7 new line escpae  sequence \.br\
                //obx5Report = obx5Report.replaceAll("\\\\.br\\\\", "\n");
                String[] reportArray = obx5Report.split("\\\\.br\\\\")
           //     LOG.debug("--------------->" + reportArray.size())
                for (int j=0; j<reportArray.size(); j++){
                    reportList.add(reportArray[j])
                }
                //reportList.add(obx5Report);
                //report.append(obx5?.value + "\n");
            }

        }
        LOG.debug("ReportArray includes: " + reportList.size())
        return reportList
        }
        /*
        def report = message.ORDER.ORDER_DETAIL.OBR[13].value
        String r = report.toString()
        LOG.debug("Order: " + r)
        String[] reportArray = r.split("\\\\.br\\\\")
        LOG.debug("Reportarray includes: " + reportArray.size())
        return (reportArray as List<String>)
        */
    //}

}
