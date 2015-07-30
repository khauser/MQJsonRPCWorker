package org.rabbitmq.task;

import java.util.HashMap;

import org.springframework.stereotype.Component;

@Component
public class MQAsyncTaskMapper extends HashMap<String, Class<? extends MQAsyncTask>> {

    private static final long serialVersionUID = 1L;

}
