package com.ge.hc.oru

import com.ge.hc.oru.orutocda.CdaPropertyBuilder
import com.ge.hc.oru.orutoxds.PropertyBuilder
import com.ge.hc.oru.pix.PixMapper
import com.ge.hc.oru.pix.PixQueryBuilder
import com.ge.hc.oru.util.IdHelper
import com.ge.hc.oru.util.JaxbUtils
import com.ge.hc.oru.util.ReferenceId
import com.ge.hc.oru.xds.EbxmlTranslator
import com.ge.hc.oru.xds.MetadataToIti18Translator
import com.sun.java.swing.plaf.windows.TMSchema
import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.spring.SpringRouteBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openehealth.ipf.commons.core.modules.api.ValidationException
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.ProvideAndRegisterDocumentSetRequestType
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry
import org.openehealth.ipf.commons.ihe.xds.core.responses.Status
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rs.RegistryError
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rs.RegistryResponseType
import org.openehealth.ipf.modules.hl7dsl.MessageAdapter
import org.openehealth.ipf.platform.camel.core.util.Exchanges
import org.openehealth.ipf.platform.camel.ihe.xds.XdsCamelValidators

class OruRouteBuilder extends SpringRouteBuilder {

    private static final transient Log LOG = LogFactory.getLog(OruRouteBuilder.class)
    //private static final PropertyBuilder properties;
    private static final MetadataToIti18Translator METADATA_TO_ITI18_TRANSLATOR = new  MetadataToIti18Translator()
    private static final EbxmlTranslator EBXML_TRANSLATOR = new EbxmlTranslator()
   // String iti41EndpointUri;
    //private MessageAdapter <ACK> acknowledge

    PixConfiguration pixConfig;
    String iti9EndPointUri = "pix-iti9://localhost:8411?audit=false"


    void configure() {

        from("mina2:tcp://${OruConfiguration.inputUrl}?sync=true&codec=#hl7codec")
            .process {LOG.info("message enters mina endpoint ...")}
            .to("log:org.apache.camel.howto?showAll=true&multiline=true")
            .setHeader("ORIGINAL.MESSAGE", body())
            //.setProperty("pixEnabled", pixConfig.enablePix)
            .to("seda:start")

        from("seda:start")
            .process { LOG.info(" message enters seda endpoint ...")}
            .to("direct:drr")
            .process{ LOG.info("message left drr and acknowledge is started ...")}
            .to("log:org.apache.camel.howto?showAll=true&multiline=true")
            .to("direct:acknowledge")

        // working on the hl7 stuff

        from("direct:drr")
                .onException(Exception.class)
                .maximumRedeliveries(0)
                .handled(true)
                .process {
                    def hl7error = Exchanges.extractException(it, false).printStackTrace()
                    LOG.error(hl7error)
                    LOG.error("Exception Message: " + Exchanges.extractException(it, false).message)
                    it.setProperty("hl7Exception", hl7error)
            }
            .nak("hl7 processing error for details look into IPF logfile ")
                .end()
             .process { LOG.info("message enters direct:drr endpoint ...")

             }
             .onException(ValidationException.class).to("direct:error").end()
            .unmarshal().ghl7()
            .to("log:org.apache.camel.howto?showAll=true&multiline=true")
                .process {
                    // we need the original message for debuging and acknowledge
                    it.setProperty('oru', it.in.body)
                    String oruMesssage = it.getProperty("oru")
                    LOG.debug("new message received : \n"+ oruMesssage.replaceAll("\r", "\r\n"))
                    MessageAdapter messageAdapter = it.in.body
                    HashMap map = OruAdapter.extractData(messageAdapter, OruConfiguration.timezoneId)
                    ArrayList<ReferenceId> referenceIdList = new ArrayList<ReferenceId>()
                    if (map.get("orderNumber") !=null) {
                        referenceIdList.add(new ReferenceId(id:map.get("orderNumber"), domain:OruConfiguration.orderNumberDomain, urn:OruConfiguration.orderNumberUrn))
                    }
                    if (map.get("accessionNumber") !=null) {
                        referenceIdList.add(new ReferenceId(id:map.get("accessionNumber"), domain:OruConfiguration.accessionNumberDomain, urn:OruConfiguration.accessionNumberUrn))
                    }
                    map.put("referenceIdList", referenceIdList)
                    OruAdapter.sortedPrintMap(map, "HL7 message")
                    it.setProperty("metaData", map)
            } .to("direct:drr-xds-code")


        // enrich with the static XDS codes

        from("direct:drr-xds-code")
                .onException(NullPointerException).to("direct:error").end()
                .process { LOG.info("message enters direct:xds-code endpoint ...") }
                .to("log:org.apache.camel.howto?showAll=true&multiline=true")
                .process {
                    HashMap metaData = it.getProperty("metaData")
                    metaData.put("title", PropertyBuilder.title)
                    metaData.put("xdsSubmissionSet.sourceID", PropertyBuilder.sourceId)
                    metaData.put("languageCode", PropertyBuilder.languageCode)
                    metaData.put("documentTitle", PropertyBuilder.documentTitle)
                    metaData.put("confidentialityCodeDisplayName",PropertyBuilder.confidentialityCodeDisplayName)
                    metaData.put("confidentialityCodeNodeRepresentation",PropertyBuilder.confidentialityCodeNodeRepresentation)
                    metaData.put("confidentialityCodeCodingScheme",PropertyBuilder.confidentialityCodeCodingScheme)
                    if (metaData.get("healthcareFacilityCodeDisplayName") == null) metaData.put("healthcareFacilityCodeDisplayName",PropertyBuilder.healthcareFacilityCodeDisplayName)
                    if (metaData.get("healthcareFacilityCodeNodeRepresentation") == null) metaData.put("healthcareFacilityCodeNodeRepresentation",PropertyBuilder.healthcareFacilityCodeNodeRepresentation)
                    if (metaData.get("healthcareFacilityCodeCodingScheme") == null) metaData.put("healthcareFacilityCodeCodingScheme",PropertyBuilder.healthcareFacilityCodeCodingScheme)
                    metaData.put("practiceCodeDisplayName",PropertyBuilder.practiceCodeDisplayName)
                    metaData.put("practiceCodeNodeRepresentation",PropertyBuilder.practiceCodeNodeRepresentation)
                    metaData.put("practiceCodeCodingScheme",PropertyBuilder.practiceCodeCodingScheme)
                    metaData.put("classCodeDisplayName", PropertyBuilder.classCodeDisplayName)
                    metaData.put("classCodeNodeRepresentation",PropertyBuilder.classCodeNodeRepresentation)
                    metaData.put("classCodeCodingScheme",PropertyBuilder.classCodeCodingScheme)
                    metaData.put("preliminaryClassCodeDisplayName", PropertyBuilder.preliminaryClassCodeDisplayName)
                    metaData.put("preliminaryClassCodeNodeRepresentation",PropertyBuilder.preliminaryClassCodeNodeRepresentation)
                    metaData.put("preliminaryClassCodeCodingScheme",PropertyBuilder.preliminaryClassCodeCodingScheme)
                    metaData.put("formatCodeDisplayName",PropertyBuilder.formatCodeDisplayName)
                    metaData.put("formatCodeNodeRepresentation",PropertyBuilder.formatCodeNodeRepresentation)
                    metaData.put("formatCodeCodingScheme",PropertyBuilder.formatCodeCodingScheme)
                    metaData.put("preliminaryFormatCodeDisplayName",PropertyBuilder.preliminaryFormatCodeDisplayName)
                    metaData.put("preliminaryFormatCodeNodeRepresentation",PropertyBuilder.preliminaryFormatCodeNodeRepresentation)
                    metaData.put("preliminaryFormatCodeCodingScheme",PropertyBuilder.preliminaryFormatCodeCodingScheme)
                    metaData.put("typeCodeDisplayName",PropertyBuilder.typeCodeDisplayName)
                    metaData.put("typeCodeNodeRepresentation",PropertyBuilder.typeCodeNodeRepresentation)
                    metaData.put("typeCodeCodingScheme",PropertyBuilder.typeCodeCodingScheme)
                    metaData.put("contentDisplayName", PropertyBuilder.contentCodeDisplayName)
                    metaData.put("contentNodeRepresentation", PropertyBuilder.contentCodeNodeRepresentation)
                    metaData.put("contentCodeCodingScheme", PropertyBuilder.contentCodeCodingScheme)
                    metaData.put("cdaTemplateId", CdaPropertyBuilder.templateId)
                    metaData.put("cdaTemplateIdExtension", CdaPropertyBuilder.cdaTemplateIdExtension)
                    metaData.put("cdaTitleCode", CdaPropertyBuilder.titleCode)
                    metaData.put("cdaTitleCodeSystem", CdaPropertyBuilder.titleCodeSystem)
                    metaData.put("cdaTitleCodeSystemName", CdaPropertyBuilder.titleCodeSystemName)
                    metaData.put("cdaTitleCodeDisplayName", CdaPropertyBuilder.titleCodeDisplayName)
                    metaData.put("cdaConfidentialityCode", CdaPropertyBuilder.confidentialityCode)
                    metaData.put("cdaConfidentialityCodeSystem", CdaPropertyBuilder.confidentialityCodeSystem)
                    metaData.put("cdaConfidentialityCodeDisplayName", CdaPropertyBuilder.confidentialityCodeDisplayName)
                    metaData.put("genderCodeSystem", CdaPropertyBuilder.genderCodeSystem)
                   // metaData.put("authorTemplateId", CdaPropertyBuilder.authorTemplateId)
                    if (metaData.get("principalResultInterpreterFacilityId") == null) metaData.put("principalResultInterpreterFacilityId", CdaPropertyBuilder.principalResultInterpreterFacilityId)
                    if (metaData.get("principalResultInterpreterFacilityDomainId") == null) metaData.put("principalResultInterpreterFacilityDomainId", CdaPropertyBuilder.principalResultInterpreterFacilityDomainId)
                    metaData.put("cdaParticipantDomainId", CdaPropertyBuilder.cdaParticipantDomainId)
                    metaData.put("accessionDomainId", CdaPropertyBuilder.accessionDomainId)
                    metaData.put("referralDomainId", CdaPropertyBuilder.referralDomainId)
                    metaData.put("performerCodeSystem",CdaPropertyBuilder.cdaPerformerCodeSystem)
                    metaData.put("perfomerTypeCode", CdaPropertyBuilder.cdaPerfomerTypeCode)
                    metaData.put("performerTemplateId", CdaPropertyBuilder.cdaPerformerTemplateId)
                    metaData.put("authorRole", PropertyBuilder.authorRole)
                    metaData.put("authorSpecialty", PropertyBuilder.authorSpecialty)
                    if (metaData.get("authorInstitutionName")== null) metaData.put("authorInstitutionName", PropertyBuilder.authorInstitution)
                    metaData.put("cdaTypeId", CdaPropertyBuilder.cdaTypeId)
                    metaData.put("cdaTypeIdExtension",CdaPropertyBuilder.cdaTypeIdExtension)
                    metaData.put("cdaIdRoot", CdaPropertyBuilder.cdaIdRoot);
                    metaData.put("cdaIdExtension", CdaPropertyBuilder.cdaIdExtension)
                    metaData.put("cdaReportTitlePreliminary", CdaPropertyBuilder.cdaReportTitlePreliminary)
                    metaData.put("cdaReportTitleFinal", CdaPropertyBuilder.cdaReportTitleFinal)
                    metaData.put("cdaReportTitleAddendum", CdaPropertyBuilder.cdaReportTitleAddendum)
                    metaData.put("cdaAuthorTemplateId", CdaPropertyBuilder.cdaAuthorTemplateId)
                    metaData.put("cdaAssignedAuthorTemplateId",CdaPropertyBuilder.cdaAssignedAuthorTemplateId)
                    if (metaData.get("cdaAuthorDomainId") == null) metaData.put("cdaAuthorDomainId", CdaPropertyBuilder.cdaAuthorDomainId)
                    if (metaData.get("cdaAuthorId") == null) metaData.put("cdaAuthorId", CdaPropertyBuilder.cdaAuthorId)
                    if (metaData.get("cdaAssignedAuthorRepresentationOrganizationIdRoot") == null) metaData.put("cdaAssignedAuthorRepresentationOrganizationIdRoot", CdaPropertyBuilder.cdaAssignedAuthorRepresentationOrganizationIdRoot)
                    if (metaData.get("cdaAssignedAuthorRepresentationOrganizationIdExtension") == null) metaData.put("cdaAssignedAuthorRepresentationOrganizationIdExtension", CdaPropertyBuilder.cdaAssignedAuthorRepresentationOrganizationIdExtension)
                    if (metaData.get("cdaAuthorOrganizationName") == null) metaData.put("cdaAuthorOrganizationName", CdaPropertyBuilder.cdaAuthorOrganizationName)
                    metaData.put("cdaWholeOrganization", CdaPropertyBuilder.cdaWholeOrganization)
                    metaData.put("cdaParticipantTemplateId", CdaPropertyBuilder.cdaParticipantTemplateId)
                    metaData.put("cdaScopingOrganizationIdRoot", CdaPropertyBuilder.cdaScopingOrganizationIdRoot)
                    if (metaData.get("cdaScopingOrganizationIdExtension") == null) metaData.put("cdaScopingOrganizationIdExtension", CdaPropertyBuilder.cdaScopingOrganizationIdExtension)
                    if (metaData.get("cdaOrderFacilityName") == null) metaData.put("cdaOrderFacilityName", CdaPropertyBuilder.cdaOrderFacilityName)
                    if (metaData.get("cdaPerformerAuthorIdRoot")==null) metaData.put("cdaPerformerAuthorIdRoot", CdaPropertyBuilder.cdaPerformerAuthorIdRoot)
                    if (metaData.get("cdaPerformerAuthorIdExtension")==null) metaData.put("cdaPerformerAuthorIdExtension", CdaPropertyBuilder.cdaPerformerAuthorIdExtension)
                    metaData.put("cdaComponentTemplateId", CdaPropertyBuilder.cdaComponentTemplateId)
                    metaData.put("cdaComponentCode", CdaPropertyBuilder.cdaComponentCode)
                    metaData.put("cdaComponentCodeSystem", CdaPropertyBuilder.cdaComponentCodeSystem)
                    metaData.put("cdaComponentCodeSystemName", CdaPropertyBuilder.cdaComponentCodeSystemName)
                    metaData.put("cdaComponentCodeDisplayName", CdaPropertyBuilder.cdaComponentCodeDisplayName)
                    metaData.put("cdaLegalAuthenticatorTime", metaData.get("localClinicalTime", null))
                    OruAdapter.sortedPrintMap(metaData, "HL7 message + PropertyBuilder")
                    //throw (new NullPointerException())
                }
                .to("direct:pixRoute")

         // enrich with the global patientId  here the same as the local

        from("direct:pixRoute")
            .process {
                it.setProperty("pixEnabled", pixConfig.enablePix);
                LOG.info("routing decision:" + it.getProperty("pixEnabled"))
            }
            .choice()
                .when(property("pixEnabled").isEqualTo("true"))
                    .to("direct:pix-query-builder")
                .otherwise()
                    .to("direct:pix-query-fake")


        from("direct:pix-query-builder")
                .process {
                    LOG.info("we need to create a pix query message")
                    String pixQueryId = pixConfig.getPixQueryId_1() + " " + pixConfig.getPixQueryId_2() + " " + pixConfig.getPixQueryId_3()
                    def meta = it.getProperty("metaData")
                    String pixQuery = PixQueryBuilder.createQBP(meta, pixConfig)
                    LOG.debug("Query global patient id for local id: " + meta.get("sourcePatientId"))
                    LOG.debug("Query global patient id for assigning authority name: " + meta.get("sourcePatientIdLocalNameSpace"))
                    LOG.debug("Query global patient id for assigning authority Id: " + meta.get("sourcePatientIdAssigningOid"))
                    LOG.debug(pixQuery.replaceAll("\r","\r\n"))
                    it.in.body = pixQuery
        }
                .to("direct:pixQuery")


        from('direct:pixQuery')
                //.to(iti9EndPointUri)
                .to("pix-iti9://" + pixConfig.getPixManagerInfo())
                .process {
                    String pixResponse = it.in.body
                    LOG.debug(pixResponse.replaceAll("\r","\r\n"))
                    HashMap metaData = it.getProperty("metaData")
                    metaData.put("globalPatientId", PixMapper.setGlobalPatientId(pixResponse))
                    metaData.put("globalPatientOid", PixMapper.setGlobalPatientOid(pixResponse))
                    metaData.put("globalPatientNameSpace", PixMapper.setGlobalPatientNameSpace(pixResponse))
        }
        .to('direct:add-meta-data')


        from("direct:pix-query-fake")
                .process { LOG.info("message enters direct:pix-query-fake endpoint ...") }
                .to("log:org.apache.camel.howto?showAll=true&multiline=true")
                .process {
                    HashMap metaData = it.getProperty("metaData")
                    metaData.put("globalPatientId", metaData.get("sourcePatientId"))
                    metaData.put("globalPatientOid", metaData.get("sourcePatientDomainId"))
                    metaData.put("globalPatientNameSpace", metaData.get("sourcePatientDomainUniversalIdType"))
                }
            .to("direct:add-meta-data")



        from("direct:add-meta-data")
           .process {
                LOG.info("message enters direct:add-meta-data endpoint ...")
                HashMap metaData = it.getProperty("metaData")
                // we need the repository unique id
                metaData.put("repositoryUniqueId", OruConfiguration.repositoryUniqueId)
               // we need the submission source id    check if i longer than 36 character
               if (OruConfiguration.submissionsSourceId.length() > 36)
                 log.warn("submisionSourceId might be too long " + OruConfiguration.submissionsSourceId.length() +  " since it is part of the unique ID generation the max length of unique ids (64) characters might be execceeded ")
               metaData.put("submissionsSourceId", OruConfiguration.submissionsSourceId)
               String numericAccessionNumber = IdHelper.removePrefixFromAccessionNumber(metaData.get("accessionNumber"))
               if (OruConfiguration.enableUniqueIdPrefix) {
                  LOG.debug("XDS Documenten id get a Prefix: " + OruConfiguration.uniqueIdPrefix)
                  metaData.put("xdsDocumentUniqueId", IdHelper.createUniqueId(PropertyBuilder.submissionsSourceId, numericAccessionNumber, OruConfiguration.uniqueIdPrefix.toString()))
               }  else {
                  LOG.debug("XDS DocuementId did not get a Prefix")
                  metaData.put("xdsDocumentUniqueId", IdHelper.createUniqueId(PropertyBuilder.submissionsSourceId, numericAccessionNumber))
               }
               // metaData.put("xdsDocumentUniqueId", IdHelper.createUniqueId(PropertyBuilder.submissionsSourceId, numericAccessionNumber))
               metaData.put("xdsSubmissionUniqueId", IdHelper.createSubmissionId(metaData.get("xdsDocumentUniqueId")))
               //metaData.put("xdsUniqueSubmissionId", PropertyBuilder.submissionsSourceId + ".0." + metaData.get("uniqueId"))
               OruAdapter.sortedPrintMap(metaData, "HL7 + PropertyBuilder + PIX Fake");
            }

            .to("direct:drr-iti18-request")


        from("direct:drr-iti18-request")
                .onException(Exception.class)
                .handled(true)
                .nak("ITI 18 request failed")
                .end()
                .process { LOG.info("message enters direct:drr-iti18-request endpoint ...") }
                .to("log:org.apache.camel.howto?showAll=true&multiline=true")
                .process {
                     LOG.debug("######################## create ITI18 request ##############################")
                     HashMap metaData = it.getProperty("metaData")
                     QueryRegistry iti18Request = METADATA_TO_ITI18_TRANSLATOR.convert(metaData)
                     it.in.body = iti18Request
                }
                .output("XDS ITI-18 Request"){ JaxbUtils.marshal(it) }
                .process(XdsCamelValidators.iti18RequestValidator())
                .to("${OruConfiguration.iti18Endpoint}")
                .output("XDS ITI-18 Response") { JaxbUtils.marshal(it) }
                .process(XdsCamelValidators.iti18ResponseValidator())

                .process {
                    RegistryResponseType response = it.in.body
                    LOG.debug("ITI-18 status: ${response.status} " + ":" +  Status.SUCCESS.getOpcode30())
                    if (response.status == Status.SUCCESS.getOpcode30()) {
                        it.setProperty('ack', 'ack')
                        HashMap metaData = it.getProperty("metaData")
                        //def numericAccesionNumber = IdHelper.removePrefixFromAccessionNumber(metaData.get("accessionNumber"))
                        String numericAccessionNumber;
                        if (OruConfiguration.enableUniqueIdPrefix) {
                            numericAccessionNumber = OruConfiguration.uniqueIdPrefix + "." + IdHelper.removePrefixFromAccessionNumber((String)  metaData.get("accessionNumber"))
                        } else {
                            numericAccessionNumber = IdHelper.removePrefixFromAccessionNumber((String) metaData.get("accessionNumber"))
                        }
                        LOG.debug("check if document this ends with ${numericAccessionNumber} already exists")
                        HashMap submission = EBXML_TRANSLATOR.getSubmissionMode(response, numericAccessionNumber,
                                metaData.get("examinationCode"),
                                metaData.get("reportMode"),
                                metaData.get("formatCodeNodeRepresentation"))
                        if (EBXML_TRANSLATOR.checkTimestamp(submission, metaData.get("creationTime"))) {
                            it.setProperty('ack', 'nak')
                            if (submission.get("mode") == "INVALID") {
                                it.in.body = ['not possible to register a preliminary document if a final one is already registered']
                            } else {
                                it.in.body = ['document not send, since a newer document is already available']
                            }
                            it.setProperty("errorList", it.in.body)
                        }
                        metaData.put("submissionMode", submission)
                        it.in.body = it.getProperty("oru")
                    } else {
                        LOG.warn("iti-18 was not ok")
                        it.in.body = []
                        for (RegistryError error : response.registryErrorList?.registryError) {
                            it.in.body << createXdsErrorMessage(error)
                        }
                        if (it.in.body.empty) {
                            it.in.body = ['XDS transaction failed for unknown reason']
                        }
                        it.setProperty('ack', 'nak')
                        it.setProperty("errorList", it.in.body)
                        it.in.body = it.getProperty("oru")
                    }
                    LOG.debug("ITI-18 response caused: " + it.getProperty('ack'))
                }
                .choice()
                    .when(header("ack").isEqualTo("ack"))
                    .to("direct:cda")

        from("direct:cda")
                .process { LOG.info("message enters direct:binary-file-content endpoint ...") }
                .to("log:org.apache.camel.howto?showAll=true&multiline=true")
                .process {
                    MessageAdapter oru = it.in.body
                    String reportText = OruAdapter.getReportText(oru, OruConfiguration.cdaBase64Encoding)
                    ArrayList<String> reportList = OruAdapter.getOrderAsList(oru);
                    HashMap metaData = it.getProperty("metaData")
                    //LOG.debug("ReportText: \n " + reportText)
                    String cda = CdaBuilder.buildCDA(reportList, metaData, OruConfiguration.cdaBase64Encoding, OruConfiguration.cdaEncoding, OruConfiguration.cdaForceB64Representation)
                    it.setProperty("document", cda)
                    it.in.body = cda
                }
                .output("CDA document", null)
                .to("direct:drr-make-iti41")

        from("direct:drr-make-iti41")
                .process { LOG.info("message enters direct:drr-make-iti-41 endpoint ...") }
                .to("log:org.apache.camel.howto?showAll=true&multiline=true")
                .process {
                    String document = it.getProperty("document")
                    HashMap metaData = it.getProperty("metaData")
                    ProvideAndRegisterDocumentSetRequestType iti41Request = EBXML_TRANSLATOR.convert(document, metaData, OruConfiguration.cdaEncoding)
                    it.in.body = iti41Request
                }
                .output('XDS ITI-41 request') { JaxbUtils.marshal(it) }
                //.process(XdsCamelValidators.iti41RequestValidator())
              //  .to("log:org.apache.camel.howto?showAll=true&multiline=true")
                .to("direct:drr-send-iti41")

        from("direct:drr-send-iti41")
                .onException(Exception.class)
                .handled(true)
                .nak("ITI 41 request failed")
                .end()
                .process { LOG.info("message enters direct:drr-send-iti41 endpoint ...") }
                .process(XdsCamelValidators.iti41RequestValidator())
                .to("log:org.apache.camel.howto?showAll=true&multiline=true")
                .to("${OruConfiguration.iti41Endpoint}")
                .output('XDS ITI-41 response') { JaxbUtils.marshal(it) }
                //.validate().iti41Response()
                .process(XdsCamelValidators.iti41ResponseValidator())
                .process {
                     it.setProperty('ack', "nak")
                     RegistryResponseType response = it.in.body
                     it.in.body = []
                     if (response.status != Status.SUCCESS.getOpcode30()) {
                        for (RegistryError error : response.registryErrorList?.registryError) {
                            it.in.body << createXdsErrorMessage(error)
                            }
                        if (it.in.body.empty) {
                            it.in.body = ['XDS transaction failed for unknown reason']
                        }
                        //println "++++++++++++++++++++++++++++++++++++++++++++send negative ack"
                        it.setProperty("ack", "nak")
                        ArrayList<String> xdsErrorList = it.in.body
                        it.setProperty("errorList",xdsErrorList)
                      } else {
                        //println "++++++++++++++++++++++++++++++++++++++++++++send positive ack"
                        it.setProperty("ack", "ack")
                     }
                    }
        .to("direct:store")



        // just save the hl7n message in the filesystem

        from("direct:store")
            .process { LOG.info("message enters direct:store endpoint ...") }
            .to("log:org.apache.camel.howto?showAll=true&multiline=true")
            .process {  LOG.debug("message entered direct:store endpoint")
                        it.in.body = it.getProperty("oru") }
            .setHeader(Exchange.FILE_NAME) { exchange ->               // set filename header to
                exchange.in.body.MSH[10].value + '.hl7'                // sending facility (MSH[4])
            }
            .marshal().ghl7()
            .to("file:destination")


        // ack a bit old fashion but will be pimped up soon
        from("direct:acknowledge")
            .process { LOG.info("message enters direct:acknowledge endpoint ...") }
            .to("log:org.apache.camel.howto?showAll=true&multiline=true")
                .choice()
                .when(header("ack").isEqualTo("ack"))
                    .process {
                        def acknowledge = OruAdapter.createAck(it.getProperty("oru"))
                        it.out.body = acknowledge.toString()
                        LOG.debug(acknowledge.toString().replaceAll("\r","\r\n"))
                    }
                 .otherwise()
                    .process {
                         def acknowledge = OruAdapter.createNack(it.getProperty("oru"), it.getProperty("errorList"))
                         it.out.body = acknowledge.toString()
                         LOG.debug(acknowledge.toString().replaceAll("\r","\r\n"))
                     }

        from("direct:error")
                .process { LOG.info("message enters direct:error endpoint ...") }
                .to("log:org.apache.camel.howto?showAll=true&multiline=true")
                .process {
                    it.setProperty("ack", "ack")
                    //def acknowledge = MdmAdapter.createAck(it.getProperty("oru"))
                    //it.out.body = acknowledge.toString()
                    //LOG.debug(acknowledge.toString().replaceAll("\r","\r\n"))
                }
        //.to("direct:acknowledge")
    }

    /**
     * Creates a string representation of an XDS registry error.
     */
    static String createXdsErrorMessage(RegistryError error) {
        StringBuilder sb = new StringBuilder()
                .append(error.severity.substring(error.severity.lastIndexOf(':') + 1))
                .append(' ')
                .append(error.errorCode)

        if (error.codeContext) {
            sb.append(', context=').append(error.codeContext)
        }
        if (error.location) {
            sb.append(', location=').append(error.location)
        }
        LOG.debug("Reported errors: " + sb.toString())
        return sb.toString()
    }

}
