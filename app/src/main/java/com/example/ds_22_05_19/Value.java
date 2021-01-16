package com.example.ds_22_05_19;

import java.io.Serializable;

public class Value implements Serializable {
    private static final long serialVersionUID = -4237749003476638384L;
    private Bus bus;
    private double latitude;
    private double longitude;
    private String errorMessage;

    // Constructor
    public Value(){}
    public Value(Bus bus){
        this.bus = bus;
        this.latitude = 0;
        this.longitude = 0;
        this.errorMessage = null;
    }

    // In case of exceeding time
    public Value(String message){
        errorMessage=message;
    }
    // Setters
    public void setLatitude(double latitude){
        this.latitude = latitude; }
    public void setLongitude(double longitude){
        this.longitude = longitude;
    }
    public void setBus(Bus bus){
        this.bus = bus;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    Bus getBus(){
        return bus;
    }
    double getLatitude(){
        return latitude;
    }
    double getLongitude(){
        return longitude;
    }
    public String getErrorMessage(){
        return errorMessage;
    }

    public String toString(){
        if (errorMessage == null){
            return (bus.getLineNumber() + " " + bus.getLineName() + ":\n" +
                    "Latitude: " + latitude +"\n"+
                    "Longitude: " + longitude +"\n"+
                    "Time: "+bus.getTime()+"\n");
        }else{
            return errorMessage;
        }

    }
}
