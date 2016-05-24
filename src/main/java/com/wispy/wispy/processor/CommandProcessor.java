package com.wispy.wispy.processor;

import com.wispy.wispy.controller.Session;

/**
 * @author WispY
 */
public interface CommandProcessor {

    String commandUsage();

    String commandDescription();

    String commandPattern();

    String commandArgumentsPattern();

    int argumentsCount();

    void process(Task task, Session session) throws Exception;

}