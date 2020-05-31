package soundquake.ibbmeshchat;

import java.util.Date;

public class Message {
    String username;
    Date timestamp;
    String data;
    Integer vstate;
    Long userId;
    Long messageId;
    String latlng;
    public Message(String username, Date timestamp, String data, Integer vstate, Long userId, Long messageId, String latlng) {
        this.username = username;
        this.timestamp = timestamp;
        this.data = data;
        this.vstate = vstate;
        this.userId = userId;
        this.messageId = messageId;
        this.latlng = latlng;
    }
    public MessageReciver toMessageReciever() {
        Long mili = this.timestamp.getTime();
        return new MessageReciver(this.username, mili.toString(), this.data, this.vstate, this.userId, this.messageId, this.latlng);
    }
}

/**
 * Message with propeties as String
 */
class MessageReciver {
    String username;
    String timestamp;
    String data;
    Integer vstate;
    Long userId;
    Long messageId;
    String latlng;
    /**
     * Builds a MessageReciever Object. Use when parsing json.
     * @param username Username
     * @param timestamp Date in milliseconds
     * @param data Data
     */
    public MessageReciver(String username, String timestamp, String data, Integer vstate, Long userId, Long messageId, String latlng) {
        this.username = username;
        this.timestamp = timestamp;
        this.data = data;
        this.vstate = vstate;
        this.userId = userId;
        this.messageId = messageId;
        this.latlng = latlng;
    }
    public Message toMessage(ChatActivity activity) {
        if (this.latlng == null || this.latlng.equals(""))
        {
            this.latlng = activity.getCurrentLocationLatLngStr();
        }
        return new Message(this.username, new Date(Long.parseLong(this.timestamp)), this.data, this.vstate, this.userId, this.messageId, this.latlng);
    }
}