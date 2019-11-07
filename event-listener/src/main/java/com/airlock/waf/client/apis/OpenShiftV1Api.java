package com.airlock.waf.client.apis;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.*;
import io.kubernetes.client.apis.ExtensionsV1beta1Api;
import io.kubernetes.client.models.V1ObjectMeta;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provide functionality to collect and watch OpenShift objects.
 */
public class OpenShiftV1Api extends ExtensionsV1beta1Api {

    private static final String[] LOCAL_VAR_ACCEPTS = new String[]{
            "application/json",
            "application/yaml",
            "application/vnd.kubernetes.protobuf",
            "application/json;stream=watch",
            "application/vnd.kubernetes.protobuf;stream=watch"
    };

    private ApiClient apiClient = Configuration.getDefaultApiClient();

    public com.squareup.okhttp.Call listRouteForAllNamespacesCall(
            Integer limit,
            String pretty,
            Integer timeoutSeconds,
            Boolean watch) throws ApiException {

        // create path and map variables
        List<Pair> localVarQueryParams = new ArrayList<>();
        if (limit != null) {
            localVarQueryParams.addAll(apiClient.parameterToPair("limit", limit));
        }
        if (pretty != null) {
            localVarQueryParams.addAll(apiClient.parameterToPair("pretty", pretty));
        }
        if (timeoutSeconds != null) {
            localVarQueryParams.addAll(apiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
        }
        if (watch != null) {
            localVarQueryParams.addAll(apiClient.parameterToPair("watch", watch));
        }

        Map<String, String> localVarHeaderParams = new HashMap<>();
        String localVarAccept = apiClient.selectHeaderAccept(LOCAL_VAR_ACCEPTS);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        String localVarContentType = apiClient.selectHeaderContentType(new String[]{"*/*"});
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[]{"BearerToken"};
        return apiClient.buildCall(
                "/apis/route.openshift.io/v1/routes",
                "GET",
                localVarQueryParams,
                new ArrayList<>(),
                null,
                localVarHeaderParams,
                new HashMap<>(),
                localVarAuthNames,
                null);
    }

    public V1RouteList listRouteForAllNamespaces(Integer limit, String pretty, Integer timeoutSeconds, Boolean watch) throws ApiException {
        ApiResponse<V1RouteList> resp = listRouteForAllNamespacesWithHttpInfo(limit, pretty, timeoutSeconds, watch);
        return resp.getData();
    }

    private ApiResponse<V1RouteList> listRouteForAllNamespacesWithHttpInfo(Integer limit, String pretty, Integer timeoutSeconds, Boolean watch) throws ApiException {
        com.squareup.okhttp.Call call = listRouteForAllNamespacesValidateBeforeCall(limit, pretty, timeoutSeconds, watch);
        Type localVarReturnType = new TypeToken<V1RouteList>() {}.getType();
        return apiClient.execute(call, localVarReturnType);
    }

    private com.squareup.okhttp.Call listRouteForAllNamespacesValidateBeforeCall(Integer limit, String pretty, Integer timeoutSeconds, Boolean watch) throws ApiException {
        return listRouteForAllNamespacesCall(limit, pretty, timeoutSeconds, watch);
    }

    @ApiModel
    @Data
    public class V1RouteList {

        @SerializedName("items")
        private List<V1Route> items = new ArrayList<>();
    }

    @ApiModel
    @Data
    public class V1Route {

        @SerializedName("metadata")
        private V1ObjectMeta metadata = null;

        @SerializedName("spec")
        private V1RouteSpec spec = null;
    }

    @ApiModel
    @Data
    public class V1RouteSpec {

        @SerializedName("host")
        private String host;

        @SerializedName("path")
        private String path;

        @SerializedName("to")
        private V1RouteToSpec to;
    }

    @ApiModel
    @Data
    public class V1RouteToSpec {

        @SerializedName("name")
        private String name;
    }
}
