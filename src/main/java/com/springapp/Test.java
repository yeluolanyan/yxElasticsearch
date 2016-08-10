package com.springapp;

import com.alibaba.fastjson.JSONArray;
import com.springapp.model.News;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * Created by xinhuan on 2016/1/21.
 * elasticsearch 常用索引的创建、删除、修改、搜索
 */
public class Test {
    public static void main(String[] args) throws Exception {
        createMapping2("newstest2", "news2");
        // addIndexData("newstest","news");
       // createIndex2();
      // deleteIndex();
      //  deleteDoc();
      //  searchExample();
      //  analysis();

    }

    /**
     * 分词测试
     * @throws UnknownHostException
     */
    public static void analysis() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "cluster_youxian")
                .build();
        InetAddress inetAddress = InetAddress.getByName("192.168.1.99");;

        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9300));

        AdminClient ac= client.admin();
        //分词
        AnalyzeResponse analyzeResponse = client.admin().indices().prepareAnalyze("裙子")
                .setAnalyzer("ik_smart").execute().actionGet();
        System.out.println(analyzeResponse.getTokens().size());
        List<AnalyzeResponse.AnalyzeToken> list= analyzeResponse.getTokens();
        for(AnalyzeResponse.AnalyzeToken token : list){
            System.out.println(token.getTerm());
        }
    }

    /**
     * 修改一条记录
     * @throws Exception
     */
    public static void updateIndex() throws Exception {
        InetAddress inetAddress = InetAddress.getByName("192.168.1.119");;
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "cluster_wubing")
                .build();
        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9300));

        //prepareUpdate(索引名称，索引类型，索引id);
        //field(修改字段名称，修改的内容)
        //方法1
        UpdateRequestBuilder update = client.prepareUpdate("news1", "news12", "*").setDoc(
                XContentFactory.jsonBuilder()
                        .startObject()
                        .field("content", "malesa2")
                        .field("title", "malessss2aaa2")
                        .endObject()
        );
        UpdateResponse res = update.execute().actionGet();

        //方法2
        /*UpdateRequest updateRequest = new UpdateRequest("news1", "news1", "AVJ32AJ-1ihUSdBSFnGu")
                .doc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("content", "malessss")
                        .endObject());
        UpdateResponse res = client.update(updateRequest).get();*/
        System.out.println(res.getShardInfo().toString());
    }

    /**
     * 删除一条记录
     * @throws UnknownHostException
     */
    public static void deleteDoc() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "cluster_wubing")
                .build();
        InetAddress inetAddress = InetAddress.getByName("192.168.1.119");;

        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9300));
        DeleteResponse response = client.prepareDelete("news1", "news1", "AVJ32AJ-1ihUSdBSFnGu")
                .execute()
                .actionGet();
        System.out.println(response.getShardInfo().toString());
    }

    /**
     * 删除整个索引文档
     * @throws UnknownHostException
     */
    public static void deleteIndex() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "cluster_wubing")
                .build();
        InetAddress inetAddress = InetAddress.getByName("192.168.1.119");;

        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9300));
        AdminClient ac= client.admin();
        //prepareDelete("索引名称1","索引名称2","索引名称3")
        DeleteIndexRequestBuilder de = ac.indices().prepareDelete("news1", "news2");
        DeleteIndexResponse res = de.execute().actionGet();
        System.out.println(res.getContext().toString());

    }

    public static void searchExample() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "cluster_wubing")
                .build();
        InetAddress inetAddress = InetAddress.getByName("192.168.1.107");;

        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9300));

        SearchRequestBuilder builder = client.prepareSearch("goodsindex")
                .setTypes("goods").setSearchType(SearchType.DEFAULT).setFrom(0).setSize(600);
        BoolQueryBuilder qb = QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("goodsDetail", "衣服").analyzer("ik"))
                .should(QueryBuilders.matchQuery("goodsName", "连衣裙").analyzer("ik"));

        /*MultiMatchQueryBuilder qb = QueryBuilders.multiMatchQuery("裙子", "goodsName").analyzer("ik");*/
        //区间查询
       // RangeQueryBuilder rang = QueryBuilders.rangeQuery("salePrice").from(10.00f).to(20.00f);
        /*BoolQueryBuilder qb = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("salePrice").from(10.00f).to(20.00f))
                .must(QueryBuilders.matchPhraseQuery("qualityFlag", "new10"));*/
        builder.setQuery(qb);
        SearchResponse response = builder.execute().actionGet();
        SearchHits searchHits = response.getHits();
        SearchHit[] searchHits1 = searchHits.hits();
        for (SearchHit searchHitFields : searchHits1) {
            Map<String,Object> map = searchHitFields.getSource();
            System.out.println(JSONArray.toJSONString(map));
        }
    }

    public static void addIndexData(String indexName,String indexType) throws Exception {
        InetAddress inetAddress = InetAddress.getByName("192.168.1.107");;
        Settings settings = Settings.settingsBuilder().put("cluster.name", "cluster_wubing").build();
        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9300));

        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        for (int i = 0; i < 100; i++) {

            News n = new News();
            n.setId(i);
            n.setTitle("hellow吴");
            n.setContent("睡觉就睡觉睡觉睡觉睡觉睡觉睡觉");
            n.setCreateTime("2016-3-09 12:45:12");
            String js = JSONArray.toJSONString(n);
            IndexRequest request = client.prepareIndex(indexName,indexType).setSource(js).request();
            bulkRequestBuilder.add(request);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
        Boolean b = bulkResponse.hasFailures();
        System.out.println(bulkResponse.buildFailureMessage()+"::"+b);
    }

    /**
     *
     * @param indexName
     * @param indexType
     * @throws UnknownHostException
     */
    public static void createMapping2(String indexName,String indexType) throws UnknownHostException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "cluster_wubing")
                .build();
        InetAddress inetAddress = InetAddress.getByName("192.168.1.107");;
        Client esClient = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9300));
        // 创建一个空索引
        esClient.admin().indices().prepareCreate(indexName).execute().actionGet();
        PutMappingResponse response  = esClient.admin().indices().preparePutMapping(indexName)
                .setType(indexType)
                .setSource("{\n" +
                        "  \"properties\": {\n" +
                        "    \"name\": {\n" +
                        "      \"type\": \"string\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}")
                .get();
        if (!response.isAcknowledged()) {
            System.out.println("Could not define mapping for type [" + indexName + "]/[" + indexType + "].");
        } else {
            System.out.println("Mapping definition for [" + indexName + "]/[" + indexType + "] succesfully created.");
        }
    }

    public static void createMapping1(String indexName,String indexType) throws Exception {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "cluster_wubing")
                .build();
        InetAddress inetAddress = InetAddress.getByName("192.168.1.107");;
        Client esClient = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9300));
        //添加mapping
        CreateIndexResponse response = esClient.admin().indices().prepareCreate(indexName)
                .addMapping(indexType,"{\n" +
                        "  \"news\": {\n" +
                        "    \"_all\": {\n" +
                        "      \"enabled\": true,\n" +
                        "      \"analyzer\": \"ik_max_word\",\n" +
                        "      \"search_analyzer\": \"ik_max_word\",\n" +
                        "      \"term_vector\": \"no\",\n" +
                        "      \"store\": \"false\"\n" +
                        "    },\n" +
                        "    \"properties\": {\n" +
                        "      \"id\": {\n" +
                        "        \"type\": \"integer\",\n" +
                        "        \"index\": \"not_analyzed\",\n" +
                        "        \"include_in_all\": false\n" +
                        "      },\n" +
                        "      \"title\": {\n" +
                        "        \"type\": \"string\",\n" +
                        "        \"store\": \"no\",\n" +
                        "        \"term_vector\": \"with_positions_offsets\",\n" +
                        "        \"analyzer\": \"ik_max_word\",\n" +
                        "        \"search_analyzer\": \"ik_max_word\",\n" +
                        "        \"include_in_all\": true\n" +
                        "      },\n" +
                        "      \"content\": {\n" +
                        "        \"type\": \"string\",\n" +
                        "        \"store\": \"no\",\n" +
                        "        \"term_vector\": \"with_positions_offsets\",\n" +
                        "        \"analyzer\": \"ik_max_word\",\n" +
                        "        \"search_analyzer\": \"ik_max_word\",\n" +
                        "        \"include_in_all\": true\n" +
                        "      },\n" +
                        "      \"createTime\": {\n" +
                        "        \"format\": \"yyy-MM-dd HH:mm:ss\",\n" +
                        "        \"type\": \"date\",\n" +
                        "        \"index\": \"not_analyzed\",\n" +
                        "        \"include_in_all\": false\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}").get();

        System.out.println(response.toString());
    }
}
