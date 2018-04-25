color 5f
@echo off
echo Send Cloud to Device Messages
set /p id="<Device Id>"  
echo %id%
iothub-explorer send %id% "Testing message from IoT Hub to device"
pause 