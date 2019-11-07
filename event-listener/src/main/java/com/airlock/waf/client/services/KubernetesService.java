package com.airlock.waf.client.services;

import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.Call;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.models.V1ConfigMapList;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1beta1Ingress;
import io.kubernetes.client.models.V1beta1IngressList;
import io.kubernetes.client.util.Watch;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class KubernetesService {

    private ApiClient apiClient;

    /**
     * Returns a Kubernetes Ingress Watch Object.
     *
     * @return Ingress Watch object
     * @throws ApiException If fail to process the API call
     */
    public Watch<V1Namespace> ingressEventWatcher() throws ApiException {
        ExtensionsV1beta1Api api = new ExtensionsV1beta1Api();
        Call call = api.listIngressForAllNamespacesCall(null, null, null, null, null, "true", null, 600, Boolean.TRUE, null, null);
        Type type = new TypeToken<Watch.Response<V1Namespace>>() {
        }.getType();
        return Watch.createWatch(apiClient, call, type);
    }

    /**
     * Returns a collection of all Kubernetes Ingress Objects.
     *
     * @return Kubernetes Ingress Objects
     * @throws ApiException If fail to process the API call
     */
    public Set<V1beta1Ingress> currentIngressSpecification() throws ApiException {
        ExtensionsV1beta1Api api = new ExtensionsV1beta1Api();
        V1beta1IngressList result = api.listIngressForAllNamespaces(null, null, null, null, null, "true", null, 600, Boolean.FALSE);
        return new HashSet<>(result.getItems());
    }

    public byte[] getMappingTemplateById(String id) throws ApiException {

        CoreV1Api api = new CoreV1Api(apiClient);
        V1ConfigMapList configMap = api.listConfigMapForAllNamespaces(null, null, null, null, null, "true", null, 600, Boolean.FALSE);
        return configMap.getItems().stream().flatMap(i -> i.getBinaryData().entrySet().stream()).filter(i -> i.getKey().equals(id)).findAny().get().getValue();
    }

}
