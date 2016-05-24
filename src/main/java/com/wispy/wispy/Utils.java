package com.wispy.wispy;

import org.springframework.http.ResponseEntity;

import static org.springframework.http.HttpStatus.*;

/**
 * @author Leonid_Poliakov
 */
public class Utils {
    public static ResponseEntity<String> badRequest(String message) {
        return new ResponseEntity<>(message, BAD_REQUEST);
    }

    public static ResponseEntity<String> internalError(Throwable throwable) {
        return new ResponseEntity<>("Internal error: " + throwable.getMessage(), INTERNAL_SERVER_ERROR);
    }

    public static ResponseEntity<String> success(String message) {
        return new ResponseEntity<>(message, OK);
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
}