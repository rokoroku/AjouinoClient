package kr.ac.ajou.ajouinoclient.util;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import kr.ac.ajou.ajouinoclient.model.Device;

/**
 * Created by YoungRok on 2014-11-26.
 */
public class DeviceManager {
    private static DeviceManager mInstance;

    private Map<String, Device> deviceTable;

    private DeviceManager() {
        deviceTable = new Hashtable<>();
    }

    public static DeviceManager getInstance() {
        if (mInstance == null) mInstance = new DeviceManager();
        return mInstance;
    }

    public Device getDevice(String deviceId) {
        return deviceTable.get(deviceId);
    }

    public Collection<Device> getDevices() {
        return deviceTable.values();
    }

    public void putDevice(Device device) {
        deviceTable.put(device.getId(), device);
    }

    public void putDevices(Collection<Device> devices) {
        for (Device device : devices) {
            deviceTable.put(device.getId(), device);
        }
    }

    public Device removeDevice(String deviceId) {
        return deviceTable.remove(deviceId);
    }

}
