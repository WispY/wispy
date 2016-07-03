package com.wispy.wispy.utils;

import com.wispy.wispy.SlackAnswer;
import com.wispy.wispy.processor.Task;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.List;

import static com.wispy.wispy.SlackAttachment.attachment;

/**
 * @author WispY
 */
public class OutputUtils {
    public static final Logger LOG = Logger.getLogger(OutputUtils.class);

    public static String format(String format, Object... arguments) {
        return new MessageFormat(format).format(arguments);
    }

    public static String hide(String text, List<String> words) {
        for (String word : words) {
            if (word != null && !word.trim().isEmpty()) {
                text = text.replaceAll(word, "*****");
            }
        }
        return text;
    }

    public static SlackAnswer plain(String text) {
        return SlackAnswer.answer().text(text);
    }

    public static SlackAnswer plain(Task task) {
        return SlackAnswer.answer()
                .text(task.buildOutput())
                .attach(attachment()
                        .fallback("abc")
                        .text("abc")
                        .color("danger")
                        .timestamp((int) (System.currentTimeMillis() / 1000)));
    }
}