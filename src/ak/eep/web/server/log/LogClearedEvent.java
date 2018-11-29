package ak.eep.web.server.log;

import ak.eep.web.server.server.WebsocketEvent;

public class LogClearedEvent extends WebsocketEvent {

    public LogClearedEvent() {
        super(LogEventType.LOG_CLEAR_MESSAGES.getStringValue(), "");
    }
}
