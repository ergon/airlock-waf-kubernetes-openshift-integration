package com.airlock.waf.kubernetes.config.rs.client;

import com.airlock.waf.kubernetes.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Provider;

import static org.springframework.http.HttpMethod.POST;

@Component
public class SessionRestApi extends RestApi {

    @Autowired
    public SessionRestApi(Context context, Provider<RestTemplate> restTemplate) {
        super(context, restTemplate);
    }

    public String create() {
        ResponseEntity<Void> response = restTemplate().exchange(
                uri("session", "create"),
                POST,
                new HttpEntity<>(defaultHttpHeaders()),
                new ParameterizedTypeReference<Void>() {});
        return response.getHeaders().get("Set-Cookie").get(0);
    }

    public ResponseEntity<Void> terminate(String cookie) {
        return restTemplate().exchange(
                uri("session", "terminate"),
                POST,
                httpEntity(cookie),
                new ParameterizedTypeReference<Void>() {});
    }
}
