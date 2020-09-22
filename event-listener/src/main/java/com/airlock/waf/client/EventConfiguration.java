package com.airlock.waf.client;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ch.ergon.restal.jsonapi.jackson.JsonApiJacksonConfigurator;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.credentials.ClientCertificateAuthentication;
import io.kubernetes.client.util.credentials.TokenFileAuthentication;

@Configuration
public class EventConfiguration {

    @Autowired
    private Context context;

    @Bean
    public ApiClient apiClient() throws IOException {
        ApiClient client = Config.defaultClient();
        client.setBasePath(context.kubernetes().apiServer());
        client.setReadTimeout(6000);
        client.setVerifyingSsl(false);
        new TokenFileAuthentication(context.kubernetes().tokenFilePath()).provide(client);;
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        return client;
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public RestTemplate restTemplate(MappingJackson2HttpMessageConverter messageConverter) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(HttpClientBuilder.create()
                .disableRedirectHandling()
                .disableCookieManagement()
                .setSSLContext(sslContext())
                .setSSLHostnameVerifier(hostnameVerifier())
                .build());
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        registerMessageConverters(messageConverter, restTemplate);
        return restTemplate;
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper);
        return messageConverter;
    }

    @Bean
    public ObjectMapper objectMapper() {

        ObjectMapper defaultMapper = new ObjectMapper();
        defaultMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        defaultMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        defaultMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        defaultMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));

        JsonApiJacksonConfigurator.configure(defaultMapper, "com.airlock.waf.kubernetes.config.rs.transfer");
        return defaultMapper;
    }

    private void registerMessageConverters(MappingJackson2HttpMessageConverter messageConverter, RestTemplate restTemplate) {
        restTemplate.getMessageConverters().removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
        restTemplate.getMessageConverters().add(messageConverter);
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
    }

    private HostnameVerifier hostnameVerifier() {

        return (hostname, session) -> true;
    }

    private SSLContext sslContext() {

        try {
            TrustManager[] unquestioningTrustManager = new TrustManager[]{
                    new X509TrustManager() {

                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {

                            return new X509Certificate[0];
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {

                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {

                        }
                    }
            };
            final SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, unquestioningTrustManager, null);
            return context;
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
