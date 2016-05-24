package com.wispy.wispy.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WispY
 */
public class Session {
    private Map<String, Object> storage;

    public Session() {
        storage = new HashMap<>();
    }

    public void set(String key, Object value) {
        storage.put(key, value);
    }

    public <T> T get(String key) {
        return (T) storage.get(key);
    }
}