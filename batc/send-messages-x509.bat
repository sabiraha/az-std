color 17
@echo off
echo Send Device to Cloud Messages with X509 Self Signed Certificate
set /p id="<Device Id>"  
echo %id%
set /p req="<Number of Requests>"
echo %req%
set /p proto="<Protocol>"
echo %proto%
java -jar ../send-receive-x509/target/send-receive-x509-1.10.0-with-deps.jar HostName=cat-poc-iothub.azure-devices.net;DeviceId=%id%;x509=true %id% %req% %proto%
pause 