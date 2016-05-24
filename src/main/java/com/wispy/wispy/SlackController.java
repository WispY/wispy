package com.wispy.wispy;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

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
            "This is a development tool used to automate merging pull request with a nice looking history",
            "",
            gitUsage
    );

    @Value("${slack.token}") private String slackToken;
    @Value("${github.organization}") private String gitOrganization;
    @Value("${issue.tracker.url}") private String issueTrackerUrl;
    @Value("${issue.tracker.pattern}") private String issueTrackerPatternString;

    private Map<String, GitHub> sessions;
    private Map<String, GitCredentials> sessionCredentials;
    private Map<String, List<GHPullRequest>> requests;
    private ExecutorService executors;
    private HttpClient callbackClient;
    private Pattern issuePattern;

    @PostConstruct
    public void init() {
        sessions = new HashMap<>();
        sessionCredentials = new HashMap<>();
        requests = new HashMap<>();
        executors = Executors.newFixedThreadPool(10);
        callbackClient = HttpClientBuilder.create().build();
        issuePattern = Pattern.compile(issueTrackerPatternString);
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
            @RequestParam("response_url") String callbackUrl
    ) {
        if (!token.equals(slackToken)) {
            return badRequest("Invalid team token");
        }
        if (!command.equals("/git")) {
            return badRequest("Invalid team token");
        }

        if (argumentsString.trim().isEmpty()) {
            return success(gitHelp);
        }
        String[] arguments = argumentsString.trim().split("\\s+");
        try {
            switch (arguments[0]) {
                case "login":
                    if (arguments.length != 3) {
                        return badRequest("Usage: `/git login name password`");
                    }
                    return executeLogin(user, arguments[1], arguments[2]);
                case "list":
                    return executeList(user, callbackUrl);
                case "merge":
                    if (arguments.length < 2) {
                        return badRequest("Usage: `/git merge id [message]`");
                    }
                    int requestId;
                    try {
                        requestId = Integer.valueOf(arguments[1]);
                    } catch (NumberFormatException up) {
                        return badRequest("Not a valid id: `" + arguments[1] + "`");
                    }
                    return executeMerge(user, requestId, mergeMessage(argumentsString, arguments), callbackUrl);
                default:
                    return badRequest(text("Unknown command: `" + arguments[0], gitUsage) + "`");
            }
        } catch (Exception up) {
            LOG.error("Command processing error: " + command, up);
            return internalError(up);
        }
    }

    private ResponseEntity<String> executeLogin(String user, String name, String password) throws Exception {
        GitHub github;
        GHMyself gitUser;
        try {
            github = GitHub.connectUsingPassword(name, password);
            gitUser = github.getMyself();
        } catch (IOException up) {
            return badRequest("Could not login: `" + up.getMessage() + "`");
        }
        sessions.put(user, github);
        sessionCredentials.put(user, new GitCredentials(name, password));
        return success("Connected as: `" + gitUser.getName() + " (" + gitUser.getLogin() + ")`");
    }

    private ResponseEntity<String> executeList(String user, String callbackUrl) throws Exception {
        GitHub github = sessions.get(user);
        if (github == null) {
            return badRequest("Please, log in first: `/git login name password`");
        }

        executeAsync(callbackUrl, () -> {
            long time = System.currentTimeMillis();

            List<String> output = new LinkedList<>();
            List<GHRepository> repositories = new LinkedList<GHRepository>();
            repositories.addAll(github.getOrganization(gitOrganization).getRepositories().values());
            int repositoryCount = repositories.size();

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

            List<GHPullRequest> listedRequests = new LinkedList<>();
            requests.put(user, listedRequests);

            output.add("");
            int index = 0;
            if (pullRequests.isEmpty()) {
                output.add("No open pull requests found");
            } else {
                for (Entry<GHRepository, List<GHPullRequest>> entry : pullRequests.entrySet()) {
                    GHRepository repository = entry.getKey();
                    output.add(link(repository.getName(), repository.getHtmlUrl()) + " " + (repository.isPrivate() ? "(private)" : "(public)"));
                    for (GHPullRequest request : entry.getValue()) {
                        output.add("> `" + index + "` " +
                                        wrappedLink("link", request.getHtmlUrl()) + " " +
                                        wrappedLink("ticket", ticketLink(request.getTitle(), request.getHead().getRef())) + " " +
                                        request.getTitle() +
                                        (request.getMergeable() ? "" : " `can't merge!`")
                        );
                        listedRequests.add(request);
                        index++;
                    }
                }
                output.add("");
                output.add("Merge with: `/git merge id [message]`");
            }

            time = System.currentTimeMillis() - time;
            output.add(0, "Looked at `" + repositoryCount + "` repositories in `" + time + " ms`");
            return success(text(output));
        });
        return success("Looking for pull requests... Please, wait");
    }

    private ResponseEntity<String> executeMerge(String user, int requestId, String message, String callbackUrl) throws Exception {
        GitHub github = sessions.get(user);
        GitCredentials credentials = sessionCredentials.get(user);
        if (github == null || credentials == null) {
            return badRequest("Please, log in first: `/git login name password`");
        }

        List<GHPullRequest> listedRequests = requests.get(user);
        if (listedRequests == null) {
            return badRequest("No pull requests were listed, run: `/git list`");
        }
        if (requestId < 0 || requestId >= listedRequests.size()) {
            return badRequest("Request with id `" + requestId + "` was not listed");
        }
        GHPullRequest request = listedRequests.get(requestId);

        executeAsync(callbackUrl, () -> {
            String commitMessage = message == null ? request.getTitle() : message;
            String localRepoName = UUID.randomUUID().toString();
            File repoDir = new File("./" + localRepoName);
            GHRepository fork = request.getRepository();
            String forkUrl = "https://" + credentials.getName() + ":" + credentials.getPassword() + "@github.com/" + fork.getFullName();
            Utils.cli(new File("."), "git clone " + forkUrl + " " + repoDir.getAbsolutePath());

            return success("Cloned into: `" + localRepoName + "`");
        });

        return success("Merging pull request " + link(request.getTitle(), request.getHtmlUrl()) + "... Please, wait");
    }

    private void executeAsync(String callbackUrl, AsyncExecution execution) {
        executors.execute(() -> {
            ResponseEntity<String> result;
            try {
                result = execution.execute();
            } catch (Exception up) {
                LOG.error("Command processing error", up);
                result = internalError(up);
            }

            HttpPost post = new HttpPost(callbackUrl);
            post.setHeader("content-type", "application/json");
            post.setEntity(new StringEntity(result.getBody(), "UTF-8"));
            try {
                LOG.info("Calling: " + callbackUrl);
                LOG.info("Callback body: " + result.getBody());
                HttpResponse response = callbackClient.execute(post);
                String body = null;
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    body = IOUtils.toString(entity.getContent(), "UTF-8");
                }
                LOG.info("Callback url answer: " + response.getStatusLine() + ": " + body);
            } catch (IOException up) {
                LOG.error("Failed to post to callback url: " + callbackUrl, up);
            }
        });
    }

    private String ticketLink(String title, String ref) {
        String issue = match(issuePattern, title);
        if (issue == null) {
            issue = match(issuePattern, ref);
        }
        if (issue == null) {
            return null;
        }
        return issueTrackerUrl + issue;
    }

    private String mergeMessage(String argumentsString, String[] arguments) {
        if (arguments.length < 3) {
            return null;
        }
        int commandIndex = argumentsString.indexOf(arguments[0]);
        int idIndex = argumentsString.indexOf(arguments[1], commandIndex + arguments[0].length());
        String message = argumentsString.substring(idIndex + arguments[1].length()).trim();
        return message.isEmpty() ? null : message;
    }

}