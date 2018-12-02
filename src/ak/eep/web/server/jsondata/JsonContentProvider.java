package ak.eep.web.server.jsondata;

import ak.eep.web.server.server.Room;
import ak.eep.web.server.server.Server;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Provide contents for the webserver under a certain URL.
 */
public class JsonContentProvider {
    private static Logger log = LoggerFactory.getLogger(JsonContentProvider.class);

    private final Map<String, String> urlsToContent = new HashMap<>();
    private final SortedSet<String> currentDataTypes = new TreeSet<>();
    private final Server server;

    public JsonContentProvider(Server server) {
        this.server = server;
    }

    public void updateInput(String json) {
        final JSONObject object = new JSONObject(json);
        final SortedSet<String> dataTypes = new TreeSet<>(object.keySet());
        final boolean dataTypesChanged = updateDataTypes(dataTypes);
        log.debug("Found Datatypes: " + dataTypes);

        for (String dataType : dataTypes) {
            final String url = "/api/v1/" + dataType;
            final String jsonForUrl = object.get(dataType).toString();
            final String lastJsonForUrl = urlsToContent.get(url);
            urlsToContent.put(url, jsonForUrl);
            if (!server.urlUsed(url)) {
                server.addServerUrl(url, () -> urlsToContent.get(url));
            }

            if (!jsonForUrl.equals(lastJsonForUrl)) {
                log.info("URL content changed: " + url);
                server.getWebsocketHandler().broadcast(
                        new DataChangedEvent(Room.ofDataType(dataType), jsonForUrl));
            }
        }

        if (dataTypesChanged) {
            server.getWebsocketHandler().broadcast(
                    new AvailableDataTypesChangedEvent(currentDataTypes));
        }
    }

    private synchronized boolean updateDataTypes(Set<String> dataTypes) {
        boolean changed = false;
        changed |= currentDataTypes.retainAll(dataTypes);
        changed |= currentDataTypes.addAll(dataTypes);
        return changed;
    }

    public synchronized Set<String> getAllCurrentDataTypes() {
        return currentDataTypes;
    }
}
