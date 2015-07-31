package org.rabbitmq;

import java.util.concurrent.ExecutorService;

import org.rabbitmq.exception.JsonConversionException;
import org.rabbitmq.task.MQAsyncTask;
import org.rabbitmq.task.MQAsyncTaskMapper;
import org.rabbitmq.task.MQAsyncTaskProviderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;

public class TestMQAsyncWorker extends MQAsyncWorker {
    private static final Logger LOG = LoggerFactory.getLogger(TestMQAsyncWorker.class);

    private final ExecutorService executorService;

    private final MQAsyncTaskProviderMapper providerMapper;

    private final MQAsyncTaskMapper taskMapper;

    public TestMQAsyncWorker(int workerId, ExecutorService threadExecutor, Channel channel, String queue
            , MQAsyncTaskProviderMapper providerMapper, MQAsyncTaskMapper taskMapper) {
        super(channel, queue);
        this.setWorkerId(workerId);
        this.executorService = threadExecutor;
        this.providerMapper = providerMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    public void initializeRequest(JSONRPC2Request request, NamedParamsRetriever np) throws JSONRPC2Error,  JsonConversionException {
        Class<? extends MQAsyncTask> taskClass = taskMapper.get(request.getMethod());
        if (taskClass == null) {
            throw JSONRPC2Error.METHOD_NOT_FOUND;
        }
        LOG.debug("Worker {} submitting {} as {}th task", this.getWorkerId(), taskClass.getSimpleName(),
                this.getProcessed());
        MQAsyncTask task = providerMapper.getMQTask(taskClass.getName());
        initializeTask(task, this, request.getID());
        task.setUpTask(np);
        LOG.debug("Worker {} initialized task {}", this.getWorkerId(), task.getId());
        this.setTask(task);
    }

    @Override
    public void processRequest() {
        executorService.submit(this.getTask());
    }
}
