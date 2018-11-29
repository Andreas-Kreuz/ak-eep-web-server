package ak.eep.web.server.server;

import io.javalin.websocket.WsSession;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class WebsocketHandler {
    private static Logger log = LoggerFactory.getLogger(WebsocketHandler.class);

    private Map<Room, List<Supplier<WebsocketEvent>>> roomInitialSuppliers = new TreeMap<>();
    private Map<Room, List<Consumer<WebsocketEvent>>> remoteMessageConsumers = new TreeMap<>();
    private Map<Room, List<WsSession>> roomSessions = new TreeMap<>();
    private List<WsSession> sessions = new ArrayList<>();

    public WebsocketHandler() {
    }


    public void addMessageConsumer(Room room, Consumer<WebsocketEvent> onMessageConsumer) {
        List<Consumer<WebsocketEvent>> list = this.remoteMessageConsumers
                .computeIfAbsent(room, (r) -> new ArrayList<>());
        list.add(onMessageConsumer);
    }

    public void addOnJoinSupplier(Room room, Supplier<WebsocketEvent> initialSupplier) {
        List<Supplier<WebsocketEvent>> list = this.roomInitialSuppliers
                .computeIfAbsent(room, (r) -> new ArrayList<>());
        list.add(initialSupplier);
    }

    void onConnect(WsSession session) {
        log.info("Websocket Connected");
        sessions.add(session);
    }

    void onMessage(WsSession session, String message) {
        WebsocketEvent websocketEvent = decodeJson(message);
        Room room = Room.of(websocketEvent);
        if (room != null) {
            if (room == Room.ROOM) {
                computeRoomMessage(session, websocketEvent);
            }

            List<Consumer<WebsocketEvent>> consumers
                    = remoteMessageConsumers.getOrDefault(room, Collections.emptyList());
            consumers.forEach(c -> c.accept(websocketEvent));
        }
    }

    private void computeRoomMessage(WsSession session, WebsocketEvent websocketEvent) {
        Room room = Room.of(websocketEvent.getPayload());
        if (room == null) {
            System.out.println("Cannot join or leave room: " + websocketEvent.getPayload());
        }

        if (RoomEvent.SUBSCRIBE.getStringValue().equals(websocketEvent.getType())) {
            System.out.println(session + " joined " + room);
            List<WsSession> list = this.roomSessions
                    .computeIfAbsent(room, (r) -> new ArrayList<>());
            list.add(session);
            List<Supplier<WebsocketEvent>> initialSuppliers = this.roomInitialSuppliers
                    .computeIfAbsent(room, (r) -> new ArrayList<>());
            initialSuppliers.forEach(s -> send(session, s.get()));
        } else if (RoomEvent.UNSUBSCRIBE.getStringValue().equals(websocketEvent.getType())) {
            System.out.println(session + " left " + room);
            List<WsSession> list = this.roomSessions.get(room);
            if (list != null) {
                list.remove(session);
            }
        }
    }

    void onClose(WsSession session, int statusCode, String reason) {
        log.info("Websocket Closed: " + statusCode + " " + reason);
        sessions.remove(session);
    }

    void onError(WsSession session, Throwable throwable) {
        log.info("Websocket Errored", throwable);
        sessions.remove(session);
    }

    private WebsocketEvent decodeJson(@NotNull String jsonMessage) {
        JSONObject o = new JSONObject(jsonMessage);
        if (null == o.get("type")) {
            System.out.println("NO type IN MESSAGE: " + o.toString());
        }
        String type = o.get("type").toString();
        String payload = o.get("payload") == null ? null : o.get("payload").toString();
        return new WebsocketEvent(type, payload);
    }


    private String jsonEncode(@NotNull WebsocketEvent action) {
        JSONObject jsonObject = new JSONObject(action);
        jsonObject.put("type", action.getType());
        jsonObject.put("payload", action.getPayload());
        String myJson = jsonObject.toString();
        return myJson;
    }

    private void send(WsSession s, @NotNull WebsocketEvent action) {
        String jsonEncoded = jsonEncode(action);
        send(s, jsonEncoded);
    }

    public void broadcast(Room room, @NotNull WebsocketEvent action) {
        String jsonEncoded = jsonEncode(action);
        broadcast(room, jsonEncoded);
    }

    private void broadcast(Room room, String jsonEventAndPayload) {
        roomSessions
                .getOrDefault(room, Collections.emptyList())
                .forEach(s -> send(s, jsonEventAndPayload));
    }

    private void send(WsSession s, String jsonEventAndPayload) {
        try {
            System.out.println("SENDING: " + jsonEventAndPayload);
            s.send(jsonEventAndPayload);
        } catch (Exception e) {
            log.info("Cannot send", e);
        }
    }
}
