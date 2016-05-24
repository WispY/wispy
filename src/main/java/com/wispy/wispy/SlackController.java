package com.wispy.wispy;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Leonid_Poliakov
 */
@Controller
@RequestMapping("/slack")
public class SlackController {

    @RequestMapping
    @ResponseBody
    public String echo(HttpServletRequest request) {
        return request.getParameterMap().toString();
    }

}