# ORU test for ITI-18

1. ITI-18 response status != sucesss
    
    edit the in the soap mock project the query respnse to Partial_Success
    check if the HL7 NACK response  include the error message:
    XDS transaction failed for unknown reason
    --> OK
    
2. ITI-18 is status success and submission mode is ORIGINAL  message is proceessed and ITI-41 is triggered
   --> OK
  
3. ITI-18 status  is Success and submission mode = REPLACE
   Prerequisite: edit external Identifier in the sopa response to the same id which is created by the new document 
   change type code to the same valuyes as your testmessage test:UXCA00 response was 34098-4
   a) if creation of new document is newer than the creation date of the old document ITI-41 is triggered
   --> ok
   
   b) if creation of new document is older than the creation date of the old document HL7 NACK with error:
    --> document not send, since a newer document is already available
    --> OK

4. ITI-18 response status is success and submission mode is invalid HL7 NACK response which has the error message:
   edit xds response: 
   edit the soapui response message substitute the format code representation with urn:ihe:rad:TEXT
   
   not possible to register a preliminary document if a final one is already registered
   --> OK
 
   

