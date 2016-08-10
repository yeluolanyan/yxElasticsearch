package com.springapp.util;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by xinhuan on 2016/1/23.
 */

@Configuration
public class ElasticSerachUtil {

    public @Bean static Client getClient() throws UnknownHostException {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "cluster_wubing")
                .build();
        //InetAddress inetAddress1 = InetAddress.getByName("192.168.1.119");
        InetAddress inetAddress2 = InetAddress.getByName("192.168.1.107");

        Client client = TransportClient.builder().settings(settings).build()
                //.addTransportAddress(new InetSocketTransportAddress(inetAddress2, 9300))
                .addTransportAddress(new InetSocketTransportAddress(inetAddress2, 9300));
        return client;
    }


    public @Bean  AdminClient getAdminClient() throws UnknownHostException {
        return getClient().admin();
    }

    public static void main(String[] args) throws UnknownHostException {
       Client client = getClient();
        System.out.println(client);

        /*AnalyzeResponse analyzeResponse = client.admin().indices().prepareAnalyze("李克强说，中巴是全天候战略合作伙伴。我今年5月访巴时，总理先生作为候任总理热情参与接待")
                .setAnalyzer("ik_smart").execute().actionGet();
        System.out.println(analyzeResponse.getTokens().size());
        List<AnalyzeResponse.AnalyzeToken> list= analyzeResponse.getTokens();
        for(AnalyzeResponse.AnalyzeToken token : list){
            System.out.println(token.getTerm());
        }*/

        /*BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        for (int i = 0; i < 100; i++) {

            News n = new News();
            n.setId(i);
            n.setTitle("hellow吴");
            n.setContent("睡觉就睡觉睡觉睡觉睡觉睡觉睡觉");
            String js = JSONArray.toJSONString(n);
            //client.prepareIndex("news1","news1").setSo
            IndexRequest request = client.prepareIndex("news2","news2").setSource(js).request();
            bulkRequestBuilder.add(request);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
        Boolean b = bulkResponse.hasFailures();
        System.out.println(bulkResponse.buildFailureMessage()+"::"+b);*/


        SearchRequestBuilder builder = client.prepareSearch("categoryindex")
                .setTypes("goodscategory").setSearchType(SearchType.DEFAULT).setFrom(0).setSize(600);
        BoolQueryBuilder qb = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("name","喜欢照相机啊").analyzer("mmseg"))
                .should(QueryBuilders.matchQuery("description","喜欢照相机啊").analyzer("ik"));

        builder.setQuery(qb);
        SearchResponse response = builder.execute().actionGet();
        System.out.println("  " + response);
        System.out.println(response.getHits().getTotalHits());

    }
}
