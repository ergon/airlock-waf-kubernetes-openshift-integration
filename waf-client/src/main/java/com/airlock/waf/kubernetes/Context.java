package com.airlock.waf.kubernetes;

import com.google.common.io.Resources;
import io.kubernetes.client.util.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

@Component
@Getter
@Accessors(fluent = true)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class Context {

    private AirlockWAFContext waf;

    private KubernetesContext kubernetes;

    @Component
    @NoArgsConstructor(onConstructor = @__(@Autowired))
    public static class KubernetesContext {

        @Value("${airlock.waf.kubernetes.user.certificate.key.path}")
        private String clientCertificateKeyPath;

        @Value("${airlock.waf.kubernetes.user.certificate.path}")
        private String clientCertificatePath ;

        @Value("${airlock.waf.kubernetes.api.server:}")
        private String apiServer;

        public byte[] clientKey() throws IOException {
            URL resource = Resources.getResource(clientCertificateKeyPath);
            return Resources.toByteArray(resource);
        }

        public byte[] clientCertificate() throws IOException {
            URL resource = Resources.getResource(clientCertificatePath);
            return Resources.toByteArray(resource);
        }

        public String apiServer() {
            if(apiServer != null && apiServer.trim().length() > 0) {
                return apiServer;
            }
            try {
                return Config.defaultClient().getBasePath();
            }
            catch (IOException e) {
                throw new IllegalStateException("Could not get kubernetes api server address", e);
            }
        }
    }

    @Component
    @Accessors(fluent = true)
    @NoArgsConstructor(onConstructor = @__(@Autowired))
    public static class AirlockWAFContext {

        @Value("${airlock.waf.host}")
        private String host;

        @Value("${airlock.waf.token.file}")
        private String tokenFile;

        @Value("${airlock.waf.base.config.comment}")
        @Getter
        private String baseConfigComment;

        @Value("${airlock.waf.virtualhost.ip}")
        @Getter
        private String ipv4Address;

        @Value("${airlock.waf.virtualhost.port}")
        @Getter
        private Integer port;

        @Value("${airlock.waf.external.logical.name}")
        @Getter
        private String externalLogicalName;

        public String token() {
            try {
                URL resource = Resources.getResource(tokenFile);
                return Resources.toString(resource, Charset.defaultCharset());
            }
            catch (IOException e) {
                throw new IllegalStateException("Airlock WAF JWT token is invalid.");
            }
        }

        public URI uri(String... pathSegments) {

            return UriComponentsBuilder.fromHttpUrl(host)
                    .pathSegment("airlock", "rest")
                    .pathSegment(pathSegments)
                    .build()
                    .encode()
                    .toUri();
        }

        public String authorizationHeaderValue() {

            return "Bearer " + token();
        }
    }
}
