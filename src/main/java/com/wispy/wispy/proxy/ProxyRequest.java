package com.wispy.wispy.proxy;

/**
 * @author Leonid_Poliakov
 */
public class ProxyRequest {
    private String token;
    private String user;
    private String command;
    private String arguments;
    private String callback;

    public ProxyRequest() {
    }

    public ProxyRequest(String token, String user, String command, String arguments, String callback) {
        this.token = token;
        this.user = user;
        this.command = command;
        this.arguments = arguments;
        this.callback = callback;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
}