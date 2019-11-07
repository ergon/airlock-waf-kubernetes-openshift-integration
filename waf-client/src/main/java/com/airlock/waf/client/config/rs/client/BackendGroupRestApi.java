package com.airlock.waf.client.config.rs.client;

import ch.ergon.restal.jsonapi.document.data.CollectionDocument;
import ch.ergon.restal.jsonapi.document.data.ResourceObject;
import com.airlock.waf.client.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Provider;

import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Component
public class BackendGroupRestApi extends RestApi {

    @Autowired
    public BackendGroupRestApi(Context context, Provider<RestTemplate> restTemplate) {
        super(context, restTemplate);
    }

    public ResponseEntity<CollectionDocument> getAll(String cookie) {
        return restTemplate().exchange(
                uri("configuration", "back-end-groups"),
                GET,
                httpEntity(cookie),
                new ParameterizedTypeReference<CollectionDocument>() {
                });
    }

    public String getKubernetesBackendGroupId (String cookie) {
        List<ResourceObject> backendGroups = getAll(cookie).getBody().getData();
        return backendGroups.stream().map(e -> e.getId()).findFirst().get();
    }
}
