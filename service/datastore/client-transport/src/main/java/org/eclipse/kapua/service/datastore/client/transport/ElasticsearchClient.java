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
package org.eclipse.kapua.service.datastore.client.transport;

import org.eclipse.kapua.service.datastore.client.ClientUnavailableException;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Elasticsearch client wrapper
 *
 * @since 1.0
 */
public class ElasticsearchClient {

    @SuppressWarnings("unused")
    private static final Logger s_logger = LoggerFactory.getLogger(ElasticsearchClient.class);

    private static EsClientProvider clientProvider;

    /**
     * Get a new Elasticsearch client instance
     *
     * @return
     * @throws ClientUnavailableException
     */
    public synchronized static Client getInstance()
            throws ClientUnavailableException {
        if (clientProvider == null) {
            ClientSettings config = ClientSettings.getInstance();
            String clientProvidername = config.getString(ClientSettingsKey.ELASTICSEARCH_CLIENT_PROVIDER);
            ClassLoader classLoader = ElasticsearchClient.class.getClassLoader();

            try {
                boolean initialize = true;
                Class<?> clazz = Class.forName(clientProvidername, !initialize, classLoader);
                clientProvider = (EsClientProvider) clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new ClientUnavailableException("Client Provider can't be created", e);
            } catch (InstantiationException e) {
                throw new ClientUnavailableException("Client Provider can't be created", e);
            } catch (IllegalAccessException e) {
                throw new ClientUnavailableException("Client Provider can't be created", e);
            }
        }

        return clientProvider.getClient();
    }
}
