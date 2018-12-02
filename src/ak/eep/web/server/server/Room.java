package ak.eep.web.server.server;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;

public class Room {
    public static final String EEP_COMMAND = "[EEPCommand]";
    public static final String LOG = "[Log]";
    public static final String PING = "[Ping]";
    public static final String ROOM = "[Room]";
    public static final String AVAILABLE_DATA_TYPES = "[AvailableDataTypes]";

    public static String ofDataType(String dataType) {
        return "[Data-" + dataType + "]";
    }

    public static String dataTypeOf(String room) {
        if (room.startsWith("[Data-") && room.endsWith("]")) {
            return room.substring(6, room.length() - 1);
        }
        throw new IllegalStateException("No data room: " + room);
    }
//    public enum Type {
//        EEP_COMMAND("[EEPCommand]"),
//        LOG("[Log]"),
//        PING("[Ping]"),
//        ROOM("[Room]"),
//        AVAILABLE_DATA_TYPES("[AvailableDataTypes]");
//
//        private final String stringValue;
//
//        Type(String stringValue) {
//            this.stringValue = stringValue;
//        }
//
//        @Nullable
//        public static Type of(String eventType) {
//            for (Type type : values()) {
//                if (eventType.startsWith(type.stringValue)) {
//                    return type;
//                }
//            }
//            return null;
//        }
//
//        public String action() {
//            return stringValue;
//        }
//    }
//
//    private final String roomName;
//    private static final Map<String, Room> rooms = new TreeMap<>();
//
//    protected Room(String roomName) {
//        this.roomName = roomName;
//    }
//
//    public String getRoomName() {
//        return roomName;
//    }
//
//    @Override
//    public String toString() {
//        return roomName;
//    }
//
//    public static Room of(@NotNull WebsocketEvent event) {
//        return of(Type.of(event.getAction()));
//    }
//
//    public static Room of(Type type) {
//        return rooms.computeIfAbsent(type.action(), typeString -> new Room(typeString));
//    }
//
//    public static Room of(String typeString) {
//        Type type = Type.of(typeString);
//        if (type != null) {
//            return of(type);
//        }
//
//        return rooms.computeIfAbsent(typeString, t -> new Room(t));
//    }
//
//    @Override
//    public int compareTo(@NotNull Room o) {
//        return this.roomName.compareTo(o.roomName);
//    }
}
