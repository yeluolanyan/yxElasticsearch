package com.springapp.service;

import com.springapp.model.News;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xinhuan on 2016/1/23.
 */
@Service
public class ElasticService {

    @Autowired
    private Client client;

    public void createBlukIndex(){
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        for (int i = 0; i < 100; i++) {

            News n = new News();
            n.setId(i);
            n.setTitle("hellow吴");
            n.setContent("睡觉就睡觉睡觉睡觉睡觉睡觉睡觉");

            IndexRequest request = client.prepareIndex("news","news").setSource(n).request();
            bulkRequestBuilder.add(request);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
    }
}
