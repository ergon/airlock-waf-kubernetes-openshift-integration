package com.airlock.waf.kubernetes.config.rs.client;


import ch.ergon.restal.jsonapi.document.data.ResourceDocument;
import com.airlock.waf.kubernetes.Context;
import com.airlock.waf.kubernetes.config.rs.transfer.CollectionRelationshipDocument;
import com.airlock.waf.kubernetes.config.rs.transfer.MappingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Provider;

import static com.airlock.waf.kubernetes.config.rs.transfer.CollectionRelationshipDocument.collectionRelationshipDocumentBuilder;
import static com.airlock.waf.kubernetes.config.rs.transfer.MappingDto.MAPPING_TYPE;
import static org.springframework.http.HttpMethod.*;

@Component
public class MappingRestApi extends RestApi {

    @Autowired
    public MappingRestApi(Context context, Provider<RestTemplate> restTemplate) {
        super(context, restTemplate);
    }

    public String create(String cookie, MappingDto dto) {
        ResponseEntity<ResourceDocument> response = restTemplate().exchange(
                uri("configuration", "mappings"),
                POST,
                httpEntity(toRequestDocument(dto), cookie),
                new ParameterizedTypeReference<ResourceDocument>() {
                });
        return response.getBody().getData().getId();
    }

    public void update(String cookie, String mappingId, MappingDto dto) {
        ResponseEntity<ResourceDocument> response = restTemplate().exchange(
                uri("configuration", "mappings", mappingId),
                PATCH,
                httpEntity(toRequestDocument(dto), cookie),
                new ParameterizedTypeReference<ResourceDocument>() {
                });
    }

    public String importFromFile(String cookie, byte[] template) {
        ResponseEntity<ResourceDocument> response = restTemplate().exchange(
                uri("configuration", "mappings", "import-mapping"),
                PUT,
                new HttpEntity(template, zipHttpHeaders(cookie)),
                new ParameterizedTypeReference<ResourceDocument>() {
                });
        return response.getBody().getData().getId();
    }

    public ResponseEntity<ResourceDocument> connectBackendGroup(String cookie, String mId, String bId) {
        CollectionRelationshipDocument requestData = collectionRelationshipDocumentBuilder().relationship(MAPPING_TYPE, Long.valueOf(mId)).build();
        return restTemplate().exchange(
                uri("configuration", "back-end-groups", bId, "relationships", "mappings"),
                PATCH,
                httpEntity(requestData, cookie),
                new ParameterizedTypeReference<ResourceDocument>() {
                });
    }
}
