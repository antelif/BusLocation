package com.example.ds_22_05_19;
import java.io.Serializable;

public class Bus implements Serializable {
    private static final long serialVersionUID = -4237749003476638384L;
    private String lineNumber;
    private String lineName;
    private String vehicleId;
    private String lineCode;
    private String routeCode;
    private int routeType;
    private String time;

    /* VARIABLE EXAMPLE - Clarification
    ** lineNumber:  [TOPIC] 550
    ** lineName:    Kifisia-Faliro OR Faliro-Kifisia
    ** vehicleId:   bus id
    ** lineCode:    1100, is unique for line 550, either it is Kifisia-Faliro or Faliro-Kifisia
    ** routeCode:   1101, if it is Kifisia-Faliro or 1102 if it is Faliro-Kifisia
    ** routeType:   1 if it is from start to end of line, Kifisia-Faliro
    **              2 if it is from end to start of line, Faliro-Kifisia
    ** latitude:    den 3erw pws na to perigra4w sta agglika :)
    ** longitude:   same :)
    ** time:        Further description, concerning time of arrival

    */

    public Bus(){
      lineNumber="";
      lineName="";
      vehicleId="";
      lineName="";
      routeCode="";
      routeType=0;
      time = null;
    }
    public Bus(Bus bus){
        this.lineNumber = bus.lineNumber;
        this.lineName = bus.lineName;
        this.vehicleId = bus.vehicleId;
        this.lineCode = bus.lineCode;
        this.routeType = bus.routeType;
        this.routeCode = bus.routeCode;
        this.time = bus.time;
    }
    // Setters
    public void setLineNumber(String lineNumber){
        this.lineNumber=lineNumber;
    }
    public void setLineName(String lineName){
        this.lineName=lineName;
    }
    public void setVehicleId(String vehicleId){
        this.vehicleId=vehicleId;
    }
    public void setLineCode(String lineCode){
        this.lineCode=lineCode;
    }
    public void setRouteType(int routeType){ this.routeType = routeType; }
//    public void setLatitude(double l){ latitude.add(l); }
//    public void setLongitude(double l){
//        longitude.add(l);
//    }
    public void setTime(String time){
        this.time = time;
    }
    public void setRouteCode(String routeCode){
        this.routeCode=routeCode;
    }

    // Getters
    public String getLineNumber(){
        return lineNumber;
    }
    public String getLineName(){
        return lineName;
    }
    public String getVehicleId(){
        return vehicleId;
    }
    public String getLineCode(){
        return lineCode;
    }
    public String getRouteCode(){ return routeCode; }
    public int getRouteType(){return routeType;}
    public String getTime(){
        return time;
    }
    public String toString(){
        return(lineNumber);
    }
}
