package org.eclipse.kapua.service.datastore.internal.schema;

import org.eclipse.kapua.service.datastore.client.DatamodelMappingException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.eclipse.kapua.service.datastore.internal.schema.Schema.FIELD_NAME_PROPERTIES;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_ALL;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_FORMAT;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_INDEX;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_SOURCE;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_TYPE;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_ENABLED;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.TYPE_DATE;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.TYPE_STRING;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.FIELD_INDEXING_NOT_ANALYZED;

public class ClientInfoSchema {

    /**
     * Client information schema name
     */
    public final static String CLIENT_TYPE_NAME = "client";
    /**
     * Client information - client identifier
     */
    public final static String CLIENT_ID = "client_id";
    /**
     * Client information - scope id
     */
    public static final String CLIENT_SCOPE_ID = "scope_id";
    /**
     * Client information - message timestamp (of the first message published in this channel)
     */
    public final static String CLIENT_TIMESTAMP = "timestamp";
    /**
     * Client information - message identifier (of the first message published in this channel)
     */
    public final static String CLIENT_MESSAGE_ID = "message_id";

    public static ObjectNode getClientTypeBuilder(boolean allEnable, boolean sourceEnable) throws DatamodelMappingException {
        ObjectNode rootNode = SchemaUtil.getObjectNode();

        ObjectNode clientNodeName = SchemaUtil.getObjectNode();
        ObjectNode sourceClient = SchemaUtil.getField(new String[] { KEY_ENABLED }, new Object[] { sourceEnable });
        clientNodeName.set(KEY_SOURCE, sourceClient);

        ObjectNode allClient = SchemaUtil.getField(new String[] { KEY_ENABLED }, new Object[] { allEnable });
        clientNodeName.set(KEY_ALL, allClient);

        ObjectNode propertiesNode = SchemaUtil.getObjectNode();
        ObjectNode clientId = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        propertiesNode.set(CLIENT_ID, clientId);
        ObjectNode clientTimestamp = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_FORMAT }, new String[] { TYPE_DATE, SchemaUtil.DATA_STORE_DATE_PATTERN });
        propertiesNode.set(CLIENT_TIMESTAMP, clientTimestamp);
        ObjectNode clientScopeId = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        propertiesNode.set(CLIENT_SCOPE_ID, clientScopeId);
        ObjectNode clientMessageId = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        propertiesNode.set(CLIENT_MESSAGE_ID, clientMessageId);
        clientNodeName.set(FIELD_NAME_PROPERTIES, propertiesNode);
        rootNode.set(CLIENT_TYPE_NAME, clientNodeName);
        return rootNode;
    }

}
