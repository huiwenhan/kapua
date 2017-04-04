package org.eclipse.kapua.service.datastore.internal;

import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.ChannelInfoFactory;
import org.eclipse.kapua.service.datastore.internal.model.ChannelInfoListResultImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.ChannelInfoQueryImpl;
import org.eclipse.kapua.service.datastore.model.ChannelInfoListResult;
import org.eclipse.kapua.service.datastore.model.query.ChannelInfoQuery;


/**
 * {@link ChannelInfoFactory} implementation.
 * 
 * @since 1.0.0
 */
@KapuaProvider
public class ChannelInfoFactoryImpl implements ChannelInfoFactory {

    @Override
    public ChannelInfoQuery newQuery(KapuaId scopeId) {
        return new ChannelInfoQueryImpl(scopeId);
    }

    @Override
    public ChannelInfoListResult newListResult() {
        return new ChannelInfoListResultImpl();
    }
}
