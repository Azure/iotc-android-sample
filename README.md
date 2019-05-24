# Azure IoTCentral Bluetooth Android Gateway
This Android application will help you connect your BLE(Bluetooth-Low-Energy) device to Azure IoTCentral

## Features
* IoTCentral application management (list, creation)
* IoTCentral device management (list,creation)
* BLE discovery
* BLE Characteristic to IoTCentral telemetry mapping

## Binaries
APKs for installing on Android are available in the [Release page](https://github.com/Azure/iotc-android-sample/releases)


Application list</br>
<img title="Application list" src="https://github.com/Azure/iotc-android-sample/raw/master/images/Screenshot_20190411-135544.png" height="350"/></br>
Device Templates list</br>
<img title="Device Templates list" src="https://github.com/Azure/iotc-android-sample/raw/master/images/Screenshot_20190411-135600.png" height="350"/></br>
Device list</br>
<img title="Device list" src="https://github.com/Azure/iotc-android-sample/raw/master/images/Screenshot_20190411-135608.png" height="350"/></br>
BLE discovery list</br>
<img title="BLE discovery list" src="https://github.com/Azure/iotc-android-sample/raw/master/images/Screenshot_20190411-135631.png" height="350"/></br>
BLE Service discovery</br>
<img title="BLE Service discovery" src="https://github.com/Azure/iotc-android-sample/raw/master/images/Screenshot_20190411-135642.png" height="350"/></br>
BLE Service mapping</br>
<img title="BLE Service mapping" src="https://github.com/Azure/iotc-android-sample/raw/master/images/Screenshot_20190411-135651.png" height="350"/></br>
<img title="BLE Service mapping" src="https://github.com/Azure/iotc-android-sample/raw/master/images/Screenshot_20190411-135657.png" height="350"/></br>
<img title="BLE Service mapping" src="https://github.com/Azure/iotc-android-sample/raw/master/images/Screenshot_20190411-135705.png" height="350"/></br>


## Sync telemetry fields to BLE characteristic
It is possible to synchronize telemetry mapping with the cloud application in a bi-directional way (update from/to cloud). The device model must contain the following settings with the right names: 

* A device property of type _text_ with field name "ble_mapping"
* A device property of type _number_ with field name "ble_version"
* A command of type _text_ with field name "updMapping" and two input fields:
  * a field of type _text_ with name "mapping" which will contain the telemetry as a JSON string
  * a field of type _number_ with name "version" which will contain the mapping version to keep track on synchronization.
  
If operator wants to update mapping on the mobile application can run the "updMapping" command and the device will sync values on properties.

 
