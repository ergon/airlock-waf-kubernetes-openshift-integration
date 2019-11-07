/*
 * $Id$
 *
 * Copyright (c) Ergon Informatik AG
 * All Rights Reserved.
 */
package com.airlock.waf.client.config.rs.transfer;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName(ConfigFileDto.CONFIG_FILE_TYPE)
public class ConfigFileDto {

	public static final String CONFIG_FILE_TYPE = "configuration";

	private String createdBy;

	private Date createdAt;

	private String comment;

	private String configType;
}
