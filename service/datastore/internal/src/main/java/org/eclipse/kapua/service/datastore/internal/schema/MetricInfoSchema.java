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
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_INCLUDE_IN_ALL;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.KEY_DYNAMIC;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.TYPE_DATE;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.TYPE_STRING;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.TYPE_OBJECT;
import static org.eclipse.kapua.service.datastore.internal.schema.Schema.FIELD_INDEXING_NOT_ANALYZED;

public class MetricInfoSchema {

    /**
     * Metric information schema name
     */
    public final static String METRIC_TYPE_NAME = "metric";
    /**
     * Metric information - channel
     */
    public final static String METRIC_CHANNEL = "channel";
    /**
     * Metric information - client identifier
     */
    public final static String METRIC_CLIENT_ID = "client_id";
    /**
     * Metric information - scope id
     */
    public static final String METRIC_SCOPE_ID = "scope_id";
    /**
     * Metric information - metric map prefix
     */
    public final static String METRIC_MTR = "metric";
    /**
     * Metric information - name
     */
    public final static String METRIC_MTR_NAME = "name";
    /**
     * Metric information - full name (so with the metric type suffix)
     */
    public final static String METRIC_MTR_NAME_FULL = "metric.name";
    /**
     * Metric information - type
     */
    public final static String METRIC_MTR_TYPE = "type";
    /**
     * Metric information - full type (so with the metric type suffix)
     */
    public final static String METRIC_MTR_TYPE_FULL = "metric.type";
    /**
     * Metric information - value
     */
    public final static String METRIC_MTR_VALUE = "value";
    /**
     * Metric information - full value (so with the metric type suffix)
     */
    public final static String METRIC_MTR_VALUE_FULL = "metric.value";
    /**
     * Metric information - message timestamp (of the first message published in this channel)
     */
    public final static String METRIC_MTR_TIMESTAMP = "timestamp";
    /**
     * Metric information - message timestamp (of the first message published in this channel, with the metric type suffix)
     */
    public final static String METRIC_MTR_TIMESTAMP_FULL = "metric.timestamp";
    /**
     * Metric information - message identifier (of the first message published in this channel)
     */
    public final static String METRIC_MTR_MSG_ID = "message_id";
    /**
     * Metric information - full message identifier (of the first message published in this channel, with the metric type suffix)
     */
    public final static String METRIC_MTR_MSG_ID_FULL = "metric.message_id";

    public static ObjectNode getMetricTypeBuilder(boolean allEnable, boolean sourceEnable) throws DatamodelMappingException {
        ObjectNode rootNode = SchemaUtil.getObjectNode();

        ObjectNode metricName = SchemaUtil.getObjectNode();
        ObjectNode sourceMetric = SchemaUtil.getField(new String[] { KEY_ENABLED }, new Object[] { sourceEnable });
        metricName.set(KEY_SOURCE, sourceMetric);

        ObjectNode allMetric = SchemaUtil.getField(new String[] { KEY_ENABLED }, new Object[] { allEnable });
        metricName.set(KEY_ALL, allMetric);

        ObjectNode propertiesNode = SchemaUtil.getObjectNode();
        ObjectNode metricAccount = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        propertiesNode.set(METRIC_SCOPE_ID, metricAccount);
        ObjectNode metricClientId = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        propertiesNode.set(METRIC_CLIENT_ID, metricClientId);
        ObjectNode metricChannel = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        propertiesNode.set(METRIC_CHANNEL, metricChannel);

        ObjectNode metricMtrNode = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_ENABLED, KEY_DYNAMIC, KEY_INCLUDE_IN_ALL },
                new Object[] { TYPE_OBJECT, true, false, false });
        ObjectNode metricMtrPropertiesNode = SchemaUtil.getObjectNode();
        ObjectNode metricMtrNameNode = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        metricMtrPropertiesNode.set(METRIC_MTR_NAME, metricMtrNameNode);
        ObjectNode metricMtrTypeNode = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        metricMtrPropertiesNode.set(METRIC_MTR_TYPE, metricMtrTypeNode);
        ObjectNode metricMtrValueNode = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        metricMtrPropertiesNode.set(METRIC_MTR_VALUE, metricMtrValueNode);
        ObjectNode metricMtrTimestampNode = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_FORMAT }, new String[] { TYPE_DATE, SchemaUtil.DATA_STORE_DATE_PATTERN });
        metricMtrPropertiesNode.set(METRIC_MTR_TIMESTAMP, metricMtrTimestampNode);
        ObjectNode metricMtrMsgIdNode = SchemaUtil.getField(new String[] { KEY_TYPE, KEY_INDEX }, new String[] { TYPE_STRING, FIELD_INDEXING_NOT_ANALYZED });
        metricMtrPropertiesNode.set(METRIC_MTR_MSG_ID, metricMtrMsgIdNode);
        metricMtrNode.set(FIELD_NAME_PROPERTIES, metricMtrPropertiesNode);
        propertiesNode.set(METRIC_MTR, metricMtrNode);

        metricName.set(FIELD_NAME_PROPERTIES, propertiesNode);

        rootNode.set(METRIC_TYPE_NAME, metricName);
        return rootNode;
    }

}
