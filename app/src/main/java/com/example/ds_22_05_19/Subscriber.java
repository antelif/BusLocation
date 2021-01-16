package com.example.ds_22_05_19;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Subscriber extends Thread implements Runnable, Serializable {
    private static final long serialVersionUID = -4237749003476638384L;
	
    public static void main(String[] args) {
        /*
         ** args[0]: Number of Subscribers
         ** args[1]: SubIp.txt
         ** args[2]: Priority
         ** args[3]: IP.txt
         */

        final String subIpTXT = args[1];
        final String IpTXT = args[3];

        Subscriber s = new Subscriber();

        // Set this Subscriber Ip and port
       String [] subIp = s.readIpFile(subIpTXT,Integer.parseInt(args[2]));
       s.setIP(subIp[0]);
       s.setPort(Integer.parseInt(subIp[1]));
       // Read Ip and port for Broker to send information abouts busLines
        Broker b = new Broker();
        String [] brokerIp = s.readIpFile(IpTXT,1);
        b.setIP((brokerIp[0]));
        b.setPort(Integer.parseInt(brokerIp[1]));

        s.getRegisteredBrokers().add(b);

        new Subscriber(s).start();
    }
    public void run(){
        Scanner in = new Scanner(System.in);
        socketGetBrokers(registeredBrokers.get(0));
        String requestedBusLine = "default";
        System.out.println("Insert bus line:\n[type 'exit' to terminate program]");
        while (!(requestedBusLine=in.nextLine()).equals("exit")){
            //requestedBusLine = in.nextLine();
            setTopic(new Topic(requestedBusLine));

            Broker b = new Broker(searchBrokers(requestedBusLine));
            // Check if broker is assigned. If not wrong busLine was given
            if (b.getIP()!=null) {
                socketSendSubscriberInfo(b);
                System.out.println("Insert bus line:\n[type 'exit' to terminate program]");
            }else{
                System.out.println("Wrong bus line. Please give valid bus line.");
            }

        }
        System.out.print("Exiting...");
    }

    //Variables
    private List<Broker> registeredBrokers;
    private Topic topic;
    private String IP;
    private int port;
    private List<Value> listOfValues;

    public Subscriber(){
        registeredBrokers =  new ArrayList<>();
        listOfValues = new ArrayList<>();
       }

    Subscriber(Subscriber s){
        this.registeredBrokers =  s.registeredBrokers;
        this.topic = s.topic;
        this.IP= s.IP;
        this.port = s.port;
        this.listOfValues = s.listOfValues;
    }
    public Subscriber(String IP, int port){
        this.IP= IP;
        this.port = port;
        registeredBrokers = new ArrayList<>();
        listOfValues = new ArrayList<>();
    }

    // Setters
    public void setRegisteredBrokers(List<Broker> registeredBrokers){
        this.registeredBrokers=registeredBrokers;
    }
    public void setIP(String IP){this.IP = IP;}
    public void setPort(int port){this.port = port;}
    public void setTopic(Topic topic){this.topic = topic;}
    public void setListOfValues(List<Value> list){
        listOfValues.clear();
        listOfValues=list;
    }

    // Getters
    public String getIP(){
        return IP;
    }
    public int getPort(){
        return port;
    }
    public List<Broker> getRegisteredBrokers(){
        return  registeredBrokers;
    }
    public Topic getTopic(){return topic;}
    public List<Value> getListOfValues(){return listOfValues;}

    public String [] readIpFile(String TXT, int priority){
        String [] tokens = new String [2];
        try{
            BufferedReader br = new BufferedReader(new FileReader(TXT));
            String line;
            int p = 0;
            while((line=br.readLine())!=null){
                p++;
                if(p==priority){
                    tokens = line.split(",");
                    break;
                }
            }
            br.close();
        }catch (IOException e) {
            System.err.println("IO Exception.\n");
        }
        return tokens;
    }

    // Find which broker has this bus line
    public Broker searchBrokers(String busLine){
        boolean breakNow = false;
        Broker broker = new Broker();
        for(int i = 0; i < registeredBrokers.size(); i++){
            if (breakNow) break;
            for(int j =0; j< registeredBrokers.get(i).getBusLines().size(); j++ ){
                if (registeredBrokers.get(i).getBusLines().get(j).getLineNumber().equals(busLine)){
                    broker=registeredBrokers.get(i);
                    breakNow = true;
                    break;
                }
            }
        }
        if(broker.getIP()==null){
            listOfValues.clear();
            listOfValues.add(new Value("Wrong line"));}
        return broker;
    }
    public void socketGetBrokers(Broker b){

        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            requestSocket = new Socket(b.getIP(),b.getPort()+10);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            setRegisteredBrokers(((Broker)in.readUnshared()).getListOfBrokers());
//            for(int i = 0; i < registeredBrokers.size(); i++){
//                System.out.println("Broker " + (i+1) + " is responsible for : " + registeredBrokers.get(i).getBusLines());
//            }
        }catch(ClassNotFoundException e) {
            System.err.println("Unknown class exception.");
            e.printStackTrace();

        }catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
            unknownHost.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                in.close(); out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    // Send Subscriber object to each Broker to be added in RegisteredSubscribersList of Broker object
    public synchronized void socketSendSubscriberInfo(Broker b){
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            requestSocket = new Socket(b.getIP(), b.getPort()+50);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            out.writeUnshared((Subscriber) this);
            out.flush();
            listOfValues = (List<Value>)in.readUnshared();
             //Check if error message
            for(Value v : listOfValues){
                if(v.getErrorMessage()!=null){
                    System.out.println(v.getErrorMessage());
                }else{
                    System.out.println(v);
                }
            }

            in.close();
            out.close();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
            unknownHost.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
