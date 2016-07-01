package com.wispy.wispy.processor;

import com.wispy.wispy.controller.Session;

/**
 * @author WispY
 */
public interface CommandHandler {

    void process(Task task, Session session) throws Exception;

}