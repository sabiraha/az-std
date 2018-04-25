@echo off
color 5f
echo IoT Hub Login for Cloud to Device Messages
REM set /p id="<Device Id>"  
REM echo %id%
iothub-explorer login HostName=cat-poc-iothub.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=+mAVnrgxRgOLaWMR2+hP7lINJTy+VdKb4yuy7UbO4dA=
pause 