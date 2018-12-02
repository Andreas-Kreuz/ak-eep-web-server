package ak.eep.web.server.jsondata;

import ak.eep.web.server.server.Room;
import ak.eep.web.server.server.WebsocketEvent;

import java.util.Set;

public class AvailableDataTypesChangedEvent extends WebsocketEvent {

    public AvailableDataTypesChangedEvent(Set<String> urls) {
        super(Room.AVAILABLE_DATA_TYPES,
                "Set",
                urls.toString());
    }
}
