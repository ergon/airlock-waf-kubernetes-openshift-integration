/*
 * $Id$
 *
 * Copyright (c) Ergon Informatik AG
 * All Rights Reserved.
 */
package com.airlock.waf.client.config.rs.transfer;

import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@EqualsAndHashCode
public class CollectionRelationshipDocument {

	private List<RelationshipResourceObject> data;

	@AllArgsConstructor
	@Getter
	@EqualsAndHashCode
	public static class RelationshipResourceObject {

		@NotNull
		private final String type;

		@NotNull
		private final String id;
	}

	public static CollectionRelationshipDocumentBuilder collectionRelationshipDocumentBuilder () {

		return new CollectionRelationshipDocumentBuilder();
	}

	@NoArgsConstructor(access = PRIVATE)
	public static class CollectionRelationshipDocumentBuilder {

		private final List<RelationshipResourceObject> data = new ArrayList<>();

		public CollectionRelationshipDocumentBuilder relationship (String type, Long reference) {

			data.add(new RelationshipResourceObject(type, reference.toString()));
			return this;
		}
		
		public CollectionRelationshipDocumentBuilder relationship (String type, String reference) {

			data.add(new RelationshipResourceObject(type, reference));
			return this;
		}

		public CollectionRelationshipDocument build () {

			return new CollectionRelationshipDocument(data);
		}
	}
}
