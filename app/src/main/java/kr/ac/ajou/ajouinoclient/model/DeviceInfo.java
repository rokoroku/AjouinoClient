package kr.ac.ajou.ajouinoclient.model;


import java.util.HashMap;
import java.util.Map;

/**
 * DeviceInfo
 * to communicate between Ajouino server and device
 * and to create a device class in ajouino server.
 *
 * @author YoungRok
 */
public class DeviceInfo {

    protected String id;
    protected String type;
    protected String address;
    protected String label;
    protected Map<String, Integer> values;

    public DeviceInfo() {
    }

    public DeviceInfo(String id, String type, String address, String label) {
        this.id = id;
        this.type = type;
        this.address = address;
        this.label = label;
        this.values = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, Integer> getValues() {
        return values;
    }

    public void setValues(Map<String, Integer> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", address='" + address + '\'' +
                ", label='" + label + '\'' +
                ", values=" + values +
                '}';
    }
}
