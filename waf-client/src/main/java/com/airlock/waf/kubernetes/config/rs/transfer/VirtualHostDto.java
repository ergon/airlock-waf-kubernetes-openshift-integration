package com.airlock.waf.kubernetes.config.rs.transfer;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = PRIVATE)
@JsonTypeName(VirtualHostDto.VIRTUAL_HOST_TYPE)
public class VirtualHostDto {

    public static final String VIRTUAL_HOST_TYPE = "virtual-host";

    private String name;

    private String hostName;

    private VirtualHostNetworkInterfaceDto networkInterface;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = PRIVATE)
    public static class VirtualHostNetworkInterfaceDto {

        private String externalLogicalInterfaceName;

        private String ipV4Address;

        private String ipV6Address;

        private HttpDto http;

        private HttpsDto https;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = PRIVATE)
    public static class HttpDto {

        private Boolean enabled;

        private Integer port;

        private Boolean httpsRedirectEnforced;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = PRIVATE)
    public static class HttpsDto {

        private Boolean enabled;

        private Integer port;

        private Boolean http2Allowed;
    }
}
