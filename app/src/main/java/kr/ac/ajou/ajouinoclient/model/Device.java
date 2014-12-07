package kr.ac.ajou.ajouinoclient.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Device
 */
public class Device extends DeviceInfo {

    public final static String TYPE_LAMP = "lamp";
    public final static String TYPE_POWERSTRIP = "powerstrip";
    public final static String TYPE_INTERCOM = "intercom";
    public final static String TYPE_ARDUINO = "arduino";

    String password;
    List<Event> events;
    Date createDate;
    Date lastSyncDate;
    boolean available;

    public Device() {
    }

    public Device(DeviceInfo deviceInfo) {
        super(deviceInfo.getId(), deviceInfo.getType(), deviceInfo.getAddress(), deviceInfo.getLabel());
    }

    public Device(String deviceID, String type, String address, String label) {
        super(deviceID, type, address, label);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        Collections.sort(events);
        this.events = events;
    }

    public void addEvent(Event event) {
        if(events == null) events = new ArrayList<>();
        events.add(event);
        Collections.sort(events);
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastSyncDate() {
        return lastSyncDate;
    }

    public void setLastSyncDate(Date lastSyncDate) {
        this.lastSyncDate = lastSyncDate;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Device{" +
                "password='" + password + '\'' +
                ", events=" + events +
                ", createDate=" + createDate +
                ", lastSyncDate=" + lastSyncDate +
                ", available=" + available +
                '}';
    }
}
