package com.ge.hc.oru.util

import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.ProvideAndRegisterDocumentSetRequestType
import org.openehealth.ipf.commons.ihe.xds.core.requests.QueryRegistry
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rs.RegistryResponseType

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

/**
 * Created by 100026806 on 2/26/14.
 */
abstract class JaxbUtils {

    /**
     * JAXB context for transforming generated XDS requests to String
     * for logging purposes.
     */
    // test
    private static final JAXBContext JAXB_CONTEXT = JAXBContext.newInstance(
            ProvideAndRegisterDocumentSetRequestType.class,
            RegistryResponseType.class,
            QueryRegistry.class

    )


    /**
     * Returns marshaled XML representation of the given ebXML POJO.
     */
    static String marshal(ebXml) {
        StringWriter writer = new StringWriter()
        Marshaller marshaller = JAXB_CONTEXT.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
        marshaller.marshal(ebXml, writer)
        return writer.toString()
    }

}
