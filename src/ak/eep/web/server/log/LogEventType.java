package ak.eep.web.server.log;

public enum LogEventType {
    LOG_ADD_MESSAGES("[Log] Lines Added"),
    LOG_CLEAR_MESSAGES("[Log] Cleared");

    private final String stringValue;

    LogEventType(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}
