package com.wispy.wispy;

import org.springframework.http.ResponseEntity;

/**
 * @author Leonid_Poliakov
 */
public interface AsyncExecution {

    ResponseEntity<String> execute() throws Exception;

}