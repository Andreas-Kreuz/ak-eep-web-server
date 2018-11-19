package ak.eep.web.server;

import io.javalin.Javalin;
import io.javalin.staticfiles.Location;
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
        // app.enableStaticFiles("C:\\Spiele\\Andreas_Kreuz\\ak-eep-web-server\\target\\classes\\public\\ak-eep-web", Location.EXTERNAL)
        app.enableStaticFiles("/public/ak-eep-web", Location.CLASSPATH)
                .enableSinglePageMode("/", "/public/ak-eep-web/index.html");
        app.enableCorsForAllOrigins();
    }

    void startServer() {
        app.start(3000);
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
