package com.example.ds_22_05_19;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    /*
    Notes:
    - In order for app to work properly, IP file should be edited.
      Txt file are in src>assets folder
    - Review package name to match in order to execute.
    - Set different priority for more subs
    */
    Subscriber s = new Subscriber();
    Broker b = new Broker();

    // Map variables and settings
    private GoogleMap mMap;
    private MarkerOptions markerOptions;
    private ArrayList<Marker> markers = new ArrayList<>();
    // Text box to write bus line
    private EditText busLineET;
    // Button which initializes search
    private Button findBtn;
    private boolean cancel = false;
    private Button cancelBtn;
    List<Value> lastValues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

       // Retrieve devices IP and give random port
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        s.setIP(ip);
        s.setPort(new Random().nextInt(9999));

        // Read IP file from Assets for Broker node
        String [] ipInfo = readFromSD("IP.txt",1);
        b.setIP(ipInfo[0]);
        b.setPort(Integer.parseInt(ipInfo[1]));

        Toast.makeText(getApplicationContext(),"IP: "+s.getIP()+" Port: "+s.getPort(),Toast.LENGTH_LONG).show();

        // Map settings
        mMap = googleMap;
        markerOptions = new MarkerOptions();

        // Add a marker in AUEB and move the camera
        LatLng athens = new LatLng(37.994129, 23.731960);
        markerOptions.position(athens);
        markerOptions.title("AUEB");
        markerOptions.snippet("Starting point.");
        //mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(athens));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15),2000,null);


        busLineET = (EditText)findViewById(R.id.busLineET);
        findBtn = (Button)findViewById(R.id.findBusLineBtn);

        // Socket initialization
        findBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Hide button until process finish
                findBtn.setVisibility(View.INVISIBLE);
                // Get requested line
                String busLineToFind = busLineET.getText().toString();
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute(busLineToFind);


            }
        });

        cancelBtn = (Button)findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                cancel=true;
            }
        });

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            return;
        }
    }
    private class AsyncTaskRunner extends AsyncTask<String, List<Value>, List<Value>>{

        @Override
        // Establish connection
        protected List<Value> doInBackground(String... strings) {
            // Only executed the first time to receive broker list with bus lines
            if (!s.getRegisteredBrokers().contains(b)) {
                s.getRegisteredBrokers().add(b);
                s.socketGetBrokers(b);
            }
            // Access Subscriber methods - Send requests and receive values back
            s.setTopic(new Topic(strings[0]));
            Broker broker = new Broker(s.searchBrokers(strings[0]));
            if (broker.getIP() != null) {
                while(!cancel) {
                    try{
                        Thread.sleep(4000);
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                    s.socketSendSubscriberInfo(broker);
                    if (s.getListOfValues().get(0).getErrorMessage()!=null)break;
                    // No need to refresh map if data is the same
                    publishProgress(s.getListOfValues());
                }
            }
            return s.getListOfValues();

        }

        @Override
        protected void onProgressUpdate(List<Value>... values) {
            // If no data were found
            mMap.clear();
            for (Value v : values[0]) {
                if (v.getErrorMessage() == null) {
                    MarkerOptions mo = new MarkerOptions();
                    LatLng latLng = new LatLng(v.getLatitude(), v.getLongitude());
                    mo.position(latLng);
                    mo.title(v.getBus().getLineNumber() + " - " + v.getBus().getLineName());
                    mo.snippet(v.getBus().getTime());

                    // Add marker to map
                    mMap.addMarker(mo);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }

        @Override
        // Shows marker with details
        protected void onPostExecute(List<Value> valuesList) {
            for(Value value : valuesList) {
                // If value was sent without errors
                if (value.getErrorMessage() != null){
                    Toast.makeText(getApplicationContext(), value.toString(), Toast.LENGTH_SHORT).show();
                }
            }
            mMap.clear();
            findBtn.setVisibility(View.VISIBLE);
            cancel = false;
        }
    }

    public String [] readFromSD(String filename, int priority){
        BufferedReader br = null;
        String [] tokens = new String [2];
        try{
            br = new BufferedReader(new InputStreamReader(getAssets().open(filename),"UTF-8"));
            String line;
            int i = 0;
            while((line=br.readLine())!=null && i < priority){
                tokens = line.split(",");
                i++;
            }
            br.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return tokens;
    }
}
