package cn.microboat.controller;

import cn.microboat.domain.User;
import cn.microboat.domain.vo.ResultVo;
import cn.microboat.service.EsUserService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author zhouwei
 */
@RestController
public class EsController {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private EsUserService esUserService;

    private String[] names = {"诸葛亮", "曹操", "李白", "韩信", "赵云", "小乔", "狄仁杰", "李四", "诸小明", "王五"};

    private String[] infos = {"我来自中国的一个小乡村，地处湖南省", "我来自中国的一个大城市，名叫上海，人们称作魔都", "我来自东北，家住大囤里，一口大碴子话"};

    @GetMapping("/user/save")
    public ResultVo saveUser() {
        Random random = new Random();
        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            User user = new User();
            user.setId(i);
            user.setName(names[random.nextInt(9)]);
            user.setAge(random.nextInt(40) + i);
            user.setInfo(infos[random.nextInt(2)]);
            users.add(user);
        }
        Iterable<User> all = esUserService.saveAll(users);
        ArrayList<User> list = new ArrayList<>();
        if (all.iterator().hasNext()) {
            list.add(all.iterator().next());
        }
        return new ResultVo(list);
    }

    @GetMapping("/user/id/{id}")
    public ResultVo getDataById(@PathVariable("id") Integer id) {
        return new ResultVo(esUserService.findById(id));
    }

    @GetMapping("/user/page")
    public ResultVo getAllDataByPage() {
        Pageable page = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
        Page<User> all = esUserService.findAll(page);
        return new ResultVo(all.getContent());
    }

    @GetMapping("/user/name/{name}")
    public ResultVo getDataByName(@PathVariable("name") String name) {
        return new ResultVo(esUserService.findByName(name));
    }

    @GetMapping("/user/nameAndInfo/{name}/{info}")
    public ResultVo getDataByNameAndInfo(@PathVariable("name") String name, @PathVariable("info") String info) {
        return new ResultVo(esUserService.findByNameAndInfo(name, info));
    }

    @GetMapping("/user/highlight/{value}")
    public ResultVo getHighLightByUser(@PathVariable("value") String value) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("info", value))
                .should(QueryBuilders.matchQuery("name", value));

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withHighlightFields(
                        new HighlightBuilder.Field("info"),
                        new HighlightBuilder.Field("name"))
                .withHighlightBuilder(new HighlightBuilder().preTags("<span style='color:red'>").postTags("</span>"))
                .build();

        SearchHits<User> search = elasticsearchRestTemplate.search(searchQuery, User.class);

        List<SearchHit<User>> searchHits = search.getSearchHits();

        ArrayList<User> users = new ArrayList<>();

        for (SearchHit<User> searchHit : searchHits) {
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            searchHit.getContent().setName(highlightFields.get("name") == null ? searchHit.getContent().getName() : highlightFields.get("name").get(0));
            searchHit.getContent().setInfo(highlightFields.get("info") == null ? searchHit.getContent().getInfo() : highlightFields.get("info").get(0));
            users.add(searchHit.getContent());
        }

        return new ResultVo(users);
    }

}
