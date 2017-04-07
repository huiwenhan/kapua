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
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.kapua.message.KapuaChannel;
import org.eclipse.kapua.message.KapuaPayload;
import org.eclipse.kapua.message.KapuaPosition;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.id.KapuaIdAdapter;

/**
 * Message returned by the data store find services
 *
 * @since 1.0
 */
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = { //
        "id", //
        "datastoreId", //
        "scopeId", //
        "deviceId", //
        "clientId", //
        "receivedOn", //
        "sentOn", //
        "capturedOn", //
        "position", //
        "channel", //
        "payload", //
}) //
public interface DatastoreMessage extends Storable {

    /**
     * Stored message identifier
     * 
     * @return
     */
    @XmlElement(name = "datastoreId")
    @XmlJavaTypeAdapter(StorableIdAdapter.class)
    public StorableId getDatastoreId();

    /**
     * Stored message identifier
     * 
     * @return
     */
    public void setDatastoreId(StorableId storableId);

    /**
     * Stored message timestamp
     * 
     * @return
     */
    public Date getTimestamp();

    // TODO TOCHECK security for updates (the timestamp shouldn't updated outside datastore)
    /**
     * Stored message timestamp
     * 
     * @return
     */
    public void setTimestamp(Date timestamp);


    /**
     * Get the message identifier
     *
     * @return
     */
    @XmlElement(name = "id")
    public UUID getId();

    /**
     * Set the message identifier
     *
     * @param id
     */
    public void setId(UUID id);

    /**
     * Get scope identifier
     *
     * @return
     */
    @XmlElement(name = "scopeId")
    @XmlJavaTypeAdapter(KapuaIdAdapter.class)
    public KapuaId getScopeId();

    /**
     * Set scope identifier
     *
     * @param scopeId
     */
    public void setScopeId(KapuaId scopeId);

    /**
     * Get client identifier
     *
     * @return
     */
    @XmlElement(name = "clientId")
    public String getClientId();

    /**
     * Set client identifier
     *
     * @param clientId
     */
    public void setClientId(String clientId);

    /**
     * Get device identifier
     *
     * @return
     */
    @XmlElement(name = "deviceId")
    @XmlJavaTypeAdapter(KapuaIdAdapter.class)
    public KapuaId getDeviceId();

    /**
     * Set device identifier
     *
     * @param deviceId
     */
    public void setDeviceId(KapuaId deviceId);

    /**
     * Get the message received on date
     *
     * @return
     */
    @XmlElement(name = "receivedOn")
    public Date getReceivedOn();

    /**
     * Set the message received on date
     *
     * @param receivedOn
     */
    public void setReceivedOn(Date receivedOn);

    /**
     * Get the message sent on date
     *
     * @return
     */
    @XmlElement(name = "sentOn")
    public Date getSentOn();

    /**
     * Set the message sent on date
     *
     * @param sentOn
     */
    public void setSentOn(Date sentOn);

    /**
     * Get the message captured on date
     *
     * @return
     */
    @XmlElement(name = "capturedOn")
    public Date getCapturedOn();

    /**
     * Set the message captured on date
     *
     * @param capturedOn
     */
    public void setCapturedOn(Date capturedOn);

    /**
     * Get the device position
     *
     * @return
     */
    @XmlElement(name = "position")
    public KapuaPosition getPosition();

    /**
     * Set the device position
     *
     * @param position
     */
    public void setPosition(KapuaPosition position);

    /**
     * Get the message channel
     *
     * @return
     */
    @XmlElement(name = "channel")
    public KapuaChannel getChannel();

    /**
     * Set the message channel
     *
     * @param semanticChannel
     */
    public void setChannel(KapuaChannel semanticChannel);

    /**
     * Get the message payload
     *
     * @return
     */
    @XmlElement(name = "payload")
    public KapuaPayload getPayload();

    /**
     * Set the message payload
     *
     * @param payload
     */
    public void setPayload(KapuaPayload payload);
}
