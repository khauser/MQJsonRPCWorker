package org.rabbitmq;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.rabbitmq.config.ServiceConfiguration;
import org.rabbitmq.config.RabbitMQProperties;
import org.rabbitmq.exception.ErrorCode;
import org.rabbitmq.exception.MQConnectionException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MQServer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MQServer.class);

    protected RabbitMQProperties rabbitMQProperties;

    public ExecutorService executor;

    private Connection connection;

    private Channel channel;

    public Channel getChannel() {
        return channel;
    }

    private String queueName;
    private int threadExecutors;


    @Autowired
    public MQServer(ServiceConfiguration configuration) {
        this.rabbitMQProperties = configuration.getRabbitMQProperties();
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return queueName;
    }

    public int getThreadExecutors() {
        return threadExecutors;
    }

    public void setThreadExecutors(int threadExecutors) {
        this.threadExecutors = threadExecutors;
    }

    public void run() {
        ConnectionFactory factory = new ConnectionFactory();
        try {
            LOG.info("rpc server connection setup for '{}:{}'", rabbitMQProperties.getHost(), rabbitMQProperties.getPort());
            factory.setHost(rabbitMQProperties.getHost());
            factory.setPort(rabbitMQProperties.getPort());

            executor = Executors.newFixedThreadPool(threadExecutors);
            connection = factory.newConnection(executor);

            channel = connection.createChannel();

            LOG.info("rpc request queue declared '{}'", this.queueName);
            channel.queueDeclare(queueName, false, false, false, null);

            channel.basicQos(1);
        }
        catch  (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void close() throws MQConnectionException {
        try {
            connection.close();
        } catch (IOException e) {
            throw new MQConnectionException(ErrorCode.MQ_IO_EXCEPTION, e);
        }
        executor.shutdownNow();
    }
}
