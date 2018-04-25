@echo off
color 17
echo Cloud to Device Messages - Receive Reboot from Azure IoT Hub
set /p id="<Device Id>"  
echo %id%
set /p key="<Device Key>"
echo %key%
java -jar ../receive-reboot/target/receive-reboot-1.0-SNAPSHOT-with-deps.jar HostName=cat-poc-iothub.azure-devices.net;DeviceId=%id%;SharedAccessKey=%key% %id% %key%
pause 