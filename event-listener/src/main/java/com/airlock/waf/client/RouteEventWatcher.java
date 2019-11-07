package com.airlock.waf.client;

import com.airlock.waf.client.domain.ActivationEvent;
import com.airlock.waf.client.domain.ActivationEvent.Path;
import com.airlock.waf.client.domain.ActivationEvent.Rule;
import com.airlock.waf.client.domain.ActivationEvent.Specification;
import com.airlock.waf.client.services.AirlockConfigurationService;
import com.airlock.waf.client.apis.OpenShiftV1Api.V1Route;
import com.airlock.waf.client.services.OpenShiftService;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.util.Watch;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

@Profile("openshift")
@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class RouteEventWatcher implements CommandLineRunner {

    private AirlockConfigurationService airlockConfigurationService;

    private OpenShiftService openShiftService;

    @Override
    public void run(String... args) throws Exception {
        try (Watch<V1Namespace> watch = openShiftService.routesEventWatcher()) {
            watch.forEach(item -> {
                try {
                    System.out.printf("Catch Kubernetes Routes event: %s [%s]%n", item.object.getMetadata().getName(), item.type);

                    Set<V1Route> currentRoutes = openShiftService.currentRouteSpecification();
                    ActivationEvent event = asIngressEvent(item, currentRoutes);
                    airlockConfigurationService.update(event);
                }
                catch (Exception e) {
                    System.err.printf("An unexpected error occurs: %s%n", e.getMessage());
                    e.printStackTrace();
                }
            });
        }
        catch (ApiException ae) {
            System.err.printf("Misconfiguration: %s - %s%n", ae.getMessage(), ae.getResponseBody());
            ae.printStackTrace();
        }
    }

    private ActivationEvent asIngressEvent(Watch.Response<V1Namespace> item, Set<V1Route> currentRoutes) {
        String comment = "Triggered by OpenShift Route Event: " + item.object.getMetadata().getName() + " [" + item.type + "]";
        Set<Specification> specs = currentRoutes.stream().map(i -> asSpecification(i)).collect(toSet());
        return ActivationEvent.builder().comment(comment).specifications(specs).build();
    }

    private Specification asSpecification(V1Route route) {
        Path path = Path.builder().path(route.getSpec().getPath()).backendServiceName(route.getSpec().getTo().getName()).build();
        Rule rule = Rule.builder().host(route.getSpec().getHost()).paths(asList(path)).build();
        Map<String, String> annotations = route.getMetadata().getAnnotations();
        if(annotations == null) {
            annotations = new HashMap<>();
        }
        return Specification.builder().rules(asList(rule)).annotation(asAnnotation(annotations)).build();
    }

    private ActivationEvent.Annotation asAnnotation(Map<String, String> annotations) {

        return ActivationEvent.Annotation.builder().metaData(annotations).build();
    }
}
