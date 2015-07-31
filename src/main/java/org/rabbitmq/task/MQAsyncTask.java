package org.rabbitmq.task;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.rabbitmq.MQAsyncWorker;
import org.rabbitmq.exception.JsonConversionException;
import org.rabbitmq.utility.Link;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;

public abstract class MQAsyncTask implements Runnable {
    //private static final Logger LOG = LoggerFactory.getLogger(MQAsyncTask.class);

    private String id;
    private String type;
    private Link link;

    public static enum TaskStatusEnum {
        Pending, Busy, Completed, Failure
    }
    private TaskStatusEnum status;

    private MQAsyncWorker worker;
    private Object requestId;

    private DateTime createdAt;
    private DateTime startedAt;
    private DateTime completedAt;

    private StackTraceElement[] error;

    public MQAsyncTask() {}

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return this.id;
    }

    public final Link getLink() {
        return link;
    }
    public final void setLink(Link link) {
        this.link = link;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MQAsyncWorker getWorker() {
        return worker;
    }
    public void setWorker(MQAsyncWorker worker) {
        this.worker = worker;
    }

    public Object getRequestId() {
        return requestId;
    }
    public void setRequestId(Object requestId) {
        this.requestId = requestId;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(DateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DateTime getStartedAt() {
        return startedAt;
    }
    public void setStartedAt(DateTime startedAt) {
        this.startedAt = startedAt;
    }

    public DateTime getCompletedAt() {
        return completedAt;
    }
    public void setCompletedAt(DateTime completedAt) {
        this.completedAt = completedAt;
    }

    public TaskStatusEnum getStatus() {
        return status;
    }
    public void setStatus(TaskStatusEnum status) {
        this.status = status;
    }

    public void setError(Throwable e) {
        this.error = e.getStackTrace();
    }
    public StackTraceElement[] getError() {
        return this.error;
    }

    public String getErrorStr() {
        return Arrays.toString(this.getError());
    }


    public void run() {
        try {
            this.executeTask();
            worker.setProcessed(worker.getProcessed() + 1);
        }
        catch (Exception e) {
            //TODO check out error handling
            e.printStackTrace();
        }
    }

    public abstract void setUpTask(NamedParamsRetriever np) throws JSONRPC2Error, JsonConversionException;

    public abstract void executeTask() throws Exception;

}
