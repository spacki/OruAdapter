package com.ge.hc.oru.util

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Created by 100026806 on 2/25/14.
 */
class IdHelper {

    private final static Log log = LogFactory.getLog(IdHelper.class)

    public static String createUniqueId(String rootId) {
        String currTime = (String) System.currentTimeMillis();
        String uniqueId = rootId + ".0." + currTime;

    }

    public static String createUniqueId(String rootId, String id) {
        int maxIdLength = 64
        String currTime = (String) System.currentTimeMillis()
        String uniqueId
        int idLength = rootId.length() + currTime.length() + id.length() + 2
        if (idLength <= maxIdLength)  {
            log.debug("no need to shorten rootId XDSDocumentEntry.uniqueId " + idLength)
            uniqueId = rootId + "." + currTime + "." + id;
        } else {
            log.debug("we need to shorten the rootId by " + idLength - maxIdLength + " character(s)")
            def shortenRootId = rootId.substring(0,(rootId.length() -(idLength - maxIdLength)))
            // make sure the new root id does not end with a period (.)
            while (shortenRootId.endsWith('.'))  {
                log.debug("shortid" + shortenRootId)
                shortenRootId = shortenRootId.substring(0,shortenRootId.length() - 1)
            }
            log.debug("new root id: rootId.substring(0, (idLength - maxIdLength)) " + shortenRootId)

            uniqueId = shortenRootId + "." + currTime + "." + id;
        }

        uniqueId

    }

    public static String createUniqueId(String rootId, String id, String prefix) {
        int maxIdLength = 64
        String currTime = (String) System.currentTimeMillis()
        String uniqueId
        int idLength = rootId.length() + currTime.length() + id.length() + 5
        if (idLength <= maxIdLength)  {
            log.debug("no need to shorten rootId XDSDocumentEntry.uniqueId ${idLength} S{maxIdLength}")
            uniqueId = rootId + "." + currTime + "." + prefix + "." + id;
        } else {
            log.debug("we need to shorten the rootId by id lenght : ${idLength} minus ${maxIdLength} = " +  (idLength - maxIdLength) + " character(s)")
            def shortenRootId = rootId.substring(0,(rootId.length() -(idLength - maxIdLength)))
            // make sure the new root id does not end with a period (.)
            while (shortenRootId.endsWith('.'))  {
                log.debug("shortid" + shortenRootId)
                shortenRootId = shortenRootId.substring(0,shortenRootId.length() - 1)
            }
            log.debug("new root id: rootId.substring(0, (idLength - maxIdLength)) " + shortenRootId)
            uniqueId = shortenRootId + "." + currTime +  "." + prefix + "." + id;
        }

        uniqueId

    }

    public static String createSubmissionId(String docID) {
        def idComponents = docID.split('\\.')
        log.debug("old time stamp " + idComponents[idComponents.length-2])
        idComponents[idComponents.length-2] =  idComponents[idComponents.length-2].toLong() + 1
        def submissionId = idComponents.join('.')
        submissionId
    }

    public static String createUniqueID() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS")
        Date now = new Date()
        String uniqueId = dateFormat.format(now)
    }

    public static String createUTCTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssZ")
        Date now = new Date()
        String messageTime = dateFormat.format(now)
    }

    public static String getUTCTimestamp(String date, String timezoneId) {
        log.debug("get utc time from local timestamp from message " + date + " timezone: " + timezoneId)
        // get the time zone
        DateTimeZone zone = DateTimeZone.forID(timezoneId)
        // convert timestamp string into date
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmss")
        DateTime dt = formatter.parseDateTime(date)
        log.debug("Local Date: " + dt)
        // convert the date to utc
        DateTime utcDate = new DateTime(zone.convertLocalToUTC(dt.getMillis(), false))
        log.debug("UTC   Date: " + utcDate)
        //convert the utc date back to string old java stuff
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        //System.out.println(dateFormat.format(utcDate));
        utcDate.toString(formatter)
    }
    // remove trailing characters from the accessionNumber
    public static String removePrefixFromAccessionNumber(String accessionNumber) {
        //accessionNumber.substring(3)
        //accessionNumber.replaceAll("^[^\\d]*", "")
        /*The ^ anchor will make sure that the 0+ being matched is at the beginning of the input.
        The (?!$) negative lookahead ensures that not the entire string will be matched.
         */
        accessionNumber.replaceAll("^[^\\d]*", "").replaceFirst("^0+(?!\\\$)", "")
    }

}
