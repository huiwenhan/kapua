package org.eclipse.kapua.service.datastore.internal.converter;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.commons.util.KapuaDateUtils;
import org.eclipse.kapua.message.KapuaPayload;
import org.eclipse.kapua.message.KapuaPosition;
import org.eclipse.kapua.message.internal.KapuaPositionImpl;
import org.eclipse.kapua.message.internal.device.data.KapuaDataChannelImpl;
import org.eclipse.kapua.message.internal.device.data.KapuaDataPayloadImpl;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.client.DatamodelMappingException;
import org.eclipse.kapua.service.datastore.client.ModelContext;
import org.eclipse.kapua.service.datastore.internal.mediator.DatastoreUtils;
import org.eclipse.kapua.service.datastore.internal.model.ChannelInfoImpl;
import org.eclipse.kapua.service.datastore.internal.model.ClientInfoImpl;
import org.eclipse.kapua.service.datastore.internal.model.DatastoreMessageImpl;
import org.eclipse.kapua.service.datastore.internal.model.MetricInfoImpl;
import org.eclipse.kapua.service.datastore.internal.model.StorableIdImpl;
import org.eclipse.kapua.service.datastore.internal.schema.ChannelInfoSchema;
import org.eclipse.kapua.service.datastore.internal.schema.ClientInfoSchema;
import org.eclipse.kapua.service.datastore.internal.schema.MessageSchema;
import org.eclipse.kapua.service.datastore.internal.schema.MetricInfoSchema;
import org.eclipse.kapua.service.datastore.internal.schema.SchemaUtil;
import org.eclipse.kapua.service.datastore.model.ChannelInfo;
import org.eclipse.kapua.service.datastore.model.ClientInfo;
import org.eclipse.kapua.service.datastore.model.DatastoreMessage;
import org.eclipse.kapua.service.datastore.model.MetricInfo;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ModelContextImpl implements ModelContext {

    private static final String CONVERSION_ERROR_MSG = "Data conversion error";
    private static final String DATE_CONVERSION_ERROR_MSG = "Date conversion error";
    private static final String UNSUPPORTED_OBJECT_TYPE_ERROR_MSG = "The conversion of object [%s] is not supported!";
    private static final String UNMARSHAL_INVALID_PARAMETERS_ERROR_MSG = "Object and/or object type cannot be null!";
    private static final String MARSHAL_INVALID_PARAMETERS_ERROR_MSG = "Object and/or object type cannot be null!";

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unmarshal(Class<T> clazz, Map<String, Object> serializedObject) throws DatamodelMappingException {
        if (clazz == null || serializedObject == null) {
            throw new DatamodelMappingException(UNMARSHAL_INVALID_PARAMETERS_ERROR_MSG);
        }
        try {
            if (DatastoreMessage.class.isAssignableFrom(clazz)) {
                return (T) unmarshalDatastoreMessage(serializedObject);
            } else if (ClientInfo.class.isAssignableFrom(clazz)) {
                return (T) unmarshalClientInfo(serializedObject);
            } else if (MetricInfo.class.isAssignableFrom(clazz)) {
                return (T) unmarshalMetricInfo(serializedObject);
            } else if (ChannelInfo.class.isAssignableFrom(clazz)) {
                return (T) unmarshalChannelInfo(serializedObject);
            }
        } catch (IOException | ParseException e) {
            throw new DatamodelMappingException(CONVERSION_ERROR_MSG, e);
        }
        throw new DatamodelMappingException(String.format(UNSUPPORTED_OBJECT_TYPE_ERROR_MSG, clazz.getName()));
    }

    @Override
    public Map<String, Object> marshal(Object object) throws DatamodelMappingException {
        if (object==null) {
            throw new DatamodelMappingException(MARSHAL_INVALID_PARAMETERS_ERROR_MSG);
        }
        try {
            if (object instanceof DatastoreMessage) {
                return marshalDatastoreMessage((DatastoreMessage) object);
            }
            if (object instanceof ClientInfo) {
                return marshalClientInfo((ClientInfo) object);
            }
            if (object instanceof ChannelInfo) {
                return marshalChannelInfo((ChannelInfo) object);
            }
            if (object instanceof MetricInfo) {
                return marshalMetricInfo((MetricInfo) object);
            }
        } catch (ParseException e) {
            throw new DatamodelMappingException(DATE_CONVERSION_ERROR_MSG, e);
        }
        throw new DatamodelMappingException(String.format(UNSUPPORTED_OBJECT_TYPE_ERROR_MSG, object.getClass().getName()));
    }
    
    private DatastoreMessage unmarshalDatastoreMessage(Map<String, Object> messageMap)
            throws DatamodelMappingException, JsonParseException, JsonMappingException, IOException, ParseException {
        DatastoreMessageImpl message = new DatastoreMessageImpl();
        String id = (String) messageMap.get(ModelContext.DATASTORE_ID_KEY);
        message.setDatastoreId(new StorableIdImpl(id));
        
        KapuaId scopeId = new KapuaEid(new BigInteger((String) messageMap.get(MessageSchema.MESSAGE_SCOPE_ID)));
        message.setScopeId(scopeId);
        KapuaId deviceId = new KapuaEid(new BigInteger((String) messageMap.get(MessageSchema.MESSAGE_DEVICE_ID)));
        message.setDeviceId(deviceId);
        String clientId = (String) messageMap.get(MessageSchema.MESSAGE_CLIENT_ID);
        message.setClientId(clientId);
        message.setDatastoreId(new StorableIdImpl(id));

        KapuaDataChannelImpl dataChannel = new KapuaDataChannelImpl();
        message.setChannel(dataChannel);

        String timestamp = (String) messageMap.get(MessageSchema.MESSAGE_TIMESTAMP);
        message.setTimestamp((Date) (timestamp == null ? null : parseDate((String) timestamp)));


        // if (fetchStyle.equals(StorableFetchStyle.FIELDS)) {
        // return message;
        // }

        @SuppressWarnings("unchecked")
        List<String> channelParts = (List<String>) messageMap.get(MessageSchema.MESSAGE_CHANNEL_PARTS);
        dataChannel.setSemanticParts(channelParts);

        KapuaDataPayloadImpl payload = new KapuaDataPayloadImpl();
        @SuppressWarnings("unchecked")
        Map<String, Object> positionMap = (Map<String, Object>) messageMap.get(MessageSchema.MESSAGE_POSITION);
        KapuaPositionImpl position = null;
        if (positionMap != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> locationMap = (Map<String, Object>) positionMap.get(MessageSchema.MESSAGE_POS_LOCATION);

            position = new KapuaPositionImpl();
            if (locationMap != null && locationMap.get(MessageSchema.MESSAGE_POSITION_LATITUDE) != null) {
                position.setLatitude((double) locationMap.get(MessageSchema.MESSAGE_POSITION_LATITUDE));
            }
            if (locationMap != null && locationMap.get(MessageSchema.MESSAGE_POSITION_LONGITUDE) != null) {
                position.setLongitude((double) locationMap.get(MessageSchema.MESSAGE_POSITION_LONGITUDE));
            }
            Object obj = positionMap.get(MessageSchema.MESSAGE_POS_ALT);
            if (obj != null) {
                position.setAltitude((double) obj);
            }
            obj = positionMap.get(MessageSchema.MESSAGE_POS_HEADING);
            if (obj != null) {
                position.setHeading((double) obj);
            }
            obj = positionMap.get(MessageSchema.MESSAGE_POS_PRECISION);
            if (obj != null) {
                position.setPrecision((double) obj);
            }
            obj = positionMap.get(MessageSchema.MESSAGE_POS_SATELLITES);
            if (obj != null) {
                position.setSatellites((int) obj);
            }
            obj = positionMap.get(MessageSchema.MESSAGE_POS_SPEED);
            if (obj != null) {
                position.setSpeed((double) obj);
            }
            obj = positionMap.get(MessageSchema.MESSAGE_POS_STATUS);
            if (obj != null) {
                position.setStatus((int) obj);
            }
            obj = positionMap.get(MessageSchema.MESSAGE_POS_TIMESTAMP);
            if (obj != null) {
                position.setTimestamp((Date) parseDate((String) obj));
            }
            message.setPosition(position);
        }
        Object capturedOnFld = messageMap.get(MessageSchema.MESSAGE_CAPTURED_ON);
        if (capturedOnFld != null) {
            message.setCapturedOn((Date) (capturedOnFld == null ? null : parseDate((String) capturedOnFld)));
        }
        Object sentOnFld = messageMap.get(MessageSchema.MESSAGE_SENT_ON);
        if (sentOnFld != null) {
            message.setSentOn((Date) (sentOnFld == null ? null : parseDate((String) sentOnFld)));
        }
        Object receivedOnFld = messageMap.get(MessageSchema.MESSAGE_RECEIVED_ON);
        if (receivedOnFld != null) {
            message.setReceivedOn((Date) (receivedOnFld == null ? null : parseDate((String) receivedOnFld)));
        }
        if (messageMap.get(MessageSchema.MESSAGE_METRICS) != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metrics = (Map<String, Object>) messageMap.get(MessageSchema.MESSAGE_METRICS);
            Map<String, Object> payloadMetrics = new HashMap<String, Object>();
            String[] metricNames = metrics.keySet().toArray(new String[] {});
            for (String metricsName : metricNames) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metricValue = (Map<String, Object>) metrics.get(metricsName);
                if (metricValue.size() > 0) {
                    String[] valueTypes = metricValue.keySet().toArray(new String[] {});
                    Object value = metricValue.get(valueTypes[0]);
                    // since elasticsearch doesn't return always the same type of the saved field
                    // (usually due to some promotion of the field type)
                    // we need to check the metric type returned by elasticsearch and, if needed, convert to the proper type
                    payloadMetrics.put(DatastoreUtils.restoreMetricName(metricsName), DatastoreUtils.convertToCorrectType(valueTypes[0], value));
                }
            }
            payload.setProperties(payloadMetrics);
        }
        // if (fetchStyle.equals(StorableFetchStyle.SOURCE_SELECT)) {
        // this.message = tmpMessage;
        // }
        if (messageMap.get(MessageSchema.MESSAGE_BODY) != null) {
            byte[] body = Base64Variants.getDefaultVariant().decode((String) messageMap.get(MessageSchema.MESSAGE_BODY));
            payload.setBody(body);
        }
        if (payload != null) {
            message.setPayload(payload);
        }
        return message;
    }

    private MetricInfo unmarshalMetricInfo(Map<String, Object> metricInfoMap)
            throws DatamodelMappingException, JsonParseException, JsonMappingException, IOException, ParseException {
        KapuaId scopeId = new KapuaEid(new BigInteger((String) metricInfoMap.get(MetricInfoSchema.METRIC_SCOPE_ID)));
        MetricInfo metricInfo = new MetricInfoImpl(scopeId);
        String id = (String) metricInfoMap.get(ModelContext.DATASTORE_ID_KEY);
        metricInfo.setId(new StorableIdImpl(id));
        Map<String, Object> metricMap = (Map<String, Object>) metricInfoMap.get(MetricInfoSchema.METRIC_MTR);
        String name = (String) metricMap.get(MetricInfoSchema.METRIC_MTR_NAME);
        String type = (String) metricMap.get(MetricInfoSchema.METRIC_MTR_TYPE);
        String lastMsgTimestamp = (String) metricMap.get(MetricInfoSchema.METRIC_MTR_TIMESTAMP);
        String lastMsgId = (String) metricMap.get(MetricInfoSchema.METRIC_MTR_MSG_ID);
        String clientId = (String) metricInfoMap.get(MetricInfoSchema.METRIC_CLIENT_ID);
        String channel = (String) metricInfoMap.get(MetricInfoSchema.METRIC_CHANNEL);
        metricInfo.setClientId(clientId);
        metricInfo.setChannel(channel);
        metricInfo.setFirstMessageId(new StorableIdImpl(lastMsgId));
        String metricName = DatastoreUtils.restoreMetricName(name);
        metricInfo.setName(metricName);
        Date timestamp = parseDate(lastMsgTimestamp);
        metricInfo.setFirstMessageOn(timestamp);
        metricInfo.setMetricType(DatastoreUtils.convertToKapuaType(type));
        return metricInfo;
    }

    private ChannelInfo unmarshalChannelInfo(Map<String, Object> channelInfoMap)
            throws DatamodelMappingException, JsonParseException, JsonMappingException, IOException, ParseException {
        KapuaId scopeId = new KapuaEid(new BigInteger((String) channelInfoMap.get(ChannelInfoSchema.CHANNEL_SCOPE_ID)));
        ChannelInfo channelInfo = new ChannelInfoImpl(scopeId);
        String id = (String) channelInfoMap.get(ModelContext.DATASTORE_ID_KEY);
        channelInfo.setId(new StorableIdImpl(id));
        channelInfo.setClientId((String) channelInfoMap.get(ChannelInfoSchema.CHANNEL_CLIENT_ID));
        channelInfo.setChannel((String) channelInfoMap.get(ChannelInfoSchema.CHANNEL_NAME));
        channelInfo.setFirstMessageId(new StorableIdImpl((String) channelInfoMap.get(ChannelInfoSchema.CHANNEL_MESSAGE_ID)));
        channelInfo.setFirstMessageOn(parseDate((String) channelInfoMap.get(ChannelInfoSchema.CHANNEL_TIMESTAMP)));
        return channelInfo;
    }

    private ClientInfo unmarshalClientInfo(Map<String, Object> clientInfoMap)
            throws DatamodelMappingException, JsonParseException, JsonMappingException, IOException, ParseException {
        KapuaId scopeId = new KapuaEid(new BigInteger((String) clientInfoMap.get(ClientInfoSchema.CLIENT_SCOPE_ID)));
        ClientInfo clientInfo = new ClientInfoImpl(scopeId);
        String id = (String) clientInfoMap.get(ModelContext.DATASTORE_ID_KEY);
        clientInfo.setId(new StorableIdImpl(id));
        clientInfo.setClientId((String) clientInfoMap.get(ClientInfoSchema.CLIENT_ID));
        clientInfo.setFirstMessageId(new StorableIdImpl((String) clientInfoMap.get(ClientInfoSchema.CLIENT_MESSAGE_ID)));
        clientInfo.setFirstMessageOn(parseDate((String) clientInfoMap.get(ClientInfoSchema.CLIENT_TIMESTAMP)));
        return clientInfo;
    }

    private Map<String, Object> marshalDatastoreMessage(DatastoreMessage message) throws ParseException {
        Map<String, Object> unmarshalledMessage = new HashMap<>();
        String scopeId = message.getScopeId().toStringId();
        String deviceIdStr = message.getDeviceId() == null ? null : message.getDeviceId().toStringId();
        unmarshalledMessage.put(MessageSchema.MESSAGE_TIMESTAMP, formatDate(message.getTimestamp()));
        unmarshalledMessage.put(MessageSchema.MESSAGE_RECEIVED_ON, formatDate(message.getReceivedOn()));
        unmarshalledMessage.put(MessageSchema.MESSAGE_IP_ADDRESS, "127.0.0.1");// TODO
        unmarshalledMessage.put(MessageSchema.MESSAGE_SCOPE_ID, scopeId);
        unmarshalledMessage.put(MessageSchema.MESSAGE_DEVICE_ID, deviceIdStr);
        unmarshalledMessage.put(MessageSchema.MESSAGE_CLIENT_ID, message.getClientId());
        unmarshalledMessage.put(MessageSchema.MESSAGE_CHANNEL, message.getChannel());
        unmarshalledMessage.put(MessageSchema.MESSAGE_CHANNEL_PARTS, message.getChannel().getSemanticParts());
        unmarshalledMessage.put(MessageSchema.MESSAGE_CAPTURED_ON, formatDate(message.getCapturedOn()));
        unmarshalledMessage.put(MessageSchema.MESSAGE_SENT_ON, formatDate(message.getSentOn()));

        KapuaPosition kapuaPosition = message.getPosition();
        if (kapuaPosition != null) {

            Map<String, Object> location = null;
            if (kapuaPosition.getLongitude() != null && kapuaPosition.getLatitude() != null) {
                location = new HashMap<String, Object>();
                location.put(MessageSchema.MESSAGE_POSITION_LONGITUDE, kapuaPosition.getLongitude());
                location.put(MessageSchema.MESSAGE_POSITION_LATITUDE, kapuaPosition.getLatitude());
            }

            Map<String, Object> position = new HashMap<String, Object>();
            position.put(MessageSchema.MESSAGE_POS_LOCATION, location);
            position.put(MessageSchema.MESSAGE_POS_ALT, kapuaPosition.getAltitude());
            position.put(MessageSchema.MESSAGE_POS_PRECISION, kapuaPosition.getPrecision());
            position.put(MessageSchema.MESSAGE_POS_HEADING, kapuaPosition.getHeading());
            position.put(MessageSchema.MESSAGE_POS_SPEED, kapuaPosition.getSpeed());
            position.put(MessageSchema.MESSAGE_POS_TIMESTAMP, formatDate(kapuaPosition.getTimestamp()));
            position.put(MessageSchema.MESSAGE_POS_SATELLITES, kapuaPosition.getSatellites());
            position.put(MessageSchema.MESSAGE_POS_STATUS, kapuaPosition.getStatus());
            unmarshalledMessage.put(MessageSchema.MESSAGE_POSITION, position);
        }

        KapuaPayload payload = message.getPayload();
        if (payload == null) {
            return unmarshalledMessage;
        }

        unmarshalledMessage.put(MessageSchema.MESSAGE_BODY, payload.getBody());

        Map<String, Object> kapuaMetrics = payload.getProperties();
        if (kapuaMetrics != null) {
            Map<String, Object> metrics = new HashMap<String, Object>();
            String[] metricNames = kapuaMetrics.keySet().toArray(new String[] {});
            for (String kapuaMetricName : metricNames) {
                Object metricValue = kapuaMetrics.get(kapuaMetricName);
                // Sanitize field names: '.' is not allowed
                String metricName = DatastoreUtils.normalizeMetricName(kapuaMetricName);
                String clientMetricType = DatastoreUtils.getClientMetricFromType(metricValue.getClass());
                String clientMetricTypeAcronim = DatastoreUtils.getClientMetricFromAcronym(clientMetricType);
                Map<String, Object> field = new HashMap<String, Object>();
                field.put(clientMetricTypeAcronim, metricValue);
                metrics.put(metricName, field);
            }
            unmarshalledMessage.put(MessageSchema.MESSAGE_METRICS, metrics);
        }
        return unmarshalledMessage;
    }

    private Map<String, Object> marshalClientInfo(ClientInfo clientInfo) throws ParseException {
        Map<String, Object> unmarshalledClientInfo = new HashMap<>();
        unmarshalledClientInfo.put(ClientInfoSchema.CLIENT_ID, clientInfo.getClientId());
        unmarshalledClientInfo.put(ClientInfoSchema.CLIENT_MESSAGE_ID, clientInfo.getFirstMessageId());
        unmarshalledClientInfo.put(ClientInfoSchema.CLIENT_TIMESTAMP, formatDate(clientInfo.getFirstMessageOn()));
        unmarshalledClientInfo.put(ClientInfoSchema.CLIENT_SCOPE_ID, clientInfo.getScopeId());
        return unmarshalledClientInfo;
    }

    private Map<String, Object> marshalChannelInfo(ChannelInfo channelInfo) throws ParseException {
        Map<String, Object> unmarshalledChannelInfo = new HashMap<>();
        unmarshalledChannelInfo.put(ChannelInfoSchema.CHANNEL_NAME, channelInfo.getChannel());
        unmarshalledChannelInfo.put(ChannelInfoSchema.CHANNEL_TIMESTAMP, formatDate(channelInfo.getFirstMessageOn()));
        unmarshalledChannelInfo.put(ChannelInfoSchema.CHANNEL_CLIENT_ID, channelInfo.getClientId());
        unmarshalledChannelInfo.put(ChannelInfoSchema.CHANNEL_SCOPE_ID, channelInfo.getScopeId());
        unmarshalledChannelInfo.put(ChannelInfoSchema.CHANNEL_MESSAGE_ID, channelInfo.getFirstMessageId());
        return unmarshalledChannelInfo;
    }

    private Map<String, Object> marshalMetricInfo(MetricInfo metricInfo) throws ParseException {
        Map<String, Object> unmarshalledMetricInfo = new HashMap<>();
        unmarshalledMetricInfo.put(MetricInfoSchema.METRIC_SCOPE_ID, metricInfo.getScopeId());
        unmarshalledMetricInfo.put(MetricInfoSchema.METRIC_CLIENT_ID, metricInfo.getClientId());
        unmarshalledMetricInfo.put(MetricInfoSchema.METRIC_CHANNEL, metricInfo.getChannel());
        Map<String, Object> unmarshalledMetricValue = new HashMap<>();
        unmarshalledMetricValue.put(MetricInfoSchema.METRIC_MTR_NAME, metricInfo.getName());
        unmarshalledMetricValue.put(MetricInfoSchema.METRIC_MTR_TYPE, DatastoreUtils.convertToClientMetricType(metricInfo.getMetricType()));
        unmarshalledMetricValue.put(MetricInfoSchema.METRIC_MTR_TIMESTAMP, formatDate(metricInfo.getFirstMessageOn()));
        unmarshalledMetricValue.put(MetricInfoSchema.METRIC_MTR_MSG_ID, metricInfo.getFirstMessageId());
        unmarshalledMetricInfo.put(MetricInfoSchema.METRIC_MTR, unmarshalledMetricValue);
        return unmarshalledMetricInfo;
    }

    private String formatDate(Date date) throws ParseException {
        if (date != null) {
            return KapuaDateUtils.formatDate(SchemaUtil.DATA_STORE_DATE_PATTERN, date);
        } else {
            return null;
        }
    }

    private Date parseDate(String date) throws ParseException {
        if (date != null) {
            return KapuaDateUtils.parseDate(SchemaUtil.DATA_STORE_DATE_PATTERN, date);
        } else {
            return null;
        }
    }

}
