package org.eclipse.kapua.service.datastore;

import org.eclipse.kapua.model.KapuaObjectFactory;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.model.ClientInfo;
import org.eclipse.kapua.service.datastore.model.ClientInfoListResult;
import org.eclipse.kapua.service.datastore.model.query.ClientInfoQuery;

/**
 * {@link ClientInfo} object factory
 *
 * @since 1.0.0
 */
public interface ClientInfoFactory extends KapuaObjectFactory {

    /**
     * Return a new client information query
     * 
     * @param scopeId
     * @return
     * 
     * @since 1.0.0
     */
    public ClientInfoQuery newQuery(KapuaId scopeId);

    /**
     * Return a new client information query
     * 
     * @return
     * 
     * @since 1.0.0
     */
    public ClientInfoListResult newListResult();
}
