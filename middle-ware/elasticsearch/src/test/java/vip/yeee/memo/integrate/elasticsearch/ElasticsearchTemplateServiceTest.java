package vip.yeee.memo.integrate.elasticsearch;

import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import vip.yeee.memo.integrate.elasticsearch.mapping.BaseIndex;
import vip.yeee.memo.integrate.elasticsearch.opr.ITProjectService;
import vip.yeee.memo.integrate.elasticsearch.opr.TProject;
import vip.yeee.memo.integrate.elasticsearch.mapping.TProjectIndex;
import vip.yeee.memo.integrate.elasticsearch.service.ElasticsearchTemplate;
import vip.yeee.memo.integrate.elasticsearch.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * description......
 *
 * @author yeeee
 * @since 2022/4/25 22:41
 */
@Slf4j
@SpringBootTest(classes = ElasticsearchApplication.class)
public class ElasticsearchTemplateServiceTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private ITProjectService iTProjectService;

    @Test
    public void testCreateIndex() throws Exception {
        boolean exists = elasticsearchTemplate.exists(TProjectIndex.class);
        log.info("-----------------exists = {}---------------------", exists);
        if (!exists) {
            boolean create = elasticsearchTemplate.createIndex(TProjectIndex.class);
            log.info("-----------------create = {}---------------------", create);
        }
    }

    @Test
    public void testBulk() throws IOException {
        List<TProject> list = iTProjectService.list();
        List<TProjectIndex> myIndexList = list.stream()
                .map(item -> {
                    TProjectIndex projectIndex = new TProjectIndex();
                    BeanUtils.copyProperties(item, projectIndex);
                    projectIndex.setId(item.getId());
                    projectIndex.setCreateTime(item.getLaunchDateRaising());
                    return projectIndex;
                })
                .collect(Collectors.toList());
        List<IndexedObjectInformation> res = elasticsearchTemplate.bulk(myIndexList, "cf_project_2");
        log.info("-------------bulk res = {}------------------", res);
    }

    @Test
    public void testSaveBatch() throws IOException {
        List<TProject> list = iTProjectService.list();
        List<TProjectIndex> myIndexList = list.stream()
                .map(item -> {
                    TProjectIndex projectIndex = new TProjectIndex();
                    BeanUtils.copyProperties(item, projectIndex);
                    projectIndex.setId(item.getId());
                    projectIndex.setCreateTime(item.getLaunchDateRaising());
                    return projectIndex;
                })
                .collect(Collectors.toList());
        Iterable<? extends BaseIndex> res = elasticsearchTemplate.saveBatch(myIndexList);
        log.info("-------------bulk res = {}------------------", res);
    }

    /**
     * //??????????????????????????????termQuery("key", obj) ????????????
     * QueryBuilders.termQuery("name","???????????????")
     *
     * //??????????????????multiMatchQuery("key.keyword", "*field1*"); ?????????????????????.keyword???????????????????????????    Es???????????????
     * QueryBuilders.wildcardQuery("name"/"name.keyword","*?????????*")
     *
     * //??????????????????????????????termsQuery("key", obj1, obj2..)   ?????????????????????
     * String[] a = {"a","ab","ac","qq"}
     * QueryBuilders.termsQuery("name",a)
     *
     * //????????????  ???wildcardQuery?????????????????????????????????
     * QueryBuilders.matchQuery("name","??????")
     *
     * //matchPhraseQuery?????????????????????
     * queryBuilders.matchPhraseQuery("key", value)
     *
     * //??????????????????
     * QueryBuilders.boolQuery()
     *
     * //??????????????????
     * QueryBuilders.boolQuery()
     *
     * //?????????????????? gte????????????,lte???????????? gt?????? lt??????
     * QueryBuilders.rangeQuery("age")
     *     .gte(jsonObjectEq.get("1"))
     *     .lte(jsonObjectEq.get("10")))
     * ......
     */
    @Test
    public void testConditionPageSearch() {
        String keyword = "??????";
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery("id").gt(1));
        queryBuilder.must(QueryBuilders.boolQuery()
                .should(QueryBuilders.matchPhraseQuery("title", keyword))
                .should(QueryBuilders.matchPhraseQuery("content", keyword)));
        PageVO<TProjectIndex> pageVO = elasticsearchTemplate.pageSearch(1, 20, queryBuilder, TProjectIndex.class);
        log.info("---------pageVO = {}-----------", pageVO);
    }

    @Test
    public void testAggregationSearch() {
        // ???categoryId ??????group by?????????????????????????????????
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("category").field("categoryId")
                .subAggregation(AggregationBuilders.dateHistogram("createDate").field("createTime").calendarInterval(DateHistogramInterval.MONTH).minDocCount(1))
                .size(500);
        Aggregations aggregations = elasticsearchTemplate.aggregationSearch(aggregationBuilder, TProjectIndex.class);

        List<? extends Terms.Bucket> buckets = ((ParsedStringTerms)aggregations.get("category")).getBuckets();
        for (Terms.Bucket bucket : buckets) {
            // ?????????key
            String key = (String) bucket.getKey();
            // long docCount = bucket.getDocCount();
        }
        log.info("---------aggregations = {}-----------", buckets.get(0).getDocCount());
    }

    @Test
    public void testExists() throws Exception {
        boolean exists = elasticsearchTemplate.exists(TProjectIndex.class);
        log.info("---------------exists = {}----------------", exists);
    }

    @Test
    public void testDelIndex() throws Exception {
        elasticsearchTemplate.delete(TProjectIndex.class);
        log.info("---------------del----------------");
    }

}
