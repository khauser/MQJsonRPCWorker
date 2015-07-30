package org.rabbitmq.utility;

import org.springframework.stereotype.Component;

@Component
public class KeyService {

    private static final String TASK_PREFIX = "task_";

    public String getTaskKey(String guid) {
        return TASK_PREFIX + guid;
    }

}