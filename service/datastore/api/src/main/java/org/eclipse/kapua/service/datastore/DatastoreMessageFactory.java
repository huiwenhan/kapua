package org.eclipse.kapua.service.datastore;

import org.eclipse.kapua.model.KapuaObjectFactory;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.model.DatastoreMessage;
import org.eclipse.kapua.service.datastore.model.MessageListResult;
import org.eclipse.kapua.service.datastore.model.query.MessageQuery;

/**
 * {@link DatastoreMessage} object factory
 * 
 * @since 1.0.0
 *
 */

public interface DatastoreMessageFactory extends KapuaObjectFactory {

    
    /**
     * Return a new datastore message query
     * 
     * @param scopeId
     * @return
     * 
     * @since 1.0.0
     */
    public MessageQuery newQuery(KapuaId scopeId);

    /**
     * Return a new metric information query
     * 
     * @return
     * 
     * @since 1.0.0
     */
    public MessageListResult newListResult();

}
