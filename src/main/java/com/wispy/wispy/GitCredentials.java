package com.wispy.wispy;

/**
 * @author Leonid_Poliakov
 */
public class GitCredentials {
    private String name;
    private String password;

    public GitCredentials() {
    }

    public GitCredentials(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}