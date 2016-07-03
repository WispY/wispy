package com.wispy.wispy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.HttpStatus.OK;

/**
 * @author Leonid_Poliakov
 */
public class Utils {
    public static final Logger LOG = Logger.getLogger(Utils.class);
    private static Gson gson = new GsonBuilder().create();

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

    public static List<String> cli(File workDirectory, String command) throws Exception {
        LOG.info("Executing command from " + workDirectory.getAbsolutePath());
        Process process = new ProcessBuilder("/bin/bash").directory(workDirectory).start();
        PrintWriter input = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);
        input.println(command);
        input.println("exit");

        int exitCode = process.waitFor();
        BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        List<String> outputLines = new ArrayList<>();
        while ((line = output.readLine()) != null) {
            LOG.info(" - " + line);
            outputLines.add(line);
        }

        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = error.readLine()) != null) {
            LOG.error(" - " + line);
        }
        if (exitCode != 0) {
            throw new RuntimeException("CLI execution failed, code: " + exitCode);
        }

        return outputLines;
    }


}