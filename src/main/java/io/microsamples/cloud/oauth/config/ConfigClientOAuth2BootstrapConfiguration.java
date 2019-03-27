package io.microsamples.cloud.oauth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableConfigurationProperties(ConfigClientOAuth2ResourceDetails.class)
@ConditionalOnClass({ConfigServicePropertySourceLocator.class, OAuth2RestTemplate.class})
@ConditionalOnProperty(value = ConfigClientOAuth2ResourceDetails.PREFIX + ".oauth2.clientId")
public class ConfigClientOAuth2BootstrapConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(ConfigClientOAuth2ResourceDetails.class)
    public ConfigClientOAuth2ResourceDetails configClientOAuth2ResourceDetails() {
        return new ConfigClientOAuth2ResourceDetails();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(value = "spring.cloud.config.enabled", matchIfMissing = true)
    protected ConfigClientOAuth2Configurer configClientOAuth2Configurator(ConfigServicePropertySourceLocator locator,
                                                                          ConfigClientOAuth2ResourceDetails configClientOAuth2ResourceDetails) {
        return new ConfigClientOAuth2Configurer(locator, configClientOAuth2ResourceDetails);
    }

    protected static class ConfigClientOAuth2Configurer {

        private final ConfigServicePropertySourceLocator locator;

        private final ConfigClientOAuth2ResourceDetails configClientOAuth2ResourceDetails;

        public ConfigClientOAuth2Configurer(ConfigServicePropertySourceLocator locator,
                                            ConfigClientOAuth2ResourceDetails configClientOAuth2ResourceDetails) {
            this.locator = locator;
            this.configClientOAuth2ResourceDetails = configClientOAuth2ResourceDetails;
        }

        @PostConstruct
        public void init() {
            OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(this.configClientOAuth2ResourceDetails.getOauth2());

//            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
//            interceptors.add(new LoggingRequestInterceptor());
//
//            restTemplate.setInterceptors(interceptors);
            restTemplate.setAccessTokenProvider(promiscuousAccessTokenProvider());
            disableCertificateChecks(restTemplate);
            this.locator.setRestTemplate(restTemplate);
        }

        private AccessTokenProvider promiscuousAccessTokenProvider() {
            AccessTokenProvider accessTokenProvider = new AccessTokenProviderChain(Arrays.<AccessTokenProvider> asList(
                    new AuthorizationCodeAccessTokenProvider(), new ImplicitAccessTokenProvider(),
                    new ResourceOwnerPasswordAccessTokenProvider(), new ClientCredentialsAccessTokenProvider()));

            return accessTokenProvider;
        }

        private static void disableCertificateChecks(OAuth2RestTemplate oauthTemplate) {

            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[] { new Dumb509TrustManager() }, null);
                ClientHttpRequestFactory requestFactory = new SSLContextRequestFactory(sslContext);

                //This is for OAuth protected resources
                oauthTemplate.setRequestFactory(requestFactory);

                ClientCredentialsAccessTokenProvider provider = new ClientCredentialsAccessTokenProvider();
                provider.setRequestFactory(requestFactory);
                oauthTemplate.setAccessTokenProvider(provider);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}

class SSLContextRequestFactory extends SimpleClientHttpRequestFactory {

    private final SSLContext sslContext;

    public SSLContextRequestFactory(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setSSLSocketFactory(sslContext.getSocketFactory());
        }
        super.prepareConnection(connection, httpMethod);
    }
}

class Dumb509TrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
            throws CertificateException {

    }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
            throws CertificateException {

    }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
