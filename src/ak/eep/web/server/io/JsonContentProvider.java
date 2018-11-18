package ak.eep.web.server.io;

import ak.eep.web.server.Server;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class JsonContentProvider {
    private static Logger log = LoggerFactory.getLogger(JsonContentProvider.class);

    private final Map<String, String> urlsToContent = new HashMap<>();
    private final Server server;

    public JsonContentProvider(Server server) {
        this.server = server;
    }

    public void updateInput(String json) {
        JSONObject object = new JSONObject(json);
        log.info("Found URLs: " + object.keySet());
        for (String key : object.keySet()) {
            String url = "/api/v1/" + key;
            String jsonForUrl = object.get(key).toString();

            urlsToContent.put(url, jsonForUrl);
            if (!server.urlUsed(url)) {
                server.addServerUrl(url, () -> urlsToContent.get(url));
            }
        }
    }
}
