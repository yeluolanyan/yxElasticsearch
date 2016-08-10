package com.springapp.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.springapp.model.News;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Created by xinhuan on 2016/1/18.
 */
@Service
public class JestService {

    @Autowired
    private JestClient jestClient;
    /**
     * 创建索引
     */
    public void builderSearchIndex(){
        /*try {
            jestClient.execute(new CreateIndex.Builder("news").build());
            News ne = new News();
            ne.setContent("");
            ne.setTitle("");

            Index index = new Index.Builder(ne).index("news").type("new").build();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        try {
            // 如果索引存在,删除索引
            //DeleteIndexRequest d = new DeleteIndexRequest("news");
            DeleteIndex deleteIndex = new DeleteIndex.Builder("news").build();
            JestResult delRes = jestClient.execute(deleteIndex);
            // 创建索引
            CreateIndex createIndex = new CreateIndex.Builder("news").build();
            jestClient.execute(createIndex);
            //index 批量添加建议使用bulk，效率高(减少通讯次数)：
            Bulk.Builder bulk = new Bulk.Builder();
            // 添加添加100万条假数据去服务端(ES)
            for (int i = 0; i < 1000; i++) {
                News news = new News();
                news.setId(i + 1);
                news.setTitle("elasticsearch" + (i + 2));
                if(i%2 == 0){
                    news.setContent("oyhk集成的例子也是用maven了如果不有熟悉maven的朋友们,可以跟我交流下,大家的女士连衣裙"
                            + (i + 3));
                }else {
                    news.setContent("oyhk集成我是中国人,士兵"+ (i + 3));
                }

                //两个参数1:索引名称2:类型名称(用文章(article)做类型名称)
                Index index = new Index.Builder(news).index("news").type("article").build();
                bulk.addAction(index);
            }
            jestClient.execute(bulk.build());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 搜索
     * @param param
     * @return
     */
    public List<News> searchNews(String param){
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        QueryStringQueryBuilder queryBuilder = new QueryStringQueryBuilder("士兵武器");
        queryBuilder.analyzer("ik").field("content");
        searchSourceBuilder.query(queryBuilder);

        //matchPhraseQuery不进行分词搜索，matchQuery分词搜索，
        //searchSourceBuilder.query(QueryBuilders.matchQuery("content", "女士").analyzer("ik"));
        /*
        * 1、FuzzyQuery是一种模糊查询，它可以简单地识别两个相近的词语。 即相似度匹配
        * 2、RangeQuery范围查询(数字、字符串等)
        * 3、PrefixQuery前缀搜索
        * 4、使用通配符查询WildcardQuery，*代表0个或多个字母，?代表0个或1个字母。
        * QueryBuilders.wildcardQuery("contents","?ild*");
        * 5、正则查询RegexpQuery跟通配符查询（WildcardQuery）的功能很相似，因为他们都可以完成一样的工作，但是不同的是正则查询支持更灵活定制细化查询，
        * */

        /*
        * SpanQuery按照词在文章中的距离或者查询几个相邻词的查询
        SpanQuery包括以下几种：
        1、SpanTermQuery：词距查询的基础，结果和TermQuery相似，只不过是增加了查询结果中单词的距离信息。
        2、SpanFirstQuery：在指定距离可以找到第一个单词的查询。
        3、 SpanNearQuery：查询的几个语句之间保持者一定的距离。
        4、SpanOrQuery：同时查询几个词句查询。
        5、SpanNotQuery：从一个词距查询结果中，去除一个词距查询
        * */


       /* 这种模糊搜索的方法是根据用户输入的单个字进行字符串间的查找，
        * 这种算法被称为levenshtein算法。
        * 这种算法在比较两个字符串时会会将动作分为三种，
        * 加上一个字母，删一个字母，改变一个字母。两个字符串之间进行比较时
        * 就是在执行将其中一个字符串，转变为另一个字符串的操作，
        * 每执行一次上述的操作，则相应的就会扣除一定的分数。
        * QueryBuilders.fuzzyQuery("","");
        * */

        /*queryStringQuery 匹配所有字段信息查询
        searchSourceBuilder.query(QueryBuilders.queryStringQuery(param));*/

        /*multiMatchQuery(text, "name", "sex")针对的是多个field，也就是说，当multiMatchQuery中，fieldNames参数只有一个时，
         其作用与matchQuery相当；而当fieldNames有多个参数时，如field1和field2，
         那查询的结果中，要么field1中包含text，要么field2中包含text;*/
        //searchSourceBuilder.query(QueryBuilders.multiMatchQuery(param,"content","title"));

        //content为field,test为查询内容.query and 条件组合查询
        //其中must表示必须满足,mustNot表示必须不满足,should表示可有可无
        /*QueryBuilder qb = QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("content", "test1"))
                        .must(QueryBuilders.termQuery("content", "test4"))
                        .mustNot(QueryBuilders.termQuery("content", "test2"))
                        .should(QueryBuilders.termQuery("content", "test3"));
        searchSourceBuilder.query(qb);*/

        /**
         * boostingQuery查询可以有效地将结果匹配一个给定的查询。
         * 不像在boolQuery查询的“mustNot”条件，
         * boostingQuery选择positive含有“test1”的文件，但会提高他们的总体评分 boost 提高评分比重
         * boostingQuery选择negative含有“test2”的文件，但会降低他们的总体评分 negativeBoost 降低评分比重。
         */
        /*QueryBuilders.boostingQuery()
                .positive(QueryBuilders.termQuery("content", "test1"))
                .boost(0.2f)
                .negative(QueryBuilders.termQuery("content", "test2"))
                .negativeBoost(0.3f);*/

        /*构造一个只会匹配的特定数据 id 的查询*/
        //QueryBuilders.idsQuery().ids("1", "2");

        /*一个生成的子查询文件产生的联合查询，
        * 而且每个分数的文件具有最高得分文件的任何子查询产生的，
        * 再加上打破平手的增加任何额外的匹配的子查询。*/
        /*QueryBuilders.disMaxQuery()
                .add(QueryBuilders.termQuery("content", "kimchy"))
                .add(QueryBuilders.termQuery("content", "elasticsearch"))
                .boost(1.2f)
                .tieBreaker(0.7f);*/

        /*查询相匹配的文档在一个范围。*/
        /*QueryBuilders.rangeQuery("name")
                    .from("葫芦1000娃").to("葫芦3000娃")
                    .includeLower(true)     //包括下界
                    .includeUpper(false);*/ //不包括上界
        /*QueryBuilders.rangeQuery("age").gt(10).lt(20)*/

        //from(0)从第0条开始，size(100)查询100条,默认只查询前十条
        searchSourceBuilder.from(0).size(100);
        /*Analysis.parseStopWords()
        AnalysisService analysisService = new AnalysisService(Analysis.parseArticles());*/
        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex("news").addType("article").build();

        try {
            JestResult result = jestClient.execute(search);
            JsonObject jb = result.getJsonObject();
            JsonObject hits = jb.getAsJsonObject("hits");
            //获得查询结果总数目
            JsonElement total = hits.get("total");
            System.out.println("查询结果共"+total+"条");
            //将查询结果转为pojo List
            List<News> list = result.getSourceAsObjectList(News.class);
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
