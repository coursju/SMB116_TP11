package com.smb116.tp11;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class DNSSDService {

    private final String TAG = "DNSSDService";
    private final String SERVICE_NAME = "Deptinfo";
    private final String SERVICE_TYPE = "_http._tcp.";
    private Context context;
    private ServerSocket serverSocket;
    private int localPort;
    private InetAddress host;
    private NsdManager nsdManager;
    private NsdManager.RegistrationListener registrationListener;
    private String serviceName;
    private Thread serverThread;
    private Socket socket;
    private Boolean serverOn = true;
    private MainActivity mainActivity;

    public DNSSDService(Context context, MainActivity mainActivity){
        this.context = context;
        this.mainActivity = mainActivity;
    }

    public void startService(){
        try {
            serverOn = true;
            initializeServerSocket();
            getServerThread();
            initializeRegistrationListener();
            registerService(localPort);
            serverThread = getServerThread();
            serverThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopService(){
        serverOn = false;
        stopServer();
        nsdManager.unregisterService(registrationListener);
        showText("NSD Service stopped");
    }

    public void initializeServerSocket() throws IOException {
        serverSocket = new ServerSocket(0);
        localPort = serverSocket.getLocalPort();
        host = serverSocket.getInetAddress();
    }

    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                serviceName = NsdServiceInfo.getServiceName();
                showText("onServiceRegistered: "+serviceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                showText("onRegistrationFailed: "+serviceName+" error code: "+String.valueOf(errorCode));
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                showText("onServiceUnregistered: "+serviceName);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                showText("onUnregistrationFailed: "+serviceName+" error code: "+String.valueOf(errorCode));
            }
        };
    }


    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        nsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        showText("NSD Service started (port: "+String.valueOf(port)+")...");
    }

    public Thread getServerThread(){
        return new Thread() {
            @Override
            public void run() {
                Log.i(TAG, " into server thread ");
                showText("Service running...");
                try {
                    while (serverOn) {
                        String sMessage;
                        socket = serverSocket.accept();
                        InputStream in = socket.getInputStream();
                        byte[] b=new byte[7];
                        int n=in.read(b);
                        if(n>0){
                            sMessage=new String(b);
                            if (!sMessage.equals("close")) {
                                Log.i(TAG, sMessage);
                                showText("Message received from client: " + sMessage);
                            }
                        }
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "NSD server stopped!");
                showText("NSD server stopped...-");
            }
        };
    }

    public void showText(String texte){
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.callServiceUI(texte);
            }
        });
    }

    public void stopServer(){
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket stopSocket = new Socket(host, localPort);
                    OutputStream out = stopSocket.getOutputStream();
                    out.write("close".getBytes());
                    stopSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
