@echo off
echo Device Bulk Identity Registration
java -jar ../device-bulk-identity-registration/target/device-identity-bulk-registration-1.0-SNAPSHOT-with-deps.jar HostName=cat-poc-iothub.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=+mAVnrgxRgOLaWMR2+hP7lINJTy+VdKb4yuy7UbO4dA= thermostats-device-001,thermostats-device-002
pause 