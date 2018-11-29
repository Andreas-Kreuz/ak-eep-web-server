package ak.eep.web.server.server;

public enum Room {
    ROOM("[Room]"),
    LOG("[Log]"),
    URLS("[URLs]"),
    EEP_COMMAND("[EEPCommand]");

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
