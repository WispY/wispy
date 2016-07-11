package com.wispy.wispy.service;

import com.wispy.wispy.controller.Session;
import com.wispy.wispy.processor.Task;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import static com.wispy.wispy.processor.Command.CommandBuilder.builder;

/**
 * @author WispY
 */
@Component
public class LogService {
    public static final Logger LOG = Logger.getLogger(LogService.class);

    public void addCommand(Session session) {
        session.addCommand(builder()
                .usage("logs {page}")
                .description("display detailed logs")
                .pattern("logs.*")
                .argumentsCount(1)
                .argumentsPattern("logs ?(\\d+)")
                .handler(this::printLogs)
                .build());
    }

    public void printLogs(Task task, Session session) {

    }
}