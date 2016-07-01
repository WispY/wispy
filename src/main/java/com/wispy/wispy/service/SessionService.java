package com.wispy.wispy.service;

import com.wispy.wispy.controller.Session;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.wispy.wispy.processor.Command.CommandBuilder.builder;

/**
 * @author WispY
 */
@Component
public class SessionService {
    public static final Logger LOG = Logger.getLogger(SessionService.class);

    @Autowired private LoginService loginService;

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
        session.addCommand(builder()
                .usage("login {name} {password}")
                .description("sign in at GitHub")
                .pattern("login.*")
                .argumentsCount(2)
                .argumentsPattern("login (\\S+) (\\S+)")
                .handler(loginService::login)
                .build());

        //session.addCommand(builder()
        //        .usage("logs {page}")
        //        .description("display previous command logs")
        //        .pattern("logs.*")
        //        .argumentsCount(1)
        //        .argumentsPattern("logs ?(\\d+)")
        //        .handler(this::)
        //        .build());
    }
}