package ak.eep.web.server;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;

public class Server {
    private static Logger log = LoggerFactory.getLogger(Server.class);
    private final Javalin app;
    private SortedSet<String> urls = new TreeSet<>();

    Server() {
        app = Javalin.create();
        app.enableCorsForOrigin("*");
    }

    void startServer() {
        app.start(3000);
        app.get("/", ctx -> ctx
                .contentType("application/json").result("Ak-EEP-Web-Server"));
    }

    /**
     * @param url             Startet mit &quot;/&quot;, z.B. /signals
     * @param contentSupplier Supplier, der den Inhalt der URL bereitstellt
     */
    public void addServerUrl(String url, Supplier<String> contentSupplier) {
        log.info("Adding URL " + url);

        urls.add(url);
        app.get(url, ctx -> ctx
                .contentType("application/json").result(contentSupplier.get()));
    }

    public boolean urlUsed(String url) {
        return urls.contains(url);
    }
}
