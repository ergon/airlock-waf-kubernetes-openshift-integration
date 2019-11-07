package com.airlock.waf.client.config.rs.client;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AirlockWAFClient {

    private SessionRestApi sessionRestApi;

    private ConfigurationRestApi configurationRestApi;

    private VirtualHostRestApi virtualHostRestApi;

    private MappingRestApi mappingRestApi;

    private BackendGroupRestApi backendGroupRestApi;

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
