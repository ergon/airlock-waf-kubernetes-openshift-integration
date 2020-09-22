package com.airlock.waf.client.services;

import com.airlock.waf.client.Context;
import com.airlock.waf.client.config.rs.client.AirlockWAFClient;
import com.airlock.waf.client.config.rs.transfer.MappingDto;
import com.airlock.waf.client.config.rs.transfer.VirtualHostDto;
import com.airlock.waf.client.domain.ActivationEvent;
import com.airlock.waf.client.domain.ActivationEvent.Path;
import com.airlock.waf.client.domain.ActivationEvent.Rule;
import com.airlock.waf.client.domain.ActivationEvent.Specification;
import io.kubernetes.client.openapi.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;
import java.util.Set;

@Service
public class AirlockConfigurationService {

    private Set<Specification> lastSpecifications;

    private final AirlockWAFClient client;

    private final Context context;

    private final KubernetesService kubernetesService;

    @Autowired
    public AirlockConfigurationService(AirlockWAFClient client, Context context, KubernetesService kubernetesService) {
        this.client = client;
        this.context = context;
        this.kubernetesService = kubernetesService;
    }

    /**
     * Create a new Airlock WAF configuration and activate it.
     *
     * @param event
     */
    public void update(ActivationEvent event) {

        // only update configuration when ingress specification has changed..
        if (requireUpdate(event)) {
            System.out.println("Building new Airlock WAF configuration...");
            long startTime = System.currentTimeMillis();
            String cookie = null;
            try {
                cookie = client.session().create();
                client.configuration().loadBaseConfig(cookie);
                String backendGroupId = client.backendgroup().getKubernetesBackendGroupId(cookie);
                for (Specification specification : event.getSpecifications()) {
                    for (Rule rule : specification.getRules()) {

                        // create virtual host
                        VirtualHostDto virtualHostDto = virtualHostDto(rule);
                        String virtualHostId = client.virtualhost().create(cookie, virtualHostDto);

                        // iterate over paths and create for each path a mapping
                        for (Path path : rule.getPaths()) {
                            String mappingId = null;
                            MappingDto mappingDto = mappingDto(specification, rule, path);

                            // create mapping based on specific mapping template...
                            Optional<String> mappingTemplateName = specification.getAnnotation().mappingTemplateId();
                            if (mappingTemplateName.isPresent()) {
                                byte[] templateAsByteArray = getMappingTemplate(mappingTemplateName.get());
                                mappingId = client.mapping().importFromFile(cookie, templateAsByteArray);
                                client.mapping().update(cookie, mappingId, mappingDto);
                            }
                            // or from empty mapping template
                            else {
                                mappingId = client.mapping().create(cookie, mappingDto);
                            }

                            // connect mapping with virtual host and default back-end group
                            client.virtualhost().connectMapping(cookie, virtualHostId, mappingId);
                            client.mapping().connectBackendGroup(cookie, mappingId, backendGroupId);
                        }
                    }
                }
                client.configuration().activate(cookie, event.getComment());
                lastSpecifications = event.getSpecifications();
                System.out.printf("Airlock WAF configuration activated: Duration %sms%n", (System.currentTimeMillis() - startTime));
            } catch (HttpClientErrorException.NotFound | ResourceAccessException notFound) {
                System.err.println("Could not connect to Airlock WAF. Probably it is a misconfiguration or Airlock WAF is not runnning.");
            } catch (ApiException apiExcpetion) {
                System.err.println("Could not load mapping template from Kubernetes ConfigMap.");
            } finally {
                if (cookie != null) {
                    client.session().terminate(cookie);
                }
            }
        } else {
            System.out.println("Ingress event does not affect Airlock WAF configuration.");
        }
    }

    private VirtualHostDto virtualHostDto(Rule rule) {
        return VirtualHostDto.builder()
                .name(rule.getHost())
                .hostName(rule.getHost())
                .networkInterface(createVirtualHostNetworkInterfaceDto())
                .build();
    }

    private MappingDto.MappingEntryPathDto createMappingEntryPathDto(Path path) {
        return MappingDto.MappingEntryPathDto.builder()
                .value(path.getPath())
                .build();
    }

    private VirtualHostDto.HttpDto createVirtualHostHttpDto() {
        return VirtualHostDto.HttpDto.builder()
                .enabled(true)
                .port(context.waf().port())
                .build();
    }

    private VirtualHostDto.VirtualHostNetworkInterfaceDto createVirtualHostNetworkInterfaceDto() {
        return VirtualHostDto.VirtualHostNetworkInterfaceDto.builder()
                .ipV4Address(context.waf().ipv4Address())
                .http(createVirtualHostHttpDto())
                .externalLogicalInterfaceName(context.waf().externalLogicalName())
                .build();
    }

    private MappingDto mappingDto(Specification specification, Rule rule, Path path) {
        return MappingDto.builder()
                .name(buildMappingName(specification.getAnnotation().mappingName(), rule.getHost(), path))
                .entryPath(createMappingEntryPathDto(path))
                .backendPath(path.getPath())
                .operationalMode("INTEGRATION")
                .build();
    }

    private String buildMappingName(Optional<String> mappingName, String hostName, Path path) {
        return mappingName.isPresent() ? mappingName.get() : hostName + "_" + path.getBackendServiceName();
    }

    private byte[] getMappingTemplate(String id) throws ApiException {
        return kubernetesService.getMappingTemplateById(id);
    }

    private boolean requireUpdate(ActivationEvent event) {
        return lastSpecifications == null || !lastSpecifications.equals(event.getSpecifications());
    }
}
