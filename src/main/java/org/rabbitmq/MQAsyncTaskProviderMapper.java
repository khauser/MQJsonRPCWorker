package org.rabbitmq;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import org.rabbitmq.config.ServiceConfiguration;
import org.rabbitmq.utility.Link;
import org.rabbitmq.utility.SystemTime;

@Component
public class MQAsyncTaskProviderMapper extends HashMap<String, Provider<? extends MQAsyncTask>> {

    //private static final Logger log = LoggerFactory.getLogger(MQTaskProviderMapper.class);

    private static final long serialVersionUID = 5595737728649614340L;

    private ServiceConfiguration serviceConfig;

    @Autowired
    public MQAsyncTaskProviderMapper(ServiceConfiguration imageServiceConfig) {
        this.serviceConfig = imageServiceConfig;
    }

    public MQAsyncTask getMQTask(String mqTaskAsyncClassname) {
        MQAsyncTask task = this.get(mqTaskAsyncClassname).get();
        task.setId(UUID.randomUUID().toString());

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serviceConfig.getServiceBaseURL().toString())
                .path("task");
        task.setLink(new Link(builder.build().encode().toString() + "/" + task.getId()));

        task.setType(mqTaskAsyncClassname);
        task.setCreatedAt(SystemTime.asDate());
        return task;
    }

}
