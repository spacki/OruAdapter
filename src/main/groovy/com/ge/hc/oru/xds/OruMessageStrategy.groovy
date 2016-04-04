package com.ge.hc.oru.xds

import com.ge.hc.oru.commons.MessageStrategyAbs
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Created by 100026806 on 2/26/14.
 */
class OruMessageStrategy extends MessageStrategyAbs {
    def clinicalDocument
    def HashMap<String, String> metadata

    private final static Log log = LogFactory.getLog(OruMessageStrategy.class)


    public OruMessageStrategy(String document, HashMap<String, String> meta) {
        clinicalDocument = document
        metadata = meta
    }

    @Override
    String getDocContent() {
        return this.clinicalDocument
    }

    @Override
    HashMap getMetaInput() {
        return this.metadata
    }
}
