package com.airlock.waf.client.config.rs.client;

import ch.ergon.restal.jsonapi.document.data.ResourceDocument;
import com.airlock.waf.client.Context;
import com.airlock.waf.client.config.rs.transfer.CollectionRelationshipDocument;
import com.airlock.waf.client.config.rs.transfer.VirtualHostDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Provider;

import static com.airlock.waf.client.config.rs.transfer.CollectionRelationshipDocument.collectionRelationshipDocumentBuilder;
import static com.airlock.waf.client.config.rs.transfer.MappingDto.MAPPING_TYPE;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;

@Component
public class VirtualHostRestApi extends RestApi {

    @Autowired
    public VirtualHostRestApi(Context context, Provider<RestTemplate> restTemplate) {
        super(context, restTemplate);
    }

    public String create(String cookie, VirtualHostDto dto) {
        ResponseEntity<ResourceDocument> response = restTemplate().exchange(
                uri("configuration", "virtual-hosts"),
                POST,
                httpEntity(toRequestDocument(dto), cookie),
                new ParameterizedTypeReference<ResourceDocument>() {
                });
        return response.getBody().getData().getId();
    }

    public ResponseEntity<ResourceDocument> connectMapping(String cookie, String vhId, String mId) {
        CollectionRelationshipDocument requestData = collectionRelationshipDocumentBuilder()
                .relationship(MAPPING_TYPE, Long.valueOf(mId))
                .build();
        return restTemplate().exchange(
                uri("configuration", "virtual-hosts", vhId, "relationships", "mappings"),
                PATCH,
                httpEntity(requestData, cookie),
                new ParameterizedTypeReference<ResourceDocument>() {});
    }
}
