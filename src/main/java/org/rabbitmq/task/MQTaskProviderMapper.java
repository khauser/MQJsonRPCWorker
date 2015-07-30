package org.rabbitmq.task;

import java.util.HashMap;

import javax.inject.Provider;

public class MQTaskProviderMapper extends HashMap<String, Provider<? extends MQTask>> {

    private static final long serialVersionUID = 1L;

    //private static final Logger log = LoggerFactory.getLogger(MQTaskProviderMapper.class);

    public MQTaskProviderMapper() {}

    public MQTask getMQTask(String mqTaskClassname) {
        return this.get(mqTaskClassname).get();
    }

}
