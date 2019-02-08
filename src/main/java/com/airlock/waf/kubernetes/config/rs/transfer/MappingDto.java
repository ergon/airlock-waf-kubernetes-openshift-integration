package com.airlock.waf.kubernetes.config.rs.transfer;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName(MappingDto.MAPPING_TYPE)
public class MappingDto {

    public static final String MAPPING_TYPE = "mapping";

    private String name;

    private MappingEntryPathDto entryPath;

    private String backendPath;

    private String operationalMode;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = PRIVATE)
    public static class MappingEntryPathDto {

        private String value;

        private Boolean regexFormatEnforced;

        private Boolean ignoreCase;

        private Integer priority;
    }
}
