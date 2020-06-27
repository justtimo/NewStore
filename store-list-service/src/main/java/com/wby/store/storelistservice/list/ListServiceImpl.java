package com.wby.store.storelistservice.list;

import com.alibaba.dubbo.config.annotation.Service;
import com.wby.store.bean.SkuInfo;
import com.wby.store.bean.SkuLsInfo;
import com.wby.store.bean.SkuLsParams;
import com.wby.store.bean.SkuLsResult;
import com.wby.store.service.ListService;
import com.wby.store.util.RedisUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListServiceImpl implements ListService {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    JestClient jestClient;
    //测试整合。

    /**
     * 哪里用到这个方法？上架商品。比如定时发售，先保存商品，到时间了再上架
     * @param skuLsInfo
     */
    public void  saveSkuListInfo(SkuLsInfo skuLsInfo){
        /**PUT /wby_sku_info/_doc/1
         {
         "id":"1",
         "price":2222.00,
         "skuName":"华为手机真好用",
         "catalog3Id":"89",
         "skuDefaultImg":"http://123.com",
         "skuAttrValueList":
         [
         {"valueId":"101"},
         {"valueId":"101"}
         ]
         }
         * public Builder(Object source):source就是要构造的对象，这里是skuLsInfo
         * 会自动将skuLsInfo转换为json串。也就是14-25行的东西
         * 除此之外，还有13行的东西也需要BUilder， PUT  index/type/id
         */

        Index.Builder indexBuilder=new Index.Builder(skuLsInfo);
        /**
         * 怎么区分PUT还是POST？
         * PUT /wby_sku_info/_doc/1
         * POST /wby_sku_info/_doc/
         * indexBuilder.index("wby11_sku_info").type("_doc").id(skuLsInfo.getId());
         * 看有没有.id()方法，没有就是POST，会有个随机的ID自动生成
         */
        indexBuilder.index("wby11_sku_info").type("doc").id(skuLsInfo.getId());
        Index index = indexBuilder.build();
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SkuLsResult getSkuLsInfoList(SkuLsParams skuLsParams) {
        /*String queary="{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\n" +
                "          \"match\": {\n" +
                "            \"skuName\": \"小米黑\"\n" +
                "          }\n" +
                "        }\n" +
                "      ],\n" +
                "      \"filter\": [\n" +
                "        {\"term\": {\n" +
                "          \"catalog3Id\": \"89\"}\n" +
                "        },\n" +
                "        {\"term\": {\n" +
                "          \"skuAttrValueList.valueId\": \"101\"}\n" +
                "        },\n" +
                "        {\"term\": {\n" +
                "          \"skuAttrValueList.valueId\": \"102\"}\n" +
                "        },\n" +
                "        {\"range\": {\n" +
                "          \"price\": {\n" +
                "            \"gte\": 2000,\n" +
                "            \"lte\": 3000\n" +
                "          }\n" +
                "        }}\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "  , \"from\": 0\n" +
                "  , \"size\": 2\n" +
                "  , \"highlight\": {\"fields\": {\"skuName\": {\n" +
                "        \"pre_tags\": \n" +
                "          \"<span style='color:red'>\"\n" +
                "        ,\n" +
                "        \"post_tags\": \n" +
                "          \"</span>\"\n" +
                "        \n" +
                "      }\n" +
                "    \n" +
                "    }\n" +
                "    \n" +
                "  }\n" +
                "  , \"aggs\": {\n" +
                "    \"groupby_valueId\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"skuAttrValueList.valueId\",\n" +
                "        \"size\": 1\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "  , \"sort\": [\n" +
                "    {\n" +
                "      \"hotScore\": {\n" +
                "        \"order\": \"desc\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";*/
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //商品名称查询搜索
        if (skuLsParams.getKeyword()!=null){
            boolQueryBuilder.must(new MatchQueryBuilder("skuName",skuLsParams.getKeyword()));
            //高亮
            searchSourceBuilder.highlight(new HighlightBuilder().field("skuName").preTags("<span style='color:red'>")
                    .postTags("</span>"));
        }

        //三级分类过滤
        //new TermsQueryBuilder()，这个方法是里面的参数满足任意一个就行，而tearm则都是妖满足
        if (skuLsParams.getCatalog3Id()!=null){
            boolQueryBuilder.filter(
                    new TermQueryBuilder("catalog3Id",skuLsParams.getCatalog3Id())
            );
        }


        //平台属性过滤
        if (skuLsParams.getValueId()!=null&&skuLsParams.getValueId().length>0){
            String[] valueIds = skuLsParams.getValueId();
            for (int i = 0; i < valueIds.length; i++) {
                String s = valueIds[i];
                boolQueryBuilder.filter(
                        new TermQueryBuilder("skuAttrValueList.valueId",s)
                );

            }
        }

        //价格
        //boolQueryBuilder.filter(new RangeQueryBuilder("price").gte(2000));

        //起始行。页码换算成行，（页码-1）*size
        searchSourceBuilder.from((skuLsParams.getPageNo()-1)*skuLsParams.getPageSize());
        //每页多少行
        searchSourceBuilder.size(skuLsParams.getPageSize());
        /*//高亮
        searchSourceBuilder.highlight(new HighlightBuilder().field("skuName").preTags("<span style='color:red'>")
        .postTags("</span>"));*/

        //聚合
        /*TermsBuilder aggsBuilder =
                AggregationBuilders.terms("groupby_value_id")
                        .field("skuAttrValueList.valueId")
                        .size(1000);
        searchSourceBuilder.aggregation(aggsBuilder);*/
        //排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        System.out.println(searchSourceBuilder.toString());


        Search.Builder search=new Search.Builder(searchSourceBuilder.toString());
        Search build = search.addIndex("wby11_sku_info").addType("doc").build();

        SkuLsResult skuLsResult = new SkuLsResult();
        try {
            SearchResult searchResult = jestClient.execute(build);


            //商品信息列表
            if (null!=searchResult.getHits(SkuLsInfo.class)){
                List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
                List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
                for (SearchResult.Hit<SkuLsInfo, Void> hit:hits
                ) {
                    SkuLsInfo skuLsInfo = hit.source;
                    String skuNameHL = hit.highlight.get("skuName").get(0);
                    skuLsInfo.setSkuName(skuNameHL);
                    skuLsInfoList.add(skuLsInfo);
                }
                skuLsResult.setSkuLsInfoList(skuLsInfoList);
            }


            //总数
            Long total = searchResult.getTotal();
            skuLsResult.setTotal(total);
            //总页数
            long totalPage= (total + skuLsParams.getPageSize() -1) / skuLsParams.getPageSize();
            skuLsResult.setTotalPages(totalPage);


            //聚合部分  商品涉及的平台属性值
            /*List<String> attrValueList = new ArrayList<>();
            List<TermsAggregation.Entry> buckets =
                    searchResult.getAggregations().
                            getTermsAggregation("groupby_value_id").getBuckets();
            for (TermsAggregation.Entry bucket:buckets
                 ) {
                String key = bucket.getKey();
                attrValueList.add(key);

            }
            skuLsResult.setAttrValueIdList(attrValueList);*/


        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        //每次只需要在redis中做+1
        //设计key。type（string）、key（sku：101：hotscore）、value（sku)
        String hotScoreKey="sku:"+skuId+"hotScore";
        Long hotScore = jedis.incr(hotScoreKey);
        //计数可以被10整除，更新es
        if (hotScore%10==0){
            updateHotScoreEs(skuId,hotScore);
        }

    }

    //跟新ES
    public void updateHotScoreEs(String skuId,Long hotScore){
        String updateJson="{\n" +
                "   \"doc\":{\n" +
                "     \"hotScore\":"+hotScore+"\n" +
                "   }\n" +
                "}";

        Update build = new Update.Builder(updateJson)
                .index("wby11_sku_info").type("_doc").id(skuId)
                .build();
        try {
            jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
