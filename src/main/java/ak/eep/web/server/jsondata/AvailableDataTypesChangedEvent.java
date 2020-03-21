package ak.eep.web.server.jsondata;

import ak.eep.web.server.server.Room;
import ak.eep.web.server.server.WebsocketEvent;
import org.json.JSONObject;

import java.util.Set;

public class AvailableDataTypesChangedEvent extends WebsocketEvent {

    public AvailableDataTypesChangedEvent(String jsonEncoded) {
        super(Room.AVAILABLE_DATA_TYPES,
                "Set",
                jsonEncoded);
    }
}
