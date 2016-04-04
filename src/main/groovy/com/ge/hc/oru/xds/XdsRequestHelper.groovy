package com.ge.hc.oru.xds

import com.ge.hc.oru.util.ReferenceId
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.ProvideAndRegisterDocumentSetRequestType
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Vocabulary
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.ObjectFactory
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.lcm.SubmitObjectsRequest
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.AssociationType1
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.ClassificationType
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.ExternalIdentifierType
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.ExtrinsicObjectType
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.IdentifiableType
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.InternationalStringType
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.ObjectRefType
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.RegistryObjectListType
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.RegistryPackageType
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.SlotType1
import sun.misc.BASE64Decoder

import javax.activation.DataHandler
import javax.activation.DataSource
import javax.mail.util.ByteArrayDataSource
import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName


/**
 * Created by 100026806 on 2/26/14.
 */
class XdsRequestHelper {

    private ObjectFactory rimFactory = new ObjectFactory();
    private static Log log = LogFactory.getLog(XdsRequestHelper.class)
    private static String RIM_URN = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
   // private static InternationalStringType strPhysical = Ebrs30Helper.createInternationalString("Physical");
   // private static InternationalStringType strDescription = Ebrs30Helper.createInternationalString("Annual physical");




    public SubmitObjectsRequest createSubmitObjectsRequest(String comment, HashMap mBean) {

        //Creating Extrinsic object also called document entry
        ExtrinsicObjectType extrinsicObjectType = createExtrinsicObject(mBean);
        JAXBElement<IdentifiableType> extrinsicElement = new JAXBElement<IdentifiableType>(
                new QName(RIM_URN, "ExtrinsicObject", "rim"),
                (Class) ExtrinsicObjectType.class,
                extrinsicObjectType);

        log.debug("XdsRequestHelper>ExtrinsicObjectType:" + extrinsicObjectType.objectType)

        //Creating Submission Set
        RegistryPackageType registryPackageType = createSubmissionSet(mBean);
        JAXBElement<IdentifiableType> submissionsetElement = new JAXBElement<IdentifiableType>(
                new QName(RIM_URN, "RegistryPackage", "rim"),
                (Class) RegistryPackageType.class,
                registryPackageType);

        log.debug("XdsRequestHelper>RegistryPackageType:" + registryPackageType.name)


        //Creating Classification node (Submission Set)
        ClassificationType subsetClassificationType =
                Ebrs30Helper.createClassification("SubmissionSet01", Vocabulary.SUBMISSION_SET_CLASS_NODE);
        String uuidSubmissionSet = 'urn:uuid:' + UUID.randomUUID()
        subsetClassificationType.setId(uuidSubmissionSet)

        log.debug("XdsRequestHelper>ClassificationType:" + subsetClassificationType.id)

        JAXBElement<IdentifiableType> classificationElement = new JAXBElement<IdentifiableType>(
                new QName(RIM_URN, "Classification", "rim"),
                (Class) ClassificationType.class,
                subsetClassificationType);

        log.debug("XdsRequestHelper>classificationElement:" + classificationElement)

        //Creating Association type (Submission Set 'hasmember' Document)
        AssociationType1 associationType = createAssociation("SubmissionSet01", "Document01");
        //associationType.setAssociationType("urn:ihe:iti:2007:AssociationType:RPLC")
        String uuidAssociation = 'urn:uuid:' + UUID.randomUUID()
        associationType.setId(uuidAssociation)


        log.debug("XdsRequestHelper>associationType:" + associationType.id)

        JAXBElement<IdentifiableType> associationElement = new JAXBElement<IdentifiableType>(
                new QName(RIM_URN, "Association", "rim"),
                (Class) AssociationType1.class,
                associationType);


        List<JAXBElement> objectRefTypeList = createObjectRefType();

        //Adding the elements to the RegistryObjectList
        RegistryObjectListType registryObjectListType = new RegistryObjectListType();
        objectRefTypeList.each {
            registryObjectListType.getIdentifiable().add(it)
        }
        //registryObjectListType.getIdentifiable().add(objectRefElement)
        registryObjectListType.getIdentifiable().add(submissionsetElement);
        registryObjectListType.getIdentifiable().add(extrinsicElement);
        //registryObjectListType.getIdentifiable().add(submissionsetElement);
        registryObjectListType.getIdentifiable().add(classificationElement);
        registryObjectListType.getIdentifiable().add(associationElement);
        HashMap submission = mBean.get("submissionMode")
        String submissionMode = submission.get("mode", "ORIGINAL")
        if (submissionMode.equalsIgnoreCase("replace")) {
            log.debug("Document must be replaced")
            AssociationType1 associationTypeReplace = createAssociationRPLC("Document01", submission.get("UUID"));
            associationTypeReplace.setAssociationType("urn:ihe:iti:2007:AssociationType:RPLC")
            String uuidAssociationRPLC = 'urn:uuid:' + UUID.randomUUID()
            associationTypeReplace.setId(uuidAssociationRPLC)
            JAXBElement<IdentifiableType> associationElementReplace = new JAXBElement<IdentifiableType>(
                    new QName(RIM_URN, "Association", "rim"),
                    (Class) AssociationType1.class,
                    associationTypeReplace);
            registryObjectListType.getIdentifiable().add(associationElementReplace);

        }
        SubmitObjectsRequest submitObjectRequest = new SubmitObjectsRequest();
        submitObjectRequest.setRegistryObjectList(registryObjectListType);
        submitObjectRequest.setComment(comment);
        return submitObjectRequest;
    }

    private static List<JAXBElement> createObjectRefType() {
        List<JAXBElement> objectRefTypeList = new ArrayList<JAXBElement>()
        ObjectRefType objectRefType = null
        JAXBElement<IdentifiableType> objectRefElement = null;
        def objectrefList = ["urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1",
                             "urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd"
                             ,"urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8"
                             ,"urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832"
                             ,"urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446"
                             ,"urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1"
                             ,"urn:uuid:aa543740-bdda-424e-8c96-df4873be8500"
                             ,"urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f"
                             ,"urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d"
                             ,"urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead"
                             ,"urn:uuid:f0306f51-975f-434e-a61c-c59651d33983"
                             ,"urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a"
                             ,"urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab"
                             ,"urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427"
                             ,"urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d"
                             ,"urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d"
                            ]

        objectrefList.each {
            objectRefType = Ebrs30Helper.createObjectRefType(it)
            objectRefElement = new JAXBElement<IdentifiableType>(
                    new QName(RIM_URN, "ObjectRef", "rim"),
                    (Class) ObjectRefType.class,
                    objectRefType);
            objectRefTypeList.add(objectRefElement);
        }
        log.debug("+++  List of ObjectRef has " + objectRefTypeList.size() + " entries")
        objectRefTypeList


    }

    public ProvideAndRegisterDocumentSetRequestType.Document createDocumentContent(String id, String data, String cdaEncoding) {
        /*
        //byte[] dataAsBytes = data.getBytes()
        //log.debug("make sure it is not z         " + dataAsBytes.size())
        ProvideAndRegisterDocumentSetRequestType.Document documentContent =
                new ProvideAndRegisterDocumentSetRequestType.Document()
        documentContent.id = id
        DataSource ds;
        log.debug("processing text document")
        ds = new ByteArrayDataSource(data.getBytes(), "application/octet-stream")
        //BASE64Decoder decoder = new BASE64Decoder()
        //byte[] decodedBytes = decoder.decodeBuffer(data)
        DataHandler dataHandler = new DataHandler(ds);
        documentContent.value(dataHandler)
        documentContent
        */
        ProvideAndRegisterDocumentSetRequestType.Document documentContent =
                new ProvideAndRegisterDocumentSetRequestType.Document()
        documentContent.id = id

        DataSource ds;

        println "processing CDA document with ${cdaEncoding}"
        ds = new ByteArrayDataSource(data.getBytes(cdaEncoding), "application/octet-stream")

        documentContent.value = new DataHandler(ds)

        documentContent




    }

    private ExtrinsicObjectType createExtrinsicObject(HashMap mBean) {

        ExtrinsicObjectType extrinsicObjectType = rimFactory.createExtrinsicObjectType()
        extrinsicObjectType.setId("Document01");
        String mimeType = mBean.get("mimeType", "text/xml")
        extrinsicObjectType.setMimeType(mimeType)
        extrinsicObjectType.setObjectType("urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1")

       // extrinsicObjectType.setName(Ebrs30Helper.createInternationalString(mBean.get("documentTitle")))
       // extrinsicObjectType.setDescription(Ebrs30Helper.createInternationalString(mBean.get("typeCodeCodingScheme")))
        List<SlotType1> slots = new ArrayList<SlotType1>()
        slots = extrinsicObjectType.getSlot()
        Ebrs30Helper.addSlot(slots, "creationTime", mBean.get("creationTime"))
        Ebrs30Helper.addSlot(slots, "languageCode", mBean.get("languageCode"))
        Ebrs30Helper.addSlot(slots, "serviceStartTime", mBean.get("serviceStartTime"))
        Ebrs30Helper.addSlot(slots, "serviceStopTime", mBean.get("serviceStopTime"))
        Ebrs30Helper.addSlot(slots, "sourcePatientId",
                mBean.get("globalPatientId") + "^^^&" + mBean.get("globalPatientOid") + "&" + mBean.get("globalPatientNameSpace"))

        String[] strPatientInfo = new String[5];
        strPatientInfo[0] = "PID-3|" +
                mBean.get("globalPatientId") + "^^^&" + mBean.get("globalPatientOid") + "&" + mBean.get("globalPatientNameSpace");
        //mBean.getSourcePatientId() + "^^^&" + mBean.getSourcePatientOId() + "&ISO";
        strPatientInfo[1] = "PID-5|" +
                mBean.get("sourcePatientName")
        strPatientInfo[2] = "PID-7|" +
                mBean.get("sourcePatientBirthdate")
        strPatientInfo[3] = "PID-8|" +
                mBean.get("sourcePatientSexCode")
        strPatientInfo[4] = "PID-11|" +
                mBean.get("sourcePatientAddress")+ "^" + mBean.get("sourcePatientAddressCity") + "^" + mBean.get("sourcePatientAddressState");
        Ebrs30Helper.addSlot(slots, "sourcePatientInfo", strPatientInfo);

        extrinsicObjectType.setName(Ebrs30Helper.createInternationalString((String) mBean.get("reportTitle")))

        // added for Ontario
        //Ebrs30Helper.addSlot(slots,"repositoryUniqueId",mBean.get("repositoryUniqueId"))
        // add slot for reportStatus
        //Ebrs30Helper.addSlot(slots, "urn:eho:reportStatus", mBean.get("persistenceMode"))

        //	Creating classification (Author)

        //Create RefernceIdList
        ArrayList<String> referencedIdList = new ArrayList<String>()
        for(ReferenceId referenceId:mBean.get("referenceIdList")) {
            log.debug("add " + referenceId);
            referencedIdList.add(referenceId.id + "^^^&" + referenceId.domain +  "&ISO^"+ referenceId.urn);
        }
        String[] referencedIds = referencedIdList.toArray();
        Ebrs30Helper.addSlot(slots, "urn:ihe:iti:xds:2013:referenceIdList", referencedIds);
        ClassificationType authorClassification =
                Ebrs30Helper.createClassificationType("urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d");
        authorClassification.setClassifiedObject("Document01");
        String uuidAuthor = 'urn:uuid:' + UUID.randomUUID()
        authorClassification.setId(uuidAuthor)
        addClassificationSchema(authorClassification, mBean);
        extrinsicObjectType.getClassification().add(authorClassification);
        // classCode
        ClassificationType classCodeClassification =
                Ebrs30Helper.createClassificationType("urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a");
        classCodeClassification.setClassifiedObject("Document01")
        String uuidClassCode = 'urn:uuid:' + UUID.randomUUID()
        classCodeClassification.setId(uuidClassCode)
        addClassificationSchema(classCodeClassification, mBean)
        extrinsicObjectType.getClassification().add(classCodeClassification)
        //confidentialityCode
        ClassificationType confidentialityCodeClassification =
                Ebrs30Helper.createClassificationType("urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f")
        confidentialityCodeClassification.setClassifiedObject("Document01")
        String uuidConfidentialityCode = 'urn:uuid:' + UUID.randomUUID()
        confidentialityCodeClassification.setId(uuidConfidentialityCode)
        addClassificationSchema(confidentialityCodeClassification, mBean)
        extrinsicObjectType.getClassification().add(confidentialityCodeClassification)
        //eventCodeList
        if (mBean.containsKey("eventCodeRepresenation")) {

            ClassificationType eventCodeClassification =
                    Ebrs30Helper.createClassificationType("urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4")
            eventCodeClassification.setClassifiedObject("Document01")
            String uuidEventCode = 'urn:uuid:' + UUID.randomUUID()
            eventCodeClassification.setId(uuidConfidentialityCode)
            addClassificationSchema(eventCodeClassification, mBean)
            extrinsicObjectType.getClassification().add(eventCodeClassification)
        }

        // formatCode
        ClassificationType formatCodeClassification =
                Ebrs30Helper.createClassificationType("urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d")
        formatCodeClassification.setClassifiedObject("Document01")
        String uuidformatCode = 'urn:uuid:' + UUID.randomUUID()
        formatCodeClassification.setId(uuidformatCode)
        addClassificationSchema(formatCodeClassification, mBean)
        extrinsicObjectType.getClassification().add(formatCodeClassification)
        //healthCareFacilityTypeCode
        ClassificationType healthCareFacilityTypeCodeClassification =
                Ebrs30Helper.createClassificationType("urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1")
        healthCareFacilityTypeCodeClassification.setClassifiedObject("Document01")
        String uuidhealthCareFacilityTypeCode = 'urn:uuid:' + UUID.randomUUID()
        healthCareFacilityTypeCodeClassification.setId(uuidhealthCareFacilityTypeCode)
        addClassificationSchema(healthCareFacilityTypeCodeClassification, mBean)
        extrinsicObjectType.getClassification().add(healthCareFacilityTypeCodeClassification)
        //practiceSettingCode
        ClassificationType practiceSettingCodeClassification =
                Ebrs30Helper.createClassificationType("urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead")
        practiceSettingCodeClassification.setClassifiedObject("Document01")
        String uuidpracticeSettingCode = 'urn:uuid:' + UUID.randomUUID()
        practiceSettingCodeClassification.setId(uuidpracticeSettingCode)
        addClassificationSchema(practiceSettingCodeClassification, mBean)
        extrinsicObjectType.getClassification().add(practiceSettingCodeClassification)
        // typeCode
        ClassificationType typeCodeClassification =
                Ebrs30Helper.createClassificationType("urn:uuid:f0306f51-975f-434e-a61c-c59651d33983")
        typeCodeClassification.setClassifiedObject("Document01")
        String uuidtypeCode = 'urn:uuid:' + UUID.randomUUID()
        typeCodeClassification.setId(uuidtypeCode)
        addClassificationSchema(typeCodeClassification, mBean)
        extrinsicObjectType.getClassification().add(typeCodeClassification)

        //	Creating External Identifiers (XDSDocumentEntry.patientId)
        String patIdExtidentifier = mBean.get("globalPatientId") + "^^^&" + mBean.get("globalPatientOid") + "&" + mBean.get("globalPatientNameSpace")

        ExternalIdentifierType externalidPatientId =
                Ebrs30Helper.createExternalIdentifier(Vocabulary.DOC_ENTRY_PATIENT_ID_EXTERNAL_ID, patIdExtidentifier);
        externalidPatientId.setName(Ebrs30Helper.createInternationalString(Vocabulary.DOC_ENTRY_LOCALIZED_STRING_PATIENT_ID));

        String uuidPatientId = 'urn:uuid:' + UUID.randomUUID()
        externalidPatientId.setId(uuidPatientId)
        externalidPatientId.setRegistryObject("Document01")
        extrinsicObjectType.getExternalIdentifier().add(externalidPatientId);


        //	Creating External Identifiers (XDSDocumentEntry.uniqueId)
        String uniqueId = mBean.get("xdsUniqueSubmissionId")
        ExternalIdentifierType externalidUniqueId =
                Ebrs30Helper.createExternalIdentifier(Vocabulary.DOC_ENTRY_UNIQUE_ID_EXTERNAL_ID, mBean.get("xdsDocumentUniqueId"));
        externalidUniqueId.setName(Ebrs30Helper.createInternationalString(Vocabulary.DOC_ENTRY_LOCALIZED_STRING_UNIQUE_ID));

        String uuidUniqueId = 'urn:uuid:' + UUID.randomUUID()
        externalidUniqueId.setId(uuidUniqueId)
        externalidUniqueId.setRegistryObject("Document01")
        extrinsicObjectType.getExternalIdentifier().add(externalidUniqueId);

        return extrinsicObjectType;

    }


    private ClassificationType addClassificationSchema(ClassificationType classType, HashMap mBean) {
        //log.debug("addClassificationSchema ++++++++++++++"  + classType.classificationScheme)
        // Manage codes
        //1. Author code
        //2. Class code
        //3. Confidentiality code
        //4. Format code
        //5. Healthcare Facility code
        //6. Practice Setting code
        //7. Type code
        switch (classType.getClassificationScheme()) {
        //Author code
            case "urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d":
                classType.setNodeRepresentation("");
                List<SlotType1> slots = new ArrayList<SlotType1>()
                slots = classType.getSlot();
                Ebrs30Helper.addSlot(slots, "authorPerson", mBean.get("authorName") + "^^^&" +
                        mBean.get("authorDomainId") + "&ISO")
                //Ebrs30Helper.addSlot(slots, "authorPerson", mBean.get("authorName"))
                Ebrs30Helper.addSlot(slots, "authorInstitution", mBean.get("authorInstitutionName"))
                Ebrs30Helper.addSlot(slots, "authorRole", mBean.get("authorRole"))
                Ebrs30Helper.addSlot(slots, "authorSpecialty", mBean.get("authorSpecialty"))
                //Ebrs30Helper.addSlot(slots, "authorPerson", mBean.get("authorName"))
                //Ebrs30Helper.addSlot(slots, "authorInstitution", mBean.get("authorInstitute") + "^^^^^^^^^" + mBean.get("authorInstituteDomain", "i don't know from there I should look it up"))
                //Ebrs30Helper.addSlot(slots, "authorRole", mBean.get("authorRole"));
                // Ebrs30Helper.addSlot(slots, "authorSpecialty", mBean.getAuthorSpeciality());
                break;
            case "urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d":
                classType.setNodeRepresentation("");
                List<SlotType1> slots = new ArrayList<SlotType1>()
                slots = classType.getSlot();
                Ebrs30Helper.addSlot(slots, "authorPerson", mBean.get("authorName") + "^^^&" +
                        mBean.get("authorDomainId") + "&ISO")
                //Ebrs30Helper.addSlot(slots, "authorPerson", mBean.get("authorName"))
                Ebrs30Helper.addSlot(slots, "authorInstitution", mBean.get("authorInstitutionName"))
                // removed not needed for Ontario
                Ebrs30Helper.addSlot(slots, "authorRole", mBean.get("authorRole"))
                Ebrs30Helper.addSlot(slots, "authorSpecialty", mBean.get("authorSpecialty"))
                break;
        // Class code
            case "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a":
                if ((String) mBean.get("reportMode") == "preliminary") {
                    classType.setNodeRepresentation(mBean.get("preliminaryClassCodeNodeRepresentation"))
                    InternationalStringType strName =
                            Ebrs30Helper.createInternationalString(mBean.get("preliminaryClassCodeDisplayName"))
                    classType.setName(strName);
                    List<SlotType1> slots = new ArrayList<SlotType1>()
                    slots = classType.getSlot();
                    Ebrs30Helper.addSlot(slots, "codingScheme", mBean.get("preliminaryClassCodeCodingScheme"))

                }   else {
                    classType.setNodeRepresentation(mBean.get("classCodeNodeRepresentation"))
                    InternationalStringType strName =
                            Ebrs30Helper.createInternationalString(mBean.get("classCodeDisplayName"))
                    classType.setName(strName);
                    List<SlotType1> slots = new ArrayList<SlotType1>()
                    slots = classType.getSlot();
                    Ebrs30Helper.addSlot(slots, "codingScheme", mBean.get("classCodeCodingScheme"))
                }
                break;
        // Confidentiality code
            case "urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f":
                classType.setNodeRepresentation(mBean.get("confidentialityCodeNodeRepresentation"))
                InternationalStringType strName =
                        Ebrs30Helper.createInternationalString(mBean.get("confidentialityCodeDisplayName"))
                classType.setName(strName);
                List<SlotType1> slots = new ArrayList<SlotType1>()
                slots = classType.getSlot();
                Ebrs30Helper.addSlot(slots, "codingScheme", mBean.get("confidentialityCodeCodingScheme"))
                break;
        // Format code
            case "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d":
                if ((String) mBean.get("reportMode") == "preliminary") {
                    classType.setNodeRepresentation(mBean.get("preliminaryFormatCodeNodeRepresentation"))
                    InternationalStringType strName =
                            Ebrs30Helper.createInternationalString(mBean.get("preliminaryFormatCodeDisplayName"))
                    classType.setName(strName);
                    List<SlotType1> slots = new ArrayList<SlotType1>()
                    slots = classType.getSlot();
                    Ebrs30Helper.addSlot(slots, "codingScheme", mBean.get("preliminaryFormatCodeCodingScheme"))

                }   else {
                    classType.setNodeRepresentation(mBean.get("formatCodeNodeRepresentation"))
                    InternationalStringType strName =
                            Ebrs30Helper.createInternationalString(mBean.get("formatCodeDisplayName"))
                    classType.setName(strName);
                    List<SlotType1> slots = new ArrayList<SlotType1>();
                    slots = classType.getSlot()
                    Ebrs30Helper.addSlot(slots, "codingScheme", mBean.get("formatCodeCodingScheme"))
                }
                break;
        // Healthcare Facility code
            case "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1":
                classType.setNodeRepresentation(mBean.get("healthcareFacilityCodeNodeRepresentation"))
                InternationalStringType strName =
                        Ebrs30Helper.createInternationalString(mBean.get("healthcareFacilityCodeDisplayName"));
                classType.setName(strName);
                List<SlotType1> slots = new ArrayList<SlotType1>();
                slots = classType.getSlot();
                Ebrs30Helper.addSlot(slots, "codingScheme", mBean.get("healthcareFacilityCodeCodingScheme"));
                break;
        // Practice Setting code
            case "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead":
                classType.setNodeRepresentation(mBean.get("practiceCodeNodeRepresentation"));
                InternationalStringType strName =
                        Ebrs30Helper.createInternationalString(mBean.get("practiceCodeDisplayName"));
                classType.setName(strName);
                List<SlotType1> slots = new ArrayList<SlotType1>();
                slots = classType.getSlot();
                Ebrs30Helper.addSlot(slots, "codingScheme", mBean.get("practiceCodeCodingScheme"));
                break;
        // Type code
            case "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983":
                classType.setNodeRepresentation(mBean.get("examinationCode"))
                InternationalStringType strName =
                        Ebrs30Helper.createInternationalString(mBean.get("examinationName"))
                classType.setName(strName);
                List<SlotType1> slots = new ArrayList<SlotType1>()
                slots = classType.getSlot()
                Ebrs30Helper.addSlot(slots, "codingScheme", mBean.get("typeCodeCodingScheme"))
                break;
        // Content Type code
            case "urn:uuid:aa543740-bdda-424e-8c96-df4873be8500":
                classType.setNodeRepresentation(mBean.get("contentNodeRepresentation"))
                InternationalStringType strName =
                        Ebrs30Helper.createInternationalString(mBean.get("contentDisplayName"))
                classType.setName(strName)
                List<SlotType1> slots = new ArrayList<SlotType1>()
                slots = classType.getSlot()
                Ebrs30Helper.addSlot(slots, "codingScheme", mBean.get("contentCodeCodingScheme"))
                break;

       //Event code list (Optional field)
            case "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4":
                // several eventCodes
                classType.setNodeRepresentation(mBean.get("eventCodeRepresenation"))
                InternationalStringType strName =
                        Ebrs30Helper.createInternationalString(mBean.get("eventCodeDisplayName"))
                classType.setName(strName);
                List<SlotType1> slots = new ArrayList<SlotType1>()
                slots = classType.getSlot();
                Ebrs30Helper.addSlot(slots, "codingScheme", mBean.get("eventCodeScheme"))
                break;

        }

        return classType;
    }

    private RegistryPackageType createSubmissionSet(HashMap mBean) {

        RegistryPackageType registryPackageType = rimFactory.createRegistryPackageType();
        registryPackageType.setId("SubmissionSet01")

        // Submission Time - current datetime
        List<SlotType1> slots = new ArrayList<SlotType1>();
        slots = registryPackageType.getSlot();
        Ebrs30Helper.addSlot(slots, "submissionTime", mBean.get("submissionTime"));
        registryPackageType.setName(Ebrs30Helper.createInternationalString("Physical"));

        //	Creating classification (Author)
        ClassificationType authorClassification =
                Ebrs30Helper.createClassificationType("urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d");
        authorClassification.setClassifiedObject("SubmissionSet01");
        String uuidAuthor = 'urn:uuid:' + UUID.randomUUID()
        authorClassification.setId(uuidAuthor)
        addClassificationSchema(authorClassification, mBean);
        registryPackageType.getClassification().add(authorClassification);

        //	Creating classification (Content Type code)
        ClassificationType contentTypeCodeClassification =
                Ebrs30Helper.createClassificationType("urn:uuid:aa543740-bdda-424e-8c96-df4873be8500");
        contentTypeCodeClassification.setClassifiedObject("SubmissionSet01");
        String uuidTypeCode = 'urn:uuid:' + UUID.randomUUID()
        contentTypeCodeClassification.setId(uuidTypeCode)
        addClassificationSchema(contentTypeCodeClassification, mBean);
        registryPackageType.getClassification().add(contentTypeCodeClassification);


        String uniqueId = mBean.get("xdsSubmissionUniqueId")

        //	Creating External Identifiers (XDSSubmissionSet.uniqueId)
        ExternalIdentifierType externalidUniqueId =
                Ebrs30Helper.createExternalIdentifier(Vocabulary.SUBMISSION_SET_UNIQUE_ID_EXTERNAL_ID, uniqueId);
        externalidUniqueId.setName(Ebrs30Helper.createInternationalString(Vocabulary.SUBMISSION_SET_LOCALIZED_STRING_UNIQUE_ID));
        String uuidUniqueId = 'urn:uuid:' + UUID.randomUUID()
        externalidUniqueId.setId(uuidUniqueId)
        externalidUniqueId.setRegistryObject("SubmissionSet01")
        registryPackageType.getExternalIdentifier().add(externalidUniqueId);

        //	Creating External Identifiers (XDSSubmissionSet.sourceId)
        ExternalIdentifierType externalidSourceId =
                Ebrs30Helper.createExternalIdentifier(Vocabulary.SUBMISSION_SET_SOURCE_ID_EXTERNAL_ID,
                        mBean.get("submissionsSourceId"));
        externalidSourceId.setName(Ebrs30Helper.createInternationalString(Vocabulary.SUBMISSION_SET_LOCALIZED_STRING_SOURCE_ID));
        String uuidSourceId = 'urn:uuid:' + UUID.randomUUID()
        externalidSourceId.setId(uuidSourceId)
        externalidSourceId.setRegistryObject("SubmissionSet01")
        registryPackageType.getExternalIdentifier().add(externalidSourceId);

        //	Creating External Identifiers (XDSSubmissionSet.patientId)
        String patIdExtidentifier =
                mBean.get("globalPatientId") + "^^^&" + mBean.get("globalPatientOid") + "&" + mBean.get("globalPatientNameSpace")

        ExternalIdentifierType externalidPatientId =
                Ebrs30Helper.createExternalIdentifier(Vocabulary.SUBMISSION_SET_PATIENT_ID_EXTERNAL_ID, patIdExtidentifier);
        externalidPatientId.setName(Ebrs30Helper.createInternationalString(Vocabulary.SUBMISSION_SET_LOCALIZED_STRING_PATIENT_ID));
        String uuidPatientId = 'urn:uuid:' + UUID.randomUUID()
        externalidPatientId.setId(uuidPatientId)
        externalidPatientId.setRegistryObject("SubmissionSet01")
        registryPackageType.getExternalIdentifier().add(externalidPatientId);

        return registryPackageType;
    }


    private AssociationType1 createAssociation(String sourceObject, String targetObject) {
        AssociationType1 associationType = Ebrs30Helper.createAssociation(sourceObject, targetObject);

        List<SlotType1> slots = new ArrayList<SlotType1>();
        slots = associationType.getSlot();
        Ebrs30Helper.addSlot(slots, "SubmissionSetStatus", "Original");

        return associationType;
    }

    private AssociationType1 createAssociationRPLC(String sourceObject, String targetObject) {
        AssociationType1 associationType = Ebrs30Helper.createAssociation(sourceObject, targetObject);

        // List<SlotType1> slots = new ArrayList<SlotType1>();
        // slots = associationType.getSlot();
        // Ebrs30Helper.addSlot(slots, "SubmissionSetStatus", "Original");

        return associationType;
    }

}
