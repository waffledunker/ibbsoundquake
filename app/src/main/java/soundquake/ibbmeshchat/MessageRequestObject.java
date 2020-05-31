package soundquake.ibbmeshchat;

/**
 * Created by Deniz on 30.05.2020.
 */
public class MessageRequestObject
{
    public String username;
    public String data;
    public LocationObject location;
    public Long messageId;
    public Long timestamp;
    public Long userId;
    public Integer vstate;
    
    public MessageRequestObject(String username, String data, Double lat, Double lon, Long messageId, Long timestamp, Long userId, Integer vstate)
    {
        this.username = username;
        this.data = data;
        this.location = new LocationObject(lat, lon);
        this.messageId = messageId;
        this.timestamp = timestamp;
        this.userId = userId;
        this.vstate = vstate;
    }
}
