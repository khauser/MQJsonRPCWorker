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
import com.rabbitmq.client.QueueingConsumer;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;

public class MQClient {
    private static final Logger LOG = LoggerFactory.getLogger(MQClient.class);

    private Connection connection;
    private Channel channel;
    private String queueName;

    private RabbitMQProperties rabbitMQProperties;

    @Autowired
    public MQClient(ServiceConfiguration configuration, String queueName) throws IOException {
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

    public Object call(String message, String correlationId) throws IOException {
        byte[] response = null;

        final String replyQueueName = channel.queueDeclare().getQueue();
        LOG.info("rpc response queue declared '{}'", replyQueueName);

        BasicProperties props = new BasicProperties.Builder()
            .correlationId(correlationId)
            .replyTo(replyQueueName)
            .build();

        final QueueingConsumer consumer = new QueueingConsumer(channel);
        final boolean autoAck = true;
        channel.basicConsume(replyQueueName, autoAck, consumer);

        LOG.debug("publishing client request on {} with correlationId={}", queueName, correlationId);
        channel.basicPublish("", queueName, props, message.getBytes());

        try {
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                //LOG.debug("Verify response corrId: {}", delivery.getProperties().getCorrelationId());
                if (delivery.getProperties().getCorrelationId().equals(correlationId)) {
                    //LOG.debug("found related delivery");
                    response = delivery.getBody();
                    //channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    break;
                }
            }
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return response;
    }

    public Object executeRequest(JSONRPC2Request request) {
        try {
            return this.call(request.toString(), request.getID().toString());
        }
        catch  (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() throws Exception {
        connection.close();
    }
}

