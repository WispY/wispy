package com.wispy.wispy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Leonid_Poliakov
 */
@Controller
@RequestMapping("/slack")
public class SlackController {

    @Value("${slack.token}") private String slackToken;

    @RequestMapping(method = GET)
    @ResponseBody
    public String hello() {
        return "hello world!";
    }

    @RequestMapping(method = POST)
    @ResponseBody
    public ResponseEntity<String> execute(@RequestParam("token") String token, HttpServletRequest request) {
        if (!token.equals(slackToken)) {
            return new ResponseEntity<>("Invalid token", BAD_REQUEST);
        }
        return new ResponseEntity<>("Success", OK);
    }

}