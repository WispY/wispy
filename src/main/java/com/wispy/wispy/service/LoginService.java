package com.wispy.wispy.service;

import com.wispy.wispy.GitCredentials;
import com.wispy.wispy.controller.Session;
import com.wispy.wispy.processor.Task;
import org.apache.log4j.Logger;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author WispY
 */
@Component
public class LoginService {
    public static final Logger LOG = Logger.getLogger(LoginService.class);

    public void login(Task task, Session session) throws Exception {
        String name = task.getArguments()[0];
        String password = task.getArguments()[1];
        task.hide(password);

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