package org.eclipse.kapua.service.datastore.internal.schema;

import org.eclipse.kapua.service.datastore.client.DatamodelMappingException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_ALL;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_FORMAT;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_INDEX;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_SOURCE;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_TYPE;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_ENABLED;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.TYPE_DATE;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.TYPE_STRING;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.FIELD_INDEXING_NOT_ANALYZED;

public class ChannelInfoSchema {

    /**
     * Channel information schema name
     */
    public final static String CHANNEL_TYPE_NAME = "channel";
    /**
     * Channel information - channel
     */
    public final static String CHANNEL_NAME = "channel";
    /**
     * Channel information - client identifier
     */
    public final static String CHANNEL_CLIENT_ID = "client_id";
    /**
     * Channel information - scope id
     */
    public static final String CHANNEL_SCOPE_ID = "scope_id";
    /**
     * Channel information - message timestamp (of the first message published in this channel)
     */
    public final static String CHANNEL_TIMESTAMP = "timestamp";
    /**
     * Channel information - message identifier (of the first message published in this channel)
     */
    public final static String CHANNEL_MESSAGE_ID = "message_id";

    public static ObjectNode getChannelTypeBuilder(boolean allEnable, boolean sourceEnable) throws DatamodelMappingException {
        ObjectNode rootNode = SchemaUtil.getObjectNode();

        ObjectNode channelNode = SchemaUtil.getObjectNode();
        ObjectNode sourceChannel = SchemaUtil.getField(new String[] { KEY_ENABLED }, new Object[] { sourceEnable });
        channelNode.set(KEY_SOURCE, sourceChannel);

        ObjectNode allChannel = SchemaUtil.getField(new String[] { KEY_ENABLED }, new Object[] { allEnable });
        channelNode.set(KEY_ALL, allChannel);

        ObjectNode propertiesNode = SchemaUtil.getObjectNode();
        ObjectNode channelScopeId = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        propertiesNode.set(CHANNEL_SCOPE_ID, channelScopeId);
        ObjectNode channelClientId = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        propertiesNode.set(CHANNEL_CLIENT_ID, channelClientId);
        ObjectNode channelName = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        propertiesNode.set(CHANNEL_NAME, channelName);
        ObjectNode channelTimestamp = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_FORMAT }, new String[] { TYPE_DATE, SchemaUtil.DATA_STORE_DATE_PATTERN });
        propertiesNode.set(CHANNEL_TIMESTAMP, channelTimestamp);
        ObjectNode channelMessageId = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        propertiesNode.set(CHANNEL_MESSAGE_ID, channelMessageId);
        channelNode.set("properties", propertiesNode);
        rootNode.set(CHANNEL_TYPE_NAME, channelNode);
        return rootNode;
    }

}
