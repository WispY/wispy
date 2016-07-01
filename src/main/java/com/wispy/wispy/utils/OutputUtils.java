package com.wispy.wispy.utils;

import com.wispy.wispy.SlackAnswer;
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
        for (String word : words) {
            if (word != null && !word.trim().isEmpty()) {
                text = text.replaceAll(word, "*****");
            }
        }
        return text;
    }

    public static SlackAnswer answer(String text) {
        SlackAnswer answer = new SlackAnswer();
        answer.setText(text);
        return answer;
    }
}