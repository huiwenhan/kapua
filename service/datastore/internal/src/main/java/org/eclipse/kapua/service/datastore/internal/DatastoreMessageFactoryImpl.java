package org.eclipse.kapua.service.datastore.internal;

import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.DatastoreMessageFactory;
import org.eclipse.kapua.service.datastore.internal.model.MessageListResultImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.MessageQueryImpl;
import org.eclipse.kapua.service.datastore.model.MessageListResult;
import org.eclipse.kapua.service.datastore.model.query.MessageQuery;

/**
 * {@link DatastoreMessageFactory} implementation
 * 
 * @since 1.0.0
 */
@KapuaProvider
public class DatastoreMessageFactoryImpl implements DatastoreMessageFactory {

    @Override
    public MessageQuery newQuery(KapuaId scopeId) {
        return new MessageQueryImpl(scopeId);
    }

    @Override
    public MessageListResult newListResult() {
        return new MessageListResultImpl();
    }
}
