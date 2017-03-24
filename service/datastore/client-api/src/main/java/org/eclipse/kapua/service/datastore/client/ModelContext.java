package org.eclipse.kapua.service.datastore.client;

import java.util.Map;

public interface ModelContext {

    public String TYPE_DESCRIPTOR_KEY = "type_descriptor";
    public String DATASTORE_ID_KEY = "datastore_id";

    public <T> T unmarshal(Class<T> clazz, Map<String, Object> serializedObject) throws DatamodelMappingException;

    public Map<String, Object> marshal(Object object) throws DatamodelMappingException;

}
