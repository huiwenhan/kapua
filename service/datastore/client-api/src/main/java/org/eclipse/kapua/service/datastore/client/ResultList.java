package org.eclipse.kapua.service.datastore.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Query result list
 * 
 * @param <T>
 *            result list object type
 */
public class ResultList<T> {

    private List<T> result;
    private long totalCount;

    /**
     * Default constructor
     * 
     * @param totalCount
     */
    public ResultList(long totalCount) {
        result = new ArrayList<T>();
        this.totalCount = totalCount;
    }

    /**
     * Add object to the result list
     * 
     * @param object
     */
    public void add(T object) {
        result.add(object);
    }

    /**
     * Get the result list
     * 
     * @return
     */
    public List<T> getResult() {
        return result;
    }

    /**
     * Get the object total count (objects that matching the search criteria)
     * 
     * @return
     */
    public long getTotalCount() {
        return totalCount;
    }

}
