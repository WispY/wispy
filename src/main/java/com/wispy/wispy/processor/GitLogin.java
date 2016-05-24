package com.wispy.wispy.processor;

import com.wispy.wispy.GitCredentials;
import com.wispy.wispy.controller.Session;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author WispY
 */
@Component
@Order(0)
public class GitLogin implements CommandProcessor {

    @Override
    public String commandUsage() {
        return "login {name} {password}";
    }

    @Override
    public String commandDescription() {
        return "sign in at GitHub";
    }

    @Override
    public String commandPattern() {
        return "login.*";
    }

    @Override
    public String commandArgumentsPattern() {
        return "login (\\S+) (\\S+)";
    }

    @Override
    public int argumentsCount() {
        return 2;
    }

    @Override
    public void process(Task task, Session session) throws Exception {
        String name = task.getArguments()[0];
        String password = task.getArguments()[1];
        task.hide(password);
        task.append("");

        task.log("logging in github as {0}", name);
        GitHub github;
        GHMyself gitUser;
        try {
            github = GitHub.connectUsingPassword(name, password);
            gitUser = github.getMyself();
        } catch (IOException up) {
            task.log("failed to log in as {0}", name);
            task.log(up);
            task.append("Could not log in, reason: `{0}`", up.getMessage());
            return;
        }
        session.set("github", github);
        session.set("credentials", new GitCredentials(name, password));
        task.append("Logged in as: `{0} ({1})`", gitUser.getName(), gitUser.getLogin());
    }

}