package org.rabbitmq;

import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.rabbitmq.utility.KeyService;
import org.rabbitmq.MQAsyncTaskMapper;
import org.rabbitmq.MQAsyncTaskProviderMapper;

@Component
public final class TestAsyncTaskProvider implements Provider<TestAsyncTask> {
    private static final Logger LOG = LoggerFactory.getLogger(TestAsyncTaskProvider.class);

    private static final String METHOD_NAME = "testMethod";

    private final KeyService keyService;


    @Autowired
    public TestAsyncTaskProvider(KeyService keyService) {
        this.keyService = keyService;
    }

    @Autowired
    public void register(MQAsyncTaskProviderMapper providerMapper) {
        LOG.debug("register {}", TestAsyncTask.class.getName());
        providerMapper.put(TestAsyncTask.class.getName(), this);
    }

    @Autowired
    public void register(MQAsyncTaskMapper taskMapper) {
        LOG.debug("bind method {} to task {}", METHOD_NAME, TestAsyncTask.class.getSimpleName());
        taskMapper.put(METHOD_NAME, TestAsyncTask.class);
    }

    @Override
    public TestAsyncTask get() {
        final TestAsyncTask task = new TestAsyncTask();
        task.setTestMethodService(this.keyService);
        return task;
    }

}
