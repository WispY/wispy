package com.wispy.wispy;

import org.apache.log4j.Logger;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.wispy.wispy.Utils.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Leonid_Poliakov
 */
@Controller
@RequestMapping("/slack")
public class SlackController {
    public static final Logger LOG = Logger.getLogger(SlackController.class);

    private static String gitUsage = text(
            "```",
            "Usage:",
            "    /git login name password  - sign in at GitHub",
            "    /git list                 - show available pull requests",
            "    /git merge id [message]   - merge pull request by id from the list,",
            "                                uses request name as commit message by default",
            "```"
    );
    private static String gitHelp = text(
            "```",
            "This is a development tool used to automate merging pull request with a nice looking history.",
            "```",
            gitUsage
    );

    @Value("${slack.token}") private String slackToken;

    private Map<String, GitHub> sessions;
    private Map<String, Map<Integer, GHPullRequest>> requests;

    @PostConstruct
    public void init() {
        sessions = new HashMap<>();
        requests = new HashMap<>();
    }

    @RequestMapping(method = GET)
    @ResponseBody
    public String stub() {
        return "hello world!";
    }

    @RequestMapping(method = POST)
    @ResponseBody
    public ResponseEntity<String> execute(
            @RequestParam("token") String token,
            @RequestParam("user_id") String user,
            @RequestParam("command") String command,
            HttpServletRequest request
    ) {
        if (!token.equals(slackToken)) {
            return badRequest("Invalid team token");
        }
        if (!command.startsWith("/git")) {
            return badRequest("Invalid team token");
        }

        String[] arguments = command.substring("/git".length()).trim().split("\\s+");
        if (arguments.length == 0) {
            return success(gitHelp);
        }
        LOG.info("Command: " + command);
        try {
            switch (arguments[0]) {
                case "login":
                    return executeLogin(user, arguments);
                default:
                    return badRequest(text("Unknown command: " + arguments[0], gitUsage));
            }
        } catch (Exception up) {
            LOG.error("Command processing error: " + command, up);
            return internalError(up);
        }
    }

    private ResponseEntity<String> executeLogin(String user, String[] arguments) throws Exception {
        if (arguments.length != 3) {
            return badRequest("Usage: `/git login name password`");
        }

        GitHub github;
        try {
            github = GitHub.connectUsingPassword(arguments[1], arguments[2]);
        } catch (IOException up) {
            return badRequest("Could not login: " + up.getMessage());
        }
        sessions.put(user, github);
        return success("Connected as: " + github.getMyself().getName() + " (" + github.getMyself().getLogin() + ")");
    }

}