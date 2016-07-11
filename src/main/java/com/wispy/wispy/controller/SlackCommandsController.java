package com.wispy.wispy.controller;

import com.wispy.wispy.SlackAnswer;
import com.wispy.wispy.processor.Command;
import com.wispy.wispy.processor.CommandProcessor;
import com.wispy.wispy.processor.Task;
import com.wispy.wispy.service.SessionService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.wispy.wispy.utils.OutputUtils.format;
import static com.wispy.wispy.utils.OutputUtils.plain;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author WispY
 */
@Controller
@RequestMapping("/slack")
public class SlackCommandsController {
    public static final Logger LOG = Logger.getLogger(SlackCommandsController.class);

    @Value("${slack.token}") private String slackToken;
    @Value("${slack.command}") private String slackCommand;

    @Autowired private SessionService sessionService;

    @RequestMapping(method = POST)
    @ResponseBody
    public SlackAnswer process(
            @RequestParam("token") String token,
            @RequestParam("user_id") String user,
            @RequestParam("command") String slashCommand,
            @RequestParam("text") String arguments,
            @RequestParam("response_url") String callback
    ) {
        if (!token.equals(slackToken)) {
            return plain("Invalid team token");
        }

        Session session = sessionService.getOrCreateSession(user);
        if (!StringUtils.hasText(arguments)) {
            String[] header = session.isInteracting()
                    ? new String[]{"Current options:"}
                    : new String[]{"This tool makes GitHub history pretty.", "Here are your options:"};
            Task task = usageTask(session, slashCommand, "Showing usage", header);
            return plain(task);
        }

        Optional<Command> command = pickCommand(session, arguments);
        if (!command.isPresent()) {
            Task task = usageTask(session, arguments, "Command not found", format("Your current options:", arguments));
            return plain(task);
        }
        return plain(format("Executing: `{0}`", command.get().getUsage()));
    }

    private Optional<Command> pickCommand(Session session, String input) {
        List<Command> commands = session.getCommands();
        return Stream.concat(
                commands.stream().filter(Command::isHardMatch),
                commands.stream().filter(command -> !command.isHardMatch())
        ).filter(command -> input.matches(command.getPattern())).findFirst();
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

    private Task usageTask(Session session, String input, String statusLine, String... header) {
        Task task = new Task();
        task.setCommand(input);
        task.setStatusLine(statusLine);
        Arrays.stream(header).forEach(task::append);
        session.getCommands().stream().forEach(command -> task.append(format("`{0} {1}` {2}", slackCommand, command.getUsage(), command.getDescription())));
        return task;
    }

}