package com.wispy.wispy;

import org.apache.log4j.Logger;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

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
            "`/git login name password`",
            "       sign in at GitHub",
            "`/git list`",
            "       show available pull requests",
            "`/git merge id [message]`",
            "       merge pull request by id from the list,",
            "       uses request name as commit message by default"
    );

    private static String gitHelp = text(
            "This is a development tool used to automate merging pull request with a nice looking history.",
            "",
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
            @RequestParam("text") String argumentsString,
            HttpServletRequest request
    ) {
        if (!token.equals(slackToken)) {
            return badRequest("Invalid team token");
        }
        if (!command.equals("/git")) {
            return badRequest("Invalid team token");
        }

        String[] arguments = argumentsString.trim().split("\\s+");
        if (arguments[0].isEmpty()) {
            return success(gitHelp);
        }
        try {
            switch (arguments[0]) {
                case "login":
                    return executeLogin(user, arguments);
                case "list":
                    return executeList(user);
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
        GHMyself gitUser;
        try {
            github = GitHub.connectUsingPassword(arguments[1], arguments[2]);
            gitUser = github.getMyself();
        } catch (IOException up) {
            return badRequest("Could not login: `" + up.getMessage() + "`");
        }
        sessions.put(user, github);
        return success("Connected as: `" + gitUser.getName() + " (" + gitUser.getLogin() + ")`");
    }

    private ResponseEntity<String> executeList(String user) throws Exception {
        GitHub github = sessions.get(user);
        if (github == null) {
            return badRequest("Please, log in first: `/git login name password`");
        }
        List<String> output = new LinkedList<>();

        Map<String, GHRepository> personalRepositories = github.getMyself().getRepositories();
        Map<String, GHOrganization> organizations = github.getMyOrganizations();

        List<GHRepository> repositories = new LinkedList<>();
        for (Entry<String, GHOrganization> entry : organizations.entrySet()) {
            repositories.addAll(entry.getValue().getRepositories().values());
        }
        repositories.addAll(personalRepositories.values());
        output.add("Searched through: `" + repositories.size() + "` repositories.");

        Map<GHRepository, List<GHPullRequest>> pullRequests = new TreeMap<>((f, s) -> f.getName().compareTo(s.getName()));

        Iterator<GHRepository> iterator = repositories.iterator();
        while (iterator.hasNext()) {
            GHRepository repository = iterator.next();
            List<GHPullRequest> requests = repository.getPullRequests(GHIssueState.OPEN);
            if (requests.isEmpty()) {
                iterator.remove();
            } else {
                Collections.sort(requests, (f, s) -> Integer.compare(f.getNumber(), s.getNumber()));
                pullRequests.put(repository, requests);
            }
        }

        if (pullRequests.isEmpty()) {
            output.add("No open pull requests found.");
        } else {
            for (Entry<GHRepository, List<GHPullRequest>> entry : pullRequests.entrySet()) {
                output.add("`" + entry.getKey().getName() + "`");
                for (GHPullRequest request : entry.getValue()) {
                    output.add("  " + request.getTitle());
                }
            }
        }
        return success(text(output));
    }

}