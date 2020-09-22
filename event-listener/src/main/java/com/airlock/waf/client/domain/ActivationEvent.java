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

    private final String comment;

    private final Set<Specification> specifications;

    @Getter
    @Builder
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Specification {

        private final List<Rule> rules;

        private final Annotation annotation;

    }

    @Builder
    @EqualsAndHashCode
    public static class Annotation {

        private final Map<String, String> metaData;

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

        private final String host;

        private final List<Path> paths;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Path {

        private final String path;

        private final String backendServiceName;
    }
}
