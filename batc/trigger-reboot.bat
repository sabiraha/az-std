@echo off
color 5f
echo Cloud to Device Messages - Trigger Reboot from Azure IoT Hub
set /p id="<Device Id>"  
echo %id%
java -jar ../trigger-reboot/target/trigger-reboot-1.0-SNAPSHOT-with-deps.jar HostName=cat-poc-iothub.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=+mAVnrgxRgOLaWMR2+hP7lINJTy+VdKb4yuy7UbO4dA= %id%
pause 