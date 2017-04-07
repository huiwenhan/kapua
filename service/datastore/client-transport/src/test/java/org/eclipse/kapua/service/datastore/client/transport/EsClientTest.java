/*******************************************************************************
 * Copyright (c) 2011, 2017 Red Hat and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.kapua.service.datastore.client.transport;

import org.eclipse.kapua.service.datastore.client.ClientUnavailableException;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.Ignore;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class EsClientTest {

    @Test
    public void elascticSearchClientShouldDefaultToLocalhost() throws UnknownHostException, ClientUnavailableException {
        // When
        TransportClient client = (TransportClient) ElasticsearchClient.getInstance();

        // Then
        String host = client.listedNodes().get(0).getHostAddress();
        assertThat(host).isEqualTo("127.0.0.1");
    }

}
