package org.eclipse.kapua.service.datastore.internal;

import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.datastore.MetricInfoFactory;
import org.eclipse.kapua.service.datastore.internal.model.MetricInfoListResultImpl;
import org.eclipse.kapua.service.datastore.internal.model.query.MetricInfoQueryImpl;
import org.eclipse.kapua.service.datastore.model.MetricInfoListResult;
import org.eclipse.kapua.service.datastore.model.query.MetricInfoQuery;


/**
 * {@link MetricInfoFactory} implementation.
 * 
 * @since 1.0.0
 */
@KapuaProvider
public class MetricInfoFactoryImpl implements MetricInfoFactory {

    @Override
    public MetricInfoQuery newQuery(KapuaId scopeId) {
        return new MetricInfoQueryImpl(scopeId);
    }

    @Override
    public MetricInfoListResult newListResult() {
        return new MetricInfoListResultImpl();
    }
}
