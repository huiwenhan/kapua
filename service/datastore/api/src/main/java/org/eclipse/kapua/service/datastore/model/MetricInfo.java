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
package org.eclipse.kapua.service.datastore.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.id.KapuaIdAdapter;

/**
 * Information about device metric value. Metric is an arbitrary named value. We usually
 * keep only the most recent value of the metric.
 * 
 * @since 1.0.0
 */
@XmlRootElement(name = "metricInfo")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = { "id",
        "scopeId",
        "clientId",
        "channel",
        "name",
        "metricType",
        "firstMessageId",
        "firstMessageOn",
        "lastMessageId",
        "lastMessageOn" })
public interface MetricInfo extends Storable {

    /**
     * Get the record identifier
     * 
     * @return
     * 
     * @since 1.0.0
     */
    @XmlElement(name = "id")
    @XmlJavaTypeAdapter(StorableIdAdapter.class)
    public StorableId getId();

    /**
     * Set the record identifier
     * 
     * @param id
     * 
     * @since 1.0.0
     */
    @XmlElement(name = "id")
    @XmlJavaTypeAdapter(StorableIdAdapter.class)
    public void setId(StorableId id);

    /**
     * Get the scope id
     * 
     * @return
     * 
     * @since 1.0.0
     */
    @XmlElement(name = "scopeId")
    @XmlJavaTypeAdapter(KapuaIdAdapter.class)
    public KapuaId getScopeId();

    /**
     * Get the client identifier
     * 
     * @return
     * 
     * @since 1.0.0
     */
    @XmlElement(name = "clientId")
    public String getClientId();

    /**
     * Sets the client identifier
     * 
     * @param clientId
     *            The client identifier
     * 
     * @since 1.0.0
     */
    public void setClientId(String clientId);

    /**
     * Get the channel
     * 
     * @return
     * 
     * @since 1.0.0
     */
    @XmlElement(name = "channel")
    public String getChannel();

    /**
     * Set the channel
     * 
     * @param channel
     * 
     * @since 1.0.0
     */
    public void setChannel(String channel);

    /**
     * Gets the metric name
     * 
     * @return The metric name
     * @since 1.0.0
     */
    @XmlElement(name = "name")
    public String getName();

    /**
     * Sets the metric name
     * 
     * @param name
     *            The metric name
     * @since 1.0.0
     */
    public void setName(String name);

    /**
     * Get the metric type
     * 
     * @return The metric type
     * @since 1.0.0
     */
    @XmlElement(name = "metricType")
    @XmlJavaTypeAdapter(MetricInfoTypeAdapter.class)
    public Class<?> getMetricType();

    /**
     * Sets the metric type
     * 
     * @param metricType
     *            The metric type
     */
    public void setMetricType(Class<?> metricType);

    /**
     * Get the message identifier (of the first message published that containing this metric)
     * 
     * @return
     * 
     * @since 1.0.0
     */
    @XmlElement(name = "firstMessageId")
    @XmlJavaTypeAdapter(StorableIdAdapter.class)
    public StorableId getFirstMessageId();

    /**
     * Set the message identifier (of the first message published that containing this metric)
     * 
     * @param firstMessageId
     * 
     * @since 1.0.0
     */
    public void setFirstMessageId(StorableId firstMessageId);

    /**
     * Get the message timestamp (of the first message published that containing this metric)
     * 
     * @return
     * 
     * @since 1.0.0
     */
    @XmlElement(name = "firstMessageOn")
    public Date getFirstMessageOn();

    /**
     * Set the message timestamp (of the first message published that containing this metric)
     * 
     * @param firstMessageOn
     * 
     * @since 1.0.0
     */
    public void setFirstMessageOn(Date firstMessageOn);

    /**
     * Get the message identifier of the last published message for this metric.<br>
     * <b>Transient data field (the last publish message identifier should get from the message table by the find service)</b>
     * 
     * @return
     * 
     * @since 1.0.0
     */
    @XmlElement(name = "lastMessageId")
    @XmlJavaTypeAdapter(StorableIdAdapter.class)
    public StorableId getLastMessageId();

    /**
     * Set the message identifier of the last published message for this metric.<br>
     * <b>Transient data field (the last publish message identifier should get from the message table by the find service)</b>
     * 
     * @param lastMessageId
     * 
     * @since 1.0.0
     */
    public void setLastMessageId(StorableId lastMessageId);

    /**
     * Get the timestamp of the last published message for this metric.<br>
     * <b>Transient data field (the last publish timestamp should get from the message table by the find service)</b>
     * 
     * @return
     * 
     * @since 1.0.0
     */
    @XmlElement(name = "lastMessageOn")
    public Date getLastMessageOn();

    /**
     * Set the timestamp of the last published message for this metric.<br>
     * <b>Transient data field (the last publish timestamp should get from the message table by the find service)</b>
     * 
     * @param lastMessageOn
     * 
     * @since 1.0.0
     */
    public void setLastMessageOn(Date lastMessageOn);
}
