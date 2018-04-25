// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.nttdata.azure.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Device Provisioning for X509
 */
public class DeviceProvisioningX509
{
    private static final String idScope = "0ne00015A50";
    private static final String globalEndpoint = "global.azure-devices-provisioning.net";
    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS_WS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT_WS;
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 10000; // in milli seconds

    private static final String publicKeyCertificateString = 
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

    

    private static final String privateKeyString = 
    		"-----BEGIN PRIVATE KEY-----\n" +
    		"MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgi+wLJqCQ0xTkUMTU\n" +
    		"ZWr4GvTitfeq5AIL6IDdNCvSeFygCgYIKoZIzj0DAQehRANCAASzMBVesJMtaHqU\n" +
    		"uY0y/wubgjnq6cZ7B+sjdk2iiRQ3tGkywVkNXfWaebuAOLmtvQyYtUvaWjzp+UDa\n" +
    		"Qrlot8OM\n" +
    		"-----END PRIVATE KEY-----\n";

    		
    private static final Collection<String> signerCertificates = new LinkedList<>();

    static class ProvisioningStatus
    {
        ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationInfoClient = new ProvisioningDeviceClientRegistrationResult();
        Exception exception;
    }

    static class ProvisioningDeviceClientRegistrationCallbackImpl implements ProvisioningDeviceClientRegistrationCallback
    {
        @Override
        public void run(ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult, Exception exception, Object context)
        {
            if (context instanceof ProvisioningStatus)
            {
                ProvisioningStatus status = (ProvisioningStatus) context;
                status.provisioningDeviceClientRegistrationInfoClient = provisioningDeviceClientRegistrationResult;
                status.exception = exception;
            }
            else
            {
                System.out.println("Received unknown context");
            }
        }
    }

    private static class IotHubEventCallbackImpl implements IotHubEventCallback
    {
        @Override
        public void execute(IotHubStatusCode responseStatus, Object callbackContext)
        {
            System.out.println("Message received!");
        }
    }

    public static void main(String[] args) throws Exception
    {
    	/**
    	if (args.length <= 1 || args.length >= 3)
        {
            System.out.format(
                    "Expected 2 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. Public Key Certificate String\n"
                            + "2. Private Key Certificate String \n",
                    args.length);
            return;
        }
    	
        String publicKeyCertificateString = args[0].trim();
        String privateKeyString = args[1].trim();
        
        System.out.println("publicKeyCertificateString :" + publicKeyCertificateString);
        System.out.println("privateKeyString : " + privateKeyString);
        **/
        		
        System.out.println("Starting...");
        System.out.println("Beginning setup.");
        ProvisioningDeviceClient provisioningDeviceClient = null;
        DeviceClient deviceClient = null;
        try
        {
            ProvisioningStatus provisioningStatus = new ProvisioningStatus();

            SecurityProvider securityProviderX509 = new SecurityProviderX509Cert(publicKeyCertificateString, privateKeyString, signerCertificates);
            provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, idScope, PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL,
                                                                       securityProviderX509);

            provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus);

            while (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() != ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ERROR ||
                        provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_DISABLED ||
                        provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_FAILED )

                {
                    provisioningStatus.exception.printStackTrace();
                    System.out.println("Registration error, bailing out");
                    break;
                }
                System.out.println("Waiting for Provisioning Service to register");
                Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
            }

            if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                System.out.println("IotHUb Uri : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
                System.out.println("Device ID : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

                // connect to iothub
                String iotHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();
                String deviceId = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId();
                try
                {
                    deviceClient = DeviceClient.createFromSecurityProvider(iotHubUri, deviceId, securityProviderX509, IotHubClientProtocol.MQTT);
                    deviceClient.open();
                    Message messageToSendFromDeviceToHub =  new Message("Whatever message you would like to send");

                    System.out.println("Sending message from device to IoT Hub...");
                    deviceClient.sendEventAsync(messageToSendFromDeviceToHub, new IotHubEventCallbackImpl(), null);
                }
                catch (IOException e)
                {
                    System.out.println("Device client threw an exception: " + e.getMessage());
                    if (deviceClient != null)
                    {
                        deviceClient.closeNow();
                    }
                }
            }
        }
        catch (ProvisioningDeviceClientException | InterruptedException e)
        {
            System.out.println("Provisioning Device Client threw an exception" + e.getMessage());
            if (provisioningDeviceClient != null)
            {
                provisioningDeviceClient.closeNow();
            }
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        System.out.println("Shutting down...");
        if (provisioningDeviceClient != null)
        {
            provisioningDeviceClient.closeNow();
        }
        if (deviceClient != null)
        {
            deviceClient.closeNow();
        }
    }
}
