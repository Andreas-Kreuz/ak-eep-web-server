package ak.eep.web.server.server;

import ak.eep.web.server.log.LogEventType;

public class WebsocketEvent {
    private String type;
    private final Object payload;

    public WebsocketEvent(LogEventType type, Object payload) {
        this.type = type.getStringValue();
        this.payload = payload;
    }

    public String getType() {
        return this.type;
    }

    public Object getPayload() {
        return payload;
    }
}
