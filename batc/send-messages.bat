@echo off
@echo off
color 17
echo Send Device to Cloud Messages
set /p id="<Device Id>"  
echo %id%
set /p key="<Device Key>"
echo %key%
set /p req="<Number of Requests>"
echo %req%
set /p proto="<Protocol>"
echo %proto%
REM echo HostName=cat-poc-iothub.azure-devices.net;DeviceId=%id%;SharedAccessKey=%key% %id% %req% %proto%
java -jar ../send-receive/target/send-receive-1.10.0-with-deps.jar HostName=cat-poc-iothub.azure-devices.net;DeviceId=%id%;SharedAccessKey=%key% %id% %req% %proto%
pause 