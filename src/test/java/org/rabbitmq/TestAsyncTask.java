package org.rabbitmq;

import java.io.IOException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.rabbitmq.exception.JsonConversionException;
import org.rabbitmq.utility.KeyService;
import org.rabbitmq.utility.SystemTime;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;

@JsonSerialize(using = TestAsyncTask.CreateImageAsyncTaskSerializer.class)
public class TestAsyncTask extends MQAsyncTask {
    private static final Logger LOG = LoggerFactory.getLogger(TestAsyncTask.class);

    private KeyService keyService;

    private String taskKey;

    private String guid;

    public void setTestMethodService(KeyService keyService) {
        this.keyService = keyService;
    }

    @Override
    public void setUpTask(NamedParamsRetriever np) throws JSONRPC2Error, JsonConversionException {
        this.setGuid(np.getString("guid"));

        this.setTaskKey(keyService.getTaskKey(this.getId()));

        this.setCreatedAt(SystemTime.asDate());
        this.setStatus(TaskStatusEnum.Pending);
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public void executeTask() throws JsonConversionException, InterruptedException {
        LOG.debug("Executing test task");

        Thread.sleep(2000);
    }

    public static class CreateImageAsyncTaskSerializer extends JsonSerializer<TestAsyncTask> {
        @Override
        public void serialize(TestAsyncTask mqTask, JsonGenerator generator, SerializerProvider provider)
                throws IOException {
            generator.writeStartObject();

            generator.writeStringField("id", mqTask.getId());
            generator.writeObjectField("link", mqTask.getLink());
            generator.writeObjectField("type", mqTask.getType());
            generator.writeStringField("createdAt", mqTask.getCreatedAt().toString());
            DateTime startedAt = mqTask.getStartedAt();
            if (startedAt != null)
                generator.writeStringField("startedAt", startedAt.toString());
            DateTime completedAt = mqTask.getCompletedAt();
            if (completedAt != null)
                generator.writeStringField("completedAt", completedAt.toString());
            generator.writeStringField("state", mqTask.getStatus().name());

            generator.writeObjectFieldStart("data");
            generator.writeStringField("guid", mqTask.getGuid());
            generator.writeEndObject(); // for "data"

            generator.writeEndObject();
        }
    }

}