package ak.eep.web.server.server;

import io.javalin.Javalin;
import io.javalin.staticfiles.Location;
import io.javalin.websocket.WsSession;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Server {
    private static Logger log = LoggerFactory.getLogger(Server.class);
    private final Javalin app;
    private SortedSet<String> urls = new TreeSet<>();
    private List<Supplier<WebsocketAction>> initialSuppliers = new ArrayList<>();
    private List<BiConsumer<WsSession, String>> websocketConsumers = new ArrayList<>();
    private List<WsSession> sessions = new ArrayList<>();

    public Server(boolean testMode) {
        app = Javalin.create();
        // app.enableStaticFiles("C:\\Spiele\\Andreas_Kreuz\\ak-eep-web-server\\target\\classes\\public\\ak-eep-web", Location.EXTERNAL)
        if (!testMode) {
            app.enableStaticFiles("/public/ak-eep-web", Location.CLASSPATH)
                    .enableSinglePageMode("/", "/public/ak-eep-web/index.html");
        }
        if (testMode) {
            app.enableCorsForAllOrigins();
        }
        app.ws("/ws", ws -> {
            ws.onConnect(session -> {
                log.info("Websocket Connected");
                sessions.add(session);
                for (Supplier<WebsocketAction> actionSupplier : initialSuppliers) {
                    this.send(actionSupplier.get());
                }
            });
            ws.onMessage((session, message) -> {
                websocketConsumers.stream().forEach(c -> c.accept(session, message));
            });
            ws.onClose((session, statusCode, reason) -> {
                log.info("Websocket Closed: " + statusCode + " " + reason);
                sessions.remove(session);
            });
            ws.onError((session, throwable) -> {
                log.info("Websocket Errored", throwable);
                sessions.remove(session);
            });
        });
    }

    public void startServer() {
        app.start(3000);
        System.out.println("" +
                "   ___     ___      ___         __      __        _      \n" +
                "  | __|   | __|    | _ \\   ___  \\ \\    / / ___   | |__   \n" +
                "  | _|    | _|     |  _/  |___|  \\ \\/\\/ / / -_)  | '_ \\  \n" +
                "  |___|   |___|   _|_|_   _____   \\_/\\_/  \\___|  |_.__/  \n" +
                "_|\"\"\"\"\"|_|\"\"\"\"\"|_| \"\"\" |_|     |_|\"\"\"\"\"|_|\"\"\"\"\"|_|\"\"\"\"\"| \n" +
                "\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-'\"`-0-0-' ");
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

    public void addWsActionConsumer(BiConsumer<WsSession, String> onMessageConsumer) {
        this.websocketConsumers.add(onMessageConsumer);
    }

    public void addWsInitialSupplier(Supplier<WebsocketAction> initialSupplier) {
        this.initialSuppliers.add(initialSupplier);
    }

    public void send(@NotNull WebsocketAction action) {
        String jsonEncoded = jsonEncode(action);
        sendMessage(jsonEncoded);
    }

    private String jsonEncode(@NotNull WebsocketAction action) {
        JSONObject jsonObject = new JSONObject(action);
        jsonObject.put("event", action.getEvent());
        jsonObject.put("payload", action.getPayload());
        String myJson = jsonObject.toString();
        System.out.println(myJson);
        return myJson;
    }

    private void sendMessage(String jsonEventAndPayload) {
        sessions.stream().forEach(s -> s.send(jsonEventAndPayload));
    }
}
