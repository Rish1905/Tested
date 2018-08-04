package com.example.rishabh.shuttleuhome;

public class DisplayListviewUserDetails {

    public DisplayListviewUserDetails(String name, String address) {
        this.address = address;
        this.name = name;
    }

    private String address;

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

    private String name;
}
