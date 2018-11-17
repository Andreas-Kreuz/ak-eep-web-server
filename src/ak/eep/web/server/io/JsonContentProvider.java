package ak.eep.web.server.io;

import ak.eep.web.server.Server;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonContentProvider {

    private final Map<String, String> urlsToContent = new HashMap<>();
    private final Server server;

    public JsonContentProvider(Server server) {
        this.server = server;
    }

    public void updateInput(String json) {
        JSONObject object = new JSONObject(json);
        System.out.println(object.keySet());
        for (String key : object.keySet()) {
            String url = "/" + key;
            String jsonForUrl = object.get(key).toString();

            urlsToContent.put(url, jsonForUrl);
            if (!server.urlUsed(url)) {
                server.addServerUrl(url, () -> urlsToContent.get(url));
            }
        }
    }
}
