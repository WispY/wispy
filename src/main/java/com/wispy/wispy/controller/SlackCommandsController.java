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

    @Value("${slack.token}") private String slackToken;

    @Autowired
    public void setProcessors(List<CommandProcessor> processors) {
        this.processors = processors.stream().collect(toMap(p -> p, p -> compile("$" + p.commandPattern() + "^")));
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
        String input = command + " " + arguments;
        CommandProcessor processor = pickProcessor(input);
        if (processor == null) {
            return answer("Unknown command");
        }

        Session session = getOrCreateSession(user);
        Task task = createTask(processor, input);
        try {
            task.log("executing: {0}", input);
            processor.process(task, session);
        } catch (Exception up) {
            task.log("failed to execute command: {0}", input);
            task.log(up);
            task.append("Failed to execute: `{0}`", input);
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
        Matcher matcher = processors.get(processor).matcher(input);
        String[] arguments = new String[processor.argumentsCount()];
        if (!matcher.find()) {
            for (int index = 0; index < arguments.length; index++) {
                arguments[index] = matcher.group(index + 1);
            }
        }

        Task task = new Task();
        task.setCommand(input);
        task.setArguments(arguments);
        return task;
    }

    private CommandProcessor pickProcessor(String input) {
        for (Entry<CommandProcessor, Pattern> entry : processors.entrySet()) {
            Matcher matcher = entry.getValue().matcher(input);
            if (matcher.matches()) {
                return entry.getKey();
            }
        }
        return null;
    }

}