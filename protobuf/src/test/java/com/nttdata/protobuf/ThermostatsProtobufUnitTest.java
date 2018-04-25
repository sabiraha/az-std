package com.nttdata.protobuf;


import org.junit.After;
import org.junit.Test;

import com.nttdata.device.protobuf.ThermostatsProtos;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ThermostatsProtobufUnitTest {
    private final String filePath = "thermostats_device";

    @After
    public void cleanup() throws IOException {
        Files.deleteIfExists(Paths.get(filePath));
    }

    @Test
    public void givenGeneratedProtobufClass_whenCreateClass_thenShouldCreateJavaInstance() {
        //when
        double temperature = 20;
        double humidity = 60;
        ThermostatsProtos.DeviceTelemetryData telemetryData = ThermostatsProtos.DeviceTelemetryData.newBuilder()
        		.setTemperature(temperature)
        		.setHumidity(humidity)
        		.build();
    	
    	
        //then
        assertEquals(telemetryData.getTemperature(), temperature, temperature);
        assertEquals(telemetryData.getHumidity(), humidity, humidity);
        
    }


    @Test
    public void givenDeviceWithOneTelemetryData_whenSaveAsAFile_shouldLoadFromFileToJavaClass() throws IOException {
        //given
    	
        double temperature = 20;
        double humidity = 60;
        ThermostatsProtos.DeviceTelemetryData telemetryData = ThermostatsProtos.DeviceTelemetryData.newBuilder()
        		.setTemperature(temperature)
        		.setHumidity(humidity)
        		.build();
    	
        String deviceId = "thermostats-device-001";
        ThermostatsProtos.Device device =
        		ThermostatsProtos.Device.newBuilder()
                        .setDeviceId(deviceId)
                        .addTelemetryData(telemetryData)
                        .build();

        //when

        
        
        FileOutputStream fos = new FileOutputStream(filePath);
        device.writeTo(fos);     
        fos.close();

        //then
        FileInputStream fis = new FileInputStream(filePath);
        ThermostatsProtos.Device deserialized =
        		ThermostatsProtos.Device.newBuilder().mergeFrom(fis).build();
        fis.close();
        assertEquals(deserialized.getDeviceId(), deviceId);
        assertEquals(deserialized.getTelemetryData(0).getTemperature(), temperature, temperature);
        assertEquals(deserialized.getTelemetryData(0).getHumidity(), humidity, temperature);
        
        

    }
}
