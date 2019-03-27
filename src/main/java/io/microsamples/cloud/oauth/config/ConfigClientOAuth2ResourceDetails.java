package io.microsamples.cloud.oauth.config;


import lombok.Data;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.util.Arrays;
@Data
@ConfigurationProperties(ConfigClientOAuth2ResourceDetails.PREFIX)
public class ConfigClientOAuth2ResourceDetails implements InitializingBean {
    public static final String PREFIX = "spring.cloud.config.client";

    private BaseOAuth2ProtectedResourceDetails oauth2 = new ClientCredentialsResourceDetails();

    public void afterPropertiesSet() throws Exception {
//        if (oauth2.getScope() == null || oauth2.getScope().isEmpty()) {
//            oauth2.setScope(Arrays.asList("read", "write"));
//        }
    }
}
