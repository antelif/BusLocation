package com.example.ds_22_05_19;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.lang.String;
import java.net.*; //Sockets

public class Publisher extends Thread implements Serializable{
    private static final long serialVersionUID = -4237749003476638384L;

    public static void main(String[] args) {
        /*
         ** args[0]: busLines.txt
         ** args[1]: busPositions.txt
         ** args[2]: RouteCodes.txt
         ** args[3]: PublisherIp.txt
         ** args[4]: IP.txt
         ** args[5]: Number of Publisher objects
         ** args[6]: priority of Publisher object
         */

        final String busLinesTXT = args[0];
        final String busPositionsTXT = args[1];
        final String RouteCodesTXT = args[2];
        final String PublisherIpTXT = args[3];
        final String IpTXT = args[4];
        final int numberOfPublishers = Integer.parseInt(args[5]);
        final int priority = Integer.parseInt(args[6]);

            Publisher p = new Publisher(4);
            int numberOfBusLines = p.countLines(RouteCodesTXT);
            p.calculateLines(args, numberOfBusLines);
            // Set this Publisher Ip and port
            String [] pubIP = p.readIpFile(PublisherIpTXT, priority);
            p.setIP(pubIP[0]);
            p.setPort(Integer.parseInt(pubIP[1]));
            p.setListOfBusInfo(RouteCodesTXT,busLinesTXT);
            // Read Ip and port for Broker to send information about hashes
            String [] brokerIP = p.readIpFile(IpTXT,1);
            Broker b = new Broker();
            b.setIP(brokerIP[0]);
            b.setPort(Integer.parseInt(brokerIP[1]));

            p.getRegisteredBrokers().add(b);

            // Print assigned bus lines
        System.out.println(p);
            System.out.println("Publisher responsible for bus lines : "+ p.getListOfBusInfo());

            // Get data from brokers
            new Publisher(p).start();
    }

    public void run(){
        // The Broker already provided from main
        socketGetBrokers(registeredBrokers.get(0));
        try {
            Thread.sleep( 5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Send all data to Brokers
        publisherToBroker();
    }

    /*  VARIABLES - Explanation
     ** listOfBusInfo: Contains every Bus object after reading the txt files. All possible routes.
     ** listOfValues: For each Bus object, a Value object is created, initialized using Constructor Value(Bus bus)
     ** frequency: Seconds that count how frequently publisher updates data.
     ** numberOfLines: Each Publisher object is assigned with a number of busLines.
     ** registeredBrokers: A list of Broker objects with their Hash and IP address, used to store retrieve information
     **                    while using sockets
     */
    private int numberOfLines;
    private int lineToReadFile;
    private List<Bus> listOfBusInfo;
    private List<Value> listOfValues;
    private int frequency;
    private List<Broker> registeredBrokers;
    private String IP;
    private int port;

    // Setters
    //public void setListOfBusInfo(List<Bus> bus){listOfBusInfo=bus; }
    public void setListOfValues(List<Value> values){listOfValues = values;}
    public void setFrequency(int frequency){this.frequency = frequency;}
    public void setRegisteredBrokers(List<Broker> listOfBrokers){
        // There is one broker, the one created in main - empty the list
        registeredBrokers.clear();
        for (Bus bus : listOfBusInfo){
            boolean registered = false;
            for(int i=0; i < listOfBrokers.size(); i++){
                if (!registeredBrokers.contains(listOfBrokers.get(i))){
                    registeredBrokers.add(listOfBrokers.get(i));
                }
                BigInteger busLineHash = new BigInteger(Hash(bus.getLineNumber()),16);
                // Bus line hash < Broker hash
                if (busLineHash.compareTo(registeredBrokers.get(i).getHashedIP())<0 && !registeredBrokers.get(i).getBusLines().contains(bus)){
                    // This Broker may be already in the list, and therefor only the actual value should be added and not
                    // the Broker object all over again.
                    registeredBrokers.get(i).getBusLines().add(bus);
                    registered = true;
                    break;
                }
            }
            if (registered==false){
                registeredBrokers.get(0).getBusLines().add(bus);
            }
        }
    }

    //Setters
    public void setIP(String IP){this.IP = IP;}
    public void setPort(int port){this.port = port;}
    public void setNumberOfLines(int numberOfLines){this.numberOfLines = numberOfLines;}
    public void setLineToReadFile(int line){lineToReadFile = line;}
    // Getters
    public List<Bus> getListOfBusInfo(){return listOfBusInfo;}
    public List<Value> getListOfValues(){return listOfValues;}
    public int getFrequency(){return frequency;}
    //public int getNumberOfLines(){return numberOfLines;}
    public List<Broker> getRegisteredBrokers(){return registeredBrokers;}
    public String getIP(){return IP;}
    public int getPort(){return port;}
    public int getLineToReadFile(){return lineToReadFile;}

    public String toString(){return ("Publisher with IP: "+IP+" and port: "+port);}


    // Constructors
    public Publisher(){
        this.listOfBusInfo = new ArrayList<>();
        this.listOfValues = new ArrayList<>();
        this.frequency = (int)((Math.random())*10);
        this.registeredBrokers = new ArrayList<>();
        this.IP = null;
        this.port = 0;
        this.lineToReadFile = 0;
    }
    // Constructor with frequency initialized
    public Publisher(int frequency){
        this.frequency=frequency;
        this.listOfBusInfo = new ArrayList<>();
        this.listOfValues = new ArrayList<>();
        this.registeredBrokers = new ArrayList<>();

    }
    // Used by Brokers
    public Publisher(String IP, int port){
        this.listOfBusInfo = new ArrayList<>();
        this.listOfValues = new ArrayList<>();
        this.frequency = (int)(((int)(Math.random()*10))*(Math.random()+1));
        this.registeredBrokers = new ArrayList<>();
        this.IP = IP;
        this.port = port;
        this.lineToReadFile = 0;
    }

    public Publisher(Publisher pub){
        this.listOfBusInfo = pub.getListOfBusInfo();
        this.listOfValues = pub.getListOfValues();
        this.frequency = pub.getFrequency();
        this.registeredBrokers = pub.getRegisteredBrokers();
        this.IP = pub.getIP();
        this.port = pub.getPort();
        this.lineToReadFile = pub.getLineToReadFile();
    }

    private int countLines(String routeCodesTXT){
        int counter = 0;
        try{
            BufferedReader br = new BufferedReader(new FileReader(routeCodesTXT));
            while ((br.readLine()) != null) {
                counter++;
            }
            br.close();
        }catch (IOException e) {
            System.err.println("IO Exception.\n");
        }
        return counter;
    }

    private void calculateLines(String [] args, int numberOfBusLines){

        int numberOfPublishers =  Integer.parseInt(args[5]);
        int thisPublisherPriority = Integer.parseInt(args[6]);
        int linesPerPublisher;
        int extraBusLines=0;


        /* If more publishers than busLines:    publishers = busLines
         ** If 0 publishers:                    publishers = busLines
         ** If less publishers than busLines:   modulo
         */
        if (numberOfPublishers==0 || numberOfBusLines <= numberOfPublishers){
            numberOfPublishers = numberOfBusLines;
            linesPerPublisher = numberOfBusLines / numberOfPublishers; // or linesPerPublisher=1;
        }
        else{
            linesPerPublisher = numberOfBusLines / numberOfPublishers;
            extraBusLines = numberOfBusLines % numberOfPublishers;
        }

        if(thisPublisherPriority > extraBusLines){
            setNumberOfLines(linesPerPublisher);
            setLineToReadFile((thisPublisherPriority-1)*linesPerPublisher+extraBusLines);
        }else{// First 2 pubs
            setNumberOfLines(linesPerPublisher+1);
            setLineToReadFile((thisPublisherPriority-1)*(linesPerPublisher+1));
        }
    }

    public String [] readIpFile(String PublisherIpTXT, int priority){
        String [] tokens = new String[2];
        try{
            BufferedReader br = new BufferedReader(new FileReader(PublisherIpTXT));
            String ipLine;
            int i=1;
            while((ipLine=br.readLine())!=null){
                if(i==priority){
                 tokens = ipLine.split(",");
                 break;
                }
                i++;
            }
            tokens = ipLine.split(",");
            br.close();
        }catch (IOException e) {
            System.err.println("IO Exception.\n");
        }
        return tokens;
    }

    // Hash function used to hash busLineNumber
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

    // Reads all 3 txt files and receives information about all active bus lines.
    void setListOfBusInfo(String routeCodesTXT, String busLinesTXT){

        try {
            // Creating new buffer to read txt files
            BufferedReader br = new BufferedReader(new FileReader(routeCodesTXT));
            String line;
            // Reach specific line
            for(int i = 0; i < lineToReadFile; i++){ br.readLine();}
            int j=0;
            while ((line = br.readLine()) != null && j < numberOfLines) {
                String[] tokens = line.split(",");
                Bus b = new Bus();
                // Route Code
                b.setRouteCode(tokens[0]);
                // Line Code
                b.setLineCode(tokens[1]);
                // Route Type
                b.setRouteType(Integer.parseInt(tokens[2]));
                // description
                b.setLineName(tokens[3]);

                listOfBusInfo.add(b);
                j++;
            }
            br.close();
            /* At this point all possible Bus objects for this Publisher object have benn created.
             ** Each Bus object contains RouteCode, LineCode, RouteType, and LineName
             */
            br = new BufferedReader(new FileReader(busLinesTXT));
            while((line = br.readLine()) != null){
                String [] tokens = line.split(",");
                for(Bus b : listOfBusInfo){
                    if(b.getLineCode().equals(tokens[0])){
                        b.setLineNumber(tokens[1]);
                    }
                }
            }
            // After reading busLines.txt file, every Bus object had LineNumber variable set.
            // For each Bus object created, a Value object is initialized and stored.
            for (Bus bus : listOfBusInfo){
                listOfValues.add(new Value(bus));
            }
            br.close();
        } catch (IOException e) {
            System.err.println("IO Exception.\n");
        }
    }

    /* Runnable implementation
    ** Each publisher updates the Bus objects they are acknowledged with in different frequency.
    */
    public void publisherToBroker(){
        String filename = "C:\\Users\\HP\\Desktop\\DS_22_05_19\\app\\src\\main\\assets\\busPositions.txt";
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");

               updateNode(tokens);
//                Topic t = new Topic(listOfBusInfo.get(indexOfValue).getLineNumber());
//                Value v = listOfValues.get(indexOfValue);
//
//                String[] IpPort = findIP(t);
//                String IP = IpPort[0];
//                int port = Integer.parseInt(IpPort[1]);
//
//                socketSendDataToBrokers(t, v, IP, port);
            }
        } catch (FileNotFoundException e) {
            System.err.println("FileNorFound Exception.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IOException Exception.");
            e.printStackTrace();
        }

    }

    private void updateNode(String [] tokens) {
        for (int i = 0; i < listOfBusInfo.size(); i++) {
            if (listOfBusInfo.get(i).getRouteCode().equals(tokens[1])) {
                listOfBusInfo.get(i).setVehicleId(tokens[2]);
                listOfBusInfo.get(i).setTime(tokens[5]);

                listOfValues.get(i).setLatitude(Double.parseDouble(tokens[3]));
                listOfValues.get(i).setLongitude(Double.parseDouble(tokens[4]));
                listOfValues.get(i).setBus(listOfBusInfo.get(i));

                Topic t = new Topic(listOfBusInfo.get(i).getLineNumber());
                Value v = listOfValues.get(i);

                String[] IpPort = findIP(t);
                String IP = IpPort[0];
                int port = Integer.parseInt(IpPort[1]);

                socketSendDataToBrokers(t, v, IP, port);
                break;
            }
        }
    }
    private String [] findIP(Topic topic){
        String [] ipPort = new String[2];
        boolean breakNow;
        for(int a = 0; a<registeredBrokers.size();a++){
            breakNow = false;
            for (int j=0; j< registeredBrokers.get(a).getBusLines().size();j++) {
                String bl = registeredBrokers.get(a).getBusLines().get(j).getLineNumber();
                String t=topic.getbusLine();
                if (bl.equals(t)) {
                    ipPort[0] = registeredBrokers.get(a).getIP();
                    ipPort[1] = String.valueOf(registeredBrokers.get(a).getPort());
                    breakNow=true;
                    break;
                }
            }
            if (breakNow){break;}
        }
        return ipPort;
    }

    public void socketSendDataToBrokers(Topic t, Value v, String IP, int port){
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            // Sleep
            try{
                Thread.sleep(frequency*1000);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            requestSocket = new Socket(InetAddress.getByName(IP), port);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            System.out.println("Sending data to IP: "+IP + " PORT: "+port);
            System.out.println(v);
            out.reset();
            out.writeUnshared((Topic) t);
            out.writeUnshared((Value) v);
            out.flush();
        } catch (UnknownHostException unknownHost) {
            System.err.println("UnknownHostException Exception.");
            unknownHost.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("IOException Exception.");
            ioException.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                System.err.println("IOException Exception.");
                ioException.printStackTrace();
            }
        }
    }

    public void socketGetBrokers(Broker b){

        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            requestSocket = new Socket(b.getIP(),b.getPort());
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            setRegisteredBrokers(((Broker)in.readUnshared()).getListOfBrokers());
            out.writeUnshared((List<Broker>)(registeredBrokers));
            out.flush();

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
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}
