package org.rabbitmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="spring.data.rest")
public class SpringRestProperties {
    
    // @Autowired
    // private static ServiceConfigFileProvider serviceConfigFileProvider;
    
    // private static final String CONFIG_FILE_PATH = serviceConfigFileProvider.getConfigFilePath();
    

    private String baseUri;

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }
}
