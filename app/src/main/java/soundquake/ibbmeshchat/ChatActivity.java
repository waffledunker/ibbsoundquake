package soundquake.ibbmeshchat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import soundquake.ibbmeshchat.WebService.IRest;
import soundquake.ibbmeshchat.WebService.ServiceGenerator;

public class ChatActivity extends AppCompatActivity {

    public MessageAdapter adaptador;
    EditText messageInput;
    Button sendBtn;
    ListView lista;
    String ip;
    WebServer webserver;
    WebsocketServer websocketserver;
    public IRest webService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // Get username
        SharedPreferences user = getSharedPreferences("user", 0);
        final String username = user.getString("username", "");
        // Initialize variables
        lista = (ListView) findViewById(R.id.messages_view);
        adaptador = new MessageAdapter(new ArrayList<Message>(), getApplicationContext());
        lista.setAdapter(adaptador);
        messageInput = (EditText) findViewById(R.id.msgInput);
        // Add button behavior
        sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = messageInput.getText().toString();
                if (content.isEmpty()) {
                    return;
                }
                Message msg = new Message(username, new Date(), content, 0, 0L, System.currentTimeMillis(), getCurrentLocationLatLngStr());
                websocketserver.sendMessage(msg);
            }
        });
        
        webService = ServiceGenerator.createService(IRest.class);
        // Main function
        enableChat(false);
        serverMessage("Initializing server...");
        // Init server
//        Utils utils = new Utils(getApplicationContext());
//        ip = utils.getIP();
        ip = Utils.getIPAddress(true);
        if (ip == null || ip.equals("")) {
            serverMessage("Couldn't start server on hotspot");
            return;
        }
        webserver = new WebServer(8080, getAssets()); // Change port to 80 if possible
        websocketserver = new WebsocketServer(3000, this);
        try {
            webserver.start();
            websocketserver.start();
            serverMessage("Listening on " + ip + ":8080");
            enableChat(true);
        }catch (IOException e) {
            Toast.makeText(getApplicationContext(), "IOException: " +  e.getMessage(), Toast.LENGTH_LONG).show();
        }catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Exception: " +  e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void addMessage(Message msg) {
        final Message message = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adaptador.add(message);
                adaptador.notifyDataSetChanged();
                messageInput.setText("");
                lista.setSelection(adaptador.getCount()-1);
            }
        });
    }

    private void serverMessage(String content) {
        addMessage(new Message("Server", new Date(), content, 0, 0L, System.currentTimeMillis(), getCurrentLocationLatLngStr()));
    }

    private void enableChat(Boolean enable) {
        sendBtn.setEnabled(enable);
        messageInput.setEnabled(enable);
    }

    public MessageAdapter getAdapter() {
        return this.adaptador;
    }

    public ListView getList() {
        return this.lista;
    }

    public void onDestroy() {
        try {
            this.webserver.closeAllConnections();
            this.webserver.stop();
            this.websocketserver.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
    
    public Location getCurrentLocation()
    {
        LocationManager locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }
        Location location = locationManager.getLastKnownLocation(bestProvider);
        return location;
    }
    
    public String getCurrentLocationLatLngStr()
    {
        LocationManager locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return null;
        }
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null)
        {
            return location.getLatitude() + ", " + location.getLongitude();
        }
        
        return "";
    }
    

}
