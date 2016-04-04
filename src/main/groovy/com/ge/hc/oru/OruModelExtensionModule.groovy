package com.ge.hc.oru

import ca.uhn.hl7v2.model.Message
import ca.uhn.hl7v2.parser.Parser
import ca.uhn.hl7v2.parser.PipeParser
import org.apache.camel.model.ProcessorDefinition
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openehealth.ipf.modules.hl7.AckTypeCode
import org.openehealth.ipf.modules.hl7.HL7v2Exception

class OruModelExtensionModule {

    private static final transient Log LOG = LogFactory.getLog(OruModelExtensionModule.class);

    static extensions = {

        ProcessorDefinition.metaClass.output = { String message, Closure c ->
            return delegate.process {
                def payload = c ? c(it.in.body) : it.in.body
                LOG.debug("\n${'-' * 20} ${message} ${'-' * 20}\n${payload}")
            }
        }

        ProcessorDefinition.metaClass.nak = {String m ->
            return delegate.process {
                // that is a MessageAdapter I don't know how to convert to ca.uhn.hl7v2.model.Message
                def mess = it.getProperty("orm")
                LOG.debug("context property is from type: " + mess.getClass())
                // so we save the original message in a Header parameter which is a String
                def mess2 = it.in.getHeader("ORIGINAL.MESSAGE")
                LOG.debug("header parameter ORIGINAL.MESSAGE is from type:  " + mess2.getClass())
                Parser p = new PipeParser()
                Message message = p.parse(mess2)
                it.in.body =  message
                def e = new HL7v2Exception(m)
                it.out.body = it.in.body.nak(e, AckTypeCode.AE)
                //it.out.body = it.in.body.nak(e, AckTypeCode.AE) -> ca.uhn.hl7v2.model.Message
            }
        }

    }



    /* static ProcessorDefinition reverse(ProcessorDefinition self) {
         self.transmogrify { it.reverse() } 
     }*/
     
}
