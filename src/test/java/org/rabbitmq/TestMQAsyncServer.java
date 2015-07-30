package org.rabbitmq;

import org.rabbitmq.config.ServiceConfiguration;
import org.rabbitmq.task.MQAsyncTaskMapper;
import org.rabbitmq.task.MQAsyncTaskProviderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestMQAsyncServer extends MQServer {
    private static final Logger LOG = LoggerFactory.getLogger(TestMQAsyncServer.class);

    private final MQAsyncTaskMapper taskMapper;

    private final MQAsyncTaskProviderMapper providerMapper;

    private int taskWorkers;


    @Autowired
    public TestMQAsyncServer(ServiceConfiguration configuration
            , TestAsyncTaskProvider createImageAsyncTaskProvider
            , MQAsyncTaskProviderMapper providerMapper
            , MQAsyncTaskMapper taskMapper) {
        super(configuration);
        this.providerMapper = providerMapper;
        this.taskMapper = taskMapper;
        this.taskWorkers = rabbitMQProperties.getTask_workers();

        this.setQueueName(rabbitMQProperties.getCommon_queue_name());
        this.setThreadExecutors(rabbitMQProperties.getThread_executors());

        createImageAsyncTaskProvider.register(taskMapper);
        createImageAsyncTaskProvider.register(providerMapper);

        new Thread(this).start();
    }

    @Override
    public void run() {
        super.run();
        LOG.debug("start running");

        for (int i = 0; i < taskWorkers; i++) {
            TestMQAsyncWorker worker = new TestMQAsyncWorker(i, executor, this.getChannel(), this.getQueueName(), providerMapper, taskMapper);
            this.executor.execute(worker);
        }
    }



}
