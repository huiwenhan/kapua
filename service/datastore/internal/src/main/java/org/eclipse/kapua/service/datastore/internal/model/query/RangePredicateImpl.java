/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.datastore.internal.model.query;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.service.datastore.model.query.RangePredicate;
import org.eclipse.kapua.service.datastore.model.query.StorableField;

/**
 * Implementation of query predicate for matching range values
 * 
 * @since 1.0
 *
 */
public class RangePredicateImpl<V extends Comparable<V>> implements RangePredicate<V>
{
    private StorableField field;
    private V        minValue;
    private V        maxValue;
//
//    private <V extends Comparable<V>> void checkRange() throws KapuaException
//    {
//        if (minValue == null || maxValue == null)
//            return;
//
//        if (minValue.compareTo(maxValue) > 0)
//            throw KapuaException.internalError("Min value must not be graeter than max value");
//    }

    /**
     * Default constructor
     */
    public RangePredicateImpl()
    {}

    /**
     * Construct a range predicate given the field and the values
     * 
     * @param field
     * @param minValue
     * @param maxValue
     */
    public  RangePredicateImpl(StorableField field, V minValue, V maxValue)
    {
        this.field = field;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public StorableField getField()
    {
        return this.field;
    }

    @Override
    public V getMinValue()
    {
        return minValue;
    }

    @Override
    public V getMaxValue()
    {
        return maxValue;
    }
}
