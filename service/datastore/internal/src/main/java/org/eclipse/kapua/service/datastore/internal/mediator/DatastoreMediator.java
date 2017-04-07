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

import java.util.Map;

import org.eclipse.kapua.KapuaIllegalArgumentException;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.message.KapuaPayload;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.ChannelInfoRegistryService;
import org.eclipse.kapua.service.datastore.ClientInfoRegistryService;
import org.eclipse.kapua.service.datastore.MetricInfoRegistryService;
import org.eclipse.kapua.service.datastore.client.ClientException;
import org.eclipse.kapua.service.datastore.client.QueryMappingException;
import org.eclipse.kapua.service.datastore.internal.ChannelInfoRegistryFacade;
import org.eclipse.kapua.service.datastore.internal.ClientInfoRegistryFacade;
import org.eclipse.kapua.service.datastore.internal.MessageStoreFacade;
import org.eclipse.kapua.service.datastore.internal.MetricInfoRegistryFacade;
import org.eclipse.kapua.service.datastore.internal.model.ChannelInfoImpl;
import org.eclipse.kapua.service.datastore.internal.model.ClientInfoImpl;
import org.eclipse.kapua.service.datastore.internal.model.MetricInfoImpl;
import org.eclipse.kapua.service.datastore.internal.model.StorableIdImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.ChannelMatchPredicateImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.MessageQueryImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.MetricInfoQueryImpl;
import org.eclipse.kapua.service.datastore.internal.schema.Metadata;
import org.eclipse.kapua.service.datastore.internal.schema.Schema;
import org.eclipse.kapua.service.datastore.model.ChannelInfo;
import org.eclipse.kapua.service.datastore.model.ClientInfo;
import org.eclipse.kapua.service.datastore.model.DatastoreMessage;
import org.eclipse.kapua.service.datastore.model.MetricInfo;

/**
 * Datastore mediator definition
 *
 * @since 1.0.0
 */
public class DatastoreMediator implements MessageStoreMediator,
        ClientInfoRegistryMediator,
        ChannelInfoRegistryMediator,
        MetricInfoRegistryMediator {

    private static DatastoreMediator instance;

    private final Schema esSchema;

    private MessageStoreFacade messageStoreFacade;
    private ClientInfoRegistryFacade clientInfoStoreFacade;
    private ChannelInfoRegistryFacade channelInfoStoreFacade;
    private MetricInfoRegistryFacade metricInfoStoreFacade;

    static {
        instance = new DatastoreMediator();

        // Be sure the data registry services are instantiated
        KapuaLocator.getInstance().getService(ClientInfoRegistryService.class);
        KapuaLocator.getInstance().getService(ChannelInfoRegistryService.class);
        KapuaLocator.getInstance().getService(MetricInfoRegistryService.class);
    }

    private DatastoreMediator() {
        esSchema = new Schema();
    }

    /**
     * Get the {@link DatastoreMediator} instance (singleton)
     *
     * @return
     * @since 1.0.0
     */
    public static DatastoreMediator getInstance() {
        return instance;
    }

    /**
     * Set the message store facade
     *
     * @param messageStoreFacade
     * @since 1.0.0
     */
    public void setMessageStoreFacade(MessageStoreFacade messageStoreFacade) {
        this.messageStoreFacade = messageStoreFacade;
    }

    /**
     * Set the client info facade
     *
     * @param clientInfoStoreFacade
     * @since 1.0.0
     */
    public void setClientInfoStoreFacade(ClientInfoRegistryFacade clientInfoStoreFacade) {
        this.clientInfoStoreFacade = clientInfoStoreFacade;
    }

    /**
     * Set the channel info facade
     *
     * @param channelInfoStoreFacade
     * @since 1.0.0
     */
    public void setChannelInfoStoreFacade(ChannelInfoRegistryFacade channelInfoStoreFacade) {
        this.channelInfoStoreFacade = channelInfoStoreFacade;
    }

    /**
     * Set the metric info facade
     *
     * @param metricInfoStoreFacade
     * @since 1.0.0
     */
    public void setMetricInfoStoreFacade(MetricInfoRegistryFacade metricInfoStoreFacade) {
        this.metricInfoStoreFacade = metricInfoStoreFacade;
    }

    /*
     * Message Store Mediator methods
     */

    @Override
    public Metadata getMetadata(KapuaId scopeId, long indexedOn) throws ClientException {
        return esSchema.synch(scopeId, indexedOn);
    }

    @Override
    public void onUpdatedMappings(KapuaId scopeId, long indexedOn, Map<String, Metric> metrics) throws ClientException {
        esSchema.updateMessageMappings(scopeId, indexedOn, metrics);
    }

    @Override
    public void onAfterMessageStore(MessageInfo messageInfo,
            DatastoreMessage message)
            throws KapuaIllegalArgumentException,
            ConfigurationException,
            ClientException {
        // convert semantic channel to String
        String semanticChannel = message.getChannel() != null ? message.getChannel().toString() : "";

        ClientInfoImpl clientInfo = new ClientInfoImpl(message.getScopeId());
        clientInfo.setClientId(message.getClientId());
        clientInfo.setFirstMessageId(message.getDatastoreId());
        clientInfo.setFirstMessageOn(message.getTimestamp());
        String clientInfoId = ClientInfoField.getOrDeriveId(null, message.getScopeId(), message.getClientId());
        clientInfo.setId(new StorableIdImpl(clientInfoId));
        clientInfoStoreFacade.upstore(clientInfo);

        ChannelInfoImpl channelInfo = new ChannelInfoImpl(message.getScopeId());
        channelInfo.setClientId(message.getClientId());
        channelInfo.setChannel(semanticChannel);
        channelInfo.setFirstMessageId(message.getDatastoreId());
        channelInfo.setFirstMessageOn(message.getTimestamp());
        channelInfo.setId(new StorableIdImpl(ChannelInfoField.getOrDeriveId(null, channelInfo)));
        channelInfoStoreFacade.upstore(channelInfo);

        KapuaPayload payload = message.getPayload();
        if (payload == null)
            return;

        Map<String, Object> metrics = payload.getProperties();
        if (metrics == null)
            return;

        int i = 0;
        MetricInfoImpl[] messageMetrics = new MetricInfoImpl[metrics.size()];
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            MetricInfoImpl metricInfo = new MetricInfoImpl(message.getScopeId());
            metricInfo.setClientId(message.getClientId());
            metricInfo.setChannel(semanticChannel);
            metricInfo.setName(entry.getKey());

            // TODO ALBERTO
            metricInfo.setMetricType(entry.getValue().getClass());
            metricInfo.setFirstMessageId(message.getDatastoreId());
            metricInfo.setFirstMessageOn(message.getTimestamp());
            metricInfo.setId(new StorableIdImpl(MetricInfoField.getOrDeriveId(null, metricInfo)));
            messageMetrics[i++] = metricInfo;
        }

        metricInfoStoreFacade.upstore(messageMetrics);
    }

    /*
     * ClientInfo Store Mediator methods
     */

    @Override
    public void onAfterClientInfoDelete(KapuaId scopeId, ClientInfo clientInfo)
            throws KapuaIllegalArgumentException,
            ConfigurationException,
            ClientException {
        messageStoreFacade.delete(scopeId, clientInfo.getFirstMessageId());
    }

    /*
     * ChannelInfo Store Mediator methods
     */

    @Override
    public void onBeforeChannelInfoDelete(ChannelInfo channelInfo)
            throws KapuaIllegalArgumentException,
            ConfigurationException,
            QueryMappingException,
            ClientException {
        ChannelMatchPredicateImpl predicate = new ChannelMatchPredicateImpl(MessageField.CHANNEL.field(), channelInfo.getChannel());

        MessageQueryImpl messageQuery = new MessageQueryImpl(channelInfo.getScopeId());
        messageQuery.setPredicate(predicate);
        messageStoreFacade.delete(messageQuery);

        MetricInfoQueryImpl metricInfoQuery = new MetricInfoQueryImpl(channelInfo.getScopeId());
        metricInfoQuery.setPredicate(predicate);
        metricInfoStoreFacade.delete(metricInfoQuery);
    }

    @Override
    public void onAfterChannelInfoDelete(ChannelInfo channelInfo) {
        // TODO Auto-generated method stub

    }

    /*
     * MetricInfo Store Mediator methods
     */

    @Override
    public void onAfterMetricInfoDelete(KapuaId scopeId, MetricInfo metricInfo) {
        // TODO Auto-generated method stub

    }
}
