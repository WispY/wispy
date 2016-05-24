package com.wispy.wispy.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wispy.wispy.SlackAnswer;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.List;

/**
 * @author WispY
 */
public class OutputUtils {
    public static final Logger LOG = Logger.getLogger(OutputUtils.class);
    private static Gson gson = new GsonBuilder().create();

    public static String format(String format, Object... arguments) {
        return new MessageFormat(format).format(arguments);
    }

    public static String hide(String text, List<String> words) {
        for (String word : words) {
            text = text.replaceAll(word, "*****");
        }
        return text;
    }

    public static String json(Object object) {
        return gson.toJson(object);
    }

    public static String answer(String text) {
        SlackAnswer answer = new SlackAnswer();
        answer.setText(text);
        return json(answer);
    }
}