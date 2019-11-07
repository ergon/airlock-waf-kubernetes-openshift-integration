package com.airlock.waf.client.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class ActivationEvent {

    private String comment;

    private Set<Specification> specifications;

    @Getter
    @Builder
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Specification {

        private List<Rule> rules;

        private Annotation annotation;

    }

    @Builder
    @EqualsAndHashCode
    public static class Annotation {

        private Map<String, String> metaData;

        private Annotation(Map<String, String> metaData) {

            Set<String> keys = metaData.keySet().stream().filter(k -> k.startsWith("waf.airlock.com")).collect(Collectors.toSet());
            keys.forEach(k -> metaData.remove(k));
            this.metaData = metaData;
        }

        public Optional<String> mappingTemplateId() {

            return Optional.ofNullable(metaData.get("waf.airlock.com/mapping.template.id"));
        }

        public Optional<String> mappingName() {

            return Optional.ofNullable(metaData.get("waf.airlock.com/mapping.name"));
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Rule {

        private String host;

        private List<Path> paths;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Path {

        private String path;

        private String backendServiceName;
    }
}
