package org.eclipse.kapua.service.datastore;

import org.eclipse.kapua.model.KapuaObjectFactory;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.model.ChannelInfo;
import org.eclipse.kapua.service.datastore.model.ChannelInfoListResult;
import org.eclipse.kapua.service.datastore.model.query.ChannelInfoQuery;

/**
 * {@link ChannelInfo} object factory
 *
 * @since 1.0.0
 */
public interface ChannelInfoFactory extends KapuaObjectFactory {

    /**
     * Return a new channel information query
     * 
     * @param scopeId
     * @return
     * 
     * @since 1.0.0
     */
    public ChannelInfoQuery newQuery(KapuaId scopeId);

    /**
     * Return a new channel information query
     * 
     * @return
     * 
     * @since 1.0.0
     */
    public ChannelInfoListResult newListResult();
}
