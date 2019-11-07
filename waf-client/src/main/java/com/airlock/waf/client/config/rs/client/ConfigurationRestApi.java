package com.airlock.waf.client.config.rs.client;

import ch.ergon.restal.jsonapi.document.data.CollectionDocument;
import ch.ergon.restal.jsonapi.document.data.ResourceObject;
import com.airlock.waf.client.Context;
import com.airlock.waf.client.config.rs.transfer.ActivationCommentDto;
import com.airlock.waf.client.config.rs.transfer.ConfigFileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Provider;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Component
public class ConfigurationRestApi extends RestApi {

    @Autowired
    public ConfigurationRestApi(Context context, Provider<RestTemplate> restTemplate) {
        super(context, restTemplate);
    }

    public ResponseEntity<CollectionDocument> getAll(String cookie) {
        return restTemplate().exchange(
                uri("configuration", "configurations"),
                GET,
                httpEntity(cookie),
                new ParameterizedTypeReference<CollectionDocument>() {});
    }

    public ResponseEntity<Void> loadBaseConfig(String cookie) {
        List<ResourceObject<ConfigFileDto>> configuration = getAll(cookie).getBody().getData();
        String id = configuration.stream()
                .filter(e -> e.getAttributes().getComment().equals(context().waf().baseConfigComment()))
                .map(e -> e.getId())
                .findFirst()
                .get();
        return restTemplate().exchange(
                uri("configuration", "configurations", id, "load"),
                POST,
                httpEntity(cookie),
                new ParameterizedTypeReference<Void>() {});
    }

    public ResponseEntity<Void> activate(String cookie, String comment) {
        ActivationCommentDto dto = ActivationCommentDto.builder().comment(comment).build();
        return restTemplate().exchange(
                uri("configuration", "configurations", "activate"),
                POST,
                httpEntity(dto, cookie),
                new ParameterizedTypeReference<Void>() {});
    }
}

