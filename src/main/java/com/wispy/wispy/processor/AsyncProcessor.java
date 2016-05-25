package com.wispy.wispy.processor;

/**
 * @author WispY
 */
public interface AsyncProcessor {

    void process(Task task) throws Exception;

}