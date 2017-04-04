package org.eclipse.kapua.service.datastore.model.query;

import org.eclipse.kapua.model.KapuaObjectFactory;

/**
 * {@link StorablePredicate} object factory
 * 
 * @since 1.0.0
 */
public interface StorablePredicateFactory extends KapuaObjectFactory {

    /**
     * Returns a {@link AndPredicate} to chain multiple {@link StorablePredicate}s.
     * 
     * @return A {@link AndPredicate} to chain multiple {@link StorablePredicate}s.
     * 
     * @since 1.0.0
     */
    public AndPredicate newAndPredicate();
    
    /**
     * Return a new {@link TermPredicate}.
     * 
     * @param field
     * @param value
     * @return
     * 
     * @since 1.0.0
     */
    public <V> TermPredicate newTermPredicate(StorableField field, V value);

    /**
     * Return a new {@link RangePredicate}.
     * 
     * @param field
     * @param minValue
     * @param maxValue
     * @return
     * 
     * @since 1.0.0
     */
    public <V extends Comparable<V>> RangePredicate<V> newRangePredicate(StorableField field, V minValue, V maxValue);
}
    