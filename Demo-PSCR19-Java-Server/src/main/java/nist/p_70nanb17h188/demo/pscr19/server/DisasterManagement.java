package nist.p_70nanb17h188.demo.pscr19.server;

import com.google.gson.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import nist.p_70nanb17h188.demo.pscr19.Device;
import nist.p_70nanb17h188.demo.pscr19.imc.Context;
import nist.p_70nanb17h188.demo.pscr19.imc.Intent;
import nist.p_70nanb17h188.demo.pscr19.imc.IntentFilter;
import nist.p_70nanb17h188.demo.pscr19.logic.Tuple2;
import nist.p_70nanb17h188.demo.pscr19.logic.app.messaging.Message;
import nist.p_70nanb17h188.demo.pscr19.logic.app.messaging.MessagingName;
import nist.p_70nanb17h188.demo.pscr19.logic.app.messaging.MessagingNameType;
import nist.p_70nanb17h188.demo.pscr19.logic.app.messaging.MessagingNamespace;
import nist.p_70nanb17h188.demo.pscr19.logic.app.messaging.Template;
import nist.p_70nanb17h188.demo.pscr19.logic.log.Log;
import nist.p_70nanb17h188.demo.pscr19.logic.net.DataReceivedHandler;
import nist.p_70nanb17h188.demo.pscr19.logic.net.Name;
import nist.p_70nanb17h188.demo.pscr19.logic.net.NetLayer;

/**
 *
 * @author jchen
 */
@ServerEndpoint("/disastermanagement/{subId}/{username}")
public class DisasterManagement {

    public static final String EVENT_TYPE_REQUEST_ILLEGAL = "request.illegal";
    public static final String EVENT_TYPE_TEMPLATES_GET_REQUEST = "templates.get.request";
    public static final String EVENT_TYPE_TEMPLATES_GET_RESPONSE = "templates.get.response";
    public static final String EVENT_TYPE_TEMPLATE_ADDED = "template.add";
    public static final String EVENT_TYPE_TEMPLATE_REMOVED = "template.remove";
    public static final String EVENT_TYPE_TEMPLATE_GET_REQUEST = "template.get.request";
    public static final String EVENT_TYPE_TEMPLATE_GET_RESPONSE = "template.get.response";
    public static final String EVENT_TYPE_TEMPLATE_GET_RESPONSE_ROOT = "root";
    public static final String EVENT_TYPE_TEMPLATE_GET_RESPONSE_COMMANDER = "commander";
    public static final String EVENT_TYPE_TEMPLATE_INSTANTIATE_REQUEST = "template.instantiate.request";
    public static final String EVENT_TYPE_TEMPLATE_INSTANTIATE_RESPONSE = "template.instantiate.response";
    public static final String EVENT_TYPE_INIT = "init";
    public static final String EVENT_TYPE_GRAPH = "graph";
    public static final String EVENT_TYPE_GRAPH_INCIDENT_ROOT = "graph.incident.root";
    public static final String EVENT_TYPE_GRAPH_DISPATCHER_ROOT = "graph.dispatcher.root";
    public static final String EVENT_TYPE_GRAPH_GET_REQUEST = "graph.get.request";
    public static final String EVENT_TYPE_GRAPH_RELATIONSHIP_ADD = "graph.relationship.add";
    public static final String EVENT_TYPE_GRAPH_RELATIONSHIP_REMOVE = "graph.relationship.remove";
    public static final String EVENT_TYPE_GRAPH_INCIDENT_REMOVE = "graph.incident.remove";
    public static final String EVENT_TYPE_GRAPH_NODE_REMOVE = "graph.node.remove";
    public static final String EVENT_TYPE_GRAPH_NODE_ADD = "graph.node.add";
    public static final String EVENT_TYPE_GRAPH_NODE_NAMECHANGE = "graph.node.namechange";
    public static final String EVENT_TYPE_MESSAGE_DELIVER = "message.deliver";
    public static final String EVENT_TYPE_CONNECTION_ESTABLISH = "connection.establish";

    private static final String DEFAULT_INITIATOR = "nist.p_70nanb17h188.demo.pscr19.server.DisasterManagement";
    private static final Gson GSON = new Gson();
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final AtomicLong SESSION_SERIAL = new AtomicLong();
    private static final String TAG = "DisasterManagement";
    // value: v1=subscribe, v2=unique name (DEFAULT_INITIATOR+"."+unique serial)
    private static final HashMap<Session, Tuple2<Name, String>> ALL_SESSIONS = new HashMap<>();
    private static final HashMap<Name, HashSet<Session>> SUBSCRIBERS = new HashMap<>();
    private static final int MAX_CONTENT_SHOW_LENGTH = 80;

    static {
        Context.getContext(MessagingNamespace.CONTEXT_MESSAGINGNAMESPACE).registerReceiver(DisasterManagement::onNamespaceChanged, new IntentFilter().addAction(MessagingNamespace.ACTION_NAMESPACE_CHANGED));
        Context.getContext(MessagingNamespace.CONTEXT_MESSAGINGNAMESPACE).registerReceiver(DisasterManagement::onAppNameChanged, new IntentFilter().addAction(MessagingNamespace.ACTION_APPNAME_CHANGED));
    }

    private static void onNamespaceChanged(Context context, Intent intent) {
        String msg = createNamespaceChangeEvent(intent.getExtra(MessagingNamespace.EXTRA_NAMES_ADDED),
                intent.getExtra(MessagingNamespace.EXTRA_NAMES_REMOVED),
                intent.getExtra(MessagingNamespace.EXTRA_RELATIONSHIPS_ADDED),
                intent.getExtra(MessagingNamespace.EXTRA_RELATIONSHIPS_REMOVED));
        Log.d(TAG, "namespaceChanged, msg=%s", msg);
        sendMessageToAllSessions(msg);
    }

    private static void onAppNameChanged(Context context, Intent intent) {
        String msg = createAppNameChangeEvent(intent.getExtra(MessagingNamespace.EXTRA_NAME));
        Log.d(TAG, "appNameChanged, msg=%s", msg);
        sendMessageToAllSessions(msg);
    }

    private static void sendMessageToAllSessions(String msg) {
        ArrayList<Session> toRemoves = new ArrayList<>();
        ALL_SESSIONS.keySet().forEach(session -> {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException | RuntimeException ex) {
                Log.e(TAG, ex, "Failed in sending msg");
                try {
                    session.close();
                } catch (IOException | RuntimeException ex2) {
                    Log.e(TAG, ex2, "Failed in closing session");
                }
                toRemoves.add(session);
            }
        });
        toRemoves.forEach(DisasterManagement::removeSession);
    }

    private static HashMap<String, Object> createEvent(String type, Object value) {
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("t", type);
        ret.put("v", value);
        return ret;
    }

    private static String createNamespaceChangeEvent(
            Collection<MessagingName> nas,
            Collection<Name> nds,
            Collection<Tuple2<Name, Name>> ras,
            Collection<Tuple2<Name, Name>> rds) {

        HashMap<String, Object> value = new HashMap<>();

        value.put("na",
                nas.stream().map(na -> {
                    HashMap<String, Object> naRepresentation = new HashMap<>();
                    naRepresentation.put("i", na.getName().getValue());
                    naRepresentation.put("n", na.getAppName());
                    naRepresentation.put("tp", na.getType());
                    naRepresentation.put("c", new int[0]);
                    return naRepresentation;
                }).toArray()
        );

        value.put("nd",
                nds.stream().map(nd -> {
                    HashMap<String, Object> ndRepresentation = new HashMap<>();
                    ndRepresentation.put("i", nd.getValue());
                    return ndRepresentation;
                }).toArray()
        );

        value.put("ra",
                ras.stream().map(ra -> {
                    HashMap<String, Object> raRepresentation = new HashMap<>();
                    raRepresentation.put("p", ra.getV1().getValue());
                    raRepresentation.put("c", ra.getV2().getValue());
                    return raRepresentation;
                }).toArray()
        );

        value.put("rd",
                rds.stream().map(rd -> {
                    HashMap<String, Object> rdRepresentation = new HashMap<>();
                    rdRepresentation.put("p", rd.getV1().getValue());
                    rdRepresentation.put("c", rd.getV2().getValue());
                    return rdRepresentation;
                }).toArray()
        );
        return GSON.toJson(createEvent(EVENT_TYPE_GRAPH, value));
    }

    private static String createAppNameChangeEvent(MessagingName name) {
        HashMap<String, Object> value = new HashMap<>();
        value.put("i", name.getName().getValue());
        value.put("n", name.getAppName());
        value.put("tp", name.getType());
        return GSON.toJson(createEvent(EVENT_TYPE_GRAPH_NODE_NAMECHANGE, value));
    }

    private static final String getSessionUniqueID() {
        return String.format("%s.%d", DEFAULT_INITIATOR, SESSION_SERIAL.incrementAndGet());
    }

    private static void addSession(Session s, Name subscribe) {
        ALL_SESSIONS.put(s, new Tuple2<>(subscribe, getSessionUniqueID()));
        HashSet<Session> subscribers = SUBSCRIBERS.get(subscribe);
        if (subscribers == null) {
            SUBSCRIBERS.put(subscribe, subscribers = new HashSet<>());
            NetLayer.subscribe(subscribe, DEFAULT_MESSAGE_RECEIVE_HANDLER, DEFAULT_INITIATOR);
        }
        subscribers.add(s);
    }

    private static void removeSession(Session s) {
        Tuple2<Name, String> sessionInfo = ALL_SESSIONS.remove(s);
        if (sessionInfo == null) {
            return;
        }
        Name subscribe = sessionInfo.getV1();
        if (subscribe == null) {
            return;
        }
        HashSet<Session> subscribers = SUBSCRIBERS.get(subscribe);
        subscribers.remove(s);
        if (subscribers.isEmpty()) {
            SUBSCRIBERS.remove(subscribe);
            NetLayer.unSubscribe(subscribe, DEFAULT_MESSAGE_RECEIVE_HANDLER, DEFAULT_INITIATOR);
        }
    }

    @OnOpen
    public void onOpen(
            @PathParam("subId") int subId,
            @PathParam("username") String userName,
            Session session, EndpointConfig config) {
        Log.d(TAG, "Open: subId: %d, username=%s", subId, userName);
        if (Device.getName() == null) {
            try {
                session.getBasicRemote().sendText(GSON.toJson(createEvent(EVENT_TYPE_CONNECTION_ESTABLISH, "Device not initialized yet!")));
            } catch (IOException ex) {
                Log.e(TAG, ex, "Failed in sending msg, subId=%d", subId);
            }
            try {
                session.close();
            } catch (IOException | RuntimeException ex2) {
                Log.e(TAG, ex2, "Failed in closing session, subId=%d", subId);
            }
            return;
        }

        addSession(session, new Name(subId));
        // check subId
        MessagingName mn = MessagingNamespace.getDefaultInstance().getName(new Name(subId));
        String establishString;
        if (mn == null) {
            // send: you are not subscribing to anything
            establishString = "Error: the group you are subscrbing to does not exist!";
        } else if (mn.getType() == MessagingNameType.Incident) {
            establishString = String.format("Warning: You are subscribing to an incident group (%d, %s) directly!", mn.getName().getValue(), mn.getAppName());
        } else {
            establishString = String.format("You are now subscribing to group (%d, %s)", mn.getName().getValue(), mn.getAppName());
        }
        try {
            session.getBasicRemote().sendText(GSON.toJson(createEvent(EVENT_TYPE_CONNECTION_ESTABLISH, establishString)));
            session.getBasicRemote().sendText(handleGetInit());
            session.getBasicRemote().sendText(handleGetGraph());
            session.getBasicRemote().sendText(handleGetTemplates());
        } catch (IOException ex) {
            Log.e(TAG, ex, "Failed in sending msg, subId=%d", subId);
            try {
                session.close();
            } catch (IOException | RuntimeException ex2) {
                Log.e(TAG, ex2, "Failed in closing session, subId=%d", subId);
            }
            removeSession(session);
        }
    }

    @OnMessage
    public String onMessage(String message, Session s) {
        JsonObject obj = JSON_PARSER.parse(message).getAsJsonObject();
        String type = obj.getAsJsonPrimitive("type").getAsString();
        switch (type) {
            case EVENT_TYPE_TEMPLATES_GET_REQUEST:
                return handleGetTemplates();
            case EVENT_TYPE_TEMPLATE_GET_REQUEST:
                return handleGetTemplate(obj);
            case EVENT_TYPE_GRAPH_GET_REQUEST:
                return handleGetGraph();
            case EVENT_TYPE_TEMPLATE_INSTANTIATE_REQUEST:
                return handleInstantiateTemplate(obj, s);
            case EVENT_TYPE_GRAPH_RELATIONSHIP_ADD:
                return handleAddNodeRelationship(obj);
            case EVENT_TYPE_GRAPH_RELATIONSHIP_REMOVE:
                return handleRemoveNodeRelationship(obj);
            case EVENT_TYPE_GRAPH_NODE_REMOVE:
                return handleRemoveNode(obj);
            case EVENT_TYPE_GRAPH_INCIDENT_REMOVE:
                return handleRemoveIncident(obj);
            case EVENT_TYPE_GRAPH_NODE_ADD:
                return handleAddNode(obj);
            case EVENT_TYPE_GRAPH_NODE_NAMECHANGE:
                return handleNodeNameChange(obj);
            case EVENT_TYPE_MESSAGE_DELIVER:
                return handleMessageDeliver(obj, s);
        }
        return GSON.toJson(createEvent(EVENT_TYPE_REQUEST_ILLEGAL, message));
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Log.d(TAG, "Close: %s", reason.getReasonPhrase());
        removeSession(session);
    }

    private String handleGetInit() {
        MessagingNamespace namespace = MessagingNamespace.getDefaultInstance();
        long incidentRoot = namespace.getIncidentRoot().getValue();
        long dispatcherRoot = namespace.getDispatcherRoot().getValue();
        HashMap<String, Object> data = new HashMap<>();
        data.put(EVENT_TYPE_GRAPH_INCIDENT_ROOT, incidentRoot);
        data.put(EVENT_TYPE_GRAPH_DISPATCHER_ROOT, dispatcherRoot);
        String ret = GSON.toJson(createEvent(EVENT_TYPE_INIT, data));
        Log.d(TAG, "handleGetInit, ret=%s", ret);
        return ret;
    }

    private String handleGetGraph() {
        MessagingNamespace namespace = MessagingNamespace.getDefaultInstance();
        Collection<MessagingName> nas = namespace.getAllNames();
        ArrayList<Tuple2<Name, Name>> ras = new ArrayList<>();
        nas.forEach(na -> {
            namespace.forEachChild(na, c -> ras.add(new Tuple2<>(na.getName(), c.getName())));
        });

        String ret = createNamespaceChangeEvent(nas, new ArrayList<>(), ras, new ArrayList<>());
        Log.d(TAG, "handleGetGraph, ret=%s", ret);
        return ret;
    }

    private String handleAddNode(JsonObject obj) {
        Name p = null;
        if (obj.has("p")) {
            JsonElement e = obj.get("p");
            if (!e.isJsonNull()) {
                p = new Name(e.getAsLong());
            }
        }
        String n = obj.getAsJsonPrimitive("n").getAsString();
        String tp = obj.getAsJsonPrimitive("tp").getAsString();
        Log.d(TAG, "handleAddNode: n=%s, tp=%s, p=%s", n, tp, p + "");
        MessagingNameType type = Enum.valueOf(MessagingNameType.class, tp);
        if (p == null) {
            MessagingNamespace.getDefaultInstance().createName(n, type, DEFAULT_INITIATOR);
        } else {
            MessagingNamespace.getDefaultInstance().createNameWithParent(n, type, p, DEFAULT_INITIATOR);
        }
        return null;
    }

    private String handleAddNodeRelationship(JsonObject obj) {
        Name pName = new Name(obj.getAsJsonPrimitive("pid").getAsLong());
        Name cName = new Name(obj.getAsJsonPrimitive("cid").getAsLong());
        Log.d(TAG, "handleAddNode: pName=%s, cName=%s", pName, cName);
        MessagingNamespace.getDefaultInstance().createRelationship(pName, cName, DEFAULT_INITIATOR);
        return null;
    }

    private String handleRemoveNode(JsonObject obj) {
        Name name = new Name(obj.getAsJsonPrimitive("id").getAsLong());
        Log.d(TAG, "handleRemoveNode: name=%s", name);
        MessagingNamespace.getDefaultInstance().removeName(name, DEFAULT_INITIATOR);
        return null;
    }

    private String handleRemoveNodeRelationship(JsonObject obj) {
        Name pName = new Name(obj.getAsJsonPrimitive("pid").getAsLong());
        Name cName = new Name(obj.getAsJsonPrimitive("cid").getAsLong());
        Log.d(TAG, "handleRemoveNodeRelationship: pName=%s, cName=%s", pName, cName);
        MessagingNamespace.getDefaultInstance().removeRelationship(pName, cName, DEFAULT_INITIATOR);
        return null;
    }

    private String handleNodeNameChange(JsonObject obj) {
        Name name = new Name(obj.getAsJsonPrimitive("id").getAsLong());
        String newAppName = obj.getAsJsonPrimitive("newName").getAsString();
        Log.d(TAG, "handleRemoveNodeRelationship: name=%s, newAppName=%s", name, newAppName);
        MessagingNamespace.getDefaultInstance().updateAppName(name, newAppName, DEFAULT_INITIATOR);
        return null;
    }

    private String handleGetTemplates() {
        ArrayList<HashMap<String, Object>> value = new ArrayList<>();
        Template.forEachTemplate(t -> {
            HashMap<String, Object> templateRepresentation = new HashMap<>();
            templateRepresentation.put("id", t.getId());
            templateRepresentation.put("name", t.getName());
            value.add(templateRepresentation);
        });
        String ret = GSON.toJson(createEvent(EVENT_TYPE_TEMPLATES_GET_RESPONSE, value));
        Log.d(TAG, "handleGetTemplates, ret=%s", ret);
        return ret;
    }

    private String handleGetTemplate(JsonObject obj) {
        int id = obj.getAsJsonPrimitive("id").getAsInt();
        Template t = Template.getTemplate(id);
        if (t == null) {
            Log.e(TAG, "handleGetTemplate, cannot find template id: %d", id);
            return null;
        }
        HashMap<String, Object> value = new HashMap<>();
        value.put("id", t.getId());
        value.put("rootNodeId", t.getRootNode().getValue());
        value.put("commanderNodeId", t.getRootNode().getValue());
        value.put("name", t.getName());
        ArrayList<HashMap<String, Object>> nodes = new ArrayList<>();
        value.put("n", nodes);
        t.forEachName(mn -> {
            HashMap<String, Object> node = new HashMap<>();
            nodes.add(node);
            node.put("i", mn.getName().getValue());
            node.put("n", mn.getAppName());
            node.put("tp", mn.getType());
            ArrayList<Long> c = new ArrayList<>();
            node.put("c", c);
            t.forEachChild(mn, child -> c.add(child.getName().getValue()));
        });
        String ret = GSON.toJson(createEvent(EVENT_TYPE_TEMPLATE_GET_RESPONSE, value));
        Log.d(TAG, "handleGetTemplate, id=%d ret=%s", id, ret);
        return ret;
    }

    private String handleInstantiateTemplate(JsonObject obj, Session s) {
        MessagingNamespace namespace = MessagingNamespace.getDefaultInstance();
        int tid = obj.getAsJsonPrimitive("tid").getAsInt();
        String name = obj.getAsJsonPrimitive("name").getAsString();
        Template template = Template.getTemplate(tid);
        if (template == null) {
            Log.e(TAG, "handleGetTemplate: cannot find template id: %d", tid);
            return null;
        }
        Name userName = ALL_SESSIONS.get(s).getV1();
        assert userName != null;
        Tuple2<Name, Name> result = namespace.instantiateTemplate(template, name, userName, DEFAULT_INITIATOR);

        HashMap<String, Object> value = new HashMap<>();
        value.put(EVENT_TYPE_TEMPLATE_GET_RESPONSE_ROOT, result.getV1().getValue());
        value.put(EVENT_TYPE_TEMPLATE_GET_RESPONSE_COMMANDER, result.getV2().getValue());
        String ret = GSON.toJson(createEvent(EVENT_TYPE_TEMPLATE_INSTANTIATE_RESPONSE, value));
        Log.d(TAG, "handleInstantiateTemplate: tid=%d, name=%s, ret=%s", tid, name, ret);
        return ret;
    }

    private String handleRemoveIncident(JsonObject obj) {
        MessagingNamespace namespace = MessagingNamespace.getDefaultInstance();
        Name incidentRootName = new Name(obj.getAsJsonPrimitive("id").getAsLong());
        Log.d(TAG, "Remove incident, id=%s", incidentRootName);
        namespace.removeIncidentTree(incidentRootName, DEFAULT_INITIATOR);
        return null;
    }

    private static final DataReceivedHandler DEFAULT_MESSAGE_RECEIVE_HANDLER = (src, dst, data, initiator) -> {
        MessagingNamespace namespace = MessagingNamespace.getDefaultInstance();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        Message msg = Message.read(buffer);
        MessagingName msgSrc = namespace.getName(src);
        MessagingName msgDst = namespace.getName(dst);
        String strSrc = msgSrc == null ? src.toString() : msgSrc.toString();
        String strDst = msgDst == null ? dst.toString() : msgDst.toString();

        if (msg == null) {
            Log.e(TAG, "received msg: src=%s, dst=%s, data.len=%d, initiator=%s, content not parsable", msgSrc, msgDst, data.length, initiator);
            return;
        }
        MessagingName msgSg = namespace.getName(msg.getSenderGroup());
        MessagingName msgRg = namespace.getName(msg.getReceiverGroup());
        String strSg = msgSg == null ? msg.getSenderGroup().toString() : msgSg.toString();
        String strRg = msgRg == null ? msg.getReceiverGroup().toString() : msgRg.toString();

        Log.d(TAG, "received msg: src=%s, dst=%s, data.len=%d, initiator=%s MSG time=%s, sg=%s, rg=%s, sn=%s, ns=%s, tp=%s, mime=%s, content=%s",
                msgSrc, msgDst, data.length, initiator,
                new Date(msg.getSendTime()), strSg, strRg, msg.getSenderName(),
                Arrays.toString(Stream.of(msg.getCarriedNames()).map(name -> {
                    MessagingName mn = namespace.getName(name);
                    return mn == null ? name.toString() : mn.toString();
                }).toArray()),
                msg.getType(),
                msg.getMime(),
                msg.getContent().length() > MAX_CONTENT_SHOW_LENGTH
                ? String.format("[%d] %s...", msg.getContent().length(), msg.getContent().substring(0, MAX_CONTENT_SHOW_LENGTH))
                : msg.getContent()
        );
        HashSet<Session> subscribers = SUBSCRIBERS.get(dst);
        if (subscribers == null) {
            Log.e(TAG, "subscribers of %s is null!", strDst);
            return;
        }

        // generate string on demand
        String toSend = null;

        ArrayList<Session> toRemoves = new ArrayList<>();
        for (Session subscriber : subscribers) {
            Tuple2<Name, String> subInfo = ALL_SESSIONS.get(subscriber);
            if (subInfo == null) {
                Log.e(TAG, "Cannot find session ifno for %s", subscriber);
                continue;
            }
            if (subInfo.getV2().equals(initiator)) {
                continue;
            }
            if (toSend == null) {
                // generate string on demand
                HashMap<String, Object> value = new HashMap<>();
                value.put("t", msg.getSendTime());
                value.put("sn", msg.getSenderName());
                value.put("sg", msg.getSenderGroup().getValue());
                value.put("rg", msg.getReceiverGroup().getValue());
                value.put("ns", Stream.of(msg.getCarriedNames()).map(n -> n.getValue()).toArray());
                value.put("tp", msg.getType());
                if (msg.getType() == Message.MessageType.MSG) {
                    value.put("v", msg.getContent());
                } else {
                    value.put("v", msg.getMime() + msg.getContent());
                }
                toSend = GSON.toJson(createEvent(EVENT_TYPE_MESSAGE_DELIVER, value));
                Log.d(TAG, "toSend: %s", toSend.length() > MAX_CONTENT_SHOW_LENGTH ? toSend.substring(0, MAX_CONTENT_SHOW_LENGTH) + "..." : toSend);
            }

            try {
                subscriber.getBasicRemote().sendText(toSend);
            } catch (IOException | RuntimeException ex) {
                Log.e(TAG, ex, "Failed in sending msg");
                try {
                    subscriber.close();
                } catch (IOException | RuntimeException ex2) {
                    Log.e(TAG, ex2, "Failed in closing session");
                }
                toRemoves.add(subscriber);
            }
        }
        toRemoves.forEach(DisasterManagement::removeSession);

    };

    private String handleMessageDeliver(JsonObject obj, Session s) {
        MessagingNamespace namespace = MessagingNamespace.getDefaultInstance();
        JsonObject inner = obj.getAsJsonObject("v");
        if (inner == null) {
            return null;
        }

        long sendTime = inner.get("t").getAsLong();
        String senderName = inner.get("sn").getAsString();
        Name senderGroup = new Name(inner.get("sg").getAsLong());
        Name receiverGroup = new Name(inner.get("rg").getAsLong());
        JsonArray nameArray = inner.getAsJsonArray("ns");
        Name[] carriedNames = new Name[nameArray.size()];
        for (int i = 0; i < carriedNames.length; i++) {
            carriedNames[i] = new Name(nameArray.get(i).getAsInt());
        }
        Message.MessageType type = Message.MessageType.valueOf(inner.get("tp").getAsString());
        String content = inner.get("v").getAsString();

        MessagingName msgSenderGroup = namespace.getName(senderGroup);
        MessagingName msgReceiverGroup = namespace.getName(receiverGroup);
        String mime;
        if (type == Message.MessageType.PNT) {
            int mimeEnd = content.indexOf("base64,") + 7;
            mime = content.substring(0, mimeEnd);
            content = content.substring(mimeEnd);
        } else {
            mime = "text/plain";
        }

        Log.d(TAG, "handleMessageDeliver time=%s, sg=%s, rg=%s, sn=%s, ns=%s, tp=%s, mime=%s, content=%s",
                new Date(sendTime),
                msgSenderGroup == null ? senderGroup.toString() : msgSenderGroup.toString(),
                msgReceiverGroup == null ? receiverGroup.toString() : msgReceiverGroup.toString(),
                senderName,
                Arrays.toString(Stream.of(carriedNames).map(name -> {
                    MessagingName mn = namespace.getName(name);
                    return mn == null ? name.toString() : mn.toString();
                }).toArray()),
                type,
                mime,
                content.length() > MAX_CONTENT_SHOW_LENGTH
                ? String.format("[%d] %s...", content.length(), content.substring(0, MAX_CONTENT_SHOW_LENGTH))
                : content
        );

        Message msg = new Message(sendTime, senderGroup, receiverGroup, senderName, carriedNames, type, mime, content);
        ByteBuffer buffer = ByteBuffer.allocate(msg.getWriteSize());
        msg.write(buffer);
        NetLayer.sendData(senderGroup, receiverGroup, buffer.array(), true, ALL_SESSIONS.get(s).getV2());
        return null;
    }
}
