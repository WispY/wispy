package com.wispy.wispy.controller;

import com.wispy.wispy.SlackAnswer;
import com.wispy.wispy.processor.CommandProcessor;
import com.wispy.wispy.processor.Task;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wispy.wispy.utils.OutputUtils.answer;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author WispY
 */
@Controller
@RequestMapping("/slack")
public class SlackCommandsController {
    public static final Logger LOG = Logger.getLogger(SlackCommandsController.class);

    @Value("${slack.token}") private String slackToken;

    @RequestMapping(method = POST)
    @ResponseBody
    public SlackAnswer process(
            @RequestParam("token") String token,
            @RequestParam("user_id") String user,
            @RequestParam("command") String command,
            @RequestParam("text") String arguments,
            @RequestParam("response_url") String callback
    ) {
        if (!token.equals(slackToken)) {
            return answer("Invalid team token");
        }

        return answer("Hello world!");
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

    // private CommandProcessor pickProcessor(String input) {
    //     return processors.entrySet().stream()
    //             .filter(entry -> entry.getValue().matcher(input).matches())
    //             .map(Entry::getKey)
    //             .findFirst().get();
    // }

}