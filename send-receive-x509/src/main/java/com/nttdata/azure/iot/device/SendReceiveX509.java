// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.nttdata.azure.iot.device;

import com.microsoft.azure.sdk.iot.device.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Handles messages from an IoT Hub. Default protocol is to use
 * MQTT transport.
 */
public class SendReceiveX509
{
    //PEM encoded representation of the public key certificate
    private static String publicKeyCertificateString =
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

    //PEM encoded representation of the private key
    private static String privateKeyString =
    		"-----BEGIN PRIVATE KEY-----\n" +
    		"MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgi+wLJqCQ0xTkUMTU\n" +
    		"ZWr4GvTitfeq5AIL6IDdNCvSeFygCgYIKoZIzj0DAQehRANCAASzMBVesJMtaHqU\n" +
    		"uY0y/wubgjnq6cZ7B+sjdk2iiRQ3tGkywVkNXfWaebuAOLmtvQyYtUvaWjzp+UDa\n" +
    		"Qrlot8OM\n" +
    		"-----END PRIVATE KEY-----\n";

    private static final int D2C_MESSAGE_TIMEOUT = 2000; // 2 seconds
    private static List failedMessageListOnClose = new ArrayList(); // List of messages that failed on close

    /** Used as a counter in the message callback. */
    protected static class Counter
    {
        protected int num;

        public Counter(int num)
        {
            this.num = num;
        }

        public int get()
        {
            return this.num;
        }

        public void increment()
        {
            this.num++;
        }

        @Override
        public String toString()
        {
            return Integer.toString(this.num);
        }
    }

    // Our MQTT doesn't support abandon/reject, so we will only display the messaged received
    // from IoTHub and return COMPLETE
    protected static class MessageCallbackMqtt implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            Counter counter = (Counter) context;
            System.out.println(
                    "Received message " + counter.toString()
                            + " with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            for (MessageProperty messageProperty : msg.getProperties())
            {
                System.out.println(messageProperty.getName() + " : " + messageProperty.getValue());
            }

            counter.increment();

            return IotHubMessageResult.COMPLETE;
        }
    }

    protected static class EventCallback implements IotHubEventCallback{
        public void execute(IotHubStatusCode status, Object context){
          Message msg = (Message) context;
          System.out.println("IoT Hub responded to message "+ msg.getMessageId()  + " with status " + status.name());
          if (status==IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE)
          {
              failedMessageListOnClose.add(msg.getMessageId());
          }
        }
    }

    /**
     * Receives requests from an IoT Hub. Default protocol is to use
     * use MQTT transport.
     *
     * @param args 
     * args[0] = IoT Hub connection string
     * args[1] = number of requests to send
     * args[2] = protocol (optional, one of 'mqtt' or 'amqps' or 'https' or 'amqps_ws')
     */
    public static void main(String[] args) throws IOException, URISyntaxException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        if (args.length <= 1 && args.length >= 4)
        {
            System.out.format(
                    "Expected 2 or 3 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "2. [Device Id string]\n"
                            + "3. [number of requests to send]\n"
                            + "4. (mqtt | https | amqps | amqps_ws | mqtt_ws)\n",
                    args.length);
            return;
        }

        String connString = args[0];
        String deviceId = args[1];
        int numRequests;
        try
        {
            numRequests = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e)
        {
            System.out.format(
                    "Could not parse the number of requests to send. "
                            + "Expected an int but received:\n%s.\n", args[1]);
            return;
        }

        IotHubClientProtocol protocol;
        if (args.length == 3)
        {
            protocol = IotHubClientProtocol.MQTT;
        }
        else
        {
            String protocolStr = args[3].toLowerCase();
            if (protocolStr.equals("amqps"))
            {
                protocol = IotHubClientProtocol.AMQPS;
            }
            else if (protocolStr.equals("mqtt"))
            {
                protocol = IotHubClientProtocol.MQTT;
            }
            else
            {
                throw new UnsupportedOperationException("The protocol " + protocolStr + " does not support x509 authentication");
            }
        }

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n", protocol.name());

        DeviceClient client = new DeviceClient(connString, protocol, publicKeyCertificateString, false, privateKeyString, false);

        System.out.println("Successfully created an IoT Hub client.");

        MessageCallbackMqtt callback = new MessageCallbackMqtt();
        Counter counter = new Counter(0);
        client.setMessageCallback(callback, counter);

        System.out.println("Successfully set message callback.");

        client.open();

        System.out.println("Opened connection to IoT Hub.");

        System.out.println("Beginning to receive messages...");

        System.out.println("Sending the following event messages: ");

        double temperature = 0.0;
        double humidity = 0.0;
        String messageType = "critical";

        for (int i = 0; i < numRequests; ++i)
        {
        	
	        temperature = 20 + Math.random() * 10;
	        humidity = 30 + Math.random() * 20;
            
            String msgStr = "{\"deviceId\":\"" + deviceId +"\",\"messageId\":" + i + ",\"messageType\":\"" + messageType +"\",\"temperature\":"+ temperature +",\"humidity\":"+ humidity +"}";
            
            try
            {
                Message msg = new Message(msgStr);
                msg.setProperty("temperatureAlert", temperature > 28 ? "true" : "false");
                msg.setMessageId(java.util.UUID.randomUUID().toString());
                msg.setExpiryTime(D2C_MESSAGE_TIMEOUT);
                System.out.println(msgStr);
                EventCallback eventCallback = new EventCallback();
                client.sendEventAsync(msg, eventCallback, msg);
                //sleep 3 seconds
                Thread.sleep(3000);
            }  
  	        catch (InterruptedException e) {
  				e.printStackTrace();
  			}            
            catch (Exception e)
            {
                System.out.print(e.getMessage());
                e.printStackTrace(); // Trace the exception
            }
        }
        
         System.out.println("Wait for " + D2C_MESSAGE_TIMEOUT / 1000 + " second(s) for response from the IoT Hub...");
        
        // Wait for IoT Hub to respond.
        try
        {
          Thread.sleep(D2C_MESSAGE_TIMEOUT);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
        
        System.out.println("In receive mode. Waiting for receiving C2D messages. Press ENTER to close");
    
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        // close the connection        
        System.out.println("Closing"); 
        client.closeNow();
        
        if (!failedMessageListOnClose.isEmpty())
        {
            System.out.println("List of messages that were cancelled on close:" + failedMessageListOnClose.toString()); 
        }

        System.out.println("Shutting down...");
    }
}
