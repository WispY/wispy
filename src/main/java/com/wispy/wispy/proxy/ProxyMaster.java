package com.wispy.wispy.proxy;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.glassfish.tyrus.server.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.Future;

/**
 * @author Leonid_Poliakov
 */
@Component
public class ProxyMaster {
    public static final Logger LOG = Logger.getLogger(ProxyMaster.class);

    @Value("${proxy.mode}") private String mode;
    @Value("${proxy.master}") private String master;
    @Value("${server.port:7777}") private int port;

    private Session session;
    private Gson gson;
    private Server server;

    @PostConstruct
    public void startServer() {
        if (!isMaster()) {
            LOG.info("Proxy is disabled");
            return;
        }

        LOG.info("Starting proxy master at: master = " + master + ", port = " + port);
        gson = new Gson();
        Endpoint.dispatcher = this;
        server = new Server(master, port, "", Endpoint.class);

        try {
            server.start();
        } catch (DeploymentException up) {
            throw new IllegalStateException("Could not start master", up);
        }
    }

    @PreDestroy
    public void stopServer() {
        server.stop();
    }

    public boolean isMaster() {
        return mode.equals("master");
    }

    private void open(Session session) {
        if (this.session != null) {
            LOG.info("Already have connected slave, disconnecting: session = " + session.getId());
            IOUtils.closeQuietly(this.session);
        }
        this.session = session;
        LOG.info("User connected to web sockets: " + session.getId());
    }

    public String sendToClient(String token, String user, String command, String arguments, String callback) {
        ProxyRequest request = new ProxyRequest(token, user, command, arguments, callback);
        Future<Void> future = session.getAsyncRemote().sendText(gson.toJson(request));
        try {
            future.get();
        } catch (Exception up) {
            LOG.error("Failed to send request to client", up);
        }
        return "No response";
    }

    private String handle(String json, Session session) {
        LOG.info("Message from slave: " + json);
        return "No response";
    }

    private void close(Session session, CloseReason closeReason) {
        LOG.info("Slave disconnected: session =  " + session.getId() + ", reason = " + closeReason);
        this.session = null;
    }

    @ServerEndpoint("")
    public static class Endpoint {
        private static ProxyMaster dispatcher;

        @OnOpen
        public void onOpen(Session session) {
            dispatcher.open(session);
        }

        @OnClose
        public void onClose(Session session, CloseReason reason) {
            dispatcher.close(session, reason);
        }

        @OnMessage
        public String onMessage(String json, Session session) {
            String response = dispatcher.handle(json, session);
            return response;
        }
    }

}