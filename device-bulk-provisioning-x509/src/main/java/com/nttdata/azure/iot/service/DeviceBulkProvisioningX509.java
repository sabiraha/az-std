package com.nttdata.azure.iot.service;

import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.Query;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;


/**
 * Device bulk provisioning
 *
 */
public class DeviceBulkProvisioningX509 
{

	/*
	 * Details of the Provisioning.
	 */
	private static final String PROVISIONING_CONNECTION_STRING = "HostName=cat-poc-iothub.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=+mAVnrgxRgOLaWMR2+hP7lINJTy+VdKb4yuy7UbO4dA=";
	private static final int QUERY_PAGE_SIZE = 3;
    private static final String registrationId1 =  "thermostats-device-003";
	private static final String clientCertificate1 = 
    		"-----BEGIN CERTIFICATE-----\n" +
    		"MIICIjCCAcigAwIBAgIFCgsMDQ4wCgYIKoZIzj0EAwIwNTETMBEGA1UEAwwKU2ln\n" +
    		"bmVyQ2VydDERMA8GA1UECgwITVNSX1RFU1QxCzAJBgNVBAYTAlVTMCAXDTE3MDEw\n" +
    		"MTAwMDAwMFoYDzM3MDEwMTMxMjM1OTU5WjBBMR8wHQYDVQQDDBZ0aGVybW9zdGF0\n" +
    		"cy1kZXZpY2UtMDAzMREwDwYDVQQKDAhNU1JfVEVTVDELMAkGA1UEBhMCVVMwWTAT\n" +
    		"BgcqhkjOPQIBBggqhkjOPQMBBwNCAASzMBVesJMtaHqUuY0y/wubgjnq6cZ7B+sj\n" +
    		"dk2iiRQ3tGkywVkNXfWaebuAOLmtvQyYtUvaWjzp+UDaQrlot8OMo4G2MIGzMBMG\n" +
    		"A1UdJQQMMAoGCCsGAQUFBwMCMIGbBgZngQUFBAEEgZAwgY0CAQEwWTATBgcqhkjO\n" +
    		"PQIBBggqhkjOPQMBBwNCAAQXT9ZlbbsnBalW7DIOyKCnTpduOXZi7zPhmzq/queh\n" +
    		"Rg7qq+Xz++eZU31U1rANpcGs4liGg67crB3elE0hOI4AMC0GCWCGSAFlAwQCAQQg\n" +
    		"ERITFAUGBwgBAgMEBQYHCAECAwQFBgcIAQIDBAUGBwgwCgYIKoZIzj0EAwIDSAAw\n" +
    		"RQIhAOhjeKXjTju7HgmXLW1sw9Z3/It+NeMyBmyivDw5lFx+AiBaGL9AnNjg/oHf\n" +
    		"7zwGsFuHKg3a9ei+0NSbAarM22kbQQ==\n" +
    		"-----END CERTIFICATE-----\n";
    private static final String registrationId2 =  "thermostats-device-004";
	private static final String clientCertificate2 = 
			"-----BEGIN CERTIFICATE-----\n" +
			"MIICIjCCAcigAwIBAgIFCgsMDQ4wCgYIKoZIzj0EAwIwNTETMBEGA1UEAwwKU2ln\n" +
			"bmVyQ2VydDERMA8GA1UECgwITVNSX1RFU1QxCzAJBgNVBAYTAlVTMCAXDTE3MDEw\n" +
			"MTAwMDAwMFoYDzM3MDEwMTMxMjM1OTU5WjBBMR8wHQYDVQQDDBZ0aGVybW9zdGF0\n" +
			"cy1kZXZpY2UtMDA0MREwDwYDVQQKDAhNU1JfVEVTVDELMAkGA1UEBhMCVVMwWTAT\n" +
			"BgcqhkjOPQIBBggqhkjOPQMBBwNCAASzMBVesJMtaHqUuY0y/wubgjnq6cZ7B+sj\n" +
			"dk2iiRQ3tGkywVkNXfWaebuAOLmtvQyYtUvaWjzp+UDaQrlot8OMo4G2MIGzMBMG\n" +
			"A1UdJQQMMAoGCCsGAQUFBwMCMIGbBgZngQUFBAEEgZAwgY0CAQEwWTATBgcqhkjO\n" +
			"PQIBBggqhkjOPQMBBwNCAAQXT9ZlbbsnBalW7DIOyKCnTpduOXZi7zPhmzq/queh\n" +
			"Rg7qq+Xz++eZU31U1rANpcGs4liGg67crB3elE0hOI4AMC0GCWCGSAFlAwQCAQQg\n" +
			"ERITFAUGBwgBAgMEBQYHCAECAwQFBgcIAQIDBAUGBwgwCgYIKoZIzj0EAwIDSAAw\n" +
			"RQIhAOhjeKXjTju7HgmXLW1sw9Z3/It+NeMyBmyivDw5lFx+AiBMgvGzi3DiYJdQ\n" +
			"/05vgr65fktUtSlS4QTmJ77myacP6w==\n" +
			"-----END CERTIFICATE-----\n";

	private static final Map<String, String> DEVICE_MAP = new HashMap<String, String>()
			{
			    {
			        put(registrationId1, clientCertificate1);
			        put(registrationId2, clientCertificate2);
			    }
			};	
	
	public static void main(String[] args) throws ProvisioningServiceClientException    {
		System.out.println("Beginning my sample for the Provisioning Service Client!");

		 // ********************************** Create a Provisioning Service Client ************************************
		 ProvisioningServiceClient provisioningServiceClient =
		         ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);  
		 
		// ******************************** Create a new bulk of IndividualEnrollment *********************************
		 System.out.println("\nCreate a new set of individualEnrollments...");
		 List<IndividualEnrollment> individualEnrollments = new LinkedList<>();
		 for(Map.Entry<String, String> device:DEVICE_MAP.entrySet())
		 {
			 Attestation attestation = X509Attestation.createFromClientCertificates(device.getValue());
			 String registrationId = device.getKey();
		     System.out.println("  Add " + registrationId);
		     IndividualEnrollment individualEnrollment =
		             new IndividualEnrollment(
		                     registrationId,
		                     attestation);
		     individualEnrollments.add(individualEnrollment);
		 }

		 
        System.out.println("\nRun the bulk operation to create the individualEnrollments...");
        BulkEnrollmentOperationResult bulkOperationResult =  provisioningServiceClient.runBulkEnrollmentOperation(
                BulkOperationMode.CREATE, individualEnrollments);
        System.out.println("Result of the Create bulk enrollment...");
        System.out.println(bulkOperationResult);

		 
        // ************************************ Get info of individualEnrollments *************************************
        for (IndividualEnrollment individualEnrollment : individualEnrollments)
        {
            String registrationId = individualEnrollment.getRegistrationId();
            System.out.println("\nGet the individualEnrollment information for " + registrationId + ":");
            IndividualEnrollment getResult = provisioningServiceClient.getIndividualEnrollment(registrationId);
            System.out.println(getResult);
        }

        // ************************************ Query info of individualEnrollments ***********************************
        System.out.println("\nCreate a query for individualEnrollments...");
        QuerySpecification querySpecification =
                new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS)
                        .createSqlQuery();
        Query query = provisioningServiceClient.createIndividualEnrollmentQuery(querySpecification, QUERY_PAGE_SIZE);

        while(query.hasNext())
        {
            System.out.println("\nQuery the next individualEnrollments...");
            QueryResult queryResult = query.next();
            System.out.println(queryResult);
        }

	}
}
