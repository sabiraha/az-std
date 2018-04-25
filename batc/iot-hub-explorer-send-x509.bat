@echo off
color 5f
echo Send Cloud to Device Messages
set /p id="<Device Id>"  
echo %id%
iothub-explorer send %id% "Testing message from IoT Hub to device"
pause 