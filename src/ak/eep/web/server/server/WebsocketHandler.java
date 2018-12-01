package ak.eep.web.server.server;

import io.javalin.websocket.WsSession;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class WebsocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebsocketHandler.class);

    private final Map<Room, List<Supplier<WebsocketEvent>>> onJoinRoomSuppliers = new TreeMap<>();
    private final Map<Room, List<Consumer<WebsocketEvent>>> onMessageConsumers = new TreeMap<>();
    private final Map<Room, List<WsSession>> roomSessions = new TreeMap<>();
    private final List<WsSession> sessions = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private Timer timer = null;

    public WebsocketHandler() {
        addOnMessageConsumer(Room.PING, e -> System.out.println("RECEIVED: " + e.getPayload() + "/" + sdf.format(new Date())));
    }


    public void addOnMessageConsumer(Room room, Consumer<WebsocketEvent> onMessageConsumer) {
        List<Consumer<WebsocketEvent>> list = this.onMessageConsumers
                .computeIfAbsent(room, (r) -> new ArrayList<>());
        list.add(onMessageConsumer);
    }

    public void addOnJoinRoomSupplier(Room room, Supplier<WebsocketEvent> initialSupplier) {
        List<Supplier<WebsocketEvent>> list = this.onJoinRoomSuppliers
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
                    = onMessageConsumers.getOrDefault(room, Collections.emptyList());
            consumers.forEach(c -> c.accept(websocketEvent));
        }
    }

    private void computeRoomMessage(WsSession session, WebsocketEvent websocketEvent) {
        Room room = Room.of(websocketEvent.getPayload());
        if (room == null) {
            System.out.println("Cannot join or leave room: " + websocketEvent.getPayload());
        }

        if (RoomEvent.SUBSCRIBE.getStringValue().equals(websocketEvent.getType())) {
            joinRoom(session, room);
        } else if (RoomEvent.UNSUBSCRIBE.getStringValue().equals(websocketEvent.getType())) {
            leaveRoom(session, room);
        }
    }

    void onClose(WsSession session, int statusCode, String reason) {
        log.info("Websocket Closed: " + statusCode + " " + reason);
        removeSession(session);
    }

    void onError(WsSession session, Throwable throwable) {
        log.info("Websocket Errored", throwable);
        removeSession(session);
    }

    private void removeSession(WsSession session) {
        Arrays.stream(Room.values()).forEach(room -> leaveRoom(session, room));
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


    private void joinRoom(WsSession session, Room room) {
        synchronized (roomSessions) {
            List<WsSession> list = this.roomSessions
                    .computeIfAbsent(room, (r) -> new ArrayList<>());
            list.add(session);
            System.out.println(session + " joined room: " + room);
        }

        List<Supplier<WebsocketEvent>> initialSuppliers = this.onJoinRoomSuppliers
                .computeIfAbsent(room, (r) -> new ArrayList<>());
        initialSuppliers.forEach(s -> send(session, s.get()));

        if (room == Room.PING) {
            startTimerIfRequired();
        }
    }

    private void leaveRoom(WsSession session, Room room) {
        synchronized (roomSessions) {
            List<WsSession> list = this.roomSessions.get(room);
            if (list != null) {
                if (list.remove(session)) {
                    System.out.println(session + " left " + room);
                }
            }
        }

        if (room == Room.PING) {
            stopTimerIfRequired();
        }
    }

    private void startTimerIfRequired() {
        synchronized (roomSessions) {
            if (roomSessions.get(Room.PING) != null
                    && !roomSessions.get(Room.PING).isEmpty()
                    && timer == null) {
                timer = new Timer(true);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        broadcast(Room.PING, new WebsocketEvent(
                                Room.PING.getStringValue(),
                                sdf.format(new Date())));
                    }
                }, 1000, 5000);
                System.out.println("Ping service started");
            }
        }
    }

    private void stopTimerIfRequired() {
        synchronized (roomSessions) {
            if (roomSessions.get(Room.PING) != null
                    && roomSessions.get(Room.PING).size() == 0
                    && timer != null) {
                timer.cancel();
                timer = null;
                System.out.println("Ping service stopped");
            }
        }
    }
}
