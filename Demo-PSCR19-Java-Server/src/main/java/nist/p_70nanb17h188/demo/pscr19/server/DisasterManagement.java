package nist.p_70nanb17h188.demo.pscr19.server;

import com.google.gson.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
    private static final String TAG = "DisasterManagement";
    private static final HashMap<Session, Name> ALL_SESSIONS = new HashMap<>();
    private static final HashMap<Name, HashSet<Session>> SUBSCRIBERS = new HashMap<>();

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
            Collection<MessagingNamespace.MessagingName> nas,
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

    private static String createAppNameChangeEvent(MessagingNamespace.MessagingName name) {
        HashMap<String, Object> value = new HashMap<>();
        value.put("i", name.getName().getValue());
        value.put("n", name.getAppName());
        value.put("tp", name.getType());
        return GSON.toJson(createEvent(EVENT_TYPE_GRAPH_NODE_NAMECHANGE, value));
    }

    private static void addSession(Session s, Name subscribe) {
        ALL_SESSIONS.put(s, subscribe);
        HashSet<Session> subscribers = SUBSCRIBERS.get(subscribe);
        if (subscribers == null) {
            SUBSCRIBERS.put(subscribe, subscribers = new HashSet<>());
            NetLayer.subscribe(subscribe, onMessageReceived);
        }
        subscribers.add(s);
    }

    private static void removeSession(Session s) {
        Name subscribe = ALL_SESSIONS.remove(s);
        if (subscribe != null) {
            HashSet<Session> subscribers = SUBSCRIBERS.get(subscribe);
            subscribers.remove(s);
            if (subscribers.isEmpty()) {
                SUBSCRIBERS.remove(subscribe);
                NetLayer.unSubscribe(subscribe, onMessageReceived);
            }
        }
    }

    private static final DataReceivedHandler onMessageReceived = (src, dst, data, initiator) -> {
    };

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
        MessagingNamespace.MessagingName mn = MessagingNamespace.getDefaultInstance().getName(new Name(subId));
        String establishString;
        if (mn == null) {
            // send: you are not subscribing to anything
            establishString = "Error: the group you are subscrbing to does not exist!";
        } else if (mn.getType() == MessagingNamespace.MessagingNameType.Incident) {
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
        Collection<MessagingNamespace.MessagingName> nas = namespace.getAllNames();
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
        MessagingNamespace.MessagingNameType type = Enum.valueOf(MessagingNamespace.MessagingNameType.class, tp);
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
        Tuple2<Name, Name> result = namespace.instantiateTemplate(template, name, DEFAULT_INITIATOR);

        // add the commander to namespace by default
        Name userName = ALL_SESSIONS.get(s);
        assert userName != null;
        namespace.createRelationship(result.getV2(), userName, DEFAULT_INITIATOR);

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

    private String handleMessageDeliver(JsonObject obj, Session s) {
        return null;
    }
}
