package com.wispy.wispy.controller;

import com.wispy.wispy.processor.CommandProcessor;
import com.wispy.wispy.processor.Task;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wispy.wispy.utils.OutputUtils.answer;
import static com.wispy.wispy.utils.OutputUtils.hide;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toMap;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author WispY
 */
@Controller
@RequestMapping("/slack")
public class SlackCommandsController {
    public static final Logger LOG = Logger.getLogger(SlackCommandsController.class);

    private Map<String, Session> sessions;
    private Map<CommandProcessor, Pattern> processors;
    private String usage;
    private String description;

    @Value("${slack.token}") private String slackToken;

    @Autowired
    public void setProcessors(List<CommandProcessor> processors) {
        this.processors = processors.stream().collect(toMap(p -> p, p -> compile("^" + p.commandPattern() + "$")));
        StringBuilder builder = new StringBuilder();
        builder.append("Usage:\n");
        for (CommandProcessor processor : processors) {
            builder.append("`/git ").append(processor.commandUsage()).append("`\n")
                    .append("> ").append(processor.commandDescription()).append("\n");
        }
        usage = builder.toString();
        description = "This is a development tool used to automate merging pull request with a nice looking history";
    }

    @PostConstruct
    public void init() {
        sessions = new HashMap<>();
    }

    @RequestMapping(method = POST)
    @ResponseBody
    public String process(
            @RequestParam("token") String token,
            @RequestParam("user_id") String user,
            @RequestParam("command") String command,
            @RequestParam("text") String arguments,
            @RequestParam("response_url") String callback
    ) {
        if (!token.equals(slackToken)) {
            return answer("Invalid team token");
        }
        if (arguments.trim().isEmpty()) {
            return answer(description + "\n\n" + usage);
        }
        CommandProcessor processor = pickProcessor(arguments);
        if (processor == null) {
            return answer("Unknown command\n" + usage);
        }

        Task task = createTask(processor, arguments);
        if (task.isFailed()) {
            task.append("Failed to parse arguments. Usage: `{0} {1}`", command, processor.commandUsage());
            return answer(hide(task.buildOutput(), task.getHiddenWords()));
        }

        Session session = getOrCreateSession(user);
        try {
            processor.process(task, session);
        } catch (Exception up) {
            task.log(up);
            task.append("Failed to execute command: `{0}`", up.getMessage());
        }

        return answer(hide(task.buildOutput(), task.getHiddenWords()));
    }

    private Session getOrCreateSession(String user) {
        Session session = sessions.get(user);
        if (session == null) {
            session = new Session();
            sessions.put(user, session);
        }
        return session;
    }

    private Task createTask(CommandProcessor processor, String input) {
        Task task = new Task();
        task.setCommand(input);

        Matcher matcher = Pattern.compile(processor.commandArgumentsPattern()).matcher(input);
        String[] arguments = new String[processor.argumentsCount()];
        if (matcher.find()) {
            for (int index = 0; index < arguments.length; index++) {
                arguments[index] = matcher.group(index + 1);
            }
            task.log("successfully parsed arguments");
        } else {
            task.log("failed to parse command arguments");
            task.setFailed(true);
        }

        task.setArguments(arguments);
        return task;
    }

    private CommandProcessor pickProcessor(String input) {
        return processors.entrySet().stream()
                .filter(entry -> entry.getValue().matcher(input).matches())
                .map(Entry::getKey)
                .findFirst().get();
    }

}