package soundquake.ibbmeshchat;

import android.content.Context;
import android.location.Location;

import com.google.gson.Gson;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.channels.NotYetConnectedException;
import java.util.HashSet;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebsocketServer extends WebSocketServer {

    private static int TCP_PORT = 4444;

    private Set<WebSocket> conns;
    private Context context;
    private ChatActivity activity;

    public WebsocketServer(ChatActivity activity) {
        super(new InetSocketAddress(TCP_PORT));
        conns = new HashSet<>();
        this.activity = activity;
    }

    public WebsocketServer(int port, ChatActivity activity) {
        super(new InetSocketAddress(port));
        TCP_PORT = port;
        conns = new HashSet<>();
        this.activity = activity;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conns.add(conn);
        Gson gson = new Gson();
        for (Message msg : activity.adaptador.getMessageList()){
            String str = gson.toJson(msg.toMessageReciever());
            try
            {
                conn.send(str);
            }
            catch (Exception e)
            {
            }
        }
        
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        conns.remove(conn);
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from client: " + message);
        Gson gson = new Gson();
        Message msg = gson.fromJson(message, MessageReciver.class).toMessage(activity);
        addMessage(msg);
        double defaultLat = 41.23119;
        double defaultLon = 29.13791;
        if (activity.getCurrentLocation() != null)
        {
            Location currentLocation = activity.getCurrentLocation();
            defaultLat = currentLocation.getLatitude();
            defaultLon = currentLocation.getLongitude();
        }
        MessageRequestObject body = new MessageRequestObject(msg.username, msg.data, defaultLat, defaultLon, msg.messageId, System.currentTimeMillis(), msg.userId, msg.vstate);
        this.activity.webService.putMessage("", body).enqueue(new Callback<ResponseBody>()
        {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response)
            {
            
            }
    
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t)
            {
        
            }
        });
        // TODO: Do something with the message
        for (WebSocket sock : conns) {
            sock.send(message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        //ex.printStackTrace();
        if (conn != null) {
            System.out.println("ERROR from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
            conns.remove(conn);
            // do some thing if required
        }
        System.out.println("Websocket error: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        // TODO: put some code in this method
    }

    public void sendMessage(Message msg) {
        System.out.println("Message sent from server: " + msg.data);
        Gson gson = new Gson();
        String str = gson.toJson(msg.toMessageReciever());
        for (WebSocket sock : conns) {
            try
            {
                sock.send(str);
            }
            catch (Exception e)
            {
            }
        }
        addMessage(msg);
    }

    private void addMessage(Message msg) {
        this.activity.addMessage(msg);
    }
}
