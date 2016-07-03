package com.wispy.wispy.service;

import com.wispy.wispy.controller.Session;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WispY
 */
@Component
public class SessionService {
    public static final Logger LOG = Logger.getLogger(SessionService.class);

    @Autowired private LoginService loginService;
    @Autowired private LogService logService;

    private Map<String, Session> sessions;

    @PostConstruct
    public void init() {
        sessions = new HashMap<>();
    }

    public Session getOrCreateSession(String user) {
        Session session = sessions.get(user);
        if (session == null) {
            session = new Session();
            fillCommands(session);
            sessions.put(user, session);
        }
        return session;
    }

    private void fillCommands(Session session) {
        loginService.addCommand(session);
        logService.addCommand(session);
    }
}