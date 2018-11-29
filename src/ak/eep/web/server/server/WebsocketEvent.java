package ak.eep.web.server.server;

public class WebsocketEvent {
    private String type;
    private String payload;

    public WebsocketEvent(String type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return this.type;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "WebsocketEvent{" +
                "type='" + type + '\'' +
                ", payload='" + payload + '\'' +
                '}';
    }
}
