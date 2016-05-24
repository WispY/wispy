package com.wispy.wispy;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;

import java.util.Arrays;

/**
 * @author Leonid_Poliakov
 */
public class Launcher extends SpringBootServletInitializer {
    public static final Logger LOG = Logger.getLogger(Launcher.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Starting application with arguments: " + Arrays.toString(args));
        SpringApplication.run(SpringConfiguration.class, args);
    }

}