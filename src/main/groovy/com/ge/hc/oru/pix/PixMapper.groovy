package com.ge.hc.oru.pix

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Created by karstenspakowski on 23/10/15.
 */
class PixMapper {

    private static final transient Log LOG = LogFactory.getLog(PixMapper.class)

    public static String setGlobalPatientId(def message) {
        def globalPatientId = null
        // LOG.debug ("Pix \n" + message.replaceAll("\r", "\r\n"))
        def pix = message.replaceAll("\r", "\r\n")
        pix.eachLine {
            if (it =~ /PID/) {
                def pid = it.split("\\|")
                def globalID = pid[3].split("\\^")
                globalPatientId = globalID[0]
            }
        }
        LOG.debug("Global Patient ID: " + globalPatientId)
        globalPatientId
    }

    public static String setGlobalPatientOid(def message) {
        def globalPatientOid = null
        def pix = message.replaceAll("\r", "\r\n")
        //LOG.debug ("Pix \n" + message.replaceAll("\r", "\r\n"))

        pix.eachLine {
            if (it =~ /PID/) {
                def pid = it.split("\\|")
                def globalID = pid[3].split("\\^")
                def organization = globalID[3].split("\\&")
                globalPatientOid = organization[1]
            }
        }
        LOG.debug("Global Patient Organization ID: " + globalPatientOid)
        globalPatientOid
    }

    public static String setGlobalPatientNameSpace(def message) {
        def globalPatientNameSpace = "ISO"

        //LOG.debug ("Pix \n" + message.replaceAll("\r", "\r\n"))
        /*
        def pix = message.replaceAll("\r", "\r\n")
        pix.eachLine {
            if (it =~ /PID/) {
                def pid = it.split("\\|")
                def globalID = pid[3].split("\\^")
                def organization = globalID[3].split("\\&")
                globalPatientNameSpace = organization[0]
            }
        }
        */
        LOG.debug("Global Patient NameSpace: " + globalPatientNameSpace)
       globalPatientNameSpace
    }


    /*
        Test method to fix encoding issues
     */
    private static String convertLatin2UTF8(String latin1) {
        byte[] latin1_ba = latin1
        byte[] utf8_ba = new String(latin1_ba, "ISO-8859-1").getBytes("UTF-8")
        def utf8 =  new String(utf8_ba, "UTF-8")
    }

}
