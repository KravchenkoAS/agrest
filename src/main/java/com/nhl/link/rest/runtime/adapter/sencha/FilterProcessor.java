package com.nhl.link.rest.runtime.adapter.sencha;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.map.ObjEntity;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.filter.FilterUtil;

class FilterProcessor {

	private static final int MAX_VALUE_LENGTH = 1024;
	private static final String EXACT_MATCH = "exactMatch";
	private static final String PROPERTY = "property";
	private static final String VALUE = "value";

	private IJacksonService jsonParser;
	private IPathCache pathCache;

	FilterProcessor(IJacksonService jsonParser, IPathCache pathCache) {
		this.jsonParser = jsonParser;
		this.pathCache = pathCache;
	}

	void process(Entity<?> clientEntity, String filtersJson) {
		if (filtersJson == null || filtersJson.length() == 0) {
			return;
		}

		JsonNode rootNode = jsonParser.parseJson(filtersJson);
		if (rootNode == null) {
			return;
		}

		for (JsonNode filterNode : rootNode) {
			JsonNode propertyNode = filterNode.get(PROPERTY);
			if (propertyNode == null) {
				throw new LinkRestException(Status.BAD_REQUEST, "filter 'property' is missing" + filterNode.asText());
			}

			JsonNode valueNode = filterNode.get(VALUE);
			if (valueNode == null) {
				throw new LinkRestException(Status.BAD_REQUEST, "filter 'value' is missing" + filterNode.asText());
			}

			String property = propertyNode.asText();

			Object valueUnescaped = extractValue(valueNode);

			boolean exactMatch = false;
			JsonNode exactMatchNode = filterNode.get(EXACT_MATCH);
			if (exactMatchNode != null) {
				exactMatch = exactMatchNode.asBoolean();
			}

			Expression qualifier;
			if (valueUnescaped == null) {
				qualifier = ExpressionFactory.matchExp(property, null);
			} else if (valueUnescaped instanceof Boolean) {
				qualifier = ExpressionFactory.matchExp(property, valueUnescaped);
			} else if (exactMatch) {
				qualifier = ExpressionFactory.matchExp(property, valueUnescaped);
			} else {
				checkValueLength((String) valueUnescaped);
				String value = FilterUtil.escapeValueForLike((String) valueUnescaped) + "%";
				qualifier = ExpressionFactory.likeIgnoreCaseExp(property, value);
			}

			// validate property path
			ObjEntity rootEntity = clientEntity.getCayenneEntity();
			pathCache.getPathDescriptor(rootEntity, (ASTObjPath) qualifier.getOperand(0));

			clientEntity.andQualifier(qualifier);
		}
	}

	private static Object extractValue(JsonNode valueNode) {
		JsonToken type = valueNode.asToken();

		// ExtJS converts everything to String except for NULL and booleans. So
		// follow the
		// same logic here...
		// (http://docs.sencha.com/extjs/4.1.2/source/Filter.html#Ext-util-Filter)
		switch (type) {
		case VALUE_NULL:
			return null;
		case VALUE_FALSE:
			return false;
		case VALUE_TRUE:
			return true;
		case VALUE_NUMBER_INT:
			return valueNode.asInt();
		case VALUE_NUMBER_FLOAT:
			return valueNode.asDouble();
		default:
			return valueNode.asText();
		}
	}

	private void checkValueLength(String value) {
		if (value.length() > MAX_VALUE_LENGTH) {
			throw new LinkRestException(Status.BAD_REQUEST, "filter 'value' is to long: " + value);
		}
	}
}