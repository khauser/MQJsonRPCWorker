package org.rabbitmq.config;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({RabbitMQProperties.class, ServerProperties.class, SpringRestProperties.class})
public class ServiceConfiguration {

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private SpringRestProperties restProperties;

    @Autowired
    private RabbitMQProperties rabbitMQProperties;


    public RabbitMQProperties getRabbitMQProperties() {
        return rabbitMQProperties;
    }

    public URI getServiceBaseURL() {
        String baseUrl;
        try {
            baseUrl = "http://"
                    + ((serverProperties.getAddress() == null || "0.0.0.0".equals(serverProperties.getAddress())) ? InetAddress.getLocalHost().getCanonicalHostName() : serverProperties.getAddress())
                    + ":" + serverProperties.getPort() + "/"
                    + (restProperties.getBaseUri() != null && !restProperties.getBaseUri().isEmpty() ? (restProperties.getBaseUri() + "/") : "");
        } catch (UnknownHostException e) {
            baseUrl = "http://localhost"
                    + ":" + serverProperties.getPort() + "/"
                    + (restProperties.getBaseUri().isEmpty() ? (restProperties.getBaseUri() + "/") : "");
        }
        return URI.create(baseUrl);
    }

}
