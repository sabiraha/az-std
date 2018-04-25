@echo off
echo Device Provisioning with X509 Certificate
REM set /p publickey="<Public Key Certificate>"  
REM echo %publickey%
REM set /p privatekey="<Private Key>"
REM echo %privatekey%
java -jar ../device-provisioning-x509/target/device-provisioning-X509-1.0-SNAPSHOT-with-deps.jar
pause 