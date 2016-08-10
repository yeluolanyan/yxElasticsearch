package com.springapp;

import com.springapp.util.ElasticSerachUtil;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.geoDistanceRangeQuery;

/**
 * Created by xinhuan on 2016/3/11.
 * 经纬度 距离搜索demo
 *
 */
public class LocationSearchTest {

    // 创建索引
    public static void createIndex2(String indexName, String indexType) throws IOException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "cluster_wubing")
                .build();
        InetAddress inetAddress = InetAddress.getByName("192.168.1.107");;

        Client esClient = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9300));
        //创建mapping,我这里使用的分词器analyzer/search_analyzer为ik,注意位置location类型为geo_point
        String mapping = "{\"properties\": {\n" +
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
                "      \"city\": {\n" +
                "        \"type\": \"string\",\n" +
                "        \"store\": \"no\",\n" +
                "        \"term_vector\": \"with_positions_offsets\",\n" +
                "        \"analyzer\": \"ik_max_word\",\n" +
                "        \"search_analyzer\": \"ik_max_word\",\n" +
                "        \"include_in_all\": true\n" +
                "      },\n" +
                "      \"location\":{\n" +
                "        \"type\":\"geo_point\",\n" +
                "        \"index\": \"not_analyzed\",\n" +
                "        \"include_in_all\": false\n" +
                "      }\n" +
                "    }}";
        esClient.admin().indices().prepareCreate(indexName).execute().actionGet();
        PutMappingResponse response  = esClient.admin().indices().preparePutMapping(indexName)
                .setType(indexType)
                .setSource(mapping)
                .get();

        if (!response.isAcknowledged()) {
            System.out.println("Could not define mapping for type [" + indexName + "]/[" + indexType + "].");
        } else {
            System.out.println("Mapping definition for [" + indexName + "]/[" + indexType + "] succesfully created.");
        }
    }
    // 创建索引
    public static void createIndex(String indexName, String indexType) throws IOException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "cluster_wubing")
                .build();
        InetAddress inetAddress = InetAddress.getByName("192.168.1.107");;

        Client esClient = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9300));
        // 创建Mapping
        XContentBuilder mapping = createMapping(indexType);
        System.out.println("mapping:" + mapping.string());
        // 创建一个空索引
        esClient.admin().indices().prepareCreate(indexName).execute().actionGet();
        PutMappingRequest putMapping = Requests.putMappingRequest(indexName).type(indexType).source(mapping);
        PutMappingResponse response = esClient.admin().indices().putMapping(putMapping).actionGet();
        if (!response.isAcknowledged()) {
            System.out.println("Could not define mapping for type [" + indexName + "]/[" + indexType + "].");
        } else {
            System.out.println("Mapping definition for [" + indexName + "]/[" + indexType + "] succesfully created.");
        }
    }

    // 创建mapping
    public static XContentBuilder createMapping(String indexType) {
        XContentBuilder mapping = null;
        try {
            mapping = jsonBuilder().startObject()
                    // 索引库名（类似数据库中的表）
                    .startObject(indexType).startObject("properties")
                            // ID
                    .startObject("id").field("type", "long").endObject()
                            // 城市
                    .startObject("city").field("type", "string").endObject()
                            // 位置
                    .startObject("location").field("type", "geo_point").endObject()
                            // 标题
                    .startObject("title").field("type", "string").endObject()

                    .endObject().endObject().endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapping;
    }

    // 添加数据
    public static Integer addIndexData(String indexName, String indexType) throws Exception {
        Client client = ElasticSerachUtil.getClient();
        List<String> cityList = new ArrayList<String>();

        City city1 = new City(1L, "北京", 116.395645, 39.929986, "中国人民站起来了，北京人民可以天天站在天安门广场吃烤鸭了");
        City city2 = new City(2L, "天津", 117.210813, 39.143931, "中国人民站起来了，天津人民可以天天在迎宾广场吃麻花了");
        City city3 = new City(3L, "青岛", 120.384428, 36.105215, "中国人民站起来了，青岛人民可以天天在！最后一次，不要错过今天");
        City city4 = new City(4L, "哈尔滨", 126.657717, 45.773225, "中国人民站起来了，哈尔滨人民可以天天站在索菲亚广场吃红肠了");
        City city5 = new City(5L, "乌鲁木齐", 87.564988, 43.840381, "中国人民站起来了，乌鲁木齐人民可以天天在人民广场啃羊腿了");
        City city6 = new City(6L, "三亚", 109.522771, 18.257776, "中国人民站起来了，三亚人民可以让青岛政府去丢吧，让他们创城去吧！");

        cityList.add(obj2JsonUserData(city1));
        cityList.add(obj2JsonUserData(city2));
        cityList.add(obj2JsonUserData(city3));
        cityList.add(obj2JsonUserData(city4));
        cityList.add(obj2JsonUserData(city5));
        cityList.add(obj2JsonUserData(city6));

        // 创建索引库
        List<IndexRequest> requests = new ArrayList<IndexRequest>();
        for (int i = 0; i < cityList.size(); i++) {
            IndexRequest request = client.prepareIndex(indexName, indexType).setSource(cityList.get(i)).request();
            requests.add(request);
        }

        // 批量创建索引
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (IndexRequest request : requests) {
            bulkRequest.add(request);
        }

        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if (bulkResponse.hasFailures()) {
            System.out.println("批量创建索引错误！");
        }
        return bulkRequest.numberOfActions();
    }

    public static String obj2JsonUserData(City city) {
        String jsonData = null;
        try {
            // 使用XContentBuilder创建json数据
            XContentBuilder jsonBuild = XContentFactory.jsonBuilder();
            jsonBuild.startObject().field("id", city.getId())
                    .field("city", city.getCity())
                    //注意纬度在前，经度在后 location类型
                    .startArray("location").value(city.getLat()).value(city.getLon()).endArray()
                    .field("title", city.getTitle())
                    .endObject();
            jsonData = jsonBuild.string();
            System.out.println(jsonData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonData;
    }

    // 模糊查询
    public static void query(String query) throws Exception {
        Client client = ElasticSerachUtil.getClient();
        QueryStringQueryBuilder qsqb = new QueryStringQueryBuilder(query);
        // qsqb.analyzer("ik").field("title");
        qsqb.field("title");
        client.admin().indices().prepareRefresh().execute().actionGet();

        SearchResponse searchResponse = client.prepareSearch("testes").setTypes("xq").setQuery(qsqb)
                // .setScroll(new TimeValue(60000))
                .addFields("id", "title", "updatetime")
                        // .addSort("updatetime", SortOrder.DESC)
                .addSort("_score", SortOrder.DESC)
                        // .addHighlightedField("title")
                .setHighlighterEncoder("UTF-8").execute().actionGet();
        // 搜索耗时
        Float usetime = searchResponse.getTookInMillis() / 1000f;
        // 命中记录数
        Long hits = searchResponse.getHits().totalHits();
        System.out.println("查询到记录数=" + hits);

        for (SearchHit hit : searchResponse.getHits()) {
            // 打分
            Float score = hit.getScore();
            Integer id = Integer.parseInt(hit.getFields().get("id").value().toString());
            String title = hit.getFields().get("title").value().toString();
            System.out.println(title);
        }
    }

    // 获取附近的城市
    public static void testGetNearbyCities(Client client, String index, String type, double lat, double lon) throws ExecutionException, InterruptedException {
        SearchRequestBuilder srb = client.prepareSearch(index).setTypes(type);
       // srb.setPos
        // wx4g0th9p0gk 为北京的geohash 范围为lt(小于) 1500km内的数据
        QueryBuilder builder = QueryBuilders.geoDistanceRangeQuery("location")
                .point(lat,lon)//注意纬度在前，经度在后
                .from("0km")
                .to("10000km")
                .includeLower(true)
                .includeUpper(false)
                .optimizeBbox("memory")
                .geoDistance(GeoDistance.ARC);

        srb.setQuery(builder);
        // 获取距离多少公里 这个才是获取点与点之间的距离的
        //GeoDistanceSortBuilder sort = SortBuilders.geoDistanceSort("location");
        GeoDistanceSortBuilder sort = new GeoDistanceSortBuilder("location");
        sort.unit(DistanceUnit.KILOMETERS);//距离单位公里
        sort.order(SortOrder.ASC);
        sort.point(lat,lon);//注意纬度在前，经度在后

        srb.addSort(sort);
        SearchResponse searchResponse = srb.execute().actionGet();

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHists = hits.getHits();
        System.out.println("北京附近的城市(" + hits.getTotalHits() + "个)：");
        for (SearchHit hit : searchHists) {
            String city = (String) hit.getSource().get("city");
            String title = (String) hit.getSource().get("title");
            // 获取距离值，并保留两位小数点
            BigDecimal geoDis = new BigDecimal((Double) hit.getSortValues()[0]);
            Map<String, Object> hitMap = hit.getSource();
            // 在创建MAPPING的时候，属性名的不可为geoDistance。
            hitMap.put("geoDistance", geoDis.setScale(2, BigDecimal.ROUND_HALF_DOWN));
            System.out.println(city + "距离北京" + hit.getSource().get("geoDistance") + DistanceUnit.KILOMETERS.toString() + "---" + title);
        }

    }

    public static void main(String[] args) throws Exception {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "cluster_wubing")
                .build();
        InetAddress inetAddress = InetAddress.getByName("192.168.1.107");;

        Client client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9300));
        String index = "testes2";
        String type = "xq2";
        //createIndex2(index, type);
        //addIndexData(index, type);

        double lat = 39.929986;
        double lon = 116.395645;
        long start = System.currentTimeMillis();
        testGetNearbyCities(client, index, type, lat, lon);
        query("政府");
        long end = System.currentTimeMillis();
        System.out.println((end - start) + "毫秒");
        client.close();
    }
}
