package com.wispy.wispy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.HttpStatus.OK;

/**
 * @author Leonid_Poliakov
 */
public class Utils {
    private static Gson gson = new GsonBuilder().create();

    public static ResponseEntity<String> badRequest(String message) {
        return success(message);
    }

    public static ResponseEntity<String> internalError(Throwable throwable) {
        return success("Internal error: `" + throwable.getMessage() + "`");
    }

    public static ResponseEntity<String> success(String message) {
        return new ResponseEntity<>(json(answer(message)), OK);
    }

    public static SlackAnswer answer(String text) {
        SlackAnswer answer = new SlackAnswer();
        answer.setText(text);
        return answer;
    }

    public static String text(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        String prefix = "";
        for (String line : lines) {
            builder.append(prefix).append(line);
            prefix = "\n";
        }
        return builder.toString();
    }

    public static String text(String... lines) {
        StringBuilder builder = new StringBuilder();
        String prefix = "";
        for (String line : lines) {
            builder.append(prefix).append(line);
            prefix = "\n";
        }
        return builder.toString();
    }

    public static String link(String name, String url) {
        if (url == null) {
            return name;
        }
        return "<" + url + "|" + name + ">";
    }

    public static String link(String name, URL url) {
        if (url == null) {
            return link(name, (String) null);
        }
        return link(name, url.toString());
    }

    public static String wrappedLink(String name, String url) {
        return "[" + link(name, url) + "]";
    }

    public static String wrappedLink(String name, URL url) {
        return "[" + link(name, url) + "]";
    }

    public static String json(Object object) {
        return gson.toJson(object);
    }

    public static String match(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}