package io.microsamples.cloud.csdemo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "rabbitmq")
@Data
public class CustomProperties {
    private String username;
    private String password;
}
