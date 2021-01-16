package com.example.ds_22_05_19;
import java.io.Serializable;

public class Topic implements Serializable{
    private static final long serialVersionUID = -4237749003476638384L;

    // Actual topic
    private String busLine;

    public Topic(String busLine){
        this.busLine = busLine;
    }

    String getbusLine(){
        return busLine;
    }
    public void setBusLine(String busLine){this.busLine=busLine;}

    public String toString(){
        return(busLine);
    }
}
