// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.nttdata.azure.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;

/**
 * Receive reboot
 *
 */
public class ReceiveReboot 
{

	private static final int METHOD_SUCCESS = 200;
	private static final int METHOD_NOT_DEFINED = 404;

	private static IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

	private static DeviceClient client;
	
	protected static class DirectMethodStatusCallback implements IotHubEventCallback
	{
	  public void execute(IotHubStatusCode status, Object context)
	  {
	    System.out.println("IoT Hub responded to device method operation with status " + status.name());
	  }
	}	
	
	protected static class DeviceTwinStatusCallback implements IotHubEventCallback
	{
	    public void execute(IotHubStatusCode status, Object context)
	    {
	        System.out.println("IoT Hub responded to device twin operation with status " + status.name());
	    }
	}
	
	protected static class PropertyCallback implements PropertyCallBack<String, String>
	{
	  public void PropertyCall(String propertyKey, String propertyValue, Object context)
	  {
	    System.out.println("PropertyKey:     " + propertyKey);
	    System.out.println("PropertyKvalue:  " + propertyKey);
	  }
	}
	
	protected static class RebootDeviceThread implements Runnable {
		  public void run() {
		    try {
		      System.out.println("Rebooting...");
		      Thread.sleep(5000);
		      Property property = new Property("lastReboot", LocalDateTime.now());
		      Set<Property> properties = new HashSet<Property>();
		      properties.add(property);
		      client.sendReportedProperties(properties);
		      System.out.println("Rebooted");
		    }
		    catch (Exception ex) {
		      System.out.println("Exception in reboot thread: " + ex.getMessage());
		    }
		  }
	}
	
	protected static class DirectMethodCallback implements com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback
	{
	  @Override
	  public DeviceMethodData call(String methodName, Object methodData, Object context)
	  {
	    DeviceMethodData deviceMethodData;
	    switch (methodName)
	    {
	      case "reboot" :
	      {
	        int status = METHOD_SUCCESS;
	        System.out.println("Received reboot request");
	        deviceMethodData = new DeviceMethodData(status, "Started reboot");
	        RebootDeviceThread rebootThread = new RebootDeviceThread();
	        Thread t = new Thread(rebootThread);
	        t.start();
	        break;
	      }
	      default:
	      {
	        int status = METHOD_NOT_DEFINED;
	        deviceMethodData = new DeviceMethodData(status, "Not defined direct method " + methodName);
	      }
	    }
	    return deviceMethodData;
	  }
	}
	
	public static void main(String[] args) throws IOException, URISyntaxException
    {
		
        if (args.length <= 1 || args.length >= 4)
        {
            System.out.format(
                    "Expected 3 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>\n"
                            + "2. [Device Id string]\n"
                            + "3. [Device Key string]\n",
                    args.length);
            return;
        }
		
        String connString = args[0];
        String deviceId = args[1]; //currently included in the connection string
        String deviceKey = args[2];//currently included in the connection string
        
		System.out.println("Starting device client sample...");
		client = new DeviceClient(connString, protocol);

		try
		{
		  client.open();
		  client.subscribeToDeviceMethod(new DirectMethodCallback(), null, new DirectMethodStatusCallback(), null);
		  client.startDeviceTwin(new DeviceTwinStatusCallback(), null, new PropertyCallback(), null);
		  System.out.println("Subscribed to direct methods and polling for reported properties. Waiting...");
		}
		catch (Exception e)
		{
		  System.out.println("On exception, shutting down \n" + " Cause: " + e.getCause() + " \n" +  e.getMessage());
		  client.close();
		  System.out.println("Shutting down...");
		}	
		
		System.out.println("Press any key to exit...");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
		client.close();
		System.out.println("Shutting down...");	

    }
}
