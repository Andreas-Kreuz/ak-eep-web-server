package ak.eep.web.server.server;

import ak.eep.web.server.log.LogEventType;

public class WebsocketAction {
    private String event;
    private final Object payload;

    public WebsocketAction(LogEventType event, Object payload) {
        this.event = event.getStringValue();
        this.payload = payload;
    }

    public String getEvent() {
        return this.event;
    }

    public Object getPayload() {
        return payload;
    }
}
