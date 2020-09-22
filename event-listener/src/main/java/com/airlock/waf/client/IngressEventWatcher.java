package com.airlock.waf.client;

/*
@Profile("kubernetes")
@Component
@AllArgsConstructor
public class IngressEventWatcher implements CommandLineRunner {

    private final AirlockConfigurationService airlockConfigurationService;

    private final KubernetesService kubernetesService;

    @Override
    public void run(String... args) throws Exception {
        try (Watch<V1Namespace> watch = kubernetesService.ingressEventWatcher()) {
            watch.forEach(item -> {
                try {
                    System.out.printf("Catch Kubernetes Ingress event: %s [%s]%n", item.object.getMetadata().getName(), item.type);
                    Set<V1beta1IngressClass> currentIngressSpecification = kubernetesService.currentIngressSpecification();
                    ActivationEvent event = asIngressEvent(item, currentIngressSpecification);
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

    private ActivationEvent asIngressEvent(Watch.Response<V1Namespace> item, Set<V1beta1IngressClass> currentIngressSpecification) {
        String comment = "Triggered by Kubernetes Ingress Event: " + item.object.getMetadata().getName() + " [" + item.type + "]";
        Set<Specification> specs = currentIngressSpecification.stream().map(i -> asSpecification(i)).collect(toSet());
        return ActivationEvent.builder().comment(comment).specifications(specs).build();
    }

    private Specification asSpecification(V1beta1IngressClass i) {
        List<Rule> rules = i.getSpec().getRules().stream().map(r -> asRule(r)).collect(toList());
        Annotation annotation = asAnnotation(i.getMetadata().getAnnotations());
        return Specification.builder()
                .rules(rules)
                .annotation(annotation)
                .build();
    }

    private Rule asRule(ExtensionsV1beta1IngressRule r) {
        List<Path> paths = r.getHttp().getPaths().stream().map(p -> asPath(p)).collect(toList());
        return Rule.builder()
                .host(r.getHost())
                .paths(paths)
                .build();
    }

    private Path asPath(ExtensionsV1beta1HTTPIngressPath p) {
        return Path.builder()
                .path(p.getPath())
                .backendServiceName(p.getBackend().getServiceName())
                .build();
    }

    private Annotation asAnnotation(Map<String, String> annotations) {

        return Annotation.builder().metaData(annotations).build();
    }
}*/
