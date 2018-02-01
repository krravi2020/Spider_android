package com.app.androidkt.mqtt;

/**
 * Created by ravikumar on 10/10/17.
 */

public class Device {
    public String device_id;
    public String device_type;
    public String device_manufacturer;
    public String label;
    public String value;
    public String time;
    public String deviceName;
    public String tempretureValue;
    public String humidityValue;
    public String burglarValue;
    public String LuminanceValue;
    public String typeOfDevice;
    public String status;

    public String getTypeOfDevice() {
        return typeOfDevice;
    }

    public void setTypeOfDevice(String typeOfDevice) {
        this.typeOfDevice = typeOfDevice;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTempretureValue() {
        return tempretureValue;
    }

    public void setTempretureValue(String tempretureValue) {
        this.tempretureValue = tempretureValue;
    }

    public String getHumidityValue() {
        return humidityValue;
    }

    public void setHumidityValue(String humidityValue) {
        this.humidityValue = humidityValue;
    }

    public String getBurglarValue() {
        return burglarValue;
    }

    public void setBurglarValue(String burglarValue) {
        this.burglarValue = burglarValue;
    }

    public String getLuminanceValue() {
        return LuminanceValue;
    }

    public void setLuminanceValue(String luminanceValue) {
        LuminanceValue = luminanceValue;
    }

    Device()
    {}

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getDevice_manufacturer() {
        return device_manufacturer;
    }

    public void setDevice_manufacturer(String device_manufacturer) {
        this.device_manufacturer = device_manufacturer;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
