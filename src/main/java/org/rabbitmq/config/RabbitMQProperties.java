package org.rabbitmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="service.rabbitmq")
public class RabbitMQProperties {

    private String host;
    private int port;
    private String queue_name;
    private String common_queue_name;
    private int thread_executors = 10;
    private int task_workers;


    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public String getQueue_name() {
        return queue_name;
    }
    public void setQueue_name(String queue_name) {
        this.queue_name = queue_name;
    }

    public String getCommon_queue_name() {
        return common_queue_name;
    }
    public void setCommon_queue_name(String common_queue_name) {
        this.common_queue_name = common_queue_name;
    }

    public int getThread_executors() {
        return thread_executors;
    }
    public void setThread_executors(int thread_executors) {
        this.thread_executors = thread_executors;
    }

    public int getTask_workers() {
        return task_workers;
    }
    public void setTask_workers(int task_workers) {
        this.task_workers = task_workers;
    }
}
