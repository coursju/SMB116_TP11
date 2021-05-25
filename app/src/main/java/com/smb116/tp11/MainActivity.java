package com.smb116.tp11;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String SERVICE_STATE = "service";
    private String DISCOVER_STATE = "discover";
    private Boolean serviceON = false;
    private Boolean discoverON = false;
    private TextView serviceTxt;
    private TextView discoveryTxt;
    private Bundle bundle;
    private DNSSDService dnssdService;
    private DNSSDDiscover dnssdDiscover;
    private String textViewDiscover = "";
    private String textViewService = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.bundle = savedInstanceState;
        setContentView(R.layout.activity_main);
        configureView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dnssdService = new DNSSDService(getApplicationContext(), this);
        dnssdDiscover = new DNSSDDiscover(getApplicationContext(), this);

        if (bundle != null){
            Log.i(TAG, "bundle not null");
            if (bundle.getBoolean(SERVICE_STATE)) {
                startServiceClick(null);
            }
            if (bundle.getBoolean(DISCOVER_STATE)){
                startDiscoveryClick(null);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (serviceON){
            dnssdService.stopService();
        }
        if (discoverON){
            dnssdDiscover.stopDiscovery();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(SERVICE_STATE, serviceON);
        outState.putBoolean(DISCOVER_STATE, discoverON);
        super.onSaveInstanceState(outState);
    }

    public void startServiceClick(View view){
        if (!serviceON) {
            textViewService = "";
            serviceON = true;
            dnssdService.startService();
        }
    }

    public void stopServiceClick(View view){
        if (serviceON) {
            serviceON = false;
            dnssdService.stopService();
        }
    }

    public void startDiscoveryClick(View view){
        if (!discoverON) {
            textViewDiscover = "";
            discoverON = true;
            dnssdDiscover.startDiscovery();
        }
    }

    public void stopDiscoveryClick(View view){
        if (discoverON) {
            discoverON = false;
            dnssdDiscover.stopDiscovery();
        }
    }

    public void sendMessageClick(View view){
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                dnssdDiscover.sendMessage();
            }
        });
    }

    public void closeClick(View view){
        finish();
    }

    public void configureView(){
        this.serviceTxt = findViewById(R.id.service_txt);
        this.discoveryTxt = findViewById(R.id.discovering_txt);
    }

    public void callServiceUI(String texte) {
        if (!textViewService.contains(texte)){
            textViewService += texte+"\n";
            serviceTxt.setText(textViewService);
        }
    }

    public void callDiscoverUI(String texte) {
        if (!textViewDiscover.contains(texte)){
            textViewDiscover += texte+"\n";
            discoveryTxt.setText(textViewDiscover);
        }
    }
}