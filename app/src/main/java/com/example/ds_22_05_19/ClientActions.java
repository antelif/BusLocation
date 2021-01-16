package com.example.ds_22_05_19;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClientActions extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;
    Broker broker;
    int type;
    Value value;
    String topic;
    boolean sent = true;// used for type 4

    // Used when Broker sends Broker list to other nodes
    public ClientActions(Socket connection, Broker broker,int type){
        try{
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            this.broker = new  Broker(broker);
            this.type = type;
        }catch(IOException e){
            System.err.println("IOException:");
            e.printStackTrace();
        }
    }

    // Used when Publisher sends data to Brokers
    public ClientActions(Socket connection, Value v, String t, int type){
        try{
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            this.value = v ;
            this.topic = t;
            this.type = type;
        }catch(IOException e){
            System.err.println("IOException:");
            e.printStackTrace();
        }
    }

    public ClientActions(Socket connection, int type){
        try{
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            this.type = type;

        }catch(IOException e){
            System.err.println("IOException:");
            e.printStackTrace();
        }
    }

    public ClientActions(Socket conection, Broker b, Value v, int type){

    }

    public List<Broker> getListOfBrokers(){return broker.getListOfBrokers();}
    public Broker getBroker(){return broker;}

    public void run() {
        // Brokers send list of brokers to Publishers
        if (type == 1) {
            try {
                out.writeUnshared((Broker) this.broker);
                out.flush();
                broker.setListOfBrokers((List<Broker>) in.readUnshared());

            } catch (IOException e) {
                System.err.println("IOException:");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();

            } finally {
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    System.err.println("IOException:");
                    e.printStackTrace();
                }
            }
        //  Broker send list of Brokers to Subscribers
        }else if(type==2){
            try {
                out.writeUnshared((Broker) this.broker);
                out.flush();
            } catch (IOException e) {
                System.err.println("IOException:");
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    System.err.println("IOException:");
                    e.printStackTrace();
                }
            }
        // Publisher sends data to Broker
        }else if(type ==3){
            try {
               // out.writeUTF((String) topic);
                out.writeUnshared((Value) value);
                out.flush();
            } catch (IOException e) {
                System.err.println("IOException:");
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    System.err.println("IOException:");
                    e.printStackTrace();
                }
            }
        // Subscriber sends Subscriber object with all requests to Broker
        }else if(type ==4){
        try {
            out.writeUnshared((Value)value);
        } catch (IOException e) {
            System.err.println("IOException:");
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                System.err.println("IOException:");
                e.printStackTrace();
            }
        }
    }

    }
}

