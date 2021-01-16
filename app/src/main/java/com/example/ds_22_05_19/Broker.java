package com.example.ds_22_05_19;
import java.math.BigInteger;
import java.util.*;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;//Hash Function
import java.security.NoSuchAlgorithmException;//Hash Function

public class Broker extends Thread implements  Runnable,Serializable, Comparable<Broker> {
    private static final long serialVersionUID = -4237749003476638384L;

    public static void main(String[] args) {

        /* ARGUMENTS LIST
        ** args[0]: Ο αριθμός των Brokers.
        ** args[1]: IP.txt
        ** args[2]: Ο αριθμός της σειράς στο txt όπου βρίσκεται η IP διεύθυνση του συγκεκριμένου υπολογιστή.
        ** args[3]: PublisherIP.txt
        ** args[4]: Number of Publishers
        */

        // Broker b1 is responsible to send all initializing data and connect to Publishers
        Broker b1 = new Broker();
        b1.setType(1);
        b1.setBrokerPriority(Integer.parseInt(args[2]));
        b1.setNumberOfPublishers(Integer.parseInt(args[4]));
        b1.createHashes(args);

        System.out.println(b1);

        if (1==b1.priority) {
            // Send all Broker data to Publishers
            b1.socketGiveBrokersToPubs(b1.numberOfPublishers);
        }
        /*
        ** Each thread is assigned  with a task.
        ** Thread differ in 'type' variable, varying 1,2,3
        ** Thread t1 - type 1: receives all data from Publishers.
        ** Thread t2 - type 2: opens connection and sends data requested to Subscribers.
        ** Thread t3 - type 3: sends broker list to each Publisher connected at any time.
        */

        Broker b2 = new Broker(b1);
        b2.setType(2);
        Broker b3 = new Broker(b1);
        b3.setType(3);

        Thread t1 = new Broker(b1);
        Thread t2 = new Broker(b2);
        Thread t3 = new Broker(b3);


        t1.start();
        t2.start();
        t3.start();

        try{
            t1.join();
            t2.join();
            t3.join();
        }catch(Exception e){
            System.out.println("Interrupted.");
        }
        System.out.println("Threads finished their work.");
    }

    public void run(){
        // t1 receives all data from Publishers
        if(this.type==1){
            socketGetDataFromPubs();
        // t2 sends data to Subscribers
        }else if(this.type==2){
            while(true){
                socketSubscriberConnection();
            }
        // t3 sends broker list to subs
        }else if(type==3){
            while(true){
                if(priority==1){
                    socketGiveBrokersToSubs();
                }
            }
        }
    }
    // Variables
    // Stores values of busLines sent from Publishers
    private List<Value> listOfValuesReceived = new ArrayList<>();
    // Stores a list of all Broker IPs and ports
    private List<Broker> listOfBrokers = new ArrayList<>();
    // Stores a list of all Subscribers registered to this Broker
    private List<Subscriber> registeredSubscribers = new ArrayList<>();
    // Used from Broker 1, stores busLines for each Broker
    private List<Bus> busLines = new ArrayList<>();
    private BigInteger hashedIP;
    private String IP;
    private int port;
    private int priority;
    private int numberOfPublishers;
    private int type;// 1 connect to Pubs, 2 connect to Subs, 3 send broker to each new Sub
    private Socket connection;// used when sending data to subs
    // Constructor
    Broker(String IP, int port){
        this.IP=IP;
        this.port = port;
    }
    Broker(){}
    Broker(String IP,int port, BigInteger hashedIP){
        this.IP = IP;
        this.port = port;
        this.hashedIP = hashedIP;
    }
    Broker (Broker b){
        this.listOfValuesReceived=b.listOfValuesReceived;
        this.listOfBrokers = b.getListOfBrokers();
        this.registeredSubscribers = b.getRegisteredSubscribers();
        this.busLines = b.getBusLines();
        this.hashedIP =  b.getHashedIP();
        this.IP = b.getIP();
        this.port = b.getPort();
        this.priority=b.priority;
        this.numberOfPublishers = b.numberOfPublishers;
        this.type = b.type;
        this.connection=b.connection;
    }


    // Setters
    public void setListOfBrokers(List<Broker> listOfBrokers){this.listOfBrokers= listOfBrokers;}
    public void setHashedIP(BigInteger hash){this.hashedIP=hash; }
    public void setIP(String IP){this.IP = IP;}
    public void setPort(int port){this.port = port;}
    public void setBrokerPriority(int priority){this.priority = priority;}
    public void setNumberOfPublishers(int numberOfPublishers){this.numberOfPublishers = numberOfPublishers;}
    public void setType(int type){this.type = type;}

    // Getters
    public List<Broker> getListOfBrokers() {return listOfBrokers;}
    public List<Subscriber> getRegisteredSubscribers(){ return registeredSubscribers;}
    public BigInteger getHashedIP() {return hashedIP;}
    public String getIP(){return this.IP;}
    public int getPort(){return port;}
    public List<Bus> getBusLines(){return this.busLines;}
    public List<Value> getListOfValuesReceived(){return listOfValuesReceived;}

    public String toString(){return ("Broker with IP: "+IP+" and port: "+port);}

    public void createHashes(String [] args){
        try{
            BufferedReader br = new BufferedReader(new FileReader(args[1]));
            for(int i = 0; i < Integer.parseInt(args[0]); i++){
                String line = br.readLine();
                String token[] = line.split(",");
                // Convert String to BigInteger to enable modulo later
                BigInteger hash = new BigInteger(Hash(token[0] + token[1]),16);
                // Set this Broker IP
                if(Integer.parseInt(args[2])==i+1){
                    setIP(token[0]);
                    setPort(Integer.parseInt(token[1]));
                    setHashedIP(hash);
                }
                // Add each Broker to a list, in order to send to Publisher object when connecting
                listOfBrokers.add(new Broker(token[0],Integer.parseInt(token[1]), hash));
            }
            Collections.sort(listOfBrokers);
            br.close();
        }catch (IOException e) {
            System.err.println("IO Exception.");
            e.printStackTrace();
        }
    }

    // Hashing Function for IP + Port
    String Hash(String input){
        StringBuffer sb = new StringBuffer();
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA1");
            byte[] result = mDigest.digest(input.getBytes());

            for (int i = 0; i < result.length; i++) {
                sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
            }
        }catch (NoSuchAlgorithmException e){
            System.err.println("NoSuchAlgorithmException Exception.");
            e.printStackTrace();
        }

        return sb.toString();
    }
    // Comparable implementation
    public int compareTo(Broker broker){
        // Cast object to Broker object in order to be able to compare
        if(getHashedIP() == null || broker.getHashedIP() == null){
            return 0;
        }
        return getHashedIP().compareTo(broker.getHashedIP());
    }

    public void socketGiveBrokersToPubs(int numberOfPublishers){
        ServerSocket providerSocket = null;
        Socket connection = null;
        try { //kanoyme ena thread sleep gia na parei tis times to getListOfBrokers
            providerSocket = new ServerSocket(port);
           for(int i = 0; i < numberOfPublishers; i++) {
               connection = providerSocket.accept();
               Thread t = new ClientActions(connection, this, 1);
               t.start();
               try {
                   int frequency;
                   frequency = (int) ((Math.random() + 1) * 10);
                   Thread.sleep(frequency * 50);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               listOfBrokers = ((ClientActions) t).getListOfBrokers();
            }
        } catch (IOException ioException) {
            System.err.println("IO Exception.");
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                System.err.println("IO Exception.");
                ioException.printStackTrace();
            }
        }
    }


    // Gives Subscribers broker list
    public void socketGiveBrokersToSubs(){
        ServerSocket providerSocket = null;
        Socket connection = null;
        try { //kanoyme ena thread sleep gia na parei tis times to getListOfBrokers
            providerSocket = new ServerSocket(port+10);
            while(true){
                connection = providerSocket.accept();
                Thread t = new ClientActions(connection, this, 2);
                t.start();
                try {
                    int frequency;
                    frequency = (int) ((Math.random() + 1) * 10);
                    Thread.sleep(frequency * 50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
           }
        } catch (IOException ioException) {
            System.err.println("IO Exception.");
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                System.err.println("IO Exception.");
                ioException.printStackTrace();
            }
        }
    }
    // Receives data from Publisher
    public synchronized void socketGetDataFromPubs(){
        ServerSocket providerSocket = null;
        Socket connection = null;
       // int serverID;
        try {
            providerSocket = new ServerSocket(port);
            connection = providerSocket.accept();
            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

            while (true) {
                connection = providerSocket.accept();
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());


                Topic t = (Topic) in.readUnshared();
                Value v = (Value) in.readUnshared();

                updateListOfValues(v);
                System.out.println(v);
                in.close();
                out.close();
                connection.close();
            }
        } catch (IOException ioException) {
            System.err.println("IO Exception.");
            ioException.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException Exception.");
            e.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                System.err.println("IO Exception.");
                ioException.printStackTrace();
            }
        }
    }

    public synchronized void updateListOfValues(Value value) {
        boolean exists = false;
        for(int i=0; i<listOfValuesReceived.size();i++){
            if (listOfValuesReceived.get(i).getBus().getRouteCode().equals(value.getBus().getRouteCode())) {
                // Change values
                listOfValuesReceived.get(i).setLongitude(value.getLongitude());
                listOfValuesReceived.get(i).setLatitude(value.getLatitude());
                listOfValuesReceived.get(i).setBus(value.getBus());
                exists = true;
                break;
            }
        }
        if(exists==false){
            listOfValuesReceived.add(value);
        }
    }

    // Get Subscriber requests
    public synchronized void socketSubscriberConnection() {
        ServerSocket providerSocket = null;
        connection = null;
        try {
            providerSocket = new ServerSocket(port + 50);
            while (true) {
                this.connection = providerSocket.accept();
                Thread t = new Broker(this) {
                    public void run() {
                        try {
                            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());

                            // Add subscriber to registeredSubscriber list and extract topic requested
                            Subscriber s = (Subscriber) in.readUnshared();
                            updateSubList(s);
                            Topic topicRequested = s.getTopic();

                            // Keeps track of time - if more than 5 seconds a message is sent instead of value
                            long start = System.currentTimeMillis();
                            long end = start + 5*1000;

                            boolean sent = false;
                            // Sleep so that data is more synchronized
                            while (sent == false) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                // If listOfValuesReceived contains the topic requested, send it
                                List<Value> listOfValuesToSend = new ArrayList<>();
                                for (int j = 0; j < listOfValuesReceived.size(); j++) {
                                    if (System.currentTimeMillis() > end){
                                        String message = "No data were retrieved at the moment.\nPlease try again later.\n";
                                        Value v = new Value(message);
                                        List<Value>listError = new ArrayList<>();
                                        listError.add(v);
                                        out.writeUnshared((List<Value>)listError);
                                        sent = true;
                                        break;
                                    }

                                    if (listOfValuesReceived.get(j).getBus().getLineNumber().equals(topicRequested.getbusLine())) {
                                        listOfValuesToSend.add(listOfValuesReceived.get(j));
//                                        out.writeUnshared((Value) getListOfValuesReceived().get(j));
//                                        out.flush();
//                                        sent = true;
//                                        break;
                                    }

                                }
                                if(listOfValuesToSend.size()>0){
                                    out.writeUnshared((List<Value>)listOfValuesToSend);
                                    out.flush();
                                    sent = true;
                                }
                            }
                            try {
                                in.close();
                                out.close();
                            } catch (IOException e) {
                                System.err.println("IOException:");
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        } catch (IOException ioException) {
            System.err.println("IO Exception.");
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                System.err.println("IO Exception.");
                ioException.printStackTrace();
            }
        }
    }
    // Updates registered subscribers list
    public void updateSubList(Subscriber s){
        boolean exists = false;
        for(Subscriber sub : registeredSubscribers){
            if(sub.getIP().equals(s.getIP()) && sub.getPort()==s.getPort()){
                sub=s;
                exists=true;
            }
        }
        if(exists==false){
            registeredSubscribers.add(s);
        }
    }
}
