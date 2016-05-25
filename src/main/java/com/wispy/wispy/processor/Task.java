package com.wispy.wispy.processor;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.LinkedList;
import java.util.List;

import static com.wispy.wispy.utils.OutputUtils.format;

/**
 * @author WispY
 */
public class Task {
    private String command;
    private String[] arguments;

    private StringBuilder output;
    private StringBuilder log;
    private List<String> hiddenWords;
    private boolean failed;
    private AsyncProcessor async;

    public Task() {
        this.output = new StringBuilder();
        this.log = new StringBuilder();
        this.hiddenWords = new LinkedList<>();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }

    public void append(String message) {
        addString(message, output, true);
    }

    public void append(String format, Object... arguments) {
        addString(format(format, arguments), output, true);
    }

    public void prepend(String message) {
        addString(message, output, false);
    }

    public void prepend(String format, Object... arguments) {
        addString(format(format, arguments), output, false);
    }

    public void log(String message) {
        addString(message, log, true);
    }

    public void log(String format, Object... arguments) {
        addString(format(format, arguments), log, true);
    }

    public void log(Throwable throwable) {
        addString(ExceptionUtils.getStackTrace(throwable), log, true);
    }

    public String buildOutput() {
        return output.toString();
    }

    public void hide(String word) {
        hiddenWords.add(word);
    }

    public List<String> getHiddenWords() {
        return hiddenWords;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public AsyncProcessor getAsync() {
        return async;
    }

    public void setAsync(AsyncProcessor async) {
        this.async = async;
    }

    private void addString(String message, StringBuilder builder, boolean append) {
        if (append) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(message);
        } else {
            if (builder.length() > 0) {
                builder.insert(0, "\n");
            }
            builder.insert(0, message);
        }
    }
}