package com.wispy.wispy.controller;

import com.wispy.wispy.processor.Command;
import com.wispy.wispy.processor.Task;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author WispY
 */
public class Session {
    private Map<String, Object> storage;
    private Task currentTask;
    private List<Command> commands;

    public Session() {
        storage = new HashMap<>();
        commands = new LinkedList<>();
    }

    public void set(String key, Object value) {
        storage.put(key, value);
    }

    public <T> T get(String key) {
        return (T) storage.get(key);
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public void addCommand(Command processor) {
        commands.add(processor);
    }

    public void clearCommands() {
        commands.clear();
    }
}