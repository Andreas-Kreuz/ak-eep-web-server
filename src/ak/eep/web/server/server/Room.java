package ak.eep.web.server.server;

public enum Room {
    EEP_COMMAND("[EEPCommand]"),
    LOG("[Log]"),
    PING("[Ping]"),
    ROOM("[Room]"),
    URLS("[URLs]");

    private final String stringValue;

    Room(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public static Room of(WebsocketEvent event) {
        return of(event.getType());
    }

    public static Room of(String eventType) {
        for (Room room : values()) {
            if (eventType.startsWith(room.getStringValue())) {
                return room;
            }
        }
        return null;
    }
}
