package org.eclipse.kapua.service.datastore.client.transport;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import org.eclipse.kapua.service.datastore.client.BulkUpdateRequest;
import org.eclipse.kapua.service.datastore.client.BulkUpdateResponse;
import org.eclipse.kapua.service.datastore.client.ClientErrorCodes;
import org.eclipse.kapua.service.datastore.client.ClientException;
import org.eclipse.kapua.service.datastore.client.ClientUnavailableException;
import org.eclipse.kapua.service.datastore.client.ClientUndefinedException;
import org.eclipse.kapua.service.datastore.client.IndexExistsRequest;
import org.eclipse.kapua.service.datastore.client.IndexExistsResponse;
import org.eclipse.kapua.service.datastore.client.InsertRequest;
import org.eclipse.kapua.service.datastore.client.InsertResponse;
import org.eclipse.kapua.service.datastore.client.ResultList;
import org.eclipse.kapua.service.datastore.client.ModelContext;
import org.eclipse.kapua.service.datastore.client.QueryConverter;
import org.eclipse.kapua.service.datastore.client.TypeDescriptor;
import org.eclipse.kapua.service.datastore.client.UpdateRequest;
import org.eclipse.kapua.service.datastore.client.UpdateResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.hash.Hashing;

/**
 * Client implementation based on Elasticsearch transport client
 *
 */
public class TransportClient implements org.eclipse.kapua.service.datastore.client.Client {

    private static final Logger logger = LoggerFactory.getLogger(TransportClient.class);

    private static final String CLIENT_UNDEFINED_MSG = "Elasticsearch client must be not null";

    private org.elasticsearch.client.Client client;
    private ModelContext modelContext;
    private QueryConverter queryConverter;

    /**
     * Constructs the client
     * 
     * @throws ClientUnavailableException
     */
    public TransportClient() throws ClientUnavailableException {
        client = ElasticsearchClient.getInstance();
    }

    /**
     * Constructs the client with the provided model context and query converter
     * 
     * @param modelContext
     * @param queryConverter
     * @throws ClientUnavailableException
     */
    public TransportClient(ModelContext modelContext, QueryConverter queryConverter) throws ClientUnavailableException {
        this();
        this.modelContext = modelContext;
        this.queryConverter = queryConverter;
    }

    @Override
    public void setModelContext(ModelContext modelContext) {
        this.modelContext = modelContext;
    }

    @Override
    public void setQueryConverter(QueryConverter queryConverter) {
        this.queryConverter = queryConverter;
    }

    @Override
    public InsertResponse insert(InsertRequest insertRequest) throws ClientException {
        checkClient();
        Map<String, Object> storableMap = modelContext.marshal(insertRequest.getStorable());
        logger.info("INSERT:'" + storableMap.toString() + "'");
        IndexRequest idxRequest = new IndexRequest(insertRequest.getTypeDescriptor().getIndex(), insertRequest.getTypeDescriptor().getType()).source(storableMap);
        IndexResponse response = client.index(idxRequest).actionGet(TimeValue.timeValueMillis(getQueryTimeout()));
        return new InsertResponse(response.getId(), insertRequest.getTypeDescriptor());
    }

    @Override
    public UpdateResponse upsert(UpdateRequest upsertRequest) throws ClientException {
        checkClient();
        Map<String, Object> storableMap = modelContext.marshal(upsertRequest.getStorable());
        IndexRequest idxRequest = new IndexRequest(upsertRequest.getTypeDescriptor().getIndex(), upsertRequest.getTypeDescriptor().getType(), upsertRequest.getId()).source(storableMap);
        org.elasticsearch.action.update.UpdateRequest updateRequest = new org.elasticsearch.action.update.UpdateRequest(upsertRequest.getTypeDescriptor().getIndex(),
                upsertRequest.getTypeDescriptor().getType(), upsertRequest.getId()).doc(storableMap);
        org.elasticsearch.action.update.UpdateResponse response = client.update(updateRequest.upsert(idxRequest)).actionGet(TimeValue.timeValueMillis(getQueryTimeout()));
        return new UpdateResponse(response.getId(), upsertRequest.getTypeDescriptor());
    }

    @Override
    public BulkUpdateResponse upsert(BulkUpdateRequest bulkUpsertRequest) throws ClientException {
        checkClient();
        BulkRequest bulkRequest = new BulkRequest();
        for (UpdateRequest upsertRequest : bulkUpsertRequest.getRequest()) {
            String type = upsertRequest.getTypeDescriptor().getType();
            String index = upsertRequest.getTypeDescriptor().getIndex();
            String id = upsertRequest.getId();
            Map<String, Object> mappedObject = modelContext.marshal(upsertRequest.getStorable());
            IndexRequest idxRequest = new IndexRequest(index, type, id).source(mappedObject);
            org.elasticsearch.action.update.UpdateRequest updateRequest = new org.elasticsearch.action.update.UpdateRequest(index, type, id).doc(mappedObject);
            updateRequest.upsert(idxRequest);
            bulkRequest.add(updateRequest);
        }
        
        BulkResponse bulkResponse = client.bulk(bulkRequest).actionGet(TimeValue.timeValueMillis(getQueryTimeout()));

        BulkUpdateResponse response = new BulkUpdateResponse();
        BulkItemResponse[] itemResponses = bulkResponse.getItems();
        if (itemResponses != null) {
            for (BulkItemResponse bulkItemResponse : itemResponses) {
                String metricId = ((org.elasticsearch.action.update.UpdateResponse) bulkItemResponse.getResponse()).getId();
                String indexName = bulkItemResponse.getIndex();
                String typeName = bulkItemResponse.getType();
                if (bulkItemResponse.isFailed()) {
                    String failureMessage = bulkItemResponse.getFailureMessage();
                    response.add(new UpdateResponse(metricId, new TypeDescriptor(indexName, typeName), failureMessage));
                    logger.trace(String.format("Upsert failed [%s, %s, %s]",
                            indexName, typeName, failureMessage));
                    continue;
                }
                response.add(new UpdateResponse(metricId, new TypeDescriptor(indexName, typeName)));
                logger.debug(String.format("Upsert on channel metric succesfully executed [%s.%s, %s]",
                        indexName, typeName, metricId));
            }
        }
        return response;
    }

    @Override
    public <T> T find(TypeDescriptor typeDescriptor, Object query, Class<T> clazz) throws ClientException {
        ResultList<T> result = query(typeDescriptor, query, clazz);
        if (result.getTotalCount() == 0) {
            return null;
        } else {
            return result.getResult().get(0);
        }
    }

    @Override
    public <T> ResultList<T> query(TypeDescriptor typeDescriptor, Object query, Class<T> clazz) throws ClientException {
        checkClient();
        ObjectNode queryMap = queryConverter.convertQuery(query);
        logger.debug("Converted query:'" + queryMap.toString() + "'");
        SearchResponse response = null;
        ObjectNode fetchSourceFields = (ObjectNode) queryMap.path(QueryConverter.SOURCE_KEY);
        String[] includesFields = toIncludedExcludedFields(fetchSourceFields.path(QueryConverter.INCLUDES_KEY));
        String[] excludesFields = toIncludedExcludedFields(fetchSourceFields.path(QueryConverter.EXCLUDES_KEY));
        SearchRequestBuilder searchReqBuilder = client.prepareSearch(typeDescriptor.getIndex());
        searchReqBuilder.setTypes(typeDescriptor.getType())
                .setSource(toSearchSourceBuilder(queryMap))
                .setFetchSource(includesFields, excludesFields);
        // @SuppressWarnings("unchecked")
        ArrayNode sortFields = (ArrayNode) queryMap.path(QueryConverter.SORT_KEY);
        for (JsonNode node : sortFields) {
            // searchReqBuilder.addSort(node., node.get);
        }
        response = searchReqBuilder
                .execute()
                .actionGet(TimeValue.timeValueMillis(getQueryTimeout()));
        SearchHit[] searchHits = response.getHits().getHits();
        // TODO verify total count
        long totalCount = response.getHits().getTotalHits();

        if (totalCount > Integer.MAX_VALUE) {
            throw new RuntimeException("Total hits exceeds integer max value");
        }

        ResultList<T> result = new ResultList<T>(totalCount);
        if (searchHits != null) {
            for (SearchHit searchHit : searchHits) {
                Map<String, Object> object = searchHit.getSource();
                object.put(ModelContext.TYPE_DESCRIPTOR_KEY, new TypeDescriptor(searchHit.getIndex(), searchHit.getType()));
                object.put(ModelContext.DATASTORE_ID_KEY, searchHit.getId());
                result.add(modelContext.unmarshal(clazz, object));
            }
        }
        return result;
    }

    @Override
    public long count(TypeDescriptor typeDescriptor, Object query) throws ClientException {
        checkClient();
        // TODO check for fetch none
        ObjectNode queryMap = queryConverter.convertQuery(query);
        SearchRequestBuilder searchReqBuilder = client.prepareSearch(typeDescriptor.getIndex());
        SearchResponse response = searchReqBuilder.setTypes(typeDescriptor.getType())
                .setSource(toSearchSourceBuilder(queryMap))
                .execute()
                .actionGet(TimeValue.timeValueMillis(getQueryTimeout()));
        SearchHits searchHits = response.getHits();

        if (searchHits == null)
            return 0;

        return searchHits.getTotalHits();
    }

    @Override
    public void delete(TypeDescriptor typeDescriptor, String id) throws ClientException {
        checkClient();
        client.prepareDelete()
                .setIndex(typeDescriptor.getIndex())
                .setType(typeDescriptor.getType())
                .setId(id)
                .get(TimeValue.timeValueMillis(getQueryTimeout()));
    }

    @Override
    public void deleteByQuery(TypeDescriptor typeDescriptor, Object query) throws ClientException {
        checkClient();
        ObjectNode queryMap = queryConverter.convertQuery(query);
        TimeValue queryTimeout = TimeValue.timeValueMillis(getQueryTimeout());
        TimeValue scrollTimeout = TimeValue.timeValueMillis(getScrollTimeout());

        // delete by query API is deprecated, scroll with bulk delete must be used
        SearchResponse scrollResponse = this.client.prepareSearch(typeDescriptor.getIndex())
                .setTypes(typeDescriptor.getType())
                .setFetchSource(false)
                .addSort("_doc", SortOrder.ASC)
                .setVersion(true)
                .setScroll(scrollTimeout)
                .setSource(toSearchSourceBuilder(queryMap))
                .setSize(100)
                .get(queryTimeout);

        // Scroll until no hits are returned
        while (true) {

            // Break condition: No hits are returned
            if (scrollResponse.getHits().getHits().length == 0)
                break;

            BulkRequest bulkRequest = new BulkRequest();
            for (SearchHit hit : scrollResponse.getHits().hits()) {
                DeleteRequest delete = new DeleteRequest().index(hit.index())
                        .type(hit.type())
                        .id(hit.id())
                        .version(hit.version());
                bulkRequest.add(delete);
            }

            client.bulk(bulkRequest).actionGet(queryTimeout);

            scrollResponse = this.client.prepareSearchScroll(scrollResponse.getScrollId())
                    .setScroll(scrollTimeout)
                    .execute()
                    .actionGet(queryTimeout);
        }
    }

    @Override
    public IndexExistsResponse isIndexExists(IndexExistsRequest indexExistsRequest) throws ClientException {
        checkClient();
        IndicesExistsResponse response = client.admin().indices()
                .exists(new IndicesExistsRequest(indexExistsRequest.getIndex()))
                .actionGet();
        return new IndexExistsResponse(response.isExists());
    }

    @Override
    public void createIndex(String indexName, ObjectNode indexSettings) throws ClientException {
        checkClient();
        client.admin()
                .indices()
                .prepareCreate(indexName)
                .setSettings(indexSettings.toString())
                .execute()
                .actionGet();
    }

    @Override
    public boolean isMappingExists(TypeDescriptor typeDescriptor) throws ClientException {
        checkClient();
        GetMappingsRequest mappingsRequest = new GetMappingsRequest().indices(typeDescriptor.getIndex());
        GetMappingsResponse mappingsResponse = client.admin().indices().getMappings(mappingsRequest).actionGet();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = mappingsResponse.getMappings();
        ImmutableOpenMap<String, MappingMetaData> map = mappings.get(typeDescriptor.getIndex());
        MappingMetaData metadata = map.get(typeDescriptor.getType());
        return metadata != null;
    }

    @Override
    public void putMapping(TypeDescriptor typeDescriptor, ObjectNode mapping) throws ClientException {
        checkClient();
        // Check message type mapping
        GetMappingsRequest mappingsRequest = new GetMappingsRequest().indices(typeDescriptor.getIndex());
        GetMappingsResponse mappingsResponse = client.admin().indices().getMappings(mappingsRequest).actionGet();
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = mappingsResponse.getMappings();
        ImmutableOpenMap<String, MappingMetaData> map = mappings.get(typeDescriptor.getIndex());
        MappingMetaData metadata = map.get(typeDescriptor.getType());
        if (metadata == null) {
            logger.info(mapping.toString());
            client.admin().indices().preparePutMapping(typeDescriptor.getIndex()).setType(typeDescriptor.getType()).setSource(mapping.toString()).execute().actionGet();
            logger.trace("Mapping {} created! ", typeDescriptor.getType());
        }
        else {
            logger.trace("Mapping {} already exists! ", typeDescriptor.getType());
        }
    }

    @Override
    public String getHashCode(String... components) {
        String concatString = "";
        for (String str : components) {
            concatString = concatString.concat(str);
        }
        byte[] hashCode = Hashing.sha256()
                .hashString(concatString, StandardCharsets.UTF_8)
                .asBytes();

        // ES 5.2 FIX
        // return Base64.encodeBytes(hashCode);

        return Base64.getEncoder().encodeToString(hashCode);
    }

    private String[] toIncludedExcludedFields(JsonNode queryMap) throws ClientException {
        if (queryMap instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) queryMap;
            String[] fields = new String[arrayNode.size()];
            int index = 0;
            for (JsonNode node : arrayNode) {
                fields[index++] = node.asText();
            }
            return fields;
        } else {
            throw new ClientException(ClientErrorCodes.QUERY_MAPPING_EXCEPTION, String.format("Invalid includes/excludes fields type! (%s)", (queryMap != null ? queryMap.getClass() : "null")));
        }
    }

    private SearchSourceBuilder toSearchSourceBuilder(ObjectNode queryMap) throws ClientException {
        SearchSourceBuilder searchSourceBuilder = null;
        try {
            String content = queryMap.toString();
            searchSourceBuilder = new SearchSourceBuilder();
            SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());
            XContentParser parser = XContentFactory.xContent(XContentType.JSON)
                    .createParser(new NamedXContentRegistry(searchModule.getNamedXContents()), content);
            searchSourceBuilder.parseXContent(new QueryParseContext(parser));
            logger.debug(searchSourceBuilder.toString());
            return searchSourceBuilder;
        } catch (Throwable t) {
            throw new ClientException(ClientErrorCodes.ACTION_ERROR, t, "Cannot parse query!");
        }
    }

    private void checkClient() throws ClientUndefinedException {
        if (client == null)
            throw new ClientUndefinedException(CLIENT_UNDEFINED_MSG);
    }

    /**
     * Get the query timeout (default value)
     * 
     * @return
     */
    public long getQueryTimeout() {
        // TODO move to configuration
        return 15000;
    }

    /**
     * Get the scroll timeout (default value)
     * 
     * @return
     */
    public static long getScrollTimeout() {
        return 60000;
    }

}
