package org.eclipse.kapua.service.datastore;

import org.eclipse.kapua.model.KapuaObjectFactory;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.model.MetricInfoListResult;
import org.eclipse.kapua.service.datastore.model.query.MetricInfoQuery;

/**
 * {@link MetricInfo} object factory
 *
 * @since 1.0.0
 */
public interface MetricInfoFactory extends KapuaObjectFactory {

    /**
     * Return a new metric information query
     * 
     * @param scopeId
     * @return
     * 
     * @since 1.0.0
     */
    public MetricInfoQuery newQuery(KapuaId scopeId);

    /**
     * Return a new metric information query
     * 
     * @return
     * 
     * @since 1.0.0
     */
    public MetricInfoListResult newListResult();
}
