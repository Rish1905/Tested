package com.example.rishabh.shuttleuhome;

public class DisplayListviewUserDetails {

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String address;
    private String name;
    private String date;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSUID() {
        return SUID;
    }

    public void setSUID(String SUID) {
        this.SUID = SUID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String SUID;
    private String time;

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    private String UID;

    public DisplayListviewUserDetails(String address, String name, String date, String SUID, String time, String UID) {
        this.address = address;
        this.name = name;
        this.date = date;
        this.SUID = SUID;
        this.time = time;
        this.UID = UID;
    }
}
