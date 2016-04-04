package com.ge.hc.oru

import groovy.xml.XmlUtil

/**
 * Created by 100026806 on 3/21/14.
 */
class CdaBuilder {


    public static String buildCDA(reportList, metaData, base64Encoding, encoding, forceB64encoding) {




        def params = [:]

        params."xmlns" = "urn:hl7-org:v3"
        params."xmlns:voc" = "urn:hl7-org:v3/voc"
        params."xmlns:xsi" = "http://www.w3.org/2001/XMLSchema-instance"
        params."xsi:schemaLocation" = "urn:hl7-org:v3 CDA.xsd"

        def markup = {
            ClinicalDocument(params) {
                typeId(extension: metaData.get("cdaTypeIdExtension"), root: metaData.get("cdaTypeId"))
                templateId(root: metaData.get("cdaTemplateId"), extension: metaData.get("cdaTemplateIdExtension"))
                id(extension: metaData.get("cdaIdExtension"), root: metaData.get("cdaIdRoot"))
                code(
                        code: metaData.get("cdaTitleCode"),
                        codeSystem: metaData.get("cdaTitleCodeSystem"),
                        codeSystemName: metaData.get("cdaTitleCodeSystemName"),
                        displayName: metaData.get("cdaTitleCodeDisplayName")
                )
                if (metaData.get("reportMode") == "preliminary")title(metaData.get("cdaReportTitlePreliminary"))
                if (metaData.get("reportMode") == "final")title(metaData.get("cdaReportTitleFinal"))
                if (metaData.get("reportMode") == "addendum")title(metaData.get("cdaReportTitleAddendum"))
                //effectiveTime(value: metaData.get("localEffectiveTime"))
                //effectiveTime(value: metaData.get("clinicalTime"))
                effectiveTime(value: metaData.get("localClinicalTime"))
                confidentialityCode(code: metaData.get("cdaConfidentialityCode"),
                        codeSystem: metaData.get("cdaConfidentialityCodeSystem"),
                        displayName: metaData.get("cdaConfidentialityCodeDisplayName") )
                languageCode(code: metaData.get("languageCode"))

                //recordTarget
                recordTarget(typeCode: "RCT", contextControlCode: "OP") {
                    patientRole {

                        id(root: metaData.get("sourcePatientDomainId"), extension: metaData.get("sourcePatientId") )
                        id(root: metaData.get("globalPatientOid"), extension: metaData.get("globalPatientId") )

                        addr {
                            streetAddressLine(metaData.get("sourcePatientAddress"))
                            city(metaData.get("sourcePatientAddressCity"))
                            if (metaData.get("sourcePatientAddressState")) {
                                state(metaData.get("sourcePatientAddressState"))
                            } else {
                                state(nullFlavor: "NI")
                            }

                            postalCode(metaData.get("sourcePatientAddressZipCode"))
                            if (metaData.get("country")) {
                                country(metaData.get("country"))
                            } else {
                                country(nullFlavor: "NI")
                            }

                        }

                        if (metaData.get("telephoneNumber") != null) {
                            telecom(value: "tel:" + metaData.get("telephoneNumber"))
                        }    else {
                            telecom(value: "tel:", nullFlavor: "UNK" )
                        }
                        patient {
                            name {
                            //    prefix(metaData.get("namePrefix", ""))
                                given(metaData.get("firstName", ""))
                              //  given(metaData.get("middleName", ""))
                                family(metaData.get("lastName", ""))
                            }

                            administrativeGenderCode(code: metaData.get("sourcePatientSexCode"), codeSystem: metaData.get("genderCodeSystem"), displayName: metaData.get("sourcePatientSexDisplayName"))
                            birthTime(value: metaData.get("sourcePatientBirthdate"))
                        }

                    }
                }

                // author
                author {
                    templateId(root: metaData.get("cdaAuthorTemplateId"))
                    //time(value: metaData.get("authorTime"))
                    time(value: metaData.get("localAuthorTime"))


                    assignedAuthor {
                        templateId(root: metaData.get("cdaAssignedAuthorTemplateId"))
                        id( root: metaData.get("cdaAuthorDomainId"), extension: metaData.get("cdaAuthorId"),)
                        addr(nullFlavor: "NI")
                        telecom(nullFlavor: "NI")
                        assignedPerson {
                            name {
                                if (metaData.get("authorGivenName")) {
                                    given(metaData.get("authorGivenName"))
                                } else {
                                    given(nullFlavor: "NI")
                                }
                                if (metaData.get("authorFamilyName")) {
                                    family(metaData.get("authorFamilyName"))
                                }
                                if (metaData.get("authorPrefix")) {
                                    prefix(metaData.get("authorPrefix"))
                                } else {
                                    prefix(nullFlavor: "NI")
                                }
                            }
                        }

                        representedOrganization {
                            //id(root: metaData.get("principalResultInterpreterFacilityId"), extension: metaData.get("principalResultInterpreterFacilityDomainId"))
                            //name(metaData.get("authorInstitutionName"))
                            id(root: metaData.get("cdaAssignedAuthorRepresentationOrganizationIdRoot"), extension: metaData.get("cdaAssignedAuthorRepresentationOrganizationIdExtension"))
                            if (metaData.get("cdaAuthorOrganizationName")) {
                                name(metaData.get("cdaAuthorOrganizationName"))
                            } else {
                                name(nullFlavor: "NI")
                            }
                            asOrganizationOf {
                                wholeOrganization {
                                    name(metaData.get("cdaWholeOrganization"))
                                }
                            }

                        }
                    }
                }

                //custodian
                /*
                custodian {
                    assignedCustodian {
                        representedCustodianOrganization {
                            id(root: metaData.get("principalResultInterpreterFacilityId") , extension: metaData.get("principalResultInterpreterFacilityDomainId"))
                            name(metaData.get("authorInstitutionName"))
                            addr(nullFlavor: "NI")
                        }
                    }
                } */
                custodian {
                    assignedCustodian {
                        representedCustodianOrganization {
                            //id(extension: metaData.get("cdaCustodianIdExtension"), root: metaData.get("cdaCustodianIdRoot"))
                            id(root: metaData.get("cdaAssignedAuthorRepresentationOrganizationIdRoot"), extension: metaData.get("cdaAssignedAuthorRepresentationOrganizationIdExtension"))
                            if (metaData.get("cdaAuthorOrganizationName")) {
                                name(metaData.get("cdaAuthorOrganizationName"))
                            } else {
                                name(nullFlavor: "NI")
                            }
                            addr {
                                country(nullFlavor: "NI")
                                city(nullFlavor: "NI")
                                postalCode(nullFlavor: "NI")
                                streetAddressLine(nullFlavor: "NI")
                            }
                        }
                    }
                }
                legalAuthenticator(typeCode: "LA") {
                    /*
                    if (metaData.get("effectiveTime")) {
                        time(value:metaData.get("effectiveTime"))
                    }   else {
                        time(nullFlavor: "NI")
                    } */
                    if (metaData.get("cdaLegalAuthenticatorTime")) {
                        time(value: metaData.get("cdaLegalAuthenticatorTime"))
                    } else {
                        time(nullFlavor: "NI")
                    }
                    signatureCode(code: "S")
                    assignedEntity(classCode: "ASSIGNED") {
                        //id(root: metaData.get("principalResultInterpreterFacilityId") , extension: metaData.get("principalResultInterpreterFacilityDomainId"))
                        id(root: metaData.get("cdaLegalAuthorDomainId"), extension: metaData.get("cdaLegalAuthorId"))
                        assignedPerson(classCode: "PSN", determinerCode: "INSTANCE") {
                            name {
                                if (metaData.get("cdaLegalAuthorGivenName")) {
                                    given(metaData.get("cdaLegalAuthorGivenName"))
                                } else {
                                    given(nullFlavor: "NI")
                                }
                                if (metaData.get("cdaLegalAuthorFamilyName")) {
                                    family(metaData.get("cdaLegalAuthorFamilyName"))
                                }
                                if (metaData.get("cdaLegalAuthorPrefix")) {
                                    prefix(metaData.get("cdaLegalAuthorPrefix"))
                                } else {
                                    prefix(nullFlavor: "NI")
                                }
                            }
                        }
                        representedOrganization {
                            //id(root: metaData.get("principalResultInterpreterFacilityId") , extension: metaData.get("principalResultInterpreterFacilityDomainId"))
                            //name(metaData.get("authorInstitutionName"))
                            id(root: metaData.get("cdaAssignedAuthorRepresentationOrganizationIdRoot"), extension: metaData.get("cdaAssignedAuthorRepresentationOrganizationIdExtension"))
                            if (metaData.get("cdaAuthorOrganizationName")) {
                                name(metaData.get("cdaAuthorOrganizationName"))
                            } else {
                                name(nullFlavor: "NI")
                            }
                        }
                    }
                }
                participant(typeCode: "REF") {
                    templateId(root: metaData.get("cdaParticipantTemplateId"))
                    if (metaData.get("participantTime")) {
                        time(value:metaData.get("participantTime"))
                    }   else {
                        time(nullFlavor: "NI")
                    }
                    associatedEntity(classCode: "PROV") {
                        id(root: metaData.get("cdaParticipantDomainId"), extension: metaData.get("cdaScopingOrganizationIdExtension"))
                        associatedPerson {
                            name {
                                if (metaData.get("participantGivenName")) {
                                    given(metaData.get("participantGivenName"))
                                } else {
                                    given(nullFlavor: "NI")
                                }
                                if (metaData.get("participantFamilyName")) {
                                    family(metaData.get("participantFamilyName"))
                                } else {
                                    family(nullFlavor: "NI")
                                }
                            }
                        }
                        scopingOrganization {
                            id(root: metaData.get("cdaScopingOrganizationIdRoot"), extension: metaData.get("cdaScopingOrganizationIdExtension"))
                            if (metaData.get("cdaOrderFacilityName")) {
                                name(metaData.get("cdaOrderFacilityName"))
                            } else {
                                name(nullFlavor: "NI")
                            }
                            if (metaData.get("cdaOrderFacilityPhone")) {
                                telecom(value: metaData.get("cdaOrderFacilityPhone"))
                            } else {
                                telecom(nullFlavor: "NI")
                            }
                            addr {
                                if (metaData.get("cdaOrderFacilityCountry")) {
                                    country(metaData.get("cdaOrderFacilityCountry"))
                                } else {
                                    country(nullFlavor: "NI")
                                }
                                if (metaData.get("cdaOrderFacilityCity")) {
                                    city(metaData.get("cdaOrderFacilityCity"))
                                } else {
                                    city(nullFlavor: "NI")
                                }
                                if (metaData.get("cdaOrderFacilityPostalCode")) {
                                    postalCode(metaData.get("cdaOrderFacilityPostalCode"))
                                } else {
                                    postalCode(nullFlavor: "NI")
                                }
                                if (metaData.get("cdaOrderFacilityStreet")) {
                                    streetAddressLine(metaData.get("cdaOrderFacilityStreet"))
                                } else {
                                    streetAddressLine(nullFlavor: "NI")
                                }
                            }

                        }
                    }
                }

                //inFulfillmentOf
                inFulfillmentOf {

                    order {
                        id(extension: metaData.get("accessionNumber"), root: metaData.get("accessionDomainId"))
                        id(extension: metaData.get("orderNumber"), root: metaData.get("referralDomainId"))

                    }
                }

                // documentationOf
                documentationOf {
                    serviceEvent(classCode: "ACT") {
                        code(code: metaData.get("examinationCode"), displayName: metaData.get("examinationName"), codeSystem: metaData.get("performerCodeSystem"))
                        //effectiveTime(value: metaData.get("localEffectiveTime"))
                        effectiveTime {
                            //low(value: metaData.get("serviceStartTime"))
                            //high(value: metaData.get("serviceStopTime"))
                            low(value: metaData.get("localServiceStartTime"))
                            high(value: metaData.get("localServiceStopTime"))
                        }
                        performer(typeCode: metaData.get("perfomerTypeCode")) {
                            templateId(root: metaData.get("performerTemplateId"))
                            if (metaData.get("cdaLocalPerformerTime")) {
                                time(value: metaData.get("cdaLocalPerformerTime"))
                            } else {
                                time(nullFlavor: "NI")
                            }
                            assignedEntity {
                                //id( root: metaData.get("authorDomainId"), extension: metaData.get("authorId"),)
                                id(root: metaData.get("cdaPerformerAuthorIdRoot"), extension: metaData.get("cdaPerformerAuthorIdExtension"))
                                assignedPerson {
                                    name {
                                        if (metaData.get("authorGivenName")) {
                                            given(metaData.get("authorGivenName"))
                                        } else {
                                            given(nullFlavor: "NI")
                                        }
                                        family(metaData.get("authorFamilyName"))
                                        if (metaData.get("authorPrefix")) {
                                            prefix(metaData.get("authorPrefix"))
                                        } else {
                                            prefix(nullFlavor: "NI")
                                        }
                                    }
                                }

                            }

                        }

                    }
                }

                // report
                /*
                component {
                    nonXMLBody {
                        if (base64Encoding) {
                            text(mediaType: "text/plain", charSet: encoding, representation: "B64", report.toString())
                        } else {
                            if (forceB64encoding) {
                                text(mediaType: "text/plain", charSet: encoding, representation: "B64", report.toString())
                            } else {
                                text(mediaType: "text/plain", charSet: encoding, report.toString())
                            }
                        }
                    }
                } */
                component {
                    structuredBody {
                        component {
                            section {
                                templateId(root:metaData.get("cdaComponentTemplateId"))
                                if (metaData.get("cdaDangerCode")) dangerCode(metaData.get("cdaDangerCode"))
                                code(code:metaData.get("cdaComponentCode"), codeSystem:metaData.get("cdaComponentCodeSystem"), codeSystemName:metaData.get("cdaComponentCodeSystemName"), displayName:metaData.get("cdaComponentCodeDisplayName"))
                                if (metaData.get("reportTitle")) title(metaData.get("reportTitle"))
                                text {
                                    reportList.each { reportLine ->
                                        paragraph(reportLine)
                                    }
                                    paragraph("  ")
                                }
                            }
                        }
                    }
                }

            }
        }
        def builder = new groovy.xml.StreamingMarkupBuilder();
        def cda = builder.bind(markup)
        def prettycda = XmlUtil.serialize(cda).replaceAll("UTF-8", encoding)
    }
}
