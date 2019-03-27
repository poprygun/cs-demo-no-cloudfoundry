package io.microsamples.cloud.csdemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableConfigurationProperties(CustomProperties.class)
public class CsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CsDemoApplication.class, args);
    }

}

@RestController
class PropertiesServer {

    @Autowired
    private CustomProperties customProperties;


    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "list",  produces= MediaType.APPLICATION_JSON_VALUE)
    private String customProperties() throws JsonProcessingException {

        return objectMapper.writeValueAsString(customProperties);
    }
}



