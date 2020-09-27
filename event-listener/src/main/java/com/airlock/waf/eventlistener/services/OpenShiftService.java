package com.airlock.waf.eventlistener.services;

import com.airlock.waf.eventlistener.openshift.OpenShiftV1Api;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.util.Watch;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class OpenShiftService {

    private ApiClient apiClient;

    /**
     * Returns a ObjectShift Route Watch Object.
     *
     * @return Returns a ObjectShift Route Watch Object.
     * @throws ApiException If fail to process the API call
     */
    public Watch<V1Namespace> routesEventWatcher() throws ApiException {
        OpenShiftV1Api api = new OpenShiftV1Api();
        Call call = api.listRouteForAllNamespacesCall(null, "true", 600, Boolean.TRUE);
        Type type = new TypeToken<Watch.Response<V1Namespace>>() {}.getType();
        return Watch.createWatch(apiClient, call, type);
    }

    /**
     * Returns a collection of all ObjectShift Route Objects.
     *
     * @return ObjectShift Route Objects
     * @throws ApiException If fail to process the API call
     */
    public Set<OpenShiftV1Api.V1Route> currentRouteSpecification() throws ApiException {
        OpenShiftV1Api api = new OpenShiftV1Api();
        OpenShiftV1Api.V1RouteList result = api.listRouteForAllNamespaces(null, "true", 600, Boolean.FALSE);
        return result.getItems().stream().collect(Collectors.toSet());
    }
}
