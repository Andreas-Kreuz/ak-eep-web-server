package ak.eep.web.server.log;

import ak.eep.web.server.server.WebsocketAction;

public class LogClearedAction extends WebsocketAction {

    public LogClearedAction() {
        super(LogEventType.LOG_CLEAR_MESSAGES, "");
    }
}
