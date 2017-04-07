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
package org.eclipse.kapua.service.datastore.internal.mediator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import org.eclipse.kapua.commons.util.KapuaDateUtils;
import org.eclipse.kapua.service.datastore.client.Client;
import org.eclipse.kapua.service.datastore.client.ClientUnavailableException;
import org.eclipse.kapua.service.datastore.internal.client.ClientFactory;
import org.eclipse.kapua.model.id.KapuaId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Datastore utility class
 *
 * @since 1.0.0
 */
public class DatastoreUtils {

    private static final Logger s_logger = LoggerFactory.getLogger(DatastoreUtils.class);
    private final static Client DATASTORE_CLIENT;

    private static final char SPECIAL_DOT = '.';
    private static final String SPECIAL_DOT_ESC = "$2e";

    private static final char SPECIAL_DOLLAR = '$';
    private static final String SPECIAL_DOLLAR_ESC = "$24";

    public static final CharSequence ILLEGAL_CHARS = "\"\\/*?<>|,. ";

    public static final String CLIENT_METRIC_TYPE_STRING = "string";
    public static final String CLIENT_METRIC_TYPE_INTEGER = "integer";
    public static final String CLIENT_METRIC_TYPE_LONG = "long";
    public static final String CLIENT_METRIC_TYPE_FLOAT = "float";
    public static final String CLIENT_METRIC_TYPE_DOUBLE = "double";
    public static final String CLIENT_METRIC_TYPE_DATE = "date";
    public static final String CLIENT_METRIC_TYPE_BOOLEAN = "boolean";
    public static final String CLIENT_METRIC_TYPE_BINARY = "binary";

    public static final String CLIENT_METRIC_TYPE_SHORT_STRING = "str";
    public static final String CLIENT_METRIC_TYPE_SHORT_INTEGER = "int";
    public static final String CLIENT_METRIC_TYPE_SHORT_LONG = "lng";
    public static final String CLIENT_METRIC_TYPE_SHORT_FLOAT = "flt";
    public static final String CLIENT_METRIC_TYPE_SHORT_DOUBLE = "dbl";
    public static final String CLIENT_METRIC_TYPE_SHORT_DATE = "dte";
    public static final String CLIENT_METRIC_TYPE_SHORT_BOOLEAN = "bln";
    public static final String CLIENT_METRIC_TYPE_SHORT_BINARY = "bin";

    static {
        try {
            DATASTORE_CLIENT = ClientFactory.getInstance();// TODO check how to handle the exception
        } catch (ClientUnavailableException e) {
            throw new RuntimeException("Cannot get client instance", e);
        }
    }

    public static String getHashCode(String... components) {
        return DATASTORE_CLIENT.getHashCode(components);
    }

    private static String normalizeIndexName(String name) {
        String normName = null;
        try {
            DatastoreUtils.checkIdxAliasName(name);
            normName = name;
        } catch (IllegalArgumentException exc) {
            s_logger.trace(exc.getMessage());
            normName = name.toLowerCase().replace(ILLEGAL_CHARS, "_");
            DatastoreUtils.checkIdxAliasName(normName);
        }

        return normName;
    }

    /**
     * Normalize the metric name to be compliant to Kapua/Elasticserach constraints.<br>
     * It escapes the '$' and '.'
     *
     * @param name
     * @return
     * @since 1.0.0
     */
    public static String normalizeMetricName(String name) {
        String newName = name;
        if (newName.contains(".")) {
            newName = newName.replace(String.valueOf(SPECIAL_DOLLAR), SPECIAL_DOLLAR_ESC);
            newName = newName.replace(String.valueOf(SPECIAL_DOT), SPECIAL_DOT_ESC);
            s_logger.trace(String.format("Metric %s contains a special char '%s' that will be replaced with '%s'", name, String.valueOf(SPECIAL_DOT), SPECIAL_DOT_ESC));
        }

        return newName;
    }

    /**
     * Restore the metric name, so switch back to the 'not escaped' values for '$' and '.'
     *
     * @param normalizedName
     * @return
     * @since 1.0.0
     */
    public static String restoreMetricName(String normalizedName) {
        String oldName = normalizedName;
        String[] split = oldName.split(Pattern.quote("."));
        oldName = split[0];
        oldName = oldName.replace(SPECIAL_DOT_ESC, String.valueOf(SPECIAL_DOT));
        oldName = oldName.replace(SPECIAL_DOLLAR_ESC, String.valueOf(SPECIAL_DOLLAR));
        return oldName;
    }

    /**
     * Return the metric parts for the composed metric name (split the metric name by '.')
     *
     * @param fullName
     * @return
     */
    public static String[] getMetricParts(String fullName) {
        return fullName == null ? null : fullName.split(Pattern.quote("."));
    }

    /**
     * Check the index alias correctness.<br>
     * The alias cnnot be null, starts with '_', contains uppercase character or contains {@link DatastoreUtils#ILLEGAL_CHARS}
     *
     * @param alias
     * @since 1.0.0
     */
    public static void checkIdxAliasName(String alias) {
        if (alias == null || alias.isEmpty())
            throw new IllegalArgumentException(String.format("Alias name cannot be %s", alias == null ? "null" : "empty"));

        if (alias.startsWith("_"))
            throw new IllegalArgumentException(String.format("Alias name cannot start with _"));

        for (int i = 0; i < alias.length(); i++) {
            if (Character.isUpperCase(alias.charAt(i)))
                throw new IllegalArgumentException(String.format("Alias name cannot contain uppercase chars [found %s]", alias.charAt(i)));
        }

        if (alias.contains(ILLEGAL_CHARS))
            throw new IllegalArgumentException(String.format("Alias name cannot contain special chars [found oneof %s]", ILLEGAL_CHARS));
    }

    /**
     * Check the index name ({@link DatastoreUtils#checkIdxAliasName(String index)}
     *
     * @param index
     * @since 1.0.0
     */
    public static void checkIdxName(String index) {
        DatastoreUtils.checkIdxAliasName(index);
    }

    /**
     * Normalize the index alias name and replace the '-' with '_'
     *
     * @param alias
     * @return
     * @since 1.0.0
     */
    public static String normalizeIndexAliasName(String alias) {
        String aliasName = normalizeIndexName(alias);
        aliasName = aliasName.replace("-", "_");
        return aliasName;
    }

    /**
     * Normalize the account index name and and the suffix '-*'
     *
     * @param accountName
     * @return
     * @since 1.0.0
     */
    public static String getDataIndexName(KapuaId scopeId) {
        String indexName = DatastoreUtils.normalizedIndexName(scopeId.toStringId());
        indexName = String.format("%s-*", indexName);
        return indexName;
    }

    /**
     * Get the data index for the specified base name and timestamp
     *
     * @param baseName
     * @param timestamp
     * @return
     */
    public static String getDataIndexName(KapuaId scopeId, long timestamp) {
        String actualName = DatastoreUtils.normalizedIndexName(scopeId.toStringId());
        Calendar cal = KapuaDateUtils.getKapuaCalendar();
        cal.setTimeInMillis(timestamp);
        int year = cal.get(Calendar.YEAR);
        int weekOfTheYear = cal.get(Calendar.WEEK_OF_YEAR);
        actualName = String.format("%s-%04d-%02d", actualName, year, weekOfTheYear);
        return actualName;
    }

    /**
     * Get the Kapua index name for the specified base name
     *
     * @param baseName
     * @return
     * @since 1.0.0
     */
    public static String getRegistryIndexName(KapuaId scopeId) {
        String actualName = DatastoreUtils.normalizedIndexName(scopeId.toStringId());
        actualName = String.format(".%s", actualName);
        return actualName;
    }

    /**
     * Normalize the index ({@link DatastoreUtils#normalizeIndexName(String index)}
     *
     * @param index
     * @return
     * @since 1.0.0
     */
    public static String normalizedIndexName(String index) {
        return normalizeIndexName(index);
    }

    /**
     * Get the full metric name used to store the metric in Elasticsearch.<br>
     * The full metric name is composed by the metric and the type acronym as suffix ('.' is used as separator between the 2 parts)
     *
     * @param name
     * @param type
     * @return
     * @since 1.0.0
     */
    public static String getMetricValueQualifier(String name, String type) {
        String shortType = DatastoreUtils.getClientMetricFromAcronym(type);
        return String.format("%s.%s", name, shortType);
    }

    /**
     * Get the client metric type from the metric value type
     *
     * @param value
     * @return
     * @since 1.0.0
     */
    public static String getClientMetricFromType(Class<?> clazz) {

        if (clazz == null)
            throw new NullPointerException("Metric value must not be null");

        String value;
        if (clazz == String.class) {
            value = CLIENT_METRIC_TYPE_STRING;
        } else if (clazz == Integer.class) {
            value = CLIENT_METRIC_TYPE_INTEGER;
        } else if (clazz == Long.class) {
            value = CLIENT_METRIC_TYPE_LONG;
        } else if (clazz == Float.class) {
            value = CLIENT_METRIC_TYPE_FLOAT;
        } else if (clazz == Double.class) {
            value = CLIENT_METRIC_TYPE_DOUBLE;
        } else if (clazz == Boolean.class) {
            value = CLIENT_METRIC_TYPE_BOOLEAN;
        } else if (clazz == Date.class) {
            value = CLIENT_METRIC_TYPE_DATE;
        } else if (clazz == byte[].class) {
            value = CLIENT_METRIC_TYPE_BINARY;
        } else {
            throw new IllegalArgumentException(String.format("Metric value type for "));
        }
        return value;
    }

    public static String getClientMetricFromValue(Object value) {
        if (value == null)
            throw new NullPointerException("Metric value must not be null");

        if (value instanceof String)
            return CLIENT_METRIC_TYPE_STRING;

        if (value instanceof Integer)
            return CLIENT_METRIC_TYPE_INTEGER;

        if (value instanceof Long)
            return CLIENT_METRIC_TYPE_LONG;

        if (value instanceof Float)
            return CLIENT_METRIC_TYPE_FLOAT;

        if (value instanceof Double)
            return CLIENT_METRIC_TYPE_DOUBLE;

        if (value instanceof Date)
            return CLIENT_METRIC_TYPE_DATE;

        if (value instanceof Byte[])
            return CLIENT_METRIC_TYPE_BINARY;

        if (value instanceof Boolean)
            return CLIENT_METRIC_TYPE_BOOLEAN;

        throw new IllegalArgumentException(String.format("Metric value type for "));
    }

    /**
     * Get the Elasticsearch metric type acronym for the given Elasticsearch metric type full name
     *
     * @param esType
     * @return
     * @since 1.0.0
     */
    public static String getClientMetricFromAcronym(String acronym) {
        if (acronym.equals("string"))
            return CLIENT_METRIC_TYPE_SHORT_STRING;

        if (acronym.equals("integer"))
            return CLIENT_METRIC_TYPE_SHORT_INTEGER;

        if (acronym.equals("long"))
            return CLIENT_METRIC_TYPE_SHORT_LONG;

        if (acronym.equals("float"))
            return CLIENT_METRIC_TYPE_SHORT_FLOAT;

        if (acronym.equals("double"))
            return CLIENT_METRIC_TYPE_SHORT_DOUBLE;

        if (acronym.equals("boolean"))
            return CLIENT_METRIC_TYPE_SHORT_BOOLEAN;

        if (acronym.equals("date"))
            return CLIENT_METRIC_TYPE_SHORT_DATE;

        if (acronym.equals("binary")) {
            return CLIENT_METRIC_TYPE_SHORT_BINARY;
        }

        throw new IllegalArgumentException(String.format("Unknown type [%s]", acronym));
    }

    /**
     * Convert the metric value class type (Kapua side) to the proper string type description (Elasticsearch side)
     *
     * @param aClass
     * @return
     * @since 1.0.0
     */
    public static <T> String convertToClientMetricType(Class<T> aClass) {
        if (aClass == String.class)
            return CLIENT_METRIC_TYPE_STRING;

        if (aClass == Integer.class)
            return CLIENT_METRIC_TYPE_INTEGER;

        if (aClass == Long.class)
            return CLIENT_METRIC_TYPE_LONG;

        if (aClass == Float.class)
            return CLIENT_METRIC_TYPE_FLOAT;

        if (aClass == Double.class)
            return CLIENT_METRIC_TYPE_DOUBLE;

        if (aClass == Boolean.class)
            return CLIENT_METRIC_TYPE_BOOLEAN;

        if (aClass == Date.class)
            return CLIENT_METRIC_TYPE_DATE;

        if (aClass == byte[].class) {
            return CLIENT_METRIC_TYPE_BINARY;
        }

        throw new IllegalArgumentException(String.format("Unknown type [%s]", aClass.getName()));
    }

    /**
     * Convert the Kapua metric type to the corresponding Elasticsearch type
     *
     * @param kapuaType
     * @return
     * @since 1.0.0
     */
    public static String convertToClientMetricType(String kapuaType) {

        if ("string".equals(kapuaType) || "String".equals(kapuaType))
            return CLIENT_METRIC_TYPE_STRING;

        if ("integer".equals(kapuaType) || "Integer".equals(kapuaType))
            return CLIENT_METRIC_TYPE_INTEGER;

        if ("long".equals(kapuaType) || "Long".equals(kapuaType))
            return CLIENT_METRIC_TYPE_LONG;

        if ("float".equals(kapuaType) || "Float".equals(kapuaType))
            return CLIENT_METRIC_TYPE_FLOAT;

        if ("double".equals(kapuaType) || "Double".equals(kapuaType))
            return CLIENT_METRIC_TYPE_DOUBLE;

        if ("boolean".equals(kapuaType) || "Boolean".equals(kapuaType))
            return CLIENT_METRIC_TYPE_BOOLEAN;

        if ("date".equals(kapuaType) || "Date".equals(kapuaType))
            return CLIENT_METRIC_TYPE_DATE;

        if ("base64Binary".equals(kapuaType)) {
            return CLIENT_METRIC_TYPE_BINARY;
        }

        throw new IllegalArgumentException(String.format("Unknown type [%s]", kapuaType));
    }

    /**
     * Convert the Elasticsearch metric type to the corresponding Kapua type
     *
     * @param esType
     * @return
     * @since 1.0.0
     */
    public static Class<?> convertToKapuaType(String clientType) {

        Class<?> clazz;
        if (CLIENT_METRIC_TYPE_STRING.equals(clientType)) {
            clazz = String.class;
        } else if (CLIENT_METRIC_TYPE_INTEGER.equals(clientType)) {
            clazz = Integer.class;
        } else if (CLIENT_METRIC_TYPE_LONG.equals(clientType)) {
            clazz = Long.class;
        } else if (CLIENT_METRIC_TYPE_FLOAT.equals(clientType)) {
            clazz = Float.class;
        } else if (CLIENT_METRIC_TYPE_DOUBLE.equals(clientType)) {
            clazz = Double.class;
        } else if (CLIENT_METRIC_TYPE_BOOLEAN.equals(clientType)) {
            clazz = Boolean.class;
        } else if (CLIENT_METRIC_TYPE_DATE.equals(clientType)) {
            clazz = Date.class;
        } else if (CLIENT_METRIC_TYPE_BINARY.equals(clientType)) {
            clazz = byte[].class;
        } else {
            throw new IllegalArgumentException(String.format("Unknown type [%s]", clientType));
        }

        return clazz;
    }

    /**
     * Convert the Elasticsearch metric value to the proper Kapua object
     *
     * @param type
     * @param value
     * @return
     * @since 1.0.0
     */
    public static Object convertToKapuaObject(String type, String value) {

        if ("string".equals(type))
            return value;

        if ("int".equals(type))
            return value == null ? null : Integer.parseInt(value);

        if ("long".equals(type))
            return value == null ? null : Long.parseLong(value);

        if ("float".equals(type))
            return value == null ? null : Float.parseFloat(value);

        if ("double".equals(type))
            return value == null ? null : Double.parseDouble(value);

        if ("boolean".equals(type))
            return value == null ? null : Boolean.parseBoolean(value);

        if ("date".equals(type)) {
            try {
                SimpleDateFormat simplWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                simplWithMillis.setTimeZone(KapuaDateUtils.getKapuaTimeZone());
                return value == null ? null : simplWithMillis.parse(value);
            } catch (ParseException exc) {
                try {
                    SimpleDateFormat simpleWithoutMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    simpleWithoutMillis.setTimeZone(KapuaDateUtils.getKapuaTimeZone());
                    return value == null ? null : simpleWithoutMillis.parse(value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(String.format("Unknown data format [%s]", value));
                }
            }
        }

        if ("base64Binary".equals(type)) {
            return value;
        }

        throw new IllegalArgumentException(String.format("Unknown type [%s]", type));
    }

    /**
     * Convert the metric value to the correct type using the metric acronym type
     *
     * @param acronymType
     * @param value
     * @return
     * @since 1.0.0
     */
    public static Object convertToCorrectType(String acronymType, Object value) {
        Object convertedValue = null;
        if (CLIENT_METRIC_TYPE_SHORT_DOUBLE.equals(acronymType)) {
            if (value instanceof Number) {
                convertedValue = new Double(((Number) value).doubleValue());
            } else if (value instanceof String) {
                convertedValue = Double.parseDouble((String) value);
            } else {
                throw new IllegalArgumentException(String.format("Type [%s] cannot be converted to Double!", value.getClass()));
            }
        } else if (CLIENT_METRIC_TYPE_SHORT_FLOAT.equals(acronymType)) {
            if (value instanceof Number) {
                convertedValue = new Float(((Number) value).floatValue());
            } else if (value instanceof String) {
                convertedValue = Float.parseFloat((String) value);
            } else {
                throw new IllegalArgumentException(String.format("Type [%s] cannot be converted to Double!", value.getClass()));
            }
        } else if (CLIENT_METRIC_TYPE_SHORT_INTEGER.equals(acronymType)) {
            if (value instanceof Number) {
                convertedValue = new Integer(((Number) value).intValue());
            } else if (value instanceof String) {
                convertedValue = Integer.parseInt((String) value);
            } else {
                throw new IllegalArgumentException(String.format("Type [%s] cannot be converted to Double!", value.getClass()));
            }
        } else if (CLIENT_METRIC_TYPE_SHORT_LONG.equals(acronymType)) {
            if (value instanceof Number) {
                convertedValue = new Long(((Number) value).longValue());
            } else if (value instanceof String) {
                convertedValue = Long.parseLong((String) value);
            } else {
                throw new IllegalArgumentException(String.format("Type [%s] cannot be converted to Long!", value.getClass()));
            }
        } else {
            // no need to translate for others field type
            convertedValue = value;
        }
        return convertedValue;
    }

    /**
     * Get the query timeout (default value)
     *
     * @return
     * @since 1.0.0
     */
    public static long getQueryTimeout() {
        return 15000;
    }

    /**
     * Get the scroll timeout (default value)
     *
     * @return
     * @since 1.0.0
     */
    public static long getScrollTimeout() {
        return 60000;
    }
}
