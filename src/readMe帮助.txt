//根据配置创建jestClient,`JestClient` 设计为单例, 不需要为每个请求都构建它!
JestClientFactory factory = new JestClientFactory();
 factory.setHttpClientConfig(new HttpClientConfig
                        .Builder("http://localhost:9200")
                        .multiThreaded(true)
                        .build());
 JestClient client = factory.getObject();
 
 //用CreateIndex创建索引 articles索引文件名称
 client.execute(new CreateIndex.Builder("articles").build());
 
 //索引设置也可以在创建过程中通过JSON格式创建，
 String settings = "\"settings\" : {\n" +
                "        \"number_of_shards\" : 5,\n" +
                "        \"number_of_replicas\" : 1\n" +
                "    }\n";
client.execute(new CreateIndex.Builder("articles")
			.settings(Settings.builder().loadFromSource(settings).build().getAsMap())
			.build());

//从Elasticsearch中使用` settingsbuilder `帮助类
import org.elasticsearch.common.settings.Settings;
.
.
Settings.Builder settingsBuilder = Settings.settingsBuilder();
settingsBuilder.put("number_of_shards",5);
settingsBuilder.put("number_of_replicas",1);
client.execute(new CreateIndex.Builder("articles").settings(settingsBuilder.build().getAsMap()).build());
//创建索引映射
//通过JEST使用JSON格式的字符串可以轻松的创建索引映射、
PutMapping putMapping = new PutMapping.Builder(
        "my_index",
        "my_type",
        "{ \"document\" : { \"properties\" : { \"message\" : {\"type\" : \"string\", \"store\" : \"yes\"} } } }"
).build();
client.execute(putMapping);
//还可用Elasticsearch的documentmapper类创建映射。
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.elasticsearch.index.mapper.object.RootObjectMapper;
.
.
RootObjectMapper.Builder rootObjectMapperBuilder = new RootObjectMapper.Builder("my_mapping_name").add(
        new StringFieldMapper.Builder("message").store(true)
);
DocumentMapper documentMapper = new DocumentMapper.Builder("my_index", null, rootObjectMapperBuilder).build(null);
String expectedMappingSource = documentMapper.mappingSource().toString();
PutMapping putMapping = new PutMapping.Builder(
        "my_index",
        "my_type",
        expectedMappingSource
).build();
client.execute(putMapping);

//Elasticsearch需要索引数据源为JSON格式，通过Jest创建索引文件
//通过jest我们可以将String,Map,POJOs作为索引的数据源
//1、json字符串方式
String source = "{\"user\":\"kimchy\"}";
//2、通过jsonbuilder Elasticsearch创建JSON；
String source = jsonBuilder()
.startObject()
.field("user", "kimchy")
.field("postDate", "date")
.field("message", "trying out Elastic Search")
.endObject().string();
//3、使用map
Map<String, String> source = new LinkedHashMap<String,String>();
source.put("user", "kimchy");
//4、通过POJO，(Article为pojo)
Article source = new Article();
source.setAuthor("John Ronald Reuel Tolkien");
source.setContent("The Lord of the Rings is an epic high fantasy novel");

//创建名称为twitter类型为tweet的索引
Index index = new Index.Builder(source).index("twitter").type("tweet").build();
client.execute(index);
//索引id可自己设置
Index index = new Index.Builder(source).index("twitter").type("tweet").id("1").build();
client.execute(index);
//@JestId注释 可以被用作标注id
class Article {
@JestId
private String documentId;

}
//Now whenever an instance of Article is indexed, index id will be value of documentId.
//If @JestId value is null, it will be set the value of ElasticSearch generated "_id".
//Jest also supports using JestId annotation on fields with type other than String but the
//catch is then you will need to manually manage the document IDs.

class NumericArticle {

@JestId
private Long documentId;

}
```

//It should be noted that when a non-String type is used for the documentId, conversion
//errors may occur unless you manually manage the documentId. For example if a NumericArticle
//instance without a documentId is indexed then Elasticsearch will assign an automatically
//generated id to that document but Jest will not be able to convert that id to Long since
//all id fields are of type String in Elasticsearch and the auto generated id contains non
//numeric characters.
//So the non-String type support for JestId annotation is purely for ease of use and should
//not be used if you plan to use the automatic id generation functionality of Elasticsearch.

//搜索查询可以是JSON字符串或由Elasticsearch sourcebuilder
//Jest默认Elasticsearch的查询，它让事情更简单。
String query = "{\n" +
            "    \"query\": {\n" +
            "        \"filtered\" : {\n" +
            "            \"query\" : {\n" +
            "                \"query_string\" : {\n" +
            "                    \"query\" : \"test\"\n" +
            "                }\n" +
            "            },\n" +
            "            \"filter\" : {\n" +
            "                \"term\" : { \"user\" : \"kimchy\" }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";
Search search = new Search.Builder(query)
                // multiple index or types can be added.
                .addIndex("twitter")
                .addIndex("tweet")
                .build();
SearchResult result = client.execute(search);
//By template 模板;
String query = "{\n" +
            "    \"id\": \"myTemplateId\"," +
            "    \"params\": {\n" +
            "        \"query_string\" : \"search for this\"" +
            "    }\n" +
            "}";
Search search = new Search.TemplateBuilder(query)
                // multiple index or types can be added.
                .addIndex("twitter")
                .addIndex("tweet")
                .build();
SearchResult result = client.execute(search);
//还支持内联搜索模板和基于文件的模板。用SearchSourceBuilder类；
SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
searchSourceBuilder.query(QueryBuilders.matchQuery("user", "kimchy"));
Search search = new Search.Builder(searchSourceBuilder.toString())
                                // multiple index or types can be added.
                                .addIndex("twitter")
                                .addIndex("tweet")
                                .build();

SearchResult result = client.execute(search);

//将结果转换对象列表；
SearchResult result = client.execute(search);
List<SearchResult.Hit<Article, Void>> hits = searchResult.getHits(Article.class);
//或者
List<Article> articles = result.getSourceAsObjectList(Article.class);
//请参阅[查询] Elasticsearch DSL（http://www.elasticsearch.org/guide/reference/query-dsl/）文件和复杂的查询工作。

//获得单个索引查询文件
Get get = new Get.Builder("twitter", "1").type("tweet").build();
JestResult result = client.execute(get);

//将结果转为对象
Get get = new Get.Builder("twitter", "1").type("tweet").build();
JestResult result = client.execute(get);
Article article = result.getSourceAsObject(Article.class);

//修改索引文件
String script = "{\n" +
                "    \"script\" : \"ctx._source.tags += tag\",\n" +
                "    \"params\" : {\n" +
                "        \"tag\" : \"blue\"\n" +
                "    }\n" +
                "}";

client.execute(new Update.Builder(script).index("twitter").type("tweet").id("1").build());
//删除索引文件
client.execute(new Delete.Builder("1")
                .index("twitter")
                .type("tweet")
                .build());
				
//bluk使用
//Elasticsearch的bluk API可以一次执行多个索引创建/删除操作。这可以大大提高索引速度。
String article1 = "tweet1";
String article2 = "tweet2";

Bulk bulk = new Bulk.Builder()
                .defaultIndex("twitter")
                .defaultType("tweet")
                .addAction(Arrays.asList(
                    new Index.Builder(article1).build(),
                    new Index.Builder(article2).build()))
                .build();
client.execute(bulk);

//参数使用
//Elasticsearch提供请求参数设置属性，如路由、版本控制、操作等类型。
//例如，刷新参数可以被设置为真的，同时索引下面的文档：
Index index = new Index.Builder("{\"user\":\"kimchy\"}")
    .index("cvbank")
    .type("candidate")
    .id("1")
    .setParameter(Parameters.REFRESH, true),
    .build();
client.execute(index);
//任何通过网址传递的请求参数都可以被设置。常用的参数，
//在enumarated `Parameters`参数类，

//异步执行
//Jest的HTTP客户端支持执行任何异步非阻塞IO Action。
//下面的例子说明了如何用JEST异步调用执行一个Action。
client.executeAsync(action,new JestResultHandler<JestResult>() {
    @Override
    public void completed(JestResult result) {
        ... do process result ....
    }
    @Override
    public void failed(Exception ex) {
       ... catch exception ...
    }
});

### Node Discovery through Nodes API

//Enabling node discovery will (poll) and update the list of servers in the client periodically.
//Configuration of the discovery process can be done in the client config as follows:

//enable host discovery
ClientConfig clientConfig = new ClientConfig.Builder("http://localhost:9200")
    .discoveryEnabled(true)
    .discoveryFrequency(1l, TimeUnit.MINUTES)
    .build();

# # #认证
//在构建客户端时，可以配置基本的用户名和密码验证，应该注意
//这些证书将用于所有服务器提供和发现。
JestClientFactory factory = new JestClientFactory();
factory.setHttpClientConfig(
    new HttpClientConfig.Builder("http://localhost:9200")
        .defaultCredentials("global_user", "global_password")
        .build()
);
//如果你的认证需求比以上更复杂的（例如：不同学历不同服务器、Kerberos等）
//然后你还可以提供一个`CredentialsProvider`实例。
BasicCredentialsProvider customCredentialsProvider = new BasicCredentialsProvider();
customCredentialsProvider.setCredentials(
        new AuthScope("192.168.0.88", 9200),
        new UsernamePasswordCredentials("eu_user", "123")
);
customCredentialsProvider.setCredentials(
        new AuthScope("192.168.0.172", 9200),
        new UsernamePasswordCredentials("us_user", "456")
);

JestClientFactory factory = new JestClientFactory();
factory.setHttpClientConfig(
    new HttpClientConfig.Builder(Arrays.asList("http://192.168.0.88:9200", "http://192.168.0.172:9200"))
        .credentialsProvider(customCredentialsProvider)
        .build()
);
# # # HTTPS / SSL
//HTTPS和SSL（或TLS）的连接可以通过你自己的`LayeredConnectionSocketFactory`生成器实例配置。

//跳过主机名检查
HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;

SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
SchemeIOSessionStrategy httpsIOSessionStrategy = SSLIOSessionStrategy(sslContext, hostnameVerifier);

JestClientFactory factory = new JestClientFactory();
factory.setHttpClientConfig(new HttpClientConfig.Builder("https://localhost:9200")
                .sslSocketFactory(sslSocketFactory) // this only affects sync calls
                .httpsIOSessionStrategy(httpsIOSessionStrategy) // this only affects async calls
                .build()
);
//记住（`SSLContext`和`hostnameverifier`信息）上面的例子只是举例，它是非常不安全的。

# # #代理
//任何系统的代理设置都将被默认使用，所以如果在系统级别上设置代理
//（例如：通过操作系统或环境变量）
//你不需要在Jest做任何进一步的配置。
//配置代理设置专门的JEST也可以通过生成器做的。
String proxyHost = "proxy.company.com";
int proxyPort = 7788;
JestClientFactory factory = new JestClientFactory();
factory.setHttpClientConfig(
    new HttpClientConfig.Builder("http://remote.server.com:9200")
        .proxy(new HttpHost(poxyHost, proxyPort))
        .build()
);
