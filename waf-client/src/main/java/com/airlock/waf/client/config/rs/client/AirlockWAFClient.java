package com.airlock.waf.client.config.rs.client;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AirlockWAFClient {

    private final SessionRestApi sessionRestApi;

    private final ConfigurationRestApi configurationRestApi;

    private final VirtualHostRestApi virtualHostRestApi;

    private final MappingRestApi mappingRestApi;

    private final BackendGroupRestApi backendGroupRestApi;

    public SessionRestApi session() {

        return sessionRestApi;
    }

    public ConfigurationRestApi configuration() {

        return configurationRestApi;
    }

    public VirtualHostRestApi virtualhost() {

        return virtualHostRestApi;
    }

    public MappingRestApi mapping() {

        return mappingRestApi;
    }

    public BackendGroupRestApi backendgroup() {

        return backendGroupRestApi;
    }

}
