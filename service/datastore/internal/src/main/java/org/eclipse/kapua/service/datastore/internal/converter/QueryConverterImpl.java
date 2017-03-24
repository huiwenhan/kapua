package org.eclipse.kapua.service.datastore.internal.converter;

import java.util.List;

import org.eclipse.kapua.service.datastore.client.DatamodelMappingException;
import org.eclipse.kapua.service.datastore.client.QueryConverter;
import org.eclipse.kapua.service.datastore.client.QueryMappingException;
import org.eclipse.kapua.service.datastore.internal.AbstractStorableQuery;
import org.eclipse.kapua.service.datastore.internal.schema.SchemaUtil;
import org.eclipse.kapua.service.datastore.model.query.SortField;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class QueryConverterImpl implements QueryConverter {

    @Override
    /**
     * <pre>
     * "_source": {
     *      "include": [ "obj1.*", "obj2.*" ],
     *      "exclude": [ "*.description" ]
     *  },
     *  "sort" : [
     *      { "post_date" : {"order" : "asc"}},
     *      "user",
     *      { "name" : "desc" },
     *      { "age" : "desc" },
     *      "_score"
     *  ],
     *  "query" : {
     *      "term" : { "user" : "kimchy" }
     *  }
     * </pre>
     */
    public ObjectNode convertQuery(Object query) throws QueryMappingException, DatamodelMappingException {
        if (query instanceof AbstractStorableQuery<?>) {
            ObjectNode rootNode = SchemaUtil.getObjectNode();
            AbstractStorableQuery<?> storableQuery = (AbstractStorableQuery<?>) query;
            // includes/excludes
            ObjectNode includesFields = SchemaUtil.getObjectNode();
            includesFields.set(INCLUDES_KEY, SchemaUtil.getAsArrayNode(storableQuery.getIncludes(storableQuery.getFetchStyle())));
            includesFields.set(EXCLUDES_KEY, SchemaUtil.getAsArrayNode(storableQuery.getExcludes(storableQuery.getFetchStyle())));
            rootNode.set(SOURCE_KEY, includesFields);
            // query
            if (storableQuery.getPredicate() != null) {
                rootNode.set(QUERY_KEY, storableQuery.getPredicate().toSerializedMap());
            }
            // sort
            ArrayNode sortNode = SchemaUtil.getArrayNode();
            List<SortField> sortFields = storableQuery.getSortFields();
            if (sortFields != null) {
                for (SortField field : sortFields) {
                    sortNode.add(SchemaUtil.getField(field.getField(), field.getSortDirection().name()));
                }
            }
            // limit/from
            rootNode.set(FROM_KEY, SchemaUtil.getNumericNode(storableQuery.getOffset()));
            rootNode.set(SIZE_KEY, SchemaUtil.getNumericNode(storableQuery.getLimit()));
            rootNode.set(SORT_KEY, sortNode);
            // TODO handle offset and limit
            return rootNode;
        } else {
            throw new QueryMappingException("Wrong query type! Only AbstractStorableQuery can be converted!");
        }
    }

}
