/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.datastore.internal.model.query;

import org.eclipse.kapua.service.datastore.client.DatamodelMappingException;
import org.eclipse.kapua.service.datastore.internal.schema.SchemaUtil;
import org.eclipse.kapua.service.datastore.model.query.ChannelMatchPredicate;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.eclipse.kapua.service.datastore.internal.model.query.PredicateConstants.PREFIX_KEY;

/**
 * Implementation of query predicate for matching the channel value
 * 
 * @since 1.0
 *
 */
public class ChannelMatchPredicateImpl implements ChannelMatchPredicate {

    private String field;
    private String expression;

    /**
     * Construct a channel match predicate for the given expression
     * 
     * @param field
     *            the field name
     * @param expression
     *            the channel expression (may use wildcard)
     */
    public ChannelMatchPredicateImpl(String field, String expression) {
        this.field = field;
        this.expression = expression;
    }

    @Override
    public String getExpression() {
        return this.expression;
    }

    @Override
    /**
     * <pre>
     * {
     *  "query": {
     *      "bool" : {
     *        "must" : {
     *          "term" : { "user" : "kimchy" }
     *        },
     *        "filter": {
     *          "term" : { "tag" : "tech" }
     *        },
     *        "must_not" : {
     *          "range" : {
     *            "age" : { "from" : 10, "to" : 20 }
     *          }
     *        },
     *        "should" : [
     *          { "term" : { "tag" : "wow" } },
     *          { "term" : { "tag" : "elasticsearch" } }
     *        ],
     *        "minimum_should_match" : 1,
     *        "boost" : 1.0
     *      }
     *  }
     *}
     * </pre>
     */
    public ObjectNode toSerializedMap() throws DatamodelMappingException {
        ObjectNode rootNode = SchemaUtil.getObjectNode();
        ObjectNode expressionNode = SchemaUtil.getField(new String[] { field.toString() }, new String[] { (String) expression });
        rootNode.set(PREFIX_KEY, expressionNode);
        return rootNode;
    }

}
