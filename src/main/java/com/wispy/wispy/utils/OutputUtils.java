package com.wispy.wispy.utils;

import com.wispy.wispy.SlackAnswer;
import com.wispy.wispy.processor.Task;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.List;

/**
 * @author WispY
 */
public class OutputUtils {
    public static final Logger LOG = Logger.getLogger(OutputUtils.class);

    public static String format(String format, Object... arguments) {
        return new MessageFormat(format).format(arguments);
    }

    public static String hide(String text, List<String> words) {
        return words.stream().reduce(text, (source, word) -> source.replaceAll(word, "***"));
    }

    public static SlackAnswer plain(String text) {
        return SlackAnswer.answer().text(text);
    }

    public static SlackAnswer plain(Task task) {
        String content = format("> _*{0}*_\n{1}", task.getStatusLine(), task.buildOutput());
        content = hide(content, task.getHiddenWords());
        return SlackAnswer.answer().text(content);
    }
}