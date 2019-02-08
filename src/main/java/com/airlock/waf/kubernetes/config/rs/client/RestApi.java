package com.airlock.waf.kubernetes.config.rs.client;

import ch.ergon.restal.jsonapi.document.data.ResourceObject;
import ch.ergon.restal.jsonapi.document.request.RequestDocument;
import com.airlock.waf.kubernetes.Context;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import javax.inject.Provider;
import java.net.URI;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AllArgsConstructor(onConstructor = @__(@Autowired))
public abstract class RestApi {

    private Context context;

    private Provider<RestTemplate> restTemplate;

    protected <R> RequestDocument<R> toRequestDocument(R dto) {
        RequestDocument<R> doc = new RequestDocument<>();
        ResourceObject data = ResourceObject.builder(dto).build();
        doc.setData(data);
        return doc;
    }

    protected HttpEntity httpEntity(Object dto, String cookie) {
        return new HttpEntity<>(dto, defaultHttpHeaders(cookie));
    }

    protected HttpEntity httpEntity(String cookie) {
        return new HttpEntity<>(defaultHttpHeaders(cookie));
    }

    protected RestTemplate restTemplate() {
        return restTemplate.get();
    }

    protected Context context() {
        return context;
    }

    protected HttpHeaders defaultHttpHeaders(String cookie) {
        return defaultHttpHeaders(APPLICATION_JSON_VALUE, Optional.of(cookie));
    }

    protected HttpHeaders defaultHttpHeaders() {
        return defaultHttpHeaders(APPLICATION_JSON_VALUE, Optional.empty());
    }

    protected HttpHeaders zipHttpHeaders(String cookie) {
        return defaultHttpHeaders("application/zip", Optional.of(cookie));
    }

    protected HttpHeaders defaultHttpHeaders(String contentType, Optional<String> cookie) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add(ACCEPT, APPLICATION_JSON_VALUE);
        requestHeaders.add(AUTHORIZATION, context.waf().authorizationHeaderValue());
        requestHeaders.add(CONTENT_TYPE, contentType);
        cookie.ifPresent(c -> requestHeaders.add("Cookie", c));
        return requestHeaders;
    }

    protected URI uri(String... pathSegments) {
        return context.waf().uri(pathSegments);
    }
}
