package org.rabbitmq;

import java.io.IOException;

import org.rabbitmq.config.ServiceConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestMQClient extends MQClient {

    @Autowired
    public TestMQClient(ServiceConfiguration configuration) throws IOException {
        super(configuration, configuration.getRabbitMQProperties().getCommon_queue_name());
    }

}