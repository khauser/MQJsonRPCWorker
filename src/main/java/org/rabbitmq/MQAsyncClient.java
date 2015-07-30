package org.rabbitmq;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.rabbitmq.config.ServiceConfiguration;
import org.rabbitmq.config.RabbitMQProperties;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;

public class MQAsyncClient {
    private static final Logger LOG = LoggerFactory.getLogger(MQAsyncClient.class);

    private Connection connection;
    private Channel channel;
    private String queueName;

    private RabbitMQProperties rabbitMQProperties;

    @Autowired
    public MQAsyncClient(ServiceConfiguration configuration, String queueName) throws IOException {
        ConnectionFactory factory = new ConnectionFactory();

        rabbitMQProperties = configuration.getRabbitMQProperties();

        LOG.info("rpc client connection setup for '{}:{}'", rabbitMQProperties.getHost(), rabbitMQProperties.getPort());
        factory.setHost(rabbitMQProperties.getHost());
        factory.setPort(rabbitMQProperties.getPort());

        connection = factory.newConnection();
        channel = connection.createChannel();
        //channel.basicQos(1);

        this.queueName = queueName;
    }

    public void call(String message, String correlationId) throws IOException {
        BasicProperties props = new BasicProperties.Builder()
            .correlationId(correlationId)
            .build();

        LOG.debug("publishing client request on {} with correlationId={}", queueName, correlationId);
        channel.basicPublish("", queueName, props, message.getBytes());
    }

    public void executeRequest(JSONRPC2Request request) {
        try {
            this.call(request.toString(), request.getID().toString());
        }
        catch  (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws Exception {
        connection.close();
    }
}

