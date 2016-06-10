package com.wispy.wispy.proxy;

import com.wispy.wispy.controller.SlackCommandsController;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

/**
 * @author Leonid_Poliakov
 */
@Component
public class ProxySlave {
    public static final Logger LOG = Logger.getLogger(ProxySlave.class);

    @Value("${proxy.mode}") private String mode;
    @Value("${proxy.master}") private String master;
    @Autowired private SlackCommandsController controller;

    private Session session;

    @PostConstruct
    public void init() throws IOException, DeploymentException {
        if (!isSlave()) {
            LOG.info("Proxy is disabled");
            return;
        }

        LOG.info("Connecting proxy slave to: " + master);
        URI serverUri = URI.create(master);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(Endpoint.class, serverUri);
    }

    private boolean isSlave() {
        return mode.equals("slave");
    }

    private void receive(String message) {
        LOG.info("From master: " + message);
    }

    private void disconnect(CloseReason reason) {
        LOG.warn("Disconnected from server: " + reason);
        try {
            if (session != null) {
                session.close();
            }
        } catch (IOException ignore) {
        }
        session = null;
    }

    @ClientEndpoint
    public static class Endpoint {
        public static ProxySlave proxy;

        @OnMessage
        public void onMessage(String message) {
            LOG.debug("Received: " + message);
            proxy.receive(message);
        }

        @OnClose
        public void onClose(CloseReason reason) {
            LOG.debug("Disconnected from server: " + reason);
            proxy.disconnect(reason);
        }
    }

}