package ak.eep.web.server.log;

import ak.eep.web.server.server.WebsocketEvent;

public class LogLinesAddedEvent extends WebsocketEvent {
    public LogLinesAddedEvent(String logLines) {
        super(LogEventType.LOG_ADD_MESSAGES, logLines);
    }
}
