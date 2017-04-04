package org.eclipse.kapua.service.datastore.internal;

import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.ClientInfoFactory;
import org.eclipse.kapua.service.datastore.internal.model.ClientInfoListResultImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.ClientInfoQueryImpl;
import org.eclipse.kapua.service.datastore.model.ClientInfoListResult;
import org.eclipse.kapua.service.datastore.model.query.ClientInfoQuery;


/**
 * {@link ClientInfoFactory} implementation.
 * 
 * @since 1.0.0
 */
@KapuaProvider
public class ClientInfoFactoryImpl implements ClientInfoFactory {

    @Override
    public ClientInfoQuery newQuery(KapuaId scopeId) {
        return new ClientInfoQueryImpl(scopeId);
    }

    @Override
    public ClientInfoListResult newListResult() {
        return new ClientInfoListResultImpl();
    }
}
