package ak.eep.web.server.server;

public enum RoomEvent {
    SUBSCRIBE("[Room] Subscribe"),
    UNSUBSCRIBE("[Room] Unsubscribe");

    private final String stringValue;

    RoomEvent(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}
