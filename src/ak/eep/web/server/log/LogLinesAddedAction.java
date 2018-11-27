package ak.eep.web.server.log;

import ak.eep.web.server.server.WebsocketAction;

public class LogLinesAddedAction extends WebsocketAction {
    public LogLinesAddedAction(String logLines) {
        super(LogEventType.LOG_ADD_MESSAGES, logLines);
    }
}
