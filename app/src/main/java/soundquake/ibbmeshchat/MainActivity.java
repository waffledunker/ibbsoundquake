package soundquake.ibbmeshchat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import soundquake.ibbmeshchat.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends PermissionsActivity
{
    
    private static final String TAG = MainActivity.class.getName();
    
    
    @Override
    void onPermissionsOkay()
    {
    
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SharedPreferences userPref = getSharedPreferences("user", 0);
        final SharedPreferences.Editor userPrefEditor = userPref.edit();
        
        
        Button startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
//                runAsRoot(restoreBackupAp());

//                String username = usernameInput.getText().toString();
//                if (username.isEmpty()) {
//                    Toast.makeText(getApplicationContext(), "Enter a username.", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                userPrefEditor.putString("username", "IBB Haberleşme Ağı");
//                userPrefEditor.commit();
//                Intent myIntent = new Intent(getApplicationContext(), ChatActivity.class);
//                startActivity(myIntent);
                String ip1 = Utils.getIPAddress(true);
                //ip gets "" otherwise
                
                runAsRoot(getRestoreIptables());
            }
        });
        
        Button flushBtn = (Button) findViewById(R.id.flushBtn);
        flushBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                runAsRoot(getFlushIptables());
            }
        });
        
        Button hotspotBtn = (Button) findViewById(R.id.hotspotBtn);
        hotspotBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                runAsRoot(saveBackupAp());
            
//                MagicActivity.useMagicActivityToTurnOn(getApplicationContext());
    
                Intent intent = new Intent(getString(R.string.intent_action_turnon));
                sendImplicitBroadcast(getApplicationContext(), intent);
    
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        userPrefEditor.putString("username", "IBB Haberleşme Ağı");
                        userPrefEditor.commit();
                        Intent myIntent = new Intent(getApplicationContext(), ChatActivity.class);
                        startActivity(myIntent);
                        
                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                runAsRoot(getConfiguredIptables2());
                            }
                        }, 2000);
                    }
                }, 3000);
            }
        });
        
        
        canRunRootCommands();


//        try {
//            Log.i(TAG, "Trying to load libnativetask.so");
//            System.loadLibrary("nativetask");
//        } catch (UnsatisfiedLinkError ule) {
//            Log.e(TAG, "Could not load libnativetask.so", ule);
//        }


//        userPrefEditor.putString("username", "IBB Haberleşme Ağı");
//        userPrefEditor.commit();
//        Intent myIntent = new Intent(getApplicationContext(), ChatActivity.class);
//        startActivity(myIntent);
    }
    
    public static boolean canRunRootCommands()
    {
        boolean retval;
        boolean exitSu;
        retval = false;
        try
        {
            Process suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
            DataInputStream osRes = new DataInputStream(suProcess.getInputStream());
            if (!(os == null || osRes == null))
            {
                os.writeBytes("id\n");
                os.flush();
                String currUid = osRes.readLine();
                if (currUid == null)
                {
                    retval = false;
                    exitSu = false;
                    Log.d(TAG, "Can't get root access or denied by user");
                }
                else if (currUid.contains("uid=0"))
                {
                    retval = true;
                    exitSu = true;
                    Log.d(TAG, "Root access granted");
                }
                else
                {
                    retval = false;
                    exitSu = true;
                    Log.d(TAG, "Root access rejected: " + currUid);
                }
                if (exitSu)
                {
                    os.writeBytes("exit\n");
                    os.flush();
                }
            }
        }
        catch (Exception e)
        {
            retval = false;
            Log.d(TAG, "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
        }
        return retval;
    }
    
    
    public void onClickTurnOnAction(View v)
    {
        Intent intent = new Intent(getString(R.string.intent_action_turnon));
        sendImplicitBroadcast(this, intent);
    }
    
    private static void sendImplicitBroadcast(Context ctxt, Intent i)
    {
        PackageManager pm = ctxt.getPackageManager();
        List<ResolveInfo> matches = pm.queryBroadcastReceivers(i, 0);
        
        for (ResolveInfo resolveInfo : matches)
        {
            Intent explicit = new Intent(i);
            ComponentName cn =
                    new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                            resolveInfo.activityInfo.name);
            
            explicit.setComponent(cn);
            ctxt.sendBroadcast(explicit);
        }
    }
    
    public void runAsRoot(String[] cmds)
    {
        try
        {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : cmds)
            {
                os.writeBytes(tmpCmd + "\n");
                os.flush();
            }
            os.writeBytes("exit\n");
            os.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public String[] getConfiguredIptables()
    {
        String[] cmd = {
                "iptables -A FORWARD -i wlan0 -p tcp --dport 53 -j ACCEPT",
                "iptables -A FORWARD -i wlan0 -p udp --dport 53 -j ACCEPT",
                "iptables -A FORWARD -i wlan0 -p tcp --dport 8080 -d " + Utils.getIPAddress(true) + " -j ACCEPT",
                "iptables -A FORWARD -i wlan0 -j DROP",
                "iptables -t nat -A PREROUTING -i wlan0 -p tcp --dport 80 -j DNAT --to-destination " + Utils.getIPAddress(true) + ":8080"
        };
        
        return cmd;
    }
    
    public String[] getConfiguredIptables2()
    {
        String[] cmd = {
                "iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080",
                "iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 8080",

//                "iptables -t nat -A PREROUTING -p udp --dport 80 -j REDIRECT --to-port 8080",
//                "iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 8080",
        
                "iptables -A FORWARD -i wlan0 -p tcp --dport 53 -j ACCEPT",
                "iptables -A FORWARD -i wlan0 -p udp --dport 53 -j ACCEPT",
//                "iptables -t nat -A PREROUTING -i wlan0 -p tcp -d 0.0.0.0/0 --dport 443 -j DNAT --to-destination " + Utils.getIPAddress(true) + ":8080"
//                "iptables -A FORWARD -i wlan0 -j DROP",
//                "iptables -t nat -A PREROUTING -i wlan0 -p tcp --dport 443 -j DNAT --to-destination " + Utils.getIPAddress(true) + ":8080",
//                "iptables -t nat -A POSTROUTING -o wlan0 -j MASQUERADE"
        };
        
        return cmd;
    }
    
    public String[] getFlushIptables()
    {
        String[] cmd = {
                "iptables -P INPUT ACCEPT",
                "iptables -P FORWARD ACCEPT",
                "iptables -P OUTPUT ACCEPT",
                "iptables -t nat -F",
                "iptables -t nat -X",
                "iptables -t mangle -F",
                "iptables -t mangle -X",
                "iptables -F",
                "iptables -X"
        };
        return cmd;
    }
    
    public String[] getRestoreIptables()
    {
        String[] cmd = {
                "iptables-restore < /storage/emulated/0/savediptablesmodified3.txt"
        };
        return cmd;
    }
    
    public String[] saveBackupAp()
    {
        String[] cmd = {
                "cp /data/misc/wifi/softap.conf /data/misc/wifi/softap.conf.backup",
                "cp /data/misc/wifi/hostapd.conf /data/misc/wifi/hostapd.conf.backup"
        };
        return cmd;
    }
    
//    public String[] restoreBackupAp()
//    {
//        String[] cmd = {
//                "mv /data/misc/wifi/softap.conf.backup data/misc/wifi/softap.conf",
//                "mv /data/misc/wifi/hostapd.conf.backup /data/misc/wifi/hostapd.conf"
//        };
//        return cmd;
//    }
    
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
   
    
    
}
