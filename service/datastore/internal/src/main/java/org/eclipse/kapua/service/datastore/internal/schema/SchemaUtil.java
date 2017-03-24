package org.eclipse.kapua.service.datastore.internal.schema;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.kapua.commons.util.KapuaDateUtils;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.client.DatamodelMappingException;
import org.eclipse.kapua.service.datastore.internal.mediator.DatastoreUtils;
import org.eclipse.kapua.service.datastore.model.StorableId;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class SchemaUtil {

    final static JsonNodeFactory factory = JsonNodeFactory.instance;

    public static final String DATA_STORE_DATE_PATTERN = "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'"; // example 24/01/2017T11:22Z
    private static final String UNSUPPORTED_OBJECT_TYPE_ERROR_MSG = "The conversion of object [%s] is not supported!";
    private static final String NOT_VALID_OBJECT_TYPE_ERROR_MSG = "Cannot convert date [%s]";

    /**
     * Return a map of map. The contained map has, as entries, the couples subKeys-values.<br>
     * <b>NOTE! No arrays subKeys-values coherence will be done (length or null check)!</>
     * 
     * @param key
     * @param subKeys
     * @param values
     * @return
     */
    public static Map<String, Object> getMapOfMap(String key, String[] subKeys, String[] values) {
        Map<String, String> mapChildren = new HashMap<>();
        for (int i=0; i<subKeys.length; i++) {
            mapChildren.put(subKeys[i], values[i]);
        }
        Map<String, Object> map = new HashMap<>();
        map.put(key, mapChildren);
        return map;
    }

    /**
     * Get the Elasticsearch data index name
     * 
     * @param scopeId
     * @return
     */
    public static String getDataIndexName(KapuaId scopeId) {
        return DatastoreUtils.getDataIndexName(scopeId);
    }

    /**
     * Get the Kapua data index name
     * 
     * @param scopeId
     * @return
     */
    public static String getKapuaIndexName(KapuaId scopeId) {
        return DatastoreUtils.getRegistryIndexName(scopeId);
    }

    public static ObjectNode getField(String[] name, Object[] value) throws DatamodelMappingException {
        ObjectNode rootNode = factory.objectNode();
        for (int i = 0; i < name.length; i++) {
            appendField(rootNode, name[i], value[i]);
        }
        return rootNode;
    }

    public static ObjectNode getField(String name, Object value) throws DatamodelMappingException {
        ObjectNode rootNode = factory.objectNode();
        appendField(rootNode, name, value);
        return rootNode;
    }

    public static void appendField(ObjectNode node, String name, Object value) throws DatamodelMappingException {
        if (value instanceof String) {
            node.set(name, factory.textNode((String) value));
        } else if (value instanceof Boolean) {
            node.set(name, factory.booleanNode((Boolean) value));
        } else if (value instanceof Integer) {
            node.set(name, factory.numberNode((Integer) value));
        } else if (value instanceof Long) {
            node.set(name, factory.numberNode((Long) value));
        } else if (value instanceof Double) {
            node.set(name, factory.numberNode((Double) value));
        } else if (value instanceof Float) {
            node.set(name, factory.numberNode((Float) value));
        } else if (value instanceof byte[]) {
            node.set(name, factory.binaryNode((byte[]) value));
        } else if (value instanceof byte[]) {
            node.set(name, factory.binaryNode((byte[]) value));
        } else if (value instanceof Date) {
            try {
                node.set(name, factory.textNode(KapuaDateUtils.formatDate(DATA_STORE_DATE_PATTERN, (Date) value)));
            } catch (ParseException e) {
                throw new DatamodelMappingException(String.format(NOT_VALID_OBJECT_TYPE_ERROR_MSG, value), e);
            }
        } else if (value instanceof StorableId) {
            node.set(name, factory.textNode(((StorableId) value).toString()));
        } else {
            throw new DatamodelMappingException(String.format(UNSUPPORTED_OBJECT_TYPE_ERROR_MSG, value.getClass()));
        }
    }

    public static ObjectNode getObjectNode() {
        return factory.objectNode();
    }

    public static NumericNode getNumericNode(long number) {
        return factory.numberNode(number);
    }

    public static ArrayNode getArrayNode() {
        return factory.arrayNode();
    }

    public static TextNode getTextNode(String value) {
        return factory.textNode(value);
    }

    public static ArrayNode getAsArrayNode(String[] fields) {
        ArrayNode rootNode = factory.arrayNode(fields.length);
        for (String str : fields) {
            rootNode.add(str);
        }
        return rootNode;
    }

}
