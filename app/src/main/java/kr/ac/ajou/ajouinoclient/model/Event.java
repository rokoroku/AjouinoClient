package kr.ac.ajou.ajouinoclient.model;

import java.util.Date;

/**
 * Device's event
 */
public class Event implements Comparable {

    public static final String TYPE_DIGITAL = "digital";
    public static final String TYPE_ANALOG = "analog";
    public static final String TYPE_COLOR = "color";
    public static final String TYPE_GUEST = "guest";
    public static final String TYPE_POWER = "power";

    String deviceID;
    String type;
    Integer value;
    Date timestamp;

    public Event() {
    }

    public Event(String deviceID, String type, int value) {
        this.deviceID = deviceID;
        this.type = type;
        this.value = value;
        this.timestamp = new Date();
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(Object o) {
        return ((Event)o).getTimestamp().compareTo(timestamp);
    }
}
